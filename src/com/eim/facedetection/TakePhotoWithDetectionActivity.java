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
import android.app.ProgressDialog;
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
		CvCameraViewListener2 {
	private static final String TAG = "TakePhotoWithDetectionActivity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(255, 192, 100, 255);

	private ControlledJavaCameraView mCameraView;
	private ImageButton mButton;

	private Mat mGray;
	private Mat mRgba;

	private FaceDetector mFaceDetector;
	private boolean mTakePhotoNow = false;
	private Uri mOutputUri;
	private ImageButton mSwitchButton;
	private int mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_BACK;
	
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if (extras == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
		final String grabbingFaces = getString(R.string.progress_dialog_grabbing_face);

		mOutputUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);

		setContentView(R.layout.activity_take_photo_with_detection);
		mCameraView = (ControlledJavaCameraView) findViewById(R.id.camera_preview_detection_surface_view);
		mCameraView.setCvCameraViewListener(this);
		mCameraView.setCameraIndex(mCurrentCameraIndex);

		mButton = (ImageButton) findViewById(R.id.take_photo_button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTakePhotoNow = true;
				mProgressDialog = ProgressDialog.show(TakePhotoWithDetectionActivity.this, "",
						grabbingFaces, true);
			}
		});

		mSwitchButton = (ImageButton) findViewById(R.id.switch_camera_button);
		mSwitchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_BACK)
					mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_FRONT;
				else
					mCurrentCameraIndex = ControlledJavaCameraView.CAMERA_ID_BACK;
				mCameraView.disableView();
				mCameraView.setCameraIndex(mCurrentCameraIndex);
				mCameraView.enableView();
			}
		});
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
		mFaceDetector = FaceDetector.getInstance(this);
		mFaceDetector.resetSizes();
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
				mProgressDialog.dismiss();
				setResult(Activity.RESULT_OK);
				finish();
				if (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_FRONT)
					Core.flip(mRgba, mRgba, 1);
				return mRgba;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_FRONT)
			Core.flip(mRgba, mRgba, 1);
		
		mGray = inputFrame.gray();
		if (mCurrentCameraIndex == ControlledJavaCameraView.CAMERA_ID_FRONT) {
			Mat flippedGrey = mGray;
			mGray = new Mat();
			Core.flip(flippedGrey, mGray, 1);
		}
		
		Rect[] facesArray = mFaceDetector.detect(mGray);

		for (Rect face : facesArray)
			drawBoundingBox(mRgba, face);

		return mRgba;
	}

	private void drawBoundingBox(Mat frame, Rect info) {
		// Bounding box
		Core.rectangle(frame, info.tl(), info.br(), FACE_RECT_COLOR, 3);
	}
}
