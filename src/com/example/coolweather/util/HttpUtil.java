package com.example.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpUtil {

	//listener是java的回调机制
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){

		StringBuilder builder=new StringBuilder();
		try {
			URL url=new URL(address);				
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			connection.setReadTimeout(15000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("GET");
			BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line="";
			while((line=reader.readLine())!=null){
				builder.append(line);
			}
			listener.onhandle(builder.toString());													
		} catch (Exception e) {
			// TODO: handle exception
			listener.onerror(e);
		}

	}



}
