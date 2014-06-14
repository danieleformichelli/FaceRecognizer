package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LongSparseArray;

import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class LBPHFaceRecognizer extends FaceRecognizer {

	private static native long createLBPHFaceRecognizer_0();

	private static native long createLBPHFaceRecognizer_1(int radius);

	private static native long createLBPHFaceRecognizer_2(int radius,
			int neighbours);

	public LBPHFaceRecognizer() {
		super(createLBPHFaceRecognizer_0());
	}

	public LBPHFaceRecognizer(int radius) {
		super(createLBPHFaceRecognizer_1(radius));
	}

	public LBPHFaceRecognizer(int radius, int neighbours) {
		super(createLBPHFaceRecognizer_2(radius, neighbours));
	}

	private static String MODEL_FILE_NAME = "trainedModel.xml";

	private static LBPHFaceRecognizer instance;
	private static String mModelPath;
	private static Context mContext;
	private static boolean isTrained;

	public static LBPHFaceRecognizer getInstance(Context c) {
		if (instance == null) {
			System.loadLibrary("facerecognizer");
			instance = new LBPHFaceRecognizer();
			mContext = c;
			mModelPath = c.getExternalFilesDir(null).getAbsolutePath() + "/"
					+ MODEL_FILE_NAME;
			if (new File(mModelPath).exists()) {
				instance.load(mModelPath);
				isTrained = true;
			}
		}

		return instance;
	}

	public static void resetModel() {
		new File(mModelPath).delete();
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
	public void incrementalTrain(String newFacePath, int label) {
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
			update(newFaces, labels);
		else
			train(newFaces, labels);

		save(mModelPath);
	}

	/**
	 * Train with the specified dataset. If the dataset is empty, a new model is
	 * created and returned. (We cannot train a LBPHFaceRecognizer with empty
	 * dataset in OpenCV, an exception is raised)
	 * 
	 * Use like this: mFaceRecognizer = mFaceRecognizer.train(dataset);
	 * 
	 * @param dataset
	 * @return the model instance
	 */
	public void train(LongSparseArray<Person> dataset) {
		if (!isDatasetValid(dataset)) {
			resetModel();
			return;
		}

		List<Mat> faces = new ArrayList<Mat>();
		Mat labels = new Mat();
		int counter = 0;

		for (int i = 0, l = dataset.size(); i < l; i++) {
			long label = dataset.keyAt(i);
			Person person = dataset.valueAt(i);
			LongSparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, k = photos.size(); i < k; i++) {
				Photo photo = photos.valueAt(j);
				Mat m = new Mat();

				Bitmap b = photo.getBitmap();
				if (b == null)
					b = BitmapFactory.decodeFile(photo.getUrl());

				Utils.bitmapToMat(b, m);
				faces.add(m);
				labels.put(counter++, 0, new int[] { (int) label });
			}
		}

		train(faces, labels);
		save(mModelPath);
	}

	private boolean isDatasetValid(LongSparseArray<Person> dataset) {
		if (dataset == null)
			return false;

		for (int i = 0, l = dataset.size(); i < l; i++)
			if (dataset.valueAt(i).getPhotos().size() > 0)
				return true;

		return false;
	}

	@Override
	public void predict(Mat src, int[] label, double[] confidence) {
		if (isTrained)
			predict(src, label, confidence);
	}

}