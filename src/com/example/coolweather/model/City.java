package com.example.coolweather.model;

public class City {

	private int id;//数据库中的编号
	private String name;//名称
	private int code;//网络中返回的城市编码
	private int province_id;//外键，对应于数据库中哪个省份
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getProvince_id() {
		return province_id;
	}
	public void setProvince_id(int province_id) {
		this.province_id = province_id;
	}	
}
