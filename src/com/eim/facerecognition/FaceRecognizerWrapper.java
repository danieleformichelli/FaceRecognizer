package com.eim.facerecognition;

import static org.bytedeco.javacpp.opencv_contrib.createLBPHFaceRecognizer;

import java.io.File;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_highgui;

import android.content.Context;
import android.util.Pair;

public class FaceRecognizerWrapper {
	private static FaceRecognizerWrapper instance = null;

	public static FaceRecognizerWrapper getInstance(Context c) {
		if (instance == null)
			instance = new FaceRecognizerWrapper(c);
		return instance;
	}

	private static final String MODEL_FILENAME = "trainedModel.xml";

	FaceRecognizer mFaceRecognizer;

	String mPersistentFilePath;

	protected FaceRecognizerWrapper(Context c) {
		mFaceRecognizer = createLBPHFaceRecognizer();

		mPersistentFilePath = c.getExternalFilesDir(null).getAbsolutePath()
				+ "/" + MODEL_FILENAME;

		if (new File(mPersistentFilePath).exists())
			;
		mFaceRecognizer.load(mPersistentFilePath);
	}

	public void trainIncremental(String faceFile, int label) {
		MatVector newFace = new MatVector();
		newFace.put(opencv_highgui.imread(faceFile,
				opencv_highgui.IMREAD_GRAYSCALE));
		CvMat newLabel = new CvMat();
		newLabel.put(0, 0, label);

		mFaceRecognizer.update(newFace, new Mat(newLabel));

		mFaceRecognizer.save(mPersistentFilePath);
	}
	
	// TODO still to decide how to pass the faceImage in (which type?)
	// XXX problem: FaceDetection uses the org.opencv.core.Mat type,
	// FaceRecognition uses org.bytedeco.opencv_core.Mat instead..
	// Don't know if type casting between them is possible.
	// Should we use rg.bytedeco.opencv_* types for detecion too?
	public Pair<Integer, Double> recognize() {
		IntPointer predictedLabel = new IntPointer();
		DoublePointer confidence = new DoublePointer();
		mFaceRecognizer.predict(new Mat(), predictedLabel, confidence);

		Pair<Integer, Double> result = new Pair<Integer, Double>(
				predictedLabel.get(), confidence.get());
		return result;
	}
}
