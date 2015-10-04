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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
				queryprovince();
				isFirstUsed=false;
				SharedPreferences.Editor editor=preferences.edit();
				editor.putBoolean("isFirstUsed", false);
				editor.commit();
			}			
			else if(msg.what==FAIL){
				closeprogress();
				Toast.makeText(ChooseAreaActivity.this, "��������ʧ��", Toast.LENGTH_SHORT).show();
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
					//-------д��ѯ�߼�
					
				}
				return false;
			}
		});
		
		
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		listview.setAdapter(adapter);
		Log.d(TAG, "��ǰ�ȼ�Ϊ"+current_level+"oncreate");
		coolweather_db=Mydatabase.getdatabase(this);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//�����ǰ�ȼ�Ϊ0��(Ĭ��Ϊ0)������һ��˵����ѯ��������
				if(current_level==PROVINCE){
					//�ȴ����ݿ��в�
					selected_province_id=province_list.get(position).getId();
					Log.d(TAG, "��ѡʡ�ݺ���"+selected_province_id);
					selected_province=province_list.get(position).getName();
					selected_province_code=province_list.get(position).getCode();//code������������
					current_level=CITY;
					querycity();					
				}
				else if (current_level==CITY) {				
					selected_city_id=city_list.get(position).getId();	
					Log.d(TAG, "��ѡ���к���"+selected_city_id);					
					selected_city=city_list.get(position).getName();
					selected_city_code=city_list.get(position).getCode();				
					current_level=COUNTY;
					Log.d(TAG, selected_city_id+"");
					querycounty();
				}
			}
		});		

		//������˸��̳߳�ȥ��û��ִ������䣬������Ѿ�ִ���ˣ�����province_listΪ��,�Ҳ����bug����������Сʱ	
		//����Ƿ��ǵ�һ��ʹ�������������򱣴��������ݵ����ݿ�
		Log.d(TAG, "isFirstUsed"+isFirstUsed);//��Ϊ�����õ��˶��̣߳����������ȱ�ִ��
		preferences=getSharedPreferences("isFirstUsed", MODE_PRIVATE);
		isFirstUsed=preferences.getBoolean("isFirstUsed", true);	

		if(isFirstUsed){			
			
			coolweather_db.clearDatabase();//���ݿ��п��������ݣ���Ϊ���ܴ���һ�������ˣ����Ѿ�����������,Ϊ�˷�ֹ�ظ���
			                                                //ɾ��֮ǰ���������
			showprogress();
			new Thread(new Runnable() {			
				@Override
				public void run() {
					boolean result=saveAllinfo();	
					//������Message���ظ����̣߳��������̴߳�����
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
			queryprovince();
		}
	}

	private int progress_value=0;//�������и��½��ȿ����
	//�������ϻ�õ��������ݴ洢�����ݿ�,����м�һ����������ͷ���false
	public boolean saveAllinfo(){
		if(queryfromServer("province", -1)){
			for(int i=0;i<province_list.size();i++){
				selected_province_code=province_list.get(i).getCode();
				selected_province_id=province_list.get(i).getId();
				Message message=new Message();
				message.what=UPDATEPROGRESS;
				progress_value++;
				handler.sendMessage(message);
				if(queryfromServer("city", province_list.get(i).getId())){//id����ʵ����ô������ݿ�ʱҪ�õã������ʱҪ��ʡ�ݵ�id
					for(int j=0;j<city_list.size();){                              //�����Ѿ��õ���citylist
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

	public void queryprovince() {
		province_list=coolweather_db.getALLprovince();
		current_level=PROVINCE;	
		//�ȴ����ݿ��в������ݣ����û�д������в�
		if(province_list.isEmpty()==false){
			datalist.clear();
			Log.d(TAG, province_list.get(0).getCode());
			for(int i=0;i<province_list.size();i++){
				datalist.add(province_list.get(i).getName());
				Log.d(TAG, province_list.get(i).getCode());
			}
			title.setText("�й�");
			adapter.notifyDataSetChanged();
			listview.setSelection(0);		
		}
		else {
			queryfromServer("province",-1);//-----------����Ҫ�ı����
		}
	}

	public void querycity() {
		current_level=CITY;
		city_list=coolweather_db.getALLcity(selected_province_id);	
		//������ǵ�һ��ʹ�����ݿ��п϶�������----������������ʾ���ݵ���Ļ�ϵ�
		datalist.clear();
		for(int i=0;i<city_list.size();i++){
			datalist.add(city_list.get(i).getName());
		}					
		title.setText(selected_province);
		adapter.notifyDataSetChanged();
		listview.setSelection(0);					
	}

	public void querycounty() {
		current_level=COUNTY;
		county_list=coolweather_db.getALLcounty(selected_city_id);		
		datalist.clear();
		for(int i=0;i<county_list.size();i++){
			datalist.add(county_list.get(i).getName());
		}
		title.setText(selected_city);
		adapter.notifyDataSetChanged();
		listview.setSelection(0);	
	}


	//isbackinfo��httpHandleMessage��
	boolean isbackinfo=false;

	public boolean queryfromServer(final String type,final int id){	
		//��ʾ���ȿ�
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
				//�ص����߳�
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
		progressDialog.setMessage("���ڼ��س����б�...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.show();		
	}

	private void closeprogress(){
		if(progressDialog!=null){
			Log.d("ChooseAreaActivity", "�ɹ��رս��ȿ�");
			progressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "�����˼�");
		back();
	}	

	private void back(){
		Log.d(TAG, "�����˼�");
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
