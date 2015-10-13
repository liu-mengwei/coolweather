package com.example.mengweather.activity;



import java.util.ArrayList;

import com.example.mengweather.R;
import com.example.mengweather.database.Mydatabase;
import com.example.mengweather.model.City;
import com.example.mengweather.model.County;
import com.example.mengweather.model.Province;
import com.example.mengweather.util.HttpCallbackListener;
import com.example.mengweather.util.HttpHandler;
import com.example.mengweather.util.HttpUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

public class ChooseAreaActivity extends BaseActivity{

	public static final String TAG="ChooseAreaActivity";
	public static final int UPDATE_COUNTYINFO_SUCCESS=0;
	public static final int UPDATE_COUNTYINFO_FAIL=1;
	public static final int PROVINCE=0;
	public static final int CITY=1;
	public static final int COUNTY=2;
	
	private boolean isbackinfo=false;
	public static int current_level;
	private ListView listview;
	private TextView title;
	private Button back;
	private Button search;

	int selected_province_id;
	String selected_province_name;
	String selected_province_code;

	int selected_city_id;
	String selected_city_name;
	String selected_city_code;

	int selected_county_id;
	String selected_county_name;
	String selected_county_code;

	ArrayList<Province> province_list;
	ArrayList<City> city_list;
	ArrayList<County> county_list;
	ArrayList<String> datalist=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	Mydatabase coolweather_db;
	
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case UPDATE_COUNTYINFO_FAIL:
				Toast.makeText(ChooseAreaActivity.this, "更新区县列表失败(>﹏<)", Toast.LENGTH_SHORT).show();
				break;
			case UPDATE_COUNTYINFO_SUCCESS:
				querycounty(null);				
			default:
				break;
			}				
		};
	};
	
	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chooselayout);
		Log.d(TAG, "活动一创建");
		listview=(ListView) findViewById(R.id.listview);
		title=(TextView) findViewById(R.id.title);	
		coolweather_db=Mydatabase.getdatabase(this);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		queryprovince(null);
			
		back=(Button) findViewById(R.id.back);
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
		
		search=(Button) findViewById(R.id.search);
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
		
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//如果当前等级为0是(默认为0)，按了一下说明查询城市数据
				if(current_level==PROVINCE){
					//先从数据库中查
					selected_province_id=province_list.get(position).getId();
					Log.d(TAG, "所选省份号码"+selected_province_id);
					selected_province_name=province_list.get(position).getName();
					selected_province_code=province_list.get(position).getCode();//code用于网络请求
					current_level=CITY;
					querycity(null);					
				}
				else if (current_level==CITY) {				
					selected_city_id=city_list.get(position).getId();	
					Log.d(TAG, "所选城市号码"+selected_city_id);					
					selected_city_name=city_list.get(position).getName();
					selected_city_code=city_list.get(position).getCode().substring(0, 4);		
					current_level=COUNTY;
					Log.d(TAG, selected_city_id+"");
					querycounty(null);
				}
				else if (current_level==COUNTY) {
					selected_county_name=county_list.get(position).getName();
					selected_county_code=county_list.get(position).getCode();
					Intent to=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					to.putExtra("county_name", selected_county_name);
					to.putExtra("county_code", selected_county_code);	
					to.putExtra("tag", "need");
					startActivity(to);		
					finish();
				}
			}
		});		
	}

	/**
	 * 从数据库中查询数据并显示数据
	 * @param province_name 用来具体查询某一个省份，如果传进来null,则查询所有城市
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
		//如果是模糊查询则要加入这个判断逻辑，用来更新title				
		String province_name=coolweather_db.getprovinceName(city_list.get(0).getProvince_id());
		if(province_name.equals(selected_province_name)){
			title.setText(selected_province_name);
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
			
			if(county_list.isEmpty()){//如果没有查询到，去网络查
				new Thread(new Update_countyinfoThread()).start();
			}
			
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
			String city_name=coolweather_db.getcityName(county_list.get(0).getCity_id());
			if(city_name.equals(selected_city_name)){
				title.setText(selected_city_name);
			}
			else {
				title.setText(county_name);
			}			
			adapter.notifyDataSetChanged();
			listview.setSelection(0);	
			return true;
		}
	}

	private boolean queryfromServer() {
		String address="http://www.weather.com.cn/data/list3/city"+selected_city_code+".xml";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {			
			@Override
			public void onhandle(String response) {
				HttpHandler.httpHandleMessage(response, "county", coolweather_db, selected_city_id);
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

	@Override
	public void onBackPressed() {
		Log.d(TAG, "按回退键");
		back();
	}	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "活动一销毁");		
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
					Toast.makeText(ChooseAreaActivity.this, "输入不能为空 (>﹏<)", Toast.LENGTH_SHORT).show();
					return;
				}
				//三级查询，这里因为后面做了优化，只把城市的数据读进去了，所以县的数据就没有
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

	private void back(){
		Log.d(TAG, "按回退键");
		if(current_level==PROVINCE){
			Intent to=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
			to.putExtra("county_code",getIntent().getStringExtra("county_code"));
			to.putExtra("tag", "Noneed");//表示让WeatherActivity不用再更新天气的标识
			startActivity(to);
			finish();
		}
		else if (current_level==CITY) {
			queryprovince(null);
		}
		else if (current_level==COUNTY) {
			querycity(null);
		}		
	}

	class Update_countyinfoThread implements Runnable{
		@Override
		public void run() {
			boolean result=queryfromServer();
			Message message=new Message();
			if(result==false){
				message.what=UPDATE_COUNTYINFO_FAIL;
				handler.sendMessage(message);				
			}
			else {
				message.what=UPDATE_COUNTYINFO_SUCCESS;
				handler.sendMessage(message);
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
			AlertDialog.Builder builder=new AlertDialog.Builder(ChooseAreaActivity.this);
			builder.setTitle("查看源码及BUG反馈");
			builder.setCancelable(false);
			builder.setMessage("查看源码请访问github.com/liu-mengwei  "
					+ "BUG反馈请联系作者微信lmw-1223,或发送邮件到542221757@qq.com");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {			
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
		
}
