package hohistar.sinde.baselibrary.utility;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by sinde on 15/12/26.
 */
public final class STImageLoader {

    private static STImageLoader imageLoader = null;
    private Map<String,SoftReference<Bitmap>> mBitmapCache = new HashMap<String, SoftReference<Bitmap>>();
    private HashMap<String,String> mNetWorkBitmapCache = new HashMap<String, String>();
    private File mCacheFiles = new File(Environment.getExternalStorageDirectory(),"stimageloader");
    private String mCacheFileName = "data.bin";
    private LruCache<String,Bitmap> mMemoryCache;
    private final List<String> mDownLoadingURLs = new ArrayList<>();

    public static STImageLoader getInstance(){
        if (imageLoader == null){
            synchronized (STImageLoader.class){
                if (imageLoader == null){
                    imageLoader = new STImageLoader();
                }
                File file = new File(imageLoader.mCacheFiles,getFoldName());
                if (!file.exists()){
                    file.mkdirs();
                }else {
                    File f = new File(file,imageLoader.mCacheFileName);
                    if (f.exists()){
                        imageLoader.recevoryCache(f);
                    }
                }
                imageLoader.deleteSomeCacheIfNeed();
                final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

                // 使用可用内存的1/8来作为Memory Cache
                final int cacheSize = maxMemory / 8;
                imageLoader.mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
                    @Override
                    protected int sizeOf(String key, Bitmap value) {
                        return value.getByteCount()/1024;
                    }
                };
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                    public X509Certificate[] getAcceptedIssuers(){return null;}
                    public void checkClientTrusted(X509Certificate[] certs, String authType){}
                    public void checkServerTrusted(X509Certificate[] certs, String authType){}
                }};

                // Install the all-trusting trust manager
                try {// 注意这部分一定要
                    HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return imageLoader;
    }

    public boolean downloadImageToPath(final String url,String file){
        try {
            STHttpRequest request = new STHttpRequest(url);
            request.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,30*1000);
            request.setParameter(CoreConnectionPNames.SO_TIMEOUT,30* 1000);
            STHttpRequest.STHttpResponse response = request.startSyncRequest();
            if (response.getStatusLineCode() == 200){
                byte[] data = response.getContentData();
                if (data != null){
                    synchronized (STImageLoader.this){
                        Utility_File.write(data,file,false);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void displayImage(String pathOrUrl,ImageView iv,final int type){
        if (TextUtils.isEmpty(pathOrUrl))return;
        if (pathOrUrl.contains("http")){
            displayImageWithURL(pathOrUrl, iv, type);
        }else {
            displayImageWithPath(pathOrUrl, iv);
        }
    }

    /**
     *@param type 0 icon,1 middle,2 high,3 x-high,4 原图
     * **/
    public void displayImageWithURL(final String url, ImageView iv,final int type){
        String tag = (String) iv.getTag();
        if (!TextUtils.isEmpty(tag)){
            Log.e("STImageLoader","b tag:"+tag);
        }else {
            Log.e("STImageLoader","b tag: null "+url);
        }
        iv.setTag(url);
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(iv);
        final String path;
        if (mNetWorkBitmapCache.containsKey(url)){
            String sPath = mNetWorkBitmapCache.get(url);
            if (sPath != null && new File(sPath).exists()){
                path = sPath;
            }else {
                path = null;
            }
        }else {
            path = null;
        }
        if (path == null){
            synchronized (mDownLoadingURLs){
                if (mDownLoadingURLs.contains(url)){
                    ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            int count =0;
                            while (mNetWorkBitmapCache.get(url) == null){
                                try {
                                    Thread.sleep(200);
                                    if (count++<100)break;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            String path = mNetWorkBitmapCache.get(url);
                            if (path != null && new File(path).exists()){
                                Bitmap bitmap = null;
                                boolean flag = true;
                                if (mBitmapCache.get(path+type) != null && mBitmapCache.get(path+type).get() != null){
                                    bitmap = mBitmapCache.get(path+type).get();
                                    flag = false;
                                }
                                if (bitmap == null){
                                    bitmap = readBitmapWithType(path,type);
                                }
                                Handler handler = getDefaultHandle(weakReference,url);
                                if (bitmap != null){
                                    if (flag)
                                        mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
                                    handler.sendMessage(handler.obtainMessage(1,bitmap));
                                }
                            }
                        }
                    });
                }else {
                    mDownLoadingURLs.add(url);
                    ThreadPoolManager.getBaoBiaoPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String path = downloadImage(url);
                                if (path != null){
                                    Bitmap bitmap = readBitmapWithType(path,type);
                                    Handler handler = getDefaultHandle(weakReference,url);
                                    if (bitmap != null){
                                        mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
                                        mNetWorkBitmapCache.put(url,path);
                                        handler.sendMessage(handler.obtainMessage(1,bitmap));
                                        saveCache();
                                    }
                                }else {
                                    Handler handler = getDefaultHandle(weakReference,url);
                                    handler.sendMessage(handler.obtainMessage(1,null));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }finally {
                                synchronized (mDownLoadingURLs){
                                    mDownLoadingURLs.remove(url);
                                }
                            }
                        }
                    });
                }
            }
        }else {
            ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    boolean flag = true;
                    if (mBitmapCache.get(path+type) != null && mBitmapCache.get(path+type).get() != null){
                        bitmap = mBitmapCache.get(path+type).get();
                        flag = false;
                    }
                    if (bitmap == null){
                        bitmap = readBitmapWithType(path,type);
                    }
                    Handler handler = getDefaultHandle(weakReference,url);
                    if (bitmap != null){
                        if (flag)
                            mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
                        Utility.i(getClass(),"img read from local");
                        handler.sendMessage(handler.obtainMessage(1,bitmap));
                    }
                }
            });
        }
    }

//    /**
//     *@param type 0 icon,1 middle,2 high,3 x-high,4 原图
//     * **/
//    public void displayImageWithURL(final String url, ImageView iv,final int type){
//        final WrapImageView wrapImageView = new WrapImageView(url,iv);
//        final String path;
//        if (mNetWorkBitmapCache.containsKey(url)){
//            String sPath = mNetWorkBitmapCache.get(url);
//            if (sPath != null && new File(sPath).exists()){
//                path = sPath;
//            }else {
//                path = null;
//            }
//        }else {
//            path = null;
//        }
//        if (path == null){
//            synchronized (mDownLoadingURLs){
//                if (mDownLoadingURLs.contains(url)){
//                    ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            while (mDownLoadingURLs.contains(url)){
//                                try {
//                                    Thread.sleep(200);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            String path = mNetWorkBitmapCache.get(url);
//                            if (path != null && new File(path).exists()){
//                                Bitmap bitmap = null;
//                                boolean flag = true;
//                                if (mBitmapCache.get(path+type) != null && mBitmapCache.get(path+type).get() != null){
//                                    bitmap = mBitmapCache.get(path+type).get();
//                                    flag = false;
//                                }
//                                if (bitmap == null){
//                                    bitmap = readBitmapWithType(path,type);
//                                }
//                                Handler handler = getDefaultHandle(wrapImageView,url);
//                                if (bitmap != null){
//                                    if (flag)
//                                        mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
//                                    handler.sendMessage(handler.obtainMessage(1,bitmap));
//                                }
//                            }
//                        }
//                    });
//                }else {
//                    mDownLoadingURLs.add(url);
//                    ThreadPoolManager.getBaoBiaoPool().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                String path = downloadImage(url);
//                                if (path != null){
//                                    Bitmap bitmap = readBitmapWithType(path,type);
//                                    Handler handler = getDefaultHandle(wrapImageView,url);
//                                    if (bitmap != null){
//                                        mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
//                                        mNetWorkBitmapCache.put(url,path);
//                                        handler.sendMessage(handler.obtainMessage(1,bitmap));
//                                        saveCache();
//                                    }
//                                }else {
//                                    Handler handler = getDefaultHandle(wrapImageView,url);
//                                    handler.sendMessage(handler.obtainMessage(1,null));
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }finally {
//                                synchronized (mDownLoadingURLs){
//                                    mDownLoadingURLs.remove(url);
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        }else {
//            ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    Bitmap bitmap = null;
//                    boolean flag = true;
//                    if (mBitmapCache.get(path+type) != null && mBitmapCache.get(path+type).get() != null){
//                        bitmap = mBitmapCache.get(path+type).get();
//                        flag = false;
//                    }
//                    if (bitmap == null){
//                        bitmap = readBitmapWithType(path,type);
//                    }
//                    Handler handler = getDefaultHandle(wrapImageView,url);
//                    if (bitmap != null){
//                        if (flag)
//                            mBitmapCache.put(path+type,new SoftReference<Bitmap>(bitmap));
//                        Utility.i(getClass(),"img read from local");
//                        handler.sendMessage(handler.obtainMessage(1,bitmap));
//                    }
//                }
//            });
//        }
//    }

    private Bitmap readBitmapWithType(String path,int type){
        float size;
        if (type == 0){
            size = Utility_Image.WIDTH_ICON;
        }else if (type == 1){
            size = Utility_Image.WIDTH_M;
        }else if (type == 2){
            size = Utility_Image.WIDTH_H;
        }else if (type == 3){
            size = Utility_Image.WIDTH_XH;
        }else {
            size = -1;
        }
        Bitmap bitmap = null;
        try {
            if (size == -1){
                bitmap = Utility_Image.readFile(path);
            }else {
                bitmap = Utility_Image.cutImagePixelFromFileWithSize(Utility_File.read(path),size,size);
            }
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private Handler getDefaultHandle(final WeakReference<ImageView> weakReference, final String url){
        return new IHandler(Looper.getMainLooper(),url){
            @Override
            public void handleMessage(Message msg) {
                ImageView imageView = weakReference.get();
                if (imageView != null){
                    Object object = imageView.getTag();
                    if (object != null && object.equals(url) && msg.obj != null){
                        imageView.setImageBitmap((Bitmap)msg.obj);
                    }else {
                        if (object == null)Utility.w(getClass(),"load img fail tag is null");
                        else if (msg.obj == null)Utility.w(getClass(),"load img fail msg.obj is null");
                        else{
                            Utility.w(getClass(),"url:"+url+"  |  tagUrl:"+object+"\n mURL:"+mURL);
                        }
//                        imageView.setImageResource(R.drawable.img_show_travel_list_item_default);
                    }
                }
            }
        };
    }

//    private Handler getDefaultHandle(final WrapImageView weakReference, final String url){
//        return new IHandler(Looper.getMainLooper(),url){
//            @Override
//            public void handleMessage(Message msg) {
//                ImageView imageView = weakReference.weakReference.get();
//                if (imageView != null){
//                    Object object = weakReference.mTag;
////                    if (object != null && msg.obj != null){
//                    if (object != null && object.equals(url) && msg.obj != null){
//                        imageView.setImageBitmap((Bitmap)msg.obj);
//                    }else {
//                        if (object == null)Utility.w(getClass(),"load img fail tag is null");
//                        else if (msg.obj == null)Utility.w(getClass(),"load img fail msg.obj is null");
//                        else{
//                            Utility.w(getClass(),"url:"+url+"  |  tagUrl:"+object+"\n mURL:"+mURL);
//                        }
//                        imageView.setImageResource(R.drawable.img_show_travel_list_item_default);
//                    }
//                }
//            }
//        };
//    }

    private class IHandler extends Handler{

        String mURL;

        IHandler(Looper looper,String url){
            super(looper);
            mURL = url;
        }

    }

    private class WrapImageView{

        String mTag = null;
        WeakReference<ImageView> weakReference = null;

        public WrapImageView(String tag,ImageView iv) {
            mTag = tag;
            weakReference = new WeakReference<ImageView>(iv);
        }
    }

    private String downloadImage(String url){
        Utility.e(getClass(),"start request image:"+url);
        String path = null;
        try {
            STHttpUrlConnection connection = new STHttpUrlConnection(url);
            connection.setReadTimeout(60*1000);
            connection.setConnectTimeout(60*1000);
            IHttpResponse response = HttpManager.get(null).execute(connection);
            if (response.getStatusLineCode() == 200){
                byte[] data = response.getContentData();
                Utility.e(getClass(),"end request image:"+url);
                if (data != null){
                    synchronized (STImageLoader.this){
                        File f = new File(mCacheFiles,getFoldName());
                        File file = new File(f,getKey(url));
                        Utility_File.write(data,file,false);
                        path = file.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

//    /**
//     *@param type 0 icon,1 middle,2 high,3 x-high,4 原图
//     * **/
//    public void displayImageWithURL(final String url, ImageView iv,final int type){
//        iv.setTag(url);
//        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(iv);
//        ThreadPoolManager.getDownloadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                String path = null;
//                if (mNetWorkBitmapCache.containsKey(url)){
//                    path = mNetWorkBitmapCache.get(url);
//                }
//                if (path != null){
//                    Bitmap bitmap = null;
//                    boolean flag = true;
//                    if (mBitmapCache.get(path) != null && mBitmapCache.get(path).get() != null){
//                        bitmap = mBitmapCache.get(path).get();
//                        flag = false;
//                    }
//                    if (bitmap == null){
//                        try {
//                            float size;
//                            if (type == 0){
//                                size = Utility_Image.WIDTH_ICON;
//                            }else if (type == 1){
//                                size = Utility_Image.WIDTH_M;
//                            }else if (type == 2){
//                                size = Utility_Image.WIDTH_H;
//                            }else if (type == 3){
//                                size = Utility_Image.WIDTH_XH;
//                            }else {
//                                size = -1;
//                            }
//                            if (size == -1){
//                                bitmap = Utility_Image.readFile(path);
//                            }else {
//                                bitmap = Utility_Image.cutImagePixelFromFileWithSize(Utility_File.read(path),size,size);
//                            }
//                        }catch (OutOfMemoryError e){
//                            e.printStackTrace();
//                        }
//                    }
//                    Handler handler = new Handler(Looper.getMainLooper()){
//                        @Override
//                        public void handleMessage(Message msg) {
//                            ImageView imageView = weakReference.get();
//                            if (imageView != null){
//                                Object object = imageView.getTag();
//                                if (object != null && object.equals(url)){
//                                    imageView.setImageBitmap((Bitmap)msg.obj);
//                                }else {
//                                    imageView.setImageResource(R.drawable.img_list_icon_default);
//                                }
//                            }
//                        }
//                    };
//                    if (bitmap != null){
//                        if (flag)
//                            mMemoryCache.put(path,bitmap);
//                        handler.sendMessage(handler.obtainMessage(1,bitmap));
//                    }
//                }else {
//                    try {
//                        Utility.e(getClass(),"start request image");
//                        STHttpUrlConnection connection = new STHttpUrlConnection(url);
////                        STHttpRequest request = new STHttpRequest(url);
//                        connection.setReadTimeout(60*1000);
//                        connection.setConnectTimeout(60*1000);
//                        IHttpResponse response = AppManager.getHttpManager().execute(connection);
//                        if (response.getStatusLineCode() == 200){
//                            byte[] data = response.getContentData();
//                            Utility.e(getClass(),"end request image");
//                            if (data != null){
//                                synchronized (STImageLoader.this){
//                                    File f = new File(mCacheFiles,getFoldName());
//                                    File file = new File(f,getKey(url));
//                                    Utility_File.write(data,file,false);
//                                    Bitmap bitmap;
//                                    float size;
//                                    if (type == 0){
//                                        size = Utility_Image.WIDTH_ICON;
//                                    }else if (type == 1){
//                                        size = Utility_Image.WIDTH_M;
//                                    }else if (type == 2){
//                                        size = Utility_Image.WIDTH_H;
//                                    }else if (type == 3){
//                                        size = Utility_Image.WIDTH_XH;
//                                    }else {
//                                        size = -1;
//                                    }
//                                    if (size == -1){
//                                        bitmap = Utility_Image.readFile(file);
//                                    }else {
//                                        bitmap = Utility_Image.cutImagePixelFromFileWithSize(Utility_File.read(file),size,size);
//                                    }
//                                    if (bitmap == null){
//                                        bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//                                        Utility_File.delete(file);
//                                    }
//                                    Handler handler = new Handler(Looper.getMainLooper()){
//                                        @Override
//                                        public void handleMessage(Message msg) {
//                                            ImageView imageView = weakReference.get();
//                                            Bitmap bit = (Bitmap)msg.obj;
//                                            if (imageView != null){
//                                                Object object = imageView.getTag();
//                                                if (object != null && object.equals(url)){
//                                                    imageView.setImageBitmap(bit);
//                                                }else {
//                                                    imageView.setImageResource(R.drawable.img_list_icon_default);
//                                                }
//                                            }
//                                        }
//                                    };
//                                    if (bitmap != null){
//                                        mMemoryCache.put(file.getAbsolutePath(),bitmap);
//                                        mNetWorkBitmapCache.put(url,file.getAbsolutePath());
//                                        handler.sendMessage(handler.obtainMessage(1,bitmap));
//                                        saveCache();
//                                    }
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

    public void displayScaleImageWithURL(final String url, ImageView iv,final int height){
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(iv);
        ThreadPoolManager.getBaoBiaoPool().execute(new Runnable() {
            @Override
            public void run() {
                String path = null;
                if (mNetWorkBitmapCache.containsKey(url)){
                    path = mNetWorkBitmapCache.get(url);
                }
                if (path != null){
                    final Bitmap bitmap = Utility_Image.readFile(path);
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            ImageView imageView = weakReference.get();
                            if (imageView != null){
                                int width = bitmap.getWidth()*height/bitmap.getHeight();
                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                if (layoutParams == null){
                                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                }
                                layoutParams.width = width;
                                imageView.setLayoutParams(layoutParams);
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    };
                    if (bitmap != null){
                        mBitmapCache.put(path,new SoftReference<Bitmap>(bitmap));
                        handler.sendEmptyMessage(1);
                    }
                }else {
                    try {
                        STHttpRequest request = new STHttpRequest(url);
                        request.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,30*1000);
                        request.setParameter(CoreConnectionPNames.SO_TIMEOUT,30* 1000);
                        STHttpRequest.STHttpResponse response = request.startSyncRequest();
                        if (response.getStatusLineCode() == 200){
                            byte[] data = response.getContentData();
                            if (data != null){
                                synchronized (STImageLoader.this){
                                    File f = new File(mCacheFiles,getFoldName());
                                    File file = new File(f,getKey());
                                    Utility_File.write(data,file,false);
                                    final Bitmap bitmap = Utility_Image.readFile(file);
                                    Handler handler = new Handler(Looper.getMainLooper()){
                                        @Override
                                        public void handleMessage(Message msg) {
                                            ImageView imageView = weakReference.get();
                                            if (imageView != null){
                                                int width = bitmap.getWidth()*height/bitmap.getHeight();
                                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                                if (layoutParams == null){
                                                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                }
                                                layoutParams.width = width;
                                                imageView.setImageBitmap(bitmap);
                                            }
                                        }
                                    };
                                    if (bitmap != null){
                                        mBitmapCache.put(file.getAbsolutePath(),new SoftReference<Bitmap>(bitmap));
                                        mNetWorkBitmapCache.put(url,file.getAbsolutePath());
                                        handler.sendEmptyMessage(1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public void displayImageWithPath(final String path, ImageView iv){
        if (mBitmapCache.get(path) != null && mBitmapCache.get(path).get()!= null){
            iv.setImageBitmap(mBitmapCache.get(path).get());
            return;
        }
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(iv);
        ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = Utility_Image.readFile(path);
                Handler handler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg) {
                        ImageView imageView = weakReference.get();
                        if (imageView != null){
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                };
                if (bitmap != null){
                    mBitmapCache.put(path,new SoftReference<Bitmap>(bitmap));
                    handler.sendEmptyMessage(1);
                }
            }
        });
    }

    public void displayVideoThumbWithPath(final String path, ImageView iv){
        if (mBitmapCache.get(path) != null && mBitmapCache.get(path).get()!= null){
            String tag = (String) iv.getTag();
            if (tag != null && tag.equalsIgnoreCase(path))
                return;
            iv.setTag(path);
            iv.setImageBitmap(mBitmapCache.get(path).get());
            return;
        }
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(iv);
        ThreadPoolManager.getImageDisplayPool().execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = getVideoThumbnail(path);
                Handler handler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg) {
                        ImageView imageView = weakReference.get();
                        if (imageView != null){
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                };
                if (bitmap != null){
                    mBitmapCache.put(path,new SoftReference<Bitmap>(bitmap));
                    handler.sendEmptyMessage(1);
                }
            }
        });
    }

    public static String getKey(){
        return Utility_Date.format(new Date(),"yyyyMMddhhmmssSSSZ")+".jpg";
    }

    public static String getKey(String url){
        String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (!TextUtils.isEmpty(imageName)){
            if (imageName.contains(".") && imageName.length()>1){
                int index = imageName.lastIndexOf(".");
                imageName = imageName.substring(0,index);
            }
        }
        return imageName;
    }

    public static String getFoldName(){
        return Utility_Date.format(new Date(),"yyyyMMdd");
    }

    private  Bitmap getVideoThumbnail(String filePath) {
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

    boolean mIsSaving = false;
    public void saveCache(){
        if (!mIsSaving){
            mIsSaving = true;
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                File file = new File(mCacheFiles,getFoldName());
                if (!file.exists()){
                    file.mkdirs();
                }
                File f = new File(file,mCacheFileName);
                fos = new FileOutputStream(f);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(mNetWorkBitmapCache);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if (oos!=null)oos.close();
                    if (fos!=null)fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mIsSaving = false;
        }
    }

    public String getCachePath(){
        return mCacheFiles.getAbsolutePath();
    }

    private void recevoryCache(File file){
        if (!file.exists())return;
        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            HashMap<String,String> data = (HashMap<String, String>) ois.readObject();
            if (data != null){
                mNetWorkBitmapCache = data;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deleteSomeCacheIfNeed(){
        ThreadPoolManager.getBaoBiaoPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Date now = Utility_Date.format(Utility_Date.format(new Date(),"yyyyMMdd"),"yyyyMMdd");
                    long n = now.getTime()/1000;
                    List<File> files = new ArrayList<File>();
                    for (File child:mCacheFiles.listFiles()){
                        if (child.isFile()){
                            child.delete();
                        }else {
                            try {
                                long t = Utility_Date.format(child.getName(),"yyyyMMdd").getTime()/1000;
                                if (n-t>=60*60*24){
                                    files.add(child);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    for (File f:files){
                        Utility_File.delete(f);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
//            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }

}
