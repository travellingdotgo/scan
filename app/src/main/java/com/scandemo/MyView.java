package com.scandemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class MyView extends View  {
    public final static String TAG  = "MyView";

    Paint mPaint;
    int Id = 1;

    public void setCamId( int i ){
        Id = i;
    }

    public MyView(Context context) {
        super(context);
        initPaint();
    }

    public MyView(Context context, AttributeSet attrs){
        super(context, attrs);
        initPaint();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(0XFF00FF00);
        mPaint.setTextSize(36);
        mPaint.setStrokeWidth(5);
    }

    public void onDraw(Canvas canvas){
        super.onDraw(canvas);


        boolean bFaceDetected = Status.bFaceDetected1;
        RectF rectF = Status.rectF1;
        RectF motionRectF = Status.motionRectF1;
        if( Id==2 ){
            bFaceDetected = Status.bFaceDetected2;
            rectF = Status.rectF2;
            motionRectF = Status.motionRectF2;
        }

        if(bFaceDetected) {
            mPaint.setStyle(Style.STROKE);
            mPaint.setAntiAlias(true);
            mPaint.setColor(0XFF00FF00);
            canvas.drawRect(    rectF.left*Screen.mScreenWidth,
                                rectF.top*Screen.mScreenWidth        *Status.previewHeight/Status.previewWidth,
                    rectF.right*Screen.mScreenWidth,
                    rectF.bottom*Screen.mScreenWidth     *Status.previewHeight/Status.previewWidth,
                    mPaint);
            Log.e(TAG, " " + rectF.left * Status.previewWidth + " " + rectF.right*Status.previewWidth );
        }
    }



}