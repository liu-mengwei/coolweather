package com.example.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;

public class JsonHandler {

	public static void JsonHandleMessage(String response,Context context){
		//����JSON��ʽ���ݲ�����sharedPreferences
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy��M��d��",Locale.CHINA);
		try {			
			JSONObject object=new JSONObject(response);
			JSONObject weatherinfo=object.getJSONObject("weatherinfo");
			String temp1=weatherinfo.getString("temp1");
			String temp2=weatherinfo.getString("temp2").split("��")[0];
			String weather_describe=weatherinfo.getString("weather");
			String update_time=weatherinfo.getString("ptime");
			//ƴ������
			SharedPreferences sharedPreferences=context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor=sharedPreferences.edit();
			editor.putString("temp1", temp1);
			editor.putString("temp2", temp2);
			editor.putString("weather_describe", weather_describe);
			editor.putString("update_time", update_time);	
			editor.putString("date", sdf.format(new Date()));
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	
	
	
}