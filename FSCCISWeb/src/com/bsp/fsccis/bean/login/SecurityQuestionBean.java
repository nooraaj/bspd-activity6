package com.bsp.fsccis.bean.login;

import java.io.IOException;
import java.io.Serializable;
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

import com.bsp.fsccis.bean.tag.InvalidSessionTag;
import com.bsp.fsccis.bean.util.CaptchaManager;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.mail.MailService;
import com.bsp.fsccis.service.UserService;

@Named("securityQuestionBean")
@ViewScoped
public class SecurityQuestionBean implements Serializable,InvalidSessionTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger
			.getLogger(SecurityQuestionBean.class.getSimpleName());

	public static final String NEW_QUESTION = "NEW_QUESTION";
	public static final String CHANGE_QUESTION = "CHANGE_QUESTION";
	public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
	
	@Inject
	private AuthBean authBean;
	
	@EJB
	private UserService service;
	private RefUserAccounts user;
	private String process;
	private String answer;
	private String jcaptcha;
	private boolean sessionInvalid = false;
	
	
	@EJB
	GenericFacade facade;
	
	@EJB
	private MailService mailService;
	
	@PostConstruct
	public void init() {
		LOGGER.info("init");

		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		Object value = sessionMap.get(NEW_QUESTION);
		// boolean needToReLogin = false;

		if (value != null) {
			LOGGER.info("here");
			setProcess(NEW_QUESTION);
			setUser(service.getUser(value.toString()));
			sessionMap.remove(NEW_QUESTION);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage("First Time Login",
					"Before you begin, please set your security question."));
			// needToReLogin = true;
			return;
		}

		value = sessionMap.get(FORGOT_PASSWORD);

		if (value != null) {
			setProcess(FORGOT_PASSWORD);
			PrimeFaces context = PrimeFaces.current();
			try{
				setUser(service.getUser(value.toString()));
			} catch (Exception e) {
				LOGGER.info("test9");
				e.printStackTrace();
				if (e != null && e.getMessage() != null
						&& e.getMessage().contains("NoResultException")) {

//					sessionMap
//							.put("msg",
//									new FacesMessage("",
//											"Invalid session detected."));
//
//					ExternalContext ec = FacesContext.getCurrentInstance()
//							.getExternalContext();
//					try {
//						ec.redirect(ec.getRequestContextPath()
//								+ "/index.xhtml?faces-redirect=true");
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
					

					sessionMap.remove(FORGOT_PASSWORD);
					context.dialog().showMessageDynamic(new FacesMessage("Password Reset",
							"Invalid Session Detected. 2"));
					return;
				} 
			}
			sessionMap.remove(FORGOT_PASSWORD);
			context.dialog().showMessageDynamic(new FacesMessage("Password Reset",
					"Please answer this security question to reset your password."));
			// needToReLogin = true;
			return;
		}

		value = sessionMap.get(CHANGE_QUESTION);

		if (value != null) {
			setProcess(CHANGE_QUESTION);
			setUser(service.getUser(value.toString()));
			sessionMap.remove(CHANGE_QUESTION);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage(
					"Change Question/Answer",
					"Please set new security question/answer."));
			// needToReLogin = true;
			return;
		}
		
		
		//if this portion is reached, assume page was purposefully visited via direct access
		if(!authBean.isLoggedIn()){ 
			setSessionInvalid(true);
		}
	}

	public void validateAnswer() {
		LOGGER.info("validateAnswer()");
		if (user.getSecurityQuestionAnswer().toUpperCase()
				.equals(answer.toUpperCase())) {
			save();
		} else {
			FacesContext
					.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Answer Security Question",
									"Your security answer does not match our records."));
			jcaptcha = null;
			refreshCaptcha();
		}
	}

	public void save() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, Object> sessionMap = context.getExternalContext()
					.getSessionMap();
			if (process.equals(FORGOT_PASSWORD)) {
				if (!CaptchaManager.validate(sessionMap, jcaptcha)) {
					jcaptcha = null;
					refreshCaptcha();
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Invalid", "Captcha mismatch."));
					return;
				}
			}
			
			user.setSecurityQuestionAnswer(answer);
			service.edit(user, user.getCuser());
			if (getProcess().equals(NEW_QUESTION)) {
				sessionMap.put(ChangePasswordBean.NEW_SET_PASSWORD,
						user.getUserName());
			} else if (getProcess().equals(FORGOT_PASSWORD)) {
		//		sessionMap.put(ChangePasswordBean.RESET_PASSWORD,
		//				user.getUserName());
				service.resetPassword(user);
				facade.edit(user);
				
				sessionMap.put("msg", new FacesMessage(
						"Forgot Password", 
						"Temporary password has been sent to your email."));
				ExternalContext ec = FacesContext.getCurrentInstance()
						.getExternalContext();
				
				try {
					//email sending
					 LOGGER.info("email sending... "+ service.getDefaultPassword());
					 mailService.sendEMailResetPassword(user, service.getDefaultPassword());
					 
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ec.redirect(ec.getRequestContextPath()
						+ "/index.xhtml?faces-redirect=true");
				LOGGER.info("Email sent!");
				return;
				
			} else if (getProcess().equals(CHANGE_QUESTION)) {
				sessionMap.put("msg", new FacesMessage(
						"Change Question/Answer Success",
						"Successfully changed Question/Answer."));
				ExternalContext ec = FacesContext.getCurrentInstance()
						.getExternalContext();
				ec.redirect(ec.getRequestContextPath()
						+ "/views/User/Account.xhtml?faces-redirect=true");
				return;
			}

			String returnString = "/ChangePassword.xhtml?faces-redirect=true";
			ExternalContext ec = FacesContext.getCurrentInstance()
					.getExternalContext();
			ec.redirect(ec.getRequestContextPath() + returnString);
			return;

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Error Encountered", "Failed to perform action."));

		}
	}

	public void refreshCaptcha(){
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		sessionMap.put(SecurityQuestionBean.FORGOT_PASSWORD, user.getUserName());

		String returnString = "/SecurityQuestion.xhtml?faces-redirect=true";
		ExternalContext ec = FacesContext.getCurrentInstance()
				.getExternalContext();
		try {
			ec.redirect(ec.getRequestContextPath() + returnString);
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
	}
	
	public RefUserAccounts getUser() {
		return user;
	}

	public void setUser(RefUserAccounts user) {
		this.user = user;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getJcaptcha() {
		return jcaptcha;
	}

	public void setJcaptcha(String jcaptcha) {
		this.jcaptcha = jcaptcha;
	}
	
	public boolean isSessionInvalid() {
		return sessionInvalid;
	}
	
	public void setSessionInvalid(boolean sessionInvalid) {
		this.sessionInvalid = sessionInvalid;
	}
}
