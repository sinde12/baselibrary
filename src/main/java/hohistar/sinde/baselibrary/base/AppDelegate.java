package hohistar.sinde.baselibrary.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

import hohistar.sinde.baselibrary.utility.Utility_File;

/**
 * Created by tianbin1 on 2015/4/30.
 */
public class AppDelegate extends Application {

    public static final String IsFirstLauncher = "IsFirstLauncher";

    /**package**/
    List<Activity> mHistoryRecord = new ArrayList<Activity>();
    private static Context mInstance = null;
    public static String TAG = "AppDelegate";
    public static String PATH_CACHE;
    public static String PATH_MAIN;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        TAG = getClass().getSimpleName();
        PATH_MAIN = Environment.getExternalStorageDirectory()+"/"+getApplicationInfo().packageName;
        PATH_CACHE = PATH_MAIN+"/cache";
        Utility_File.createFiles(PATH_CACHE);
        CrashHandler.getInstance().init(this,PATH_MAIN);
    }

    public static Context getInstance(){
        return mInstance;
    }

    @Override
    public void onTerminate() {
        for (Activity a: mHistoryRecord){
            a.finish();
        }
        super.onTerminate();
    }

    void addActivityRecord(Activity a){
        mHistoryRecord.add(a);
    }

    void removeActivityRecord(Activity a){
        mHistoryRecord.remove(a);
    }

    public void popToMainActivity(){
        for (int i=1;i<mHistoryRecord.size();i++){
            mHistoryRecord.get(i).finish();
        }
    }

    public void finishAllActivity(){
        for (Activity a: mHistoryRecord){
            a.finish();
        }
    }

    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }
}
