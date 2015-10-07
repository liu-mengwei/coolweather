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
import com.example.coolweather.util.MyLocation;
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
import android.text.TextUtils;
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
	public static final int UPDATE_PROGRESS=4;

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
	private int progress_value=0;//�������и��½��ȿ����

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
	//���������ص���Ϣ
	Handler handler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case UPDATE_WEATHERINFO_SUCCESS:
				updateUI();				
				Toast.makeText(WeatherActivity.this, "����������Ϣ�ɹ� (^_^)", Toast.LENGTH_SHORT).show();
				closeprogress();
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
				Toast.makeText(WeatherActivity.this, "�����б�ɹ�(^_^)", Toast.LENGTH_SHORT).show();
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
		publish_time=(TextView) findViewById(R.id.publish_time);
		weather_image=(ImageView) findViewById(R.id.weather_image);
		temp1=(TextView) findViewById(R.id.temp1);
		temp2=(TextView) findViewById(R.id.temp2);
		weahter_describe=(TextView) findViewById(R.id.weather_describe);
		date=(TextView) findViewById(R.id.date);
		//������˸��̳߳�ȥ��û��ִ������䣬������Ѿ�ִ���ˣ�����province_listΪ��,�Ҳ����bug����������Сʱ	
		//����Ƿ��ǵ�һ��ʹ�������������򱣴��������ݵ����ݿ�
		isfirstused_pre=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=isfirstused_pre.getBoolean("isFirstUsed", true);	
		t1=new Thread(new update_cityinfoThread());
		t2=new Thread(new update_weatherinfoThread());	
		if(isFirstUsed){//���ݿ��п��������ݣ���Ϊ���ܴ���һ�������ˣ����Ѿ�����������,Ϊ�˷�ֹ�ظ���ɾ��֮ǰ���������
			coolweather_db.clearDatabase();
			showprogress("update_cityinfo");	
			t1.start();
		}		
		else {//������ǵ�һ��ʹ�����������countycode
			from=getIntent();
			county_code=from.getStringExtra("county_code");
			county_name=from.getStringExtra("county_name");
			updateUI();
			showprogress("update_weather");		
		}		
		t2.start();		
		//--------��ť���¼�
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
		//��this��ʾ��view
		this.weather_title.setText(county_name);
		this.temp1.setText(temp2);//��Ϊ���������ص�temp1�Ǹ��£����Ի�һ��
		this.temp2.setText(temp1);
		this.weahter_describe.setText(weather_describe);
		this.publish_time.setText("����"+update_time+"����");
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
			progressDialog.setMessage("���ڼ��س����б�...");
		}
		progressDialog.show();
	}

	private void closeprogress(){
		if(progressDialog!=null){
			Log.d("ChooseAreaActivity", "�ɹ��رս��ȿ�");
			try {
				Thread.sleep(500);//��һ�����ݵ�ͣ��ʱ�䣬������̫��ֱ��˲��ص�����TM�ⶼ���ǵ��ˣ���ֱ����
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
		Log.d(TAG, "�������");
	}

	//�������ϻ�õ��������ݴ洢�����ݿ�,����м�һ����������ͷ���false
	public boolean saveAllinfo(){
		if(queryfromServer("province", null,-1)){
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Log.d(TAG, "���ڶ�ȡ"+province_list.get(i).getName());
				Message message=new Message();
				message.what=UPDATE_PROGRESS;
				progress_value++;
				handler.sendMessage(message);
				if(queryfromServer("city",null, province_list.get(i).getId())){//id����ʵ����ô������ݿ�ʱҪ�õã������ʱҪ��ʡ�ݵ�id
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
		case "county"://��ȡ�ؼ��б�
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
					(response, "getWeatherCode", coolweather_db, -1);//-1��ʾ�������û��		
					break;
				case "weather":	
					Log.d(TAG, "��response�����Ƕ���"+response);
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

	/**
	 * ������ȫ�������б������ݿ���߳���
	 * */
	class update_cityinfoThread implements Runnable{
		@Override
		public void run() {
			boolean result=saveAllinfo();
			//������Message���ظ����̣߳��������̴߳�����
			Message message=new Message();	
			if(result==true){				
				message.what=UPDATE_CITYINFO_SUCCESS;		
				//��λ����д���������һ����������				
				county_name=MyLocation.getLocationName(WeatherActivity.this);			
				if(TextUtils.isEmpty(county_name)){
					county_code=coolweather_db.getcounty(county_name).get(0).getCode();//����ֱ�Ӹ��ô��룬ȡ��һ�����о���	
				}				
			}
			else {
				message.what=UPDATE_CITYINFO_FAIL;
			}
			handler.sendMessage(message);
		}
	}

	/**
	 *��������������Ϣ���߳���
	 */
	class update_weatherinfoThread implements Runnable{
		@Override
		public void run() {
			if(county_code.equals("")){//�ȴ�countycode
				try {
					t1.join();//���߳�һ��ִ�н�������Ϊû��countycode
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "countycodeֵΪ"+county_code);
			if(TextUtils.isEmpty(county_code)==false){
				getweather_code(county_code);//�������������weather_code��ֵ
			}		
			Log.d(TAG, "weathercodeֵΪ"+weather_code);
			showWeather(weather_code);					
		}		
	}





}
