package com.eim.facedetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.eim.R;

public class FaceDetectionAndExtractionActivity extends Activity {
	private static final String TAG = "FaceDetectionAndExtractionActivity";

	protected static final int REQUEST_TAKE_PHOTO = 1;
	protected static final int REQUEST_PICK_PHOTO = 2;

	public static final String PERSON_NAME = "personName";
	public static final String PHOTO_PATH = "photoPath";

	private File mSceneFile;
	private Mat mScene;

	private FaceDetector mFaceDetector;

	private String mLabelName = "Unknown";
	
	private boolean mAlreadyStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras == null)
			finish();

		mLabelName = extras.getString(PERSON_NAME);
	}

	/**
	 * OpenCV initialization
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		if (mAlreadyStarted)
			return;
		
		mAlreadyStarted = true;		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				new BaseLoaderCallback(this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS:
							Log.i(TAG, "OpenCV loaded successfully");
							showChooserDialog();
							break;
						default:
							Log.i(TAG, "OpenCV connection error: " + status);
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

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {

				FaceDetectionAndExtractionActivity activity = FaceDetectionAndExtractionActivity.this;

				try {
					File outputDir = activity.getExternalFilesDir(null);
					mSceneFile = File
							.createTempFile("scene", ".jpg", outputDir);
				} catch (IOException e) {
					e.printStackTrace();
				}

				switch (item) {
				case 0: // take photo from camera

					Intent takePictureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);

					// check if camera activity is available
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
				}
			}
		};

		new AlertDialog.Builder(this).setTitle(dialogTitle)
				.setItems(dialogOptions, dialogClickListener).show();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_PICK_PHOTO:
			copyPickedPhoto(data);
		case REQUEST_TAKE_PHOTO:

			Bitmap[] detectedFaces = detectFaces();

			if (detectedFaces.length == 0) {
				showChooserDialog(true);
				return;
			}

			if (detectedFaces.length > 1) {
				displayFaceChooser(detectedFaces);
				return;
			}

			processFace(detectedFaces[0]);
			break;
		}
	}

	private void processFace(Bitmap bmp) {

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = mLabelName + timeStamp + ".png";

		FileOutputStream out;

		String filename = getExternalFilesDir(null).getAbsolutePath() + "/"
				+ imageFileName;
		try {
			out = new FileOutputStream(filename);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent returnIntent = new Intent();
		returnIntent.putExtra(PERSON_NAME, mLabelName);
		returnIntent.putExtra(PHOTO_PATH, filename);

		Log.i(TAG, filename);

		mSceneFile.delete();

		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}

	private void displayFaceChooser(final Bitmap[] detectedFaces) {

		setContentView(R.layout.activity_face_detection_and_extraction);

		ThumbnailAdapter adapter = new ThumbnailAdapter(this, detectedFaces);

		GridView grid = (GridView) this.findViewById(R.id.face_chooser_grid);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				processFace(detectedFaces[position]);
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

	private void initDetector() {

		mFaceDetector = new FaceDetector(this);

		Bitmap sceneBitmap = BitmapFactory.decodeFile(mSceneFile
				.getAbsolutePath());
		mScene = new Mat();
		Utils.bitmapToMat(sceneBitmap, mScene);
		sceneBitmap = null; // free bitmap memory

	}

	private Bitmap[] detectFaces() {
		if (mFaceDetector == null)
			initDetector();

		MatOfRect faces = new MatOfRect();

		Rect[] faceRegions = mFaceDetector.detect(mScene, faces);

		Bitmap[] detectedFaces = new Bitmap[faceRegions.length];

		Mat subRegion = new Mat();

		for (int i = 0; i < faceRegions.length; i++) {
			subRegion = mScene.submat(faceRegions[i]);
			detectedFaces[i] = Bitmap.createBitmap(subRegion.cols(),
					subRegion.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(subRegion, detectedFaces[i]);
		}

		return detectedFaces;
	}

	public class ThumbnailAdapter extends BaseAdapter {
		private Context context;
		private Bitmap[] thumbnails;

		public ThumbnailAdapter(Context c, Bitmap[] thumbs) {
			context = c;
			thumbnails = thumbs;
		}

		// ---returns the number of images---
		public int getCount() {
			return thumbnails.length;
		}

		// ---returns the ID of an item---
		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// ---returns an ImageView view---
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(context);
				imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(5, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageBitmap(thumbnails[position]);
			return imageView;
		}
	}
}
