package hohistar.sinde.baselibrary.utility;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sinde on 15/7/16.
 */
public class Utility_Image {

    public static final float WIDTH_ICON = 150f;
    public static final float WIDTH_M = 300f;
    public static final float WIDTH_H = 800f;
    public static final float WIDTH_XH = 1000f;
    public static final float WIDTH_XXH = 1500f;
    public static final float WIDTH_XXXH = 1800f;

    public static Bitmap compressImagePixelFromFileWithSize(byte[] data,
                                                            float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = height;//
        float ww = width;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
        return bitmap;

    }

    public static Bitmap cutImagePixelFromFileWithSize(byte[] data,
                                                            float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = height;//
        float ww = width;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.outHeight = (int)height;
        newOpts.outWidth = (int)width;
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
        return bitmap;

    }

    public static Bitmap revitionImageSize(String path) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                new File(path)));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int i = 0;
        Bitmap bitmap = null;
        while (true) {
            if ((options.outWidth >> i <= 256)
                    && (options.outHeight >> i <= 256)) {
                in = new BufferedInputStream(
                        new FileInputStream(new File(path)));
                options.inSampleSize = (int) Math.pow(2.0D, i);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(in, null, options);
                break;
            }
            i += 1;
        }
        return bitmap;
    }

    public static void compressBmpToFile(Bitmap bmp,File file){
        if (file == null) {
            throw new IllegalArgumentException("file == null");
        }
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100) {
            if(options <= 10)
                break;
            baos.reset();
            options -= 10;
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compressBmpToFile(Bitmap bmp,String path,int option){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100) {
            if(options <= option)
                break;
            baos.reset();
            options -= 5;
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final Bitmap readFile(File file){
        if (file == null)
            return null;
        byte[] data = Utility_File.read(file);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static final Bitmap readFile(String file){
        if (file == null)
            return null;
        byte[] data = Utility_File.read(file);
        if (data == null)return null;
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Bitmap readFromData(byte[] data){
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * 获取剪切后的图片
     */
    public static Intent getImageClipIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);//裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 80);//输出图片大小
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);
        return intent;
    }

    public static int readPictureDegree(File path){
        return path==null?0:readPictureDegree(path.getAbsolutePath());
    }

    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static void setPictureDegreeZero(String path){
        try {
            ExifInterface exifInterface = new ExifInterface(path);
//修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
//例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     将图片按照某个角度进行旋转
     *
     *
     @param bm
      *
     需要旋转的图片
     *
     @param degree
      *
     旋转角度
     *
     @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        //根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            //将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


}
