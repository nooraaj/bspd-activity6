package com.bsp.fsccis.entity.tag;

public interface PKTag_GetMax<T extends Number> {
	
//    public T getIdToGenerate();
    public void setIdToGenerate(T id);
    public String getPKColumnName();
    public String getTableName();
    public T getDefaultId();

}
