package com.example.coolweather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper{

	public static final String CREATE_PROVINCE="create table province("
			+ "id integer primary key autoincrement,"
			+ "name text,"
			+ "code integer)";
	
	public static final String CREATE_CITY="create table city("
			+ "id integer primary key autoincrement,"
			+ "name text,"
			+ "code integer,"
			+ "province_id integer)";
	
	public static final String CREATE_COUNTY="create table county("
			+ "id integer primary key autoincrement,"
			+ "name text,"
			+ "code integer,"
			+ "city_id integer)";
	

	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {	
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
