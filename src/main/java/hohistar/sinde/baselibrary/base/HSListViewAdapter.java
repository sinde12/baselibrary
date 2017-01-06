package hohistar.sinde.baselibrary.base;

import android.content.Context;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.Map;

import hohistar.sinde.baselibrary.R;
import hohistar.sinde.baselibrary.utility.FindById;

/**
 *Created by sinde on 15/5/22.
 */
public abstract class HSListViewAdapter<T,K> extends BaseAdapter {

    protected List<T> mData;

    protected Context mContext;

    protected Map<K,List<T>> mNodeData;

    protected LayoutInflater mInflater;

    protected Holder mPHolder;
    private LruCache<Integer,Long> mRefreshCache = new LruCache<>(50);

    public HSListViewAdapter(Context c, List<T> data){
        this.mContext = c;
        this.mData = data;
        mInflater = LayoutInflater.from(c);
    }

    public HSListViewAdapter(Context c, Map<K,List<T>> data){
        this.mContext = c;
        this.mNodeData = data;
        mInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        if (mData != null || mNodeData != null){
            if (mData == null){
                int count = 0;
                for(K k: mNodeData.keySet()){
                    List<T> v = mNodeData.get(k);
                    count += (v.size()+1);
                }
                return count;
            }
            return mData.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mData == null){
            int index = position;
            for (K k:mNodeData.keySet()){
                List<T> v = mNodeData.get(k);
                if (index == 0){
                    return k;
                }else{
                    index--;
                    if (index >= v.size()){
                        index = index - v.size();
                    }else{
                        return v.get(index);
                    }
                }
            }
            return null;
        }
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return mData==null?2:1;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = getItem(position);
        if (obj instanceof String) {
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        long now = System.currentTimeMillis();
        Long time = mRefreshCache.get(position);
        if (time!= null && now-time<50){
            if (convertView != null){
                return convertView;
            }
        }
        mRefreshCache.put(position,now);
        convertView = getBindView(position, convertView, parent);
        return convertView;
    }

    public abstract View getBindView(int position, View convertView, ViewGroup parent);

    public class Holder{

        public TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8,tv9,tv10;
        public ImageView iv1,iv2;
        public EditText et1;
        public CheckBox cb1;
        public LinearLayout ll1,ll2;
        public Holder(View view){
            tv1 = (TextView)view.findViewById(R.id.tv1);
            tv2 = (TextView)view.findViewById(R.id.tv2);
            tv3 = (TextView)view.findViewById(R.id.tv3);
            tv4 = (TextView)view.findViewById(R.id.tv4);
            tv5 = (TextView)view.findViewById(R.id.tv5);
            tv6 = (TextView)view.findViewById(R.id.tv6);
            tv7 = (TextView)view.findViewById(R.id.tv7);
            tv8 = (TextView)view.findViewById(R.id.tv8);
            tv9 = (TextView)view.findViewById(R.id.tv9);
            tv10 = (TextView)view.findViewById(R.id.tv10);
            iv1 = (ImageView) view.findViewById(R.id.iv1);
            iv2 = (ImageView) view.findViewById(R.id.iv2);
            et1 = (EditText) view.findViewById(R.id.et1);
            cb1 = (CheckBox) view.findViewById(R.id.cb1);
            ll1 = (LinearLayout) view.findViewById(R.id.ll1);
            ll2 = (LinearLayout) view.findViewById(R.id.ll2);
            view.setTag(this);
        }
    }

    public void setData(List<T> data){
        this.mData = data;
    }

    public void setData(Map<K,List<T>> data){
        this.mNodeData = data;
    }

    public List<T> getData(){
        return mData;
    }

    public Map<K,List<T>> getNodeData(){
        return mNodeData;
    }

}
