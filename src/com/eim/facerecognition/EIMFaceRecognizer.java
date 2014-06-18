package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

public class EIMFaceRecognizer {

	public enum Type {
		EIGEN, FISHER, LBPH;

		boolean isIncrementable() {
			return this == LBPH;
		}

		int toInt() {
			switch (this) {
			case EIGEN:
				return 0;
			case FISHER:
				return 1;
			case LBPH:
				return 2;
			default:
				return -1;
			}
		}
	}

	private static final String TAG = "EIMFaceRecognizer";

	private static String MODEL_FILE_NAME = "trainedModel.xml";

	private final static int typesCount = Type.values().length;
	
	private static EIMFaceRecognizer[] instances = new EIMFaceRecognizer[typesCount];
	private static FaceRecognizer[] faceRecognizers = new FaceRecognizer[typesCount];
	private static String[] modelPaths = new String[typesCount];
	private static boolean[] isTrained = new boolean[typesCount];
	SparseArray<Person> dataset;
	Type type;

	public static EIMFaceRecognizer getInstance(Context mContext, Type mType) {
		if (mContext == null)
			throw new IllegalArgumentException("mContext cannot be null");
		if (mType == null)
			throw new IllegalArgumentException("mType cannot be null");

		System.loadLibrary("facerecognizer");

		int index;
		switch (mType) {
		case EIGEN:
			index = 0;

			if (instances[index] != null)
				return instances[index];

			instances[index] = new EIMFaceRecognizer();
			instances[index].type = Type.EIGEN;
			faceRecognizers[index] = new EigenFaceRecognizer();
			break;
		case FISHER:
			index = 1;

			if (instances[index] != null)
				return instances[index];

			instances[index] = new EIMFaceRecognizer();
			instances[index].type = Type.FISHER;
			faceRecognizers[index] = new FisherFaceRecognizer();
			break;
		case LBPH:
			index = 2;

			if (instances[index] != null)
				return instances[index];

			instances[index] = new EIMFaceRecognizer();
			instances[index].type = Type.LBPH;
			faceRecognizers[index] = new LBPHFaceRecognizer();
			break;
		default:
			throw new IllegalArgumentException("Invalid mType");
		}

		modelPaths[index] = mContext.getExternalFilesDir(null)
				.getAbsolutePath() + "/" + index + "_" + MODEL_FILE_NAME;

		if (new File(modelPaths[index]).exists()) {
			faceRecognizers[index].load(modelPaths[index]);
			isTrained[index] = true;
		} else
			isTrained[index] = false;

		return instances[index];
	}

	/**
	 * Resets the trained model
	 */
	public static void resetModels() {
		for (int i = 0; i < typesCount; i++) {
			File mModelFile = new File(modelPaths[i]);
			if (mModelFile != null)
				mModelFile.delete();
			isTrained[i] = false;
		}
	}

	/**
	 * Resets the trained model
	 */
	public void resetModel() {
		int index = type.toInt();

		File mModelFile = new File(modelPaths[index]);
		if (mModelFile != null)
			mModelFile.delete();
		isTrained[index] = false;
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
		int index = type.toInt();

		if (!type.isIncrementable())
			throw new IllegalStateException("Face detector of type "
					+ type.toString() + "cannot be trained incrementally");

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

		if (isTrained[index])
			faceRecognizers[index].update(newFaces, labels);
		else
			faceRecognizers[index].train(newFaces, labels);

		faceRecognizers[index].save(modelPaths[index]);
	}

	/**
	 * Train with the specified dataset. If the dataset contains no faces the
	 * model is reset
	 * 
	 * @param sparseArray
	 *            faces dataset, keys are the labels and faces are contained in
	 *            the field Photos of the value
	 */
	public void train(SparseArray<Person> sparseArray) {
		int index = type.toInt();

		if (!isDatasetValid(sparseArray)) {
			resetModel();
			return;
		}

		List<Mat> faces = new ArrayList<Mat>();
		List<Integer> labels = new ArrayList<Integer>();

		int counter = 0;

		for (int i = 0, l = sparseArray.size(); i < l; i++) {
			int label = sparseArray.keyAt(i);
			Person person = sparseArray.valueAt(i);
			SparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, k = photos.size(); j < k; j++) {
				Photo mPhoto = photos.valueAt(j);
				Mat mMat = new Mat();

				Bitmap face = mPhoto.getBitmap();
				if (face == null)
					face = BitmapFactory.decodeFile(mPhoto.getUrl());

				Utils.bitmapToMat(face, mMat);
				Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);
				faces.add(mMat);
				labels.add((int) label);

				Log.d(TAG, "Inserting " + label + ":" + mPhoto.getUrl());

				// labels.put(counter++, 0, new int[] { (int) label });
			}
		}

		Mat labelsMat = new Mat(labels.size(), 1, CvType.CV_32SC1);
		for (counter = 0; counter < labelsMat.rows(); counter++)
			labelsMat.put(counter, 0, new int[] { labels.get(counter) });

		Log.i(TAG, labelsMat.dump());

		faceRecognizers[index].train(faces, labelsMat);
		faceRecognizers[index].save(modelPaths[index]);
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
		int index = type.toInt();

		if (isTrained[index])
			faceRecognizers[index].predict(src, label, confidence);
	}
}