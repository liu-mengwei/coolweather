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
		JSONObject object;
		JSONObject info;
		SharedPreferences.Editor editor = null;
		SharedPreferences sharedPreferences;
		try {			
			object=new JSONObject(response);
			info=object.getJSONArray("HeWeather data service 3.0").getJSONObject(0);		
			String update_time=info.getJSONObject("basic").getJSONObject("update").getString("loc");
			
			String county_name=info.getJSONObject("basic").getString("city");
			String weather_describe=info.getJSONObject("now").getJSONObject("cond").getString("txt");
			String tmp=info.getJSONObject("now").getString("tmp");
			
			JSONObject tommorow=info.getJSONArray("daily_forecast").getJSONObject(1);
			String tommorow_de=tommorow.getJSONObject("cond").getString("txt_d");
			String tmp1=tommorow.getJSONObject("tmp").getString("min");
			String tmp2=tommorow.getJSONObject("tmp").getString("max");
			
			JSONObject after_tommorow=info.getJSONArray("daily_forecast").getJSONObject(2);
			String after_de=after_tommorow.getJSONObject("cond").getString("txt_d");
			String after_tmp1=after_tommorow.getJSONObject("tmp").getString("min");
			String after_tmp2=after_tommorow.getJSONObject("tmp").getString("max");
			
			//生活指数
			JSONObject suggestion=info.getJSONObject("suggestion");
			String index1=suggestion.getJSONObject("comf").getString("brf");
			String detail1=suggestion.getJSONObject("comf").getString("txt");
			String index2=suggestion.getJSONObject("cw").getString("brf");
			String detail2=suggestion.getJSONObject("cw").getString("txt");
			String index3=suggestion.getJSONObject("drsg").getString("brf");
			String detail3=suggestion.getJSONObject("drsg").getString("txt");
			String index4=suggestion.getJSONObject("flu").getString("brf");
			String detail4=suggestion.getJSONObject("flu").getString("txt");
			String index5=suggestion.getJSONObject("sport").getString("brf");
			String detail5=suggestion.getJSONObject("sport").getString("txt");
			String index6=suggestion.getJSONObject("trav").getString("brf");
			String detail6=suggestion.getJSONObject("trav").getString("txt");
			String index7=suggestion.getJSONObject("uv").getString("brf");
			String detail7=suggestion.getJSONObject("uv").getString("txt");
			//拼接数据			
			sharedPreferences=context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
			editor=sharedPreferences.edit();			
			editor.putString("update_time", update_time);
			editor.putString("county_name", county_name);
			editor.putString("tmp", tmp);
			editor.putString("weather_describe", weather_describe);
			
			editor.putString("tommorow_de", tommorow_de);
			editor.putString("tmp1", tmp1);
			editor.putString("tmp2", tmp2);
			
			editor.putString("after_de", after_de);			
			editor.putString("after_tmp1", after_tmp1);
			editor.putString("after_tmp2", after_tmp2);
			
			editor.putString("index1", index1);
			editor.putString("detail1", detail1);
			editor.putString("index2", index2);
			editor.putString("detail2", detail2);
			editor.putString("index3", index3);
			editor.putString("detail3", detail3);
			editor.putString("index4", index4);
			editor.putString("detail4", detail4);
			editor.putString("index5", index5);
			editor.putString("detail5", detail5);
			editor.putString("index6", index6);
			editor.putString("detail6", detail6);
			editor.putString("index7", index7);
			editor.putString("detail7", detail7);
			
			String quality=info.getJSONObject("aqi").getJSONObject("city").getString("qlty");
			editor.putString("quality", quality);	
		} catch (Exception e) {
			e.printStackTrace();
			editor.putString("quality", "无数据");
		}finally{
			editor.commit();
		}
	}
	
	
	/**
	 * 处理json数据,因为更换定位方式，所以这个用不到了
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
