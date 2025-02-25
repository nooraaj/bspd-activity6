package com.bsp.fsccis.bean.login;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

@Named("singleLoginMonitor")
@ApplicationScoped
public class SingleLoginMonitor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(SingleLoginMonitor.class.getSimpleName());

	private static Map<String, HttpSession> logins = new HashMap<String, HttpSession>();

	
	public Map<String, HttpSession> getLogins(){ 
		return logins;
	}
	
	public boolean isLoggedIn(String username){
		LOGGER.info("isLoggedIn() " + username);
		return logins.get(username) != null ? true:false;
	}
	
	public void registerUserSession(String username, HttpSession session){
		LOGGER.info("registerUserSession(): " + username);
		logins.put(username, session);		 
	}
	
	public HttpSession unregisterUserSession(String username){
		LOGGER.info("unregisterUserSession() " + username);
		return logins.remove(username);
	}
}
