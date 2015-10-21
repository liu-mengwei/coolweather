package com.example.mengweather.service;

import com.example.mengweather.R;
import com.example.mengweather.activity.WeatherActivity;
import com.example.mengweather.receiver.WeahterReceiver;
import com.example.mengweather.util.HttpCallbackListener;
import com.example.mengweather.util.HttpUtil;
import com.example.mengweather.util.JsonHandler;
import com.example.mengweather.util.Pingyin;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class WeatherService extends Service{

	public static final String TAG="WeatherService";
	private String weather_code;
	private 	NotificationManager manager;
	private Notification notification;
	private boolean result=false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {	
		super.onCreate();
		//前台通知
		manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification=new Notification(R.drawable.zzzzaunch, "实时天气", System.currentTimeMillis());
		RemoteViews notification_view=new RemoteViews(getPackageName(), R.layout.notification);
		notification.contentView=notification_view;
		Intent intent=new Intent(getApplicationContext(), WeatherActivity.class);
		PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		notification.contentIntent=pendingIntent;
		startForeground(1, notification);		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		weather_code=intent.getStringExtra("weather_code");
		Thread t1=new Thread(new Update_weatherinfoThread());
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(result==true){
			updateUI();
			Log.d(TAG, "---------");
		}
		else {
			RemoteViews view=new RemoteViews(getPackageName(), R.layout.notification);
			view.setTextViewText(R.id.notification_uptime, "网络连接失败");
			notification.contentView=view;
			manager.notify(1, notification);
		}
		//定时任务
		startTiming();	
		return super.onStartCommand(intent, flags, startId);
	}


	class Update_weatherinfoThread implements Runnable{
		@Override
		public void run() {
			queryFromServer();
		}		
	}

	private void queryFromServer(){
		String address="https://api.heweather.com/x3/weather?cityid="+"CN"+weather_code+"&key"
				+ "=e2c80cc8f31a4189b0621adfd4813193";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {	
			@Override
			public void onhandle(String response) {
				JsonHandler.JsonHandleMessage(response,WeatherService.this);
				result=true;
			}

			@Override
			public void onerror(Exception e) {
				e.printStackTrace();
				result=false;
			}
		});	
	}

	public void updateUI(){
		SharedPreferences weatherinfo_pre=getSharedPreferences("weather_info", MODE_PRIVATE);
		String tmp=weatherinfo_pre.getString("tmp", "")+"℃";
		String weather_describe=weatherinfo_pre.getString("weather_describe", "");
		String county_name=weatherinfo_pre.getString("county_name", "");
		String quality=weatherinfo_pre.getString("quality", "");
		String update_time="今天"+weatherinfo_pre.getString("update_time", "").split(" ")[1]+"发布";
		String image_name=Pingyin.getPingYin(weather_describe).split("zhuan")[0];//转化成拼音并取转前面的天气
		int imageID=Pingyin.getimageID(image_name);	
			
		RemoteViews view=new RemoteViews(getPackageName(), R.layout.notification);
		view.setTextViewText(R.id.notification_cityName, county_name);			
		view.setTextViewText(R.id.notification_describe, weather_describe);
		view.setTextViewText(R.id.notification_temp, tmp);
		view.setTextViewText(R.id.notification_quality, quality);
		view.setTextViewText(R.id.notification_uptime, update_time);	
		view.setImageViewResource(R.id.notification_image, imageID);
		notification.contentView=view;
		manager.notify(1, notification);
	}

	public void startTiming(){
		AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
		long time=SystemClock.elapsedRealtime()+3*3600*1000;//定时三个小时
		Intent intent=new Intent(this, WeahterReceiver.class);
		PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, intent, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pendingIntent);	
	}
	

}
