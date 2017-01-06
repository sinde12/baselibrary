package hohistar.sinde.baselibrary.utility;

import android.text.TextUtils;

import java.io.File;
import java.util.Map;

/**
 * Created by sinde on 16/3/8.
 */
public abstract class IHttpRequest {

    protected File mLogDir = null;

    public Map<String,Object> setParameter(String key, Object value){
        throw new RuntimeException("Sub!");
    }

    public abstract void setConnectTimeout(int timeout);

    public abstract void setReadTimeout(int timeout);

    public abstract int getConnectTimeout();

    public abstract int getReadTimeout();

    public abstract boolean abort();

    public abstract void setPostData(String data);

    public abstract void setPostFile(File file);

    public abstract void addHead(String key, String value);

    public abstract void setRequestMethod(String method);

    public void setLogDir(String dir){
        if (!TextUtils.isEmpty(dir)){
            mLogDir = new File(dir);
            if (!mLogDir.exists()){
                if(!mLogDir.mkdirs()){
                    mLogDir = null;
                }
            }
        }
    }

}
