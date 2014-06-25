package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
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
	private static final String MODEL_FILE_NAME = "trainedModel.xml";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";

	public enum Type {
		LBPH, EIGEN, FISHER;

		public boolean isIncrementable() {
			return this == LBPH;
		}

		public boolean needResize() {
			return this != LBPH;
		}

		public int numberOfNeededClasses() {
			switch (this) {
			case EIGEN:
				return 1;
			case FISHER:
				return 2;
			case LBPH:
				return 1;
			}

			return -1;
		}
	}

	public enum CutMode {
		NO_CUT, HORIZONTAL, VERTICAL, TOTAL
	}

	private boolean isTrained;
	private String mModelPath;
	private Context mContext;
	private Type mRecognizerType;
	private FaceRecognizer mFaceRecognizer;
	private SharedPreferences mSharedPreferences;
	private boolean normalize;
	private CutMode mCutMode;
	private double mCutPercentage;
	private int lbphRadius, lbphNeighbours, lbphGridX, lbphGridY;
	private int eigenComponents;
	private int fisherComponents;

	private Size size;

	public EIMFaceRecognizer(Context mContext, Type mRecognizerType,
			boolean normalize, CutMode mCutMode, int mCutPercentage,
			Integer... params) {
		if (mContext == null)
			throw new IllegalArgumentException("mContext cannot be null");
		if (mRecognizerType == null)
			throw new IllegalArgumentException("mRecognizerType cannot be null");
		if (mCutMode == null)
			throw new IllegalArgumentException("mCutMode cannot be null");

		this.mContext = mContext;
		this.mRecognizerType = mRecognizerType;
		this.normalize = normalize;
		this.mCutMode = mCutMode;
		this.mCutPercentage = (100 - mCutPercentage) / 100.0;

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		mModelPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/"
				+ MODEL_FILE_NAME;

		instantiateFaceRecognizer(params);

		size = new Size(mSharedPreferences.getInt(WIDTH, -1),
				mSharedPreferences.getInt(HEIGHT, -1));
		if (size.width == -1)
			size.width = Double.MAX_VALUE;
		if (size.height == -1)
			size.height = Double.MAX_VALUE;

		if (new File(mModelPath).exists()) {
			try {
				mFaceRecognizer.load(mModelPath);
			} catch (CvException e) {
				new File(mModelPath).delete();
				return;
			}
			isTrained = true;
		} else
			isTrained = false;
	}

	private void instantiateFaceRecognizer(Integer... params) {
		int paramsLength = params.length;

		switch (mRecognizerType) {
		case LBPH:
			if (paramsLength != 4)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 4");

			lbphRadius = params[0];
			lbphNeighbours = params[1];
			lbphGridX = params[2];
			lbphGridY = params[3];

			mFaceRecognizer = new LBPHFaceRecognizer(lbphRadius,
					lbphNeighbours, lbphGridX, lbphGridY, Double.MAX_VALUE);
			break;
		case EIGEN:
			if (paramsLength != 1)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 1");

			eigenComponents = params[0];

			if (eigenComponents == 0)
				mFaceRecognizer = new EigenFaceRecognizer();
			else
				mFaceRecognizer = new EigenFaceRecognizer(eigenComponents);
			break;
		case FISHER:
			if (paramsLength != 1)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 1");

			fisherComponents = params[0];
			if (fisherComponents == 0)
				mFaceRecognizer = new FisherFaceRecognizer();
			else
				mFaceRecognizer = new FisherFaceRecognizer(mModelPath,
						fisherComponents);
			break;
		default:
			throw new IllegalArgumentException("Invalid mType");
		}
	}

	/**
	 * Resets the trained model
	 */
	public void resetModel() {
		deleteModelFromDisk(mContext);
		isTrained = false;
	}

	/**
	 * Delete the model stored on the disk
	 */
	public static void deleteModelFromDisk(Context mContext) {
		String mModelPath = mContext.getExternalFilesDir(null)
				.getAbsolutePath() + "/" + MODEL_FILE_NAME;
		File mModelFile = new File(mModelPath);
		if (mModelFile != null)
			mModelFile.delete();
	}

	/**
	 * Train the recognizer with a new face.
	 * 
	 * @param newFace
	 *            the new face
	 * @param label
	 *            the id of the person related to the new face
	 */
	public void incrementalTrain(String newFacePath, int label) {
		if (!mRecognizerType.isIncrementable())
			throw new IllegalStateException("Face detector of type "
					+ mRecognizerType.toString()
					+ "cannot be trained incrementally");

		List<Mat> newFaces = new ArrayList<Mat>();
		Mat labels = new Mat(1, 1, CvType.CV_32SC1);
		Mat newFaceMat = new Mat();

		Bitmap newFace = BitmapFactory.decodeFile(newFacePath);
		if (newFace == null)
			throw new IllegalArgumentException("Cannot load the image at "
					+ newFacePath);

		Utils.bitmapToMat(newFace, newFaceMat);

		Imgproc.cvtColor(newFaceMat, newFaceMat, Imgproc.COLOR_RGB2GRAY);

		if (mRecognizerType.needResize())
			Imgproc.resize(newFaceMat, newFaceMat, size);

		preprocessImage(newFaceMat);

		newFaces.add(newFaceMat);
		labels.put(0, 0, new int[] { label });

		if (isTrained)
			mFaceRecognizer.update(newFaces, labels);
		else
			mFaceRecognizer.train(newFaces, labels);

		newFaceMat.release();
		labels.release();

		isTrained = true;
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
				incrementalTrain(mNewFacePath, mLabel);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						mFaceRecognizer.save(mModelPath);
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
	public boolean train(SparseArray<Person> people) {
		if (!isDatasetValid(people)) {
			resetModel();
			return false;
		}

		int images = 0;
		double height = 0.0;
		double width = 0.0;
		List<Mat> faces = new ArrayList<Mat>();
		List<Integer> labels = new ArrayList<Integer>();

		for (int i = 0, l = people.size(); i < l; i++) {

			int label = people.keyAt(i);
			Person person = people.valueAt(i);
			SparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, k = photos.size(); j < k; j++) {
				Photo mPhoto = photos.valueAt(j);
				Mat mMat = new Mat();

				Utils.bitmapToMat(mPhoto.getBitmap(), mMat);
				Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);

				labels.add(label);
				faces.add(mMat);

				if (mRecognizerType.needResize()) {
					Size s = mMat.size();
					height += s.height;
					width += s.width;
					images++;
				}
			}
		}

		if (mRecognizerType.needResize()) {
			size.height = height / images;
			size.width = width / images;
		}

		mSharedPreferences.edit().putInt(WIDTH, (int) size.width)
				.putInt(HEIGHT, (int) size.height).apply();

		if (mRecognizerType.needResize()) {
			Log.i(TAG, "Set size of all faces to " + size.width + "x"
					+ size.height);

			for (Mat face : faces)
				Imgproc.resize(face, face, size);
		}

		for (Mat face : faces)
			preprocessImage(face);

		Mat labelsMat = new Mat(labels.size(), 1, CvType.CV_32SC1);
		int i = 0;
		for (Integer label : labels)
			labelsMat.put(i++, 0, new int[] { label });

		mFaceRecognizer.train(faces, labelsMat);
		mFaceRecognizer.save(mModelPath);
		isTrained = true;

		for (Mat face : faces)
			face.release();
		labelsMat.release();

		return true;
	}

	public void trainWithLoading(Activity activity, SparseArray<Person> people) {
		final SparseArray<Person> mPeople = people;
		final ProgressDialog mProgressDialog = ProgressDialog.show(activity,
				"", "Training...", true);
		final Activity mActivity = activity;

		(new Thread() {
			public void run() {
				final boolean trained = train(mPeople);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						if (trained)
							mFaceRecognizer.save(mModelPath);

						mProgressDialog.dismiss();
					}
				});
			}
		}).start();
	}

	private boolean isDatasetValid(SparseArray<Person> dataset) {
		if (dataset == null
				|| dataset.size() < mRecognizerType.numberOfNeededClasses())
			return false;

		int numberOfClasses = 0;

		for (int i = 0, l = dataset.size(); i < l; i++)
			if (dataset.valueAt(i).getPhotos().size() > 0) {
				numberOfClasses++;
				if (numberOfClasses >= mRecognizerType.numberOfNeededClasses())
					return true;
				continue;
			}

		return false;
	}

	public void predict(Mat src, int[] label, double[] confidence) {
		if (isTrained) {
			if (mRecognizerType.needResize())
				Imgproc.resize(src, src, size);
			preprocessImage(src);
			mFaceRecognizer.predict(src, label, confidence);
		}
	}

	private void preprocessImage(Mat image) {
		// Illuminance normalization
		if (normalize)
			Imgproc.equalizeHist(image, image);

		// Cut
		Size imageSize = image.size();
		Rect roi = new Rect();

		switch (mCutMode) {
		case NO_CUT:
			return;
		case HORIZONTAL:
			roi.width = (int) (imageSize.width * mCutPercentage);
			roi.height = (int) imageSize.height;
			break;
		case VERTICAL:
			roi.width = (int) imageSize.width;
			roi.height = (int) (imageSize.height * mCutPercentage);
			break;
		case TOTAL:
			roi.width = (int) (imageSize.width * mCutPercentage);
			roi.height = (int) (imageSize.height * mCutPercentage);
			break;
		}

		roi.x = (image.cols() - roi.width) / 2;
		roi.y = (image.rows() - roi.height) / 2;
		image = image.submat(roi);
	}

	public Type getType() {
		return mRecognizerType;
	}
}