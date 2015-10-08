package com.example.mengweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class JsonHandler {
	
	public static final String TAG="JsonHandler";

	public static void JsonHandleMessage(String response,Context context){
		//����JSON��ʽ���ݲ�����sharedPreferences
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy��M��d��",Locale.CHINA);
		try {			
			JSONObject object=new JSONObject(response);
			JSONObject weatherinfo=object.getJSONObject("weatherinfo");
			String county_name=weatherinfo.getString("city");//�����ʵ������
			String temp1=weatherinfo.getString("temp1");
			String temp2=weatherinfo.getString("temp2").split("��")[0];
			String weather_describe=weatherinfo.getString("weather");
			//ƴ������
			SharedPreferences sharedPreferences=context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor=sharedPreferences.edit();
			editor.putString("county_name", county_name);
			editor.putString("temp1", temp1);
			editor.putString("temp2", temp2);
			editor.putString("weather_describe", weather_describe);
			editor.putString("date", sdf.format(new Date()));
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * ����json����
	 * @param response
	 * @param context
	 * @return ���������ĳ�����
	 */
	public static String JsonHandlePosition(String response,Context context){
		String cityName="";
		try {
			String jsondata=response.substring(29);
			JSONObject object=new JSONObject(jsondata);
			JSONObject result=object.getJSONObject("result");
			JSONObject addressComponent=result.getJSONObject("addressComponent");
			String city=addressComponent.getString("city");
			cityName=city.substring(0, city.length()-1);
			Log.d(TAG, cityName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cityName;	
	}
	
}
