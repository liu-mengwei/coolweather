package com.example.coolweather.database;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;

public class Mydatabase{
	
	public static Mydatabase mydatabase;
	public SQLiteDatabase coolweather;
	
	private Mydatabase(Context context) {
		MyDatabaseHelper helper=new MyDatabaseHelper(context, "coolweather.db", null, 1);
		coolweather=helper.getReadableDatabase();	
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
		coolweather.insert("province", null, values);	
	}
	
	public void savecity(City city){
		ContentValues values=new ContentValues();
		values.put("name", city.getName());
		values.put("code", city.getCode());
		values.put("province_id", city.getProvince_id());
		coolweather.insert("city", null, values);
	}
	
	public void savecounty(County county){
		ContentValues values=new ContentValues();
		values.put("name", county.getName());
		values.put("code", county.getCode());
		values.put("city_id", county.getCity_id());
		coolweather.insert("county", null, values);	
	}
	
	public List<Province> getALLprovince(){
		Cursor cursor=coolweather.query("province", null, null, null, null, null, null);
		ArrayList<Province> list=new ArrayList<Province>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));	
				province.setName(cursor.getString(cursor.getColumnIndex("name")));
				province.setCode(cursor.getInt(cursor.getColumnIndex("code")));			
				list.add(province);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}
	
	public List<City> getALLcity(){
		Cursor cursor=coolweather.query("city", null, null, null, null, null, null);
		ArrayList<City> list=new ArrayList<City>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setName(cursor.getString(cursor.getColumnIndex("name")));
				city.setCode(cursor.getInt(cursor.getColumnIndex("code")));					
				city.setProvince_id(cursor.getColumnIndex("province_id"));
				list.add(city);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}
	
	public List<County> getALLcounty(){
		Cursor cursor=coolweather.query("county", null, null, null, null, null, null);
		ArrayList<County> list=new ArrayList<County>();
		//如果移动成功证明有数据
		if(cursor.moveToFirst()){
			do {
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setName(cursor.getString(cursor.getColumnIndex("name")));
				county.setCode(cursor.getInt(cursor.getColumnIndex("code")));					
				county.setCity_id(cursor.getColumnIndex("city_id"));
				list.add(county);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

}
