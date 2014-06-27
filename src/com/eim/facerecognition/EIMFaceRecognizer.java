package com.eim.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.SparseArray;

import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

public class EIMFaceRecognizer {
	private static final String MODEL_FILE_NAME = "trainedModel.xml";
	private static final Size defaultFaceSize = new Size(150, 150);

	public enum Type {
		LBPH, EIGEN, FISHER;

		public boolean isIncrementable() {
			return this == LBPH;
		}

		public boolean needResize() {
			return this != LBPH;
		}

		public int numberOfNeededClasses() {
			switch (this) {
			case EIGEN:
				return 1;
			case FISHER:
				return 2;
			case LBPH:
				return 1;
			}

			return -1;
		}
	}

	public enum CutMode {
		NO_CUT, EYES, HORIZONTAL, VERTICAL, TOTAL
	}

	private boolean isTrained;
	private String mModelPath;
	private Context mContext;
	private Type mRecognizerType;
	private FaceRecognizer mFaceRecognizer;
	private Size faceSize;
	// private boolean normalize;
	private CutMode mCutMode;
	private double mCutPercentage;
	private Rect cutRect;
	private int lbphRadius, lbphNeighbours, lbphGridX, lbphGridY;
	private int eigenComponents;
	private int fisherComponents;

	public EIMFaceRecognizer(Context mContext, Type mRecognizerType,
			int faceSize, boolean normalize, CutMode mCutMode,
			int mCutPercentage, Integer... params) {
		if (mContext == null)
			throw new IllegalArgumentException("mContext cannot be null");
		if (mRecognizerType == null)
			throw new IllegalArgumentException("mRecognizerType cannot be null");
		if (mCutMode == null)
			throw new IllegalArgumentException("mCutMode cannot be null");

		this.mContext = mContext;
		this.mRecognizerType = mRecognizerType;
		this.faceSize = new Size(faceSize, faceSize);
		// this.normalize = normalize;
		this.mCutMode = mCutMode;
		this.mCutPercentage = (100 - mCutPercentage) / 100.0;
		computeCutRect();

		mModelPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/"
				+ MODEL_FILE_NAME;

		instantiateFaceRecognizer(params);

		if (new File(mModelPath).exists()) {
			try {
				mFaceRecognizer.load(mModelPath);
			} catch (CvException e) {
				new File(mModelPath).delete();
				return;
			}
			isTrained = true;
		} else
			isTrained = false;
	}

	private void computeCutRect() {
		if (mCutMode == CutMode.EYES)
			return;

		cutRect = new Rect();

		switch (mCutMode) {
		case NO_CUT:
			return;
		case HORIZONTAL:
			cutRect.width = (int) (faceSize.width * mCutPercentage);
			cutRect.height = (int) faceSize.height;
			break;
		case VERTICAL:
			cutRect.width = (int) faceSize.width;
			cutRect.height = (int) (faceSize.height * mCutPercentage);
			break;
		case TOTAL:
			cutRect.width = (int) (faceSize.width * mCutPercentage);
			cutRect.height = (int) (faceSize.height * mCutPercentage);
			break;
		default:
			break;
		}

		cutRect.x = (int) (faceSize.width - cutRect.width) / 2;
		cutRect.y = (int) (faceSize.height - cutRect.height) / 2;
	}

	private void instantiateFaceRecognizer(Integer... params) {
		int paramsLength = params.length;

		switch (mRecognizerType) {
		case LBPH:
			if (paramsLength != 4)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 4");

			lbphRadius = params[0];
			lbphNeighbours = params[1];
			lbphGridX = params[2];
			lbphGridY = params[3];

			mFaceRecognizer = new LBPHFaceRecognizer(lbphRadius,
					lbphNeighbours, lbphGridX, lbphGridY, Double.MAX_VALUE);
			break;
		case EIGEN:
			if (paramsLength != 1)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 1");

			eigenComponents = params[0];

			if (eigenComponents == 0)
				mFaceRecognizer = new EigenFaceRecognizer();
			else
				mFaceRecognizer = new EigenFaceRecognizer(eigenComponents);
			break;
		case FISHER:
			if (paramsLength != 1)
				throw new IllegalArgumentException("Invalid params length "
						+ params.length + ", expected 1");

			fisherComponents = params[0];
			if (fisherComponents == 0)
				mFaceRecognizer = new FisherFaceRecognizer();
			else
				mFaceRecognizer = new FisherFaceRecognizer(mModelPath,
						fisherComponents);
			break;
		default:
			throw new IllegalArgumentException("Invalid mType");
		}
	}

	/**
	 * Resets the trained model
	 */
	public void resetModel() {
		deleteModelFromDisk(mContext);
		isTrained = false;
	}

	/**
	 * Delete the model stored on the disk
	 */
	public static void deleteModelFromDisk(Context mContext) {
		String mModelPath = mContext.getExternalFilesDir(null)
				.getAbsolutePath() + "/" + MODEL_FILE_NAME;
		File mModelFile = new File(mModelPath);
		if (mModelFile != null)
			mModelFile.delete();
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
		incrementalTrain(new String[] { newFacePath }, label);
	}

	public void incrementalTrain(String[] newFacesPaths, int label) {
		if (!mRecognizerType.isIncrementable())
			throw new IllegalStateException("Face detector of type "
					+ mRecognizerType.toString()
					+ "cannot be trained incrementally");

		List<Mat> newFaces = new ArrayList<Mat>();
		Mat labels = new Mat(newFacesPaths.length, 1, CvType.CV_32SC1);

		for (int i = 0; i < newFacesPaths.length; i++) {
			String newFacePath = newFacesPaths[i];

			Mat newFaceMat = Highgui.imread(newFacePath,
					Highgui.CV_LOAD_IMAGE_UNCHANGED);

			if (newFaceMat.empty())
				throw new IllegalArgumentException("Cannot load the image at "
						+ newFacePath);

			Imgproc.cvtColor(newFaceMat, newFaceMat, Imgproc.COLOR_BGR2GRAY);

			preprocessImage(newFaceMat);

			newFaces.add(newFaceMat);
			labels.put(i, 0, new int[] { label });
		}

		if (isTrained)
			mFaceRecognizer.update(newFaces, labels);
		else
			mFaceRecognizer.train(newFaces, labels);

		// mFaceRecognizer.save(mModelPath);

		for (Mat newFaceMat : newFaces)
			newFaceMat.release();
		labels.release();

		isTrained = true;
	}

	public void incrementalTrainWithLoading(Activity activity,
			String newFacePath, int label) {
		incrementalTrainWithLoading(activity, new String[] { newFacePath },
				label);
	}

	public void incrementalTrainWithLoading(Activity activity,
			String[] newFacesPaths, int label) {
		final String[] mNewFacesPaths = newFacesPaths;
		final int mLabel = label;
		final ProgressDialog mProgressDialog = ProgressDialog.show(activity,
				"", "Training...", true);
		final Activity mActivity = activity;

		(new Thread() {
			public void run() {
				incrementalTrain(mNewFacesPaths, mLabel);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						mFaceRecognizer.save(mModelPath);
						mProgressDialog.dismiss();
					}
				});
			}
		}).start();
	}

	/**
	 * Train with the specified dataset. If the dataset contains no faces the
	 * model is reset
	 * 
	 * @param people
	 *            faces dataset, keys are the labels and faces are contained in
	 *            the field Photos of the value
	 */
	public boolean train(SparseArray<Person> people) {
		if (!isDatasetValid(people)) {
			resetModel();
			return false;
		}
		List<Mat> faces = new ArrayList<Mat>();
		List<Integer> labels = new ArrayList<Integer>();

		for (int i = 0, l = people.size(); i < l; i++) {

			int label = people.keyAt(i);
			Person person = people.valueAt(i);
			SparseArray<Photo> photos = person.getPhotos();

			for (int j = 0, k = photos.size(); j < k; j++) {
				Photo mPhoto = photos.valueAt(j);
				Mat mMat = new Mat();

				Utils.bitmapToMat(mPhoto.getBitmap(), mMat);
				Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);

				labels.add(label);
				faces.add(mMat);

			}
		}

		for (Mat face : faces)
			preprocessImage(face);

		Mat labelsMat = new Mat(labels.size(), 1, CvType.CV_32SC1);
		int i = 0;
		for (Integer label : labels)
			labelsMat.put(i++, 0, new int[] { label });

		mFaceRecognizer.train(faces, labelsMat);
		// mFaceRecognizer.save(mModelPath);
		isTrained = true;

		for (Mat face : faces)
			face.release();
		labelsMat.release();

		return true;
	}

	public void trainWithLoading(Activity activity, SparseArray<Person> people) {
		final SparseArray<Person> mPeople = people;
		final ProgressDialog mProgressDialog = ProgressDialog.show(activity,
				"", "Training...", true);
		final Activity mActivity = activity;

		(new Thread() {
			public void run() {
				final boolean trained = train(mPeople);
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						if (trained)
							mFaceRecognizer.save(mModelPath);

						mProgressDialog.dismiss();
					}
				});
			}
		}).start();
	}

	private boolean isDatasetValid(SparseArray<Person> dataset) {
		if (dataset == null
				|| dataset.size() < mRecognizerType.numberOfNeededClasses())
			return false;

		int numberOfClasses = 0;

		for (int i = 0, l = dataset.size(); i < l; i++)
			if (dataset.valueAt(i).getPhotos().size() > 0) {
				numberOfClasses++;
				if (numberOfClasses >= mRecognizerType.numberOfNeededClasses())
					return true;
				continue;
			}

		return false;
	}

	public void predict(Mat src, int[] label, double[] confidence) {
		if (isTrained) {
			preprocessImage(src);
			mFaceRecognizer.predict(src, label, confidence);
		}
	}

	private void preprocessImage(Mat image) {
		// Illuminance normalization
		// if (normalize) {
		// Imgproc.equalizeHist(image, image);
		// illuminanceNormalization(image, image);
		// }

		// Resize
		if (faceSize.width != 0)
			Imgproc.resize(image, image, faceSize);
		else if (mRecognizerType.needResize())
			Imgproc.resize(image, image, defaultFaceSize);

		// Cut
		if (mCutMode == CutMode.EYES)
			cutToEyes(image, image);
		else
			image = image.submat(cutRect);
	}

	private void cutToEyes(Mat src, Mat dst) {
		
	}

	@SuppressWarnings("unused")
	private void illuminanceNormalization(Mat src, Mat dst) {
		float alpha = 0.1f;
		float tau = 10.0f;
		float gamma = 0.2f;
		int sigma0 = 1;
		int sigma1 = 2;

		float scale = src.rows() / 100.0f;

		sigma0 = (int) (sigma0 * scale);
		sigma1 = (int) (sigma1 * scale);

		Mat I = new Mat();

		debugImg(src, "0.png", 1);

		src.convertTo(I, CvType.CV_32FC1, 1.0 / 255);

		// Start preprocessing:
		Core.pow(I, gamma, I);

		debugImg(I, "1.png");

		// Calculate the DOG Image:
		{
			Mat gaussian1 = new Mat();
			Mat gaussian0 = new Mat();
			// Kernel Size:
			int kernel_sz0 = (3 * sigma0);
			int kernel_sz1 = (3 * sigma1);
			// Make them odd for OpenCV:
			kernel_sz0 |= 1;
			kernel_sz1 |= 1;

			Imgproc.GaussianBlur(I, gaussian0,
					new Size(kernel_sz0, kernel_sz0), sigma0, sigma0,
					Imgproc.BORDER_REPLICATE);
			Imgproc.GaussianBlur(I, gaussian1,
					new Size(kernel_sz1, kernel_sz1), sigma1, sigma1,
					Imgproc.BORDER_REPLICATE);
			Core.subtract(gaussian0, gaussian1, I);

			gaussian0.release();
			gaussian1.release();

			// Normalize
			MinMaxLocResult m = Core.minMaxLoc(I);
			Core.subtract(I, new Scalar(m.minVal), I);
			Core.multiply(I, new Scalar(1 / (m.maxVal - m.minVal)), I);
		}

		debugImg(I, "2.png");

		{
			double meanI = 0.0;
			{
				Mat tmp = new Mat();
				// trick to make abs(I), abs() is not in OpenCV4Android...
				Core.absdiff(I, new Scalar(0), tmp);
				Core.pow(tmp, alpha, tmp);
				meanI = Core.mean(tmp).val[0];
				tmp.release();
			}

			Core.multiply(I, new Scalar(1 / Math.pow(meanI, 1.0 / alpha)), I);

		}

		debugImg(I, "3.png");

		{
			double meanI = 0.0;
			{
				Mat tmp = new Mat();
				// trick to make abs(I), abs() is not in OpenCV4Android...
				Core.absdiff(I, new Scalar(0), tmp);

				Core.min(tmp, new Scalar(tau), tmp);
				Core.pow(tmp, alpha, tmp);
				meanI = Core.mean(tmp).val[0];
				tmp.release();
			}
			Core.multiply(I, new Scalar(1 / Math.pow(meanI, 1.0 / alpha)), I);
		}

		debugImg(I, "4.png");

		// Squash into the tanh:
		{
			for (int r = 0, lr = I.rows(); r < lr; r++) {
				for (int c = 0, lc = I.cols(); c < lc; c++) {
					I.put(r, c, I.get(r, c)[0] / tau);
				}
			}
			Core.multiply(I, new Scalar(tau), I);
		}

		debugImg(I, "5.png");

		I.convertTo(dst, CvType.CV_8UC1, 255);
		I.release();
	}

	private void debugImg(Mat d, String name) {
		debugImg(d, name, 255);
	}

	private void debugImg(Mat d, String name, double s) {
		return;
		// Mat img = new Mat();
		//
		// d.convertTo(img, CvType.CV_8UC1, s);
		//
		// Bitmap debug = Bitmap.createBitmap(img.cols(),
		// img.rows(), Bitmap.Config.ARGB_8888);
		// Utils.matToBitmap(img, debug);
		//
		// img.release();
		//
		// String filename =
		// mContext.getExternalFilesDir(null).getAbsolutePath()
		// + "/" + name;
		//
		// try {
		// FileOutputStream out;
		// out = new FileOutputStream(filename);
		// debug.compress(Bitmap.CompressFormat.PNG, 100, out);
		// out.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}
	
	private void rotate(Mat src, Mat dst, double angle)
	{
	    Point pt = new Point(src.cols()/2., src.rows()/2.);    
	    Mat r = Imgproc.getRotationMatrix2D(pt, angle, 1.0);
	    Imgproc.warpAffine(src, dst, r, new Size(src.cols(), src.rows()));
	}
	
	private Mat crop(Mat uncropped, Point p, Size s) {
		Rect roi = new Rect(p,s);
		Mat cropped = new Mat(uncropped, roi);
		return cropped;
	}
	

	private void ScaleRotateTranslate(Mat img, double angle, Point center,
			Point nCenter, double scale /* resample = Image.BICUBIC ? */) {
		
		Mat dst = new Mat();
		if (scale == -1 || center == null) {
			rotate(img, dst, angle);
			img = dst;
			return;
		}
		
		double x, nx, y ,ny;
		x = nx = center.x;
		y = ny = center.y;	
		
		if (nCenter != null) {
			nx = nCenter.x;
			ny = nCenter.y;
		}
		
		double sx, sy;
		sx = sy = 1.0;
		
		if (0.0 <= scale && scale <= 1.0) {
			sx = sy = scale;
		}
		
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		
		double a,b,c,d,e,f;
		
		a = cos/sx;
		b = sin/sx;
		c = x - (nx * a) - (ny * b);
		d = - sin/sy;
		e = cos/sy;
		f = y - (nx * d) - (ny * e);
		
		// Matrix
		// [ a d ]
		// [ b e ]
		// [ c f ]
		
		Mat transMat = new Mat(2,3, CvType.CV_64F);
		transMat.put(0, 0, a);
		transMat.put(1, 0, b);
		transMat.put(2, 0, c);
		transMat.put(0, 1, d);
		transMat.put(1, 1, e);
		transMat.put(2, 1, f);
		
		Core.transform(img, dst, transMat /*, resample*/);
		
		img = dst;
		
	}
	
	private Mat cropFace(Mat img, Point eyeLeft, Point eyeRight, Point offs,
			Size dsize) {
		
		Mat tmp = new Mat();
		
		// Compute offsets in original image
		int offsetH = (int) Math.floor(((float)(offs.x))*dsize.width);
		int offsetV = (int) Math.floor(((float)(offs.y))*dsize.height);
		
		// Get the direction
		Point eyeDir = new Point(eyeRight.x - eyeLeft.x,eyeRight.y - eyeLeft.y);
		
		// Calc rotation angle in radians
		double rotation = - Math.atan2((float)(eyeDir.y),(float)(eyeDir.x));
		
		// Distance between them
		
		double dist = Math.sqrt(eyeDir.x*eyeDir.x + eyeDir.y*eyeDir.y);
		
		// Calculate the reference eye-width
		double reference = dsize.width - 2.0* offsetH;
		
		// Scale factor
		double scale = ((float)(dist))/((float)(reference));
		
		// Rotate original around the left eye
		ScaleRotateTranslate(img, rotation, eyeLeft, null, -1);
		
		// crop the rotated image
		Point crop = new Point (eyeLeft.x - scale*offsetH, 
				eyeLeft.x - scale*offsetV);
		
		Size cropSize = new Size(dsize.width * scale, dsize.height * scale);
		
		tmp = crop(img, crop, cropSize);
		
		Imgproc.resize(tmp, img, dsize);
		
		return img;
	}

	public Type getType() {
		return mRecognizerType;
	}
}
