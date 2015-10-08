package com.example.mengweather.activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityController {
	
	public static List<Activity> activities=new ArrayList<Activity>();
	
	public static void addActivity(Activity activity){
		activities.add(activity);
	}
	
	public static void removeActivity(Activity activity){
		activities.remove(activity);
	}
	
	public static void closeProcess(){
		for (Activity activity : activities) {
			if(activity.isFinishing()==false){
				activity.finish();
			}
		}
	}
	
}
