package com.example.mengweather.receiver;

import com.example.mengweather.service.WeatherService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent to=new Intent(context, WeatherService.class);
		to.putExtra("weather_code", intent.getStringExtra("weather_code"));
		context.startService(to);
		Log.d("Receiver", " ’µΩπ„≤•");
	}

}
