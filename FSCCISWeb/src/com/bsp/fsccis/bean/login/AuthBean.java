package com.bsp.fsccis.bean.login;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.bsp.fsccis.entity.RefAgencyGroup;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger LOGGER = Logger.getLogger(AuthBean.class.getSimpleName());
	
	private String name;
	private String username;
	private String password;
	private boolean loggedIn;
	private String cuser;
	private RefAgencyGroup agencyGroup;
	private String cssFileName;
	private Boolean isBrowserCompatible = true;
	
	private Boolean testMode = true;
	
	private boolean sessionExistTag = false;
	private boolean authenticated = false;
	
	@PostConstruct
	public void init(){
		LOGGER.info("AuthBean init");
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();

		Double fullVersion;
		if (request.getHeader("User-Agent").indexOf("MSIE") != -1) {
			String line = request.getHeader("User-Agent");
			String pattern = "MSIE ([0-9]{1,}[\\.0-9]{0,})";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);			
			if (m.find()){
				fullVersion = Double.parseDouble(m.group(1));
				if(fullVersion < 9)
					cssFileName = "styles_ie_08.css";
			}
			
			isBrowserCompatible = false;
		}else{
			cssFileName = "styles.css";
		}
		
	}
	
	public long getSessionTimeoutInMilliseconds() {
		HttpServletRequest request = ((HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest());
		return (request.getSession().getMaxInactiveInterval()) * 1000;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isLoggedIn() {
		return loggedIn;
	}
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getCuser() {
		return cuser;
	}

	public void setCuser(String cuser) {
		this.cuser = cuser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RefAgencyGroup getAgencyGroup() {
		return agencyGroup;
	}

	public void setAgencyGroup(RefAgencyGroup agencyGroup) {
		this.agencyGroup = agencyGroup;
	}

	public String getCssFileName() {
		return cssFileName;
	}

	public void setCssFileName(String cssFileName) {
		this.cssFileName = cssFileName;
	}

	public Boolean getIsBrowserCompatible() {
		return isBrowserCompatible;
	}

	public void setIsBrowserCompatible(Boolean isBrowserCompatible) {
		this.isBrowserCompatible = isBrowserCompatible;
	}

	public Boolean getTestMode() {
		return testMode;
	}

	public void setTestMode(Boolean testMode) {
		this.testMode = testMode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSessionExistTag() {
		return sessionExistTag;
	}

	void setSessionExistTag(boolean sessionExistTag) {
		this.sessionExistTag = sessionExistTag;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

}
