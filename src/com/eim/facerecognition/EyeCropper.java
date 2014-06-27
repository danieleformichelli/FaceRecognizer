package com.eim.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.util.Log;

import com.eim.R;

public class EyeCropper {
	private static final String TAG = "EyeDetector";

	private static String eyesCascadeXML = "haarcascade_eye_tree_eyeglasses.xml";

	private static final double EYE_MIN_SIZE_PERCENTAGE = 0.05;
	private static final double EYE_MAX_SIZE_PERCENTAGE = 0.5;

	private final double SCALE_FACTOR = 1.1;
	private final int MIN_NEIGHBOURS = 2;
	private final int FLAGS = 0;

	public enum Type {
		LEFT, RIGHT
	}

	private Context mContext;

	private File mCascadeFile;
	private CascadeClassifier eyesCascade;

	private Rect leftEye, rightEye;

	double offset;

	public EyeCropper(Context context) {

		if (context == null)
			throw new IllegalArgumentException("context cannot be null");

		mContext = context;

		offset = 0.05;

		loadCascadeFile();

		eyesCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
	}

	private void loadCascadeFile() {
		File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
		mCascadeFile = new File(cascadeDir, eyesCascadeXML);

		if (!mCascadeFile.exists()) {
			// load cascade file from application resources
			try {
				int resId = R.raw.haarcascade_eye_tree_eyeglasses;
				InputStream is = mContext.getResources().openRawResource(resId);
				FileOutputStream os = new FileOutputStream(mCascadeFile);

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1)
					os.write(buffer, 0, bytesRead);

				is.close();
				os.close();
			} catch (IOException e) {
				mCascadeFile.delete();
				mCascadeFile = null;
				e.printStackTrace();
			}
		}
	}

	public Mat cropEyes(Mat src) {
		if (detectEye(src))
			return cropFaceToEyes(src);

		return src;
	}

	/**
	 * Detects eyes in a greyscale Mat that contains a face
	 * 
	 * @param img
	 * @return true if 2 eyes are found, false otherwise
	 */
	private boolean detectEye(Mat img) {
		MatOfRect matEyes = new MatOfRect();

		double width = img.size().width;

		Size minSize = new Size(width * EYE_MIN_SIZE_PERCENTAGE, width
				* EYE_MIN_SIZE_PERCENTAGE);
		Size maxSize = new Size(width * EYE_MAX_SIZE_PERCENTAGE, width
				* EYE_MAX_SIZE_PERCENTAGE);

		eyesCascade.detectMultiScale(img, matEyes, SCALE_FACTOR,
				MIN_NEIGHBOURS, FLAGS, minSize, maxSize);

		Rect[] eyes = matEyes.toArray();

		if (eyes.length != 2) {
			// The input is a face. I should recognize two eyes.
			Log.e(TAG, "detected " + eyes.length + " eyes");
			return false;
		}

		if (eyes[0].x < eyes[1].x) {
			leftEye = eyes[0];
			rightEye = eyes[1];
		} else {
			leftEye = eyes[1];
			rightEye = eyes[0];
		}

		// Log.e(TAG, "FACE: " + face.width + "x" + face.height);
		// Log.e(TAG, "LEFT EYE: " + leftEye.x + ", " + leftEye.y + "   "
		// + leftEye.width + "x" + leftEye.height);
		// Log.e(TAG, "RIGHT EYE: " + rightEye.x + ", " + rightEye.y + "   "
		// + rightEye.width + "x" + rightEye.height);

		return true;
	}

	/**
	 * Crop a face horizontally near the eyes
	 * @param src
	 *            the source image
	 */
	private Mat cropFaceToEyes(Mat src) {
		// Compute offsets in original image
		final Size faceSize = src.size();
		final int offsetH = (int) Math.floor(offset * faceSize.width);
		final int xLeft = leftEye.x - offsetH > 0 ? leftEye.x - offsetH : 0;
		final int xRight = rightEye.x + rightEye.width + offsetH < faceSize.width ? rightEye.x
				+ rightEye.width + offsetH
				: (int) faceSize.width;

		Rect roi = new Rect(xLeft, 0, xRight, (int) faceSize.height);

		// Log.e(TAG, "roi: " + roi.x + ", " + roi.y + "   " + roi.width + "x"
		// + roi.height);

		return src.submat(roi);
	}
}
