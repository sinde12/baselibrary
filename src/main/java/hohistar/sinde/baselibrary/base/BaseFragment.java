package hohistar.sinde.baselibrary.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import hohistar.sinde.baselibrary.utility.Utility;

/**
 * Created by sinde on 15/8/15.
 */
public abstract class BaseFragment extends Fragment {

    public BaseFragment() {}

    boolean mIsOnFront = false;

    private BaseActivity mBaseActivity;

    public boolean isOnFront(){
        return mIsOnFront;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)recoveryDataFromBundle(savedInstanceState);
        mHandler = new STHandler(this);
        mIsOnFront = true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private View mView;
    private boolean mViewIsDestroy = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mIsOnFront = true;
        if (mView == null || mViewIsDestroy){
            mViewIsDestroy = false;
            mView = onCreateViewBySelf(inflater,container,savedInstanceState);
        }else {
            mView.requestLayout();
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof BaseActivity){
            mBaseActivity = (BaseActivity) getActivity();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        saveIfNeed(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * save some data which use AutoSaveInActivity anim
     * **/
    private void saveIfNeed(Bundle outState){
        Field[] fields = getClass().getDeclaredFields();
        for (Field field:fields){
            if (field.isAnnotationPresent(AutoSaveInActivity.class)){
                try {
                    field.setAccessible(true);
                    Object obj = field.get(this);
                    if (obj==null)continue;
                    if (obj.getClass() == Integer.TYPE){
                        outState.putInt(field.getName(),(int)obj);
                    }else if (obj.getClass() == Float.TYPE){
                        outState.putFloat(field.getName(),(float)obj);
                    }else if (obj.getClass() == Double.TYPE){
                        outState.putDouble(field.getName(),(double)obj);
                    }else if (obj instanceof Serializable){
                        outState.putSerializable(field.getName(),(Serializable)obj);
                    }else if (obj instanceof Parcelable){
                        outState.putParcelable(field.getName(),(Parcelable)obj);
                    }else {
                        throw new RuntimeException(obj.getClass().getName()+" is not simple type or not implement Serializable,Parcelable");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * read data from savedInstanceState to set field which use AutoSaveInActivity anim
     * **/
    private void recoveryDataFromBundle(Bundle savedInstanceState){
        Field[] fields = getClass().getDeclaredFields();
        for (Field field:fields){
            if (field.isAnnotationPresent(AutoSaveInActivity.class)){
                try {
                    if (savedInstanceState.containsKey(field.getName())){
                        Object value = savedInstanceState.get(field.getName());
                        field.setAccessible(true);
                        field.set(this,value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void setBaseActivity(BaseActivity activity){
        mBaseActivity = activity;
    }

    public Activity getBaseActivity(){
        return mBaseActivity;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewIsDestroy = true;
        mIsOnFront = false;
    }

    public void addFragment(Fragment f, int layoutID){
        if (mBaseActivity != null)mBaseActivity.addFragment(f,layoutID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public abstract View onCreateViewBySelf(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 是否允许左滑手势
     * **/
    protected boolean isAllowFling(){
        return false;
    }

    protected void keyBoardStateChange(boolean isShow){}

    protected STHandler mHandler;
    public Handler getHandler(){
        return mHandler;
    }

    protected boolean handleMessage(Message msg){
        return false;
    }

    public void runOnUiThread(Runnable run){
        if (isAdded()){
            getActivity().runOnUiThread(run);
        }
    }

    public Message obtainMessage(int what){
        if (mHandler != null)
            return mHandler.obtainMessage(what);
        else
            return Message.obtain(mHandler,what);
    }

    public Message obtainMessage(int what, Object obj){
        if (mHandler != null)
            return mHandler.obtainMessage(what,obj);
        else
            return Message.obtain(getHandler(),what,obj);
    }

    public Message obtainMessage(int what, int arg1, int arg2, Object obj){
        if (mHandler != null)
            return mHandler.obtainMessage(what,arg1,arg2,obj);
        else
            return Message.obtain(getHandler(),what,arg1,arg2,obj);
    }

    public void sendEmptyMessage(int what){
        if (mHandler != null)
            mHandler.sendEmptyMessage(what);
    }

    public void sendMessage(Message msg){
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    public void post(Runnable runnable){
        if (mHandler != null)
            mHandler.post(runnable);
    }

    /**
     * 该 内部内对handle事件进行一些公共处理可以进行重载处理，如若处理返回为true ，改类将不会处理
     * 且当activity进入后台，该handler也将不会进行预处理
     * **/
    private static class STHandler extends Handler {

        WeakReference<BaseFragment> mWeakRef;
        //        STProcessDialog mProgressDialog;
        STProcessDialog mProgressDialog;
        public STHandler(BaseFragment amf){
            mWeakRef = new WeakReference<BaseFragment>(amf);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final BaseFragment amf = mWeakRef.get();
            if (amf != null && amf.getActivity()!=null && !amf.getActivity().isFinishing()){
                boolean state = amf.handleMessage(msg);
                if (state)
                    return;
                switch (msg.what){
                    case Utility.UI_UIProcessDialog:
                    {
                        if (mProgressDialog == null){
                            mProgressDialog = new STProcessDialog(amf.getActivity());
//                            mProgressDialog = new STProcessDialog(amf.getActivity());
                            mProgressDialog.setCanceledOnTouchOutside(false);
                        }
                        if (mProgressDialog.isShowing()){
                            if (msg.obj != null){
                                mProgressDialog.setMessage((String)msg.obj);
                            }else {
                                mProgressDialog.dismiss();
                            }
                        }else {
                            mProgressDialog.setMessage((String)msg.obj);
                            mProgressDialog.show();
                        }
//                    Utility.e(getClass(),"UI_UIProcessDialog-----");
                    }
                    break;
                    case Utility.UI_UIProcessToast:{
                        Toast.makeText(amf.getActivity(), (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
                    case Utility.UI_UIDismiss_ProcessDialog:{
                        if (mProgressDialog != null && mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                    }
                    break;
                    case Utility.UI_Default_Dialog:
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(amf.getActivity());
                        builder.setTitle("提示");
                        builder.setMessage((String)msg.obj);
                        builder.setCancelable(false);
                        builder.setPositiveButton("确定",null);
                        builder.create().show();
                    }
                    break;
                }
            }
        }
    }

}