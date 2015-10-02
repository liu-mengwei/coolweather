package com.example.coolweather.util;



import android.util.Log;

import com.example.coolweather.database.Mydatabase;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

public class HttpHandler {
	
	public static final String TAG="HttpHandler";

	public static void httpHandleMessage(String response,String type,Mydatabase coolweather_db,int id) {
		
		// TODO 自动生成的方法存根
		if(type.equals("province")){
			String [] allprovinces=response.split(",");
			Log.d("HttpHandler", response);
			Log.d("HttpHandler", allprovinces[0]);
			//注意这里要用|划分的话必须加\\-------------什么鬼规定
			Log.d("HttpHandler", allprovinces[0].split("\\|")[0]);
			for (String province_value : allprovinces) {
				Province province=new Province();
				province.setCode(province_value.split("\\|")[0]);
				province.setName(province_value.split("\\|")[1]);
				coolweather_db.saveprovince(province);			
			}
		}
		else if (type.equals("city")) {
			String [] allcitys=response.split(",");
			Log.d("HttpHandler", allcitys[0].split("\\|")[0].substring(0, 2));
			for (String city_value : allcitys) {
				City city=new City();
				city.setCode(city_value.split("\\|")[0]);				
				city.setName(city_value.split("\\|")[1]);
				city.setProvince_id(id);			
				coolweather_db.savecity(city);	
			}
		}
		else if (type.equals("county")) {
			String [] allcounty=response.split(",");
			for (String county_value : allcounty) {
				County county=new County();	
				county.setCode(county_value.split("\\|")[0]);		
				county.setName(county_value.split("\\|")[1]);
				county.setCity_id(id);	
				coolweather_db.savecounty(county);
			}
		}		
	}

}
