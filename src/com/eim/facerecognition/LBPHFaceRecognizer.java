package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;
 
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
    		if (new File(mModelPath).exists())
    			instance.load(mModelPath);
    	}
		return instance;
    }
    
    /**
     * Train the recognizer with a new face.
     * @param newFace the new face
     * @param label the id of the person related to the new face
     */
    public void incrementalTrain(String newFacePath, int label) {
    	ArrayList<Mat> newFaces = new ArrayList<Mat>();
    	Mat labels = new Mat();
    	Mat newFaceMat = new Mat();
    	
    	Bitmap newFace = BitmapFactory.decodeFile(newFacePath);
    	
    	Utils.bitmapToMat(newFace, newFaceMat);
    	
    	newFaces.add(newFaceMat);
    	labels.put(0, 0, new int[] { label });
    	
    	update(newFaces, labels);
    	save(mModelPath);
    }
    
    public void train(SparseArray<Person> dataset) {
    	
    	List<Mat> faces = new ArrayList<Mat>();
    	Mat labels = new Mat();
    	int counter = 0;
    	
    	for(int i = 0; i < dataset.size(); i++){
    	    int label = dataset.keyAt(i);
    	    Person pe = dataset.valueAt(i);
    	    
    	    for (Photo ph: pe.getPhotos()) {
    	    	Mat m = new Mat();
    	    	
    	    	Bitmap b = ph.getBitmap();
    	    	if (b == null)
    	    		b = BitmapFactory.decodeFile(ph.getUrl());
    	    	
    	    	Utils.bitmapToMat(b, m);
    	    	faces.add(m);
    	    	labels.put(counter++, 0, label);
    	    }
    	}
    	
    	train(faces, labels);
    	save(mModelPath);
    }
}