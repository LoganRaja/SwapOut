package com.digiryte.swapout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EditActivityCopy extends AppCompatActivity implements View.OnTouchListener {

    Bitmap imgBitmap=null;
    Intent intentImageSelectActivity =null;

    private ImageView save,share;
    TextView eraser_size_text;
    BottomNavigationView bottomNavigationView;
    String folderName="SwapOut";
    LinearLayout hue_sat_lay,eraser_size_layout;
    AppCompatSeekBar hue,sat,eraser_size;
    FrameLayout frameLayout;
    DrawerLayout drawer;


    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;


    static final int DRAG = 1;
    static final int NONE = 0;
    private static final int REQUEST_SELECT_PICTURE = 1;
    private static final String TAG = "Touch";
    static final int ZOOM = 2;
    public static Bitmap bitmap;
    public static int brushSize = 30;
    public static Bitmap finalBitmap;
    public static Bitmap mainCanvasBitmap;
    int bottomButtonNumber = 3;
    Canvas canvas;
    int count = -1;
    Bitmap cutCanvasImage;
    HashMap<Integer, Integer> hashMap = new HashMap();
    ArrayList<Bitmap> imageArrayRedo = new ArrayList();
    ArrayList<Bitmap> imageArrayUndo = new ArrayList();
    float imageNewHeight;
    float imageNewWidth;
    float imageViewHeight;
    float imageViewWidth;
    ImageView imvBackground;
    ImageView imvSplashImage;
    Context mContext;
    private Path mPath = new Path();
    Bitmap mainImage;
    Matrix matrix = new Matrix();
    PointF mid = new PointF();
    int minimum = 20;
    int mode = NONE;
    float oldDist = 1.0f;
    Paint paint;
    public ArrayList<Path> paths = new ArrayList();
    Bitmap reSizeImage;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    Matrix tempMatrix = new Matrix();
    Paint tempPaint;
    float translateX;
    float translateY;
    private ArrayList<Path> undonePaths = new ArrayList();
    private ArrayList<String> undoneSize = new ArrayList();
    private ArrayList<String> redoneSize = new ArrayList();
    String shape="ROUND";

    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    ProgressDialog progressDialog =null;

    Toolbar toolbar;

    Button buttonShapeCircle,buttonShapeSquare;
    boolean cameraview=false;
    File mypathBg=null;

    float satValue=1,hueValue=0;

    Bitmap bgBitmap=null;
    Bitmap tempBitmap=null;
    Bitmap hueBitmap = null;

    int eraseCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        this.imvBackground=(ImageView)findViewById(R.id.imvBackground);
        this.imvSplashImage=(ImageView)findViewById(R.id.imvSplashImage);
         this.save=(ImageView)findViewById(R.id.save);
         this.share=(ImageView)findViewById(R.id.share);
         this.hue_sat_lay=(LinearLayout)findViewById(R.id.hue_sat_lay);
        this.eraser_size_layout=(LinearLayout)findViewById(R.id.eraser_size_layout);
         this.hue=(AppCompatSeekBar)findViewById(R.id.hue);
        this.sat=(AppCompatSeekBar)findViewById(R.id.sat);
        this.eraser_size=(AppCompatSeekBar)findViewById(R.id.eraser_size);

        this.eraser_size_text=(TextView)findViewById(R.id.eraser_size_text);

        this.buttonShapeSquare=(Button) findViewById(R.id.buttonShapeSquare);

        this.buttonShapeCircle=(Button) findViewById(R.id.buttonShapeCircle);

        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        sat.setProgress(256);

        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 hueValue = (float) hue.getProgress();
                updateHueSat(satValue,hueValue);
            }
        });

        sat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
       if (imgBitmap != null) {
            satValue= (float) sat.getProgress() / 256;
            updateHueSat(satValue,hueValue);
        }
    }
});

        eraser_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                brushSize=progress;
               eraser_size_text.setText(Integer.toString(brushSize));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonShapeCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(shape.equals("SQUARE"))
                {
                    shape="ROUND";
                    buttonShapeCircle.setBackgroundResource(R.drawable.eraser_circle_select);
                    buttonShapeSquare.setBackgroundResource(R.drawable.eraser_square_unselect);
                }
            }
        });

        buttonShapeSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shape.equals("ROUND"))
                {
                    shape="SQUARE";
                    buttonShapeSquare.setBackgroundResource(R.drawable.eraser_square_select);
                    buttonShapeCircle.setBackgroundResource(R.drawable.eraser_circle_unselect);
                }
            }
        });

        intentImageSelectActivity = getIntent();
        if(intentImageSelectActivity.getStringExtra("ImageFromGallery")!=null) {
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(intentImageSelectActivity.getStringExtra("ImageFromGallery")));

                File file = new File(intentImageSelectActivity.getStringExtra("FilePath"));
                file.delete();

                bitmap = imgBitmap;
               new BitmapFactory.Options().inJustDecodeBounds = true;
        this.mainImage = bitmap;
        this.reSizeImage = this.getResizedBitmap(this.mainImage).copy(Bitmap.Config.ARGB_8888, true);
        this.cutCanvasImage = this.getResizedBitmap(this.mainImage).copy(Bitmap.Config.ARGB_8888, true);
        this.hueBitmap = this.cutCanvasImage;
        mainCanvasBitmap = Bitmap.createBitmap(this.reSizeImage.getWidth(), this.reSizeImage.getHeight(), Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(mainCanvasBitmap);
        this.canvas.drawBitmap(this.reSizeImage, 0.0f, 0.0f, null);
        this.imvSplashImage.setImageBitmap(mainCanvasBitmap);

        this.imvSplashImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imvSplashImage.getViewTreeObserver().removeOnPreDrawListener(this);
                imageViewHeight = (float) imvSplashImage.getMeasuredHeight();
                imageViewWidth = (float)  imvSplashImage.getMeasuredWidth();
                translateX = (imageViewWidth - ((float) imvSplashImage.getDrawable().getIntrinsicWidth())) / 0.0f;
                translateY = (imageViewHeight - ((float) imvSplashImage.getDrawable().getIntrinsicHeight())) / 0.0f;
                Drawable d = imvSplashImage.getDrawable();

                RectF imageRectF = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                RectF viewRectF = new RectF(0, 0, imvSplashImage.getWidth(), imvSplashImage.getHeight());
                matrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
                imvSplashImage.setImageMatrix(matrix);
                return true;
            }
        });
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                Log.e("Exceptions",e.toString());
            }
        }
        this.imvSplashImage.setOnTouchListener(this) ;





        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.zoom:
                                bottomButtonNumber = 5;
                                hue_sat_lay.setVisibility(View.GONE);
                                eraser_size_layout.setVisibility(View.GONE);
                                break;
                            case R.id.eraser:
                                bottomButtonNumber = 3;
                                hue_sat_lay.setVisibility(View.GONE);
                                eraser_size_layout.setVisibility(View.VISIBLE);
                                break;
                            case R.id.flip:
                               if(bgBitmap!=null)
                               {
                                   AlertDialog.Builder builder = new AlertDialog.Builder(EditActivityCopy.this);
                                   builder.setTitle("Alert ").
                                          setMessage("If flip is applied your not undo your erase\nDo you want continue?")
                                           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                               public void onClick(DialogInterface dialog, int id) {
                                                   tempBitmap=bgBitmap;
                                                   bgBitmap=mainCanvasBitmap;
                                                   mainImage=tempBitmap;
                                                   reSizeImage = getResizedBitmap(mainImage).copy(Bitmap.Config.ARGB_8888, true);
                                                   cutCanvasImage = getResizedBitmap(mainImage).copy(Bitmap.Config.ARGB_8888, true);
                                                   hueBitmap = cutCanvasImage;
                                                   mainCanvasBitmap = Bitmap.createBitmap(reSizeImage.getWidth(), reSizeImage.getHeight(), Bitmap.Config.ARGB_8888);
                                                   canvas = new Canvas(mainCanvasBitmap);
                                                   canvas.drawBitmap(reSizeImage, 0.0f, 0.0f, null);
                                                   imvSplashImage.setImageBitmap(mainCanvasBitmap);
                                                   Matrix matrix=new Matrix();
                                                   Drawable d = imvSplashImage.getDrawable();
                                                   RectF imageRectF = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                                   RectF viewRectF = new RectF(0, 0, imvSplashImage.getWidth(), imvSplashImage.getHeight());
                                                   matrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
                                                   imvSplashImage.setImageMatrix(matrix);
                                                   imvBackground.setImageBitmap(bgBitmap);
                                                   paths.clear();
                                                   undonePaths.clear();
                                                   imageArrayRedo.clear();
                                                   redoneSize.clear();
                                               }
                                           })
                                           .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                               public void onClick(DialogInterface dialog, int id) {
                                                   dialog.cancel();
                                               }
                                           }).show();

                               }
                               break;
                            case R.id.hue_sat:
                                bottomButtonNumber=NONE;
                                hue_sat_lay.setVisibility(View.VISIBLE);
                                eraser_size_layout.setVisibility(View.GONE);
                                break;
                            case R.id.add_bg:
                                bottomButtonNumber=NONE;
                                hue_sat_lay.setVisibility(View.GONE);
                                eraser_size_layout.setVisibility(View.GONE);
                                selectImage();
                                break;
                        }
                        return true;
                    }
                });








         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       drawer.setDrawerListener(toggle);
        toggle.syncState();
        if(!drawer.isDrawerOpen(GravityCompat.START))
         drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
        {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                else{
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("StatementWithEmptyBody")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.about_us:
                        startActivity(new Intent(getApplicationContext(),AboutUsActivity.class));
                        break;
                    case R.id.t_a_c:
                        startActivity(new Intent(getApplicationContext(),TermsAndConditionsActivity.class));
                        break;
                    case R.id.p_p:
                        startActivity(new Intent(getApplicationContext(),PrivacyPolicyActivity.class));
                        break;
                }


                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkAndRequestPermissions()) {
                    new ShareImage().execute();
                }

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions()) {
                    new ImageSaved().execute();
                }
            }
        });





    }





    private void selectImage() {

        final CharSequence[] options = { "Background Camera","Device Camera", "Choose from Gallery","Remove Background" };

        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivityCopy.this);
        builder.setTitle("Add Photo!");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Remove Background")) {
                    bgBitmap = null;
                    imvBackground.setImageBitmap(null);

                }
               else if(checkAndRequestPermissions()) {

                    if (options[item].equals("Background Camera")) {
                        imvBackground.setImageBitmap(null);

                        frameLayout.destroyDrawingCache();
                        frameLayout.setDrawingCacheEnabled(true);
                        frameLayout.buildDrawingCache();

                        Bitmap bm = Bitmap.createBitmap(frameLayout.getDrawingCache());
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                        mypathBg = new File(imagesFolder, "temporary_file.png");
                        try {
                            mypathBg.createNewFile();
                            new FileOutputStream(mypathBg).write(bytes.toByteArray());
                            Intent intentCameraSurfaceActivity = new Intent(getApplicationContext(), CameraActivity.class);
                            intentCameraSurfaceActivity.putExtra("Image", FileProvider.getUriForFile(EditActivityCopy.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg).toString());
                            intentCameraSurfaceActivity.putExtra("ImagePath",mypathBg.toString());
                            startActivity(intentCameraSurfaceActivity);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    else if (options[item].equals("Device Camera")) {

                        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                        mypathBg = new File(imagesFolder, "temporary_file.png");
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(EditActivityCopy.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg));
                        startActivityForResult(intent, 1);
                    }
                    else if (options[item].equals("Choose from Gallery")) {

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);

                    }

                }

            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    try {
                        imvSplashImage.setImageBitmap(mainCanvasBitmap);
                        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                        mypathBg = new File(imagesFolder, "temporary_file.png");
                        Log.e("Result OK",mypathBg.getPath().toString());
                        this.imvBackground.setImageBitmap(null);
                        this.bgBitmap = BitmapFactory.decodeFile(mypathBg.getPath());
                        this.imvBackground.setImageBitmap(this.bgBitmap);
                        mypathBg.delete();
                    }
                    catch (Exception e)
                    {
                        /*File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                        mypathBg = new File(imagesFolder, "temporary_file.png");
                        Log.e("My Path Bg", String.valueOf(mypathBg));
                        this.imvBackground.setImageBitmap(null);
                        this.bgBitmap = BitmapFactory.decodeFile(mypathBg.getPath());
                        this.imvBackground.setImageBitmap(this.bgBitmap);
                        mypathBg.delete();*/
                        Log.e("Exception",e.toString());
                    }
                }
                else if(resultCode==RESULT_CANCELED)
                {
                    imvSplashImage.setImageBitmap(mainCanvasBitmap);
                    Log.e("REsult","Canceled");
                }

                break;
            case 2:
                if(resultCode == RESULT_OK){

                    try {
                        Uri selectedImage = data.getData();
                        InputStream imageStream = null;
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        this.bgBitmap= BitmapFactory.decodeStream(imageStream);
                        this.imvBackground.setImageBitmap(null);
                        this.imvBackground.setImageBitmap(this.bgBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;


        }
    }



    public void updateHueSat(float sat,float hue) {

        Bitmap hsbitmap = this.hueBitmap;

        int w = hsbitmap.getWidth();
        int h = hsbitmap.getHeight();
        Bitmap bitmapResult =
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);
        Paint paint = new Paint();
        ColorMatrix mat = new ColorMatrix();
        mat.setSaturation(sat);
        adjustHue(mat, hue);
        paint.setColorFilter(new ColorMatrixColorFilter(mat));
        canvasResult.drawBitmap(hsbitmap, 0, 0, paint);
        this.canvas.drawBitmap(bitmapResult,0,0,null);
        this.imvSplashImage.postInvalidate();
    }


    public static void adjustHue(ColorMatrix cm, float value)
    {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;

        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]
                {
                        lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                        0f, 0f, 0f, 1f, 0f,
                        0f, 0f, 0f, 0f, 1f };
        cm.postConcat(new ColorMatrix(mat));
    }

    private static float cleanValue(float p_val, float p_limit)
    {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.undo:
                onClickUndo();
                break;
            case R.id.redo:
                onClickRedo();
                break;
    }

        return true;
    }



    public void onBackPressed() {
        alertShow(EditActivityCopy.this);
    }

    @TargetApi(18)
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.imvSplashImage /*2131624068*/:
                if (this.bottomButtonNumber == 5 || this.bottomButtonNumber == 4) {
                    ImageView view = (ImageView) v;
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    switch (event.getAction() & 255) {
                        case NONE /*0*/:
                            this.savedMatrix.set(this.matrix);
                            this.start.set(event.getX(), event.getY());
                            mode = DRAG;
                            lastEvent = null;

                            this.mode = REQUEST_SELECT_PICTURE;
                            break;

                        case REQUEST_SELECT_PICTURE /*1*/:
                        case 6 /*6*/:
                            this.mode = NONE;
                            lastEvent = null;
                            break;
                        case ZOOM /*2*/:

                            if (mode == REQUEST_SELECT_PICTURE) {
                                this.matrix.set(this.savedMatrix);
                                float dx = event.getX() - this.start.x;
                                float dy = event.getY() - this.start.y;
                                this.matrix.postTranslate(dx, dy);
                            }


                            if (this.mode == ZOOM) {
                                float newDist=spacing(event);

                                if (newDist > 10.0f) {
                                    this.matrix.set(this.savedMatrix);
                                    float scale = newDist / this.oldDist;
                                    matrix.postScale(scale, scale, this.mid.x, this.mid.y);
                                    float[] values = new float[9];
                                    this.matrix = this.tempMatrix;
                                    this.matrix.getValues(values);
                                    float width = values[NONE] * ((float) this.reSizeImage.getWidth()/2);
                                    float height = values[4] * ((float) this.reSizeImage.getHeight()/2);

                                    if (newDist >= this.oldDist) {
                                        this.matrix.postScale(scale, scale, this.mid.x, this.mid.y);

                                    }

                                }


                                if (lastEvent != null && event.getPointerCount() == 2 || event.getPointerCount() == 3) {
                                    newRot = rotation(event);
                                    float r = newRot - d;
                                    float[] values = new float[9];
                                    matrix.getValues(values);
                                    float tx = values[2];
                                    float ty = values[5];
                                    float sx = values[0];
                                    float xc = (view.getWidth() / 2) * sx;
                                    float yc = (view.getHeight() / 2) * sx;
                                    this.matrix.postRotate(r, tx + xc, ty + yc);
                                }
                            }

                            break;
                        case 5 /*5*/:

                            this.oldDist = spacing(event);
                            if (this.oldDist > 10.0f) {
                                this.savedMatrix.set(this.matrix);
                                midPoint(this.mid, event);
                                this.mode = ZOOM;
                            }
                            lastEvent = new float[4];
                            lastEvent[0] = event.getX(0);
                            lastEvent[1] = event.getX(1);
                            lastEvent[2] = event.getY(0);
                            lastEvent[3] = event.getY(1);
                            d = rotation(event);
                            break;
                    }
                    view.setImageMatrix(this.matrix);
                    this.imvSplashImage.invalidate();
                    this.imvBackground.setImageMatrix(this.matrix);
                    this.imvBackground.invalidate();
                }
                if (this.bottomButtonNumber == ZOOM || this.bottomButtonNumber == 3) {
                    this.tempPaint = new Paint();
                    this.tempPaint.setDither(false);
                    this.tempPaint.setFilterBitmap(true);
                    this.tempPaint.setAntiAlias(false);
                    if (this.bottomButtonNumber == 3) {
                        this.tempPaint.setStrokeWidth((float) brushSize);
                        this.tempPaint.setStyle(Paint.Style.STROKE);
                        if(shape.equals("ROUND")) {
                            this.tempPaint.setStrokeCap(Paint.Cap.ROUND);
                            this.tempPaint.setStrokeJoin(Paint.Join.ROUND);
                        }
                        else {
                            this.tempPaint.setStrokeCap(Paint.Cap.SQUARE);
                            this.tempPaint.setStrokeJoin(Paint.Join.MITER);
                        }
                        this.tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    } else if (this.bottomButtonNumber == ZOOM) {
                        this.tempPaint.setColor(Color.BLUE);
                        this.tempPaint.setStrokeWidth(20.0f);
                        this.tempPaint.setStyle(Paint.Style.STROKE);
                        this.tempPaint.setStrokeJoin(Paint.Join.ROUND);
                        this.tempPaint.setStrokeCap(Paint.Cap.ROUND);
                        this.paint = new Paint();
                        this.paint.setDither(false);
                        this.paint.setFilterBitmap(true);
                        this.paint.setAntiAlias(false);
                        this.paint.setStrokeWidth(20.0f);
                        this.paint.setStyle(Paint.Style.FILL);
                        this.paint.setStrokeJoin(Paint.Join.ROUND);
                        this.paint.setStrokeCap(Paint.Cap.ROUND);
                    }
                    this.undonePaths.clear();
                    this.imageArrayRedo.clear();


                    this.redoneSize.clear();


                    Matrix inverse = new Matrix();
                    this.imvSplashImage.getImageMatrix().invert(inverse);
                    float[] touchPoint = new float[ZOOM];
                    touchPoint[NONE] = event.getX();
                    touchPoint[REQUEST_SELECT_PICTURE] = event.getY();
                    inverse.mapPoints(touchPoint);
                    float touchX = touchPoint[NONE];
                    float touchY = touchPoint[REQUEST_SELECT_PICTURE];
                    switch (event.getAction()) {
                        case NONE /*0*/:
                            this.mPath.reset();
                            this.mPath.moveTo(touchX, touchY);
                            this.paths.add(this.mPath);

                            this.count += REQUEST_SELECT_PICTURE;
                            this.hashMap.put(Integer.valueOf(this.count), Integer.valueOf(this.bottomButtonNumber));
                            if (this.bottomButtonNumber != 3) {
                                if (this.bottomButtonNumber == ZOOM) {
                                    this.canvas.drawPath(this.mPath, this.tempPaint);
                                    break;
                                }
                            }
                            this.canvas.drawPath(this.mPath, this.tempPaint);
                            this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            break;
                        case REQUEST_SELECT_PICTURE /*1*/:
                            this.mPath.lineTo(touchX, touchY);
                            if (this.bottomButtonNumber == 3) {
                                this.canvas.drawPath(this.mPath, this.tempPaint);
                                this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            } else if (this.bottomButtonNumber == ZOOM) {
                                this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                                this.canvas.drawPath(this.mPath, this.paint);
                                this.canvas.drawBitmap(this.cutCanvasImage, 0.0f, 0.0f, this.paint);
                                this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            }
                            try{
                                this.imageArrayUndo.add(this.cutCanvasImage);
                                this.undoneSize.add(brushSize+","+shape+","+satValue+","+hueValue);
                            }
                            catch(Exception e){

                                this.imageArrayUndo.remove(0);
                                this.undoneSize.remove(0);

                                this.imageArrayUndo.add(this.cutCanvasImage);
                                this.undoneSize.add(brushSize+","+shape+","+satValue+","+hueValue);

                            }
                            this.hueBitmap = null;
                            this.hueBitmap = this.cutCanvasImage;
                            this.mPath = new Path();
                            break;
                        case ZOOM /*2*/:
                            this.mPath.lineTo(touchX, touchY);
                            if (this.bottomButtonNumber != 3) {
                                if (this.bottomButtonNumber == ZOOM) {
                                    this.canvas.drawPath(this.mPath, this.tempPaint);
                                    break;
                                }
                            }
                            this.canvas.drawPath(this.mPath, this.tempPaint);
                            this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            break;
                        default:
                            return false;
                    }

                    eraseCount++;

                    this.imvSplashImage.invalidate();
                    break;
                }
                break;
        }
        return true;
    }


    public Bitmap getResizedBitmap(Bitmap mainImage) {
        int mainImageWidth = mainImage.getWidth();
        int mainImageHeight = mainImage.getHeight();
        Display display = getWindowManager().getDefaultDisplay();
        float displayWidth = (float) display.getWidth();
        float currentDisplayHeight = ((((float) display.getHeight()) - toolbar.getHeight()) - bottomNavigationView.getHeight()) ;
        getAspectRatio((float) mainImageWidth, (float) mainImageHeight, displayWidth, currentDisplayHeight);
        float scaleWidth = this.imageNewWidth / ((float) mainImageWidth);
        float scaleHeight = this.imageNewHeight / ((float) mainImageHeight);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(mainImage, NONE, NONE, mainImageWidth, mainImageHeight, matrix, true);
    }

    public void getAspectRatio(float iWidth, float iHeight, float sWidth, float sHeight) {
        if (iWidth < iHeight) {
            if (iWidth >= sWidth && iHeight >= sHeight) {
                this.imageNewHeight = sHeight - 100.0f;
                this.imageNewWidth = (float) ((int) (((double) iWidth) * ((double) (this.imageNewHeight / iHeight))));
            } else if (iWidth >= sWidth && iHeight <= sHeight) {
                this.imageNewWidth = sWidth;
                this.imageNewHeight = (float) ((int) (((double) iHeight) * (((double) this.imageNewWidth) / ((double) iWidth))));
            } else if (iWidth <= sWidth && iHeight <= sHeight) {
                this.imageNewHeight = iHeight;
                this.imageNewWidth = iWidth;
            } else if (iWidth <= sWidth && iHeight >= sHeight) {
                this.imageNewHeight = sHeight - 100.0f;
                this.imageNewWidth = (float) ((int) (((double) iWidth) * (((double) this.imageNewHeight) / ((double) iHeight))));
            }
        } else if (iWidth < iHeight) {
            this.imageNewWidth = iWidth;
            this.imageNewHeight = iHeight;
        } else if (iWidth >= sWidth && iHeight >= sHeight) {
            this.imageNewWidth = sWidth;
            this.imageNewHeight = (float) ((int) (((double) (iHeight / 100.0f)) * (((double) this.imageNewWidth) / (((double) iWidth) / 100.0d))));
        } else if (iWidth <= sWidth && iHeight >= sHeight) {
            this.imageNewHeight = sHeight;
            this.imageNewWidth = (float) ((int) (((double) iWidth) * (((double) this.imageNewHeight) / ((double) iHeight))));
        } else if (iWidth <= sWidth && iHeight <= sHeight) {
            this.imageNewWidth = iWidth;
            this.imageNewHeight = iHeight;
        } else if (iWidth >= sWidth && iHeight <= sHeight) {
            this.imageNewWidth = sWidth;
            this.imageNewHeight = (float) ((int) (((double) (iHeight / 100.0f)) * (((double) this.imageNewWidth) / (((double) iWidth) / 100.0d))));
        }
    }




    private void onClickUndo() {
        int mSize = this.paths.size();
        if (mSize > 0) {
            Bitmap imagePrevious;
           if (mSize > REQUEST_SELECT_PICTURE) {
                imagePrevious = (Bitmap) this.imageArrayUndo.get(mSize - 2);
                this.count--;
            } else {
                imagePrevious = this.reSizeImage;
                this.count = -1;
            }
            this.canvas.drawBitmap(imagePrevious, 0.0f, 0.0f, null);
            this.cutCanvasImage = imagePrevious;
            this.imageArrayRedo.add(this.imageArrayUndo.get(mSize - 1));
            this.undonePaths.add(this.paths.remove(mSize - 1));
            this.imageArrayUndo.remove(mSize - 1);
            this.redoneSize.add(this.undoneSize.remove(mSize - 1));
            this.imvSplashImage.invalidate();
            return;
        }
        Dialog(this.mContext, " Undo Completed", " Ok");
    }

    public void onClickRedo() {
        int rSize = this.undonePaths.size();
        if (rSize > 0) {
            Bitmap imageNext = (Bitmap) this.imageArrayRedo.get(rSize - 1);
            Path redoPath = (Path) this.undonePaths.get(rSize - 1);
            String[] size_shape = this.redoneSize.get(rSize - 1).split(",");
            Float bSize = Float.valueOf(size_shape[0]);
            int buttonNumberFromHashMap = ((Integer) this.hashMap.get(Integer.valueOf(this.count + REQUEST_SELECT_PICTURE))).intValue();
            if (buttonNumberFromHashMap == 3) {
                Paint redoPaint = new Paint();
                redoPaint.setDither(false);
                redoPaint.setFilterBitmap(true);
                redoPaint.setAntiAlias(false);
                redoPaint.setStrokeWidth(bSize);
                redoPaint.setStyle(Paint.Style.STROKE);
                if(size_shape[1].equals("ROUND")){
                    redoPaint.setStrokeJoin(Paint.Join.ROUND);
                    redoPaint.setStrokeCap(Paint.Cap.ROUND);
                }
                else{
                    redoPaint.setStrokeJoin(Paint.Join.MITER);
                    redoPaint.setStrokeCap(Paint.Cap.SQUARE);
                }

                redoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                this.canvas.drawPath(redoPath, redoPaint);
            }

            this.cutCanvasImage = imageNext;
            this.imageArrayUndo.add(imageNext);
            this.imageArrayRedo.remove(rSize - 1);
            this.paths.add(this.undonePaths.remove(this.undonePaths.size() - 1));

            this.undoneSize.add(this.redoneSize.remove(rSize - 1));
            this.count += REQUEST_SELECT_PICTURE;

            this.imvSplashImage.invalidate();

            return;
        }
        Dialog(getApplicationContext(), " Redo  Completed", " Ok");
    }


    public void alertShow(Context mContext) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder.setMessage("All unsaved Changes will be lost!!! Do you really want to start over again?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Process.killProcess(Process.myPid());
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }

    public void Dialog(Context mContext, String message, String negativieButton) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(EditActivityCopy.this);
        alertDialogBuilder.setTitle(BuildConfig.FLAVOR);
        alertDialogBuilder.setMessage(message).setCancelable(false).setNegativeButton(negativieButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }



    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        float s=x * x + y * y;
        return (float) Math.sqrt(s);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);

    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }



    @Override
    public void onPause()
    {
          cameraview=true;
     super.onPause();

    }



    @Override
    public void onResume()
    {
        if(cameraview)
        {
            if(mypathBg!=null) {
                if (mypathBg.exists()) {
                    setBackGround();
                }
                else{
                    if(bgBitmap!=null)
                        imvBackground.setImageBitmap(bgBitmap);
                }
            }

        }
        super.onResume();

    }

    public void setBackGround()
    {
        imvBackground.setImageBitmap(null);
       bgBitmap= BitmapFactory.decodeFile(mypathBg.getPath());
        imvBackground.setImageBitmap(bgBitmap);
        //mypathBg.delete();
    }


    class ImageSaved extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditActivityCopy.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Image Saving....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            frameLayout.destroyDrawingCache();
            frameLayout.setDrawingCacheEnabled(true);
            finalBitmap=Bitmap.createBitmap(frameLayout.getDrawingCache());

        }
        @Override
        protected String doInBackground(Void... arg0)
        {



                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName),"");
                    imagesFolder.mkdirs();
                    imagesFolder.setWritable(true);
                    File mypath=new File(imagesFolder,new SimpleDateFormat("yyyyMMMddHmmss").format(Calendar.getInstance().getTime()) + ".png");

                    int height = finalBitmap.getHeight();
                    int width = finalBitmap.getWidth();
                    double y = Math.sqrt(1200000
                            / (((double) width) / height));
                    double x = (y / height) * width;


                    BitmapFactory.Options opts = new BitmapFactory.Options();
                  Bitmap  watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark,opts);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(watermark, width, height, true);

                    Canvas canvas = new Canvas(finalBitmap);
                    canvas.drawBitmap(scaledBitmap,0,0,null);


                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();

                        Thread.currentThread().interrupt();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            final Uri contentUri = FileProvider.getUriForFile(EditActivityCopy.this,BuildConfig.APPLICATION_ID + ".provider", mypath);
                            scanIntent.setData(contentUri);
                            sendBroadcast(scanIntent);
                        } else {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                        }
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
            Toast.makeText(getApplicationContext(),"Image saved", Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);

        }
    }



    class ShareImage extends AsyncTask<Void, Void, String> {
        Bitmap  bm=null;
        File mypathTemp=null;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditActivityCopy.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Please Wait....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            frameLayout.destroyDrawingCache();
            frameLayout.setDrawingCacheEnabled(true);
            bm = Bitmap.createBitmap(frameLayout.getDrawingCache());

        }
        @Override
        protected String doInBackground(Void... arg0)
        {

            Bitmap watermark;
            int height = bm.getHeight();
            int width = bm.getWidth();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark,opts);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(watermark, width, height, true);

            Canvas canvas = new Canvas(bm);
            canvas.drawBitmap(scaledBitmap,0,0,null);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName),"");
            imagesFolder.mkdirs();
            imagesFolder.setWritable(true);
             mypathTemp=new File(imagesFolder,"temporary.png");

            if(mypathTemp.exists())
            {
                mypathTemp.delete();
            }

            try {
                mypathTemp.createNewFile();
                new FileOutputStream(mypathTemp).write(bytes.toByteArray());
                progressDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(EditActivityCopy.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    mypathTemp));
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_TEXT, "SwapOut");
            startActivityForResult((Intent.createChooser(share, "SwapOut")),3);
            super.onPostExecute(result);

        }
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
