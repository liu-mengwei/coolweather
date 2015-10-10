package com.example.mengweather.util;

import java.util.List;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


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
		String address="http://api.map.baidu.com/geocoder/v2/?ak=rfRk6cyi48zoePeEPOX0uT9m"
				+"&mcode=B8:F7:0F:28:97:D0:2A:F8:33:51:AE:46:ED:56:62:FD:C4:B3:4A:36;com.example.mengweather"
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
