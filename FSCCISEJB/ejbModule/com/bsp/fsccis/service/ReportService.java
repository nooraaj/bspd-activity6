package com.bsp.fsccis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import com.bsp.fsccis.entity.RefAgency;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisible;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.entity.SysConfiguration;
import com.bsp.fsccis.facade.GenericFacade;

@Stateless
public class ReportService {
	private static final Logger LOGGER = Logger.getLogger(ReportService.class
			.getSimpleName());

	@EJB
	GenericFacade facade;

	private String rootDirectory;

	@PostConstruct
	public void init() {
		LOGGER.info("init");
		
		System.out.println("getFacade: " + getFacade());
		System.out.println("getFacade().getEntityManager(): " + getFacade().getEntityManager());
		List<String> result = getFacade()
				.getEntityManager()
				.createNamedQuery("SysConfiguration.findValueByName",
						String.class)
				.setParameter("name", SysConfiguration.ROOT_FOLDER_DIRECTORY)
				.getResultList();
		if(result != null && !result.isEmpty()){
			rootDirectory = result.get(0);
		}
//		rootDirectory = "D:/FSCCIS/";
		LOGGER.info("ROOT DIRECTORY: " + rootDirectory);
	}
	
	public List<RefAgency> getAgencyList(boolean includeDisabled){
		List<RefAgency> result = null;
		
		result = facade.findAll(RefAgency.class);
		
		List<RefAgencyGroup> temp = new ArrayList<RefAgencyGroup>();
		
		for(RefAgency ra : result){
			if(ra.getRefAgencyGroupList() != null && !ra.getRefAgencyGroupList().isEmpty()){
				for(RefAgencyGroup rag : ra.getRefAgencyGroupList()){
					if(!includeDisabled && rag.getAccessLevel() != RefAgencyGroup.DISABLED){
						temp.add(rag);
					}
				}
			}
			if(!includeDisabled){
				ra.setRefAgencyGroupList(temp);
				temp = new ArrayList<RefAgencyGroup>();
			}
		}
		
		
		return result;
	}
	
	public RefAgencyGroupFolderVisible getAgencyFolderVisibleByOwnerTarget(RefAgencyGroup ownerGroupId, RefAgencyGroup targetGroupId){
		RefAgencyGroupFolderVisible rafv = null;
		try{
			rafv = getFacade().getEntityManager().
			createNamedQuery("RefAgencyGroupFolderVisible.findByOwnerAgencyGroupTargetAgency", RefAgencyGroupFolderVisible.class).
			setParameter("ownerGroupId", ownerGroupId).setParameter("targetGroupId", targetGroupId).getSingleResult();
			
			rafv.getOwnerGroupId();
			rafv.getTargetGroupId();
			
		}catch(Exception e){
			LOGGER.info("RefAgencyGroupFolderVisible not found. Create new.");
		}
		
		return rafv;
	}
	
	public List<RefAgencyGroupFolderVisible> getAgencyFolderVisibleByOwner(RefAgencyGroup ownerAgency){
		List<RefAgencyGroupFolderVisible> list = null;
		
		list = getFacade().getEntityManager().
		createNamedQuery("RefAgencyGroupFolderVisible.findByOwnerAgencyGroup", RefAgencyGroupFolderVisible.class).
		setParameter("ownerGroupId", ownerAgency).getResultList();
		
		return list;
	}
	
	public List<RefAgencyGroupFolderVisible> getAgencyFolderVisibleByTargetAndIsFolderVisible(RefAgencyGroup targetAgency){
		List<RefAgencyGroupFolderVisible> list = null;
		
		list = getFacade().getEntityManager().
		createNamedQuery("RefAgencyGroupFolderVisible.findByTargetAgencyAndIsFolderVisible", RefAgencyGroupFolderVisible.class).
		setParameter("targetGroupId", targetAgency).getResultList();
		
		return list;
	}
	
	public void updateTrustedGroup(RefAgencyGroup owner,List<RefAgencyGroup> targetList,String cuser){
		String query = "DELETE FROM \"FSCC-IS\".TRUSTED_GROUPS WHERE OWNER_GROUP_ID = " + owner.getAgencyGroupId();
		
		int result = getFacade().getEntityManager().createNativeQuery(query).executeUpdate();
		
		query = "DELETE FROM \"FSCC-IS\".REF_FILE_PERMISSION WHERE OWNER_GROUP_ID = " + owner.getAgencyGroupId();
		
		result = getFacade().getEntityManager().createNativeQuery(query).executeUpdate();
		if(targetList == null || targetList.isEmpty()){
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<targetList.size();i++){
			RefAgencyGroup rag = targetList.get(i);
			
			sb.append(rag.getAgencyGroupId());
			
			if(i < targetList.size() - 1){
				sb.append(",");
			}
		}
		
		query = "INSERT INTO \"FSCC-IS\".TRUSTED_GROUPS (OWNER_GROUP_ID, TARGET_GROUP_ID, CDATE, CTIME, CUSER)" +
				"SELECT " + owner.getAgencyGroupId() + ",AGENCY_GROUP_ID,CURRENT_DATE,CURRENT_TIME,'" + cuser + "'" +
				" FROM \"FSCC-IS\".REF_AGENCY_GROUP WHERE AGENCY_GROUP_ID IN (" + sb.toString() + ")";
		
		
		result = getFacade().getEntityManager().createNativeQuery(query).executeUpdate();
		
		query = "INSERT INTO \"FSCC-IS\".REF_FILE_PERMISSION "
				+ " (FILE_PROPERTY_ID, TARGET_GROUP_ID, OWNER_GROUP_ID, VISIBLE, CDATE, CTIME, CUSER) "
				+ " SELECT RFP.FILE_PROPERTY_ID,RAG.AGENCY_GROUP_ID AS TARGET_GROUP_ID,RFP.OWNER_GROUP_ID "
				+ "		," + RefFilePermission.VISIBLE + ",CURRENT_DATE,CURRENT_TIME,'" + cuser + "'" 
				+ " FROM \"FSCC-IS\".REF_FILE_PROPERTY RFP "
				+ " LEFT JOIN \"FSCC-IS\".REF_AGENCY_GROUP RAG ON RAG.AGENCY_GROUP_ID IN (" + sb.toString() + ")"
				+ " WHERE RFP.FILE_TYPE != 1 AND RFP.OWNER_GROUP_ID = " + owner.getAgencyGroupId();

		LOGGER.info("query: " + query);
		result = getFacade().getEntityManager().createNativeQuery(query).executeUpdate();
	}
	
	@Resource SessionContext ctx;
	
	public List<RefFilePermission> getVisibleFilePermissionBy(RefAgencyGroup ownerGroupId, RefAgencyGroup targetGroupId){

		LOGGER.info(ctx.getCallerPrincipal().getName() + ":" + "owner: " + ownerGroupId.getAgencyGroupShortname());
		LOGGER.info(ctx.getCallerPrincipal().getName() + ":" + "target: " + targetGroupId.getAgencyGroupShortname());
		List<RefFilePermission> list = null;
		
		list = getFacade().getEntityManager().
				createNamedQuery("RefFilePermission.findByOwnerTargetAndIsVisible", RefFilePermission.class).
				setParameter("ownerGroupId", ownerGroupId).
				setParameter("targetGroupId", targetGroupId).getResultList();
		
		return list;
	}

	public RefUserAccounts getUserPermission(String username) {
		RefUserAccounts user = facade
				.getEntityManager()
				.createNamedQuery("RefUserAccounts.findByUserName",
						RefUserAccounts.class)
				.setParameter("userName", username).getSingleResult();
		if (user != null) {
			for (RefFilePermission rfp : user.getRefAgencyGroupId()
					.getRefFilePermissionList()) {
			}
		}
		return user;
	}

	public RefAgencyGroup getAgencyPermission(String agencyGroupShortname) {
		RefAgencyGroup agency = facade
				.getEntityManager()
				.createNamedQuery("RefAgencyGroup.findByAgencyGroupShortname",
						RefAgencyGroup.class)
				.setParameter("agencyGroupShortname", agencyGroupShortname).getSingleResult();

		for (RefFilePermission rfp : agency.getRefFilePermissionList()){}
		
		return agency;
	}

	public List<RefFileProperty> loadAllAgencyFiles(RefAgencyGroup ownerGroupId) {
		List<RefFileProperty> result = null;

		result = facade
				.getEntityManager()
				.createNamedQuery("RefFileProperty.findAllAgencyGroupFiles",
						RefFileProperty.class).setParameter("ownerGroupId", ownerGroupId).getResultList();

		for (RefFileProperty rfp : result) {
			for (RefFilePermission rfpp : rfp.getRefFilePermissionList()) {
			}
		}

		return result;
	}

	public GenericFacade getFacade() {
		return this.facade;
	}

	public void delete(RefFileProperty toDelete) {
		LOGGER.info("toDelete: " + toDelete);
		LOGGER.info("toDelete2: " + toDelete.getRefFilePermissionList());

		toDelete = getFacade().find(toDelete.getPk(), RefFileProperty.class);

		// LOGGER.info("toDelete3: " + toDelete.getRefFilePermissionList());

		for (RefFilePermission perm : toDelete.getRefFilePermissionList()) {
			getFacade().remove(perm);
		}
		getFacade().remove(toDelete);
	}
	
	public void deleteUploaded(RefFileProperty toDelete, String folderName) {
		LOGGER.info("toDelete: " + toDelete);
		LOGGER.info("toDelete2: " + toDelete.getRefFilePermissionList());

		toDelete = getFacade().find(toDelete.getPk(), RefFileProperty.class);

		// LOGGER.info("toDelete3: " + toDelete.getRefFilePermissionList());

		for (RefFilePermission perm : toDelete.getRefFilePermissionList()) {
			getFacade().removeUploaded(perm, folderName);
		}
		getFacade().removeUploaded(toDelete, folderName);
	}

	public void deleteAutoUpload(RefFileProperty toDelete, String folderName) {
		LOGGER.info("toDelete: " + toDelete);
		LOGGER.info("toDelete2: " + toDelete.getRefFilePermissionList());

		toDelete = getFacade().find(toDelete.getPk(), RefFileProperty.class);

		// LOGGER.info("toDelete3: " + toDelete.getRefFilePermissionList());

		for (RefFilePermission perm : toDelete.getRefFilePermissionList()) {
			getFacade().removeUploaded(perm, folderName);
		}
		getFacade().removeUploaded(toDelete, folderName);
	}


	public RefFileProperty lookupFileInDB(String path) {
		LOGGER.info("lookupFileInDB: " + path);
		List<RefFileProperty> resultList = getFacade()
				.getEntityManager()
				.createNamedQuery("RefFileProperty.findByRelativeLocation",
						RefFileProperty.class)
				.setParameter("relativeLocation",
						retrieveRelativeFilePath(path)).getResultList();

		RefFileProperty result = null;
		if (!resultList.isEmpty()) {
			result = resultList.get(0);
		}

		return result;
	}

	public String retrieveRelativeFilePath(String filePath) {
		String result = filePath.substring(getRootDirectory().length());
//		LOGGER.info("retrieveRelativeFilePath: " + result);
		return result;
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public List<RefAgencyGroup> getTrustedGroup(RefAgencyGroup ownerGroup) {
		return new ArrayList<RefAgencyGroup>(facade
				.getEntityManager().createNamedQuery("TrustedGroups.findTargetGroupsByOwner", RefAgencyGroup.class)
				.setParameter("ownerGroup", ownerGroup)
				.getResultList());
		
	}
	public void updateRefFileProperty(RefFileProperty refProp){	
		System.out.println("TEST RUN");
		String query = "DELETE FROM \"FSCC-IS\".REF_FILE_PROPERTY WHERE FILE_PROPERTY_ID = " + refProp.getFilePropertyId();
		int result = getFacade().getEntityManager().createNativeQuery(query).executeUpdate();
		getFacade().getEntityManager().flush();
//		getFacade().remove(entity);
		System.out.println("END TEST RUN");
	}

}
