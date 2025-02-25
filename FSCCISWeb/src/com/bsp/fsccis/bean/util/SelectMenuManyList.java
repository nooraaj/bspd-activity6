package com.bsp.fsccis.bean.util;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectMenuManyList<T> {
	private List<T> list;
	private List<T> selectedList;
	
	public SelectMenuManyList(){
		list = new ArrayList<T>();
		selectedList = new ArrayList<T>();
	}
	
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public List<T> getSelectedList() {
		return selectedList;
	}
	public void setSelectedList(List<T> selectedList) {
		this.selectedList = selectedList;
	}
}
