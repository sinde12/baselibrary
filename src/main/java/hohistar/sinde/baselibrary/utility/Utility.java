package hohistar.sinde.baselibrary.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tianbin1 on 2015/4/8.
 */
public class Utility {

    public static final int UI_UIUpdate = 0x10001;
    public static final int UI_UIInit = 0x10000;
    public static final int UI_UIProcessDialog = 0x10002;
    public static final int UI_UIDismiss_ProcessDialog = 0x10007;
    public static final int UI_UIProcessToast= 0x10003;
    public static final int UI_UITOKENFAIL= 0x10004;
    public static final int UI_LOGIN=0x10005;
    public static final int UI_Default_Dialog =0x10006;

    public static final String ID = "ID";
    public static final String ProdName = "ProdName";
    public static final String Display = "Display";
    public static final String IsStock = "IsStock";
    public static final String RealPrice = "RealPrice";
    public static final String SuggestPrice = "SuggestPrice";
    public static final String PromotionPrice = "PromotionPrice";
    public static final String ShelfLife = "ShelfLife";
    public static final String Count = "Count";
    public static final String Unit = "Unit";
    public static final String Reporter = "Reporter";
    public static final String ReportDate = "ReportDate";

    private static final boolean DEBUG = true;


    //压缩像素 ，减少载入内存的大小

    public static Bitmap compressImagePixelFromFileWithSize(byte[] data,float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,newOpts);
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
        newOpts.inSampleSize = be;// 设置采样率
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;// 该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,newOpts);
        return bitmap;
    }

    //压缩图片质量，针对于图片网路传输等
    public static void compressBmpToFile(Bitmap bmp,File file){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            options -= 5;
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

    public static float getScreenWidthPixel(Activity context){
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
    public static float getScreenHeightPixel(Activity context){
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
    public static float getScreenDensity(Activity context){
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    public static String getDeviceID(Context c){
        String id= null;
        TelephonyManager TelephonyMgr = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        id = TelephonyMgr.getDeviceId();
        if (id == null){
            WifiManager wm = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
            id = wm.getConnectionInfo().getMacAddress();
        }
        return id;
    }

    public static byte[] bytesWithInputStream(final InputStream is) throws IOException {
        byte[] buffers = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length = 0;
        while ((length = is.read(buffers, 0, 512)) != -1) {
            baos.write(buffers, 0, length);
        }
        return baos.toByteArray();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    

    public static void i(Class clazz,String obj){
        if (DEBUG)
            Log.i(clazz.getSimpleName(),obj);
    }

    public static void v(Class clazz,String obj){
        if (DEBUG)
            Log.v(clazz.getSimpleName(),obj);
    }

    public static void w(Class clazz,String obj){
        if (DEBUG)
            Log.w(clazz.getSimpleName(),obj);
    }

    public static void e(Class clazz,String obj){
        if (DEBUG)
            Log.e("YJR:: "+clazz.getSimpleName(),obj);
    }

    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static void releaseMediaPlayerIfNeed(){
        if (mediaPlayer != null){
            try {
                lastAudioPlayListener.over(true);
                lastAudioPlayListener = null;
                mLastSrc = null;
                mediaPlayer.stop();
                mediaPlayer.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static MediaPlayer mediaPlayer = null;
    private static long lastPlayTime = 0;
    private static AudioPlayListener lastAudioPlayListener = null;
    private static String mLastSrc = null;
    public static void playMusic(Context context,String src,final AudioPlayListener listener){
        long now = System.currentTimeMillis();
        if (now-lastPlayTime<100)return;
        lastPlayTime = now;
        try {
            try {
                synchronized (Utility.class){
                    if (mediaPlayer != null){
                        if (lastAudioPlayListener != null)lastAudioPlayListener.over(true);
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        if (mLastSrc != null && mLastSrc.equalsIgnoreCase(src))return;
                    }
                }
            }catch (Exception e1){
                e1.printStackTrace();
            }
            lastAudioPlayListener = listener;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(src);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mp != null){
                        mp.release();
                        mediaPlayer = null;
                    }
                    if (listener != null){
                        listener.over(true);
                    }
                    mLastSrc = null;
                    lastAudioPlayListener = null;
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.release();
                    mediaPlayer = null;
                    if (listener != null){
                        listener.over(false);
                    }
                    mLastSrc = null;
                    lastAudioPlayListener = null;
                    return false;
                }
            });
            mediaPlayer.start();
            mLastSrc = src;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface AudioPlayListener{

        void over(boolean isSuccess);

    }

}
