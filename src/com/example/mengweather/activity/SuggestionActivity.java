package com.example.mengweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.mengweather.R;
import com.example.mengweather.model.Suggestion;
import com.example.mengweather.util.SuggestionAdapter;
import com.hp.hpl.sparta.Document.Index;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SuggestionActivity extends BaseActivity{
	private Button back;
	private ListView suggestion;
	private SuggestionAdapter suggestionadapter;
	private List<Suggestion> suggestions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suggestion);
		initializeSuggestion();
		suggestionadapter=new SuggestionAdapter(this, R.layout.suggestion_item, suggestions);
		suggestion=(ListView) findViewById(R.id.suggestion);
		suggestion.setAdapter(suggestionadapter);
		
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
	}

	private void initializeSuggestion() {
		SharedPreferences pre=getSharedPreferences("weather_info", Context.MODE_PRIVATE);
		suggestions=new ArrayList<Suggestion>();
		suggestions.add(new Suggestion("舒适指数", pre.getString("index1", ""), pre.getString("detail1", "")));
		suggestions.add(new Suggestion("洗车指数", pre.getString("index2", ""), pre.getString("detail2", "")));
		suggestions.add(new Suggestion("穿衣指数", pre.getString("index3", ""), pre.getString("detail3", "")));
		suggestions.add(new Suggestion("感冒指数", pre.getString("index4", ""), pre.getString("detail4", "")));
		suggestions.add(new Suggestion("运动指数", pre.getString("index5", ""), pre.getString("detail5", "")));
		suggestions.add(new Suggestion("旅游指数", pre.getString("index6", ""), pre.getString("detail6", "")));
		suggestions.add(new Suggestion("紫外线指数", pre.getString("index7", ""), pre.getString("detail7", "")));
	}

	@Override
	public void onBackPressed() {
		back();
	}

	public void back(){
		Intent to=new Intent(SuggestionActivity.this, WeatherActivity.class);
		to.putExtra("tag", "Noneed");//表示让WeatherActivity不用再更新天气的标识
		to.putExtra("county_code", getIntent().getStringExtra("county_code"));
		startActivity(to);
		finish();
	}
	
	
}
