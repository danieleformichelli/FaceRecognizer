package com.eim.facerecognition;

import java.io.File;

import org.opencv.contrib.FaceRecognizer;

import android.content.Context;

class EigenFaceRecognizer extends FaceRecognizer {

	private static native long createEigenFaceRecognizer_0();

	private static native long createEigenFaceRecognizer_1(int num_components);

	private static native long createEigenFaceRecognizer_2(int num_components,
			double threshold);
	
	EigenFaceRecognizer() {
		super(createEigenFaceRecognizer_0());
	}

	private EigenFaceRecognizer(int num_components) {
		super(createEigenFaceRecognizer_1(num_components));
	}

	private EigenFaceRecognizer(int num_components, double threshold) {
		super(createEigenFaceRecognizer_2(num_components, threshold));
	}

	private static String MODEL_FILE_NAME = "trainedModel.xml";

	private static EigenFaceRecognizer instance;
	private static String mModelPath;
	private static Context mContext;
	private static boolean isTrained;

	static EigenFaceRecognizer getInstance(Context c) {
		if (instance == null) {
			System.loadLibrary("facerecognizer");
			instance = new EigenFaceRecognizer();
			mContext = c;
			mModelPath = c.getExternalFilesDir(null).getAbsolutePath() + "/"
					+ MODEL_FILE_NAME;
			if (new File(mModelPath).exists()) {
				instance.load(mModelPath);
				isTrained = true;
			} else
				isTrained = false;
		}

		return instance;
	}
}
