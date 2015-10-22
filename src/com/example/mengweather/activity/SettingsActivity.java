package com.example.mengweather.activity;

import com.example.mengweather.R;
import com.example.mengweather.service.WeatherService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class SettingsActivity extends BaseActivity{

	private Button back;
	private Button service_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settingslayout);

		back=(Button) findViewById(R.id.back);
		back.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.back2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					back();
				}
				return false;		
			}
		});	
		
		service_button=(Button) findViewById(R.id.service_button);
		final SharedPreferences button_pre=getSharedPreferences("button_state", MODE_PRIVATE);
		final SharedPreferences.Editor editor=button_pre.edit();
		boolean button_state=button_pre.getBoolean("button_state", true);
		if(button_state==true){
			service_button.setBackgroundResource(R.drawable.zzzzbutton_on);
		}
		else {
			service_button.setBackgroundResource(R.drawable.zzzzbutton_off);
		}
			
		service_button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				boolean button_state=button_pre.getBoolean("button_state", true);
				if(button_state==true){
					Intent intent=new Intent(SettingsActivity.this, WeatherService.class);
					stopService(intent);
					v.setBackgroundResource(R.drawable.zzzzbutton_off);
					editor.putBoolean("button_state", false);
					editor.commit();
				}
				else {
					Intent intent=new Intent(SettingsActivity.this, WeatherService.class);
					intent.putExtra("weather_code", getIntent().getStringExtra("weather_code"));
					Log.d("SettingsActivity", getIntent().getStringExtra("weather_code"));
					startService(intent);
					v.setBackgroundResource(R.drawable.zzzzbutton_on);
					editor.putBoolean("button_state", true);
					editor.commit();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		back();
	}

	public void back(){
		Intent to=new Intent(this, WeatherActivity.class);
		to.putExtra("tag", "Noneed");//表示让WeatherActivity不用再更新天气的标识
		to.putExtra("county_code", getIntent().getStringExtra("county_code"));
		to.putExtra("weather_code", getIntent().getStringExtra("weather_code"));
		startActivity(to);
		finish();
	}

}
