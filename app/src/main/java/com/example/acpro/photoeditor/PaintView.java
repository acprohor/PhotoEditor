package com.example.acpro.photoeditor;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class PaintView extends View{
    public static int BRUSH_SIZE = 20;
    public int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    public Path mPath;
    public Paint mPaint;

    public ArrayList<FingerPath> paths = new ArrayList<>();
    public int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    public int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap loadBitmap;

    public String drawMode = "default";

    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public PaintView(Context context) {
        this(context, null);
    }
    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        clear();
    }
    public void clear() {
        //backgroundColor = DEFAULT_BG_COLOR;
        mCanvas.drawColor(Color.WHITE);
        paths.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // if app was started by launch icon
        if (mBitmap == null) {
            clearBitmap();
        }

        mBitmap = Bitmap.createScaledBitmap(mBitmap, getWidth(), getHeight(), true);
        mCanvas = new Canvas(mBitmap);
    }

    private void clearBitmap() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.WHITE);
        mCanvas = new Canvas(mBitmap);
    }
/*
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        //canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (paths.size() > 0) {
            for (FingerPath fp : paths) {
                mPaint.setColor(fp.color);
                mPaint.setStrokeWidth(fp.strokeWidth);
                mPaint.setMaskFilter(null);

                mCanvas.drawPath(fp.path, mPaint);

            }
        }

        if (loadBitmap != null){
            canvas.drawBitmap(loadBitmap, 0, 0, mBitmapPaint);
            //canvas.drawBitmap(loadBitmap, 0,0,mBitmapPaint);
        }
        else {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }
        canvas.restore();
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //canvas.drawColor(Color.WHITE);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);
            canvas.drawPath(fp.path, mPaint);
        }

        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

    }

    private void touchMove(float x, float y) {

        float left = Math.min(mX, x);
        float right = Math.max(mX, x);
        float top = Math.min(mY, y);
        float bottom = Math.max(mY, y);

        switch (drawMode) {
            case "default":
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                break;
            case "rect":
                mPath.reset();
                mPath.addRect(left, top, right, bottom, Path.Direction.CCW);
                break;
            case "circle":
                mPath.reset();
                mPath.addOval(left, top, right, bottom, Path.Direction.CCW);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    private void touchUp() {
    }

    public Bitmap getImage(){
        /*Canvas newC = new Canvas();*/

        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(fp.path, mPaint);

        }
        /*newC.drawBitmap(mBitmap, 0, 0, mBitmapPaint);*/

        return mBitmap;
    }

    public void loadImage(Bitmap bitmap){
        /*File root = Environment.getExternalStorageDirectory();
        Bitmap bMap = BitmapFactory.decodeFile(root+"/Pictures/01.jpg");*/

        mCanvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
    }

    public void setBackground(Bitmap image) {
        if (getHeight() > 0 && getWidth() > 0) {
            image = Bitmap.createScaledBitmap(image, getWidth(), getHeight(), true);
        }

        image.setConfig(Bitmap.Config.ARGB_8888);
        mBitmap = image;
        paths.clear();
        mCanvas = new Canvas(mBitmap);

    }

    public int getLineWidth(){
        return strokeWidth;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }
}
