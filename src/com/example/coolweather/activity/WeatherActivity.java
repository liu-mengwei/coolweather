package com.example.coolweather.activity;

import java.util.ArrayList;

import com.example.coolweather.R;
import com.example.coolweather.database.Mydatabase;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpHandler;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.JsonHandler;
import com.example.coolweather.util.Pingyin;

import android.R.integer;
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

public class WeatherActivity extends BaseActivity{

	public static final String TAG="WeatherActivity";
	public static final int UPDATE_WEATHERINFO_SUCCESS=0;
	public static final int UPDATE_WEATHERINFO_FAIL=1;
	public static final int UPDATE_CITYINFO_SUCCESS=2;
	public static final int UPDATE_CITYINFO_FAIL=3;
	public static final int UPDATEPROGRESS=4;
	private static final String lock="666";

	private Button home;
	private Button reset;
	private TextView weather_title;
	private TextView publish_time;
	private ImageView weather_image;
	private TextView temp1;
	private TextView temp2;
	private TextView weahter_describe;
	private TextView date;

	private SharedPreferences weatherinfo_pre;
	private SharedPreferences isfirstused_pre;
	private boolean isbackinfo=false;
	private boolean isFirstUsed=true;
	private ProgressDialog progressDialog;
	private int progress_value=0;//用来进行更新进度框操作

	private String county_code="";
	private String county_name;
	private String weather_code;

	private Intent from;
	private Mydatabase coolweather_db;
	private ArrayList<Province> province_list;
	private ArrayList<City> city_list;
	private ArrayList<County> county_list;
	private 	int selected_province_id;
	private String selected_province_code;
	private int selected_city_id;
	private 	String selected_city_code;
	private Thread t1;
	private Thread t2;
	
	Handler handler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case UPDATE_WEATHERINFO_SUCCESS:
				updateUI();				
				Toast.makeText(WeatherActivity.this, "更新天气信息成功 (^_^)", Toast.LENGTH_SHORT).show();
				closeprogress();
				break;
			case UPDATE_WEATHERINFO_FAIL:
				Toast.makeText(WeatherActivity.this, "更新天气信息失败 (>﹏<)", Toast.LENGTH_SHORT).show();
				closeprogress();
				break;
			case UPDATE_CITYINFO_SUCCESS:
				isFirstUsed=false;
				SharedPreferences.Editor editor=isfirstused_pre.edit();
				editor.putBoolean("isFirstUsed", false);
				editor.commit();
				Toast.makeText(WeatherActivity.this, "加载列表成功(^_^)", Toast.LENGTH_SHORT).show();
				closeprogress();
				break;
			case UPDATE_CITYINFO_FAIL:
				Toast.makeText(WeatherActivity.this, "网络连接失败(>﹏<)", Toast.LENGTH_SHORT).show();
				isFirstUsed=true;
				closeprogress();
				break;
			case UPDATEPROGRESS://只有这个不关进度框也是醉了-----
				progressDialog.setProgress(progress_value);
				break;		
			}
		};	
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weatherlayout);
		coolweather_db=Mydatabase.getdatabase(this);
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
		//这里分了个线程出去，没有执行完这句，后面就已经执行了，所以province_list为空,我草这个bug查了两个多小时	
		//检查是否是第一次使用软件，如果是则保存所有数据到数据库
		isfirstused_pre=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=isfirstused_pre.getBoolean("isFirstUsed", true);	
		t1=new Thread(new update_cityinfoThread());
		t2=new Thread(new update_weatherinfoThread());	
		if(isFirstUsed){//数据库中可能有数据，因为可能存了一办网断了，但已经存入了数据,为了防止重复，删除之前存入的数据
			coolweather_db.clearDatabase();
			showprogress("update_cityinfo");	
			t1.start();
		}		
		else {//如果不是第一次使用则正常获得countycode
//			from=getIntent();
//			county_code=from.getStringExtra("county_code");
//			county_name=from.getStringExtra("county_name");
//			weather_title.setText(county_name);
			county_code="280601";
			updateUI();
			showprogress("update_weather");		
		}		
		t2.start();		
		//--------按钮绑定事件
		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.reset2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.reset1);
					showprogress("update_weather");
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
					startActivity(intent);
					finish();
				}
				return false;
			}
		});
	}

	private void updateUI() {
		weatherinfo_pre=getSharedPreferences("weather_info", MODE_PRIVATE);
		String temp1=weatherinfo_pre.getString("temp1", "");
		String temp2=weatherinfo_pre.getString("temp2", "");
		String weather_describe=weatherinfo_pre.getString("weather_describe", "");
		String update_time=weatherinfo_pre.getString("update_time", "");
		String date=weatherinfo_pre.getString("date", "");
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
		queryfromServer("getWeatherCode",county_code,-1);
	}

	private void showWeather(String weather_code) {
		boolean result=queryfromServer("weather", weather_code,-1);
		Message message=new Message();	
		if(result==true){
			message.what=UPDATE_WEATHERINFO_SUCCESS;
		}
		else {
			message.what=UPDATE_WEATHERINFO_FAIL;
		}
		handler.sendMessage(message);
	}

	private void showprogress(String type) {
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);
		if(type.equals("update_weather")){
			progressDialog.setMessage("正在更新天气信息...");
		}
		else {
			progressDialog.setMax(34);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("正在加载城市列表...");
		}
		progressDialog.show();
	}

	private void closeprogress(){
		if(progressDialog!=null){
			Log.d("ChooseAreaActivity", "成功关闭进度框");
			try {
				Thread.sleep(500);//加一个短暂的停留时间，避免查的太快直接瞬间关掉，我TM这都考虑到了，简直醉了
			} catch (Exception e) {
				e.printStackTrace();
			}		
			progressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		ActivityController.closeProcess();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "活动二销毁");
	}

	//将从网上获得的所有数据存储到数据库,如果中间一环出了问题就返回false
	public boolean saveAllinfo(){
		if(queryfromServer("province", null,-1)){
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Log.d(TAG, "正在读取"+province_list.get(i).getName());
				Message message=new Message();
				message.what=UPDATEPROGRESS;
				progress_value++;
				handler.sendMessage(message);
				if(queryfromServer("city",null, province_list.get(i).getId())){//id号其实是最好存入数据库时要用得，存城市时要有省份的id
					for(int j=0;j<city_list.size();){    
						selected_city_code=city_list.get(j).getCode();
						selected_city_id=city_list.get(j).getId();			
						if(queryfromServer("county",null,city_list.get(j).getId())){			
							j++;
						}
						else {
							return false;
						}
					}
				}
				else {
					return false;
				}
			}
		}
		else {
			return false;
		}
		return true;
	}

	private boolean queryfromServer(final String type, final String code,final int id) {
		String address="";
		switch (type) {
		case "getWeatherCode"://获取天气码
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";	
			break;
		case "weather":	//获取天气信息
			address="http://www.weather.com.cn/data/cityinfo/"+code+".html";
			break;
		case "province"://获取省级列表
			address="http://www.weather.com.cn/data/list3/city.xml";
			break;
		case "city"://获取市级列表
			address="http://www.weather.com.cn/data/list3/city"+selected_province_code+".xml";		
			break;
		case "county"://获取县级列表
			address="http://www.weather.com.cn/data/list3/city"+selected_city_code+".xml";
			break;	
		default:
			break;
		}
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onhandle(String response) {	
				switch (type) {
				case "getWeatherCode":
					weather_code=HttpHandler.httpHandleMessage
					(response, "getWeatherCode", coolweather_db, -1);//-1表示这个数据没用		
					break;
				case "weather":	
					Log.d(TAG, "这response究竟是多少"+response);
					JsonHandler.JsonHandleMessage(response,WeatherActivity.this);
					break;
				case "province":
					HttpHandler.httpHandleMessage(response, "province", coolweather_db, -1);
					province_list=coolweather_db.getALLprovince();
					break;
				case "city":	
					HttpHandler.httpHandleMessage(response, "city", coolweather_db, selected_province_id);
					city_list=coolweather_db.getALLcity(selected_province_id);
					break;
				case "county":	
					HttpHandler.httpHandleMessage(response, "county", coolweather_db, selected_city_id);
					county_list=coolweather_db.getALLcounty(selected_city_id);
					break;				
				default:
					break;
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

	class update_cityinfoThread implements Runnable{
		@Override
		public void run() {
			boolean result=saveAllinfo();	
			//这里用Message返回给主线程，告诉主线程处理结果
			Message message=new Message();	
			if(result==true){				
				message.what=UPDATE_CITYINFO_SUCCESS;
				county_code="280601";//测试		
			}
			else {
				message.what=UPDATE_CITYINFO_FAIL;
			}
			handler.sendMessage(message);
		}
	}
	
	class update_weatherinfoThread implements Runnable{
		@Override
		public void run() {
			if(county_code.equals("")){//等待countycode
				try {
					t1.join();//让线程一先执行
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "countycode值为"+county_code);
			getweather_code(county_code);//用这个方法给变weather_code的值
			Log.d(TAG, "weathercode值为"+weather_code);
			showWeather(weather_code);					
		}		
	}
	
	
	
	

}
