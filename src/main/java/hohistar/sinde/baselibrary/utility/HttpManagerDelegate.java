package hohistar.sinde.baselibrary.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by sinde on 16/3/8.
 */

/**package**/class HttpManagerDelegate extends HttpManager {

    private HttpHandler mHandler = null;
    private HttpManagerThread mThread = null;

    HttpManagerDelegate(String logDir) {
        super(logDir);
        initHttpManagerThreadIfNeed();
    }

    @Override
    public IHttpResponse execute(IHttpRequest request) {
        IHttpResponse response = null;
        request.setLogDir(mLogDir);
        if (request instanceof STHttpRequest){
            STHttpRequest stHttpRequest = (STHttpRequest)request;
            WeakReference<STHttpRequest> reference = new WeakReference<STHttpRequest>(stHttpRequest);
            int timeOut = stHttpRequest.getConnectTimeout()+stHttpRequest.getReadTimeout();
//            int timeOut = 1000;
            initHttpManagerThreadIfNeed();
            int what = getMessageWhat();
            Message msg = mHandler.obtainMessage(what,reference);
            mHandler.sendMessageDelayed(msg,timeOut);
            if (stHttpRequest.mPostData != null){
                response = stHttpRequest.startSyncRequest(stHttpRequest.mPostData);
            }else if (stHttpRequest.mPostFile != null){
                response = stHttpRequest.startSyncRequestFile(stHttpRequest.mPostFile.getAbsolutePath());
            }else {
                response = stHttpRequest.startSyncRequest();
            }
            mHandler.removeMessages(msg.what);
        }else if (request instanceof STHttpUrlConnection){
            STHttpUrlConnection connection = (STHttpUrlConnection)request;
            WeakReference<STHttpUrlConnection> reference = new WeakReference<STHttpUrlConnection>(connection);
            int timeOut = connection.getConnectTimeout()+connection.getReadTimeout();
//            int timeOut = 20;
            initHttpManagerThreadIfNeed();
            int what = getMessageWhat();
            Message msg = mHandler.obtainMessage(what,reference);
            mHandler.sendMessageDelayed(msg,timeOut);
            try {
                if (connection.mPostData != null){
                    response = connection.connectPostData(connection.mPostData);
                }else if (connection.mPostFile != null){
                    response = connection.connectPosFile(connection.mPostFile);
                }else {
                    response = connection.connect();
                }
//                Log.e("TAG","what "+what+" over");
            }catch (Exception e){
                e.printStackTrace();
            }
            mHandler.removeMessages(msg.what);
        }
//        try {
//            int status = response.getStatusLineCode();
//            if (status != 200 && status != 401){
//                SysApplication.getInstance().sendBroadcast(new Intent(NetWorkReceiver.NETWORK_ERROR));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return response;
    }

    @Override
    public void clear() {
        try {
            if (mThread != null)
                mThread.quit();
            mHandler = null;
            mThread = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void initHttpManagerThreadIfNeed(){
        try {
            if (mThread == null){
                mThread = new HttpManagerThread(getClass().getSimpleName());
                mThread.start();
            }else {
                if (!mHandler.sendEmptyMessage(-1)){
                    mThread.quit();
                    mHandler = null;
                    mThread = new HttpManagerThread(getClass().getSimpleName());
                    mThread.start();
                }
            }
            while (true){
                if (mHandler != null)break;
                else Thread.sleep(100);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int what = 0;
    int getMessageWhat(){
        synchronized (this){
            if (what>1000){
                what = 1;
            }else {
                what++;
            }
            return what;
        }
    }

    private class HttpManagerThread extends HandlerThread{

        public HttpManagerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandler = new HttpHandler(getLooper());
        }
    }

    private class HttpHandler extends Handler{

        public HttpHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what>0){
                WeakReference<IHttpRequest> reference = (WeakReference<IHttpRequest>)msg.obj;
                IHttpRequest request = reference.get();
                if (request != null){
                    request.abort();
                    Log.e("TAG","what "+msg.what+" abort");
                }
            }
        }
    }

}
