package com.bsp.fsccis.util;

public class ClassDisplayName {
	private Class<?> cls;
	private String displayName;
	
	public ClassDisplayName(Class<?> cls,String disString){
		this.cls = cls;
		this.displayName = disString;
	}
	public Class<?> getCls() {
		return this.cls;
	}
	public String getDisplayName() {
		return displayName;
	}
}
