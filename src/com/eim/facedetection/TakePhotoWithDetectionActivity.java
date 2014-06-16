package com.eim.facedetection;

import java.io.FileOutputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.eim.R;
import com.eim.facerecognition.ControlledJavaCameraView;

public class TakePhotoWithDetectionActivity extends Activity implements
		CvCameraViewListener2, OnClickListener {
	private static final String TAG = "TakePhotoWithDetectionActivity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(255, 192, 100, 255);

	private ControlledJavaCameraView mCameraView;
	private ImageButton mButton;

	private Mat mGray;
	private Mat mRgba;

	private FaceDetector mFaceDetector;
	private boolean mTakePhotoNow = false;
	private Uri mOutputUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if (extras == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		mOutputUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);

		setContentView(R.layout.activity_take_photo_with_detection);
		mCameraView = (ControlledJavaCameraView) findViewById(R.id.camera_preview_detection_surface_view);
		mCameraView.setCvCameraViewListener(this);

		mButton = (ImageButton) findViewById(R.id.take_photo_button);
		mButton.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				new BaseLoaderCallback(this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS:
							Log.i(TAG, "OpenCV loaded successfully");
							mCameraView.enableView();
							break;
						default:
							Log.i(TAG, "OpenCV connection error: " + status);
							super.onManagerConnected(status);
						}
					}
				});
	}

	@Override
	public void onPause() {
		if (mCameraView != null)
			mCameraView.disableView();

		super.onPause();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mFaceDetector = new FaceDetector(this);

		mGray = new Mat();
		mRgba = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();

		if (mTakePhotoNow) {
			Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),
					Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mRgba, bmp);

			FileOutputStream out;
			try {
				out = new FileOutputStream(mOutputUri.getPath());
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
				setResult(Activity.RESULT_OK);
				finish();
				return mRgba;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mGray = inputFrame.gray();

		Rect[] facesArray = mFaceDetector.detect(mGray);

		for (Rect face : facesArray)
			drawBoundingBox(mRgba, face);

		return mRgba;
	}

	private void drawBoundingBox(Mat frame, Rect info) {
		// Bounding box
		Core.rectangle(frame, info.tl(), info.br(), FACE_RECT_COLOR, 3);
	}

	@Override
	public void onClick(View v) {
		mTakePhotoNow = true;
	}
}
