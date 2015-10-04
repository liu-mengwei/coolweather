package com.example.coolweather.database;

import java.util.ArrayList;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
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
	
	
	
	

}
