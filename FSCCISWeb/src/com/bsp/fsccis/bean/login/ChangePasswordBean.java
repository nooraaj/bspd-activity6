package com.bsp.fsccis.bean.login;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.omnifaces.cdi.ViewScoped;
//import org.primefaces.context.RequestContext;
import org.primefaces.PrimeFaces;

import com.bsp.fsccis.bean.tag.InvalidSessionTag;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.service.UserService;

@Named("changePasswordBean")
@ViewScoped
public class ChangePasswordBean implements Serializable,InvalidSessionTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger LOGGER = Logger
			.getLogger(ChangePasswordBean.class.getSimpleName());
	
	@Inject
	private AuthBean authBean;
	
	@EJB
	private UserService service;
	private String oldpassword;
	private String newpassword1;
	private String newpassword2;
	private RefUserAccounts user;

	public static final String LOGIN_EXPIRED = "LOGIN_EXPIRED";
	public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
	public static final String NEW_SET_PASSWORD= "NEW_SET_PASSWORD";
	public static final String RESET_PASSWORD= "RESET_PASSWORD";
	private boolean needToReLogin = false;
	private boolean needToEnterOldPassword = true;
	
	private boolean sessionInvalid = false;
	@PostConstruct
	public void init() {
		LOGGER.info("init");

		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		
		Object value = sessionMap.get(RESET_PASSWORD);
		if(value!= null){
			user = service.getUser(value.toString());
			sessionMap.remove(RESET_PASSWORD);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage("Reset Password",
					"You need to update your password"));
			needToReLogin = true;
			needToEnterOldPassword = false;
			return;
		}
		
		value = sessionMap.get(LOGIN_EXPIRED);

		if (value != null) {
			user = service.getUser(value.toString());
			sessionMap.remove(LOGIN_EXPIRED);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage("Password Expired",
					"You need to update your password"));
			needToReLogin = true;
			return;
		}
		
		value = sessionMap.get(NEW_SET_PASSWORD);
		if(value!= null){
			user = service.getUser(value.toString());
			sessionMap.remove(NEW_SET_PASSWORD);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage("First Time Login",
					"You need to update your password"));
			needToReLogin = true;
			return;
		}
		
		value = sessionMap.get(PASSWORD_CHANGE);
		if(value!= null){
			user = service.getUser(value.toString());
			sessionMap.remove(PASSWORD_CHANGE);
			return;
		}
		

		//if this portion is reached, assume page was purposefully visited via direct access
		if(!authBean.isLoggedIn()){ 
			setSessionInvalid(true);
		}
	}

	public void changePassword() {
		try {
			
			if(!needToEnterOldPassword){
				oldpassword = user.getUserPw();
			}
			
			if(needToEnterOldPassword && !service.validate(user.getUserName(), oldpassword)){
				FacesContext
						.getCurrentInstance()
						.addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR,
										"Invalid Login",
										"The username and/or password you provided does not match our records."));
				if (user != null) {
					String cuser = user.getUserName().length() > 5 ? user
							.getUserName().substring(0, 5) : user.getUserName();
					service.invalidLogin(user, cuser);
				}
				return;
			}
			int result = service.changePassword(getUser(), oldpassword, newpassword1,
					getUser().getUserName());
			if (result == UserService.PWD_NEW_INVALID) {
				FacesContext
						.getCurrentInstance()
						.addMessage(
								null,
								new FacesMessage(
										FacesMessage.SEVERITY_ERROR,
										"Change Password",
										"New password is invalid. It must be at least 10 in length containing upper and lower case letters, digits, and symbols"));
			} else if (result == UserService.PWD_SAME_AS_PREVIOUS) {
				FacesContext
						.getCurrentInstance()
						.addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR,
										"Change Password",
										"New password cannot be the same as your previous password."));
			} else if (result == UserService.PWD_SUCCESS) {

				if(needToReLogin){
					Map<String, Object> sessionMap = FacesContext
							.getCurrentInstance().getExternalContext()
							.getSessionMap();
					
					LOGGER.info("logout: Change Password");
					FacesContext context = FacesContext.getCurrentInstance();
					HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
					
					try {
						request.logout();
						context.getExternalContext().invalidateSession();

					} catch (ServletException e) {
						LOGGER.info("ERROR: " + e.getMessage());
						e.printStackTrace();
					}
					
					sessionMap
					.put("msg",
							new FacesMessage("Change Password Success",
									"Successfully changed password. Please Login using your new password"));
					
					ExternalContext ec = FacesContext.getCurrentInstance()
							.getExternalContext();
					ec.redirect(ec.getRequestContextPath()
							+ "/index.xhtml?faces-redirect=true");
					
				}else{
					Map<String, Object> sessionMap = FacesContext
							.getCurrentInstance().getExternalContext()
							.getSessionMap();
					sessionMap
							.put("msg",
									new FacesMessage("Change Password Success",
											"Successfully changed password."));
					ExternalContext ec = FacesContext.getCurrentInstance()
							.getExternalContext();
					ec.redirect(ec.getRequestContextPath()
							+ "/views/User/Account.xhtml?faces-redirect=true");
				}
			} 
		} catch (Exception e) {

			if (e.getMessage() != null
					&& e.getMessage().contains("NoResultException")) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR,
								"Invalid Login",
								"The username not found in our records."));
				return;
			}

			LOGGER.log(Level.SEVERE,"ERROR: " + e,e.getMessage());
			FacesContext.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Expected Error",
									"An unexpected error has occured."));
		}
	}

	public String getNewpassword1() {
		return newpassword1;
	}

	public void setNewpassword1(String newpassword1) {
		this.newpassword1 = newpassword1;
	}

	public String getNewpassword2() {
		return newpassword2;
	}

	public void setNewpassword2(String newpassword2) {
		this.newpassword2 = newpassword2;
	}

	public RefUserAccounts getUser() {
		return user;
	}

	public void setUser(RefUserAccounts user) {
		this.user = user;
	}

	public String getOldpassword() {
		return oldpassword;
	}

	public void setOldpassword(String oldpassword) {
		this.oldpassword = oldpassword;
	}

	public boolean isNeedToEnterOldPassword() {
		return needToEnterOldPassword;
	}

	public void setNeedToEnterOldPassword(boolean needToEnterOldPassword) {
		this.needToEnterOldPassword = needToEnterOldPassword;
	}
	
	public boolean isSessionInvalid() {
		return sessionInvalid;
	}
	
	public void setSessionInvalid(boolean sessionInvalid) {
		this.sessionInvalid = sessionInvalid;
	}

}