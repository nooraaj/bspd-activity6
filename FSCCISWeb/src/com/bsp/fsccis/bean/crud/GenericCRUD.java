package com.bsp.fsccis.bean.crud;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.ClassBean;
import com.bsp.fsccis.util.ClassDisplayName;

@Named("genericCRUD")
@ViewScoped
public class GenericCRUD implements Serializable,AddEditTag{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(GenericCRUD.class.getSimpleName());

	@Inject
	AuthBean auth;
	
	@EJB
	private
	GenericFacade facade;
	
	private List<?> entityList;
	private Object entity;
	private Class<?> entityClass;
	private String displayName;
	private String addEditMode = ADD;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostConstruct
	public void init(){
		
//		Map<String, String> parameterMap = (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//		String className = parameterMap.get("contentUrl");
		
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String reqArr[] = request.getRequestURL().toString().split("/");
		String className = reqArr[reqArr.length - 1].replace(".xhtml", "");
		
		if(className != null && !className.isEmpty()){
			try{
				ClassDisplayName cdn = ClassBean.getInstance().getClassMap().get(className);
				entityClass = cdn.getCls();
				displayName = cdn.getDisplayName();
				setEntityList(new ArrayList(facade.findAll(entityClass)));
				LOGGER.info("init(): " + className);

				facade.logAuditTrail(cdn.getDisplayName(), AuditTrail.ACTION_VIEW, cdn.getDisplayName(), auth.getAgencyGroup(), auth.getCuser());
			}catch(Exception e){
//				LOGGER.info("No class set");
			}
		}else{
//			LOGGER.info("No class set");
		}
		
	}
	
	
	public void initNewEntity(){
		LOGGER.info("initNewEntity");
		Constructor<?> constructor;
		try {
			constructor = entityClass.getConstructor(new Class<?>[]{});
			setEntity(constructor.newInstance(new Object[]{}));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Problem initializing data. Page most likely will crash");
		}
	}
	
	
	public void add(){
		LOGGER.info("add");
		try{
			if (entity instanceof BSPTimeStampable) {
				BSPTimeStampable bts = (BSPTimeStampable) entity;
				bts.setCuser(auth.getCuser());
			}
			facade.create(this.entity);
			setEntityList(facade.findAll(entityClass));
			initNewEntity();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Add", "Successful"));
		}catch(Exception e){ 
			if(e.getMessage().contains("org.apache.openjpa.persistence.EntityExistsException")){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Duplicate entries detected."));
			}else{
				LOGGER.log(Level.SEVERE,"ERROR: " + e,e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Problem Encountered in Adding New Entity"));
			}
		}
	}
	
	public void edit(){
		LOGGER.info("edit");
		try{
			if (entity instanceof BSPTimeStampable) {
				BSPTimeStampable bts = (BSPTimeStampable) entity;
				bts.setCuser(auth.getCuser());
			}
			facade.edit(this.entity);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Edit", "Successful"));
		}catch(Exception e){
			if (e.getMessage().contains(
					"javax.validation.ConstraintViolationException")) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage("Error Edit",
								"Duplicate entries detected."));
			} else {
				LOGGER.log(Level.SEVERE,"ERROR: " + e,e.getMessage());
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage("Edit",
								"Problem Encountered in Editing Entity"));
			}
		}
	}
	
	public List<?> getEntityList() {
		return entityList;
	}
	public void setEntityList(List<?> entityList) {
		this.entityList = entityList;
	}
	public Object getEntity() {
		return entity;
	}
	public void setEntity(Object entity) {
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


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}
	
	
}
