package com.bsp.fsccis.bean.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import com.bsp.fsccis.bean.crud.AddEditTag;
import com.bsp.fsccis.bean.crud.GenericCRUDBean;
import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.login.SingleLoginMonitor;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgency;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.entity.RefUserRole;
import com.bsp.fsccis.entity.SysRole;
import com.bsp.fsccis.entity.tag.CRUDTag;
import com.bsp.fsccis.entity.tag.U_Enabled_Tag;
import com.bsp.fsccis.mail.MailService;
import com.bsp.fsccis.service.UserService;

@Named("editUser")
@ViewScoped
public class EditUser extends GenericCRUDBean<RefUserAccounts> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(EditUser.class.getSimpleName());
	
	@Inject
	AuthBean auth;

	@Inject
	private SingleLoginMonitor monitor;

	@EJB
	private UserService service;
	
	@EJB
	private MailService mailService;
	
	private SysRole selectedRole;
	private List<RefUserAccounts> filteredUserList;
	private List<RefUserRole> focusRoleList;
	private List<RefUserRole> selectedRoles;
	private List<RefAgency> refAgency;
	private List<RefAgencyGroup> refAgencyGroup;
	private List<String> agencyGroupList;
	private List<String> agencyList;
	private List<SysRole> sysRoleList;
	
	private boolean resetpassword = false;
	private boolean needToReLogin = false;
	private boolean twoFactorAuthResetpassword = false;
	
	@PostConstruct
	public void initData(){
		setEntityList(service.getUsersWithRoles());
		if(!getEntityList().isEmpty()){
			for(RefUserAccounts rua : getEntityList()){
				initRoleDisplay(rua);
			}
		}
		
		refAgency = new ArrayList<RefAgency>(service.getFacade().findAll(RefAgency.class));
//		refAgencyGroup = new ArrayList<RefAgencyGroup>(service.getFacade().findAll(RefAgencyGroup.class));
		agencyGroupList = new ArrayList<String>(service.getFacade().getEntityManager().createNamedQuery("RefAgencyGroup.findAllAgencyGroupShortname", String.class).getResultList());
		agencyList = new ArrayList<String>(service.getFacade().getEntityManager().createNamedQuery("RefAgency.findAllAgencyShortname", String.class).getResultList());
		
		//log view		
		service.getFacade().logAuditTrail("User Management",AuditTrail.ACTION_VIEW,"User Management",auth.getAgencyGroup(),auth.getCuser());
	}
	
	public void resetLoginAttempts(RefUserAccounts user){
		try{
			service.resetLoginAttempts(user, auth.getCuser());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Unlock User","Action performed successfully."));
		}catch(Exception e){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Unlock User","Failed to perform action."));
		}
	}
	
	public void updateRefAgencyGroup(){
		if(this.entity!= null && this.entity.getAgencyId() != null){
			refAgencyGroup = new ArrayList<RefAgencyGroup>(service.getFacade().getEntityManager().createNamedQuery("RefAgencyGroup.findByAgencyIdNoDisabled", RefAgencyGroup.class).setParameter("agencyId", this.entity.getAgencyId()).getResultList());
		}
		updateSysRoleList();
	}
	
	public void initRoleDisplay(RefUserAccounts rua){
		LOGGER.info("initRoleDisplay");
		StringBuilder sb = new StringBuilder();
		for(RefUserRole rur : rua.getRefUserRoleList()){
			if(rur.getUEnabled() == U_Enabled_Tag.ENABLED){
				LOGGER.info("ROLE: " + rur.getSysRoles().getRoleName());
				sb.append(rur.getSysRoles().getRoleName()).append(", ");
			}
		} 
		if(!sb.toString().isEmpty()){
			rua.setDisplayRoleList(sb.toString().substring(0,sb.toString().length() - 2));
		}
	}
	
	public void resetPassword(){
		service.resetPassword(getEntity());
		resetpassword = true;
	}
	
	public void resetTwoFactorAuth(){
		service.reset2FAPassword(getEntity(), auth.getCuser());
		twoFactorAuthResetpassword = true;
	}
	
	public void initDialog(){
		
		this.resetpassword = false;
		this.twoFactorAuthResetpassword = false;
		
		LOGGER.info("initDialog(): " + addEditMode);
		if(addEditMode.equals(AddEditTag.ADD)){
			initNewEntity();
			entity.setUserId(-1L);
			focusRoleList = new ArrayList<RefUserRole>();
			if(refAgencyGroup != null && !refAgencyGroup.isEmpty()){
				refAgencyGroup.clear();
			}
		}else if(addEditMode.equals(AddEditTag.EDIT)){
			if(entity != null){
				if(getFilteredUserList() == null || getFilteredUserList().isEmpty()){
					entity = getEntityList().get(getEntityList().indexOf(entity));
				}else{
					entity = getFilteredUserList().get(getFilteredUserList().indexOf(entity));
				}
				if(entity.getRefUserRoleList() != null && !entity.getRefUserRoleList().isEmpty()){
					focusRoleList = new ArrayList<RefUserRole>();
					for(RefUserRole rur : entity.getRefUserRoleList()){
						if(rur.getUEnabled() == U_Enabled_Tag.ENABLED){
							focusRoleList.add(rur);
						}
					}
				}else{
					entity.setRefUserRoleList(new ArrayList<RefUserRole>());
					focusRoleList = new ArrayList<RefUserRole>();
				}
				updateRefAgencyGroup();
			}
		}
	}
	
	public void addRole(){
		LOGGER.info("addRole()");
		if(entity != null && selectedRole != null){
			if(entity.getRefUserRoleList() == null){
				LOGGER.info("here0");
				entity.setRefUserRoleList(new ArrayList<RefUserRole>());
			}
			
			RefUserRole toAdd = new RefUserRole(entity.getUserId(), selectedRole.getRoleId());
			
			if(!entity.getRefUserRoleList().contains(toAdd)){
				toAdd.setSysRoles(selectedRole);
				toAdd.setRefUserAccounts(entity);
				entity.getRefUserRoleList().add(toAdd);
				toAdd.setCrudStatus(CRUDTag.NEW);

				initRoleDisplay(entity);
				focusRoleList.add(toAdd);
			}else{
				RefUserRole lastCheck = entity.getRefUserRoleList().get(entity.getRefUserRoleList().indexOf(toAdd));
				if(lastCheck != null && lastCheck.getUEnabled() == U_Enabled_Tag.DISABLED){

					lastCheck.setUEnabled(U_Enabled_Tag.ENABLED);
					lastCheck.setCrudStatus(CRUDTag.EDIT);

					initRoleDisplay(entity);
					focusRoleList.add(lastCheck);
				} else {
					FacesContext.getCurrentInstance().addMessage("rolemsg", new FacesMessage("Selected role already exists."));
				}
			}
		}
	}
	
	public void disableRole(){
		LOGGER.info("disableRole()");
		if(entity != null && selectedRoles != null && !selectedRoles.isEmpty()){
			for(U_Enabled_Tag rur : selectedRoles){
				LOGGER.info("here7");
				focusRoleList.remove(rur);
				
				RefUserRole toUpdate = entity.getRefUserRoleList().get(entity.getRefUserRoleList().indexOf(rur));
				
				if(toUpdate.getCrudStatus() != CRUDTag.NEW){
					LOGGER.info("here8");
					toUpdate.setUEnabled(U_Enabled_Tag.DISABLED);
					toUpdate.setCrudStatus(CRUDTag.EDIT);
					initRoleDisplay(entity);
				}else if(toUpdate.getCrudStatus() == CRUDTag.NEW){
					LOGGER.info("here9");
					entity.getRefUserRoleList().remove(toUpdate);
				}
			}
		}else{
			LOGGER.info("here8");
			FacesContext.getCurrentInstance().addMessage("rolemsg", new FacesMessage("Select at least one role to delete."));
		}
	}
	
	@Override
	public void add() {
		try{
			entity.setAuthDefault((short)2);
			service.resetPassword(entity);
			service.add(entity,auth.getCuser());
			getEntityList().add(entity);
			
			if(getFilteredUserList() == null){
				setFilteredUserList(new ArrayList<RefUserAccounts>());
			}else{
				getFilteredUserList().clear();
			}
			
			getFilteredUserList().addAll(getEntityList());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Add", "Successful"));
			
			//email sending
			LOGGER.info("email sending add... "+ service.getDefaultPassword());
			mailService.sendEMail(entity, service.getDefaultPassword());
			
		}catch(Exception e){
			if(e.getMessage() != null && e.getMessage().contains("org.apache.openjpa.persistence.EntityExistsException")){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Username should be unique."));
			}else{
				LOGGER.log(Level.SEVERE,"ERROR: " + e);
				e.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Problem Encountered in Adding New Entity"));
			}
			
		}
	}
	
	@Override
	public void edit() {
		try {
			if (!focusRoleList.isEmpty()) {

				service.edit(entity, auth.getCuser());
				setEntityList(service.getUsersWithRoles());
				
				if (!getEntityList().isEmpty()) {
					for (RefUserAccounts rua : getEntityList()) {
						initRoleDisplay(rua);
					}
					
				}

				if(getFilteredUserList() == null){
					setFilteredUserList(new ArrayList<RefUserAccounts>());
				}else{
					getFilteredUserList().clear();
				}
				getFilteredUserList().addAll(getEntityList());
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Edit", "Successful"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Select at least one role."));
			}
			
			//email sending
			if (resetpassword) {
				LOGGER.info("email sending edit... "+ service.getDefaultPassword());
				mailService.sendEMailResetPassword(entity, service.getDefaultPassword());
				
				if (entity.getCuser().equals(entity.getUserName())) {
					needToReLogin = true;
					//logout and re-login account
					if (needToReLogin) {
						LOGGER.info("logout");
						FacesContext context = FacesContext.getCurrentInstance();
						HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
						
						try {
							String sessionId = ((HttpSession) context.getExternalContext().getSession(false)).getId();
							AuditTrail trail = new AuditTrail("Logout", AuditTrail.ACTION_LOGOUT);
							trail.setDetails("Session ID: " + sessionId + ", Reset Password");
							trail.setAgencyGroupId(auth.getAgencyGroup());
							trail.setCdate(new Date());
							trail.setCtime(new Date());
							if (auth.getCuser() != null) {
								trail.setCuser(new String(auth.getCuser()));
							}
							
							HttpSession session = monitor.unregisterUserSession(auth.getCuser());
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
						ec.redirect(ec.getRequestContextPath()
								+ "/index.xhtml?faces-redirect=true");
						
						return;
					}
				}
			}
			
			if (twoFactorAuthResetpassword) {
				LOGGER.info("email sending edit 2FA... ");
				 mailService.sendEmail2FAPassword(entity);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE,"ERROR: " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage("Error Edit",
							"Problem Encountered in Editing Entity"));
		}
	}

	public void updateSysRoleList(){
		sysRoleList = new ArrayList<SysRole>(service.getFacade().findAll(SysRole.class));
		if(entity.getRefAgencyGroupId() == null || !entity.getRefAgencyGroupId().getAccessLevel().equals(RefAgencyGroup.SYSTEM_OWNER)){
			sysRoleList = new ArrayList<SysRole>(service.getFacade().getEntityManager()
					.createNamedQuery("SysRole.findNotRoleId", SysRole.class)
					.setParameter("roleId", SysRole.SYSTEM_ADMIN).getResultList());
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

	public SysRole getSelectedRole() {
		return selectedRole;
	}

	public void setSelectedRole(SysRole selectedRole) {
		this.selectedRole = selectedRole;
	}

	public List<RefUserRole> getFocusRoleList() {
		return focusRoleList;
	}

	public void setFocusRoleList(List<RefUserRole> focusRoleList) {
		this.focusRoleList = focusRoleList;
	}

	public List<RefUserRole> getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(List<RefUserRole> selectedRoles) {
		this.selectedRoles = selectedRoles;
	}

	public List<RefUserAccounts> getFilteredUserList() {
		return filteredUserList;
	}

	public void setFilteredUserList(List<RefUserAccounts> filteredUserList) {
		this.filteredUserList = filteredUserList;
	}

	public List<RefAgencyGroup> getRefAgencyGroup() {
		return refAgencyGroup;
	}

	public void setRefAgencyGroup(List<RefAgencyGroup> refAgencyGroup) {
		this.refAgencyGroup = refAgencyGroup;
	}

	public List<String> getAgencyGroupList() {
		return agencyGroupList;
	}

	public void setAgencyGroupList(List<String> agencyGroupList) {
		this.agencyGroupList = agencyGroupList;
	}

	public List<String> getAgencyList() {
		return agencyList;
	}

	public void setAgencyList(List<String> agencyList) {
		this.agencyList = agencyList;
	}

	public List<RefAgency> getRefAgency() {
		return refAgency;
	}

	public void setRefAgency(List<RefAgency> refAgency) {
		this.refAgency = refAgency;
	}

	public List<SysRole> getSysRoleList() {
		return sysRoleList;
	}

	public void setSysRoleList(List<SysRole> sysRoleList) {
		this.sysRoleList = sysRoleList;
	}

	public boolean isResetpassword() {
		return resetpassword;
	}

	public void setResetpassword(boolean resetpassword) {
		this.resetpassword = resetpassword;
	}

	public boolean isNeedToReLogin() {
		return needToReLogin;
	}

	public void setNeedToReLogin(boolean needToReLogin) {
		this.needToReLogin = needToReLogin;
	}

	public boolean isTwoFactorAuthResetpassword() {
		return twoFactorAuthResetpassword;
	}

	public void setTwoFactorAuthResetpassword(boolean twoFactorAuthResetpassword) {
		this.twoFactorAuthResetpassword = twoFactorAuthResetpassword;
	}



	
}
