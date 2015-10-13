package com.example.mengweather.activity;

import java.util.ArrayList;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.mengweather.R;
import com.example.mengweather.database.Mydatabase;
import com.example.mengweather.model.City;
import com.example.mengweather.model.Province;
import com.example.mengweather.util.HttpCallbackListener;
import com.example.mengweather.util.HttpHandler;
import com.example.mengweather.util.HttpUtil;
import com.example.mengweather.util.JsonHandler;
import com.example.mengweather.util.MyLocation;
import com.example.mengweather.util.Pingyin;

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
	private  LocationClient locationClient;
	private LocationClientOption option;

	private Button home;
	private Button reset;
	private TextView weather_title;
	private ImageView weather_image;
	private TextView temp1;
	private TextView temp2;
	private TextView weahter_describe;
	private TextView date;

	private SharedPreferences weatherinfo_pre;
	private SharedPreferences isfirstused_pre;
	private SharedPreferences location_pre;
	private boolean isbackinfo=false;
	private boolean isFirstUsed=true;
	private ProgressDialog progressDialog;
	private int progress_value=0;//�������и��½��ȿ����

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

	//���������ص���Ϣ
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case UPDATE_WEATHERINFO_SUCCESS:
				updateUI();				
				closeprogress();
				Toast.makeText(WeatherActivity.this, "����������Ϣ�ɹ�(^_^)", Toast.LENGTH_SHORT).show();
				break;
			case UPDATE_WEATHERINFO_FAIL:
				Toast.makeText(WeatherActivity.this, "����������Ϣʧ�� (>�n<)", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(WeatherActivity.this, "��������ʧ��(>�n<)", Toast.LENGTH_SHORT).show();
				isFirstUsed=true;
				closeprogress();
				break;
			case UPDATE_PROGRESS://ֻ��������ؽ��ȿ�Ҳ������-----
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
		Log.d(TAG, "�������");
		//��ȡʵ��
		home=(Button) findViewById(R.id.home);
		reset=(Button) findViewById(R.id.reset);
		weather_title=(TextView) findViewById(R.id.weather_title);	
		weather_image=(ImageView) findViewById(R.id.weather_image);
		temp1=(TextView) findViewById(R.id.temp1);
		temp2=(TextView) findViewById(R.id.temp2);
		weahter_describe=(TextView) findViewById(R.id.weather_describe);
		date=(TextView) findViewById(R.id.date);
		
		//����Ƿ��ǵ�һ��ʹ�������������򱣴��������ݵ����ݿ�
		isfirstused_pre=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=isfirstused_pre.getBoolean("isFirstUsed", true);	
		String tag="need";

		if(isFirstUsed){//���ݿ��п��������ݣ���Ϊ���ܴ���һ�������ˣ����Ѿ�����������,Ϊ�˷�ֹ�ظ���ɾ��֮ǰ���������
			coolweather_db.clearDatabase();
			showprogress("update_cityinfo");	
			new Thread(new Update_weatherinfoThread()).start();//�����������ݵ��߳�	
			//������λ
			startLocate();
			locationClient.registerLocationListener(new BDLocationListener() {			
				@Override
				public void onReceiveLocation(BDLocation location) {
					String city=location.getCity();
					Log.d(TAG, city);
					county_name=city.substring(0, city.length()-1);			
				}
			});
		}	

		else {//������ǵ�һ��ʹ�����������countycode
			isFirstUsed=false;
			from=getIntent();
			county_code=from.getStringExtra("county_code");//���Ǵ��б����л�������
			county_name=from.getStringExtra("county_name");
			tag=from.getStringExtra("tag");
			if(county_code==null){//�Լ��ĳ���(�����ʱ)
				location_pre=getSharedPreferences("location_pre", MODE_PRIVATE);
				county_name=location_pre.getString("locationName", "");		
				county_code=location_pre.getString("locationCode", "");//��ʱ��϶���countycode,
				tag="need";			
				//������λ
				startLocate();
				locationClient.registerLocationListener(new BDLocationListener() {			
					@Override
					public void onReceiveLocation(BDLocation location) {
						String city=location.getCity();
						Log.d(TAG, city);
						String cityName=city.substring(0, city.length()-1);		
						String cityCode=coolweather_db.getcity(cityName).get(0).getCode();
						if(!county_name.equals(cityName)){//������³���
							SharedPreferences.Editor editor=location_pre.edit();					
							editor.putString("locationName", cityName);
							editor.putString("locationCode", cityCode);
							editor.commit();
							county_code=cityCode;
							new Thread(new Update_weatherinfoThread()).start();//������в�һ�����ٸ���һ���������������д���pre��
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
			new Thread(new Update_weatherinfoThread()).start();;//���������߳�							
		}	
		
		//--------��ť���¼�
		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.reset2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.reset1);
					if(isFirstUsed==true){//�Բ�666
						coolweather_db.clearDatabase();
						showprogress("update_cityinfo");
						progress_value=0;
						new Thread(new Update_weatherinfoThread()).start();//���ִ��ʧ�ܣ���һ����ִ��һ��
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
		String date=weatherinfo_pre.getString("date", "");
		String county_name=weatherinfo_pre.getString("county_name", "");
		//��this��ʾ��view
		this.weather_title.setText(county_name);
		this.temp1.setText(temp2+"	~");//��Ϊ���������ص�temp1�Ǹ��£����Ի�һ��
		this.temp2.setText(temp1);
		this.weahter_describe.setText(weather_describe);
		this.date.setText(date);
		//����ͼƬ
		String image_name=Pingyin.getPingYin(weather_describe).split("zhuan")[0];//ת����ƴ����ȡתǰ�������
		int imageID=Pingyin.getimageID(image_name);	
		this.weather_image.setImageResource(imageID);
	}

	private void getweather_code(String county_code) {
		queryfromServer("getWeatherCode",county_code,-1);
	}

	private void showWeather(String weather_code) {
		boolean result=queryfromServer("weather", weather_code,-1);
		if(result==false){
			Log.d(TAG, "������û");
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
			progressDialog.setMessage("���ڸ���������Ϣ...");
		}
		else {
			progressDialog.setMax(34);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("���ڼ��س����б�.....(>�n<)\n��ʧ�����л���WIFI����������Ͻ�ˢ�°�ť");
		}
		progressDialog.show();
	}

	private void closeprogress(){
		if(progressDialog!=null){
			try {
				Thread.sleep(500);//��һ�����ݵ�ͣ��ʱ�䣬������̫��ֱ��˲��ص�����TM�ⶼ���ǵ��ˣ���ֱ����
			} catch (Exception e) {
				e.printStackTrace();
			}		
			progressDialog.dismiss();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();                                
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
		Log.d(TAG, "�������");
	}
	
	//�������ϻ�õ��������ݴ洢�����ݿ�,����м�һ����������ͷ���false,��ֻ����ʡ������
	public boolean saveAllinfo(){
		if(queryfromServer("province", null,-1)==false){
			return false;
		}
		else {
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Log.d(TAG, "���ڶ�ȡ"+province_list.get(i).getName());
				Message message=new Message();
				message.what=UPDATE_PROGRESS;
				progress_value++;
				handler.sendMessage(message);
				//id����ʵ����ô������ݿ�ʱҪ�õã������ʱҪ��ʡ�ݵ�id
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
		case "getWeatherCode"://��ȡ������
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";	
			break;
		case "weather":	//��ȡ������Ϣ
			address="http://www.weather.com.cn/data/cityinfo/"+code+".html";
			break;
		case "province"://��ȡʡ���б�
			address="http://www.weather.com.cn/data/list3/city.xml";
			break;
		case "city"://��ȡ�м��б�
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
					(response, "getWeatherCode", coolweather_db, -1);//-1��ʾ�������û��		
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
	 * ������ȫ�������б������ݿ���߳���
	 * */
	class Update_cityinfoThread implements Runnable{
		@Override
		public void run() {
			boolean result=saveAllinfo();
			//������Message���ظ����̣߳��������̴߳�����
			Message message=new Message();	
			if(result==true){				
				message.what=UPDATE_CITYINFO_SUCCESS;		
				county_code=coolweather_db.getcity(county_name).get(0).getCode();//����ֱ�Ӹ��ô��룬ȡ��һ�����о���	
				//���ڲ����п���ֱ�ӵ���getSharedPreferences����
				SharedPreferences location_pre=getSharedPreferences("location_pre", MODE_PRIVATE);
				SharedPreferences.Editor editor=location_pre.edit();
				editor.putString("locationName", county_name);//����λ�õĳ������浽pre��
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
	 *��������������Ϣ���߳���
	 */
	class Update_weatherinfoThread implements Runnable{
		@Override
		public void run() {
			if(TextUtils.isEmpty(county_code)){//�ȴ�countycode
				try {
					Thread t1=new Thread(new Update_cityinfoThread());//���߳�һ��ִ�н�������Ϊû��countycode
					t1.start();
					t1.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "countycodeֵΪ"+county_code);
			getweather_code(county_code);//�����Ǹ��߳�ִ���꣬����county_code��ֵ��
			showWeather(weather_code);			
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
			builder.setTitle("�鿴Դ�뼰BUG����");
			builder.setCancelable(false);
			builder.setMessage("�鿴Դ�������github.com/liu-mengwei  "
					+ "BUG��������ϵ����΢��lmw-1223,�����ʼ���542221757@qq.com");
			builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
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
