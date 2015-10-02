package com.example.coolweather.model;

public class County {

	private int id;
	private String name;
	private String code;
	private int city_id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getCity_id() {
		return city_id;
	}
	public void setCity_id(int city_id) {
		this.city_id = city_id;
	}
	
	@Override
	public String toString() {
		return this.id+this.name+this.code+"city_id:"+this.city_id;
	}
	
}
