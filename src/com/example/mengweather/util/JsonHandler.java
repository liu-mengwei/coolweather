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
		//解析JSON格式数据并存入sharedPreferences
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		try {			
			JSONObject object=new JSONObject(response);
			JSONObject weatherinfo=object.getJSONObject("weatherinfo");
			String county_name=weatherinfo.getString("city");//这个其实是县名
			String temp1=weatherinfo.getString("temp1");
			String temp2=weatherinfo.getString("temp2").split("℃")[0];
			String weather_describe=weatherinfo.getString("weather");
			//拼接数据
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
	 * 处理json数据
	 * @param response
	 * @param context
	 * @return 所处地区的城市名
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
