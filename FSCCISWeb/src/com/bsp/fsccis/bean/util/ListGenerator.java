package com.bsp.fsccis.bean.util;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.ClassBean;
import com.bsp.fsccis.util.ClassDisplayName;

@Named("listGenerator")
@ApplicationScoped
public class ListGenerator {
	private static final Logger LOGGER = Logger.getLogger(ListGenerator.class.getSimpleName());
	
	@EJB private GenericFacade facade;
	
	public List<?> getList(String className){
		ClassDisplayName cdn = ClassBean.getInstance().getClassMap().get(className);
		return facade.findAll(cdn.getCls());
	}
}
