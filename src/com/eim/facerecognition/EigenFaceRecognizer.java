package com.eim.facerecognition;

import org.opencv.contrib.FaceRecognizer;

public class EigenFaceRecognizer extends FaceRecognizer {

	private static native long createEigenFaceRecognizer_0();

	private static native long createEigenFaceRecognizer_1(int num_components);

	private static native long createEigenFaceRecognizer_2(int num_components,
			double threshold);
	
	private EigenFaceRecognizer() {
		super(createEigenFaceRecognizer_0());
	}

	private EigenFaceRecognizer(int num_components) {
		super(createEigenFaceRecognizer_1(num_components));
	}

	private EigenFaceRecognizer(int num_components, double threshold) {
		super(createEigenFaceRecognizer_2(num_components, threshold));
	}
}
