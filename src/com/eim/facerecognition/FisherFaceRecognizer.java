package com.eim.facerecognition;

import org.opencv.contrib.FaceRecognizer;

public class FisherFaceRecognizer extends FaceRecognizer {

	private static native long createFisherFaceRecognizer_0();

	private static native long createFisherFaceRecognizer_1(int num_components);

	private static native long createFisherFaceRecognizer_2(int num_components,
			double threshold);
	
	private FisherFaceRecognizer() {
		super(createFisherFaceRecognizer_0());
	}

	private FisherFaceRecognizer(String modelPath, int num_components) {
		super(createFisherFaceRecognizer_1(num_components));
	}

	private FisherFaceRecognizer(String modelPath, int num_components, double threshold) {
		super(createFisherFaceRecognizer_2(num_components, threshold));
	}
}
