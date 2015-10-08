package com.example.mengweather.util;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * 用来实现地图定位功能的类
 */
public class MyLocation {
	
	public static final String TAG="Mylocation";
	private static LocationManager locationManager;
	private static String provider;
	private static List<String> providerList;
	private static String cityName="";
	
	/**
	 * 用反向地理编码返回一个市名称
	 * @param 
	 * @return 市(县)名称
	 */
	public static String getLocationName(final Context context){
		Location location=getlocation(context);
		Log.d(TAG, "经度"+location.getLatitude());
		//拼接反向地理编码的地址
		String address="http://api.map.baidu.com/geocoder/v2/?ak=TBLDlcNrPgqBwGnPGBUkDZgR"
				+"&mcode=60:7D:2E:3A:A7:92:57:4A:D3:E9:9C:72:24:4D:24:06:2B:B7:6C:8A;com.example.mengweather"
				+"&callback=renderReverse&location="+location.getLatitude()+","
				+location.getLongitude()+"&output=json&pois=1";
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {		
			@Override
			public void onhandle(String response) {		
				Log.d(TAG, response);
				cityName=JsonHandler.JsonHandlePosition(response, context);
			}			
			@Override
			public void onerror(Exception e) {
				e.printStackTrace();
			}
		});	
		return cityName;	
	}
	
	/**
	 *获取所处位置的经纬度
	 * @param context
	 * @return 所处位置的经纬度
	 */
	public static Location getlocation(Context context){
		locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
		providerList=locationManager.getProviders(true);//获取所有可用的位置提供器
		if(providerList.contains(LocationManager.GPS_PROVIDER)){
			provider=LocationManager.GPS_PROVIDER;
		}
		else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider=LocationManager.NETWORK_PROVIDER;
		}
		else {
			Log.d("MyLocation", "定位失败");
		}		
		Location location=locationManager.getLastKnownLocation(provider);//卧槽封装的真好，就这么一行代码就返回地理位置了？
		return location;		
	}

	
}
