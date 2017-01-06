package hohistar.sinde.baselibrary.utility;

/**
 * Created by sinde on 16/3/8.
 */
public abstract class HttpManager {

    HttpManager(String logDir){
        this.mLogDir = logDir;
    }

    private static HttpManager mInstance = null;
    protected String mLogDir;

    public static HttpManager get(String logDir){
        if (mInstance == null){
            synchronized (HttpManager.class){
                if (mInstance == null)
                    mInstance = new HttpManagerDelegate(logDir);
            }
        }
        return mInstance;
    }

    public abstract IHttpResponse execute(IHttpRequest request);

    public abstract void clear();

}
