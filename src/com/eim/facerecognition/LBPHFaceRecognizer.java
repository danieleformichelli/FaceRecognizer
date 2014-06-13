package com.eim.facerecognition;

import java.util.ArrayList;

import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Mat;

import android.content.Context;
 
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
    
    private static String MODEL_FILE_NAME = "trainedModel.xml"; 
    
    private static LBPHFaceRecognizer instance;
    private static String mModelPath;
    public static LBPHFaceRecognizer getInstance(Context c) {
    	if (instance == null) {
    		instance = new LBPHFaceRecognizer();
    		mModelPath = c.getExternalFilesDir(null).getAbsolutePath() + "/" + MODEL_FILE_NAME;
    		instance.load(mModelPath);
    	}
		return instance;
    }
    
    public void incrementalTrain(Mat newFace, int label) {
    	ArrayList<Mat> newFaces = new ArrayList<Mat>();
    	Mat labels = new Mat();
    	
    	newFaces.add(newFace);
    	labels.put(0, 0, new int[] { label });
    	
    	update(newFaces, labels);
    	
    	save(mModelPath);
    }
}