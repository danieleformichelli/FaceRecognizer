package com.eim.facerecognition;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eim.R;
import com.eim.facedetection.FaceDetector;
import com.eim.facesmanagement.peopledb.PeopleDatabase;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.utilities.EIMPreferences;
import com.eim.utilities.FaceRecognizerMainActivity.OnOpenCVLoaded;
import com.eim.utilities.Swipeable;

public class FaceRecognitionFragment extends Fragment implements Swipeable,
		OnOpenCVLoaded, CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {
	private static final boolean multithread = false;
	private static final String TAG = "FaceRecognitionFragment";
	private static final Scalar FACE_RECT_COLOR = new Scalar(23, 150, 0, 255);
	private static final Scalar FACE_UNKNOWN_RECT_COLOR = new Scalar(240, 44,
			0, 255);

	private static int mDistanceThreshold;

	public enum Type {
		EIGEN, FISHER, LBPH
	}

	private Activity activity;

	private ControlledJavaCameraView mCameraView;

	private boolean mOpenCVLoaded = false;
	private int mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_BACK;

	private Mat mRgba;
	private Mat mSceneForRecognizer;
	private LabelledRect[] mLabelsForDrawer;

	private SparseArray<Mat> thumbnails;
	private int mThumbnailSize = 25;
	private int mHeight;

	private FaceDetector mFaceDetector;
	private EIMFaceRecognizer mFaceRecognizer;
	private PeopleDatabase mPeopleDatabase;

	private SeekBar mThresholdBar;
	private TextView mThresholdTextView;
	private ImageButton mSwitchButton;

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
		mDistanceThreshold = EIMPreferences.getInstance(activity)
				.recognitionThreshold();

		mCameraView = (ControlledJavaCameraView) activity
				.findViewById(R.id.face_recognition_surface_view);
		mCameraView.setCvCameraViewListener(this);
		mCameraView.setCameraIndex(mCurrentCameraIndex);

		mSwitchButton = (ImageButton) activity
				.findViewById(R.id.switch_camera_button);
		mSwitchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_BACK)
					mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_FRONT;
				else
					mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_BACK;
				mCameraView.disableView();
				mCameraView.setCameraIndex(mCurrentCameraIndex);
				mCameraView.enableView();
			}
		});

		mThresholdTextView = (TextView) activity
				.findViewById(R.id.threshold_text);
		mThresholdBar = (SeekBar) activity.findViewById(R.id.threshold_bar);
		mThresholdBar.setOnSeekBarChangeListener(this);
		mThresholdBar.setProgress(mDistanceThreshold);

		thumbnails = new SparseArray<Mat>();
	}

	@Override
	public void swipeOut(boolean right) {
		if (mCameraView != null)
			mCameraView.disableView();

		if (thumbnails != null) {
			for (int i = 0, l = thumbnails.size(); i < l; i++)
				thumbnails.valueAt(i).release();

			thumbnails.clear();
		}
	}

	@Override
	public void swipeIn(boolean right) {
		mFaceDetector.resetSizes();
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

		mOpenCVLoaded = false;
		super.onPause();
	}

	public void onOpenCVLoaded() {
		mOpenCVLoaded = true;
		if (mCameraView != null && getUserVisibleHint())
			mCameraView.enableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		setupFaceRecognition();

		mRgba = new Mat();
		mSceneForRecognizer = new Mat();

		mHeight = height;

		mSwitchButton.setEnabled(true);
		mThresholdBar.setEnabled(true);

		mRecognitionThread = new Thread(mRecognitionWorker);
		mRecognitionThread.start();
	}

	@Override
	public void onCameraViewStopped() {
		mSwitchButton.setEnabled(false);
		mThresholdBar.setEnabled(false);

		if (mRecognitionThread != null) {
			mRecognitionThread.interrupt();
			mRecognitionThread = null;
		}

		mSceneForRecognizer.release();
		mRgba.release();
	}

	private Thread mRecognitionThread = null;
	private Runnable mRecognitionWorker = new Runnable() {
		@Override
		public void run() {
			if (!multithread)
				return;

			while (!Thread.interrupted()) {
				Rect[] facesArray = mFaceDetector.detect(mSceneForRecognizer);
				mLabelsForDrawer = recognizeFaces(facesArray);
			}
		}
	};

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		boolean flip;

		mRgba = inputFrame.rgba();
		mSceneForRecognizer = inputFrame.gray();

		flip = (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_FRONT);
		if (flip)
			Core.flip(mRgba, mRgba, 1);

		if (multithread) {
			for (LabelledRect faceAndLabel : mLabelsForDrawer)
				drawLabel(mRgba, faceAndLabel, flip);
		} else {
			Rect[] facesArray = mFaceDetector.detect(mSceneForRecognizer);

			mLabelsForDrawer = recognizeFaces(facesArray);

			for (LabelledRect faceAndLabel : mLabelsForDrawer)
				drawLabel(mRgba, faceAndLabel, flip);
		}

		return mRgba;
	}

	private LabelledRect[] recognizeFaces(Rect[] facesArray) {

		LabelledRect[] recognizedPeople = new LabelledRect[facesArray.length];

		for (int i = 0; i < facesArray.length; i++) {
			Rect faceRect = facesArray[i];

			try {
				Mat face = mSceneForRecognizer.submat(faceRect);
				int[] predictedLabel = new int[1];
				double[] distance = new double[1];
				mFaceRecognizer.predict(face, predictedLabel, distance);
				face.release();

				Person guess = mPeopleDatabase.getPerson(predictedLabel[0]);
				if (guess == null) {
					recognizedPeople[i] = new LabelledRect(faceRect, null, null);
					continue;
				}

				if (distance[0] < mDistanceThreshold)
					recognizedPeople[i] = new LabelledRect(faceRect,
							guess.getName(), getThumbnail(predictedLabel[0]));
				else
					recognizedPeople[i] = new LabelledRect(faceRect,
							guess.getName()
									+ " "
									+ String.valueOf(((Double) distance[0])
											.intValue()), null);
			} catch (CvException e) {
				Log.e(TAG, "faceRect in " + faceRect.x + ", " + faceRect.y
						+ " " + faceRect.width + "x" + faceRect.height);
				e.printStackTrace();
			}
		}

		return recognizedPeople;
	}

	private void drawLabel(Mat frame, LabelledRect info, boolean flip) {
		if (info == null)
			return;

		Scalar color = (info.thumbnail == null) ? FACE_UNKNOWN_RECT_COLOR
				: FACE_RECT_COLOR;

		// Bounding box
		if (flip)
			info.rect.x = frame.cols() - (info.rect.x + info.rect.width);
		Core.rectangle(frame, info.rect.tl(), info.rect.br(), color, 3);

		if (info.text == null)
			return;

		// Text...
		int fontFace = Core.FONT_HERSHEY_PLAIN;
		double fontScale = 4;
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

		Core.putText(frame, info.text, textOrigin, fontFace, fontScale, color,
				thickness);

		// Thumbnail
		if (info.thumbnail != null) {
			Rect thumbnailPosition = new Rect(info.rect.x, info.rect.y,
					mThumbnailSize, mThumbnailSize);
			info.thumbnail.copyTo(frame.submat(thumbnailPosition));
		}

	}

	private Mat getThumbnail(int id) {
		if (thumbnails.get(id) != null)
			return thumbnails.get(id);

		// TEST THUMBNAIL LOADING
		Bitmap mBitmap = PeopleDatabase.getInstance(activity).getPerson(id)
				.getPhotos().valueAt(0).getBitmap();
		Mat thumbnail = new Mat();
		Mat transparentThumbnail = new Mat();
		Utils.bitmapToMat(mBitmap, thumbnail);
		Mat newThumbnail = new Mat();

		double absoluteFaceSize = mHeight
				* mFaceDetector.getMinRelativeFaceSize();
		mThumbnailSize = (int) (absoluteFaceSize * 0.6);
		Core.subtract(thumbnail, new Scalar(0, 0, 0, 100), transparentThumbnail);

		Imgproc.resize(transparentThumbnail, newThumbnail, new Size(
				mThumbnailSize, mThumbnailSize));
		thumbnail.release();
		transparentThumbnail.release();

		thumbnails.put(id, newThumbnail);
		return newThumbnail;
	}

	private void setupFaceRecognition() {
		mFaceDetector = FaceDetector.getInstance(activity);
		mPeopleDatabase = PeopleDatabase.getInstance(activity);
		EIMPreferences mPreferences = EIMPreferences.getInstance(activity);
		EIMFaceRecognizer.Type mRecognitionType = mPreferences
				.recognitionType();

		switch (mRecognitionType) {
		case LBPH:
			int radius = mPreferences.LBPHRadius();
			int neighbours = mPreferences.LBPHNeighbours();
			int gridX = mPreferences.LBPHGridX();
			int gridY = mPreferences.LBPHGridY();
			mFaceRecognizer = EIMFaceRecognizer.getInstance(activity,
					mRecognitionType, radius, neighbours, gridX, gridY);
			break;
		case EIGEN:
		case FISHER:
			int components = mPreferences.EigenComponents();
			mFaceRecognizer = EIMFaceRecognizer.getInstance(activity,
					mRecognitionType, components);
			break;
		default:
			throw new IllegalArgumentException("invalid recognition type");
		}
	}

	public class LabelledRect {
		public LabelledRect(Rect rect, String text, Mat thumbnail) {
			super();
			this.rect = rect;
			this.text = text;
			this.thumbnail = thumbnail;
		}

		public Rect rect;
		public String text;
		public Mat thumbnail;
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		mDistanceThreshold = arg1;
		mThresholdTextView.setText(String.valueOf(mDistanceThreshold));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}
}
