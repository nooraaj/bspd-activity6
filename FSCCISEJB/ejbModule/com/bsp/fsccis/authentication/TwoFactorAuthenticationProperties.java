package com.bsp.fsccis.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwoFactorAuthenticationProperties {

	private static final Logger log = Logger.getLogger(TwoFactorAuthenticationProperties.class.getName());
	
	private String admin;
	private String auth;
	private String redirect;
	private String login;
	private String retries;
	private String username;
	private String password;
	private String vendor;
	private String tokens;
	private String group;
	private String devtest;
	private String email;
	private String zone;
	
	public void initialize() {
		
		Properties properties = new Properties();
		
		try {
			
			InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("fsf-eis.properties");
			
			properties.load(stream);
			
			admin = properties.getProperty("idg.url.admin");
			auth = properties.getProperty("idg.url.auth");
			redirect = properties.getProperty("idg.url.redirect");
			login = properties.getProperty("idg.url.login");
			retries = properties.getProperty("idg.num.retries");
			username = properties.getProperty("idg.admin.username");
			password = properties.getProperty("idg.admin.password");
			vendor = properties.getProperty("idg.eis.vendor");
			tokens = properties.getProperty("idg.eis.tokens");
			group = properties.getProperty("idg.eis.group");
			devtest = properties.getProperty("devtesting");
			email = properties.getProperty("email");
			zone = properties.getProperty("zone");
			
			log.log(Level.INFO, "2FA Properties Retrieved: " + admin + ", " + auth + ", " + redirect + ", " 
											+ login + ", " + retries + ", " + username + ", "
											+ password + ", " + vendor + ", " + tokens + ", "
											+ group + ", " + devtest + ", " + email + ", "
											+ zone);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getRetries() {
		return retries;
	}

	public void setRetries(String retries) {
		this.retries = retries;
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

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getTokens() {
		return tokens;
	}

	public void setTokens(String tokens) {
		this.tokens = tokens;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDevtest() {
		return devtest;
	}

	public void setDevtest(String devtest) {
		this.devtest = devtest;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
	
}
