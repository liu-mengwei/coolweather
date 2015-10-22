package com.example.mengweather.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.mengweather.R;
import com.example.mengweather.database.Mydatabase;
import com.example.mengweather.model.City;
import com.example.mengweather.model.Province;
import com.example.mengweather.service.WeatherService;
import com.example.mengweather.util.HttpCallbackListener;
import com.example.mengweather.util.HttpHandler;
import com.example.mengweather.util.HttpUtil;
import com.example.mengweather.util.JsonHandler;
import com.example.mengweather.util.MyLocation;
import com.example.mengweather.util.Pingyin;

import android.R.bool;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
	public static final int UPDATE_PROGRESS=4;
	public static final int LOCATE_FAIL=5;
	private  LocationClient locationClient;
	private LocationClientOption option;

	private Button home;
	private Button reset;
	private Button suggestion;
	private TextView weather_title;
	private ImageView weather_image;
	private TextView tmp;
	private TextView weahter_describe;
	private TextView quality;
	private TextView tormmorw_de;
	private TextView after_de;
	private TextView update_time;
	private TextView tommorow_tmp;
	private TextView after_tmp;

	private SharedPreferences weatherinfo_pre;
	private SharedPreferences isfirstused_pre;
	private SharedPreferences location_pre;
	private boolean isbackinfo=false;
	private boolean isFirstUsed=true;
	private boolean service_tag=false;//需要启动service的标识
	private boolean locate_tag=false;//定位失败的标识,默认失败
	private ProgressDialog progressDialog;
	private int progress_value=0;//用来进行更新进度框操作

	private String county_code="";
	private String county_name;
	private String weather_code;

	private Intent from;
	private Mydatabase coolweather_db;
	private ArrayList<Province> province_list;
	private ArrayList<City> city_list;
	private 	int selected_province_id;
	private String selected_province_code;
	private long exitTime;

	//用来处理返回的消息
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case UPDATE_WEATHERINFO_SUCCESS:
				updateUI();				
				closeprogress();
				Toast.makeText(WeatherActivity.this, "更新天气信息成功(^_^)", Toast.LENGTH_SHORT).show();
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
				closeprogress();
				break;
			case UPDATE_CITYINFO_FAIL:
				Toast.makeText(WeatherActivity.this, "网络连接失败(>﹏<)...请按右上角刷新键重试", Toast.LENGTH_SHORT).show();
				isFirstUsed=true;
				closeprogress();
				break;
			case UPDATE_PROGRESS://只有这个不关进度框也是醉了-----
				progressDialog.setProgress(progress_value);
				break;		
			case LOCATE_FAIL:		
				Toast.makeText(WeatherActivity.this, "定位失败(>﹏<)...请退出程序重试", Toast.LENGTH_SHORT).show();
				closeprogress();
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
		suggestion=(Button) findViewById(R.id.suggestion);
		weather_title=(TextView) findViewById(R.id.weather_title);	
		weather_image=(ImageView) findViewById(R.id.weather_image);
		tmp=(TextView) findViewById(R.id.tmp);
		weahter_describe=(TextView) findViewById(R.id.weather_describe);
		quality=(TextView) findViewById(R.id.quality);
		tormmorw_de=(TextView) findViewById(R.id.tormmorw_de);
		after_de=(TextView) findViewById(R.id.after_de);
		update_time=(TextView) findViewById(R.id.update_time);	
		tommorow_tmp=(TextView) findViewById(R.id.tommorow_tmp);
		after_tmp=(TextView) findViewById(R.id.after_tmp);

		//检查是否是第一次使用软件，如果是则保存所有数据到数据库
		isfirstused_pre=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=isfirstused_pre.getBoolean("isFirstUsed", true);	
		String tag="need";

		if(isFirstUsed){//数据库中可能有数据，因为可能存了一办网断了，但已经存入了数据,为了防止重复，删除之前存入的数据
			findViewById(R.id.tommorow).setVisibility(View.INVISIBLE);
			findViewById(R.id.after).setVisibility(View.INVISIBLE);
			findViewById(R.id.nowweather).setVisibility(View.INVISIBLE);
			suggestion.setVisibility(View.INVISIBLE);
			coolweather_db.clearDatabase();
			showprogress("update_cityinfo");	
			
			//开启定位
			startLocate();			
			startTiming();
			locationClient.registerLocationListener(new BDLocationListener() {			
				@Override
				public void onReceiveLocation(BDLocation location) {
					String city=location.getCity();
					Log.d(TAG, city);
					county_name=city.substring(0, city.length()-1);			
					service_tag=true;
					locate_tag=true;
					new Thread(new Update_weatherinfoThread()).start();//开启存入数据的线程						
				}
			});
		}	

		else {//如果不是第一次使用则正常获得countycode
			isFirstUsed=false;
			from=getIntent();
			county_code=from.getStringExtra("county_code");//这是从列表里切换过来的
			county_name=from.getStringExtra("county_name");
			weather_code=from.getStringExtra("weather_code");
			tag=from.getStringExtra("tag");
			if(county_code==null){//自己的城市(打开软件时)
				service_tag=true;
				location_pre=getSharedPreferences("location_pre", MODE_PRIVATE);
				county_name=location_pre.getString("locationName", "");		
				county_code=location_pre.getString("locationCode", "");//这时候肯定有countycode,
				tag="need";		
				//开启定位(假如到了最新城市，软件一打开就显示当地天气)
				startLocate();
				locationClient.registerLocationListener(new BDLocationListener() {			
					@Override
					public void onReceiveLocation(BDLocation location) {
						String city=location.getCity();
						Log.d(TAG, city);
						String cityName=city.substring(0, city.length()-1);		
						String cityCode=coolweather_db.getcity(cityName).get(0).getCode();
						if(!county_name.equals(cityName)){//获得最新城市
							SharedPreferences.Editor editor=location_pre.edit();					
							editor.putString("locationName", cityName);
							editor.putString("locationCode", cityCode);
							editor.commit();
							county_code=cityCode;
							new Thread(new Update_weatherinfoThread()).start();//如果城市不一样则再更新一遍天气，并将城市存入pre中
						}				
					}
				});			
			}					
			updateUI();
			if(tag.equals("need")){
				showprogress("update_weather");
			}					
		}	

		if(tag.equals("need")&&isFirstUsed==false){			
			new Thread(new Update_weatherinfoThread()).start();//更新天气线程							
		}	
	
		//--------按钮绑定事件
		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.reset2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.reset1);
					if(isFirstUsed==true){//卧槽666
						coolweather_db.clearDatabase();
						showprogress("update_cityinfo");
						progress_value=0;
						new Thread(new Update_weatherinfoThread()).start();//如果执行失败，点一下再执行一次
					}
					else {				
						showprogress("update_weather");
						new Thread(new Update_weatherinfoThread()).start();
					}
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
					intent.putExtra("weather_code", weather_code);
					startActivity(intent);
					finish();
				}
				return false;
			}
		});

		suggestion.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.zzzuggestion2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.zzzuggestion1);
					Intent intent=new Intent(WeatherActivity.this, SuggestionActivity.class);
					intent.putExtra("county_code", county_code);
					intent.putExtra("weather_code", weather_code);
					startActivity(intent);
					finish();
				}
				return false;
			}
		});
	}
	
	//因为百度的定位有时不知为撒会失败，只能用以下方法先把进度框关掉
	private void startTiming() {
		TimerTask task=new TimerTask() {		
			@Override
			public void run() {
				if(locate_tag==false){//定位失败
					Looper.prepare();
					Message message=new Message();
					message.what=LOCATE_FAIL;
					handler.sendMessage(message);	
				}
			}
		};
		Timer timer=new Timer();
		long delay=5000;
		timer.schedule(task, delay);		
	}

	private void updateUI() {	
		findViewById(R.id.tommorow).setVisibility(View.VISIBLE);
		findViewById(R.id.after).setVisibility(View.VISIBLE);
		findViewById(R.id.nowweather).setVisibility(View.VISIBLE);
		suggestion.setVisibility(View.VISIBLE);

		weatherinfo_pre=getSharedPreferences("weather_info", MODE_PRIVATE);
		String tmp=weatherinfo_pre.getString("tmp", "");
		String weather_describe=weatherinfo_pre.getString("weather_describe", "");
		String county_name=weatherinfo_pre.getString("county_name", "");
		String quality=weatherinfo_pre.getString("quality", "");
		String tommorow_de=weatherinfo_pre.getString("tommorow_de", "");
		String after_de=weatherinfo_pre.getString("after_de", "");
		String update_time=weatherinfo_pre.getString("update_time", "");
		String tmp1=weatherinfo_pre.getString("tmp1", "");
		String tmp2=weatherinfo_pre.getString("tmp2", "");
		String after_tmp1=weatherinfo_pre.getString("after_tmp1", "");
		String after_tmp2=weatherinfo_pre.getString("after_tmp2", "");

		//用this表示是view
		this.weather_title.setText(county_name);
		this.tmp.setText(tmp+"℃");
		this.weahter_describe.setText(weather_describe);
		this.quality.setText("空气质量："+quality);
		this.tormmorw_de.setText(tommorow_de);
		this.after_de.setText(after_de);
		this.update_time.setText(update_time.split(" ")[1]+"发布");
		this.tommorow_tmp.setText(tmp1+"~"+tmp2+"℃");
		this.after_tmp.setText(after_tmp1+"~"+after_tmp2+"℃");

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
		if(result==false){
			Log.d(TAG, "看发了没");
		}
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
			progressDialog.setMessage("正在加载城市列表.....(>﹏<)\n若失败请切换至WIFI环境点击右上角刷新按钮\n查看源码请按菜单键");
		}
		progressDialog.show();
	}

	private void closeprogress(){
		if(progressDialog!=null){
			try {
				Thread.sleep(500);//加一个短暂的停留时间，避免查的太快直接瞬间关掉，我TM这都考虑到了，简直醉了
			} catch (Exception e) {
				e.printStackTrace();
			}		
			progressDialog.dismiss();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
			if((System.currentTimeMillis()-exitTime) > 2000){  
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
				exitTime = System.currentTimeMillis();   
			} else {
				ActivityController.closeProcess();
			}
			return true;   
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "活动二销毁");
	}

	//将从网上获得的所有数据存储到数据库,如果中间一环出了问题就返回false,先只加载省市数据
	public boolean saveAllinfo(){
		if(queryfromServer("province", null,-1)==false){
			return false;
		}
		else {
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Log.d(TAG, "正在读取"+province_list.get(i).getName());
				Message message=new Message();
				message.what=UPDATE_PROGRESS;
				progress_value++;
				handler.sendMessage(message);
				//id号其实是最好存入数据库时要用得，存城市时要有省份的id
				if(queryfromServer("city",null, province_list.get(i).getId())==false){
					return false;
				}				
			}
			return true;
		}	
	}

	private boolean queryfromServer(final String type, final String code,final int id) {
		String address="";
		switch (type) {
		case "getWeatherCode"://获取天气码
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";	
			break;
		case "weather":	//获取天气信息
			address="https://api.heweather.com/x3/weather?cityid="+"CN"+weather_code+"&key"
					+ "=e2c80cc8f31a4189b0621adfd4813193";
			break;
		case "province"://获取省级列表
			address="http://www.weather.com.cn/data/list3/city.xml";
			break;
		case "city"://获取市级列表
			address="http://www.weather.com.cn/data/list3/city"+selected_province_code+".xml";		
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

	/**
	 * 用来将全部城市列表导入数据库的线程类
	 * */
	class Update_cityinfoThread implements Runnable{
		@Override
		public void run() {
			boolean result=saveAllinfo();
			//这里用Message返回给主线程，告诉主线程处理结果
			Message message=new Message();	
			if(result==true&&county_name!=null){				
				message.what=UPDATE_CITYINFO_SUCCESS;		
				county_code=coolweather_db.getcity(county_name).get(0).getCode();//这里直接复用代码，取第一个城市就行	
				//在内部类中可以直接调用getSharedPreferences方法
				SharedPreferences location_pre=getSharedPreferences("location_pre", MODE_PRIVATE);
				SharedPreferences.Editor editor=location_pre.edit();
				editor.putString("locationName", county_name);//将定位好的城市名存到pre中
				editor.putString("locationCode", county_code);
				editor.commit();
			}
			else {
				message.what=UPDATE_CITYINFO_FAIL;
			}
			handler.sendMessage(message);
		}
	}

	public void startLocate(){
		locationClient=new LocationClient(this);
		LocationClientOption option=new LocationClientOption();
		option.setIsNeedAddress(true);
		locationClient.setLocOption(option);
		locationClient.start();	
		Log.d(TAG, "+++++++++");
	}

	/**
	 *用来更新天气信息的线程类
	 */
	class Update_weatherinfoThread implements Runnable{
		@Override
		public void run() {
			if(TextUtils.isEmpty(county_code)){//等待countycode
				try {
					Thread t1=new Thread(new Update_cityinfoThread());//让线程一先执行结束，因为没有countycode
					t1.start();
					t1.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "countycode值为"+county_code);
			getweather_code(county_code);//上面那个线程执行完，就有county_code的值了
			showWeather(weather_code);			
			//下面这个是本来用户设置的选项
			boolean button_state=getSharedPreferences("button_state", MODE_PRIVATE).getBoolean("button_state", true);
			if(service_tag==true&&button_state==true){
				Log.d(TAG, "service启动");		
				Intent intent=new Intent(WeatherActivity.this, WeatherService.class);
				intent.putExtra("weather_code", weather_code);
				startService(intent);	
			}
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.question_item:
			AlertDialog.Builder builder=new AlertDialog.Builder(WeatherActivity.this);
			builder.setTitle("查看源码及BUG反馈");
			builder.setCancelable(false);
			builder.setMessage("查看源码请访问github.com/liu-mengwei (version4.0分支)， "
					+ "BUG反馈请联系作者微信lmw-1223,或发送邮件到542221757@qq.com");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			break;
		case R.id.settings_item:	
			Intent intent=new Intent(WeatherActivity.this, SettingsActivity.class);
			intent.putExtra("county_code", county_code);
			intent.putExtra("weather_code", weather_code);
			Log.d(TAG, "++"+weather_code);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
		return true;	
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(locationClient!=null){
			locationClient.stop();
		}
	}

}
