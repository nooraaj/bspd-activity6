package com.bsp.fsccis.bean.util;

public class PermHeader {

	private String name;
	private int rowSpan = 1;
	private int colSpan = 1;
	private Integer permId = 0;
	
	public PermHeader(String name){
		this.name = name;
	}
	
	public PermHeader(String name, int rowSpan, int colSpan){
		this.name = name;
		this.rowSpan = rowSpan;
		this.colSpan = colSpan;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRowSpan() {
		return rowSpan;
	}
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
	public int getColSpan() {
		return colSpan;
	}
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}
	
	@Override
	public String toString() {
		return this.name + ", " + colSpan + ", " + rowSpan;
	}

	public Integer getPermId() {
		return permId;
	}

	public void setPermId(Integer permId) {
		this.permId = permId;
	}
}
