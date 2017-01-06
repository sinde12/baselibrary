package hohistar.sinde.baselibrary.utility;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.location.core.GeoPoint;
import java.io.IOException;
import java.util.List;

/**
 * Created by sinde on 15/7/29.
 */
public final class GPSManager {

    private LocationManagerProxy mProxy;
    private Context mContext;
    private LocationManager mLocationManager = null;

    private GPSManager(Context c) {
        mProxy = LocationManagerProxy.getInstance(c);
        this.mContext = c;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private static GPSManager mInstance;

    public static GPSManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new GPSManager(c);
        }
        return mInstance;
    }

    public void requestLocaion(boolean onlyGPS) {
        tempLat = 0;
        tempLon = 0;
        if (mProxy == null) {
            mProxy = LocationManagerProxy.getInstance(mContext);
        }
        if (onlyGPS) {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return;
            }
            if (locationListener != null) {
//                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);
                //                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        } else {
            mProxy.requestLocationData(LocationProviderProxy.AMapNetwork, 6 * 1000, 50, aMapLocationListener);
        }
        //        Log.e(getClass().getSimpleName(),"start get GPS");
    }

    public void stopLoaction() {
        if (mProxy != null) {
            mProxy.removeUpdates(aMapLocationListener);
            mProxy.destroy();
        }
        mProxy = null;
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        mLocationManager.removeUpdates(locationListener);
        tempLat = 0;
        tempLon = 0;
        //        Log.e(getClass().getSimpleName(),"stop get GPS");
    }

    private IGPSCallback mCallback;
    public void setGPSCallback(IGPSCallback callback){
        this.mCallback = callback;
    }

    private double tempLat = 0;//纬度
    private double tempLon = 0;//经度

    private double lastLat = 0;//纬度
    private double lastLon = 0;//经度

    /**
     * 获取 纬度，当没有定位成功时可能为0
     * **/
    public double getLatitude(){
        return lastLat;
    }

    /**
     * 获取 经度，当没有定位成功时可能为0
     * **/
    public double getLongitude(){
        return lastLon;
    }

    private AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            Utility.w(getClass(),"aMapLocation.getAMapException().getErrorCode() = " + aMapLocation.getAMapException().getErrorCode());
            if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0){
                double lat = aMapLocation.getLatitude();
                double lon = aMapLocation.getLongitude();
                String city = aMapLocation.getCity();
                handleResult(lat,lon,city);
            }
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void handleResult(double lat,double lon,String city){
        double dLat = Math.abs(lat - tempLat);
        double dLon = Math.abs(lon - tempLon);
        if ((dLat > 0.0005 | dLon > 0.0005)){
            lastLat = tempLat = lat;lastLon = tempLon = lon;
            if (mCallback != null)
                mCallback.gpsCallback(lat,lon,city);
            Utility.e(getClass(), "lat:" + dLat + ",lon:" + lon + ",city:" + city);
        }
    }

    /**
     * 位置监听器
     */
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        // 当位置变化时触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                double currLatitude = location.getLatitude();
                double currLongitude = location.getLongitude();
                String city = getAddress(mContext,currLongitude,currLatitude);
                handleResult(currLatitude, currLongitude,city);
                //                mLocationManager.removeUpdates(this);
                //                toggleGPS();
            }
        }
    };

    private void toggleGPS() {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings",
                               "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用高德地理解析，根据经纬度获取对应地址,；<br>
     * 若使用百度地图经纬度，须经过百度API接口(BMap.Convertor.transMore(points,2,callback))的转换；
     * @param context
     * @param longitude 经度
     * @param latitude 纬度
     * @return 详细街道地址
     */
    public static String getAddress(Context context,double longitude,double latitude){
        String address= null;

        GeoPoint geo = new GeoPoint((int)(latitude*1E6),(int)(longitude*1E6));

        Geocoder mGeocoder = new Geocoder(context);

        int x = geo.getLatitudeE6();//得到geo 纬度，单位微度(度* 1E6)
        double x1 = ((double)x)/1000000;
        int y = geo.getLongitudeE6();//得到geo 经度，单位微度(度* 1E6)
        double y1 = ((double) y) / 1000000;

        //得到逆理编码，参数分别为：纬度，经度，最大结果集
        try {
            //高德根据政府规定，在由GPS获取经纬度显示时，使用getFromRawGpsLocation()方法;
            List<Address> listAddress = mGeocoder.getFromLocation(x1, y1, 3);
            if(listAddress.size()!=0){
                Address a = listAddress.get(0);

                address = a.getSubLocality()==null?a.getLocality():a.getSubLocality();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
