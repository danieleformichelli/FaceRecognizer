package com.eim.facerecognition;

import org.opencv.contrib.FaceRecognizer;
 
public class LBPHFaceRecognizer extends FaceRecognizer {
 
    static{ System.loadLibrary("facerecognizer"); }
 
    private static native long createLBPHFaceRecognizer_0();
    private static native long createLBPHFaceRecognizer_1(int radius);
    private static native long createLBPHFaceRecognizer_2(int radius,int neighbours);
 
    public LBPHFaceRecognizer() {
    	super(createLBPHFaceRecognizer_0());
    }
    public LBPHFaceRecognizer(int radius) {
    	super(createLBPHFaceRecognizer_1(radius));
    }
    public LBPHFaceRecognizer(int radius,int neighbours) {
    	super(createLBPHFaceRecognizer_2(radius,neighbours));
    }
}