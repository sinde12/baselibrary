package hohistar.sinde.baselibrary.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import hohistar.sinde.baselibrary.R;
import hohistar.sinde.baselibrary.utility.Parameter;
import hohistar.sinde.baselibrary.utility.PerformAfterOnCreate;
import hohistar.sinde.baselibrary.utility.Utility;
import hohistar.sinde.baselibrary.utility.Utility_File;

/**
 * Created by sinde on 16/4/1.
 */
public class BaseActivity extends Activity {

    protected final int MAX_SPEED = 8000;
    protected final int MIDDLE_SPEED = 3000;
    protected final int SMALL_SPEED = 1000;
    protected final int MIDDLE_DISTANCE = 200;
    protected final int SMALL_DISTANCE = 100;

    private final String SAVE_BY_ME = "SAVE_BY_ME";
    List<Fragment> mStack = new ArrayList<Fragment>();//fragment 栈
    private STHandler mHandler;
    boolean mIsStop = false;//判断当前activity是否进入后台
    RelativeLayout mView = null;//父容器
    private InputMethodManager manager;
    int mMaximumFlingVelocity;
    float mDensity = 1f;//屏幕密度
    protected float mScreenWidth = 0;
    protected float mScreenHeight = 0;
    int mMinMoveDistance = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configWindow();
        Application app = getApplication();
        if (savedInstanceState == null) {
            if (app instanceof AppDelegate)
                ((AppDelegate) app).addActivityRecord(this);
        }else {
            recoveryDataFromBundle(savedInstanceState);
        }
        mHandler = new STHandler(this);
        mMaximumFlingVelocity = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();
        mDensity = getResources().getDisplayMetrics().density;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mMinMoveDistance = (int)(getResources().getDisplayMetrics().density*60);
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID,null);
        setContentView(view);
    }

    /**
     * 获取当前界面的view UI 快照。
     * **/
    public Bitmap getCurrentViewCache(){
        mView.setDrawingCacheEnabled(true);
        mView.buildDrawingCache();
        Bitmap cache = mView.getDrawingCache();
        Bitmap bitmap = null;
        if (cache != null){
            bitmap = Bitmap.createBitmap(mView.getDrawingCache());
        }
        mView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 将我们的父根 view 实例化且作为根视图
     * **/
    @Override
    public void setContentView(View view) {
        mView = new RelativeLayout(this);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        mView.setLayoutParams(lp);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp2);
        mView.setBackgroundColor(Color.WHITE);
        mView.addView(view);
        setContentViewBefore(mView);
        super.setContentView(mView);
        manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        mView.setOnClickListener(mHelpOnClickListener);
    }

    protected void setContentViewBefore(View view){}

    private boolean INIT = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (!INIT){
            INIT = true;
            performInitMethodIfNeed();
        }
        mIsStop = false;
        mIsActivityPause = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveIfNeed(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void performInitMethodIfNeed(){
        Method[] methods = getClass().getDeclaredMethods();
        if (methods != null && methods.length>0){
            for (Method m:methods){
                if (m.isAnnotationPresent(PerformAfterOnCreate.class)){
                    Annotation[][] parameters = m.getParameterAnnotations();
                    List<String> ps = new ArrayList<>();
                    for (Annotation[] annotations: parameters){
                       for (Annotation animation:annotations){
                            if (animation.annotationType().equals(Parameter.class)){
                                Parameter p = (Parameter)animation;
                                if (p.values().length>0){
                                    ps.add(p.values()[0]);
                                }else {
                                    ps.add("");
                                }
                            }
                       }
                    }
                    m.setAccessible(true);
                    int l = m.getParameterTypes().length;
                    try {
                        if (l==0)m.invoke(this);
                        else if (l==1)m.invoke(this,ps.get(0));
                        else if (l==2)m.invoke(this,ps.get(0),ps.get(1));
                        else if (l==3)m.invoke(this,ps.get(0),ps.get(1),ps.get(2));
                        else if (l==4)m.invoke(this,ps.get(0),ps.get(1),ps.get(2),ps.get(3));
                        else if (l==5)m.invoke(this,ps.get(0),ps.get(1),ps.get(2),ps.get(3),ps.get(4));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 配置窗体 如全屏等，改方法将在setContentView 之前调用
     * **/
    protected void configWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//通知栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//navigationbar，以及华为下面的软件
        }
    }

    /**
     * @see FragmentTransaction replace
     * 改方法会进行自动压栈 操作
     * */
    public void repleaceFragment(Fragment f, int layoutID){
        FragmentTransaction fts = getFragmentManager().beginTransaction();
        fts.addToBackStack(null);
        fts.replace(layoutID, f);
        fts.commitAllowingStateLoss();
        mStack.clear();
        mStack.add(f);
    }

    /**
     * @see FragmentTransaction replace
     * 改方法会进行自动压栈 操作
     * */
    public void repleaceFragment(Fragment f, int layoutID, String tag){
        FragmentTransaction fts = getFragmentManager().beginTransaction();
        fts.replace(layoutID, f,tag);
        fts.commitAllowingStateLoss();
        mStack.clear();
        mStack.add(f);
    }


    public void addFragment(Fragment f, int layoutID){
        FragmentTransaction fts = getFragmentManager().beginTransaction();
//        fts.setCustomAnimations(R.animator.fragment_lef_enter,R.animator.fragment_left_exit,R.animator.fragment_pop_left_enter,R.animator.fragment_pop_left_exit);
        fts.addToBackStack(null);
        fts.replace(layoutID, f);
        fts.commitAllowingStateLoss();
        if (f instanceof BaseFragment){
            ((BaseFragment)f).setBaseActivity(this);
        }
        mStack.add(f);
    }

    public void addFragment(Fragment f, int layoutID, String tag){
        FragmentTransaction fts = getFragmentManager().beginTransaction();
//        fts.setCustomAnimations(R.animator.fragment_lef_enter,R.animator.fragment_left_exit,R.animator.fragment_pop_left_enter,R.animator.fragment_pop_left_exit);
        fts.addToBackStack(null);
        fts.replace(layoutID, f,tag);
        fts.commitAllowingStateLoss();
        if (f instanceof BaseFragment){
            ((BaseFragment)f).setBaseActivity(this);
        }
        mStack.add(f);
    }

    /**
     * 获取当前 最上层的 Fragment
     * **/
    public Fragment getCurrentFragment(){
        if (mStack.size()>0){
            return mStack.get(mStack.size()-1);
        }
        return null;
    }

    private Point mLastPoint = new Point();
    public Point getLastPoint(){
        return mLastPoint;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastPoint.x = (int)ev.getX();
                mLastPoint.y = (int)ev.getY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
        mIsStop = true;
    }

    private boolean mIsActivityPause = false;
    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.sendEmptyMessage(Utility.UI_UIDismiss_ProcessDialog);
        Application app = getApplication();
        if (app instanceof AppDelegate) {
            AppDelegate delegate = (AppDelegate)app;
            if (delegate.mHistoryRecord.size()>0){
                String className = delegate.mHistoryRecord.get(delegate.mHistoryRecord.size()-1).getLocalClassName();
                File file = new File(Utility_File.PATH+"/classSave/"+className);
                Utility_File.delete(file);
            }
            ((AppDelegate) app).removeActivityRecord(this);
        }
    }

    public void addView(View child){
        mView.addView(child);
    }

    public void removeView(View child){
        mView.removeView(child);
    }

    public void addHelpView(Bitmap bitmap, String flag){
        SharedPreferences sharedPreferences = getSharedPreferences(AppDelegate.TAG, Context.MODE_PRIVATE);
        boolean f = sharedPreferences.getBoolean(flag, false);
        if (!f){
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageBitmap(bitmap);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            iv.setLayoutParams(params);
            iv.setOnClickListener(mHelpOnClickListener);
            addView(iv);
        }
    }

    private final View.OnClickListener mHelpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(mView)){
                if (mIsKeyBoardShow){
                    hideKeyboard();
                }
            }else {
                mView.removeView(v);
            }
        }
    };

    public boolean mIsKeyBoardShow = false;
    private Rect mContentViewVisibleRect = new Rect();
    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mView.getWindowVisibleDisplayFrame(mContentViewVisibleRect);
            if (getResources().getDisplayMetrics().heightPixels-mContentViewVisibleRect.height()>100){
                if (!mIsKeyBoardShow){
                    mIsKeyBoardShow = true;
                    if (!mIsActivityPause)
                        onKeyBoardStateChange(true,mContentViewVisibleRect.height(),getResources().getDisplayMetrics().heightPixels);
                }
            }else {
                if (mIsKeyBoardShow){
                    mIsKeyBoardShow = false;
                    if (!mIsActivityPause)
                        onKeyBoardStateChange(false,mContentViewVisibleRect.height(),getResources().getDisplayMetrics().heightPixels);
                }
            }
        }
    };

    private void onKeyBoardStateChange(boolean isShow,int visibleHeight,int height){
        if (!handleOnKeyBoardStateChange(isShow,visibleHeight,height)){
            if (isShow){
                Point point = getLastPoint();
                int marginHeight = (int)(80*getResources().getDisplayMetrics().density);
                int y = visibleHeight-point.y;
                if (y<marginHeight){
                    ViewGroup viewGroup = getContentView();
                    viewGroup.scrollTo(0,marginHeight-y);
                }
            }else {
                ViewGroup viewGroup = getContentView();
                viewGroup.scrollTo(0,0);
            }
        }
    }

    protected boolean handleOnKeyBoardStateChange(boolean isShow, int visibleHeight, int height) {
        return true;
    }

    public ViewGroup getContentView(){
        return mView;
    }


    /**
     * 针对内部 @mHandler 发送的消息进行处理
     * @see STHandler
     * **/
    protected boolean handleMessage(Message msg){
        return false;
    }

    /**
     * 针对 KEYCODE_BACK 事件进行预处理，当 当前根视图拥有两个以上的child的时，将先移除最上层的子child
     * 如若没有，将使用 @see onBaseBack() 进行预处理，以前给当前界面单元发送消息
     **/
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (dispatchOnBack()){
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    boolean dispatchOnBack(){
        if (onInterceptOnBack()){
            return true;
        }
        int childCount = mView.getChildCount();
        if (childCount>1){
            View view = mView.getChildAt(mView.getChildCount()-1);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
            view.startAnimation(animation);
            mView.removeView(view);
            hideKeyboard();
            return true;
        }
        return onBack();
    }

    public void onBackAction(){
        dispatchOnBack();
    }

    protected boolean onInterceptOnBack(){
        return false;
    }

    protected boolean onBack(){
        if (mStack.size()>1){
            getFragmentManager().popBackStack();
            Fragment f = mStack.get(mStack.size()-1);
            mStack.remove(f);
            return true;
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    /**
     * 弹出键盘
     * **/
    public void showKeyBoard(View v){
        v.requestFocus();
        manager.showSoftInput(v, 0);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public Message obtainMessage(int what){
        return mHandler.obtainMessage(what);
    }

    public Message obtainMessage(int what, Object obj){
        return mHandler.obtainMessage(what,obj);
    }

    public Message obtainMessage(int what, int arg1, int arg2, Object obj){
        return mHandler.obtainMessage(what,arg1,arg2,obj);
    }

    public boolean sendEmptyMessage(int what){
        return mHandler.sendEmptyMessage(what);
    }

    public boolean sendMessage(Message msg){
        return mHandler.sendMessage(msg);
    }

    public void post(Runnable runnable){
         mHandler.post(runnable);
    }

    public Handler getHandler(){
        return mHandler;
    }



    /**
     * 该 内部内对handle事件进行一些公共处理可以进行重载处理，如若处理返回为true ，改类将不会处理
     * 且当activity进入后台，该handler也将不会进行预处理
     * **/
    private static class STHandler extends Handler {

        WeakReference<BaseActivity> mWeakRef;
        //        ProgressDialog mProgressDialog;
        STProcessDialog mProgressDialog;
        public STHandler(BaseActivity amf){
            mWeakRef = new WeakReference<BaseActivity>(amf);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final BaseActivity amf = mWeakRef.get();
            if (amf == null||amf.isFinishing())return;
            boolean state = amf.handleMessage(msg);
            if (state)
                return;
            if (amf.mIsStop)
                return;
            switch (msg.what){
                case Utility.UI_UIProcessDialog:
                {
                    if (mProgressDialog == null){
//                        mProgressDialog = new ProgressDialog(amf);
                        mProgressDialog = new STProcessDialog(amf);
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
                    Toast.makeText(amf, (String) msg.obj, Toast.LENGTH_LONG).show();
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
                    AlertDialog.Builder builder=new AlertDialog.Builder(amf);
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
