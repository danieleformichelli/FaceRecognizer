package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

public class EIMFaceRecognizer {
	private static final String TAG = "EIMFaceRecognizer";
	private static final String MIN_WIDTH = "min_width";
	private static final String MIN_HEIGHT = "min_height";

	public enum Type {
		EIGEN, FISHER, LBPH;

		public boolean isIncrementable() {
			return this == LBPH;
		}

		public boolean needResize() {
			return this != LBPH;
		}
	}

	private static EIMFaceRecognizer instance;
	private static String MODEL_FILE_NAME = "trainedModel.xml";
	private boolean isTrained;
	private String mModelPath;
	private Type mRecognizerType;
	private FaceRecognizer mFaceRecognizer;
	private SharedPreferences mSharedPreferences;

	private Size size;

	private EIMFaceRecognizer(Context mContext, Type mType) {
		System.loadLibrary("facerecognizer");

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		mRecognizerType = mType;

		switch (mType) {
		case EIGEN:
			mFaceRecognizer = new EigenFaceRecognizer();
			break;
		case FISHER:
			mFaceRecognizer = new FisherFaceRecognizer();
			break;
		case LBPH:
			mFaceRecognizer = new LBPHFaceRecognizer();
			break;
		default:
			throw new IllegalArgumentException("Invalid mType");
		}

		size = new Size(mSharedPreferences.getInt(MIN_WIDTH, -1),
				mSharedPreferences.getInt(MIN_HEIGHT, -1));
		if (size.width == -1)
			size.width = Double.MAX_VALUE;
		if (size.height == -1)
			size.height = Double.MAX_VALUE;

		Log.e(TAG, size.width + "," + size.height);

		mModelPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/"
				+ MODEL_FILE_NAME;

		if (new File(mModelPath).exists()) {
			mFaceRecognizer.load(mModelPath);
			isTrained = true;
		}
	}

	public static EIMFaceRecognizer getInstance(Context mContext, Type mType) {
		if (mContext == null)
			throw new IllegalArgumentException("mContext cannot be null");
		if (mType == null)
			throw new IllegalArgumentException("mType cannot be null");

		if (instance != null) {
			if (instance.mRecognizerType == mType)
				return instance;
			else
				instance.resetModel();
		}

		instance = new EIMFaceRecognizer(mContext.getApplicationContext(),
				mType);

		return instance;
	}

	/**
	 * Resets the trained model
	 */
	public void resetModel() {
		File mModelFile = new File(mModelPath);
		if (mModelFile != null)
			mModelFile.delete();
		isTrained = false;
	}

	/**
	 * Train the recognizer with a new face.
	 * 
	 * @param newFace
	 *            the new face
	 * @param label
	 *            the id of the person related to the new face
	 */
	private void incrementalTrain(String newFacePath, int label) {
		if (!mRecognizerType.isIncrementable())
			throw new IllegalStateException("Face detector of type "
					+ mRecognizerType.toString()
					+ "cannot be trained incrementally");

		// TODO in case isIncrementable != !needResize it doesn't work

		List<Mat> newFaces = new ArrayList<Mat>();
		Mat labels = new Mat(1, 1, CvType.CV_32SC1);
		Mat newFaceMat = new Mat();

		Bitmap newFace = BitmapFactory.decodeFile(newFacePath);
		if (newFace == null)
			return;

		Utils.bitmapToMat(newFace, newFaceMat);

		Imgproc.cvtColor(newFaceMat, newFaceMat, Imgproc.COLOR_RGB2GRAY);

		newFaces.add(newFaceMat);
		labels.put(0, 0, new int[] { label });

		if (isTrained)
			mFaceRecognizer.update(newFaces, labels);
		else
			mFaceRecognizer.train(newFaces, labels);

		mFaceRecognizer.save(mModelPath);
	}

	public void incrementalTrainWithLoading(Activity activity,
			String newFacePath, int label) {
		final String mNewFacePath = newFacePath;
		final int mLabel = label;
		final ProgressDialog mProgressDialog = ProgressDialog.show(activity,
				"", "Training...", true);
		final Activity mActivity = activity;

		(new Thread() {
			public void run() {
				EIMFaceRecognizer.this.incrementalTrain(mNewFacePath, mLabel);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						mProgressDialog.dismiss();
					}
				});
			}
		}).start();
	}

	/**
	 * Train with the specified dataset. If the dataset contains no faces the
	 * model is reset
	 * 
	 * @param people
	 *            faces dataset, keys are the labels and faces are contained in
	 *            the field Photos of the value
	 */
	private void train(SparseArray<Person> people) {

		if (!isDatasetValid(people)) {
			resetModel();
			return;
		}

		Log.i(TAG, "Training!");
		List<Mat> faces = new ArrayList<Mat>();
		List<Integer> labels = new ArrayList<Integer>();

		for (int i = 0, l = people.size(); i < l; i++) {

			int label = people.keyAt(i);
			Person person = people.valueAt(i);
			SparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, k = photos.size(); j < k; j++) {
				Photo mPhoto = photos.valueAt(j);
				Mat mMat = new Mat();

				Bitmap face = mPhoto.getBitmap();
				if (face == null)
					face = BitmapFactory.decodeFile(mPhoto.getUrl());

				Utils.bitmapToMat(face, mMat);
				Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);

				if (mRecognizerType.needResize()) {
					Size s = mMat.size();
					if (s.height < size.height)
						size.height = s.height;
					if (s.width < size.width)
						size.width = s.width;
				}

				labels.add(label);
				faces.add(mMat);
			}
		}

		mSharedPreferences.edit().putInt(MIN_WIDTH, (int) size.width)
				.putInt(MIN_HEIGHT, (int) size.height).apply();

		Log.e(TAG, size.width + "," + size.height);

		// for EIGEN and FISHER

		if (mRecognizerType.needResize()) {
			Log.i(TAG, "Set size of all faces to " + size.width + "x"
					+ size.height);

			for (Mat face : faces)
				Imgproc.resize(face, face, size);
		}

		Mat labelsMat = new Mat(labels.size(), 1, CvType.CV_32SC1);
		int i = 0;
		for (Integer label : labels)
			labelsMat.put(i++, 0, new int[] { label });

		Log.i(TAG, labelsMat.dump());

		mFaceRecognizer.train(faces, labelsMat);
		mFaceRecognizer.save(mModelPath);

		isTrained = true;
	}

	public void trainWithLoading(Activity activity, SparseArray<Person> people) {
		final SparseArray<Person> mPeople = people;
		final ProgressDialog mProgressDialog = ProgressDialog.show(activity,
				"", "Training...", true);
		final Activity mActivity = activity;

		(new Thread() {
			public void run() {
				EIMFaceRecognizer.this.train(mPeople);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						mProgressDialog.dismiss();
					}
				});
			}
		}).start();
	}

	private boolean isDatasetValid(SparseArray<Person> dataset) {
		if (dataset == null)
			return false;

		for (int i = 0, l = dataset.size(); i < l; i++)
			if (dataset.valueAt(i).getPhotos().size() > 0)
				return true;

		return false;
	}

	public void predict(Mat src, int[] label, double[] confidence) {
		Log.i(TAG, "Try prediction image. Type = " + mRecognizerType.name());
		if (isTrained) {
			if (mRecognizerType.needResize())
				Imgproc.resize(src, src, size);

			mFaceRecognizer.predict(src, label, confidence);
		}
	}

	public Type getType() {
		return mRecognizerType;
	}

	public void setType(Type mType) {
		if (mType == null)
			return;

		switch (mType) {

		case EIGEN:
			mFaceRecognizer = new EigenFaceRecognizer();
			break;
		case FISHER:
			mFaceRecognizer = new FisherFaceRecognizer();
			break;
		case LBPH:
			mFaceRecognizer = new LBPHFaceRecognizer();
			break;
		default:
			throw new IllegalArgumentException("Invalid mType");
		}

		mRecognizerType = mType;

		resetModel();
	}

}