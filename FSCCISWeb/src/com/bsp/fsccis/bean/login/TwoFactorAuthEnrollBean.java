package com.bsp.fsccis.bean.login;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
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

import com.bsp.fsccis.authentication.TwoFactorAuthenticationProperties;
import com.bsp.fsccis.bean.util.TwoFactorException;
import com.bsp.fsccis.bean.util.TwoFactorUtil;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.service.UserService;

@Named("twoFactorAuthEnrollBean")
@ViewScoped
public class TwoFactorAuthEnrollBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger
			.getLogger(TwoFactorAuthEnrollBean.class.getSimpleName());
	
	@Inject
	private AuthBean authBean;
	
	@EJB
	private UserService service;
	
	private RefUserAccounts user;
	private TwoFactorAuthenticationProperties props;
	
	private String serialNumber;
	private String activationCode;
	private String registrationCode;
	private String authenticationCode;
	private String process;
	
	public static final String INACTIVE_USER_2FA = "INACTIVE_USER_2FA";
	public static final String ACTIVE_USER_2FA = "ACTIVE_USER_2FA";
	public static final String DEV_TEST_2FA = "DEV_TEST_2FA";

	private String parms;
	private String vUserName2FA;
	private Short authDefault;
	private int index;
	private String vAliasName;
	private String vFullName;
	private String vMessage;
	private String vSerialNo;
	private String vActivationCode;
	
	private boolean sessionInvalid = false;
	private boolean vAuthCall = true;
	private boolean vError = false;
	
	private String cuser;
	
	@PostConstruct
	public void init() {
		LOGGER.info("2FA INIT");
		
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();	
		
		LOGGER.info("Initialize 2FA properties");
		initializeProperties();
		
		if (props.getDevtest().equalsIgnoreCase("true")) {
			Object value = sessionMap.get(DEV_TEST_2FA);
			LOGGER.info("Session username value " + value);
			
			if (value != null) {
				LOGGER.info("2FA LOG: DEVTEST ");
				setUser(service.getUser(value.toString()));
				vUserName2FA = user.getEmail().trim() +"_"+ props.getGroup();
				authDefault = user.getAuthDefault();
				LOGGER.info("vUserName2FA: " + vUserName2FA);
				LOGGER.info("authDefault: " + user.getAuthDefault());
				
				sessionMap.remove(DEV_TEST_2FA);
				/*RequestContext context = RequestContext.getCurrentInstance();
				context.showMessageInDialog(new FacesMessage("Two-Factor Authentication",
						"Please generate Two-Factor Authentication."));*/
				LOGGER.info("DEV TEST");
				return;
			
			}
			
		} else {
			Object value = sessionMap.get(INACTIVE_USER_2FA);
			LOGGER.info("Session username value " + value);
			
			if (value != null) {
				LOGGER.info("2FA LOG: NEW/RESET USER ");
				setUser(service.getUser(value.toString()));
				vUserName2FA = user.getEmail().trim() +"_"+ props.getGroup();
				authDefault = user.getAuthDefault();
				LOGGER.info("vUserName2FA: " + vUserName2FA);
				LOGGER.info("authDefault: " + user.getAuthDefault());
				
				sessionMap.remove(INACTIVE_USER_2FA);
				/*RequestContext context = RequestContext.getCurrentInstance();
				context.showMessageInDialog(new FacesMessage("Two-Factor Authentication",
						"Please generate Two-Factor Authentication."));*/
				LOGGER.info("NEW/RESET USER");
				return;
			}
			
			value = sessionMap.get(ACTIVE_USER_2FA);
			
			if (value != null) {
				LOGGER.info("2FA LOG: ACTIVE USER ");
				setProcess(ACTIVE_USER_2FA);
				PrimeFaces context = PrimeFaces.current();
				try {
					
					setUser(service.getUser(value.toString()));
					vUserName2FA = user.getEmail().trim() +"_"+ props.getGroup();
					authDefault = user.getAuthDefault();
					LOGGER.info("vUserName2FA: " + vUserName2FA);
					LOGGER.info("authDefault: " + user.getAuthDefault());
					
				} catch (Exception e) {
					LOGGER.info("2FA TEST");
					e.printStackTrace();
					if (e != null && e.getMessage() != null
							&& e.getMessage().contains("NoResultException")) {
						
						
						sessionMap.remove(ACTIVE_USER_2FA);
						context.dialog().showMessageDynamic(new FacesMessage("Two-Factor Authentication",
								"Invalid Session Detected. 2"));
					}
				}
				sessionMap.remove(ACTIVE_USER_2FA);
				context.dialog().showMessageDynamic(new FacesMessage("Two-Factor Authentication",
						"Please authenticate"));
				LOGGER.info("2FA ACTIVE USER: Proceed to Authenticate" + " " + vUserName2FA);
				return;
			}
		}

		//if this portion is reached, assume page was purposefully visited via direct access
		if(!authBean.isLoggedIn()){ 
			setSessionInvalid(true);
			LOGGER.info("2FA: DEV TEST END ");
		}
	}
	
	//2FA Enrollment Screen
	public void generate() throws TwoFactorException, RemoteException {
    	LOGGER.info("2FA LOG: GENERATE ");  
    	FacesContext context = FacesContext.getCurrentInstance();
    	Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
    	
    	index = vUserName2FA.indexOf('@');
    	//String vAliasName = vUserName.substring(0,index < 0? vUserName.length():index);
		vAliasName = vUserName2FA.replace("@","_");
		LOGGER.info("vAliasName: " + vAliasName);
        vFullName = user.getFirstName().trim() + " " 
        			+ user.getMiddleName().trim() + " " + user.getLastName().trim(); 
        
        TwoFactorUtil util = new TwoFactorUtil();
		
        try {
        	
        	util.createUser(vUserName2FA, vFullName, vAliasName);
        	parms = util.createToken(vUserName2FA);
        	vSerialNo = util.getTokenSerialForUser(vUserName2FA);
        	vActivationCode = util.getActivationCode(vUserName2FA, vSerialNo);
        	System.out.println("User details: " + parms + " " + vSerialNo + " " + vActivationCode);

        	this.serialNumber = vSerialNo;
        	this.activationCode = vActivationCode;

        	System.out.println("textField populated.");
        	
        } catch (Exception e) {
        	System.out.println("Error in Generate");
        	if (e.toString().contains("USER_EXIST:5205002")) {
        		
        		try {
					vSerialNo = util.getTokenSerialForUser(vUserName2FA);
					
					System.out.println("vSerialNo:" + vSerialNo);
	                if (vSerialNo.isEmpty()) {
	                    vSerialNo = util.createToken(vUserName2FA);
	                }
	                
					LOGGER.info("vUserName and vSerialNo: " + vUserName2FA + " " + vSerialNo);
					vActivationCode = util.getActivationCode(vUserName2FA, vSerialNo);
					LOGGER.info("vActivationCode: " + vActivationCode);
	                
					this.serialNumber = vSerialNo;
					this.activationCode = vActivationCode;

					System.out.println("textField populated.");
        		} catch (Exception e1) {
        			e1.printStackTrace();
        			
        			 sessionMap.put(TwoFactorAuthEnrollBean.INACTIVE_USER_2FA, user.getUserName());
        		}
        		
        	} else {
        		vMessage = e.toString();
        		System.out.println("Error:" + parms + " " + vSerialNo + " " + vActivationCode);
        		LOGGER.info("Details: " + serialNumber + " " + activationCode);
        		FacesContext.getCurrentInstance().addMessage(null,
	        			 new FacesMessage(FacesMessage.SEVERITY_WARN,
	        					 vMessage, ""));
        	}
        }
	}
	
	public void register() throws IOException {
		LOGGER.info("2FA LOG: REGISTER");
		
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		
		if (props.getDevtest().equalsIgnoreCase("true")) {
			
			sessionMap.put(TwoFactorAuthEnrollBean.DEV_TEST_2FA, user.getUserName());
			
			String returnString = "/TwoFactorAuth.xhtml?faces-redirect=true";
     		ExternalContext ec = FacesContext.getCurrentInstance()
 				.getExternalContext();
     		
     		try {
				ec.redirect(ec.getRequestContextPath() + returnString);
				
			} catch (IOException e) {
				e.printStackTrace();
				vMessage = "Register authentication error";
				FacesContext.getCurrentInstance().addMessage(null,
     					new FacesMessage(FacesMessage.SEVERITY_WARN,
     							vMessage, ""));
			}
			
		} else {
			
	        LOGGER.info("vUsername2FA:" + vUserName2FA);
			vError = false;
	        
	        if (serialNumber.isEmpty() || activationCode.isEmpty()) {
	        	vError = true;
	        	vMessage = "Please generate 2FA Token details.";
	        } else if (registrationCode.isEmpty()) {
	        	vError = true;
	        	vMessage = "Please input registration code.";
	        }
	        
	        if (vError) {
	        		System.out.println("Results: " + serialNumber + " " + activationCode + " " + registrationCode);
	        		FacesContext.getCurrentInstance().addMessage(null,
         					new FacesMessage(FacesMessage.SEVERITY_WARN,
         							vMessage, ""));
	        		return;
	        	}
	        
	        TwoFactorUtil util = new TwoFactorUtil();
	        
	        try {
	        	LOGGER.info("vUserName2FA: " + vUserName2FA);
	        	LOGGER.info("serialNumber: " + serialNumber);
	        	LOGGER.info("registrationCode: " + registrationCode);
	        	
	        	util.activateToken(vUserName2FA, serialNumber, registrationCode);
	        	vMessage = "Activated.";
	        	
	        	if (!(authDefault == 1)) {
					System.out.println("USER: " + user.getUserName());
					user.setAuthDefault((short)1);
					service.getFacade().edit(user, "SYSTEM");
				}
	        	
	        	sessionMap.put(TwoFactorAuthEnrollBean.ACTIVE_USER_2FA, user.getUserName());
	        	String returnString = "/TwoFactorAuth.xhtml?faces-redirect=true";
         		ExternalContext ec = FacesContext.getCurrentInstance()
     				.getExternalContext();
	        	
         		ec.redirect(ec.getRequestContextPath() + returnString);
	        
	        } catch (Exception e) {
	        	System.out.println("Exception: " + e.toString());
	        	
	        	FacesContext.getCurrentInstance().addMessage(null,
	        			new FacesMessage(FacesMessage.SEVERITY_WARN,
	        					"Failed 2FA Activation: " + e.toString(), ""));
	        }
		}
	}
	
	//2FA Authentication Screen
	public void authenticate() throws ServletException {
		LOGGER.info("2FA LOG: AUTHENTICATE! ");

		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		
		if (props.getDevtest().equalsIgnoreCase("true")) {
			
			try {
				String username = user.getUserName();
				String sessionId = ((HttpSession) context.getExternalContext().getSession(false)).getId();
				AuditTrail trail = new AuditTrail("Login", AuditTrail.ACTION_LOGIN);
				trail.setDetails("Session ID: " + sessionId + ", 2FA Login");
				trail.setAgencyGroupId(authBean.getAgencyGroup());
				trail.setCdate(new Date());
				trail.setCtime(new Date());
				LOGGER.info("username: " +  username);
				trail.setCuser(username);
				
				service.getFacade().logAuditTrail(trail);
				authBean.setAuthenticated(true);
				
				LOGGER.info("2FA LOG: Login Success");
				ExternalContext ec = FacesContext.getCurrentInstance()
						.getExternalContext();
				
					ec.redirect(ec.getRequestContextPath()
							+ "/index.xhtml?faces-redirect=true");
					 
					return;
					
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.log(Level.SEVERE, "ERROR: " + e.toString());
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Invalid Authentication code",""));
				}
			
		} else {
			
			TwoFactorUtil util = new TwoFactorUtil();
			
			try {
				String username = user.getUserName();
				LOGGER.info("vAuthCall details: " + vUserName2FA + " " + authenticationCode);
				vAuthCall = util.authenticate(vUserName2FA, authenticationCode);
				
				if (vAuthCall) {
					
					String sessionId = ((HttpSession) context.getExternalContext().getSession(false)).getId();
					AuditTrail trail = new AuditTrail("Login", AuditTrail.ACTION_LOGIN);
					trail.setDetails("Session ID: " + sessionId + ", 2FA Login");
					trail.setAgencyGroupId(authBean.getAgencyGroup());
					trail.setCdate(new Date());
					trail.setCtime(new Date());
					LOGGER.info("username: " +  username);
					trail.setCuser(username);
					
					service.getFacade().logAuditTrail(trail);
					authBean.setAuthenticated(true);
					
					LOGGER.info("2FA LOG: Login Success");
					ExternalContext ec = FacesContext.getCurrentInstance()
							.getExternalContext();
					
						ec.redirect(ec.getRequestContextPath()
								+ "/index.xhtml?faces-redirect=true");
						
						return;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				
				LOGGER.info("vAuthCall value: " + vAuthCall);
				Logger.getLogger(TwoFactorAuthEnrollBean.class.getName())
				.log(Level.SEVERE, "Error @ twoFactorAuth ", e.toString());
				System.out.println("error: " + e.getLocalizedMessage());
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"Invalid Authentication Code.", ""));
			}
		}
	}
	
	public void initializeProperties() {
		
		props = new TwoFactorAuthenticationProperties();
		try {
			
			props.initialize();
			
			/*LOGGER.info("2FA Properties Retrieved: " + props.getAdmin() + ", " + props.getAuth() + ", " + props.getRedirect() + ", " 
					+ props.getLogin() + ", " + props.getRetries() + ", " + props.getUsername() + ", "
					+ props.getPassword() + ", " + props.getVendor() + ", " + props.getTokens() + ", "
					+ props.getGroup() + ", " + props.getDevtest());*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
	
	//form1 next page
	public String getAuthenticationCode() {
		return authenticationCode;
	}

	public void setAuthenticationCode(String authenticationCode) {
		this.authenticationCode = authenticationCode;
	}
	
	public boolean isSessionInvalid() {
		return sessionInvalid;
	}
	
	public void setSessionInvalid(boolean sessionInvalid) {
		this.sessionInvalid = sessionInvalid;
	}

	public RefUserAccounts getUser() {
		return user;
	}

	public void setUser(RefUserAccounts user) {
		this.user = user;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getParms() {
		return parms;
	}

	public void setParms(String parms) {
		this.parms = parms;
	}

	public String getvUserName2FA() {
		return vUserName2FA;
	}

	public void setvUserName2FA(String vUserName2FA) {
		this.vUserName2FA = vUserName2FA;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getvAliasName() {
		return vAliasName;
	}

	public void setvAliasName(String vAliasName) {
		this.vAliasName = vAliasName;
	}

	public String getvFullName() {
		return vFullName;
	}

	public void setvFullName(String vFullName) {
		this.vFullName = vFullName;
	}

	public String getvMessage() {
		return vMessage;
	}

	public void setvMessage(String vMessage) {
		this.vMessage = vMessage;
	}

	public String getvSerialNo() {
		return vSerialNo;
	}

	public void setvSerialNo(String vSerialNo) {
		this.vSerialNo = vSerialNo;
	}

	public String getvActivationCode() {
		return vActivationCode;
	}

	public void setvActivationCode(String vActivationCode) {
		this.vActivationCode = vActivationCode;
	}

	public boolean isvAuthCall() {
		return vAuthCall;
	}

	public void setvAuthCall(boolean vAuthCall) {
		this.vAuthCall = vAuthCall;
	}

	public boolean isvError() {
		return vError;
	}

	public void setvError(boolean vError) {
		this.vError = vError;
	}	
	
}
