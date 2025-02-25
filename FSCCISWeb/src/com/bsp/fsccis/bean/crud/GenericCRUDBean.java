package com.bsp.fsccis.bean.crud;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import com.bsp.fsccis.facade.GenericFacade;

public abstract class GenericCRUDBean<T> implements AddEditTag{
	private List<T> entityList;
	protected T entity;
	protected String addEditMode = ADD; 
	
	@EJB
	private
	GenericFacade facade;
	
	public abstract Logger getLogger();
	
	public void initNewEntity(){
		getLogger().info("initNewEntity()");
		Constructor<T> constructor;
		try {
			constructor = getEntityClass().getConstructor(new Class<?>[]{});
			setEntity(constructor.newInstance(new Object[]{}));
		} catch (Exception e) {
			getLogger().log(Level.SEVERE,"Problem initializing data. Page most likely will crash");
		}
	}
	
	protected abstract Class<T> getEntityClass();
	
	public void add() throws Exception{
		facade.create(this.entity);
	}
	
	public void edit(){
		try{
			facade.edit(this.entity);
		}catch(Exception e){
			getLogger().log(Level.SEVERE,"ERROR: " + e,e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Edit", "Problem Encountered in Editing Entity"));
		}
	}
	
	public List<T> getEntityList() {
		return entityList;
	}
	public void setEntityList(List<T> entityList) {
		this.entityList = entityList;
	}
	public T getEntity() {
		return entity;
	}
	public void setEntity(T entity) {
		this.entity = entity;
	}
	public GenericFacade getFacade() {
		return facade;
	}

	public String getAddEditMode() {
		return addEditMode;
	}

	public void setAddEditMode(String addEditMode) {
		this.addEditMode = addEditMode;
	}
}
