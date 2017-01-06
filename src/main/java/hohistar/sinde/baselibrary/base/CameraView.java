package hohistar.sinde.baselibrary.base;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sinde on 2016/12/19.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

    private float mDensity = 1f;
    private int mWidth,mHeight;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera = null;
    private Camera.Size mPreviewSize = null;
    private PreviewCallback mPreviewCallback;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context c){
        mDensity = c.getResources().getDisplayMetrics().density;
        mWidth = c.getResources().getDisplayMetrics().widthPixels;
        mHeight = c.getResources().getDisplayMetrics().heightPixels;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            initCamera();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        freeCameraResource();
    }

    private void initCamera(){
        freeCameraResource();
        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
            freeCameraResource();
        }
        if (mCamera == null)
            return;
        setCameraParams();
        mPreviewCallback = new PreviewCallback();
        mCamera.setPreviewCallback(mPreviewCallback);
        mCamera.setDisplayOrientation(90);
        startPreview();
    }

    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.set("orientation", "portrait");
            params.setPictureFormat(PixelFormat.JPEG);
//            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            findBestSize(params);
            mCamera.setParameters(params);
        }
    }

    private final double BEST_RATE = 4f/3;//预览尺寸
    private void findBestSize(Camera.Parameters params){
//        List<Camera.Size> sizes = params.getSupportedVideoSizes();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        double tempRate = 2;
        double tempRate2 = 1;
        for (Camera.Size size:sizes){
            double rate =((double) size.width)/size.height;
//            if (Math.abs(rate-BEST_RATE)<tempRate && size.width<=getResources().getDisplayMetrics().widthPixels){
            if (Math.abs(rate-BEST_RATE)<tempRate){
                tempRate = Math.abs(rate-BEST_RATE);
                mPreviewSize = size;
                tempRate2 = rate;
                Log.i("TAG","width:"+size.width+",height:"+size.height);
            }
        }
        params.setPreviewSize(mPreviewSize.width,mPreviewSize.height);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null){
            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
            layoutParams.height = (int)(layoutParams.width*tempRate2);
            setLayoutParams(layoutParams);
        }
        if (mCameraViewListener != null)mCameraViewListener.resize(layoutParams.width,layoutParams.height);
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                onFocus(event.getX(),event.getY());
                break;
        }
        return super.onTouchEvent(event);
    }

    private void onFocus(float x,float y){
        if (mCamera == null)return;
        Camera.Parameters parameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT >= 14){
            if (parameters.getMaxNumFocusAreas() <=0)return;
            List<Camera.Area> list = new ArrayList<>();
            int top = (int)(y-mDensity*30);top = top>0?top:0;
            int left = (int)(x-mDensity*30);left = left>0?left:0;
            int bottom = (int)(y+mDensity*30);bottom=bottom>getMeasuredHeight()?getMeasuredHeight():bottom;
            int right = (int)(x+mDensity*30);right=right>getMeasuredWidth()?getMeasuredWidth():right;
            Camera.Area area = new Camera.Area(new Rect(left,top,right,bottom),100);
            list.add(area);
            parameters.setFocusAreas(list);
            try {
                mCamera.setParameters(parameters);
                mCamera.autoFocus(null);
            }catch (Exception e){
                e.printStackTrace();
                mCamera.cancelAutoFocus();
            }
        }
    }

    private CameraViewListener mCameraViewListener;
    public void setCameraViewListener(CameraViewListener listener){
        mCameraViewListener = listener;
    }

    public interface CameraViewListener{

        void resize(int width, int height);

    }

    private class PreviewCallback implements Camera.PreviewCallback{


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    }

}
