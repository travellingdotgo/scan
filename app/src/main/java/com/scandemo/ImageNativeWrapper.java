package com.scandemo;

/**
 * Created by user on 11/23/16.
 */
public class ImageNativeWrapper {


    public static native int init(int w,int h,int threshhold);
    public static native int deinit();
    public static native int[] detect(int w, int h, byte[] data);

    static {
        System.loadLibrary("NativeHelper");
    }

}
