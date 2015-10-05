package com.example.coolweather.activity;


import java.util.ArrayList;

import com.example.coolweather.R;
import com.example.coolweather.R.layout;
import com.example.coolweather.database.Mydatabase;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpHandler;
import com.example.coolweather.util.HttpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{

	public static final String TAG="ChooseAreaActivity";
	public static final int SUCCESS=0;
	public static final int FAIL=1;
	public static final int UPDATEPROGRESS=2;
	public static final int PROVINCE=0;
	public static final int CITY=1;
	public static final int COUNTY=2;
	public static int current_level;

	ListView listview;
	TextView title;
	ProgressDialog progressDialog;
	Button back;
	Button search;

	String selected_province;
	String selected_city;
	int selected_province_id;
	String selected_province_code;
	String selected_city_code;
	String selected_county_code;
	int selected_city_id;

	ArrayList<Province> province_list;
	ArrayList<City> city_list;
	ArrayList<County> county_list;
	ArrayList<String> datalist=new ArrayList<String>();
	ArrayAdapter<String> adapter;

	Mydatabase coolweather_db;

	boolean isFirstUsed=true;
	SharedPreferences preferences;

	Handler handler=new Handler(){

		public void handleMessage(android.os.Message msg) {
			if(msg.what==SUCCESS){
				closeprogress();
				queryprovince(null);
				isFirstUsed=false;
				SharedPreferences.Editor editor=preferences.edit();
				editor.putBoolean("isFirstUsed", false);
				editor.commit();
				Toast.makeText(ChooseAreaActivity.this, "加载列表成功(^_^)", Toast.LENGTH_SHORT);
			}			
			else if(msg.what==FAIL){
				closeprogress();
				Toast.makeText(ChooseAreaActivity.this, "网络连接失败(>﹏<)", Toast.LENGTH_SHORT).show();
				isFirstUsed=true;
			}
			else if (msg.what==UPDATEPROGRESS) {
				progressDialog.setProgress(progress_value);
			}
		};
	};


	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chooselayout);
		listview=(ListView) findViewById(R.id.listview);
		title=(TextView) findViewById(R.id.title);
		back=(Button) findViewById(R.id.back);
		search=(Button) findViewById(R.id.search);

		back.setOnTouchListener(new OnTouchListener() {	

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.back2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.back1);
					back();
				}
				return false;		
			}
		});

		search.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					v.setBackgroundResource(R.drawable.search2);
				}
				else if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.search1);
					showSearchDialogue();	
				}
				return false;
			}
		});


		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		listview.setAdapter(adapter);
		Log.d(TAG, "当前等级为"+current_level+"oncreate");
		coolweather_db=Mydatabase.getdatabase(this);

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//如果当前等级为0是(默认为0)，按了一下说明查询城市数据
				if(current_level==PROVINCE){
					//先从数据库中查
					selected_province_id=province_list.get(position).getId();
					Log.d(TAG, "所选省份号码"+selected_province_id);
					selected_province=province_list.get(position).getName();
					selected_province_code=province_list.get(position).getCode();//code用于网络请求
					current_level=CITY;
					querycity(null);					
				}
				else if (current_level==CITY) {				
					selected_city_id=city_list.get(position).getId();	
					Log.d(TAG, "所选城市号码"+selected_city_id);					
					selected_city=city_list.get(position).getName();
					selected_city_code=city_list.get(position).getCode();				
					current_level=COUNTY;
					Log.d(TAG, selected_city_id+"");
					querycounty(null);
				}
			}
		});		

		//这里分了个线程出去，没有执行完这句，后面就已经执行了，所以province_list为空,我草这个bug查了两个多小时	
		//检查是否是第一次使用软件，如果是则保存所有数据到数据库
		Log.d(TAG, "isFirstUsed"+isFirstUsed);//因为这里用到了多线程，所以这行先被执行
		preferences=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=preferences.getBoolean("isFirstUsed", true);	

		if(isFirstUsed){			

			coolweather_db.clearDatabase();//数据库中可能有数据，因为可能存了一办网断了，但已经存入了数据,为了防止重复，
			//删除之前存入的数据
			showprogress();
			new Thread(new Runnable() {			
				@Override
				public void run() {
					boolean result=saveAllinfo();	
					//这里用Message返回给主线程，告诉主线程处理结果
					Message message=new Message();	
					if(result==true){				
						message.what=SUCCESS;
					}
					else {
						message.what=FAIL;
					}
					handler.sendMessage(message);
				}
			}).start();		
		}
		else {
			queryprovince(null);
		}
	}

	private int progress_value=0;//用来进行更新进度框操作
	//将从网上获得的所有数据存储到数据库,如果中间一环出了问题就返回false
	public boolean saveAllinfo(){
		if(queryfromServer("province", -1)){
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Message message=new Message();
				message.what=UPDATEPROGRESS;
				progress_value++;
				handler.sendMessage(message);
				if(queryfromServer("city", province_list.get(i).getId())){//id号其实是最好存入数据库时要用得，存城市时要有省份的id
					for(int j=0;j<city_list.size();){                              //这里已经得到了citylist
						selected_city_code=city_list.get(j).getCode();
						selected_city_id=city_list.get(j).getId();			
						if(queryfromServer("county", city_list.get(j).getId())){			
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

	/**
	 * 从数据库中查询数据并显示数据
	 * @param province_name 用来具体查询某一个省份名称，如果传进来null,则查询所有城市
	 */
	public boolean queryprovince(String province_name) {
		current_level=PROVINCE;	
		if(province_name==null){
			province_list=coolweather_db.getALLprovince();
		}
		else {
			province_list=coolweather_db.getprovince(province_name);
		}			
		if(province_list.isEmpty()==true){//如果为空说明没有查询到数据
			return false;
		}		
		//如果不是第一次使用数据库中肯定有数据----下面是用来显示数据到屏幕上的
		datalist.clear();
		Log.d(TAG, province_list.get(0).getCode());
		for(int i=0;i<province_list.size();i++){
			datalist.add(province_list.get(i).getName());
			Log.d(TAG, province_list.get(i).getCode());
		}
		title.setText("中国");
		adapter.notifyDataSetChanged();
		listview.setSelection(0);	
		return true;	
	}

	public boolean querycity(String city_name) {
		current_level=CITY;
		if(city_name==null){
			city_list=coolweather_db.getALLcity(selected_province_id);	
		}
		else {
			city_list=coolweather_db.getcity(city_name);
		}	
		if(city_list.isEmpty()==true){
			return false;
		}		
		//如果不是第一次使用数据库中肯定有数据----下面是用来显示数据到屏幕上的
		datalist.clear();
		for(int i=0;i<city_list.size();i++){
			datalist.add(city_list.get(i).getName());
		}					
		String province_name=coolweather_db.getprovince(city_list.get(0).getProvince_id());
		if(province_name.equals(selected_province)){
			title.setText(selected_province);
		}
		else {
			title.setText(city_name);
		}
		adapter.notifyDataSetChanged();
		listview.setSelection(0);					
		return true;
	}

	public boolean querycounty(String county_name) {
		current_level=COUNTY;	
		if(county_name==null){
			county_list=coolweather_db.getALLcounty(selected_city_id);		
		}
		else {
			county_list=coolweather_db.getcounty(county_name);
		}	

		if(county_list.isEmpty()==true){//如果为空说明没有查询到数据
			return false;
		}
		else {
			datalist.clear();
			for(int i=0;i<county_list.size();i++){
				datalist.add(county_list.get(i).getName());
				Log.d(TAG, county_list.get(i).getName());
			}
			title.setText(selected_city);//-----------
			adapter.notifyDataSetChanged();
			listview.setSelection(0);	
			return true;
		}
	}


	//isbackinfo给httpHandleMessage用
	boolean isbackinfo=false;

	public boolean queryfromServer(final String type,final int id){	
		//显示进度框
		//showprogress();	
		String address = null;
		if(type.equals("province")){
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		else if (type.equals("city")) {
			Log.d(TAG, selected_province_code+"");
			address="http://www.weather.com.cn/data/list3/city"+selected_province_code+".xml";		
		}
		else if (type.equals("county")) {
			address="http://www.weather.com.cn/data/list3/city"+selected_city_code+".xml";
		}

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onhandle(String response) {
				HttpHandler.httpHandleMessage(response,type,coolweather_db,id);
				//回到主线程
				if(type.equals("province")){
					province_list=coolweather_db.getALLprovince();
				}
				else if (type.equals("city")) {
					city_list=coolweather_db.getALLcity(selected_province_id);
				}
				else if (type.equals("county")) {
					county_list=coolweather_db.getALLcounty(selected_city_id);
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
		progressDialog.setMax(34);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("正在加载城市列表...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
		Log.d(TAG, "按回退键");
		back();
	}	

	private void back(){
		Log.d(TAG, "按回退键");
		if(current_level==PROVINCE){
			finish();
		}
		else if (current_level==CITY) {
			queryprovince(null);
		}
		else if (current_level==COUNTY) {
			querycity(null);
		}		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "ondestory");		
	}


	private void showSearchDialogue(){
		LayoutInflater inflater=LayoutInflater.from(this);
		//动态加载布局-------------这里有问题
		LinearLayout dialogue_layout=(LinearLayout) inflater.inflate(R.layout.dialogue, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setView(dialogue_layout);
		final AlertDialog alertDialog=builder.create();
		alertDialog.show();
		final EditText city_text=(EditText) dialogue_layout.findViewById(R.id.cityname);

		Button ok=(Button) dialogue_layout.findViewById(R.id.cityname_ok);
		ok.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) { 				
				//-----正在查询
				alertDialog.dismiss();
				String city_name=city_text.getText().toString();
				if(city_name.equals("")){
					Toast.makeText(ChooseAreaActivity.this, "输入不能为空(>﹏<)", Toast.LENGTH_SHORT).show();
					return;
				}
				//三级查询
				if(querycity(city_name)==false){
					if(querycounty(city_name)==false){
						if(queryprovince(city_name)==false){
							queryprovince(null);
							Toast.makeText(ChooseAreaActivity.this, "未找到相关城市 (>﹏<) ", Toast.LENGTH_SHORT).show();						
						}
					}
				}
			}
		});
		
		Button cancle=(Button) dialogue_layout.findViewById(R.id.cityname_cancle);
		cancle.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();			
			}
		});
		
	}
}
