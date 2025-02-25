package com.bsp.fsccis.entity.tag;

public interface CRUDTag {
	int NEW = 1;
	int EDIT = 2;
//	int DELETE = -1;
//	int DELETE_FROM_EDIT = -2;
//	int DELETE_FROM_NEW = -3;
	
	public int getCrudStatus();
	public void setCrudStatus(int crudStatus);
}
