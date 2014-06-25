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

	private Size mMinAbsoluteFaceSize;
	private double mMinRelativeFaceSize;

	private Size mMaxAbsoluteFaceSize;
	private double mMaxRelativeFaceSize;

	public FaceDetector(Context context, Type detectorType,
			Classifier classifier, double scaleFactor, int minNeighbors,
			double minRelativeFaceSize, double maxRelativeFaceSize) {
		if (context == null)
			throw new IllegalArgumentException("context cannot be null");
		if (detectorType == null)
			throw new IllegalArgumentException("detectorType cannot be null");
		if (classifier == null)
			throw new IllegalArgumentException("classifier cannot be null");

		mContext = context;
		mDetectorType = detectorType;
		mClassifier = classifier;
		mScaleFactor = scaleFactor;
		mMinNeighbors = minNeighbors;
		mMinRelativeFaceSize = minRelativeFaceSize;
		mMaxRelativeFaceSize = maxRelativeFaceSize;

		loadCascadeFile();

		switch (mDetectorType) {
		case JAVA:
			mJavaDetector = new CascadeClassifier(
					mCascadeFile.getAbsolutePath());

			if (mJavaDetector.empty())
				throw new IllegalArgumentException(
						"Failed to load cascade classifier "
								+ classifier.toString());
			break;
		case NATIVE:
			mNativeDetector = new DetectionBasedTracker(
					mCascadeFile.getAbsolutePath(), 0);
			break;
		}
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

		if (mMinAbsoluteFaceSize == null) {
			final double minSize = (scene.cols() <= scene.rows() ? scene.cols()
					: scene.rows());
			final double minRelativeSize = minSize * mMinRelativeFaceSize;
			final double maxRelativeSize = minSize * mMaxRelativeFaceSize;

			switch (mDetectorType) {
			case JAVA:
				mMinAbsoluteFaceSize = new Size(minRelativeSize,
						minRelativeSize);
				mMaxAbsoluteFaceSize = new Size(maxRelativeSize,
						maxRelativeSize);
				break;
			case NATIVE:
				mNativeDetector.setMinFaceSize((int) minRelativeSize);
				break;
			}
		}

		switch (mDetectorType) {
		case JAVA:
			mJavaDetector.detectMultiScale(scene, faces, mScaleFactor,
					mMinNeighbors, 0, mMinAbsoluteFaceSize,
					mMaxAbsoluteFaceSize);
			break;
		case NATIVE:
			mNativeDetector.detect(scene, faces);
			break;
		}

		return faces.toArray();
	}
}
