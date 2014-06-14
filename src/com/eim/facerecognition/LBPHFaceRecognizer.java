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

	public static LBPHFaceRecognizer getInstance(Context c) {
		if (instance == null) {
			System.loadLibrary("facerecognizer");
			instance = new LBPHFaceRecognizer();
			mContext = c;
			mModelPath = c.getExternalFilesDir(null).getAbsolutePath() + "/"
					+ MODEL_FILE_NAME;
			if (new File(mModelPath).exists())
				instance.load(mModelPath);
		}
		return instance;
	}

	public static LBPHFaceRecognizer getNewInstance(Context c) {
		if (mModelPath == null)
			mModelPath = c.getExternalFilesDir(null).getAbsolutePath() + "/"
					+ MODEL_FILE_NAME;
		resetModel();
		return getInstance(c);
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
		ArrayList<Mat> newFaces = new ArrayList<Mat>();
		Mat labels = new Mat(1,1, CvType.CV_32SC1);
		Mat newFaceMat = new Mat();

		Bitmap newFace = BitmapFactory.decodeFile(newFacePath);
		Utils.bitmapToMat(newFace, newFaceMat);
		
		Imgproc.cvtColor(newFaceMat, newFaceMat, Imgproc.COLOR_RGB2GRAY); 

		newFaces.add(newFaceMat);
		labels.put(0, 0, new int[] { label });

		update(newFaces, labels);
		save(mModelPath);
	}

	/**
	 * Train with the specified dataset. If the dataset is empty, a new model is created and returned.
	 * (We cannot train a LBPHFaceRecognizer with empty dataset in OpenCV, an exception is raised)
	 * 
	 * Use like this:
	 * mFaceRecognizer = mFaceRecognizer.train(dataset); 
	 * 
	 * @param dataset
	 * @return the model instance
	 */
	public LBPHFaceRecognizer train(LongSparseArray<Person> dataset) {

		if (dataset.size() == 0)
			return getNewInstance(mContext);

		List<Mat> faces = new ArrayList<Mat>();
		Mat labels = new Mat();
		int counter = 0;

		for (int i = 0; i < dataset.size(); i++) {
			long label = dataset.keyAt(i);
			Person person = dataset.valueAt(i);
			LongSparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, l = photos.size(); i < l; i++) {
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
		return this;
	}

	public static void resetModel() {
		new File(mModelPath).delete();
		instance = null;
	}
}