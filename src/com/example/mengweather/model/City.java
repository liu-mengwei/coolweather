package com.example.mengweather.model;

public class City {

	private int id;//���ݿ��еı��
	private String name;//����
	private String code;//�����з��صĳ��б���
	private int province_id;//�������Ӧ�����ݿ����ĸ�ʡ��
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
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
	
	@Override
	public String toString() {
		return this.id+this.name+this.code+"/provinceid:"+this.province_id;
	}
}
