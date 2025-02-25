package com.bsp.fsccis.bean.reference;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
//import org.primefaces.context.RequestContext;
import org.primefaces.PrimeFaces;

import com.bsp.fsccis.bean.crud.GenericCRUDBean;
import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.login.ChangePasswordBean;
import com.bsp.fsccis.bean.login.SecurityQuestionBean;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.service.UserService;

@Named("accountBean")
@ViewScoped
public class AccountBean extends GenericCRUDBean<RefUserAccounts> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(AccountBean.class.getSimpleName());
	
	@Inject private AuthBean auth;
	
	@EJB private UserService service;
	
	@PostConstruct
	public void init(){
		LOGGER.info("init");
		if(auth.isLoggedIn()){	
			entity = service.getUser(auth.getUsername());
		}
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		Object value = sessionMap.get("msg");
		if (value instanceof FacesMessage) {
			sessionMap.remove("msg");
			FacesMessage msg = (FacesMessage) value;
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(msg);
		} 
		service.getFacade().logAuditTrail("Profile Account", AuditTrail.ACTION_VIEW, "Profile Account", auth.getAgencyGroup(), auth.getCuser());
	}
	
	public void changeSecurityQuestionAnswer(){
		LOGGER.info("changeSecurityQuestionAnswer()");
		Map<String, Object> sessionMap = FacesContext
		.getCurrentInstance().getExternalContext().getSessionMap();
		sessionMap.put(SecurityQuestionBean.CHANGE_QUESTION,auth.getUsername());
		
		String returnString = "/SecurityQuestion.xhtml?faces-redirect=true";
		ExternalContext ec = FacesContext.getCurrentInstance()
				.getExternalContext();
		try {
			ec.redirect(ec.getRequestContextPath() + returnString);
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
	}
	
	public void changePassword(){

		Map<String, Object> sessionMap = FacesContext
		.getCurrentInstance().getExternalContext().getSessionMap();
		sessionMap.put(ChangePasswordBean.PASSWORD_CHANGE,entity.getUserName());
		
		String returnString = "/ChangePassword.xhtml?faces-redirect=true";
		ExternalContext ec = FacesContext.getCurrentInstance()
				.getExternalContext();
		try {
			ec.redirect(ec.getRequestContextPath() + returnString);
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
	}
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected Class<RefUserAccounts> getEntityClass() {
		return RefUserAccounts.class;
	}

}
