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

	public FaceDetector(Context context, Type detectorType,
			Classifier classifier, double scaleFactor, int minNeighbors,
			double minRelativeFaceSize, double maxRelativeFaceSize) {

		mContext = context;
		mDetectorType = detectorType;
		mClassifier = classifier;
		mScaleFactor = scaleFactor;
		mMinNeighbors = minNeighbors;
		mMinRelativeFaceSize = minRelativeFaceSize;
		mMaxRelativeFaceSize = maxRelativeFaceSize;

		loadCascadeFile();

		mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

		if (mJavaDetector.empty()) {
			Log.e(TAG, "Failed to load cascade classifier");
			mJavaDetector = null;
		} else {
			Log.i(TAG,
					"Loaded cascade classifier from "
							+ mCascadeFile.getAbsolutePath());
		}

		System.loadLibrary("nativedetector");
		mNativeDetector = new DetectionBasedTracker(
				mCascadeFile.getAbsolutePath(), 0);
	}

	private void loadCascadeFile() {

		File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
		mCascadeFile = new File(cascadeDir, mClassifier.toString().toLowerCase(
				Locale.US)
				+ ".xml");

		if (!mCascadeFile.exists()) {
			// load cascade file from application resources
			try {
				int resId;

				switch (mClassifier) {
				case HAARCASCADE_FRONTALFACE_ALT:
					resId = R.raw.haarcascade_frontalface_alt;
					break;
				case HAARCASCADE_FRONTALFACE_ALT2:
					resId = R.raw.haarcascade_frontalface_alt2;
					break;
				case HAARCASCADE_FRONTALFACE_ALT_TREE:
					resId = R.raw.haarcascade_frontalface_alt_tree;
					break;
				case HAARCASCADE_FRONTALFACE_DEFAULT:
					resId = R.raw.haarcascade_frontalface_default;
					break;
				case LBPCASCADE_FRONTALFACE:
					resId = R.raw.lbpcascade_frontalface;
					break;
				default:
					Log.e(TAG, "classifier is null!");
					return;
				}

				InputStream is = mContext.getResources().openRawResource(resId);

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
		}

		switch (mDetectorType) {
		case JAVA:
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(scene, faces, mScaleFactor,
						mMinNeighbors, 0, new Size(mMinAbsoluteFaceSize,
								mMinAbsoluteFaceSize), new Size(
								mMaxAbsoluteFaceSize, mMaxAbsoluteFaceSize));
			break;
		case NATIVE:
			if (mNativeDetector != null)
				mNativeDetector.detect(scene, faces);
			break;
		}

		return faces.toArray();
	}
}
