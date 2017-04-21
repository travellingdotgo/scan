package com.scandemo;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;

/**
 * Created by user on 11/22/16.
 */
public class Utils {
    public final static String TAG = "Utils";

    public static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void reviseCameraCoor(Rect rect){
        rect.left = Math.max(rect.left, -1000);
        rect.top = Math.max(rect.top, -1000);

        rect.right = Math.min(rect.right, 1000);
        rect.bottom = Math.min(rect.bottom, 1000);

    }

    public static int[] decodeYUV420SPGray( byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        int rgb[]=new int[width*height];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;


                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                int gray  = (r*30 + g*59 + b*11 + 50) / 100;
                rgb[yp] = 0x000000FF & gray;
            }
        }
        return rgb;
    }

    public static Bitmap rawByteArray2RGBABitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }


    public static Bitmap rawByteArray2GrayBitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                y = y < 16 ? 16 : y;

                int r = y;
                int g = y;
                int b = y;

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }


		/*
		YuvImage img = new YuvImage(data, PixelFormat.YCbCr_420_SP, Status.previewWidth, Status.previewHeight, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		img.compressToJpeg(new Rect(0, 0, Status.previewWidth, Status.previewHeight), 100, out);
		Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
		*/

    public static int getMaxSeq(int[] cntArr){

        int max=-1;
        int min=999999999;
        int max_seq = -1;
        for (int y_id=0; y_id<3; y_id++){
            String str = "dots: ";
            for (int x_id=0; x_id<3; x_id++){
                int i=y_id*3+x_id;
                str += " "+cntArr[i];
                if( cntArr[i]>max )
                {
                    max=cntArr[i];
                    max_seq=i;
                }
                if( cntArr[i]<min )
                {
                    min=cntArr[i];
                }
            }

            Log.e(TAG, "dots: " + str);
        }

        Log.e(TAG, "dots: ---------------");

        return max_seq;
    }






    public static int[] decodeYUV420SP( byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        int rgb[]=new int[width*height];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                        0xff00) | ((b >> 10) & 0xff);


            }
        }
        return rgb;
    }


}
