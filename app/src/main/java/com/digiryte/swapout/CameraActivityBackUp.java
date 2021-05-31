package com.digiryte.swapout;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
public class CameraActivity extends Activity {
    private CameraPreview mPreview;
    private RelativeLayout mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status-bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide title-bar, must be before setContentView
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Requires RelativeLayout.
        mLayout = new RelativeLayout(this);
        setContentView(mLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the second argument by your choice.
        // Usually, 0 for back-facing camera, 1 for front-facing camera.
        // If the OS is pre-gingerbreak, this does not have any effect.
        mPreview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
        LayoutParams previewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // Un-comment below lines to specify the size.
        //previewLayoutParams.height = 500;
        //previewLayoutParams.width = 500;

        // Un-comment below line to specify the position.
        //mPreview.setCenterPosition(270, 130);

        mLayout.addView(mPreview, 0, previewLayoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mLayout.removeView(mPreview); // This is necessary.
        mPreview = null;
    }
}
*/


public class CameraActivityBackUp extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    ImageView btn_capture;
    ImageView facing_change;
    Camera camera1;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    public static boolean previewing = false;
    ImageView imageView;

    String folderName="SwapOut";
    Bitmap imgBitmap;
    public static boolean boFrontCam=false;
   ProgressDialog progressDialog =null;
    private Camera.Size  size;
    List<Camera.Size> mSupportedPreviewSizes;
    float mDist=0;
    LinearLayout capture_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surface);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));



        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceview);

        View C = findViewById(R.id.surfaceview);
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        parent.addView(new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent), index);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn_capture = (ImageView) findViewById(R.id.capture);

        facing_change = (ImageView) findViewById(R.id.facing_change);

        imageView=(ImageView)findViewById(R.id.imageView);

        capture_layout=(LinearLayout) findViewById(R.id.capture_layout);


      Intent intentImageSelectActivity = getIntent();

        if(intentImageSelectActivity.getStringExtra("Image")!=null) {
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(intentImageSelectActivity.getStringExtra("Image")));
                File file =new File(Uri.parse(intentImageSelectActivity.getStringExtra("ImagePath")).getPath());
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(imgBitmap!=null)
        {
            imageView.setImageBitmap(imgBitmap);
        }


//        if(!previewing){
//
//            camera1 = Camera.open();
//            if (camera1 != null){
//                try {
//                    setCameraDisplayOrientation(CameraActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK , camera1);
//
//                    Camera.Parameters params = camera1.getParameters();
////                    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
////                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
////                    } else {
////                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
////                    }
//                    List<Camera.Size> sizeList = camera1.getParameters().getSupportedPreviewSizes();
//                    Camera.Size selected  = sizeList.get(0);
//
//                    for(int i = 1; i < sizeList.size(); i++){
//                        if((sizeList.get(i).width * sizeList.get(i).height) >
//                                (selected.width * selected.height)){
//                            selected.width = sizeList.get(i).width;
//                            selected.height = sizeList.get(i).height;
//
//
//                        }
//                        Log.d("Size", selected.width +" "+ selected.height);
//                    }
//                    params.setZoom(0);
////                    params.setPreviewSize(selected.width,selected.height);
////                    params.setPictureSize(selected.width,selected.height);
//                    int height = surfaceView.getHeight();
//                    int width = (height * selected.width)/selected.height;
//                    Log.d("Size Surface:",width+" "+height);
//                    surfaceView.getHolder().setFixedSize(width,height);
//                    camera1.setParameters(params);
//                    camera1.startPreview();
//                    camera1.setPreviewDisplay(surfaceHolder);
//                    surfaceView.setFocusableInTouchMode(true);
//                    previewing = true;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        btn_capture.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(camera1 != null)
                {
                    camera1.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);

                }
            }
        });


        facing_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
if(!boFrontCam) {
    for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(camNo, camInfo);
        if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
            camera1.stopPreview();
            camera1.release();
            camera1 = Camera.open(camNo);
            if (camera1 != null) {
                try {
                    Camera.Parameters params = camera1.getParameters();
//                    if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                    } else {
//                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                    }
                    List<Camera.Size> sizeList = camera1.getParameters().getSupportedPreviewSizes();
                    Camera.Size selected  = sizeList.get(0);
                    for (int i = 1; i < sizeList.size(); i++) {
                        if ((sizeList.get(i).width * sizeList.get(i).height) >
                                (selected.width * selected.height)) {
                            selected.width = sizeList.get(i).width;
                            selected.height = sizeList.get(i).height;
                        }
                    }
                    params.setZoom(0);
                    params.setPreviewSize(selected.width,(selected.height));
                    params.setPictureSize(selected.width,(selected.height));
                    camera1.setParameters(params);
                    setCameraDisplayOrientation(CameraActivityBackUp.this, camNo, camera1);
                    camera1.setPreviewDisplay(surfaceHolder);
                    camera1.startPreview();
                    surfaceView.setFocusableInTouchMode(true);
                    previewing = true;
                    boFrontCam = true;
                    return;
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
        }

    }
    Toast.makeText(getApplicationContext(), "Your device don't have a front facing camera", Toast.LENGTH_SHORT).show();
}


            else{

                camera1.stopPreview();
                camera1.release();
                previewing=false;
    if(!previewing) {

        camera1 = Camera.open();
        if (camera1 != null) {
            try {

                Camera.Parameters params = camera1.getParameters();
//                if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                } else {
//                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                }
                List<Camera.Size> sizeList = camera1.getParameters().getSupportedPreviewSizes();
                Camera.Size selected = sizeList.get(0);

                for (int i = 1; i < sizeList.size(); i++) {
                    if ((sizeList.get(i).width * sizeList.get(i).height) >
                            (selected.width * selected.height)) {
                        selected.width = sizeList.get(i).width;
                        selected.height = sizeList.get(i).height;
                    }

                }
                Camera.Parameters parameters = camera1.getParameters();

                for (Camera.Size previewSize: camera1.getParameters().getSupportedPreviewSizes())
                {
                    // if the size is suitable for you, use it and exit the loop.
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    break;
                }


                params.setZoom(0);
                params.setPreviewSize(selected.width, selected.height);
                params.setPictureSize(selected.width, selected.height);
                camera1.setParameters(parameters);
                camera1.startPreview();
                camera1.setPreviewDisplay(surfaceHolder);
                setCameraDisplayOrientation(CameraActivityBackUp.this, Camera.CameraInfo.CAMERA_FACING_BACK, camera1);
                surfaceView.setFocusableInTouchMode(true);
                previewing = true;
                boFrontCam=false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

            }


            }
        });


    }



    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback(){

        public void onShutter() {

        }};

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback(){

        public void onPictureTaken(byte[] arg0, Camera arg1) {

        }};

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){

        public void onPictureTaken(final byte[] arg0, Camera arg1) {

        if(checkAndRequestPermissions()) {
            progressDialog = new ProgressDialog(CameraActivityBackUp.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Image Saving....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                    if (!imagesFolder.exists()) {
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                    }
                    File mypath = new File(imagesFolder, "temporary_file.png");

                    Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
                    bitmapPicture=Bitmap.createScaledBitmap(bitmapPicture, surfaceView.getHeight(), surfaceView.getWidth(), false);

                    ExifInterface exif = null;
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        exif = new ExifInterface(mypath.toString());
                        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                            if (!boFrontCam)
                                bitmapPicture = rotate(bitmapPicture, 90);
                            else
                                bitmapPicture = rotate(bitmapPicture, 180);
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                            bitmapPicture = rotate(bitmapPicture, 270);
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                            bitmapPicture = rotate(bitmapPicture, 180);
                        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                            if (!boFrontCam)
                                bitmapPicture = rotate(bitmapPicture, 90);
                            else
                                bitmapPicture = rotate(bitmapPicture, 90);
                        }

                        bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, fos);

                        fos.close();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    progressDialog.dismiss();

                    finish();
                }
            }).start();

        }

        }};


        public static Bitmap rotate(Bitmap bitmap, int degree) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            Matrix mtx = new Matrix();


            if (boFrontCam)
            {
                float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
                Matrix matrixMirrorY = new Matrix();
                matrixMirrorY.setValues(mirrorY);

                mtx.postConcat(matrixMirrorY);
            }

            mtx.postRotate(degree);

            return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);

        }


    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        if(previewing){
            camera1.stopPreview();
            previewing = false;
        }

        if (camera1 != null){
            try {
                camera1.setPreviewDisplay(surfaceHolder);
                camera1.startPreview();
                previewing = true;
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {


    }









    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {


        if(previewing) {
            camera1.stopPreview();
            camera1.release();
            camera1 = null;
            previewing = false;
        }
        else{
            camera1=null;
            previewing = false;
        }

    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }










    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = camera1.getParameters();
        int action = event.getAction();


        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                camera1.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        camera1.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera1.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt((x * x + y * y));
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private  boolean checkAndRequestPermissions() {
        int camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();

                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);


                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {

                        Toast.makeText(getApplicationContext(),"Your now eligible to access all app features",Toast.LENGTH_SHORT).show();

                    } else {


                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                ) {
                            showDialogOK("Service Permissions are required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:

                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }

                        else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
    private void explain(String msg){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.digiryte.swapout")));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }


}
