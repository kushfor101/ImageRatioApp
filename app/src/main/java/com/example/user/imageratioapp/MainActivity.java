package com.example.user.imageratioapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    DrawingView dv ;
    private Paint mPaint;
    private Button loadButton, rotateButton, okButton;
    private static int RESULT_LOAD_IMAGE = 1;
    private Uri selectedImage;
    private int rotate = 0;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dv = new DrawingView(this);
        dv.setId(121);
        setContentView(R.layout.activity_main);
        loadButton = (Button) findViewById(R.id.loadButton);
        rotateButton = (Button)findViewById(R.id.rotateButton);
        okButton = (Button)findViewById(R.id.okButton);
        rotateButton.setVisibility(View.INVISIBLE);
        okButton.setVisibility(View.INVISIBLE);

        rotate = 0;
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate -= 90;
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                Picasso.get().load(selectedImage).rotate(rotate).into(imageView);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateButton.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                layout.addView(dv, 4,params);
                ConstraintSet set = new ConstraintSet();
                set.clone(layout);
                set.connect(dv.getId(),ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
                set.connect(dv.getId(),ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
                set.applyTo(layout);
                dv.init();
                Toast.makeText(getApplicationContext(), "Click on the GREEN BOX to add markers", Toast.LENGTH_SHORT).show();
            }
        });
//        set.connect(dv.getId(),ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);
//        set.connect(dv.getId(),ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    public int getCameraPhotoOrientation(String imageFilePath) {
        int rotate = 0;
        try {

            ExifInterface exif;

            exif = new ExifInterface(imageFilePath);
            String exifOrientation = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.d("exifOrientation", exifOrientation);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Log.d("kushhh", "orientation :" + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            Log.d("kushhh",picturePath);
//            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Picasso.get().load(selectedImage).into(imageView);
        //    imageView.setImageURI(selectedImage);
        //    imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            loadButton.setVisibility(View.GONE);
            rotateButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.VISIBLE);

        }
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path    mPath, linePath1, linePath2;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private ArrayList<Path> circlePaths;
        private Path circlePath;
        private ArrayList<Float> xvals;
        private ArrayList<Float> yvals;
        private int count = 1;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            linePath1 = new Path();
            linePath2 = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            circlePaint = new Paint();
            circlePaths = new ArrayList<>();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
            count = 0;
            xvals = new ArrayList<>();
            yvals = new ArrayList<>();
        }

        public void init() {
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPath.reset();
            mPath.addRect(300,100,400,200,Path.Direction.CW);
            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            for(Path circle : circlePaths) {
                Log.d("kushhh","got circle");
                canvas.drawPath( circle,  circlePaint);
            }
        }

        private float mX = 500, mY= 500;
        private float prevx = -1, prevy = -1;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
//            mPath.reset();
//            mPath.moveTo(200, 200);
            //    mX = x;
            //    mY = y;
        }

        private void touch_move(float x, float y) {
//            mPath.reset();
//            mPath.moveTo(x,y);
//            float dx = Math.abs(x - mX);
//            float dy = Math.abs(y - mY);
            if (prevx!=-1 && prevy!=-1) {
                //    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);

                circlePath.reset();
                circlePath.addCircle(mX+(x-prevx)/2.0f, mY+(y-prevy)/2.0f, 3, Path.Direction.CW);
                circlePath.addCircle(mX+(x-prevx)/2.0f, mY+(y-prevy)/2.0f, 30,Path.Direction.CW);
                mX = mX+(x-prevx)/2.0f;
                mY = mY+(y-prevy)/2.0f;
                prevx=x;
                prevy=y;
            }
            else {
                prevx = x;
                prevy = y;
            }
        }

        private void touch_up() {
            prevx = prevy = -1;
//            mPath.lineTo(mX, mY);
//            circlePath.reset();
//            // commit the path to our offscreen
//            mCanvas.drawPath(mPath,  mPaint);
//            // kill this so we don't double draw
//            mPath.reset();
        }

        private float dist(float x1,float y1,float x2,float y2) {
            return (float)Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(x>=300 && x<=400 && y>=100 && y<=200) {
                        count++;
                        if(count>1) {
                            Log.d("kushhh", "mX = " + mX + " mY= " + mY + " prevx= " + prevx + " prevy= " + prevy);
                            xvals.add(mX);
                            yvals.add(mY);
                        }
                        if(count>4) {
                            float len1 = dist(xvals.get(0),yvals.get(0),xvals.get(1),yvals.get(1));
                            float len2 = dist(xvals.get(2),yvals.get(2),xvals.get(3),yvals.get(3));
                            float ratio = len1/len2;
                            Log.d("kushhh","len1 = "+len1+"  len2= "+len2+" ratio= "+ratio);
                            Toast.makeText(getContext(),"len1 = "+len1+"  len2= "+len2+" ratio= "+ratio,Toast.LENGTH_LONG).show();
                            break;
                        }
                        circlePath = new Path();
                        circlePaths.add(circlePath);
                        mX = 500;
                        mY = 500;
                        Toast.makeText(getContext(), "New Marker Added. Slide your finger on the screen to adjust the position", Toast.LENGTH_LONG).show();
                        break;
                    }
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(count>0 && count<=4) {
                        touch_move(x, y);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}
