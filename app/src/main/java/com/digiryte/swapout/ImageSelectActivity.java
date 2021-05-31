package com.digiryte.swapout;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageSelectActivity extends Activity {

    ImageView camera,gallery;
    Bitmap imgBitmap;
    final int REQUEST_CAMERA=100;
    final int SELECT_FILE=101;
    Intent intentEditActivity=null;
    String folderName="SwapOut";
    Uri imgUri=null;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    String buttonTag="camera";
    File mypathCameraImage=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select_new);

        camera = (ImageView) findViewById(R.id.camera);
        gallery = (ImageView) findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTag="camera";
//                if(checkAndRequestPermissions()) {
//                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
//                    imagesFolder.mkdirs();
//                    imagesFolder.setWritable(true);
//                    mypathCameraImage = new File(imagesFolder,"Test"+ ".PNG");
//                    if(Build.VERSION.SDK_INT>23)
//                    imgUri = FileProvider.getUriForFile(ImageSelectActivity.this,
//                            BuildConfig.APPLICATION_ID + ".provider",
//                            mypathCameraImage);
//                    else
//                        imgUri=Uri.fromFile(mypathCameraImage);
//                    Log.e("Log",imgUri.toString());
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
//
//                    startActivityForResult(intent, REQUEST_CAMERA);
//                }
               startActivity(new Intent(ImageSelectActivity.this,InitCameraActivity.class));
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTag="gallery";
                if(checkAndRequestPermissions()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                }
            }
        });
    }





    private  boolean checkAndRequestPermissions() {
        int camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
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
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if(buttonTag.equals("gallery")&&perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                    }
                  else  if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED&&
                            perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Toast.makeText(getApplicationContext(),"Your now eligible to access all app features",Toast.LENGTH_SHORT).show();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                ||ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
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




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                if (data != null) {
            String realPath=null;
                         intentEditActivity = new Intent(this, EditActivity.class);
                         imgUri=data.getData();

                     if (Build.VERSION.SDK_INT < 19)
                        realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
                    else
                        realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                    imgUri = FileProvider.getUriForFile(ImageSelectActivity.this,
                             BuildConfig.APPLICATION_ID + ".provider",
                             new File(realPath));
                        intentEditActivity.putExtra("ImageFromGallery",imgUri.toString());
                        intentEditActivity.putExtra("FilePath","");
                        startActivity(intentEditActivity);
                }
            }

            else if (requestCode == REQUEST_CAMERA) {
                try {
                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                    imagesFolder.mkdirs();
                    imagesFolder.setWritable(true);
                    mypathCameraImage = new File(imagesFolder,"Test"+ ".PNG");

                    imgUri = FileProvider.getUriForFile(ImageSelectActivity.this,
                             BuildConfig.APPLICATION_ID + ".provider",
                             mypathCameraImage);
                    intentEditActivity = new Intent(this, EditActivity.class);
                    intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
                    intentEditActivity.putExtra("FilePath",mypathCameraImage.toString());
                    startActivity(intentEditActivity);
                }
                catch (NullPointerException e) {
                    Log.e("Exception",e.toString());
                }

            }
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

}
