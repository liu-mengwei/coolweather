package com.example.coolweather.activity;

import com.example.coolweather.R;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpHandler;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.JsonHandler;
import com.example.coolweather.util.Pingyin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity{
	
	public static final String TAG="WeatherActivity";
	public static final int SUCCESS=0;
	public static final int FAIL=1;
	private Button home;
	private Button reset;
	private TextView weather_title;
	private TextView publish_time;
	private ImageView weather_image;
	private TextView temp1;
	private TextView temp2;
	private TextView weahter_describe;
	private TextView date;
	private SharedPreferences sharedPreferences;
	private boolean isbackinfo=false;
	private ProgressDialog progressDialog;

	private String county_code;
	private String county_name;
	private String weather_code;

	Handler handler=new Handler(){
		
		public void handleMessage(Message message) {
			if(message.what==SUCCESS){
				updateUI();				
				Toast.makeText(WeatherActivity.this, "更新数据成功 (^_^)", Toast.LENGTH_SHORT).show();
			}
			else if (message.what==FAIL) {
				Toast.makeText(WeatherActivity.this, "更新失败 (>﹏<)", Toast.LENGTH_SHORT).show();
			}	
			closeprogress();
		};	
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weatherlayout);
		Log.d(TAG, "活动二创建");
		//获取实例
		home=(Button) findViewById(R.id.home);
		reset=(Button) findViewById(R.id.reset);
		weather_title=(TextView) findViewById(R.id.weather_title);	
		publish_time=(TextView) findViewById(R.id.publish_time);
		weather_image=(ImageView) findViewById(R.id.weather_image);
		temp1=(TextView) findViewById(R.id.temp1);
		temp2=(TextView) findViewById(R.id.temp2);
		weahter_describe=(TextView) findViewById(R.id.weather_describe);
		date=(TextView) findViewById(R.id.date);

		Intent intent=getIntent();
		county_code=intent.getStringExtra("county_code");
		county_name=intent.getStringExtra("county_name");
		
		weather_title.setText(county_name);
		Log.d(TAG, "");
		updateUI();
		showprogress();
		//--------按钮绑定事件
		new Thread(new Runnable() {//感觉这里代码设计缺陷，应该不能从这里就分个线程出去，应该是网络请求时再分		
			@Override
			public void run() {
				getweather_code(county_code);//用这个方法给变weather_code的值
				showWeather(weather_code);		
			}
		}).start();	
			
		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.reset2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.reset1);
					showprogress();
					new Thread(new Runnable() {				
						@Override
						public void run() {
							showWeather(weather_code);
						}
					}).start();
				}
				return false;
			}
		});
		
		home.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.home2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.home1);
					Intent intent=new Intent(WeatherActivity.this, ChooseAreaActivity.class);
					intent.putExtra("county_name", county_name);
					intent.putExtra("county_code", county_code);
					Log.d(TAG, county_name);
					startActivity(intent);
					finish();
				}
				return false;
			}
		});
	}
	
	private void updateUI() {
		sharedPreferences=getSharedPreferences("weather_info", MODE_PRIVATE);
		String temp1=sharedPreferences.getString("temp1", "");
		String temp2=sharedPreferences.getString("temp2", "");
		String weather_describe=sharedPreferences.getString("weather_describe", "");
		String update_time=sharedPreferences.getString("update_time", "");
		String date=sharedPreferences.getString("date", "");
		//用this表示是view
		this.temp1.setText(temp2);//因为服务器返回的temp1是高温，所以换一下
		this.temp2.setText(temp1);
		this.weahter_describe.setText(weather_describe);
		this.publish_time.setText("今天"+update_time+"发布");
		this.date.setText(date);
		//更新图片
		String image_name=Pingyin.getPingYin(weather_describe).split("zhuan")[0];//转化成拼音并取转前面的天气
		int imageID=Pingyin.getimageID(image_name);	
		this.weather_image.setImageResource(imageID);
	}
	
	private void getweather_code(String county_code) {
		queryfromServer("getWeatherCode",county_code);
	}

	private void showWeather(String weather_code) {
		boolean result=queryfromServer("weather", weather_code);
		Message message=new Message();	
		if(result==true){
			message.what=SUCCESS;
		}
		else {
			message.what=FAIL;
		}
		handler.sendMessage(message);
	}


	private boolean queryfromServer(final String type, final String code) {
		String address="";
		if(type.equals("getWeatherCode")){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";	
		}
		else if (type.equals("weather")) {
			address="http://www.weather.com.cn/data/cityinfo/"+code+".html";
		}
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onhandle(String response) {	
				//如果是countycode则表明要获取weather_code
				if(type.equals("getWeatherCode")){
					weather_code=HttpHandler.httpHandleMessage(response, "getWeatherCode", null, -1);//-1表示这个数据没用					
				}
				else if (type.equals("weather")) {
					JsonHandler.JsonHandleMessage(response,WeatherActivity.this);
				}		
				isbackinfo=true;
			}

			@Override
			public void onerror(Exception e) {
				e.printStackTrace();
				isbackinfo=false;
			}
		});
		return isbackinfo;		
	}

	private void showprogress() {
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("正在更新数据...");
		progressDialog.show();		
	}

	private void closeprogress(){
		if(progressDialog!=null){
			Log.d("ChooseAreaActivity", "成功关闭进度框");
			progressDialog.dismiss();
		}
	}


	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "活动二销毁");
	}








}
