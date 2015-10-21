package com.example.mengweather.database;

import java.util.ArrayList;

import com.example.mengweather.model.City;
import com.example.mengweather.model.County;
import com.example.mengweather.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Mydatabase{

	private static Mydatabase mydatabase;
	public SQLiteDatabase database;

	private Mydatabase(Context context) {
		MyDatabaseHelper helper=new MyDatabaseHelper(context, "coolweather.db", null, 1);
		database=helper.getReadableDatabase();	
	}

	public static Mydatabase getdatabase(Context context){
		if(mydatabase==null){
			mydatabase=new Mydatabase(context);
		}
		return mydatabase;	
	}

	public void saveprovince(Province province){
		ContentValues values=new ContentValues();
		values.put("name", province.getName());
		values.put("code", province.getCode());
		database.insert("province", null, values);	
	}

	public void savecity(City city){
		ContentValues values=new ContentValues();
		values.put("name", city.getName());
		values.put("code", city.getCode());
		values.put("province_id", city.getProvince_id());
		database.insert("city", null, values);
	}

	public void savecounty(County county){
		ContentValues values=new ContentValues();
		values.put("name", county.getName());
		values.put("code", county.getCode());
		values.put("city_id", county.getCity_id());
		database.insert("county", null, values);	
	}

	public ArrayList<Province> getALLprovince(){
		Cursor cursor=database.query("province", null, null, null, null, null, null);
		ArrayList<Province> list=new ArrayList<Province>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));	
				province.setName(cursor.getString(cursor.getColumnIndex("name")));
				province.setCode(cursor.getString(cursor.getColumnIndex("code")));			
				list.add(province);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	public ArrayList<City> getALLcity(int province_id){
		Cursor cursor=database.query("city", null, "province_id = ?", new String[]{String.valueOf(province_id)}, null, null, null);
		ArrayList<City> list=new ArrayList<City>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setName(cursor.getString(cursor.getColumnIndex("name")));
				city.setCode(cursor.getString(cursor.getColumnIndex("code")));					
				city.setProvince_id(cursor.getInt(cursor.getColumnIndex("province_id")));
				list.add(city);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	public ArrayList<County> getALLcounty(int city_id){
		Cursor cursor=database.query("county", null, "city_id = ?", new String[]{String.valueOf(city_id)}, null, null, null);
		ArrayList<County> list=new ArrayList<County>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setName(cursor.getString(cursor.getColumnIndex("name")));
				county.setCode(cursor.getString(cursor.getColumnIndex("code")));					
				county.setCity_id(cursor.getInt(cursor.getColumnIndex("city_id")));
				list.add(county);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	public void clearDatabase(){
		database.delete("province", null, null);
		database.delete("city", null, null);
		database.delete("county", null, null);		
	}

	public ArrayList<Province> getprovince(String province_name) {
		ArrayList<Province> provinces=new ArrayList<Province>();
		Cursor cursor=database.query("province", null, "name like ?", new String[]{province_name+"%"}, null, null, null);
		//如果查询成功
		if(cursor.moveToFirst()){	
			do {
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));	
				province.setName(cursor.getString(cursor.getColumnIndex("name")));
				province.setCode(cursor.getString(cursor.getColumnIndex("code")));			
				provinces.add(province);
			} while (cursor.moveToNext());
			cursor.close();
		}
		//如果输入的是北京市，查不出北京，我都醉了只能这样写了
		else{
			province_name=province_name.substring(0, province_name.length()-1);
			Cursor cursor2=database.query("province", null, "name = ?", new String[]{province_name}, null, null, null);
			if(cursor2.moveToFirst()){
				do {
					Province province=new Province();
					province.setId(cursor2.getInt(cursor2.getColumnIndex("id")));	
					province.setName(cursor2.getString(cursor2.getColumnIndex("name")));
					province.setCode(cursor2.getString(cursor2.getColumnIndex("code")));			
					provinces.add(province);			
				} while (cursor2.moveToNext());
				cursor2.close();
			}
		}		
		return provinces;
	}

	
	public ArrayList<City> getcity(String city_name) {	
		ArrayList<City> cities=new ArrayList<City>();
		Cursor cursor=database.query("city", null, "name like ?", new String[]{city_name+"%"}, null, null, null);	
		//如果查询成功
		if(cursor.moveToFirst()){	
			do {
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));	
				city.setName(cursor.getString(cursor.getColumnIndex("name")));
				city.setCode(cursor.getString(cursor.getColumnIndex("code")));		
				city.setProvince_id(cursor.getInt(cursor.getColumnIndex("province_id")));
				cities.add(city);
			}while (cursor.moveToNext());
			cursor.close();
		}
		//如果输入的是北京市，查不出北京，我都醉了只能这样写了
		else{
			city_name=city_name.substring(0, city_name.length()-1);
			Cursor cursor2=database.query("city", null, "name = ?", new String[]{city_name}, null, null, null);
			if(cursor2.moveToFirst()){
				do {
					City city=new City();
					city.setId(cursor2.getInt(cursor2.getColumnIndex("id")));	
					city.setName(cursor2.getString(cursor2.getColumnIndex("name")));
					city.setCode(cursor2.getString(cursor2.getColumnIndex("code")));	
					city.setProvince_id(cursor2.getInt(cursor2.getColumnIndex("province_id")));
					cities.add(city);
				} while (cursor2.moveToNext());
				cursor2.close();
			}
		}				
		return cities;		
	}
		
	
	public ArrayList<County> getcounty(String county_name) {		
		ArrayList<County> counties=new ArrayList<County>();
		Cursor cursor=database.query("county", null, "name like ?", new String[]{county_name+"%"}, null, null, null);	
		//如果查询成功
		if(cursor.moveToFirst()){	
			do {
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));	
				county.setName(cursor.getString(cursor.getColumnIndex("name")));
				county.setCode(cursor.getString(cursor.getColumnIndex("code")));		
				county.setCity_id(cursor.getInt(cursor.getColumnIndex("city_id")));
				counties.add(county);
			}while (cursor.moveToNext());
			cursor.close();
		}
		//如果输入的是北京市，查不出北京，我都醉了只能这样写了
		else{
			county_name=county_name.substring(0, county_name.length()-1);
			Cursor cursor2=database.query("county", null, "name = ?", new String[]{county_name}, null, null, null);
			if(cursor2.moveToFirst()){
				do {
					County county=new County();
					county.setId(cursor2.getInt(cursor2.getColumnIndex("id")));	
					county.setName(cursor2.getString(cursor2.getColumnIndex("name")));
					county.setCode(cursor2.getString(cursor2.getColumnIndex("code")));	
					county.setCity_id(cursor2.getInt(cursor2.getColumnIndex("city_id")));
					counties.add(county);
				} while (cursor2.moveToNext());
				cursor2.close();
			}
		}				
		return counties;		
	}
	
	/**
	 * 知道数据库省份id号返回省份名称
	 * @param province_id 省份id号
	 * @return
	 */
	public String getprovinceName(int province_id){
		Cursor cursor=database.query("province", null, "id = ?", new String[]{province_id+""}, null, null, null);
		cursor.moveToFirst();
		String province_name=cursor.getString(cursor.getColumnIndex("name"));		
		cursor.close();
		return province_name;
	}
	
	/**
	 * 知道城市id号返回城市名称
	 * @param city_id 城市id号
	 * @return
	 */
	public String getcityName(int city_id){
		Cursor cursor=database.query("city", null, "id = ?", new String[]{city_id+""}, null, null, null);
		cursor.moveToFirst();
		String city_name=cursor.getString(cursor.getColumnIndex("name"));
		cursor.close();
		return city_name;
	}
	
}
