package com.bsp.fsccis.bean.login;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.omnifaces.cdi.ViewScoped;
//import org.primefaces.context.RequestContext;
import org.primefaces.PrimeFaces;

import com.bsp.encryption.SHA256_UTF8_SALT;
import com.bsp.fsccis.authentication.TwoFactorAuthenticationProperties;
import com.bsp.fsccis.bean.util.CaptchaManager;
import com.bsp.fsccis.bean.util.TwoFactorUtil;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.entity.RefUserRole;
import com.bsp.fsccis.entity.RefUserRolePK;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.service.UserService;

@Named("loginBean")
@ViewScoped
public class LoginBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int LOGOUT_OTHER_USER = 1;
	public static final int BACK_TO_LOGIN_PAGE = 2;

	public static final Logger LOGGER = Logger.getLogger(LoginBean.class.getSimpleName());

	@Inject
	private SingleLoginMonitor monitor;

	@Inject
	AuthBean auth;

	@EJB
	private GenericFacade facade;

	@EJB
	private UserService service;

	/*
	 * @EJB private AgingService aging;
	 */
	private String username;
	private String password;
	private String jcaptcha;
	private String cuser;

	private boolean captchaChallengeFulfilled = false;

	private int choice;

	private TwoFactorAuthenticationProperties props;
	
	@PostConstruct
	public void init() {
		LOGGER.info("init");	
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		cuser = auth.getCuser();
		LOGGER.info("cuser: " + cuser);

		/*FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		if (request.getUserPrincipal() != null) {
			LOGGER.info("001.1");

			sessionMap.put("msg", new FacesMessage("Session Expired", "You have been logged out due to inactivity"));

			String returnString = "/logout.xhtml?faces-redirect=true";
			ExternalContext ec = context.getExternalContext();
			try {
				ec.redirect(ec.getRequestContextPath() + returnString);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}*/

		RefUserRolePK rupk = new RefUserRolePK();
		RefUserRole rur = new RefUserRole();
		RefUserAccounts refUser = new RefUserAccounts();

		Object value = sessionMap.get("msg");
		if (value instanceof FacesMessage) {
			sessionMap.remove("msg");
			FacesMessage msg = (FacesMessage) value;
			PrimeFaces rcontext = PrimeFaces.current();
			rcontext.dialog().showMessageDynamic(msg);
		}
		// file
		java.io.File root = new java.io.File("/FSCCIS");
		for (java.io.File file1 : root.listFiles()) {
			LOGGER.info("L1: " + file1.getAbsolutePath());
			if (file1.isDirectory()) {
				for (java.io.File file2 : file1.listFiles()) {
					LOGGER.info("L2: " + file2.getAbsolutePath());
					if (file2.isDirectory()) {
						for (java.io.File file3 : file2.listFiles()) {
							LOGGER.info("L3: " + file3.getAbsolutePath());
						}
					}
				}
			}
		}
		// if(testFile.exists()){
		// LOGGER.info("File found!!!! " + testFile.getAbsolutePath());
		// LOGGER.info("DELETE!");
		// if(!testFile.delete()){
		// LOGGER.info("FAILED!!!");
		// }
		// }else{
		// LOGGER.info("TEST FILE NOT FOUND");
		// }
		
		//2FA Properties
		initializeProperties();
	}

	public void forgotPassword() {
		LOGGER.info("forgotPassword");
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		sessionMap.put(SecurityQuestionBean.FORGOT_PASSWORD, username);

		String returnString = "/SecurityQuestion.xhtml?faces-redirect=true";
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		try {
			ec.redirect(ec.getRequestContextPath() + returnString);
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		}
	}

	public void processSessionExistChoice() {
		LOGGER.info("processSessionExistChoice(): " + choice);
		if (choice == LOGOUT_OTHER_USER) {
			HttpSession session = monitor.unregisterUserSession(username);
			if (session != null) {
				session.invalidate();
				session = null;
			}
			// by the time it reaches here, captcha was already validated to be
			// ok
			captchaChallengeFulfilled = true;
			login();
		} else {

			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext ec = context.getExternalContext();
			try {
				ec.redirect(ec.getRequestContextPath() + "/index.xhtml?faces-redirect=true");
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error: " + e, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private final String projectWebName = "FSFEIS";

	public void login() {
		LOGGER.info("login");

		auth.setSessionExistTag(false);

		RefUserAccounts user = null;
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

		LOGGER.info("captchatext: " + sessionMap.get("captchatext"));

		try {
			if (!captchaChallengeFulfilled) {
				if (!CaptchaManager.validate(sessionMap, jcaptcha)) {
					// if(false){
					context.addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Login", "Captcha mismatch."));
					jcaptcha = null;
					return;
				}
			}

			user = service.getUser(username);
			LOGGER.info("passed User 1 ");
			if (user.getRefAgencyGroupId().getAccessLevel() == RefAgencyGroup.DISABLED) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Access Disabled", "Access Disabled."));
				return;
			}

			LOGGER.info("passed User 2 ");
			if (user.getLoginAttempts() >= RefUserAccounts.MAX_LOGIN_ATTEMPT) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User Account Locked",
						"User account is locked. Please contact your system administrator."));
				return;
			}
			LOGGER.info("passed User 3");

			// boolean valid = request.authenticate(response);
			// if(valid){
			Date today = new Date();
			ExternalContext ec = context.getExternalContext();

			if (user.getUserPw().equals(SHA256_UTF8_SALT.encrypt(password.trim()))) {
				if (user.getSecurityQuestionId() == null) {
					// first time login redirect to set
					LOGGER.info("First Time Login");
					sessionMap.put(SecurityQuestionBean.NEW_QUESTION, username);

					String returnString = "/SecurityQuestion.xhtml?faces-redirect=true";
					ec.redirect(ec.getRequestContextPath() + returnString);
					return;

				} else if (user.getAccountStatus() == RefUserAccounts.STATUS_ACTIVE
						&& user.getPwDateExpiration().before(today)) {
					// forgot password
					LOGGER.info("Reset Password: VALIDATION PASSED! ");
					sessionMap.put(ChangePasswordBean.RESET_PASSWORD, username);

					String returnString = "/ChangePassword.xhtml?faces-redirect=true";
					ec.redirect(ec.getRequestContextPath() + returnString);
					LOGGER.info("Reset Password: CHANGE PASS! ");
					return;

				} else if (user.getPwDateExpiration().before(today)) {

					// password expired,
					LOGGER.info("Password Expired");
					sessionMap.put(ChangePasswordBean.LOGIN_EXPIRED, username);

					String returnString = "/ChangePassword.xhtml?faces-redirect=true";
					ec.redirect(ec.getRequestContextPath() + returnString);

					return;
				}

				LOGGER.info("passed User 4");

				if (user.getAccountStatus() == RefUserAccounts.STATUS_INACTIVE) {
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User Account Inactive",
							"User account is inactive. Please contact your system administrator."));
					return;
				}
				// }
				LOGGER.info("passed User 5");

				// Check if user has existing login (duplicate login/failed to
				// logout properly on prev session)
				if (monitor.isLoggedIn(username)) {
					LOGGER.info(username + " username is already logged in.");

					auth.setUsername(username);
					auth.setCuser(username);
					auth.setPassword(this.password);

					auth.setSessionExistTag(true);

					ec.redirect(ec.getRequestContextPath() + "/views/Login/SessionExist.xhtml?faces-redirect=true");

					return;
				}
				LOGGER.info("passed User 6");

			} else {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Login",
						"The username and/or password you provided does not match our records."));

				return;
			}

//			try {
				// invalidate session generated from initial viewing of login
				// page and let system generate new session after login
//				LOGGER.info("Session start ");
//				String sessionId = ((HttpSession) context.getExternalContext().getSession(false)).getId();
//				AuditTrail trail = new AuditTrail("Logout", AuditTrail.ACTION_LOGOUT);
//				trail.setDetails("Session ID: " + sessionId + ", User Triggered");
//				trail.setAgencyGroupId(auth.getAgencyGroup());
//				trail.setCdate(new Date());
//				trail.setCtime(new Date());
//				if (cuser != null) {
//					trail.setCuser(new String(cuser));
//				}

//				LOGGER.info("Generating new session before login...");
//				request.logout();
//				LOGGER.info("Logout initiated");
//				context.getExternalContext().invalidateSession();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			if (props.getDevtest().equalsIgnoreCase("false")) {
				TwoFactorUtil util = new TwoFactorUtil();
				String vUserName2FA = user.getEmail().trim() + "_" + props.getGroup();
				LOGGER.info("vUserName2FA: " + vUserName2FA);
				
				switch (user.getAuthDefault()) {
                case 0:
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Two-Factor Authentication",
                            "Inactive Two-Factor Authentication. Please contact your system administrator."));
                    //reset 2FA Authentication by administrator
                    return;
                case 1:
                    sessionMap
                    .put(TwoFactorAuthEnrollBean.ACTIVE_USER_2FA, username);
                    break;
                case 2:
                	if (!util.getCurrentTokenSerialForUser(vUserName2FA).isEmpty()) {
                        System.out.println("before delete token ");
                        util.deleteUser(vUserName2FA);
                        System.out.println("after delete token ");
                    }
                                        
                    sessionMap
                        .put(TwoFactorAuthEnrollBean.INACTIVE_USER_2FA, username);
                    
                    break;
                default:    
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Two-Factor Authentication",
                            "Inactive Two-Factor Authentication. Please contact your system administrator {null}"));                    
                    return;
                
                }
			}
			//user validation for internal and external users
			if (props.getEmail() != null && props.getZone() != null){
				
				String validEmail = props.getEmail();
				String userEmail = user.getEmail().substring
						(user.getEmail().indexOf("@"),user.getEmail().length());
				
				boolean isSystemAdmin = false;
				
				for(RefUserRole rur : user.getRefUserRoleList()){
					LOGGER.info("ROLE: " + rur.getSysRoles().getRoleName());
					
					if (rur.getSysRoles().getRoleName().equalsIgnoreCase("System Admin")) {
						
						isSystemAdmin = true;
						
						break;
					}
				}
				LOGGER.info("User Email: " + userEmail);
				LOGGER.info("Valid Email: " + validEmail);
				LOGGER.info("Zone: " + props.getZone());

				if (userEmail.equals(validEmail)) {
					
					if (props.getZone().equalsIgnoreCase("internal")) {
						
						 LOGGER.info("User is Internal: " + user.getEmail());
						 request.login(username, password);
						
					} else if (props.getZone().equalsIgnoreCase("external")
							&& !isSystemAdmin) {
						
						 LOGGER.info("User is External/BSP User: " + user.getEmail());
						 request.login(username, password);
						
					} else if (props.getZone().equalsIgnoreCase("internal")) {
						 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error",
								 "Invalid account for internal zone"));
						 return;
					} else if (props.getZone().equalsIgnoreCase("external")) {
						context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error",
								 "Invalid account for external zone"));
						 return;
					}
				} else {
					
					if (props.getZone().equalsIgnoreCase("external")) {
						
						LOGGER.info("User is External: " + user.getEmail());
						request.login(username, password);
						
					} else if (props.getZone().equalsIgnoreCase("external")) {
						context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error",
								 "Invalid account for external zone"));
						 return;
					} else if (props.getZone().equalsIgnoreCase("internal")) {
						context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error",
								 "Invalid account for internal zone"));
						 return;
					}
				}
				
				
			} else {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"An unexpected error has occured."));
				return;
			}
			
			LOGGER.info("Login start");

			//request.login(username, password);

			LOGGER.info("Login success");
			LOGGER.info("System Admin: " + request.isUserInRole("System Admin"));
			LOGGER.info("Uploader: " + request.isUserInRole("Uploader"));
			LOGGER.info("Viewer: " + request.isUserInRole("Viewer"));

			auth.setLoggedIn(true);
			auth.setUsername(username);
			auth.setCuser(username);
			if (user.getRefAgencyGroupId() != null) {
				auth.setAgencyGroup(user.getRefAgencyGroupId());
			}

			if (user != null) {
				String name = user.getLastName() + ", ";
				name += user.getFirstName() + " ";
				name += user.getMiddleName() != null && !user.getMiddleName().isEmpty()
						? user.getMiddleName().substring(0, 1) + "." : "";
				auth.setName(name);
			}

			if (user.getLoginAttempts() > 0) {
				service.resetLoginAttempts(user, user.getUserName());
			}

			/* autoClosedBean.triggerAutoClose(); */

			Map<String, Object> cookies = context.getExternalContext().getRequestCookieMap();

				if (cookies != null && !cookies.isEmpty() && cookies.get("WASReqURL") != null) {

					if (props.getDevtest().equalsIgnoreCase("false")) {
						
						if (user.getAuthDefault() != 1) {
							Cookie cookie = (Cookie) cookies.get("WASReqURL");
							String returnString;
							if (!cookie.getValue().contains(projectWebName)) {
								sessionMap.put(TwoFactorAuthEnrollBean.DEV_TEST_2FA, username);
								returnString = "/TwoFactorAuthEnroll.xhtml?faces-redirect=true";
							} else {
								returnString = cookie.getValue().substring(
										cookie.getValue().indexOf(projectWebName) + projectWebName.length(),
										cookie.getValue().length());
							}
							LOGGER.info("returnString: " + returnString);

							ec.redirect(ec.getRequestContextPath() + returnString);
						} else {
							Cookie cookie = (Cookie) cookies.get("WASReqURL");
							String returnString;
							if (!cookie.getValue().contains(projectWebName)) {
								returnString = "/TwoFactorAuth.xhtml?faces-redirect=true";
							} else {
								returnString = cookie.getValue().substring(
										cookie.getValue().indexOf(projectWebName) + projectWebName.length(),
										cookie.getValue().length());
							}
							LOGGER.info("returnString: " + returnString);

							ec.redirect(ec.getRequestContextPath() + returnString);
						}
						
					} else {
						Cookie cookie = (Cookie) cookies.get("WASReqURL");
						String returnString;
						if (!cookie.getValue().contains(projectWebName)) {
							sessionMap.put(TwoFactorAuthEnrollBean.DEV_TEST_2FA, username);
							returnString = "/TwoFactorAuthEnroll.xhtml?faces-redirect=true";
						} else {
							returnString = cookie.getValue().substring(
									cookie.getValue().indexOf(projectWebName) + projectWebName.length(),
									cookie.getValue().length());
						}
						LOGGER.info("returnString: " + returnString);

						ec.redirect(ec.getRequestContextPath() + returnString);
					}
				} else {
					
					if (props.getDevtest().equalsIgnoreCase("false")) {
						// auth_default = 2
						if (user.getAuthDefault() != 1) {
						ec.redirect(ec.getRequestContextPath() 
								+ "/TwoFactorAuthEnroll.xhtml?faces-redirect=true");
						}
						else {
							ec.redirect(ec.getRequestContextPath() 
									+ "/TwoFactorAuth.xhtml?faces-redirect=true");
						}
					} else {
						sessionMap.put(TwoFactorAuthEnrollBean.DEV_TEST_2FA, username);
						ec.redirect(ec.getRequestContextPath() 
								+ "/TwoFactorAuthEnroll.xhtml?faces-redirect=true");
					}
					
				}

			LOGGER.info("Trailing");

			AuditTrail trail = new AuditTrail("Login", AuditTrail.ACTION_LOGIN);

			String loginDeviceDetail = "";
			if (request.getHeader("X-Forwarded-For") != null && !request.getHeader("X-Forwarded-For").isEmpty()) {
				loginDeviceDetail += "SOURCE: " + request.getHeader("X-Forwarded-For");
			} else if (request.getHeader("X-Forwarded-Host") != null
					&& !request.getHeader("X-Forwarded-Host").isEmpty()) {
				loginDeviceDetail += "SOURCE: " + request.getHeader("X-Forwarded-Host");
			}
			loginDeviceDetail += " DESC: " + request.getHeader("User-Agent");

			if (request.getHeader("User-Agent").indexOf("Mobile") != -1) {
				trail.setDetails(", Mobile Login -- " + loginDeviceDetail);
			} else {
				trail.setDetails(", Desktop Login -- " + loginDeviceDetail);
			}
			LOGGER.info("Trailing Session ID Start");
			LOGGER.info("Session ID: " + ((HttpSession) context.getExternalContext().getSession(false)).getId()
					+ trail.getDetails());

			trail.setDetails("Session ID: " + ((HttpSession) context.getExternalContext().getSession(false)).getId()
					+ trail.getDetails());

			if (trail.getDetails().length() > 2000) {
				trail.setDetails(trail.getDetails().substring(0, 1980) + "...");
			}
			LOGGER.info("Trailing Session ID End");

			trail.setAgencyGroupId(auth.getAgencyGroup());
			trail.setCdate(new Date());
			trail.setCtime(new Date());
			trail.setCuser(username);

			service.getFacade().logAuditTrail(trail);

			monitor.registerUserSession(username, (HttpSession) context.getExternalContext().getSession(false));

		} catch (ServletException e) {

			if (e.getMessage().contains("Authentication had been already established")) {

				sessionMap.put("msg",
						new FacesMessage("Session Expired", "You have been logged out due to inactivity"));

				String returnString = "/logout.xhtml?faces-redirect=true";
				ExternalContext ec = context.getExternalContext();
				try {
					ec.redirect(ec.getRequestContextPath() + returnString);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}
			//LOGGER.log(Level.SEVERE, "Error eto ba?: " + e + " " + e.getMessage(), e.getMessage());
			LOGGER.log(Level.SEVERE, "ERROR: " + e);
			e.printStackTrace();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Login",
					"The username and/or password you provided does not match our records."));
			if (user != null) {
				String cuser = username.length() > 15 ? username.substring(0, 15) : username;
				service.invalidLogin(user, cuser);
			}
		} catch (IOException e) {
			LOGGER.info("ERROR: " + e.getMessage());
		} catch (Exception e) {
			//LOGGER.info("test9");
			e.printStackTrace();
			if (e != null && e.getMessage() != null && e.getMessage().contains("NoResultException")) {

				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Login",
						"The username and/or password you provided does not match our records."));
				return;
			}
			LOGGER.log(Level.SEVERE, "ERROR: " + e.getMessage(), e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Expected Error",
					"An unexpected error has occured."));
		}
	}

	@PreDestroy
	public void destroy() {
	}

	public void logout() {
		LOGGER.info("logout");
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

		try {

			String sessionId = ((HttpSession) context.getExternalContext().getSession(false)).getId();
			AuditTrail trail = new AuditTrail("Logout", AuditTrail.ACTION_LOGOUT);
			trail.setDetails("Session ID: " + sessionId + ", User Triggered");
			trail.setAgencyGroupId(auth.getAgencyGroup());
			trail.setCdate(new Date());
			trail.setCtime(new Date());
			if (cuser != null) {
				trail.setCuser(new String(cuser));
			}

			HttpSession session = monitor.unregisterUserSession(cuser);
			if (session != null) {
				session.invalidate();
				session = null;
			}

			request.logout();
			context.getExternalContext().invalidateSession();

			service.getFacade().logAuditTrail(trail);
		} catch (ServletException e) {
			LOGGER.info("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		
		ExternalContext ec = FacesContext.getCurrentInstance()
				.getExternalContext();
		
			try {
				ec.redirect(ec.getRequestContextPath()
						+ "/index.xhtml?faces-redirect=true");
			} catch (IOException e) {
				e.printStackTrace();
			}

		return;
	}
	
	public void initializeProperties() {

		props = new TwoFactorAuthenticationProperties();
		try {

			props.initialize();

			LOGGER.info("2FA Properties Retrieved: " + props.getAdmin() + ", " + props.getAuth() + ", "
					+ props.getRedirect() + ", " + props.getLogin() + ", " + props.getRetries() + ", "
					+ props.getUsername() + ", " + props.getPassword() + ", " + props.getVendor() + ", "
					+ props.getTokens() + ", " + props.getGroup() + ", " + props.getDevtest() + ", " 
					+ props.getEmail() + ", " + props.getZone());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJcaptcha() {
		return jcaptcha;
	}

	public void setJcaptcha(String jcaptcha) {
		this.jcaptcha = jcaptcha;
	}

	public int getChoice() {
		return choice;
	}

	public void setChoice(int choice) {
		this.choice = choice;
	}

}
