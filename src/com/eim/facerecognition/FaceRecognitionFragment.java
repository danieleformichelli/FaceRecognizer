package com.eim.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.eim.R;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;

public class FaceRecognitionFragment extends Fragment implements Swipeable,
		CvCameraViewListener2, FaceRecognizerMainActivity.OpenCVLoadedCallback {

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

	private static final String TAG = "FaceRecognitionFragment";

	private static final Scalar FACE_RECT_COLOR = new Scalar(255, 192, 100, 255);

	private ControlledJavaCameraView mCameraView;

	private boolean mOpenCVLoaded = false;

	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;

	private Mat mGray;
	private Mat mRgba;

	private long mAbsoluteFaceSize = 0;
	private double mRelativeFaceSize = 0.3;

	private Mat mTestThumbnail;
	private int mThumbnailSize = 25;

	/**
	 * Called when the fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_face_recognition, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// XXX da gestire in uscita?
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setupFaceDetection();

		mCameraView = (ControlledJavaCameraView) getActivity().findViewById(
				R.id.face_recognition_surface_view);

		mCameraView.setCvCameraViewListener(this);
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void swipeOut(boolean right) {
		mCameraView.disableView();
	}

	@Override
	public void swipeIn(boolean right) {
		mCameraView.enableView();
	}

	public void onResume() {
		super.onResume();
		if (mOpenCVLoaded)
			mCameraView.enableView();
	}

	@Override
	public void onPause() {

		if (mCameraView != null)
			mCameraView.disableView();

		super.onPause();
	}

	@Override
	public void onOpenCVLoaded() {
		mOpenCVLoaded = true;
		if (mCameraView != null)
			mCameraView.enableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		/*
		 * for (Size s: mCameraView.getResolutionList()) if (s.width == 320) {
		 * mCameraView.setResolution(s); break; }
		 */
		mGray = new Mat();
		mRgba = new Mat();
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

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			// mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
			
			// TEST THUMBNAIL LOADING
			Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.barbetta);
			Mat thumbnail = new Mat();
			Mat transparentThumbnail = new Mat();
			Utils.bitmapToMat(thumb, thumbnail);
			mTestThumbnail = new Mat();
						
			mThumbnailSize = (int) (mAbsoluteFaceSize * 0.6);
			Core.subtract(thumbnail, new Scalar(0,0,0,100), transparentThumbnail);
						
			Imgproc.resize(transparentThumbnail, mTestThumbnail, new Size(mThumbnailSize, mThumbnailSize));
			thumbnail.release();
			transparentThumbnail.release();
		}

		MatOfRect faces = new MatOfRect();

		if (true) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2,
						2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
						new Size());
		}
		// else if (mDetectorType == NATIVE_DETECTOR) {
		// if (mNativeDetector != null)
		// mNativeDetector.detect(mGray, faces);
		// }
		// else {
		// Log.e(TAG, "Detection method is not selected!");
		// }

		Rect[] facesArray = faces.toArray();

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

		Rect thumbnailPosition = new Rect(info.rect.x, info.rect.y, mThumbnailSize, mThumbnailSize);
		
		mTestThumbnail.copyTo(frame.submat(thumbnailPosition));
		
	}

	private List<LabelledRect> recognizeFaces(Rect[] facesArray) {

		List<LabelledRect> recognizedPeople = new ArrayList<LabelledRect>();

		for (Rect faceRect : facesArray)
			recognizedPeople.add(new LabelledRect(faceRect, "Tizio", null));

		return recognizedPeople;
	}

	private void setupFaceDetection() {
		try {
			// load cascade file from application resources
			InputStream is = getResources().openRawResource(
					R.raw.lbpcascade_frontalface);
			File cascadeDir = getActivity().getDir("cascade",
					Context.MODE_PRIVATE);
			mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(mCascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();

			mJavaDetector = new CascadeClassifier(
					mCascadeFile.getAbsolutePath());
			if (mJavaDetector.empty()) {
				Log.e(TAG, "Failed to load cascade classifier");
				mJavaDetector = null;
			} else
				Log.i(TAG,
						"Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

			// mNativeDetector = new
			// DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
			cascadeDir.delete();

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
	}
}
