package hohistar.sinde.baselibrary.utility;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sinde on 15/11/8.
 */
public class Utility_Android {

    /**
     * 获取系统版本号
     **/
    public static String getVersionCode(Context c) {
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);

            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Map<String, String> getDeviceInfo() {
        Map<String, String> info = new LinkedHashMap<String, String>();
        String deviceType = android.os.Build.MODEL;
        @SuppressWarnings("deprecation")
        String sdk = android.os.Build.VERSION.SDK;
        String release = android.os.Build.VERSION.RELEASE;
        info.put("Device Type", deviceType);
        info.put("SDK Version", sdk);
        info.put("android System", release);

        return info;
    }

    public static String packageName(Context context) {
        return context.getPackageName();
    }

    // 通过pid获取app包名
    // String callerPackage = getAppNameByPID(getContext(), Binder.getCallingPid());
    private static String getAppName(Context context, int pID) {

        String processName = null;
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }

    /**
     * permission android.permission.READ_PHONE_STATE
     **/
    public static String getTelephonID(Context context) {
        TelephonyManager telephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getDeviceId();
    }

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * permission android.permission.ACCESS_WIFI_STATE
     **/
    public static String getWlanMAC(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.getConnectionInfo().getMacAddress();
    }

    /**
     * permission android.permission.BLUETOOTH
     **/
    public static String getBluetoothMAC(Context context) {
        BluetoothAdapter m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return m_BluetoothAdapter.getAddress();
    }

    public static String getDeviceID(Context context) {
        String deviceID = getTelephonID(context);
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = getAndroidID(context);
            if (TextUtils.isEmpty(deviceID)) {
                deviceID = getWlanMAC(context);
                if (TextUtils.isEmpty(deviceID)) {
                    deviceID = getBluetoothMAC(context);
                }
            }
        }
        return deviceID;
    }
}
