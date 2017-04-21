package com.scandemo;

import android.graphics.RectF;

public class Status {
    public static boolean bFaceDetected1 = false;
    public static boolean bFaceDetected2 = false;

    public static boolean bNeedUseNative = true;
    public static boolean bNeedShowMotionPix = false;
    public static boolean bNeedDetectMotion = true;
    public static boolean bNeedFaceDetection = true;
    public static boolean bNeedFaceDetection2 = true;
    public static boolean bNeedLogDetail = false;

    public static float x,y,eyesDistance;
    public static RectF rectF1;
    public static RectF rectF2;

    public static int previewWidth=1920;
    public static int previewHeight=1080;

    /*
    public static int previewWidth=2688;
    public static int previewHeight=1512;

    public static int previewWidth=1920;
    public static int previewHeight=1080;

    public static int previewWidth=1280;
    public static int previewHeight=720;  // RK.doubleCamera. 50%
    */

    public static boolean bMotionDetected = false;
    public static int MotionId = 0;


    public static RectF motionRectF1;
    public static RectF meterRectF1;

    public static RectF motionRectF2;
    public static RectF meterRectF2;
}
