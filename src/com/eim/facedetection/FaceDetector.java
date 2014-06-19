package com.eim.facedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.util.Log;

import com.eim.R;
import com.eim.utilities.EIMPreferences;

public class FaceDetector {
	private static final String TAG = "FaceDetector";

	public enum Type {
		JAVA, NATIVE
	}

	public enum Classifier {
		LBPCASCADE_FRONTALFACE, HAARCASCADE_FRONTALFACE_DEFAULT, HAARCASCADE_FRONTALFACE_ALT, HAARCASCADE_FRONTALFACE_ALT2, HAARCASCADE_FRONTALFACE_ALT_TREE
	}

	private Context mContext;

	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	private Type mDetectorType;
	private Classifier mClassifier;

	private double mScaleFactor;
	private int mMinNeighbors;

	private long mMinAbsoluteFaceSize = 0;
	private double mMinRelativeFaceSize;

	private double mMaxAbsoluteFaceSize = 0;
	private double mMaxRelativeFaceSize;

	private static FaceDetector instance = null;

	public static FaceDetector getInstance(Context c) {
		if (instance == null)
			instance = new FaceDetector(c);

		return instance;
	}

	private FaceDetector(Context c) {
		mContext = c;
		mDetectorType = EIMPreferences.getInstance(c).detectorType();
		mClassifier = EIMPreferences.getInstance(c).detectorClassifier();
		mScaleFactor = EIMPreferences.getInstance(c).detectionScaleFactor();
		mMinNeighbors = EIMPreferences.getInstance(c).detectionMinNeighbors();
		mMinRelativeFaceSize = EIMPreferences.getInstance(c).detectionMinRelativeFaceSize();
		mMaxRelativeFaceSize = EIMPreferences.getInstance(c).detectionMaxRelativeFaceSize();;
		initDetector();
	}

	private void initDetector() {
		loadCascadeFile();

		mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
		if (mJavaDetector.empty()) {
			Log.e(TAG, "Failed to load cascade classifier");
			mJavaDetector = null;
		} else
			Log.i(TAG,
					"Loaded cascade classifier from "
							+ mCascadeFile.getAbsolutePath());

		System.loadLibrary("nativedetector");
		mNativeDetector = new DetectionBasedTracker(
				mCascadeFile.getAbsolutePath(), 0);
	}

	private void loadCascadeFile() {
		File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
		mCascadeFile = new File(cascadeDir, mClassifier.toString()
				.toLowerCase(Locale.US) + ".xml");
		
		if (!mCascadeFile.exists()) {
			// load cascade file from application resources
			try {
				InputStream is = mContext.getResources().openRawResource(
						R.raw.lbpcascade_frontalface);

				FileOutputStream os = new FileOutputStream(mCascadeFile);

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1)
					os.write(buffer, 0, bytesRead);

				is.close();
				os.close();
			} catch (IOException e) {
				mCascadeFile.delete();
				mCascadeFile = null;
				e.printStackTrace();
			}
		}
	}

	public FaceDetector.Classifier Classifier() {
		return mClassifier;
	}

	public void setClassifier(Classifier classifier) {
		 this.mClassifier = classifier;
		 initDetector();
	}

	public Type getDetectorType() {
		return mDetectorType;
	}

	public void setDetectorType(Type detectorType) {
		mDetectorType = detectorType;
	}

	public double getMinRelativeFaceSize() {
		return mMinRelativeFaceSize;
	}

	public void setMinRelativeFaceSize(double d) {
		if (d > 1 || d < 0)
			throw new IllegalArgumentException(
					"Argument must be between 0 and 1");

		mMinRelativeFaceSize = d;
		mMinAbsoluteFaceSize = 0;
	}

	public double getMaxRelativeFaceSize() {
		return mMaxRelativeFaceSize;
	}

	public void setMaxRelativeFaceSize(double d) {
		if (d > 1 || d < 0)
			throw new IllegalArgumentException(
					"Argument must be between 0 and 1");

		mMaxRelativeFaceSize = d;
		mMaxAbsoluteFaceSize = 0;
	}

	public double getScaleFactor() {
		return mScaleFactor;
	}

	public void setScaleFactor(double mScaleFactor) {
		this.mScaleFactor = mScaleFactor;
	}

	public int getMinNeighbors() {
		return mMinNeighbors;
	}

	public void setMinNeighbors(int mMinNeighbors) {
		this.mMinNeighbors = mMinNeighbors;
	}

	public Rect[] detect(Mat scene) {

		MatOfRect faces = new MatOfRect();

		if (mMinAbsoluteFaceSize == 0) {

			int height = scene.rows();
			if (Math.round(height * mMinRelativeFaceSize) > 0)
				mMinAbsoluteFaceSize = Math
						.round(height * mMinRelativeFaceSize);

			mNativeDetector.setMinFaceSize((int) mMinAbsoluteFaceSize);
		}

		if (mMaxAbsoluteFaceSize == 0) {

			int height = scene.rows();
			if (Math.round(height * mMaxRelativeFaceSize) > 0)
				mMaxAbsoluteFaceSize = Math
						.round(height * mMaxRelativeFaceSize);

			mNativeDetector.setMinFaceSize((int) mMinAbsoluteFaceSize);
		}

		if (mDetectorType == Type.JAVA) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(scene, faces, mScaleFactor,
						mMinNeighbors,
						2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mMinAbsoluteFaceSize, mMinAbsoluteFaceSize),
						new Size(mMaxAbsoluteFaceSize, mMaxAbsoluteFaceSize));
		} else if (mDetectorType == Type.NATIVE) {
			if (mNativeDetector != null)
				mNativeDetector.detect(scene, faces);
		} else {
			Log.e(TAG, "Detection method is not selected!");
		}

		return faces.toArray();

	}

}
