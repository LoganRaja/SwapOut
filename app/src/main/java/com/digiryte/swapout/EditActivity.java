package com.digiryte.swapout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.graphics.drawable.BitmapDrawable;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digiryte.swapout.util.IabHelper;
import com.digiryte.swapout.util.IabResult;
import com.digiryte.swapout.util.Inventory;
import com.digiryte.swapout.util.Purchase;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.logo;
import static android.R.attr.pivotX;
import static android.R.attr.pivotY;
import static android.R.attr.thickness;


public class EditActivity extends AppCompatActivity implements View.OnTouchListener {

    Bitmap imgBitmap= null;
    Intent intentImageSelectActivity = null;
    Intent intentEditActivity=null;

    private ImageView save,share;
    TextView eraser_size_text;
    BottomNavigationView bottomNavigationView;
    String folderName="SwapOut";
    LinearLayout hue_sat_lay,hue_sat_lay_bg,eraser_size_layout,rotate_view_layout;
    AppCompatSeekBar hue,sat,hue_bg,sat_bg,eraser_size;
    FrameLayout frameLayout;
    DrawerLayout drawer;
    String typeOfStorage=null;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;


    static final int DRAG = 1;
    static final int NONE = 0;
    private static final int REQUEST_SELECT_PICTURE = 1;
    static final int ZOOM = 2;
    public static Bitmap bitmap;
    public static int brushSize = 30;
    public static Bitmap finalBitmap;
    public static Bitmap mainCanvasBitmap;
    int bottomButtonNumber = 5;
    Canvas canvas;
    int count = -1;
    Bitmap cutCanvasImage;
    HashMap<Integer, Integer> hashMap = new HashMap();
    ArrayList<Bitmap> imageArrayRedo = new ArrayList();
    ArrayList<Bitmap> imageArrayUndo = new ArrayList();

    // for background image undo redo
    ArrayList<Bitmap> bgImageArrayRedo = new ArrayList();
    ArrayList<Bitmap> bgImageArrayUndo = new ArrayList();

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
    Matrix matrixBg = new Matrix();
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

    Button buttonShapeCircle,buttonShapeSquare,rotate_clock_wise,rotate_anticlock_wise;
    boolean cameraview=false;
    File mypathBg=null;
    File mypathCameraImage=null;
    Uri imgUri=null;

    float satValue=1,hueValue=0;
    float satValueBG=1,hueValueBG=0;
    float foreGroundRotation = 0;

    Bitmap bgBitmap=null;
    Bitmap tempBitmap=null;
    Bitmap hueBitmap = null;

    int eraseCount = 0;
    boolean backgroundImageSelect=false;

    SharedPreferences pref = null;
    SharedPreferences.Editor editor = null;

    private static final String TAG = "com.digiryte.swapout";
    IabHelper mHelper;
    //    static final String ITEM_SKU = "android.test.purchased";
    static final String ITEM_SKU = "com.example.billing";

    NavigationView navigationView=null;


    int widthFrame=0,widthLay=0,heightFrame=0,heightLay=0;
    ViewGroup.LayoutParams paramsImage=null;

    float[] foregroundMatrixValues = new float[9];
    boolean pushRedo = true;
    Matrix originalForegroundMatrix = new Matrix();

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
        this.hue_sat_lay_bg=(LinearLayout)findViewById(R.id.hue_sat_lay_bg);
        this.rotate_view_layout=(LinearLayout)findViewById(R.id.rotate_view_layout);
        this.eraser_size_layout=(LinearLayout)findViewById(R.id.eraser_size_layout);
        this.hue=(AppCompatSeekBar)findViewById(R.id.hue);
        this.sat=(AppCompatSeekBar)findViewById(R.id.sat);
        this.hue_bg=(AppCompatSeekBar)findViewById(R.id.hue_bg);
        this.sat_bg=(AppCompatSeekBar)findViewById(R.id.sat_bg);
//        this.rotateImageSeekBar=(AppCompatSeekBar)findViewById(R.id.rotateImageSeekBar);
        this.eraser_size=(AppCompatSeekBar)findViewById(R.id.eraser_size);

        this.eraser_size_text=(TextView)findViewById(R.id.eraser_size_text);

        this.buttonShapeSquare=(Button) findViewById(R.id.buttonShapeSquare);

        this.buttonShapeCircle=(Button) findViewById(R.id.buttonShapeCircle);

        this.rotate_clock_wise = (Button) findViewById(R.id.rotate_clock_wise);
        this.rotate_anticlock_wise = (Button) findViewById(R.id.rotate_anticlock_wise);

        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        sat.setProgress(256);

        sat_bg.setProgress(256);

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
                if(backgroundImageSelect&&(bgBitmap!=null)) {
                    updateHueSatForBackgroundImage(satValue,hueValue,true,false);
                }else
                    updateHueSat(satValue,hueValue,true,true);
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
                    if(backgroundImageSelect&&(bgBitmap!=null)) {
                        updateHueSatForBackgroundImage(satValue,hueValue,true,false);
                    }else
                        updateHueSat(satValue,hueValue,true,true);
                }
            }
        });


        hue_bg.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                hueValueBG = (float) hue_bg.getProgress();
                if(backgroundImageSelect&&(bgBitmap!=null)) {
                    updateHueSatForBackgroundImage(satValueBG,hueValueBG,true,false);
                }else
                    updateHueSat(satValueBG,hueValueBG,true,true);
            }
        });

        sat_bg.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (imgBitmap != null) {
                    satValueBG= (float) sat_bg.getProgress() / 256;
                    if(backgroundImageSelect&&(bgBitmap!=null)) {
                        updateHueSatForBackgroundImage(satValueBG,hueValueBG,true,false);
                    }else
                        updateHueSat(satValueBG,hueValueBG,true,true);
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

                if(shape.equals("SQUARE")) {
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


        //clockwise direction rotate image
        rotate_clock_wise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRot = 90;
                if(backgroundImageSelect){
                    imvBackground.setScaleType(ImageView.ScaleType.MATRIX);
                    ImageView bgImageView = imvBackground;
                    matrixBg.postRotate(newRot, imvBackground.getWidth() / 2, imvBackground.getHeight() / 2);
                    bgImageView.setImageMatrix(matrixBg);
                    imvBackground.invalidate();
                }else {
                    ImageView imageView = imvSplashImage;
                    matrix.postRotate(newRot, imvSplashImage.getWidth()/2, imvSplashImage.getHeight() / 2);
                    imageView.setImageMatrix(matrix);
                    imvSplashImage.invalidate();
                }
            }
        });

        //Anticlockwise direction rotate image
        rotate_anticlock_wise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRot = -90;
                if(backgroundImageSelect){
                    imvBackground.setScaleType(ImageView.ScaleType.MATRIX);
                    ImageView bgImageView = imvBackground;
                    matrixBg.postRotate(newRot, imvBackground.getWidth() / 2, imvBackground.getHeight() / 2);
                    bgImageView.setImageMatrix(matrixBg);
                    imvBackground.invalidate();
                }else {
                    ImageView imageView = imvSplashImage;
                    matrix.postRotate(newRot, imvSplashImage.getWidth() / 2, imvSplashImage.getHeight() / 2);
                    imageView.setImageMatrix(matrix);
                    imvSplashImage.invalidate();
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
                this.mainImage = bitmap;
                this.reSizeImage = this.getResizedBitmap(this.mainImage).copy(Bitmap.Config.ARGB_8888, true);
                this.cutCanvasImage = this.getResizedBitmap(this.mainImage).copy(Bitmap.Config.ARGB_8888, true);
                this.hueBitmap = this.cutCanvasImage;
                mainCanvasBitmap = Bitmap.createBitmap(this.reSizeImage.getWidth(), this.reSizeImage.getHeight(), Bitmap.Config.ARGB_8888);
                this.canvas = new Canvas(mainCanvasBitmap);
                this.canvas.drawBitmap(this.reSizeImage, 0.0f, 0.0f, null);
                this.imvSplashImage.setImageBitmap(mainCanvasBitmap);
                imvSplashImage.setScaleType(ImageView.ScaleType.MATRIX);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                final int heightDis = displayMetrics.heightPixels;
                final int widthDis = displayMetrics.widthPixels;
                ViewTreeObserver vto = frameLayout.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            frameLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        widthFrame  = frameLayout.getMeasuredWidth();
                        heightFrame = frameLayout.getMeasuredHeight();
                    }
                });
                ViewTreeObserver vtoL = rotate_view_layout.getViewTreeObserver();
                vtoL.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rotate_view_layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rotate_view_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        widthLay  = rotate_view_layout.getMeasuredWidth();
                        heightLay = rotate_view_layout.getMeasuredHeight();
                        ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                        // Changes the height and width to the specified *pixels*
                        params.height = heightFrame;
//                        float widthNew= widthFrame;
                        //old commit
//                        params.height = heightFrame-(heightLay+60) ;
                        float widthNew=(((float)widthDis/(float) heightDis))*params.height;

                        //testing
//                        params.height = heightFrame -(heightLay) ;
//                        float widthNew=(((float)reSizeImage.getWidth()/(float) reSizeImage.getHeight()))*params.height;
                        params.width= (int) widthNew;
                        frameLayout.setLayoutParams(params);
                        paramsImage = imvSplashImage.getLayoutParams();
                        paramsImage.height=params.height;
                        paramsImage.width=params.width;
                        imvSplashImage.setLayoutParams(paramsImage);

                        imvSplashImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                imvSplashImage.getViewTreeObserver().removeOnPreDrawListener(this);
                                Drawable d = imvSplashImage.getDrawable();
                                RectF imageRectF = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                RectF viewRectF = new RectF(0, 0, paramsImage.width,paramsImage.height );
                                matrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
                                imvSplashImage.setImageMatrix(matrix);
                                imvSplashImage.invalidate();
                                return true;
                            }
                        });
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





        //bottomNavigationView.getMenu().getItem(1).setChecked(true);



        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_checked}  // pressed
        };

        int[] colors = new int[] {
                this.getResources().getColor(R.color.colorGray),
                Color.WHITE
        };
        final ColorStateList myList = new ColorStateList(states, colors);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomNavigationView.setItemIconTintList(myList);
                switch (item.getItemId()) {
                    case R.id.zoom:
                        bottomNavigationView.setItemIconTintList(myList);
                        bottomButtonNumber = 5;
                        hue_sat_lay.setVisibility(View.GONE);
                        hue_sat_lay_bg.setVisibility(View.GONE);
                        eraser_size_layout.setVisibility(View.GONE);
                        rotate_view_layout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.eraser:
                        bottomButtonNumber = 3;
                        hue_sat_lay.setVisibility(View.GONE);
                        hue_sat_lay_bg.setVisibility(View.GONE);
                        eraser_size_layout.setVisibility(View.VISIBLE);
                        rotate_view_layout.setVisibility(View.GONE);
                        break;
                    case R.id.flip:
                        bottomNavigationView.setItemIconTintList(null);
                        hue_sat_lay.setVisibility(View.GONE);
                        hue_sat_lay_bg.setVisibility(View.GONE);
                        eraser_size_layout.setVisibility(View.GONE);
                        rotate_view_layout.setVisibility(View.GONE);
                               /*if(bgBitmap!=null)
                               {
                                   AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
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

                               }*/
                        if(bgBitmap!=null) {
                            bottomButtonNumber = NONE;
                            if(backgroundImageSelect){
                                backgroundImageSelect = false;
                                item.setIcon(R.drawable.select_fg);

                            }
                            else{
                                item.setIcon(R.drawable.select_bg);
                                backgroundImageSelect=true;
                            }
                        }
                        else{
                            // reset to selcted foreground image on bg removed.
                            backgroundImageSelect = false;
                            item.setIcon(R.drawable.select_fg);
                            bottomButtonNumber=NONE;
                        }
                        break;
                    case R.id.hue_sat:
                        bottomButtonNumber=NONE;
                        rotate_view_layout.setVisibility(View.GONE);
                        if(backgroundImageSelect)
                            hue_sat_lay_bg.setVisibility(View.VISIBLE);
                        else
                            hue_sat_lay.setVisibility(View.VISIBLE);
                        eraser_size_layout.setVisibility(View.GONE);
                        break;
                    case R.id.add_bg:
                        bottomButtonNumber=NONE;
                        hue_sat_lay_bg.setVisibility(View.GONE);
                        hue_sat_lay.setVisibility(View.GONE);
                        eraser_size_layout.setVisibility(View.GONE);
                        rotate_view_layout.setVisibility(View.GONE);
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
        //in app billing
        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        Menu nav_Menu = navigationView.getMenu();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        if(pref.getBoolean("purchased", false)){
//            nav_Menu.findItem(R.id.i_a_b).setVisible(false);
            hideOrShowInAppBilling(true);
        } else {
//            nav_Menu.findItem(R.id.i_a_b).setVisible(true);
            hideOrShowInAppBilling(false);
        }
        //in app billing end

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
                    case R.id.i_a_b:
                        mHelper.launchPurchaseFlow(EditActivity.this, ITEM_SKU, 10001,
                                mPurchaseFinishedListener, "mypurchasetoken");
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

    @Override
    protected void onStart() {
        super.onStart();

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtpijclMWaF4GoevnaL4OZzwyF5oWca6ZeyI/m9MSb990rKspIjCVYuwnwXjFRGUjwfck1rkVC/7Wd51s/ACEC2btG1aGkYkAfGEF6kjYaHaBjpb31o45rc0InhwcvcWjnM8iJVAqQCqrMw/h93e154EIbwztrcFRhYUw1LSVkkXYtXO5wUpSv4GX6XPkD5b/vny0WAkcheGridNZF7PR8RigYsiFyD4vUH0h0+8Kjp5PP3dsBQziSaq51+cuShuToASPITy9nihi65DOy3YU9lCkeDIy3MLYhcoP0O9OJpz7udosIDJrI2757wp9QiByvLRVyGxv+8iQbG3rWdqJWwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.e(TAG, "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.e(TAG, "In-app Billing is set up OK");
                                               // if setup is ok check if already purchased
                                               mHelper.queryInventoryAsync(mGotInventoryListener);
                                           }
                                       }
                                   });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;
            try {
                Purchase purchase = inventory.getPurchase(ITEM_SKU);
                if (purchase != null) {
                    //purchased hide inAppBilling Button.
//                    Log.d("Logging--", "onQueryInventoryFinished: purchased bool-"+pref.getBoolean("purchased", false));
                    hideOrShowInAppBilling(true);
                } else {
//                    Log.d("Logging--", "onQueryInventoryFinished: not purchased bool-"+pref.getBoolean("purchased", false));
                }
            } catch (NullPointerException e) {
//                Log.d(TAG, "onQueryInventoryFinished: error::"+e);
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d("Logging", "onIabPurchaseFinished: "+result);
                if (result.getResponse() == 7) {
//                    IabResult: Unable to buy item (response: 7:Item Already Owned) -->> set to true so that watermark
                    hideOrShowInAppBilling(true);

//                    editor.putBoolean("purchased", true);
//                    editor.commit();
//                    Menu nav_Menu = navigationView.getMenu();
//                    pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
//                    editor = pref.edit();
//                    nav_Menu.findItem(R.id.i_a_b).setVisible(false);
                }
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                //buyButton.setEnabled(false);
            }

        }
    };


    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
//            Log.d("Logging--", "onQueryInventoryFinished:  wuery finish:"+result.isFailure());
            if (result.isFailure()) {
                // Handle failure
            } else {
                hideOrShowInAppBilling(true);
// uncomment this to consume activity.
//                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
//                        mConsumeFinishedListener);
            }
        }
    };
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        hideOrShowInAppBilling(true);
                    } else {
                        Toast.makeText(getApplicationContext(),"Sry ! Error in purchase",Toast.LENGTH_LONG).show();
                        // handle error
                    }
                }
            };



    private void selectImage() {

        final CharSequence[] options = { "Capture through Image","Capture Over Image", "Choose from Gallery","Remove Background" };

        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle("Add Photo!");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean exist=false;
                String message="Background image will be remove.Do you want continue?";
                if(bgBitmap!=null) {
                    exist=true;
                }
                if (options[item].equals("Remove Background")) {

                    backgroundImageSecondCheck("Remove Background",message,exist);
                }
                else if(checkAndRequestPermissions()) {

                    if (options[item].equals("Capture through Image")) {

                        backgroundImageSecondCheck("Capture through Image",message,exist);

                    }

                    else if (options[item].equals("Capture Over Image")) {

                        backgroundImageSecondCheck("Capture Over Image",message,exist);

                    }
                    else if (options[item].equals("Choose from Gallery")) {

                        backgroundImageSecondCheck("Choose from Gallery",message,exist);

                    }

                }

            }
        });
        builder.show();



    }


    void backgroundImageSecondCheck(final String type, String message, boolean exist) {

        if(exist){
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(EditActivity.this);
            alertDialogBuilder.setTitle("Warning");
            alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    backgroundImageFunction(type);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.create().show();
        }
        else{

            backgroundImageFunction(type);
        }

    }


    void backgroundImageFunction(String type) {

        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
        Intent intent =null;
        switch(type){
            case "Capture through Image":
                imvBackground.setImageBitmap(null);
                frameLayout.setBackground(null);
                frameLayout.destroyDrawingCache();
                frameLayout.setDrawingCacheEnabled(true);
                frameLayout.buildDrawingCache();

                Bitmap bm = Bitmap.createBitmap(frameLayout.getDrawingCache());
                frameLayout.setBackgroundResource(R.drawable.image_bg);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                imagesFolder.mkdirs();
                imagesFolder.setWritable(true);
                mypathBg = new File(imagesFolder, "temporary_file.JPEG");
                try {
                    mypathBg.createNewFile();
                    new FileOutputStream(mypathBg).write(bytes.toByteArray());
                    Intent intentCameraSurfaceActivity = new Intent(getApplicationContext(), CameraActivity.class);
                    intentCameraSurfaceActivity.putExtra("Image", FileProvider.getUriForFile(EditActivity.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg).toString());
                    intentCameraSurfaceActivity.putExtra("ImagePath",mypathBg.toString());
                    startActivity(intentCameraSurfaceActivity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "Capture Over Image":
//                /*Intent intentCameraSurfaceActivity = new Intent(getApplicationContext(), CameraActivity.class);
//                startActivity(intentCameraSurfaceActivity);*/
////                 imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
////                imagesFolder.mkdirs();
////                imagesFolder.setWritable(true);
////                mypathCameraImage = new File(imagesFolder,"temporary.png");
////                if(Build.VERSION.SDK_INT>23)
////                    imgUri = FileProvider.getUriForFile(EditActivity.this,
////                            BuildConfig.APPLICATION_ID + ".provider",
////                            mypathCameraImage);
////                else
////                    imgUri=Uri.fromFile(mypathCameraImage);
////                Log.e("Log",imgUri.toString());
////                 intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
////                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
////                startActivityForResult(intent, 1);
//
//                //invokes sytemCamera....
//                imagesFolder.mkdirs();
//                imagesFolder.setWritable(true);
//                mypathBg = new File(imagesFolder, "temporary_file.png");
//                Uri imgUri=null;
//                if(Build.VERSION.SDK_INT>23)
//                    imgUri = FileProvider.getUriForFile(EditActivity.this,
//                            BuildConfig.APPLICATION_ID + ".provider",
//                            mypathBg);
//                else
//                    imgUri=Uri.fromFile(mypathBg);
//                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
//                startActivityForResult(intent, 1);



                // copied same as through image
                imvBackground.setImageBitmap(null);
                frameLayout.setBackground(null);
                frameLayout.destroyDrawingCache();
                frameLayout.setDrawingCacheEnabled(true);
                frameLayout.buildDrawingCache();

                Bitmap bitMap = Bitmap.createBitmap(frameLayout.getDrawingCache());
                frameLayout.setBackgroundResource(R.drawable.image_bg);
                ByteArrayOutputStream byteOP = new ByteArrayOutputStream();
                bitMap.compress(Bitmap.CompressFormat.PNG, 100, byteOP);
                imagesFolder.mkdirs();
                imagesFolder.setWritable(true);
                mypathBg = new File(imagesFolder, "temporary_file.JPEG");
                try {
                    mypathBg.createNewFile();
                    new FileOutputStream(mypathBg).write(byteOP.toByteArray());
                    Intent intentCameraSurfaceActivity = new Intent(getApplicationContext(), CameraActivity.class);
                    intentCameraSurfaceActivity.putExtra("Image", FileProvider.getUriForFile(EditActivity.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg).toString());
                    intentCameraSurfaceActivity.putExtra("ImagePath",mypathBg.toString());
                    intentCameraSurfaceActivity.putExtra("hideImageView",true);
                    startActivity(intentCameraSurfaceActivity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "Choose from Gallery":
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
                break;
            case "Remove Background":
                bgBitmap = null;
                imvBackground.setImageBitmap(null);
                //reset the flipped image
                bottomNavigationView.setSelectedItemId(R.id.flip);
                break;

        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        switch(requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    try {

                        imvSplashImage.setImageBitmap(mainCanvasBitmap);
                        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                        imagesFolder.mkdirs();
                        imagesFolder.setWritable(true);
                        mypathBg = new File(imagesFolder, "temporary_file.png");
                        Log.e("Result OK",mypathBg.getPath().toString());
                        this.imvBackground.setImageBitmap(null);
                        this.bgBitmap = BitmapFactory.decodeFile(mypathBg.getPath());
//                        Log.d("Logging--", "onActivityResult: --->> case 1 if try");
                        this.imvBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        this.imvBackground.setImageBitmap(this.bgBitmap);
                        mypathBg.delete();

//
//
//                        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
//                        imagesFolder.mkdirs();
//                        imagesFolder.setWritable(true);
//                        mypathCameraImage = new File(imagesFolder,"temporary.png");
//                        mypathBg=new File(imagesFolder, "temporary_file.png");
//                        imgUri = FileProvider.getUriForFile(EditActivity.this,
//                                BuildConfig.APPLICATION_ID + ".provider",
//                                mypathCameraImage);
//                        intentEditActivity = new Intent(this, RotationActivity.class);
//                        intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
//                        intentEditActivity.putExtra("FilePath",mypathCameraImage.toString());
//                        intentEditActivity.putExtra("EditOrNot","Edit");
//                        startActivity(intentEditActivity);
                    }
                    catch (NullPointerException e) {
                        Log.e("Exception",e.toString());
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {

                    try {
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            InputStream imageStream = null;
                            imageStream = getContentResolver().openInputStream(selectedImage);
                            this.bgBitmap= BitmapFactory.decodeStream(imageStream);
                            this.imvBackground.setImageBitmap(null);
//                            Log.d("Logging--", "onActivityResult: --->> case 2 if try");
                            this.imvBackground.setScaleType(ImageView.ScaleType.MATRIX);
                            this.imvBackground.setImageBitmap(this.bgBitmap);
                          /*  File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                            imagesFolder.mkdirs();
                            imagesFolder.setWritable(true);
                            mypathBg=new File(imagesFolder, "temporary_file.png");
                            String realPath = null;
                            Intent intentEditActivity = new Intent(this, RotationActivity.class);
                            Uri imgUri = data.getData();

                            if (Build.VERSION.SDK_INT < 19)
                                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
                            else
                                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                            imgUri = FileProvider.getUriForFile(EditActivity.this,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    new File(realPath));
                            intentEditActivity.putExtra("ImageFromGallery", imgUri.toString());
                            intentEditActivity.putExtra("FilePath", "");
                            intentEditActivity.putExtra("EditOrNot","Edit");
                            startActivity(intentEditActivity);*/
                        }
//                        Uri selectedImage = data.getData();
//                        InputStream imageStream = null;
//                        imageStream = getContentResolver().openInputStream(selectedImage);
//                        this.bgBitmap = BitmapFactory.decodeStream(imageStream);
//                        this.imvBackground.setImageBitmap(null);
//                        this.imvBackground.setScaleType(ImageView.ScaleType.FIT_XY);
//                        this.imvBackground.setImageBitmap(this.bgBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                    File mypathBg = new File(imagesFolder, "temporary"+ ".png");
                    mypathBg.delete();
                }
                break;

            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

                    if (resultCode == RESULT_OK) {
                        //imvSplashImage.setImageURI(result.getUri());
                        //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
                        Bitmap temp=BitmapFactory.decodeFile(UCrop.getOutput(data).getPath());
                        Bitmap bitmapForSave=temp.copy(Bitmap.Config.ARGB_8888,true);
                        if(!pref.getBoolean("purchased", false)) {
                            Canvas canvas = new Canvas(bitmapForSave);
                            //                    if(bgBitmap!=null)
                            //                    {
                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);
                            paint.setAlpha(128);
                            paint.setTextSize(30);
                            paint.setAntiAlias(true);
                            paint.setUnderlineText(false);
                            canvas.drawText("Created by SwapOut",bitmapForSave.getWidth()-300,bitmapForSave.getHeight()-30, paint);
                        }
                        File mypathBg=null;
                        if(typeOfStorage.equals("save")){
                            File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                            imagesFolder.mkdirs();
                            imagesFolder.setWritable(true);
                            mypathBg = new File(imagesFolder, new SimpleDateFormat("yyyyMMMddHmmss").format(Calendar.getInstance().getTime()) + ".png");
                        }
                        else if(typeOfStorage.equals("share")){
                            File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(folderName), "");
                            imagesFolder.mkdirs();
                            imagesFolder.setWritable(true);
                            mypathBg = new File(imagesFolder, "temporary"+ ".png");
                        }
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(mypathBg);
                            bitmapForSave.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            if(typeOfStorage.equals("share")){
                                Intent share = new Intent();
                                share.setAction(Intent.ACTION_SEND);
                                share.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(EditActivity.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg));
                                share.setType("image/jpeg");
                                share.putExtra(Intent.EXTRA_TEXT, "SwapOut");
                                startActivityForResult((Intent.createChooser(share, "SwapOut")),3);
                            }
                            else if(typeOfStorage.equals("save")){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    Uri contentUri = FileProvider.getUriForFile(EditActivity.this,BuildConfig.APPLICATION_ID + ".provider", mypathBg);
                                    final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(mypathBg));
                                    sendBroadcast(scanIntent);
                                } else {
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,Uri.fromFile(mypathBg)));
                                }
                                Toast.makeText(getApplicationContext(),"Image saved",Toast.LENGTH_LONG).show();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        Process.killProcess(Process.myPid());
//                        deleteCache(mContext);

                    } else if (resultCode == UCrop.RESULT_ERROR) {
                        Toast.makeText(this, "Sorry! Error an image saving" , Toast.LENGTH_LONG).show();
                    }
                }

        }
    }



    public void updateHueSatForBackgroundImage(float sat,float hue,boolean pushToStack,boolean isForeGround) {

        Bitmap hsbitmap = this.bgBitmap;

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
        if (pushToStack) {
//            Log.d("Logging--", "updateHueSatForBackgroundImage: pushing bg sat");
            //undo redo workout --> Start
            this.paths.add(this.mPath);
            this.count += REQUEST_SELECT_PICTURE;
            this.hashMap.put(Integer.valueOf(this.count), Integer.valueOf(this.bottomButtonNumber));
            this.imageArrayUndo.add(bitmapResult);
            // push to array maintained seperately for background.
            bgImageArrayUndo.add(((BitmapDrawable)imvBackground.getDrawable()).getBitmap());
            this.undoneSize.add(brushSize + "," + shape + "," + sat + "," + hue+","+isForeGround);
            // undo redo workout ---> end
        } else {
//            Log.d("Logging--", "updateHueSatForBackgroundImage:  else not pushing bg sat");
        }
        imvBackground.setImageBitmap(bitmapResult);
    }


    public void updateHueSat(float sat,float hue,boolean pushToStack,boolean isForeGround) {

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
        if (pushToStack) {
            //hue sat undo redo workout --> Start
            this.paths.add(this.mPath);
            this.count += REQUEST_SELECT_PICTURE;
            this.hashMap.put(Integer.valueOf(this.count), Integer.valueOf(this.bottomButtonNumber));
            this.imageArrayUndo.add(bitmapResult);
            this.undoneSize.add(brushSize + "," + shape + "," + sat + "," + hue+","+isForeGround);
            //hue sat undo redo workout ---> end
        } else {

        }
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
        alertShow(EditActivity.this);
    }

    @TargetApi(18)
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction())
        {

            // Action - single finger touch
            // Pointer - 2 finger touch
            case MotionEvent.ACTION_DOWN:
                if (backgroundImageSelect){
                    matrixBg.getValues(foregroundMatrixValues);
                } else {
                    matrix.getValues(foregroundMatrixValues);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (bottomButtonNumber == 5 && !backgroundImageSelect){
                    // if zoom selected 5 and foreground image selected.
                    //size_shape[0]-eraser size 1-eraser sh
                    // ape ,2-saturation, 3-hue value, 4-isForeground, 5-rotation angle.
                    paths.add(mPath);
                    count += REQUEST_SELECT_PICTURE;
                    hashMap.put(Integer.valueOf(count), Integer.valueOf(bottomButtonNumber));
                    BitmapDrawable drawable = (BitmapDrawable) imvSplashImage.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    imageArrayUndo.add(bitmap);
                    String floatVals = "";
                    for(int i=0;i<foregroundMatrixValues.length-1;i++){
                        floatVals = floatVals + Float.toString(foregroundMatrixValues[i])+"@";
                    }
                    floatVals = floatVals+Float.toString(foregroundMatrixValues[foregroundMatrixValues.length-1]);
                    undoneSize.add(brushSize + "," + shape + "," + satValue + "," + hueValue+","+true+","+foreGroundRotation+","+floatVals);

                } else if (bottomButtonNumber == 5 && backgroundImageSelect){
                    // if zoom selected 5 and background image selected.


                    paths.add(mPath);
                    count += REQUEST_SELECT_PICTURE;
                    hashMap.put(Integer.valueOf(count), Integer.valueOf(bottomButtonNumber));
                    BitmapDrawable drawable = (BitmapDrawable) imvBackground.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    imageArrayUndo.add(bitmap);
                    bgImageArrayUndo.add(((BitmapDrawable)imvBackground.getDrawable()).getBitmap());
                    String floatVals = "";
                    for(int i=0;i<foregroundMatrixValues.length-1;i++){
                        floatVals = floatVals + Float.toString(foregroundMatrixValues[i])+"@";
                    }
                    floatVals = floatVals+Float.toString(foregroundMatrixValues[foregroundMatrixValues.length-1]);
                    undoneSize.add(brushSize + "," + shape + "," + satValue + "," + hueValue+","+false+","+foreGroundRotation+","+floatVals);

                }
                break;
        }
        switch (v.getId()) {
            case R.id.imvSplashImage /*2131624068*/:
                if (this.bottomButtonNumber == 5 || this.bottomButtonNumber == 4) {
                    ImageView view = (ImageView) v;
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    switch (event.getAction() & 255) {
                        case NONE /*0*/:
                            if(backgroundImageSelect){
                                this.savedMatrix.set(this.matrixBg);
                            }else
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
                                if(backgroundImageSelect){
                                    this.matrixBg.set(this.savedMatrix);
                                }else
                                    this.matrix.set(this.savedMatrix);
                                float dx = event.getX() - this.start.x;
                                float dy = event.getY() - this.start.y;
                                if(backgroundImageSelect){
                                    this.matrixBg.postTranslate(dx, dy);
                                }else
                                    this.matrix.postTranslate(dx, dy);
                            }


                            if (this.mode == ZOOM) {
                                float newDist=spacing(event);

                                if (newDist > 10.0f) {
                                    if(backgroundImageSelect){
                                        this.matrixBg.set(this.savedMatrix);
                                    }else
                                        this.matrix.set(this.savedMatrix);
                                    float scale = newDist / this.oldDist;
                                    if(backgroundImageSelect){
                                        matrixBg.postScale(scale, scale, this.mid.x, this.mid.y);
                                    }else
                                        matrix.postScale(scale, scale, this.mid.x, this.mid.y);
                                    float[] values = new float[9];
                                    if(backgroundImageSelect){
                                        this.matrixBg = this.tempMatrix;
                                        this.matrixBg.getValues(values);
                                    }else {
                                        this.matrix = this.tempMatrix;
                                        this.matrix.getValues(values);
                                    }
                                    float width = values[NONE] * ((float) this.reSizeImage.getWidth()/2);
                                    float height = values[4] * ((float) this.reSizeImage.getHeight()/2);

                                    if (newDist >= this.oldDist) {
                                        if(backgroundImageSelect){
                                            this.matrixBg.postScale(scale, scale, this.mid.x, this.mid.y);
                                        }else {
                                            this.matrix.postScale(scale, scale, this.mid.x, this.mid.y);
                                        }
                                    }

                                }


                                if (lastEvent != null && event.getPointerCount() == 2 || event.getPointerCount() == 3) {
                                    newRot = rotation(event);
                                    float r = newRot - d;
                                    float[] values = new float[9];
                                    if(backgroundImageSelect){
                                        matrixBg.getValues(values);
                                    }else {
                                        matrix.getValues(values);
                                    }
                                    float tx = values[2];
                                    float ty = values[5];
                                    float sx = values[0];
                                    float xc = (view.getWidth() / 2) * sx;
                                    float yc = (view.getHeight() / 2) * sx;
                                    if(backgroundImageSelect){
                                        matrixBg.postRotate(r, tx + xc, ty + yc);
                                    }else
                                        this.matrix.postRotate(r, tx + xc, ty + yc);
                                }
                            }

                            break;
                        case 5 /*5*/:

                            this.oldDist = spacing(event);
                            if (this.oldDist > 10.0f) {
                                if(backgroundImageSelect){
                                    this.savedMatrix.set(this.matrixBg);
                                }else
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

                    if(backgroundImageSelect){
                        this.imvBackground.setScaleType(ImageView.ScaleType.MATRIX);
                        this.imvBackground.setImageMatrix(this.matrixBg);
                        this.imvBackground.invalidate();
                    }
                    else{
                        view.setImageMatrix(this.matrix);
                        this.imvSplashImage.invalidate();
                    }
                }
                if(!backgroundImageSelect)
                    if (this.bottomButtonNumber == ZOOM || this.bottomButtonNumber == 3) {
                        this.tempPaint = new Paint();
                        this.tempPaint.setDither(false);
                        this.tempPaint.setFilterBitmap(true);
                        this.tempPaint.setAntiAlias(true);
                        if (this.bottomButtonNumber == 3) {
                            this.tempPaint.setStrokeWidth((float) brushSize);
                            if(shape.equals("ROUND")) {
                                this.tempPaint.setStyle(Paint.Style.STROKE);
                                this.tempPaint.setStrokeCap(Paint.Cap.ROUND);
                                this.tempPaint.setStrokeJoin(Paint.Join.ROUND);
                            }
                            else {
                                //this.tempPaint.setStyle(Paint.Style.FILL);
                                this.tempPaint.setStyle(Paint.Style.STROKE);
                                this.tempPaint.setStrokeCap(Paint.Cap.SQUARE);
                                this.tempPaint.setStrokeJoin(Paint.Join.BEVEL);
                            }
                            this.tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        } else if (this.bottomButtonNumber == ZOOM) {
                            this.tempPaint.setColor(Color.BLUE);
                            this.tempPaint.setStrokeWidth(20.0f);
                            this.tempPaint.setStyle(Paint.Style.FILL);
                            this.tempPaint.setStrokeJoin(Paint.Join.ROUND);
                            this.tempPaint.setStrokeCap(Paint.Cap.ROUND);
                            this.paint = new Paint();
                            this.paint.setDither(false);
                            this.paint.setFilterBitmap(true);
                            this.paint.setAntiAlias(true);
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
                        try {
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
                                /*if (shape.equals("ROUND")) {
                                    this.mPath.lineTo(touchX, touchY);
                                } else {
                                    float x = 10;
                                    this.mPath.moveTo(touchX, touchY);
                                    this.mPath.lineTo(touchX + 0.19f * brushSize * x, touchY + 0.3f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.001f * brushSize * x, touchY + 0.21f * brushSize * x);
                                    this.mPath.lineTo(touchX - 0.19f * brushSize * x, touchY + 0.3f * brushSize * x);
                                    this.mPath.lineTo(touchX - 0.16f * brushSize * x, touchY + 0.09f * brushSize * x);
                                    this.mPath.lineTo(touchX - 0.3f * brushSize * x, touchY - 0.07f * brushSize * x);
                                    this.mPath.lineTo(touchX - 0.1f * brushSize * x, touchY - 0.11f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.001f * brushSize * x, touchY - 0.3f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.1f * brushSize * x, touchY - 0.11f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.3f * brushSize * x, touchY - 0.07f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.16f * brushSize * x, touchY + 0.09f * brushSize * x);
                                    this.mPath.lineTo(touchX + 0.19f * brushSize * x, touchY + 0.3f * brushSize * x);
                                }*/
                                    this.mPath.lineTo(touchX, touchY);
                                    try {
                                        if (this.bottomButtonNumber == 3) {
                                            this.canvas.drawPath(this.mPath, this.tempPaint);
                                            this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        } else if (this.bottomButtonNumber == ZOOM) {
                                            this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                                            this.canvas.drawPath(this.mPath, this.paint);
                                            this.canvas.drawBitmap(this.cutCanvasImage, 0.0f, 0.0f, this.paint);
                                            this.cutCanvasImage = mainCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        }

                                        this.imageArrayUndo.add(this.cutCanvasImage);
                                        this.undoneSize.add(brushSize + "," + shape + "," + satValue + "," + hueValue+","+true);
                                    } catch (Exception e) {
                                        Log.e("Error", "errror");
                                        this.imageArrayUndo.remove(0);
                                        this.undoneSize.remove(0);

                                        this.imageArrayUndo.add(this.cutCanvasImage);
                                        this.undoneSize.add(brushSize + "," + shape + "," + satValue + "," + hueValue+","+true);

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
                        }
                        catch (OutOfMemoryError e)
                        {
                            this.cutCanvasImage=mainCanvasBitmap;
                            Log.e("Error","error");
                        }
                        catch (Exception e)
                        {
                            Log.e("Error","error");
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
//        Log.d("Logging--", "onClickUndo: ");
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

            String[] size_shape = this.undoneSize.get(mSize-1).split(",");
            String floatVals = "";
            if (pushRedo) {
                // for redone addition
                matrix.getValues(foregroundMatrixValues);
                for(int i=0;i<foregroundMatrixValues.length-1;i++){
                    floatVals = floatVals + Float.toString(foregroundMatrixValues[i])+"@";
                }
                floatVals = floatVals+Float.toString(foregroundMatrixValues[foregroundMatrixValues.length-1]);
//            redoneSize.add(brushSize + "," + shape + "," + satValue + "," + hueValue+","+true+","+foreGroundRotation+","+floatVals);
            }
            //undo rotation at index 5 if exists. and foreground
            if (size_shape.length >= 6 && Boolean.valueOf(size_shape[4]))
            {
//                Log.d(TAG, "onClickUndo: >=6 and foreground");
                ImageView imageView = imvSplashImage;
                String arr = size_shape[6];
                String[] valArr = arr.split("@");
                float[] values = new float[valArr.length];
                for(int i=0;i<valArr.length;i++){
                    values[i] = Float.parseFloat(valArr[i]);
                }
                //if foreground
                if (Boolean.valueOf(size_shape[4])) {
                    matrix.setValues(values);
                    imageView.setImageMatrix(matrix);
                    imvSplashImage.invalidate();
                }
            }

            //if foreground
            if (Boolean.valueOf(size_shape[4])) {
//                Log.d("Logging--", "onClickUndo: foreground");
                this.canvas.drawBitmap(imagePrevious, 0.0f, 0.0f, null);
                this.cutCanvasImage = imagePrevious;
                this.imageArrayRedo.add(this.imageArrayUndo.get(mSize - 1));
                this.undonePaths.add(this.paths.remove(mSize - 1));
                this.imageArrayUndo.remove(mSize - 1);
                // update hue sat seekbar
                try {
                    if (mSize == 1) {
                        hue.setProgress(0);
                        sat.setProgress(256);
                    } else {
                        hue.setProgress(NumberFormat.getInstance().parse(size_shape[3]).intValue());
                       float hueValue = (NumberFormat.getInstance().parse(size_shape[2]).floatValue()*256);
                        sat.setProgress(Math.round(hueValue));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                avoid unnecessary forground push to array
                if (pushRedo) {
                    this.undoneSize.remove(mSize - 1);
                    redoneSize.add(size_shape[0] + "," + size_shape[1] + "," + size_shape[2] + "," + size_shape[3]+","+size_shape[4]+","+foreGroundRotation+","+floatVals);
                    pushRedo = false;
                } else {
                    this.redoneSize.add(this.undoneSize.remove(mSize - 1));
                }
                this.imvSplashImage.invalidate();
            } else {
//                Log.d("Logging--", "onClickUndo: background");
//                this.canvas.drawBitmap(imagePrevious, 0.0f, 0.0f, null);
//                this.cutCanvasImage = imagePrevious;
                this.imageArrayRedo.add(this.imageArrayUndo.get(mSize - 1));
                this.undonePaths.add(this.paths.remove(mSize - 1));
                this.imageArrayUndo.remove(mSize - 1);
                this.redoneSize.add(this.undoneSize.remove(mSize - 1));
                // update hue sat seekbar
                try {
                    hue_bg.setProgress(NumberFormat.getInstance().parse(size_shape[3]).intValue());
                    float hueValue = (NumberFormat.getInstance().parse(size_shape[2]).floatValue()*256);
                    sat_bg.setProgress(Math.round(hueValue));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int arrayIndex = bgImageArrayUndo.size() - 1;
                this.imvBackground.setImageBitmap(bgImageArrayUndo.get(arrayIndex));
                this.bgImageArrayRedo.add(this.bgImageArrayUndo.get(arrayIndex));
                this.bgImageArrayUndo.remove(arrayIndex);
                this.imvBackground.invalidate();
            }


            return;
        }
        Dialog(this.mContext, " Undo Completed", " Ok");
    }

    public void onClickRedo() {
//        Log.d("Logging--", "onClickRedo: ");
        int rSize = this.undonePaths.size();
        if (rSize > 0) {
            Bitmap imageNext = (Bitmap) this.imageArrayRedo.get(rSize - 1);
            Path redoPath = (Path) this.undonePaths.get(rSize - 1);
            String[] size_shape = this.redoneSize.get(rSize - 1).split(",");
            //size_shape[0]-eraser size 1-eraser shape ,2-saturation, 3-hue value, 4-isForeground, 5-rotation angle.
            Float bSize = Float.valueOf(size_shape[0]);
            try {
                int buttonNumberFromHashMap = ((Integer) this.hashMap.get(Integer.valueOf(this.count + REQUEST_SELECT_PICTURE))).intValue();
                if (buttonNumberFromHashMap == 3) {
                    Paint redoPaint = new Paint();
                    redoPaint.setDither(false);
                    redoPaint.setFilterBitmap(true);
                    redoPaint.setAntiAlias(true);
                    redoPaint.setStrokeWidth(bSize);
                    if(size_shape[1].equals("ROUND")){
                        redoPaint.setStyle(Paint.Style.STROKE);
                        redoPaint.setStrokeJoin(Paint.Join.ROUND);
                        redoPaint.setStrokeCap(Paint.Cap.ROUND);
                    }
                    else{
                        // redoPaint.setStyle(Paint.Style.FILL);
                        redoPaint.setStyle(Paint.Style.STROKE);
                        redoPaint.setStrokeJoin(Paint.Join.BEVEL);
                        redoPaint.setStrokeCap(Paint.Cap.SQUARE);
                    }

                    redoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    //if foreground draw path
                    if (Boolean.valueOf(size_shape[4])) {
                        this.canvas.drawPath(redoPath, redoPaint);
                    }
                }
            }  catch (Exception e) {}

            this.cutCanvasImage = imageNext;
            this.imageArrayUndo.add(imageNext);
            this.imageArrayRedo.remove(rSize - 1);
            this.paths.add(this.undonePaths.remove(this.undonePaths.size() - 1));
            //undo rotation at index 5 if exists.
            if (size_shape.length >= 6)
            {

                ImageView imageView = imvSplashImage;
                String arr = size_shape[6];
                String[] valArr = arr.split("@");
                float[] values = new float[valArr.length];
                for(int i=0;i<valArr.length;i++){
                    values[i] = Float.parseFloat(valArr[i]);
                }
                //if foreground
                if (Boolean.valueOf(size_shape[4])) {
                    matrix.setValues(values);
                    imageView.setImageMatrix(matrix);
                    imvSplashImage.invalidate();
                }


            }
            this.undoneSize.add(this.redoneSize.remove(rSize - 1));
            this.count += REQUEST_SELECT_PICTURE;
            if (Boolean.valueOf(size_shape[4])) {
                //update hue sat for forground call the existing updateHueSat pass extra 2 params pushtostack and is foreground.
                updateHueSat(Float.valueOf(size_shape[2]),Float.valueOf(size_shape[3]),false,true);
                this.imvSplashImage.invalidate();
                // update hue sat seekbar
                try {
                    hue.setProgress(NumberFormat.getInstance().parse(size_shape[3]).intValue());
                    float hueValue = (NumberFormat.getInstance().parse(size_shape[2]).floatValue()*256);
                    sat.setProgress(Math.round(hueValue));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {

                int arrayIndex = bgImageArrayRedo.size() - 1;
//                this.imvBackground.setImageBitmap(bgImageArrayRedo.get(arrayIndex));
                updateHueSatForBackgroundImage(Float.valueOf(size_shape[2]),Float.valueOf(size_shape[3]),false,false);
                this.bgImageArrayUndo.add(this.bgImageArrayRedo.get(arrayIndex));
                this.bgImageArrayRedo.remove(arrayIndex);
                this.imvBackground.invalidate();
//                Log.d(TAG, "onClickRedo: hue value-- "+size_shape[3]);
                // update hue sat seekbar
                try {
                    hue_bg.setProgress(NumberFormat.getInstance().parse(size_shape[3]).intValue());
                    float hueValue = (NumberFormat.getInstance().parse(size_shape[2]).floatValue()*256);
                    sat_bg.setProgress(Math.round(hueValue));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                imvBackground.setImageBitmap(imageNext);
//                //update hue sat for forground call the existing updateHueSatForBackgroundImage pass extra 2 params pushtostack and is foreground.
//                updateHueSatForBackgroundImage(Float.valueOf(size_shape[2]),Float.valueOf(size_shape[3]),false,true);
//                imvBackground.invalidate();
            }
            return;
        }
        Dialog(getApplicationContext(), " Redo  Completed", " Ok");
    }


    public void alertShow(final Context mContext) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder.setMessage("All unsaved Changes will be lost!!! Do you really want to start over again?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Process.killProcess(Process.myPid());
                deleteCache(mContext);
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void Dialog(Context mContext, String message, String negativieButton) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(EditActivity.this);
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
        frameLayout.setBackgroundResource(R.drawable.image_bg);
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
//        Log.d("Logging--", "setBackGround: ---->>");
        imvBackground.setScaleType(ImageView.ScaleType.MATRIX);//ImageView.ScaleType.FIT_XY -> CENTER_CROP -- my edit in 3 place
        imvBackground.setImageBitmap(null);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bgBitmap= BitmapFactory.decodeFile(mypathBg.getPath(),bmOptions);
        //get frame dimension to background image matrix.
        bgBitmap = Bitmap.createScaledBitmap(bgBitmap,frameLayout.getWidth(), frameLayout.getHeight(), true);
        mypathBg.delete();
        imvBackground.setImageBitmap(bgBitmap);
        bgImageArrayUndo.add(bgBitmap);

    }


    class ImageSaved extends AsyncTask<Void, Void, String> {
        Uri contentUri=null;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Image Saving....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            frameLayout.setBackground(null);
            frameLayout.destroyDrawingCache();
            frameLayout.setDrawingCacheEnabled(true);
//            if(bgBitmap!=null)
//            finalBitmap=Bitmap.createBitmap(frameLayout.getDrawingCache());
//            else
            //      finalBitmap=Bitmap.createBitmap(mainCanvasBitmap,0,0,imvSplashImage.getWidth(),imvSplashImage.getHeight(),matrix,true);
            finalBitmap=Bitmap.createBitmap(frameLayout.getDrawingCache());
            frameLayout.setBackgroundResource(R.drawable.image_bg);
        }
        @Override
        protected String doInBackground(Void... arg0)
        {



            File imagesFolder = new File(getApplicationContext().getCacheDir(),"");
            imagesFolder.mkdirs();
            imagesFolder.setWritable(true);
            File mypath=new File(imagesFolder,"temp_save" + ".png");

//                    int height = finalBitmap.getHeight();
//                    int width = finalBitmap.getWidth();
//                    double y = Math.sqrt(1200000
//                            / (((double) width) / height));
//                    double x = (y / height) * width;
//
//
//                    BitmapFactory.Options opts = new BitmapFactory.Options();
//                  Bitmap  watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark,opts);
//
//                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(watermark, width, height, true);

//                    Canvas canvas = new Canvas(finalBitmap);
////                    if(bgBitmap!=null)
////                    {
//                        Paint paint = new Paint();
//                        paint.setColor(Color.WHITE);
//                        paint.setAlpha(128);
//                        paint.setTextSize(30);
//                        paint.setAntiAlias(true);
//                        paint.setUnderlineText(false);
//                        canvas.drawText("Created by SwapOut",finalBitmap.getWidth()-300,finalBitmap.getHeight()-30, paint);
//                    }
//                        else {
//                        //canvas.setMatrix(matrix);
//                        Paint paint = new Paint();
//                        paint.setColor(Color.RED);
//                        paint.setTextSize(50);
//                        paint.setAntiAlias(true);
//                        paint.setUnderlineText(true);
//                        canvas.drawText("Created by SwapOut",40,40, paint);
//                        }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                Thread.currentThread().interrupt();

//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                             contentUri = FileProvider.getUriForFile(EditActivity.this,BuildConfig.APPLICATION_ID + ".provider", mypath);
//                            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
//                            sendBroadcast(scanIntent);
//                        } else {
//                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(mypath.getAbsolutePath())));
//                        }
                typeOfStorage="save";
                startCropImageActivity(Uri.fromFile(mypath));
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

            //Toast.makeText(getApplicationContext(),"Image saved", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(EditActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Please Wait....");
            progressDialog.setTitle("In Progress");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            frameLayout.setBackground(null);
            frameLayout.destroyDrawingCache();
            frameLayout.setDrawingCacheEnabled(true);
            bm = Bitmap.createBitmap(frameLayout.getDrawingCache());
            frameLayout.setBackgroundResource(R.drawable.image_bg);

        }
        @Override
        protected String doInBackground(Void... arg0)
        {

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            File imagesFolder = new File(getApplicationContext().getCacheDir(),"");
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
            typeOfStorage="share";
            startCropImageActivity(Uri.fromFile(mypathTemp));
            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {
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


    private void startCropImageActivity(Uri imageUri) {

        UCrop.of(imageUri,imageUri)
                .start(this);

//            CropImage.activity(imageUri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setMultiTouchEnabled(true)
//                    .start(this);
    }

    private void hideOrShowInAppBilling(boolean mode){
        Menu nav_Menu = navigationView.getMenu();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        if (mode) {
            // true show inAppBilling
            editor.putBoolean("purchased", true);
            editor.commit();
            editor = pref.edit();
            nav_Menu.findItem(R.id.i_a_b).setVisible(false);
        } else {
            // false hide inAppBilling
            editor.putBoolean("purchased", false);
            editor.commit();
            editor = pref.edit();
            nav_Menu.findItem(R.id.i_a_b).setVisible(true);
        }
    }


}
