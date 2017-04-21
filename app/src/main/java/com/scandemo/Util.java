
package com.scandemo;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

   public static String[] getAssetsPath(Context context,String path) throws IOException {
       String fileNames[] = context.getAssets().list(path);
       return fileNames;


   }


    public static byte[] readModel(Context context) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        try {
            inputStream = context.getAssets().open("model");

            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] readLiveDetectModel(Context context,String modelName)
    {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count = -1;
        byte[] buffer = new byte[1024];
        try {
            inputStream = context.getAssets().open(modelName);

            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return byteArrayOutputStream.toByteArray();
    }


    public static byte[] getGrayscale(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        byte[] ret = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int j = 0; j < bitmap.getHeight(); ++j)
            for (int i = 0; i < bitmap.getWidth(); ++i) {
                int pixel = bitmap.getPixel(i, j);
                int red = ((pixel & 0x00FF0000) >> 16);
                int green = ((pixel & 0x0000FF00) >> 8);
                int blue = pixel & 0x000000FF;
                ret[j * bitmap.getWidth() + i] = (byte) ((299 * red + 587
                        * green + 114 * blue) / 1000);
            }
        return ret;
    }


}
