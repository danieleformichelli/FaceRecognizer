package com.eim.facedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	private Context mContext;

	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	private Type mDetectorType = Type.NATIVE;

	private double mScaleFactor = 1.1;
	private int mMinNeighbors = 2;

	private long mMinAbsoluteFaceSize = 0;
	private double mMinRelativeFaceSize = 0.2;

	private double mMaxAbsoluteFaceSize = 0;
	private double mMaxRelativeFaceSize = 1;

	private static FaceDetector instance = null;
	
	public static FaceDetector getInstance(Context c) {
		if (instance == null)
			instance = new FaceDetector(c);
		return instance;
	}
	
	protected FaceDetector(Context c) {
		mContext = c;
		initDetector();
	}

	private void initDetector() {
		try {
			File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
			mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");

			if (!mCascadeFile.exists()) {
				// load cascade file from application resources
				InputStream is = mContext.getResources().openRawResource(
						R.raw.lbpcascade_frontalface);

				FileOutputStream os = new FileOutputStream(mCascadeFile);

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
				is.close();
				os.close();
			}

			mJavaDetector = new CascadeClassifier(
					mCascadeFile.getAbsolutePath());
			if (mJavaDetector.empty()) {
				Log.e(TAG, "Failed to load cascade classifier");
				mJavaDetector = null;
			} else
				Log.i(TAG,
						"Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

			System.loadLibrary("nativedetector");
			mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
			
			cascadeDir.delete();

			loadParamsFromPreferences();

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
	}

	public void loadParamsFromPreferences() {

		EIMPreferences appPrefs = EIMPreferences.getInstance(mContext);

		setMinNeighbors(appPrefs.detectionMinNeighbors());
		setMinRelativeFaceSize(appPrefs.detectionMinRelativeFaceSize());
		setMaxRelativeFaceSize(appPrefs.detectionMaxRelativeFaceSize());
		setScaleFactor(appPrefs.detectionScaleFactor());
		setDetectorType(appPrefs.detectorType());

	}
	// TODO
	public Object getClassifier() {
		return null;
	}
	// TODO
	public void setClassifier(Object classifier) {
		return;
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
