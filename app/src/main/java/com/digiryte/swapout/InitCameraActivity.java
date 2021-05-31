package com.digiryte.swapout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class InitCameraActivity extends AppCompatActivity {

    ImageView imageCaptureButton,frontCamButton,flashButton;
    String folderName="SwapOut";

    public static boolean isFrontCamOpened = false;
    int isFlashenabled = 0;// 0 - flash-auto(default); 1 - flash-enabled; 2 - flash-disabled;
    ProgressDialog progressDialog =null;
    CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_camera_surface);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        getSupportActionBar().hide();

        // get reference to view.
        imageCaptureButton = (ImageView) findViewById(R.id.initialCameraCapture);
        frontCamButton = (ImageView) findViewById(R.id.initialCameraFaceCam);
        flashButton=(ImageView)findViewById(R.id.initialCameraFlash);

        // camera surface view created
        cameraView = findViewById(R.id.initialCameraView);
        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
        cameraView.setCropOutput(false);
        cameraView.setJpegQuality(100);
        cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
        cameraView.setZoom(CameraKit.Constants.ZOOM_PINCH);

        // configure progress bar
        progressDialog = new ProgressDialog(InitCameraActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Image Saving....");
        progressDialog.setTitle("In Progress");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        //
        imageCaptureButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                cameraView.captureImage();
                progressDialog.show();
            }
        });


        frontCamButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (isFrontCamOpened){
                    isFrontCamOpened = false;
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                } else {
                    isFrontCamOpened = true;
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                }
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                switch (isFlashenabled) {
                    case 0:
                        //automatic --> enabled mode
                        isFlashenabled = 1;
                        flashButton.setImageResource(R.drawable.flash_enabled);
                        cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                        Toast.makeText(InitCameraActivity.this, "Flash Enabled.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        //enabled --> disabled mode
                        isFlashenabled = 2;
                        flashButton.setImageResource(R.drawable.flash_disabled);
                        cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                        Toast.makeText(InitCameraActivity.this, "Flash Disabled", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        //disabled --> auto mode
                        isFlashenabled = 3;
                        flashButton.setImageResource(R.drawable.flash_auto);
                        cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
                        Toast.makeText(InitCameraActivity.this, "Flash Auto", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        // default to auto
                        isFlashenabled = 0;
                        flashButton.setImageResource(R.drawable.flash_disabled);
                        cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
                        Toast.makeText(InitCameraActivity.this, "Flash Auto", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });

        // set listener for camera
        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);
                // save to file using bitmap
                File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                    imagesFolder.setWritable(true);
                }
                File mypath = new File(imagesFolder, "Test.JPEG");

                Bitmap bitmapPicture = BitmapFactory.decodeByteArray(picture, 0, picture.length);
//                    bitmapPicture=Bitmap.createScaledBitmap(bitmapPicture, surfaceView.getHeight(), surfaceView.getWidth(), false);
                ExifInterface exif = null;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    exif = new ExifInterface(mypath.toString());
                    bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri imgUri = FileProvider.getUriForFile(InitCameraActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        mypath);
                Intent intentEditActivity = new Intent(InitCameraActivity.this, EditActivity.class);
                intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
                intentEditActivity.putExtra("FilePath",mypath.toString());
                startActivity(intentEditActivity);
                progressDialog.dismiss();
                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }


//    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){
//
//        public void onPictureTaken(final byte[] arg0, Camera arg1) {
//
//        if(checkAndRequestPermissions()) {
//            progressDialog = new ProgressDialog(InitCameraActivity.this);
//            progressDialog.setMax(100);
//            progressDialog.setMessage("Image Saving....");
//            progressDialog.setTitle("In Progress");
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
//                    if (!imagesFolder.exists()) {
//                        imagesFolder.mkdirs();
//                        imagesFolder.setWritable(true);
//                    }
//                    File mypath = new File(imagesFolder, "Test.PNG");
//
//                    Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
//                    bitmapPicture=Bitmap.createScaledBitmap(bitmapPicture, surfaceView.getHeight(), surfaceView.getWidth(), false);
//
//                    ExifInterface exif = null;
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(mypath);
//                        exif = new ExifInterface(mypath.toString());
//                        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
//                            if (!boFrontCam)
//                                bitmapPicture = rotate(bitmapPicture, 90);
//                            else
//                                bitmapPicture = rotate(bitmapPicture, 180);
//                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
//                            bitmapPicture = rotate(bitmapPicture, 270);
//                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
//                            bitmapPicture = rotate(bitmapPicture, 180);
//                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
//                            if (!boFrontCam)
//                                bitmapPicture = rotate(bitmapPicture, 90);
//                            else
//                                bitmapPicture = rotate(bitmapPicture, 90);
//                        }
//
//                        bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, fos);
//
//                        fos.close();
//
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    progressDialog.dismiss();
//                    Uri imgUri = FileProvider.getUriForFile(InitCameraActivity.this,
//                            BuildConfig.APPLICATION_ID + ".provider",
//                            mypath);
//                    Intent  intentEditActivity = new Intent(InitCameraActivity.this, EditActivity.class);
//                    intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
//                    intentEditActivity.putExtra("FilePath",mypath.toString());
//                    startActivity(intentEditActivity);
//                    finish();
//                }
//            }).start();
//
//        }
//
//        }};

}
