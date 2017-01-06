package hohistar.sinde.baselibrary.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hohistar.sinde.baselibrary.R;

/**
 * Created by tianbin1 on 2015/4/30.
 */
public class NavigationView extends RelativeLayout {

    NavigationActionListener mActionListener;
    private Bitmap mCurrentBitmap = null;
    private View mAction;

    public NavigationView(Context c){
        super(c);
        initView(c);
    }

    public NavigationView(Context c, AttributeSet attr){
        super(c,attr);
        initView(c);
    }

    public NavigationView(Context c, AttributeSet attr, int defaultStyle){
        super(c,attr,defaultStyle);
        initView(c);
    }

    private ImageView mBackIV,mActionIV;
    private TextView mBackTV,mTitleTV,mSubTitleTV,mActionTV;
    private SegmentedGroup mSegmentedGroup = null;
    private ImageView mBG;
    private void initView(Context c){
        LayoutInflater.from(c).inflate(R.layout.com_sinde_android_navigation_view,this);
        mBackIV = (ImageView)findViewById(R.id.com_sinde_android_navigation_view_backIV);
        mActionIV = (ImageView)findViewById(R.id.com_sinde_android_navigation_view_actionIV);
        mBackTV = (TextView)findViewById(R.id.com_sinde_android_navigation_view_backTV);
        mTitleTV = (TextView)findViewById(R.id.com_sinde_android_navigation_view_titleIV);
        mSubTitleTV = (TextView)findViewById(R.id.com_sinde_android_navigation_view_subTitleIV);
        mActionTV = (TextView)findViewById(R.id.com_sinde_android_navigation_view_actionTV);
        mBG = (ImageView)findViewById(R.id.com_sinde_android_navigation_viewBG);
        findViewById(R.id.ll1).setOnClickListener(mOnClickListener);
        mAction = findViewById(R.id.com_sinde_android_navigation_view_actionRL);
        mSegmentedGroup = (SegmentedGroup)findViewById(R.id.segment_control);
        mAction.setOnClickListener(mOnClickListener);
        mBackIV.setOnClickListener(mOnClickListener);
//        mActionIV.setOnClickListener(mOnClickListener);
        mBackTV.setOnClickListener(mOnClickListener);
//        mActionTV.setOnClickListener(mOnClickListener);
        setBackgroundColor(Color.BLUE);
    }

    @Override
    public void setBackgroundResource(int resid) {
        mBG.setImageResource(resid);
    }

    public void setActionEnabled(boolean enabled){
        mActionTV.setTextColor(enabled ? Color.WHITE : Color.GRAY);
        mAction.setEnabled(enabled);
    }

    public void update(String title, String action, NavigationActionListener listener){
        update(null, null, title, null, action, listener);
    }

    void update(Drawable backDrawable, String backText, String title, String subTitle, String action, NavigationActionListener listener){
        update(backDrawable, backText, title, subTitle, null, action, listener);
    }

    void update(Drawable backDrawable, String backTxt, String title, String subTitle, Drawable actionDrawable, String action, NavigationActionListener listener){
        if (backDrawable != null){
            mBackIV.setImageDrawable(backDrawable);
        }
        if (backTxt != null){
            mBackTV.setText(backTxt);
            mBackTV.setVisibility(View.VISIBLE);
        }else {
            mBackTV.setVisibility(View.GONE);
        }
        mTitleTV.setText(title);
        if (subTitle != null){
            mSubTitleTV.setText(subTitle);
            mSubTitleTV.setVisibility(View.VISIBLE);
        }else {
            mSubTitleTV.setVisibility(View.GONE);
        }
        if (actionDrawable != null){
            mActionIV.setImageDrawable(actionDrawable);
            mActionTV.setVisibility(View.GONE);
            mActionIV.setVisibility(View.VISIBLE);
        }else {
            if (action == null){
                mActionTV.setVisibility(View.GONE);
            }else {
                mActionTV.setVisibility(View.VISIBLE);
            }
            mActionTV.setText(action);
            mActionIV.setVisibility(View.GONE);
        }
        this.mActionListener = listener;
    }

    public void changeTitle(String title){
        mTitleTV.setText(title);
    }

    public String getTitle(){
        return mTitleTV.getText().toString();
    }

    public String getAction(){
        return mActionTV.getText().toString();
    }

    public Bitmap getActionBitmap(){
        if (mCurrentBitmap == null){
            mCurrentBitmap = drawableToBitmap(mActionIV.getDrawable());
        }
        return mCurrentBitmap;
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        if (drawable == null)return null;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

//    @Deprecated
//    public void changeAction(String action, Drawable ad){
//        if (ad != null){
//            mActionIV.setImageDrawable(ad);
//            mActionTV.setVisibility(View.GONE);
//            mActionIV.setVisibility(View.VISIBLE);
//        }else {
//            if (action == null){
//                mActionTV.setVisibility(View.GONE);
//            }else {
//                mActionTV.setVisibility(View.VISIBLE);
//                mActionTV.setText(action);
//            }
//            mActionIV.setVisibility(View.GONE);
//        }
//    }

    public TextView getTitleView(){
        return mTitleTV;
    }

    public TextView getActionView(){
        return mActionTV;
    }

    public void changeAction(String action, Bitmap ad){
        if (ad != null){
            mActionIV.setImageBitmap(ad);
            mActionTV.setVisibility(View.GONE);
            mActionIV.setVisibility(View.VISIBLE);
        }else {
            if (action == null){
                mActionTV.setVisibility(View.GONE);
            }else {
                mActionTV.setVisibility(View.VISIBLE);
                mActionTV.setText(action);
            }
            mActionIV.setVisibility(View.GONE);
        }
    }

    public void setBackVisibility(int visibility){
        mBackTV.setVisibility(visibility);
        mBackIV.setVisibility(visibility);
    }

    public void changeBackText(String back){
        mBackTV.setText(back);
        mBackTV.setVisibility(VISIBLE);
    }

    public void changeBackTextColor(int color){
        mBackTV.setTextColor(color);
        mBackTV.setVisibility(VISIBLE);
    }

    public void changeTitleSize(float size){
        mTitleTV.setTextSize(size);
    }

    public SegmentedGroup getSegmentedGroup(){
        mTitleTV.setVisibility(GONE);
        mSegmentedGroup.setVisibility(VISIBLE);
        return mSegmentedGroup;
    }

    public void changeTitleColor(int color){
        mTitleTV.setTextColor(color);
    }

//    @Deprecated
//    public void setAction(String action, Drawable actionDrawable){
//        if (actionDrawable != null){
//            mActionIV.setImageDrawable(actionDrawable);
//            mActionTV.setVisibility(View.GONE);
//            mActionIV.setVisibility(View.VISIBLE);
//        }else {
//            if (action == null){
//                mActionTV.setVisibility(View.GONE);
//            }else {
//                mActionTV.setText(action);
//                mActionTV.setVisibility(View.VISIBLE);
//            }
//            mActionIV.setVisibility(View.GONE);
//        }
//    }

    public void setAction(String action, Bitmap actionBitmap){
        if (actionBitmap != null){
            mCurrentBitmap = actionBitmap;
            mActionIV.setImageBitmap(actionBitmap);
            mActionTV.setVisibility(View.GONE);
            mActionIV.setVisibility(View.VISIBLE);
        }else {
            if (action == null){
                mActionTV.setVisibility(View.GONE);
            }else {
                mActionTV.setText(action);
                mActionTV.setVisibility(View.VISIBLE);
            }
            mActionIV.setVisibility(View.GONE);
        }
    }

    public void setActionColor(int color){
        mActionTV.setTextColor(color);
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ll1){
                if (mActionListener != null)
                    mActionListener.back();
            }else if (v.getId() == R.id.com_sinde_android_navigation_view_backIV){
                if (mActionListener != null)
                    mActionListener.back();
            }else if (v.getId() == R.id.com_sinde_android_navigation_view_backTV){
                if (mActionListener != null)
                    mActionListener.back();
            }else if (v.getId() == R.id.com_sinde_android_navigation_view_actionIV){
                if (mActionListener != null && (mActionTV.getVisibility() == VISIBLE || mActionIV.getVisibility() == VISIBLE)){
                    mActionListener.action();
                }
            }else if (v.getId() == R.id.com_sinde_android_navigation_view_actionTV){
                if (mActionListener != null && (mActionTV.getVisibility() == VISIBLE || mActionIV.getVisibility() == VISIBLE)){
                    mActionListener.action();
                }
            }else if (v.getId() == R.id.com_sinde_android_navigation_view_actionRL){
                if (mActionListener != null && (mActionTV.getVisibility() == VISIBLE || mActionIV.getVisibility() == VISIBLE)){
                    mActionListener.action();
                }
            }
        }
    };

    public interface NavigationActionListener{
        boolean back();
        void action();
    }

}
