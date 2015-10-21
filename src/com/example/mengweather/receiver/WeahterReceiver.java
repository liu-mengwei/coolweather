package com.example.mengweather.receiver;

import com.example.mengweather.service.WeatherService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeahterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent to=new Intent(context, WeatherService.class);
		context.startService(to);
	}

}
