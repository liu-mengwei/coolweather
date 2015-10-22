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
	private boolean service_tag=false;//��Ҫ����service�ı�ʶ
	private boolean locate_tag=false;//��λʧ�ܵı�ʶ,Ĭ��ʧ��
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
				Toast.makeText(WeatherActivity.this, "��������ʧ��(>�n<)...�밴���Ͻ�ˢ�¼�����", Toast.LENGTH_SHORT).show();
				isFirstUsed=true;
				closeprogress();
				break;
			case UPDATE_PROGRESS://ֻ��������ؽ��ȿ�Ҳ������-----
				progressDialog.setProgress(progress_value);
				break;		
			case LOCATE_FAIL:		
				Toast.makeText(WeatherActivity.this, "��λʧ��(>�n<)...���˳���������", Toast.LENGTH_SHORT).show();
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
		Log.d(TAG, "�������");
		//��ȡʵ��
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

		//����Ƿ��ǵ�һ��ʹ�������������򱣴��������ݵ����ݿ�
		isfirstused_pre=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=isfirstused_pre.getBoolean("isFirstUsed", true);	
		String tag="need";

		if(isFirstUsed){//���ݿ��п��������ݣ���Ϊ���ܴ���һ�������ˣ����Ѿ�����������,Ϊ�˷�ֹ�ظ���ɾ��֮ǰ���������
			findViewById(R.id.tommorow).setVisibility(View.INVISIBLE);
			findViewById(R.id.after).setVisibility(View.INVISIBLE);
			findViewById(R.id.nowweather).setVisibility(View.INVISIBLE);
			suggestion.setVisibility(View.INVISIBLE);
			coolweather_db.clearDatabase();
			showprogress("update_cityinfo");	
			
			//������λ
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
					new Thread(new Update_weatherinfoThread()).start();//�����������ݵ��߳�						
				}
			});
		}	

		else {//������ǵ�һ��ʹ�����������countycode
			isFirstUsed=false;
			from=getIntent();
			county_code=from.getStringExtra("county_code");//���Ǵ��б����л�������
			county_name=from.getStringExtra("county_name");
			weather_code=from.getStringExtra("weather_code");
			tag=from.getStringExtra("tag");
			if(county_code==null){//�Լ��ĳ���(�����ʱ)
				service_tag=true;
				location_pre=getSharedPreferences("location_pre", MODE_PRIVATE);
				county_name=location_pre.getString("locationName", "");		
				county_code=location_pre.getString("locationCode", "");//��ʱ��϶���countycode,
				tag="need";		
				//������λ(���絽�����³��У����һ�򿪾���ʾ��������)
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
			new Thread(new Update_weatherinfoThread()).start();//���������߳�							
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
	
	//��Ϊ�ٶȵĶ�λ��ʱ��֪Ϊ����ʧ�ܣ�ֻ�������·����Ȱѽ��ȿ�ص�
	private void startTiming() {
		TimerTask task=new TimerTask() {		
			@Override
			public void run() {
				if(locate_tag==false){//��λʧ��
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

		//��this��ʾ��view
		this.weather_title.setText(county_name);
		this.tmp.setText(tmp+"��");
		this.weahter_describe.setText(weather_describe);
		this.quality.setText("����������"+quality);
		this.tormmorw_de.setText(tommorow_de);
		this.after_de.setText(after_de);
		this.update_time.setText(update_time.split(" ")[1]+"����");
		this.tommorow_tmp.setText(tmp1+"~"+tmp2+"��");
		this.after_tmp.setText(after_tmp1+"~"+after_tmp2+"��");

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
			progressDialog.setMessage("���ڼ��س����б�.....(>�n<)\n��ʧ�����л���WIFI����������Ͻ�ˢ�°�ť\n�鿴Դ���밴�˵���");
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
			address="https://api.heweather.com/x3/weather?cityid="+"CN"+weather_code+"&key"
					+ "=e2c80cc8f31a4189b0621adfd4813193";
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
			if(result==true&&county_name!=null){				
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
			//��������Ǳ����û����õ�ѡ��
			boolean button_state=getSharedPreferences("button_state", MODE_PRIVATE).getBoolean("button_state", true);
			if(service_tag==true&&button_state==true){
				Log.d(TAG, "service����");		
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
			builder.setTitle("�鿴Դ�뼰BUG����");
			builder.setCancelable(false);
			builder.setMessage("�鿴Դ�������github.com/liu-mengwei (version4.0��֧)�� "
					+ "BUG��������ϵ����΢��lmw-1223,�����ʼ���542221757@qq.com");
			builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {			
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
