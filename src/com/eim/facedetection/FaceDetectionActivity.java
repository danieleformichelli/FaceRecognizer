package com.eim.facedetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

import com.eim.R;
import com.eim.facesmanagement.peopledb.Photo;
import com.eim.utilities.EIMPreferences;
import com.eim.utilities.PhotoAdapter;

public class FaceDetectionActivity extends Activity {
	private static final String TAG = "FaceDetectionAndExtractionActivity";

	protected static final int REQUEST_TAKE_PHOTO = 1;
	protected static final int REQUEST_PICK_PHOTO = 2;
	protected static final int REQUEST_TAKE_PHOTO_DETECTION = 3;

	public static final String PERSON_ID = "personId";
	public static final String PERSON_NAME = "personName";
	public static final String PHOTO_PATHS = "photoPath";

	private FaceDetector mFaceDetector;

	private File mSceneFile;

	private int personId = -1;
	private String mLabelName = "Unknown";

	private boolean isPhotoReady = false;
	private boolean mChooserVisible = false;

	private List<String> mFacesResults;

	private ProgressDialog mProgressDialog;
	private String savingFace;

	private interface GenericCancelListener extends OnCancelListener,
			OnDismissListener {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras == null)
			finish();

		personId = extras.getInt(PERSON_ID);
		mLabelName = extras.getString(PERSON_NAME);

		savingFace = getString(R.string.progress_dialog_saving_face);

		showChooserDialog();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!isPhotoReady)
			return;

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				new BaseLoaderCallback(this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS:
							detectAndProcessFaces();
							break;
						default:
							Log.e(TAG, "OpenCV connection error: " + status);
							super.onManagerConnected(status);
						}
					}
				});
	}

	private void showChooserDialog() {
		showChooserDialog(false);
	}

	private void showChooserDialog(boolean retry) {

		String dialogTitle;

		if (retry)
			dialogTitle = this.getResources().getString(
					R.string.face_detection_retry_add_photo_title);
		else
			dialogTitle = this.getResources().getString(
					R.string.face_detection_add_photo_title);

		String[] dialogOptions = this.getResources().getStringArray(
				R.array.face_detection_add_photo_options);

		GenericCancelListener dialogCancelListener = new GenericCancelListener() {

			void exitWithError() {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}

			@Override
			public void onCancel(DialogInterface dialog) {
				exitWithError();
			}

			@Override
			public void onDismiss(DialogInterface arg0) {
				exitWithError();
			};
		};

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {

				FaceDetectionActivity activity = FaceDetectionActivity.this;

				try {
					File outputDir = activity.getExternalFilesDir(null);
					mSceneFile = File
							.createTempFile("scene", ".jpg", outputDir);
				} catch (IOException e) {
					e.printStackTrace();
				}

				isPhotoReady = false;

				switch (item) {
				case 0: // take photo from camera

					Intent takePictureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);

					// check if camera activity is not available
					if (takePictureIntent.resolveActivity(activity
							.getPackageManager()) == null) {
						mSceneFile.delete();
						return;
					}

					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(mSceneFile));

					activity.startActivityForResult(takePictureIntent,
							REQUEST_TAKE_PHOTO);
					break;
				case 1: // pick photo from gallery

					Intent pickImageIntent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					pickImageIntent.setType("image/*");
					activity.startActivityForResult(Intent.createChooser(
							pickImageIntent, "Select Image"),
							REQUEST_PICK_PHOTO);
					break;
				case 2: // take photo from camera

					Intent takeDetectedPictureIntent = new Intent(activity,
							TakePhotoWithDetectionActivity.class);

					// check if camera activity is not available
					if (takeDetectedPictureIntent.resolveActivity(activity
							.getPackageManager()) == null) {
						mSceneFile.delete();
						return;
					}

					takeDetectedPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(mSceneFile));

					activity.startActivityForResult(takeDetectedPictureIntent,
							REQUEST_TAKE_PHOTO);
					break;
				}

			}
		};

		new AlertDialog.Builder(this).setTitle(dialogTitle)
				.setItems(dialogOptions, dialogClickListener)
				.setOnCancelListener(dialogCancelListener)
				/* .setOnDismissListener(dialogCancelListener) */
				.show();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			showChooserDialog();
			return;
		}

		switch (requestCode) {
		case REQUEST_PICK_PHOTO:
			copyPickedPhoto(data);
		case REQUEST_TAKE_PHOTO:
		case REQUEST_TAKE_PHOTO_DETECTION:
			isPhotoReady = true;
			// detection will be started by opencv async init
		}

	}

	private void detectAndProcessFaces() {
		setupFaceDetection();

		Bitmap[] detectedFaces = detectFaces();
		mSceneFile.delete();

		if (detectedFaces.length == 0) {
			showChooserDialog(true);
			return;
		}

		displayFaceChooser(detectedFaces);
	}

	private void setupFaceDetection() {
		final EIMPreferences mPreferences = EIMPreferences.getInstance(this);

		// final FaceDetector.Type type = mPreferences.detectorType(); TODO
		final FaceDetector.Type type = FaceDetector.Type.JAVA;
		final FaceDetector.Classifier classifier = mPreferences
				.detectorClassifier();
		final double scaleFactor = mPreferences.detectionScaleFactor();
		final int minNeighbors = mPreferences.detectionMinNeighbors();
		final double minRelativeFaceSize = mPreferences
				.detectionMinRelativeFaceSize();
		final double maxRelativeFaceSize = mPreferences
				.detectionMaxRelativeFaceSize();

		mFaceDetector = new FaceDetector(this, type, classifier, scaleFactor,
				minNeighbors, minRelativeFaceSize, maxRelativeFaceSize);
	}

	@Override
	public void onBackPressed() {
		if (mChooserVisible)
			showChooserDialog();
		else
			super.onBackPressed();
	}

	private void processFace(Bitmap bitmap) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = mLabelName + "_" + timeStamp + "_"
				+ mFacesResults.size() + ".png";

		String filename = getExternalFilesDir(null).getAbsolutePath() + "/"
				+ imageFileName;

		try {
			FileOutputStream out;
			out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			Log.i(TAG, "Face saved to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}

		mFacesResults.add(filename);
	}

	private void finishWithResults() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(PERSON_ID, personId);
		returnIntent
				.putExtra(PHOTO_PATHS, mFacesResults.toArray(new String[0]));

		setResult(Activity.RESULT_OK, returnIntent);
		mProgressDialog.dismiss();
		finish();
	}

	private void displayFaceChooser(final Bitmap[] detectedFaces) {

		mFacesResults = new ArrayList<String>();
		mChooserVisible = true;
		setContentView(R.layout.activity_face_detection);

		List<Photo> faces = new ArrayList<Photo>();
		for (Bitmap bitmap : detectedFaces)
			faces.add(new Photo(bitmap));

		PhotoAdapter adapter = new PhotoAdapter(this, faces, null);

		GridView grid = (GridView) findViewById(R.id.face_chooser_grid);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mChooserVisible = false;
				mProgressDialog = ProgressDialog.show(
						FaceDetectionActivity.this, "", savingFace, true);
				processFace(detectedFaces[position]);
				finishWithResults();
			}
		});

		Button all = (Button) findViewById(R.id.choose_all);
		all.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mProgressDialog = ProgressDialog.show(
						FaceDetectionActivity.this, "", savingFace, true);

				for (Bitmap face : detectedFaces)
					processFace(face);

				finishWithResults();
			}
		});

	}

	private void copyPickedPhoto(Intent data) {
		try {
			File dst = mSceneFile;

			InputStream in = getContentResolver().openInputStream(
					data.getData());
			OutputStream out = new FileOutputStream(dst);

			byte[] buf = new byte[4096];
			int len;

			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);

			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Bitmap[] detectFaces() {
		Mat scene = Highgui.imread(mSceneFile.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_UNCHANGED);
		Imgproc.cvtColor(scene, scene, Imgproc.COLOR_BGR2RGB);
		
		Mat gray = new Mat();

		Imgproc.cvtColor(scene, gray, Imgproc.COLOR_RGB2GRAY);

		Rect[] faceRegions = mFaceDetector.detect(gray);

		Bitmap[] detectedFaces = new Bitmap[faceRegions.length];

		Mat subRegion = new Mat();

		for (int i = 0; i < faceRegions.length; i++) {
			subRegion = scene.submat(faceRegions[i]);
			detectedFaces[i] = Bitmap.createBitmap(subRegion.cols(),
					subRegion.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(subRegion, detectedFaces[i]);
		}

		scene.release();
		gray.release();
		subRegion.release();
		
		return detectedFaces;
	}
}
