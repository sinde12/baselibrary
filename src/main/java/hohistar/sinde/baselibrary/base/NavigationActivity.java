package hohistar.sinde.baselibrary.base;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

import hohistar.sinde.baselibrary.R;

/**
 * Created by sinde on 16/5/16.
 */
public class NavigationActivity extends BaseActivity {

    private HashMap<Integer,String> mActionText = new HashMap<Integer,String>();
    private HashMap<Integer,Bitmap> mActionDrawable = new HashMap<Integer,Bitmap>();
    protected ArrayList<String> mFragmentTitle = new ArrayList<String>();

    public NavigationView mNavigationView;
    TextView mNotificationView;

    @Override
    protected void setContentViewBefore(View view) {
        super.setContentViewBefore(view);
        getWindow().setWindowAnimations(R.style.notAnimation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mNavigationView = new NavigationView(this);
            View contentView = mView.getChildAt(0);
            mView.removeView(contentView);
            LinearLayout ll  = new LinearLayout(this);
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
            LinearLayout.LayoutParams nvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) (getResources().getDisplayMetrics().density*49));
            mNavigationView.setLayoutParams(nvParams);
            mNotificationView = new TextView(this);
            ViewGroup.LayoutParams tv = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getStatusBarHeight(this));
            mNotificationView.setLayoutParams(tv);
            ll.setLayoutParams(params);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(mNotificationView);
            ll.addView(mNavigationView);
            ll.addView(contentView);
            mView.addView(ll);
            changeNotificationTitleView(mNotificationView);
        }else {
            mNavigationView = new NavigationView(this);
            View contentView = mView.getChildAt(0);
            mView.removeView(contentView);
            LinearLayout ll = new LinearLayout(this);
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            LinearLayout.LayoutParams nvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) (getResources().getDisplayMetrics().density*49));
            mNavigationView.setLayoutParams(nvParams);
            ll.setLayoutParams(params);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(mNavigationView);
            ll.addView(contentView);
            mView.addView(ll);
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mNavigationView.update("","",mListener);
        if (mIsFirstInit){
            mIsFirstInit = false;
            initNavigationBar(mNavigationView);
        }
    }

    private boolean mIsFirstInit = true;
    @Override
    protected void onStart() {
        super.onStart();
    }

    public NavigationView getNavigationView(){
        return mNavigationView;
    }

    protected void initNavigationBar(NavigationView nv){}

    protected void changeNavigationView(final NavigationView nv){
        Fragment f = getCurrentFragment();
        if (f != null && f instanceof STFragment){
            STFragment sf = (STFragment)f;
            sf.changeNavigationView(nv);
            if (nv.getTitle() != null)
                mFragmentTitle.add(nv.getTitle());
            if (nv.getAction() != null){
                mActionText.put(mStack.size()-1,nv.getAction());
            }
            if (nv.getActionBitmap() != null){
                mActionDrawable.put(mStack.size()-1,nv.getActionBitmap());
            }
        }else {
            if (nv.getTitle() != null)
                mFragmentTitle.add(nv.getTitle());
            if (nv.getAction() != null){
                mActionText.put(mStack.size()-1,nv.getAction());
            }
            if (nv.getActionBitmap() != null){
                mActionDrawable.put(mStack.size()-1,nv.getActionBitmap());
            }
        }
    }
    private void changeNavigationBar(String title, String action, Bitmap actionBitmap) {
        mNavigationView.changeTitle(title);
        mNavigationView.changeAction(action,actionBitmap);
    }



    /**
     * 重载父类 onBaseBack 函数，进一步对该运行单元进行分发
     * **/
    @Override
    protected boolean onBack() {
        boolean tag;
        if (getCurrentFragment() != null && getCurrentFragment() instanceof STFragment){
            STFragment f = (STFragment)getCurrentFragment();
            tag = f.onBack();
            if (tag)
                return tag;
        }
        tag = super.onBack();
        if (tag){
            String title = mFragmentTitle.get(mFragmentTitle.size()-2);
            mFragmentTitle.remove(mFragmentTitle.size() - 1);
            String action = mActionText.get(mStack.size()-1);
            Bitmap ad = mActionDrawable.get(mStack.size()-1);
            mActionText.remove(mStack.size());
            mActionDrawable.remove(mStack.size());
            changeNavigationBar(title, action, ad);
            Fragment f = getCurrentFragment();
            while (true){
                if (f.getActivity()!=null){
                    ((STFragment)f).onRefresh();
                    break;
                }
            }
        }else {
            onBaseBack(true);
            tag = true;
        }
        return tag;
    }

    private synchronized void onBaseBack(boolean needAnimation){
        if (needAnimation){
            TranslateAnimation animation = new TranslateAnimation(0,getResources().getDisplayMetrics().widthPixels,0,0);
            animation.setDuration(300);
            AccelerateInterpolator interpolator = new AccelerateInterpolator();
            animation.setInterpolator(interpolator);
            mView.startAnimation(animation);
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },300);
        }
    }


    protected void onAction(){
        Fragment f = getCurrentFragment();
        if (f != null && f instanceof STFragment){
            final STFragment sf = (STFragment)f;
            sf.onAction();
        }
    }

    protected boolean isAllowFillingBack(){
        return true;
    }

    /**
     * 当系统版本高于API 19 时 调用，否则默认不调用
     * **/
    protected void changeNotificationTitleView(View v){
    }

    final NavigationView.NavigationActionListener mListener = new NavigationView.NavigationActionListener() {
        @Override
        public boolean back() {
            dispatchOnBack();
            return false;
        }

        @Override
        public void action() {
            onAction();
        }
    };

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out);
    }

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
        super.startActivityFromFragment(fragment, intent, requestCode, options);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private enum State{
        prepare,start,cancel,end,
    }

    private int mStartX;
    private State mState = State.cancel;
    private float mTempX,mTempX2;
    private float mTempX3 = 0;//当向右滑动一段距离后，在向左滑时，记录本次左滑开始的值

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                float sx = ev.getX();
                if (mState == State.cancel && sx/getResources().getDisplayMetrics().widthPixels<0.10f && isAllowFillingBack()){
                    mStartX = (int)sx;
                    mState = State.prepare;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:{
                if (mState == State.prepare){
                    float sx = ev.getX();
                    if ((sx-mStartX)/getResources().getDisplayMetrics().widthPixels>0.1){
                        mState = State.start;
                        mTempX = sx;//记录正式滑动的位置
                    }
                    mTempX2 = sx;
                }else if (mState == State.start){
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
                    float sx = ev.getX();
                    if (sx<mTempX){
                        mState = State.end;
                        TranslateAnimation animation = new TranslateAnimation(mStartX,0,0,0);
                        animation.setDuration(100);
                        mView.startAnimation(animation);
                        params.setMargins(0,0,0,0);
                        mView.setLayoutParams(params);
                    }else {
                        float distance = sx - mTempX2;
                        if (distance<0){
                            if (mTempX3 == -1) mTempX3 = sx;
                        }else {
                            mTempX3 = -1;
                        }
                        params.setMargins((int)(sx-mStartX),0,(int)(mStartX-sx),0);
                        mView.setLayoutParams(params);
                    }
                    mTempX2 = sx;
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
                mState = State.cancel;
                mTempX3 = -1;
                break;
            case MotionEvent.ACTION_UP:{
                if (mState == State.start){
                    float sx = ev.getX();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)mView.getLayoutParams();
                    int width = getResources().getDisplayMetrics().widthPixels;
                    if (mTempX3 != -1 && (sx-mTempX3)<-30){
                        TranslateAnimation animation = new TranslateAnimation(layoutParams.leftMargin,0,0,0);
                        animation.setDuration(300);
                        mView.startAnimation(animation);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
                        params.setMargins(0,0,0,0);
                        mView.setLayoutParams(params);
                    }else {
                        TranslateAnimation animation = new TranslateAnimation(sx-width,0,0,0);
                        animation.setDuration(300);
                        AccelerateInterpolator interpolator = new AccelerateInterpolator();
                        animation.setInterpolator(interpolator);
                        mView.startAnimation(animation);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
                        params.setMargins(getResources().getDisplayMetrics().widthPixels,0,-getResources().getDisplayMetrics().widthPixels,0);
                        mView.setLayoutParams(params);
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },250);
                    }
                }
                mState = State.cancel;
                mTempX3 = -1;
            }
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

}
