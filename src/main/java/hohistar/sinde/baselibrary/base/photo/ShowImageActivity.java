package hohistar.sinde.baselibrary.base.photo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hohistar.sinde.baselibrary.R;
import hohistar.sinde.baselibrary.base.BaseActivity;
import hohistar.sinde.baselibrary.utility.STImageLoader;

/**
 *
 */
public class ShowImageActivity extends BaseActivity {

    private static final int UPDATE_INDICATOR = 200;
    STImageLoader imageLoader = STImageLoader.getInstance();

    public static final String CURRENT_POSITION = "current_position";
    public static final String PHOTO_BEAN_LIST = "photo_bean_list";
    public static final String IS_ALI_CLOUD_KEY = "is_ali_cloud_key";
    public static final String IS_ONLINE = "is_online";

    private boolean mIsOnline;
    private boolean isAliCloudKey;
    private TextView mIndicatorTv;
    private int mCurrentPosition;
    private ArrayList<PhotoParcelable> mData;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_activity);
        mIsOnline = false;
        mPager = (ViewPager)findViewById(R.id.pager);
        findViewById(R.id.iv_show_image_del).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 删除图片
                        mData.remove(mCurrentPosition);
                        if (mCurrentPosition != 0) {
                            mCurrentPosition--;
                        }
                        mPager.setAdapter(new ViewPagerAdapter(mData));
                        mPager.setCurrentItem(mCurrentPosition);
                        if (mData.size() == 0) {
                            Intent intent = new Intent();
                            intent.putParcelableArrayListExtra(PHOTO_BEAN_LIST, mData);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        getHandler().sendEmptyMessage(UPDATE_INDICATOR);
                    }
                });
            }
        });
        mIndicatorTv = (TextView)findViewById(R.id.tv_show_image_indicator);
        findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onInterceptOnBack();
            }
        });

        Intent intent = getIntent();
        mData = intent.getParcelableArrayListExtra(PHOTO_BEAN_LIST);
        mCurrentPosition = intent.getIntExtra(CURRENT_POSITION, 0);
        isAliCloudKey = intent.getBooleanExtra(IS_ALI_CLOUD_KEY, false);
        mIsOnline = intent.getBooleanExtra(IS_ONLINE, mIsOnline);
        getHandler().sendEmptyMessage(UPDATE_INDICATOR);

        ViewPagerAdapter adapter = new ViewPagerAdapter(mData);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(mCurrentPosition);
        mPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                getHandler().sendEmptyMessage(UPDATE_INDICATOR);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected boolean handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_INDICATOR:
                mIndicatorTv.setText(mCurrentPosition + 1 + "/" + mData.size());
                break;
        }
        return super.handleMessage(msg);
    }

    class ViewPagerAdapter extends PagerAdapter {
        private List<PhotoParcelable> mData;

        public ViewPagerAdapter(List<PhotoParcelable> data) {
            mData = data;
        }

        public void setData(List<PhotoParcelable> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final PhotoView view = new PhotoView(ShowImageActivity.this);
            view.setScaleType(ScaleType.FIT_XY);
            view.setAutoScale();
            view.enable();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onInterceptOnBack();
                        }
                    });
                }
            });

            PhotoParcelable photoBean = mData.get(position);

//            view.setImageResource(R.mipmap.icon_default); // 设置默认图片

            if (mIsOnline) {
                if (TextUtils.isEmpty(photoBean.cloudKey)){
//                    ToastUtils.show(ShowImageActivity.this, "该图片无法查看");
                } else {
                    if (isAliCloudKey) {
                        imageLoader.displayImage(photoBean.cloudKey,view,4);
                    } else {
//                        String url = Utility_SP.downALYPhoto(AliyunOSSUtil.getHCloudKey(photoBean.cloudKey));
//                        imageLoader.displayImage(url, view);
                    }
                }
            } else {
                // 根据图片名查找本地图片 hdpi 图片
                String path = photoBean.filePath;
                //                    String path = SysApplication.PATH_IMG + File.separator + fileName;
                if (new File(path).exists()) {
                    // 本地加载
//                    imageLoader.displayImage("file://" + path, view);
                    STImageLoader.getInstance().displayImage(path,view,4);
                } else {
//                    ToastUtils.show(ShowImageActivity.this, "该图片无法查看");
                }
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected boolean onInterceptOnBack() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(PHOTO_BEAN_LIST, mData);
        setResult(RESULT_OK, intent);
        return true;
    }

}
