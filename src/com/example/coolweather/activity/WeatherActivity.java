package com.example.coolweather.activity;

import com.example.coolweather.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class WeatherActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weatherlayout);
		
		
	}

}
