package com.digiryte.swapout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RotationActivity extends AppCompatActivity {
    Intent intentImageSelectActivity = null;
    Bitmap imgBitmap = null,bitmapForSave = null;
    ImageView imageView;
    ImageView rotationImage,finishImage;
    int rotateDegree=0;
    ProgressDialog progressDialog =null;
    String folderName="SwapOut";
    Uri imgUri=null;
    File mypathBg=null;
    Intent intentEditActivity=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));


        imageView = (ImageView) findViewById(R.id.imageView);
        rotationImage = (ImageView) findViewById(R.id.rotationImage);
        finishImage = (ImageView) findViewById(R.id.finishImage);

        intentImageSelectActivity = getIntent();
        if (intentImageSelectActivity.getStringExtra("ImageFromGallery") != null) {
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(intentImageSelectActivity.getStringExtra("ImageFromGallery")));
                File file = new File(intentImageSelectActivity.getStringExtra("FilePath"));
                file.delete();
                imageView.setImageBitmap(imgBitmap);
                bitmapForSave=imgBitmap;
            } catch (Exception e) {

            }
        }

        rotationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((rotateDegree+=90)==360) {
                    rotateDegree=0;
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotateDegree,
                        imgBitmap.getWidth(), imgBitmap.getHeight());

                bitmapForSave = Bitmap.createBitmap(
                        imgBitmap,
                        0, 0,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        matrix, true);

                imageView.setImageBitmap(bitmapForSave);

            }
        });

        finishImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageSaved().execute();
            }
        });

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

    class ImageSaved extends AsyncTask<Void, Void, String> {
        Uri contentUri=null;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RotationActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Image Saving....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(Void... arg0)
        {

            File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
            imagesFolder.mkdirs();
            imagesFolder.setWritable(true);
            if(intentImageSelectActivity.getStringExtra("EditOrNot")!=null)
              mypathBg = new File(imagesFolder, "temporary_file.png");
            else
                mypathBg = new File(imagesFolder,"Test"+ ".PNG");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypathBg);
                bitmapForSave.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                progressDialog.dismiss();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  "";
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            if(intentImageSelectActivity.getStringExtra("EditOrNot")!=null) {
                finish();
            }
            else{
                imgUri = FileProvider.getUriForFile(RotationActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        mypathBg);
                intentEditActivity = new Intent(getApplicationContext(), EditActivity.class);
                intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
                intentEditActivity.putExtra("FilePath",mypathBg.toString());
                startActivity(intentEditActivity);
                finish();
            }
        }
    }


}
