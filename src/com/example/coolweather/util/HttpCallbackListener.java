package com.example.coolweather.util;

public interface HttpCallbackListener {

	public void onhandle(String response);
	public void onerror(Exception e);
	
}
