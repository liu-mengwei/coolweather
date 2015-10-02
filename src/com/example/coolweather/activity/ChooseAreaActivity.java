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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	
	public static final String TAG="ChooseAreaActivity";

	public static final int PROVINCE=0;
	public static final int CITY=1;
	public static final int COUNTY=2;
	public static int current_level;

	ListView listview;
	TextView title;
	ProgressDialog progressDialog;
	
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

	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chooselayout);
		listview=(ListView) findViewById(R.id.listview);
		title=(TextView) findViewById(R.id.title);
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
					selected_province_id=province_list.get(position).getId();//------这里province_list还没有得到实例注意
					Log.d(TAG, "所选省份号码"+selected_province_id);
					selected_province=province_list.get(position).getName();
					selected_province_code=province_list.get(position).getCode();//code用于网络请求
					current_level=CITY;
					querycity();					
				}
				else if (current_level==CITY) {				
					selected_city_id=city_list.get(position).getId();//------这里city_list还没有得到实例注意		
					Log.d(TAG, "所选城市号码"+selected_city_id);					
					selected_city=city_list.get(position).getName();
					selected_city_code=city_list.get(position).getCode();				
					current_level=COUNTY;
					Log.d(TAG, selected_city_id+"");
					querycounty();
				}
			}
		});		
		queryprovince();		
	}

	public void queryprovince() {
		province_list=coolweather_db.getALLprovince();
		current_level=PROVINCE;	
		//先从数据库中查找数据，如果没有从网络中查
		if(province_list.isEmpty()==false){
			datalist.clear();
			for(int i=0;i<province_list.size();i++){
				datalist.add(province_list.get(i).getName());				
			}
			title.setText("中国");
			adapter.notifyDataSetChanged();
			listview.setSelection(0);		
		}
		else {
			queryfromServer("province",-1);//-----------可能要改变参数
		}
	}

	public void querycity() {
		current_level=CITY;
		city_list=coolweather_db.getALLcity(selected_province_id);	
		if(city_list.isEmpty()==false){
			datalist.clear();
			for(int i=0;i<city_list.size();i++){
				datalist.add(city_list.get(i).getName());
			}					
			title.setText(selected_province);
			adapter.notifyDataSetChanged();
			listview.setSelection(0);					
		}
		else {
			queryfromServer("city",selected_province_id);
		}	
	}

	public void querycounty() {
		current_level=COUNTY;
		county_list=coolweather_db.getALLcounty(selected_city_id);		
		if(county_list.isEmpty()==false){
			datalist.clear();
			for(int i=0;i<county_list.size();i++){
				datalist.add(county_list.get(i).getName());
			}
			title.setText(selected_city);
			adapter.notifyDataSetChanged();
			listview.setSelection(0);	
		}
		else {
			queryfromServer("county",selected_city_id);
		}	
	}


	//参数给httpHandleMessage用
	public void queryfromServer(final String type,final int id){	
		//显示进度框
		showprogress();	
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
				// TODO 自动生成的方法存根
				HttpHandler.httpHandleMessage(response,type,coolweather_db,id);
				//回到主线程
				runOnUiThread(new Runnable() {
					public void run() {
						closeprogress();
						if(type.equals("province")){
							queryprovince();
						}
						else if (type.equals("city")) {
							querycity();
						}
						else if (type.equals("county")) {
							querycounty();
						}					
					}
				});
			}
			
			@Override
			public void onerror(Exception e) {
				// TODO 自动生成的方法存根
				e.printStackTrace();
				runOnUiThread(new Runnable() {
					public void run() {
						closeprogress();
						Toast.makeText(ChooseAreaActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
					}
				});			
			}
		});
	}	
	
	private void showprogress() {
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("正在读取数据...");
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
		if(current_level==PROVINCE){
			finish();
		}
		else if (current_level==CITY) {
			queryprovince();
		}
		else if (current_level==COUNTY) {
			querycity();
		}		
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "ondestory");		
	}
}
