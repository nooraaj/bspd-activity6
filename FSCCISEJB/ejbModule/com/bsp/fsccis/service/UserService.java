package com.bsp.fsccis.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.bsp.encryption.PasswordValidator;
import com.bsp.encryption.SHA256_UTF8_SALT;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.entity.RefUserRole;
import com.bsp.fsccis.entity.RefUserRolePK;
import com.bsp.fsccis.entity.SysConfiguration;
import com.bsp.fsccis.entity.SysRole;
import com.bsp.fsccis.entity.tag.CRUDTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.GetNextIdUtil;

@Stateless
public class UserService {
	private static final Logger LOGGER = Logger.getLogger(UserService.class
			.getSimpleName());

	@EJB
	GenericFacade facade;
	private String defaultPassword;
	public GenericFacade getFacade() {
		return this.facade;
	}

	public List<RefUserAccounts> getUsersWithRoles() {
		LOGGER.info("getUsersWithRoles");
		List<RefUserAccounts> result = new ArrayList<RefUserAccounts>(
				facade.findAll(RefUserAccounts.class));

		for (RefUserAccounts rua : result) {
			rua.getRefUserRoleList();
		}

		return result;
	}
	
	@Deprecated
	public List<RefUserAccounts> getUsersByRole(int roleId){
		LOGGER.info("getUsersByRole(): " + roleId);
		List<RefUserRole> userRoleList = facade.getEntityManager().createNamedQuery("RefUserRole.findByRoleIdEnabled",RefUserRole.class).setParameter("roleId", SysRole.ACCOUNT_MANAGER).getResultList();
		List<RefUserAccounts> result = new ArrayList<RefUserAccounts>();
		
		for(RefUserRole rur : userRoleList){
			result.add(rur.getRefUserAccounts());
		}
		
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void add(RefUserAccounts toAdd, String cuser) {
		LOGGER.info("add");
		toAdd.setCuser(cuser);
		
		PKTag_GetMax pk = (PKTag_GetMax) toAdd;
		pk.setIdToGenerate(GetNextIdUtil.getNextId(pk, facade.getEntityManager()));
		
		if (toAdd.getRefUserRoleList() != null) {
			for (RefUserRole rur : toAdd.getRefUserRoleList()) {				
				rur.setRefUserRolePK(new RefUserRolePK(toAdd.getUserId(), rur.getSysRoles().getRoleId()));
				rur.setCuser(cuser);
			}
		}	
		
		facade.create(toAdd);
				
		if (toAdd.getRefUserRoleList() != null) {
			for (RefUserRole rur : toAdd.getRefUserRoleList()) {				
				facade.create(rur);
			}
		}	
		
	}

	public void edit(RefUserAccounts toEdit, String cuser) {
		LOGGER.info("edit");
		if (toEdit.getRefUserRoleList() != null) {
			for (RefUserRole rur : toEdit.getRefUserRoleList()) {
				if (rur.getCrudStatus() == CRUDTag.NEW) {
					rur.setCuser(cuser);
					facade.create(rur);
				} else if (rur.getCrudStatus() == CRUDTag.EDIT) {
					rur.setCuser(cuser);
					facade.edit(rur);
				}

			}
		}
		
		toEdit.setCuser(cuser);
		facade.edit(toEdit);
	}

	public void invalidLogin(RefUserAccounts user, String cuser) {
		LOGGER.info("invalidLogin() for "+user.getCuser());
		user.setLoginAttempts((short) (user.getLoginAttempts() + 1));
		user.setCuser(cuser);
		facade.edit(user);
	}
	
	public void resetLoginAttempts(RefUserAccounts user, String cuser) {
		user.setCuser(cuser);
		user.setLoginAttempts((short) 0);
		facade.edit(user);
	}

	public RefUserAccounts getUser(String username) {
		RefUserAccounts result = facade
				.getEntityManager()
				.createNamedQuery("RefUserAccounts.findByUserName",
						RefUserAccounts.class)
				.setParameter("userName", username).getSingleResult();
		//TODO: set refUserRoleList without looping
		List<RefUserRole> refUserRoleList =  new ArrayList<RefUserRole>();
		for(RefUserRole rur :result.getRefUserRoleList()){
			if(rur.getUEnabled() == 1){
				refUserRoleList.add(rur);
			}
		}
		
//		RefAgencyStorage storage = facade.find(result.getRefAgency().getAgencyId(), RefAgencyStorage.class);
//		LOGGER.info("storage: " + storage.getDisplayName());
//		result.getRefAgency().setRefAgencyStorage(storage);
		result.setRefUserRoleList(refUserRoleList);
		
		return result;
	}

	public void resetPassword(RefUserAccounts user) {
		LOGGER.info("resetPassword()");
		// LOGGER.info("PREV: " + user.getUserPw());
		//String newPw = SHA256_UTF8_SALT.encrypt(user.getUserName());
		String newPw = SHA256_UTF8_SALT.encrypt(defaultPassword());
		//LOGGER.info("NEW: " + newPw);
		user.setUserPw(newPw);
		// Set Password as expired to force user to replace password upon login
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		user.setPwDateExpiration(cal.getTime());
	}

	public boolean validate(String username, String password) {
		try {
			String pw = SHA256_UTF8_SALT.encrypt(password);
			LOGGER.info("validate: " + username+" " +  pw);
			facade.getEntityManager()
					.createQuery(
							"SELECT r FROM RefUserAccounts r WHERE r.userName = :userName and r.userPw = :userPw",
							RefUserAccounts.class)
					.setParameter("userName", username)
					.setParameter("userPw", pw)
					.getSingleResult();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static final int PWD_SUCCESS = 0;
	public static final int PWD_SAME_AS_PREVIOUS = -1;
	public static final int PWD_NEW_INVALID = -2;
	public static final int PWD_VALIDITY = 60;

	public int changePassword(RefUserAccounts user, String oldpassword, String newpassword,
			String cuser) {
		LOGGER.info("changePassword()");
		if (newpassword.contains(" ")
				|| !new PasswordValidator().validate(newpassword)) {
			LOGGER.info("changePassword()1");
			return PWD_NEW_INVALID;
		}

		String oldpw = SHA256_UTF8_SALT.encrypt(oldpassword);
		String newpw = SHA256_UTF8_SALT.encrypt(newpassword);

		if (oldpw.equals(newpw)) {
			LOGGER.info("changePassword()2");
			return PWD_SAME_AS_PREVIOUS;
		}

		user.setUserPw(newpw);
		String expiryDays = getFacade()
		.getEntityManager()
		.createNamedQuery("SysConfiguration.findValueByName",
				String.class)
		.setParameter("name", SysConfiguration.PASSWORD_RETENTION)
		.getSingleResult();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, Integer.parseInt(expiryDays));
		user.setPwDateExpiration(cal.getTime());
		user.setCuser(cuser);

		 facade.edit(user);

		LOGGER.info("changePassword()3");
		return PWD_SUCCESS;
	}
	
	public String defaultPassword() {

		String defaultPass = "";
		int max = 72;
		int min = 1;
		// int range = max - 1;

		String stringChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()";

		for (int i = 0; i < 10; i++) {
			int rand = (int) (Math.random() * (max - min)) + min;

			// defaultPass += stringChar.substring(rand, rand + 1);
			defaultPass += stringChar.charAt(rand);

			// Output is different everytime this code is executed
			System.out.println(rand);
		}
		
		setDefaultPassword(defaultPass);
		//LOGGER.info("NEW PASSWORD!!! " + defaultPass);
		return defaultPass;
	}
	
	public String getDefaultPassword() {
		return defaultPassword;
	}

	public void setDefaultPassword(String defaultPassword) {
		this.defaultPassword = defaultPassword;
	}
	
	public void reset2FAPassword(RefUserAccounts user, String cuser) {
		user.setCuser(cuser);
		user.setAuthDefault((short) 2);
		facade.edit(user);
	}
}
