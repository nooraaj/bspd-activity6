package com.bsp.fsccis.bean.reference;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import com.bsp.fsccis.bean.crud.GenericCRUD;
import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.util.SelectMenuManyList;
import com.bsp.fsccis.entity.RefAgency;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.service.ReportService;
import com.bsp.fsccis.util.ClassBean;
import com.bsp.fsccis.util.ClassDisplayName;

@Named("editAgency")
@ViewScoped
public class EditAgency implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger
			.getLogger(EditAgency.class.getSimpleName());

	@Inject
	AuthBean auth;
	
	@Inject
	private GenericCRUD crudBean;

	@EJB
	private ReportService service;

	private String focusEntity;
	private SelectMenuManyList<RefAgency> agency;
	private SelectMenuManyList<RefAgencyGroup> agencyGroup;
	private RefAgency focusAgency;
	private RefAgencyGroup focusAgencyGroup;
	
	private SelectMenuManyList<RefAgencyGroup> trustedGroup;
	
	private boolean includeDisabled = false;

	@PostConstruct
	public void init() {
		LOGGER.info("init");

		setAgency(new SelectMenuManyList<RefAgency>() {
		});
		setAgencyGroup(new SelectMenuManyList<RefAgencyGroup>() {
		});

		getAgency().setList(service.getFacade()
				.getEntityManager()
				.createNamedQuery("RefAgency.findAllOrderByNm",
						RefAgency.class).getResultList());

	}

	public void add() {
		crudBean.add();
		refreshList();
	}
	
	public void addAgencyGroup(){
		try{
			if (crudBean.getEntity() instanceof BSPTimeStampable) {
				BSPTimeStampable bts = (BSPTimeStampable) crudBean.getEntity();
				bts.setCuser(auth.getCuser());
			}
			
			if (crudBean.getEntity() instanceof RefAgencyGroup) {
				RefAgencyGroup newRag = (RefAgencyGroup) crudBean.getEntity();
				focusAgencyGroup = newRag;
			}
			add();
		}catch(Exception e){
			e.printStackTrace();
//			LOGGER.severe(e.getMessage());
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Failed to create new Agency."));
			initNewEntity();
			return;
		}
		
		LOGGER.info("CREATE");
		String rootDirectory = service.getRootDirectory();
		File newFolder = new File(rootDirectory + File.separatorChar + RefFileProperty.ROOT_AGENCY_GROUP_FOLDER_NAME_PREFIX + focusAgencyGroup.getAgencyGroupId());
		if(!newFolder.exists() && newFolder.mkdir()){
		}else{
			LOGGER.info("Failed to create agency: " + newFolder.getAbsolutePath());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Error Add", "Failed to create new Agency"));
			service.getFacade().remove(focusAgencyGroup);
		}
//		getAgencyGroup().setList(new ArrayList<RefAgencyGroup>(service.getFacade().findAll(RefAgencyGroup.class)));
	}

	private void refreshList() {
		getAgency().setList(service.getFacade()
				.getEntityManager()
				.createNamedQuery("RefAgency.findAllOrderByNm",
						RefAgency.class).getResultList());

		if(getFocusAgency() != null){
			if(includeDisabled){
				getAgencyGroup().setList(service.getFacade()
						.getEntityManager()
						.createNamedQuery("RefAgencyGroup.findByAgencyId",
								RefAgencyGroup.class)
						.setParameter("agencyId", getFocusAgency())
						.getResultList());
			}else{
				getAgencyGroup().setList(service.getFacade()
						.getEntityManager()
						.createNamedQuery("RefAgencyGroup.findByAgencyIdNoDisabled",
								RefAgencyGroup.class)
						.setParameter("agencyId", getFocusAgency())
						.getResultList());
			}
			
			
		}
	}
	
	public void initTrustedGroup(){
		if(trustedGroup == null){
			trustedGroup = new SelectMenuManyList<RefAgencyGroup>() {};
		}
		
		if(trustedGroup != null && (trustedGroup.getList() == null || trustedGroup.getList().isEmpty())){
			trustedGroup.setList(service.getFacade()
					.getEntityManager().createNamedQuery("RefAgencyGroup.findAllNoDisabled", RefAgencyGroup.class)
					.getResultList());
		}
		
//		if(agencyGroup != null && agencyGroup.getSelectedList() != null && agencyGroup.getSelectedList().size() == 1){
			LOGGER.info("agroup: " + focusAgencyGroup);
			List<RefAgencyGroup> list = service.getTrustedGroup(focusAgencyGroup);
			LOGGER.info("result: " + list.toString());
				trustedGroup.setSelectedList(list);
//		}
	}
	
	public void updateTrustedGroup(AjaxBehaviorEvent abe){
		service.updateTrustedGroup(focusAgencyGroup, trustedGroup.getSelectedList(), auth.getCuser());
	}
	
	public void edit() {
		crudBean.edit();
		refreshList();
	}

	public void initNewEntity() {
		LOGGER.info("focusEntity: " + focusEntity);
		ClassDisplayName cdn = ClassBean.getInstance().getClassMap()
				.get(focusEntity);
		Class<?> entityClass = cdn.getCls();
		crudBean.setEntityClass(entityClass);
		crudBean.initNewEntity();

		if (focusEntity.equalsIgnoreCase("RefAgencyGroup") && getFocusAgency() != null) {
			RefAgencyGroup agencyGroup = new RefAgencyGroup();
			agencyGroup.setAgencyId(getFocusAgency());
			crudBean.setEntity(agencyGroup);
		}
	}
	
	public void initEditEntity() {
		LOGGER.info("focusEntity: " + focusEntity);
		ClassDisplayName cdn = ClassBean.getInstance().getClassMap()
				.get(focusEntity);
		Class<?> entityClass = cdn.getCls();
		crudBean.setEntityClass(entityClass);
		crudBean.initNewEntity();

		if (focusEntity.equalsIgnoreCase("RefAgency")) {
			crudBean.setEntity(getFocusAgency());
		} else if (focusEntity.equalsIgnoreCase("RefAgencyGroup")) {
			crudBean.setEntity(getFocusAgencyGroup());
		}
	}
	
	public void updateAgency() {
		LOGGER.info("updateAgency()");
		if (getAgency().getSelectedList() != null
				&& !getAgency().getSelectedList().isEmpty()) {
			if (getAgency().getSelectedList().size() == 1) {
				setFocusAgency(getAgency().getSelectedList().get(0));
 
				if(includeDisabled){
					getAgencyGroup().setList(service.getFacade()
							.getEntityManager()
							.createNamedQuery("RefAgencyGroup.findByAgencyId",
									RefAgencyGroup.class)
							.setParameter("agencyId", getFocusAgency())
							.getResultList());
				}else{
					getAgencyGroup().setList(service.getFacade()
							.getEntityManager()
							.createNamedQuery("RefAgencyGroup.findByAgencyIdNoDisabled",
									RefAgencyGroup.class)
							.setParameter("agencyId", getFocusAgency())
							.getResultList());
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage("Selection",
								"Multiple selection not allowed"));
			}
		}
	}
	
	public void updateAgencyGroup() {
		LOGGER.info("updateAgencyGroup()");
		if (getAgencyGroup().getSelectedList() != null && !getAgencyGroup().getSelectedList().isEmpty()) {
			if (getAgencyGroup().getSelectedList().size() == 1) {
				setFocusAgencyGroup(getAgencyGroup().getSelectedList().get(0));
				initTrustedGroup();
			}
		}
	}

	public String getFocusEntity() {
		return focusEntity;
	}

	public void setFocusEntity(String focusEntity) {
		this.focusEntity = focusEntity;
	}

	public SelectMenuManyList<RefAgency> getAgency() {
		return agency;
	}

	public void setAgency(SelectMenuManyList<RefAgency> agency) {
		this.agency = agency;
	}

	public SelectMenuManyList<RefAgencyGroup> getAgencyGroup() {
		return agencyGroup;
	}

	public void setAgencyGroup(SelectMenuManyList<RefAgencyGroup> agencyGroup) {
		this.agencyGroup = agencyGroup;
	}

	public RefAgency getFocusAgency() {
		return focusAgency;
	}

	public void setFocusAgency(RefAgency focusAgency) {
		this.focusAgency = focusAgency;
	}

	public RefAgencyGroup getFocusAgencyGroup() {
		return focusAgencyGroup;
	}

	public void setFocusAgencyGroup(RefAgencyGroup focusAgencyGroup) {
		this.focusAgencyGroup = focusAgencyGroup;
	}

	public boolean isIncludeDisabled() {
		return includeDisabled;
	}

	public void setIncludeDisabled(boolean includeDisabled) {
		this.includeDisabled = includeDisabled;
	}

	public SelectMenuManyList<RefAgencyGroup> getTrustedGroup() {
		return trustedGroup;
	}

	public void setTrustedGroup(SelectMenuManyList<RefAgencyGroup> trustedGroup) {
		this.trustedGroup = trustedGroup;
	} 
	
}
