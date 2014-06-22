package com.eim.facerecognition;

import org.opencv.contrib.FaceRecognizer;

class LBPHFaceRecognizer extends FaceRecognizer {

	private static native long createLBPHFaceRecognizer_0();

	private static native long createLBPHFaceRecognizer_1(int radius);

	private static native long createLBPHFaceRecognizer_2(int radius,
			int neighbours);

	private static native long createLBPHFaceRecognizer_3(int radius,
			int neighbours, int gridX, int gridY, double threshold);

	LBPHFaceRecognizer() {
		super(createLBPHFaceRecognizer_0());
	}

	LBPHFaceRecognizer(int radius) {
		super(createLBPHFaceRecognizer_1(radius));
	}

	LBPHFaceRecognizer(int radius, int neighbours) {
		super(createLBPHFaceRecognizer_2(radius, neighbours));
	}

	LBPHFaceRecognizer(int radius, int neighbours, int gridX,
			int gridY, double threshold) {
		super(createLBPHFaceRecognizer_3(radius, neighbours, gridX, gridY,
				threshold));
	}
}