package hohistar.sinde.baselibrary.utility;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sinde on 16/9/13.
 */
public class ViewInject {

    public static void inject(Activity c){
        viewInject(c,new ViewFinder(c));
    }

    public static void inject(Object handler, View view){
        viewInject(handler,new ViewFinder(handler,view));
    }

    static void viewInject(Object handler,ViewFinder viewFinder){
        Field[] fields = handler.getClass().getDeclaredFields();
        int length = fields.length;
        for (int i =0;i<length;i++){
            Field field = fields[i];
            if (field.isAnnotationPresent(FindById.class)){
                FindById id = field.getAnnotation(FindById.class);
                View view = viewFinder.findViewById(id.id());
                if (view == null){
                    Utility.e(handler.getClass(),id.id()+" find fail!"); continue;};
                field.setAccessible(true);
                try {
                    field.set(handler,view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        Method[] methods = handler.getClass().getDeclaredMethods();
        int mLength = methods.length;
        for (Method m:methods){
            if (m.isAnnotationPresent(OnClick.class)){
                OnClick onClick = m.getAnnotation(OnClick.class);
                int [] ids = onClick.ids();
                for (int id: ids){
                    viewFinder.setOnClickListener(id,m);
                }
            }else if (m.isAnnotationPresent(OnItemClick.class)){
                OnItemClick click = m.getAnnotation(OnItemClick.class);
                int [] ids = click.ids();
                for (int id: ids){
                    viewFinder.setOnItemClickListener(id,m);
                }
            }
        }
    }

    private static class ViewFinder{

        private Activity mActivity;
        private Object mHandler;
        private View mView;
        private View.OnClickListener mOnClickListener = null;
        private AdapterView.OnItemClickListener mOnItemClickListener = null;

        ViewFinder(Activity activity){
            this.mActivity = activity;
        }

        ViewFinder(Object handler,View view){
            mView = view;
            mHandler = handler;
        }

        View findViewById(int id){
            if (mActivity != null){
                return mActivity.findViewById(id);
            }
            if (mView != null){
                return mView.findViewById(id);
            }
            return null;
        }

        void setOnClickListener(int id,Method m){
            if (mOnClickListener == null)mOnClickListener = new VOnClickListener(m);
            findViewById(id).setOnClickListener(mOnClickListener);
        }

        void setOnItemClickListener(int id,Method m){
            if (mOnItemClickListener == null)mOnItemClickListener = new VOnItemClickListener(m);
            ((AdapterView)findViewById(id)).setOnItemClickListener(mOnItemClickListener);
        }

        private class VOnClickListener implements View.OnClickListener{

            private Method mMethod;

            VOnClickListener(Method m){
                mMethod = m;
            }

            @Override
            public void onClick(View v) {
                if (mMethod != null){
                    mMethod.setAccessible(true);
                    try {
                        if (mActivity != null){
                            mMethod.invoke(mActivity,v);
                        }else {
                            mMethod.invoke(mHandler,v);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        private class VOnItemClickListener implements AdapterView.OnItemClickListener{

            private Method mMethod;

            VOnItemClickListener(Method m){
                mMethod = m;
            }


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMethod != null){
                    mMethod.setAccessible(true);
                    try {
                        if (mActivity != null){
                            mMethod.invoke(mActivity,parent,view,position,id);
                        }else {
                            mMethod.invoke(mHandler,parent,view,position,id);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

}
