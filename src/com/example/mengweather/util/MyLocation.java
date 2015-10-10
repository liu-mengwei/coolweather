package com.example.mengweather.util;

import java.util.List;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


/**
 * ����ʵ�ֵ�ͼ��λ���ܵ���
 */
public class MyLocation {
	
	public static final String TAG="Mylocation";
	private static LocationManager locationManager;
	private static String provider;
	private static List<String> providerList;
	private static String cityName="";
	
	/**
	 * �÷��������뷵��һ��������
	 * @param 
	 * @return ��(��)����
	 */
	public static String getLocationName(final Context context){
		Location location=getlocation(context);
		Log.d(TAG, "����"+location.getLatitude());
		//ƴ�ӷ���������ĵ�ַ
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
	 *��ȡ����λ�õľ�γ��
	 * @param context
	 * @return ����λ�õľ�γ��
	 */
	public static Location getlocation(Context context){
		locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
		providerList=locationManager.getProviders(true);//��ȡ���п��õ�λ���ṩ��
		if(providerList.contains(LocationManager.GPS_PROVIDER)){
			provider=LocationManager.GPS_PROVIDER;
		}
		else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider=LocationManager.NETWORK_PROVIDER;
		}
		else {
			Log.d("MyLocation", "��λʧ��");
		}		
		Location location=locationManager.getLastKnownLocation(provider);//�Բ۷�װ����ã�����ôһ�д���ͷ��ص���λ���ˣ�
		return location;		
	}

	
}
