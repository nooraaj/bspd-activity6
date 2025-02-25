package com.bsp.fsccis.bean.crud;

public interface AddEditTag {

	String ADD = "ADD";
	String EDIT = "EDIT";

	public String getAddEditMode();

	public void setAddEditMode(String addEditMode);
}
