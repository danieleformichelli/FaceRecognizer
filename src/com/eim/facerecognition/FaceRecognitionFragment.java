package com.eim.facerecognition;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eim.R;
import com.eim.facedetection.FaceDetector;
import com.eim.facesmanagement.peopledb.PeopleDatabase;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.utilities.FaceRecognizerMainActivity.OnOpenCVLoaded;
import com.eim.utilities.EIMPreferences;
import com.eim.utilities.Swipeable;

public class FaceRecognitionFragment extends Fragment implements Swipeable,
		OnOpenCVLoaded, CvCameraViewListener2 {
	private static final String TAG = "FaceRecognitionFragment";
	private static final Scalar FACE_RECT_COLOR = new Scalar(255, 192, 100, 255);
	private static final Double CONFIDENCE_THRESHOLD = 0.0;

	private Activity activity;

	private ControlledJavaCameraView mCameraView;

	private boolean mOpenCVLoaded = false;

	private Mat mGray;
	private Mat mRgba;

	private Mat mTestThumbnail;
	private int mThumbnailSize = 25;

	private FaceDetector mFaceDetector;
	private EIMFaceRecognizer mFaceRecognizer;
	private EIMFaceRecognizer.Type mFaceRecognizerType;
	private PeopleDatabase mPeopleDatabase;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_face_recognition, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();

		mCameraView = (ControlledJavaCameraView) activity
				.findViewById(R.id.face_recognition_surface_view);
		mCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void swipeOut(boolean right) {
		if (mCameraView != null)
			mCameraView.disableView();
	}

	@Override
	public void swipeIn(boolean right) {
		if (mCameraView != null)
			mCameraView.enableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mOpenCVLoaded && getUserVisibleHint())
			mCameraView.enableView();
	}

	@Override
	public void onPause() {
		if (mCameraView != null)
			mCameraView.disableView();

		super.onPause();
	}

	public void onOpenCVLoaded() {
		mOpenCVLoaded = true;
		if (mCameraView != null && getUserVisibleHint())
			mCameraView.enableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		setupFaceDetection();

		mGray = new Mat();
		mRgba = new Mat();

		// TEST THUMBNAIL LOADING
		Bitmap thumb = BitmapFactory.decodeResource(getResources(),
				R.drawable.barbetta);
		Mat thumbnail = new Mat();
		Mat transparentThumbnail = new Mat();
		Utils.bitmapToMat(thumb, thumbnail);
		mTestThumbnail = new Mat();

		double absoluteFaceSize = height
				* mFaceDetector.getMinRelativeFaceSize();
		mThumbnailSize = (int) (absoluteFaceSize * 0.6);
		Core.subtract(thumbnail, new Scalar(0, 0, 0, 100), transparentThumbnail);

		Imgproc.resize(transparentThumbnail, mTestThumbnail, new Size(
				mThumbnailSize, mThumbnailSize));
		thumbnail.release();
		transparentThumbnail.release();
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
		mTestThumbnail.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		Rect[] facesArray = mFaceDetector.detect(mGray);

		List<LabelledRect> labelledFaces = recognizeFaces(facesArray);

		for (LabelledRect faceAndLabel : labelledFaces)
			drawLabel(mRgba, faceAndLabel);

		return mRgba;
	}

	private void drawLabel(Mat frame, LabelledRect info) {

		// Bounding box
		Core.rectangle(frame, info.rect.tl(), info.rect.br(), FACE_RECT_COLOR,
				3);

		// Text...
		double fontScale = 6;
		int fontFace = Core.FONT_HERSHEY_PLAIN;
		int thickness = 3;

		Size textSize = Core.getTextSize(info.text, fontFace, fontScale,
				thickness, null);

		// ... under the box centered ...
		Point textOrigin = new Point();
		textOrigin.x = info.rect.tl().x - (textSize.width - info.rect.width)
				/ 2;
		textOrigin.y = info.rect.br().y + textSize.height + 20;

		// ... with semi-transparent white background rectngle
		double padding = 20;
		Point rectangleTL = new Point(textOrigin.x, textOrigin.y
				- textSize.height);
		Point rectangleBR = new Point(textOrigin.x + textSize.width,
				textOrigin.y);

		rectangleTL.x -= padding;
		rectangleTL.y -= padding;

		rectangleBR.x += padding;
		rectangleBR.y += padding;

		Core.rectangle(frame, rectangleTL, rectangleBR, new Scalar(255, 255,
				255, 150), Core.FILLED);

		Core.putText(frame, info.text, textOrigin, fontFace, fontScale,
				FACE_RECT_COLOR, thickness);

		// Thumbnail

		Rect thumbnailPosition = new Rect(info.rect.x, info.rect.y,
				mThumbnailSize, mThumbnailSize);

		mTestThumbnail.copyTo(frame.submat(thumbnailPosition));

	}

	private List<LabelledRect> recognizeFaces(Rect[] facesArray) {

		List<LabelledRect> recognizedPeople = new ArrayList<LabelledRect>();

		for (Rect faceRect : facesArray) {
			Mat face = mGray.submat(faceRect);
			int[] predictedLabel = new int[1];
			double[] confidence = new double[1];
			mFaceRecognizer.predict(face, predictedLabel, confidence);
			if (confidence[0] > CONFIDENCE_THRESHOLD) {
				// LabelledRect l = getInfoFromLabel(prediction.first);

				Person guess = mPeopleDatabase.getPerson(predictedLabel[0]);
				Log.d(TAG, "Prediction: " + predictedLabel[0] + " ("
						+ confidence[0] + ")");
				if (guess != null)
					recognizedPeople.add(new LabelledRect(faceRect, guess
							.getName(), guess.getPhotos().get(0)));
			}
		}

		return recognizedPeople;
	}

	private void setupFaceDetection() {
		mFaceDetector = new FaceDetector(activity);
		mFaceRecognizerType = EIMPreferences.getInstance(activity)
				.recognitionType();
		mFaceRecognizer = EIMFaceRecognizer.getInstance(activity,
				mFaceRecognizerType);
		mPeopleDatabase = PeopleDatabase.getInstance(activity);
	}

	public class LabelledRect {
		public LabelledRect(Rect rect, String text, Object thumbnail) {
			super();
			this.rect = rect;
			this.text = text;
			this.thumbnail = thumbnail;
		}

		public Rect rect;
		public String text;
		public Object thumbnail;
	}
}
