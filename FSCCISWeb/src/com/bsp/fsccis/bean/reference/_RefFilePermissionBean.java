package com.bsp.fsccis.bean.reference;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
//import org.primefaces.context.RequestContext;
import org.primefaces.PrimeFaces;

import com.bsp.fsccis.bean.crud.GenericCRUDBean;
import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.util.PermHeader;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisible;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisiblePK;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.service.ReportService;

@Named("_refFilePermissionBean")
@ViewScoped
public class _RefFilePermissionBean extends GenericCRUDBean<RefFilePermission>
		implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(RefFilePermission.class.getSimpleName());

	@Inject
	private AuthBean auth;
	
	@EJB
	private ReportService service;
	
	private RefAgencyGroup focusAgencyGroup;
	
	private List<RefAgencyGroup> agencyGroupList;
	private List<RefAgencyGroupFolderVisible> agencyVisibilityList;
	private List<RefFileProperty> allFolders;
	private List<RefFileProperty> allFiles;
	
	private String agencyGroupRootDirectory;
	
	@PostConstruct
	public void init(){
		LOGGER.info("init");
		
		if(auth.getAgencyGroup() == null){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Initialization Error",
					"Your user account has not yet been assigned in an agency group. Contact your User Account Manager."));
			return;
		}
		
		setAgencyGroupList(new ArrayList<RefAgencyGroup>(service.getFacade().getEntityManager().createNamedQuery("RefAgencyGroup.findAllNoDisabled", RefAgencyGroup.class).getResultList()));
		
		focusAgencyGroup = auth.getAgencyGroup();
		updateFileFolderView();

		//log view		
		service.getFacade().logAuditTrail("File Permission",AuditTrail.ACTION_VIEW,"File Permission",auth.getAgencyGroup(),auth.getCuser());
	}
	
	public void updateFileFolderView(){
		
		if(!focusAgencyGroup.equals(auth.getAgencyGroup()) && auth.getAgencyGroup().getAccessLevel() != RefAgencyGroup.SYSTEM_OWNER){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("View Permission",
					"System owners alone can manage permissions."));
			return;
		}
		
		agencyVisibilityList = service.getAgencyFolderVisibleByOwner(focusAgencyGroup);
		
		displayDataMap = new HashMap<String, Boolean>(1);
		dataMap = new HashMap<String, RefFilePermission>(1);
		
		allFolders = service
				.getFacade()
				.getEntityManager()
				.createNamedQuery("RefFileProperty.findAllAgencyGroupFolders",
						RefFileProperty.class).setParameter("ownerGroupId", focusAgencyGroup).getResultList();
		
		allFiles = service.loadAllAgencyFiles(focusAgencyGroup);

		initDocumentFilter();

		filePropertyIdList = new ArrayList<Integer>(1);
		if (service.getRootDirectory() != null
				&& !service.getRootDirectory().isEmpty()) {
			setHeader1(new ArrayList<PermHeader>());
			setHeader2(new ArrayList<PermHeader>());
			temp_header1 = new ArrayList<PermHeader>();
			temp_header2 = new ArrayList<PermHeader>();
			
			PermHeader agencyGroup = new PermHeader("Agency&nbsp;Group",2,1);
			PermHeader folder = new PermHeader("Folder Visible",2,1);
			
			fheader = new ArrayList<PermHeader>();
			fheader.add(agencyGroup);
			fheader.add(folder);
			folderAndCorrespondingFilesMap = new HashMap<Integer, List<Integer>>();
			
			agencyGroupRootDirectory = service.getRootDirectory() + File.separatorChar + RefFileProperty.ROOT_AGENCY_GROUP_FOLDER_NAME_PREFIX + focusAgencyGroup.getAgencyGroupId();
			File rootdir = new File(agencyGroupRootDirectory);
			LOGGER.info("rootDir: " + rootdir.getName());
			
			listFilesForFolder(
					new File(agencyGroupRootDirectory));
			
		}
		
		initAgencyGroupFolderVisibility();
		
		initFilePermissionDisplayAndMap();
		
		String out = "";
		for(PermHeader p : header1){
			out += p.toString() + ": " ;
		}
		LOGGER.info(out);
		out = "";
		for(PermHeader p : header2){
			out += p.toString() + ": " ;
		}
		LOGGER.info(out);
		
		if(temp_header1 != null && !temp_header1.isEmpty()){
			for(PermHeader ph : temp_header1){
				header1.add(ph);
			}
		}
		if(temp_header2 != null && !temp_header2.isEmpty()){
			for(PermHeader ph : temp_header2){
				header2.add(ph);
			}
		}
		
		updateFileAndFolderLevelSelectAll();
	}


	private void initFilePermissionDisplayAndMap() {
		for(RefFileProperty fileRfp : allFiles){
			for(RefFilePermission rfpp : fileRfp.getRefFilePermissionList()){
				String map = rfpp.getTargetGroupId().getAgencyGroupId().toString().concat(FILE_MAP_SEPARATOR).concat(rfpp.getRefFileProperty().getFilePropertyId().toString());
				dataMap.put(map, rfpp);
				displayDataMap.put(map, (rfpp.getVisible() == RefFilePermission.VISIBLE_TRUE) ? true:false);
			}
		}
	}


	private void initAgencyGroupFolderVisibility() {
		for(RefAgencyGroupFolderVisible rafv : agencyVisibilityList){
			String map = rafv.getOwnerGroupId().getAgencyGroupId().toString().concat(FOLDER_MAP_SEPARATOR).concat(rafv.getTargetGroupId().getAgencyGroupId().toString());
			displayDataMap.put(map,(rafv.getFolderVisible() == RefAgencyGroupFolderVisible.VISIBLE) ? true:false);
		}
	}


	private void initDocumentFilter() {
		PPT_PDF_XLS_DOC_ONLY = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				
				if(name.toUpperCase().endsWith(".XLS") || name.toUpperCase().endsWith(".XLSX")
						|| name.toUpperCase().endsWith(".PPT") || name.toUpperCase().endsWith(".PPTX")
						|| name.toUpperCase().endsWith(".DOC") || name.toUpperCase().endsWith(".DOCX")
						|| name.toUpperCase().endsWith(".PDF")){
					return true;
				}
				
				return false;
			}
		};
	}
	
	/**
	 * 
	 * @author IndelibleD
	 */
	private void updateFileAndFolderLevelSelectAll() {
		LOGGER.info("updateFileAndFolderLevelSelectAll()");
		for (Integer folderId : folderAndCorrespondingFilesMap.keySet()) {
			String key;
			boolean isChecked = true;
			boolean checkedMainFolder = true;
			for (Integer fileId : folderAndCorrespondingFilesMap.get(folderId)) {
				for (RefAgencyGroup agency : agencyGroupList) {
					key = agency.getAgencyGroupId().toString().concat(FILE_MAP_SEPARATOR).concat(fileId.toString());
					isChecked = displayDataMap.get(key) == null	|| displayDataMap.get(key) == false ? false : true;
					if (!isChecked) {
						checkedMainFolder = false;
						displayDataMap.put("FILE_" + fileId, false);
						displayDataMap.put("ALL_FILE_" + fileId, false);
						break;
					} else {
						displayDataMap.put("FILE_" + fileId, true);
						displayDataMap.put("ALL_FILE_" + fileId, true);
					}
				}
				displayDataMap.put("ALL_FOLDER_" + folderId, checkedMainFolder);
				displayDataMap.put("FOLDER_" + folderId, checkedMainFolder);
			}
		}
	}
	
	public String preview(String sampleView){
		return "/views/Administrator/SampleView.xhtml?faces-redirect=true&sampleView=" + sampleView + "&owner=" + focusAgencyGroup.getAgencyGroupShortname();
	}
	
	public void toggleAllFilesForAllAgencies(Long folderId){
		toggleAllFilesForAllAgencies(folderId.intValue());
	}
		
	/**
	 * @author IndelibleD
	 * @param folderId
	 */
	public void toggleAllFilesForAllAgencies(int folderId){
		LOGGER.info("toggleAllFilesForAllAgencies()" + folderId);	
		
		boolean folderStat = displayDataMap.get("FOLDER_" + folderId);
		
		if(displayDataMap.get("ALL_FOLDER_" + folderId) != null && displayDataMap.get("ALL_FOLDER_" + folderId)){
			folderStat = false;
		}
		
		LOGGER.info("folderStat: " + folderStat);
		
		for(Integer fileId : folderAndCorrespondingFilesMap.get(folderId)){
			for(RefAgencyGroup agency : agencyGroupList){
				String key = agency.getAgencyGroupId().toString().concat(FILE_MAP_SEPARATOR).concat(fileId.toString());
				if(displayDataMap.get(key) != folderStat){
					toggleFile(key, false);
				}
			}
		}
		updateFileAndFolderLevelSelectAll();	
		
		LOGGER.info(" " + displayDataMap.get("FOLDER_" + folderId));
	}
	
	public void toggleForAllAgencies(Integer fileId){
		LOGGER.info("toggleForAllAgencies(): " + fileId);
		boolean fileStat = displayDataMap.get("FILE_" + fileId);
		if(displayDataMap.get("ALL_FILE_" + fileId) != null && displayDataMap.get("ALL_FILE_" + fileId)){
			fileStat = false;
		}
		LOGGER.info("fileStat: " + fileStat);
		for (RefAgencyGroup agency : agencyGroupList) {
			String key = agency.getAgencyGroupId().toString().concat(FILE_MAP_SEPARATOR)
					.concat(fileId.toString());
//			checkAllAgencyPerFolder(key);
			if(displayDataMap.get(key) != fileStat){
				toggleFile(key, false);
			}
		}

		updateFileAndFolderLevelSelectAll();

	}
	
	
	private FilenameFilter PPT_PDF_XLS_DOC_ONLY;
	
	private List<PermHeader> fheader;
	private List<PermHeader> header1;
	private List<PermHeader> header2;
	private List<PermHeader> temp_header1;
	private List<PermHeader> temp_header2;
	private Map<String, Boolean> displayDataMap; 
	private Map<String, RefFilePermission> dataMap;
	private List<Integer> filePropertyIdList;
	private Map<Integer,List<Integer>> folderAndCorrespondingFilesMap;

	private void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {

				String relativePath = service
						.retrieveRelativeFilePath(fileEntry.getPath());
				if(auth.getTestMode()){
					relativePath = relativePath.replace("\\", "/");
					LOGGER.info("find: relative path: " + relativePath);
				}
				RefFileProperty focusFolder = null;

				for (RefFileProperty rfp : allFolders) {
					if (rfp.getRelativeLocation().equals(relativePath)) {
						focusFolder = rfp;
					}
				}

				if (focusFolder != null) {
					listFilesForFolder(fileEntry);
					
					File[] fileList = fileEntry.listFiles(PPT_PDF_XLS_DOC_ONLY);
					PermHeader folderHeader = new PermHeader(focusFolder.getFileName());
					folderHeader.setPermId(focusFolder.getFilePropertyId());
					if(fileList.length > 1){
						folderHeader.setColSpan(fileList.length);
					}

					if(fileList.length == 0){
						temp_header1.add(folderHeader);
						PermHeader emptyHeader = new PermHeader("No Files Found");
						temp_header2.add(emptyHeader);
					}else{
						header1.add(folderHeader);
						
						if(folderAndCorrespondingFilesMap.get(focusFolder.getFilePropertyId()) == null){
							folderAndCorrespondingFilesMap.put(focusFolder.getFilePropertyId(), new ArrayList<Integer>());
						}
						
						for(File file : fileList){
							RefFileProperty focusFileRfp = null;
							
							relativePath = service
									.retrieveRelativeFilePath(file.getPath());
							
							if(auth.getTestMode()){
								relativePath = relativePath.replace("\\", "/");
								LOGGER.info("find: relative path: " + relativePath);
							}
							
							for (RefFileProperty rfp : allFiles) {
								if (rfp.getRelativeLocation().equals(relativePath)) {
									focusFileRfp = rfp;
								}
							}
							
							if(focusFileRfp != null){
								PermHeader fileHeader = new PermHeader(focusFileRfp.getFileName());
								header2.add(fileHeader);
								fileHeader.setPermId(focusFileRfp.getFilePropertyId());
								filePropertyIdList.add(focusFileRfp.getFilePropertyId());
								folderAndCorrespondingFilesMap.get(focusFolder.getFilePropertyId()).add(focusFileRfp.getFilePropertyId());
							}else{
								LOGGER.severe(":File not found in DB: "
										+ file.getPath());
							}
						}
					}
					
				} else {
					LOGGER.severe("File not found in DB: "
							+ fileEntry.getPath());
				}
			}
		}
	}
	
	public static final String FOLDER_MAP_SEPARATOR = "-";
	public static final String FILE_MAP_SEPARATOR = ":";
	
	public void toggleFolder(String key){
		LOGGER.info("toggleFolder: " + key);
		String[] ownerTargetArr = key.split(FOLDER_MAP_SEPARATOR);
		final int owner = 0;
		final int target = 1;
		int ownerId = Integer.parseInt(ownerTargetArr[owner]);
		int targetId = Integer.parseInt(ownerTargetArr[target]);
//		for(RefAgency ra : getAgencyList()){
//			if(ra.getAgencyId().equals(ownerId)){
//				ownerAgency = ra;
//				break;
//			}
//		}

		RefAgencyGroup ownerAgency = service.getFacade().find(ownerId, RefAgencyGroup.class);
		RefAgencyGroup targetAgency = service.getFacade().find(targetId, RefAgencyGroup.class);
		RefAgencyGroupFolderVisible rafv = null;
		
		if(ownerAgency == null || targetAgency == null){
			LOGGER.severe("OwnerAgency or TagertAgency is null");
			return;
		}
		rafv = service.getAgencyFolderVisibleByOwnerTarget(ownerAgency, targetAgency);
		
		try{
			if(rafv != null){
				boolean visible = true;
				if(rafv.getFolderVisible() == RefAgencyGroupFolderVisible.VISIBLE){
					visible = false;
					rafv.setFolderVisible(RefAgencyGroupFolderVisible.NOT_VISIBLE);
				}else{
					visible = true;
					rafv.setFolderVisible(RefAgencyGroupFolderVisible.VISIBLE);
				}
				rafv.setCuser(auth.getCuser());
				service.getFacade().edit(rafv);
				displayDataMap.put(key, visible);
			}else{
				//create new rafv
				rafv = new RefAgencyGroupFolderVisible(new RefAgencyGroupFolderVisiblePK(ownerAgency.getAgencyGroupId(), targetAgency.getAgencyGroupId()));
				rafv.setOwnerGroupId(ownerAgency);
				rafv.setTargetGroupId(targetAgency);
				rafv.setFolderVisible(RefAgencyGroupFolderVisible.VISIBLE);
				rafv.setCuser(auth.getCuser());
				displayDataMap.put(key, true);
				
				service.getFacade().create(rafv);
				
			}
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.severe(e.getMessage());
			FacesContext
			.getCurrentInstance()
			.addMessage(
					null,
					new FacesMessage("Toggle Folder",
							"Problem encountered in editing permission."));
		}
	}
	
	public void toggleFile(String key,boolean updateFileAndFolderLevelSelectAll){
		LOGGER.info("toggleFile: " + key);
		RefFilePermission toToggle = null;
		
		toToggle = dataMap.get(key);
		
		if(toToggle == null){
			//new permission
			String[] keyArr = key.trim().split(FILE_MAP_SEPARATOR);
			final int PROP_ID = 1;
			final int AGENCY_ID = 0;
			int refFilePropertyId = Integer.parseInt(keyArr[PROP_ID]);
			int refAgencyId = Integer.parseInt(keyArr[AGENCY_ID]);
			toToggle = new RefFilePermission(refFilePropertyId,refAgencyId);
			
			toToggle.setOwnerGroupId(focusAgencyGroup);
			toToggle.setTargetGroupId(service.getFacade().find(refAgencyId, RefAgencyGroup.class));
			toToggle.setRefFileProperty(service.getFacade().find(refFilePropertyId, RefFileProperty.class));
			
			try{
				toToggle.setCuser(auth.getCuser());
				service.getFacade().create(toToggle);
				dataMap.put(key, toToggle);
				displayDataMap.put(key, true);
				if(updateFileAndFolderLevelSelectAll){
					updateFileAndFolderLevelSelectAll();
				}
			}catch(Throwable e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
				if (e.getMessage()
						.contains(
								"org.apache.openjpa.persistence.EntityExistsException")) {
					FacesContext
							.getCurrentInstance()
							.addMessage(
									null,
									new FacesMessage("Toggle File",
											"File permission already exists."));
				} else {
					FacesContext
							.getCurrentInstance()
							.addMessage(
									null,
									new FacesMessage("Toggle File",
											"Problem encountered in adding new entity."));
				}
			}
		}else{
			//update existing
			boolean value = true;
			if(toToggle.getVisible() == RefFilePermission.VISIBLE_TRUE){
				toToggle.setVisible(RefFilePermission.VISIBLE_FALSE);
				value = false;
			}else{
				toToggle.setVisible(RefFilePermission.VISIBLE_TRUE);
				value = true;
			}
			
			try{
				toToggle.setCuser(auth.getCuser());
				service.getFacade().edit(toToggle);
				displayDataMap.put(key, value);
				if(updateFileAndFolderLevelSelectAll){
					updateFileAndFolderLevelSelectAll();
				}
			}catch(Exception e){
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
				FacesContext
				.getCurrentInstance()
				.addMessage(
						null,
						new FacesMessage("Toggle File",
								"Problem encountered in editing permission."));
			}
		}
	}
	
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected Class<RefFilePermission> getEntityClass() {
		return RefFilePermission.class;
	}


	public Map<String, Boolean> getDisplayDataMap() {
		return displayDataMap;
	}


	public void setDisplayDataMap(Map<String, Boolean> displayDataMap) {
		this.displayDataMap = displayDataMap;
	}


	public List<PermHeader> getHeader1() {
		return header1;
	}


	public void setHeader1(List<PermHeader> header1) {
		this.header1 = header1;
	}


	public List<PermHeader> getHeader2() {
		return header2;
	}


	public void setHeader2(List<PermHeader> header2) {
		this.header2 = header2;
	}


	public Map<String, RefFilePermission> getDataMap() {
		return dataMap;
	}


	public void setDataMap(Map<String, RefFilePermission> dataMap) {
		this.dataMap = dataMap;
	}

	public List<Integer> getFilePropertyIdList() {
		return filePropertyIdList;
	}

	public void setFilePropertyIdList(List<Integer> filePropertyIdList) {
		this.filePropertyIdList = filePropertyIdList;
	}

	public List<RefAgencyGroup> getAgencyGroupList() {
		return agencyGroupList;
	}

	public void setAgencyGroupList(List<RefAgencyGroup> agencyGroupList) {
		this.agencyGroupList = agencyGroupList;
	}

	public List<PermHeader> getFheader() {
		return fheader;
	}

	public void setFheader(List<PermHeader> fheader) {
		this.fheader = fheader;
	}

	public List<RefAgencyGroupFolderVisible> getAgencyVisibilityList() {
		return agencyVisibilityList;
	}


	public void setAgencyVisibilityList(List<RefAgencyGroupFolderVisible> agencyVisibilityList) {
		this.agencyVisibilityList = agencyVisibilityList;
	}
	
	public String getFolderMapSeparator(){
		return FOLDER_MAP_SEPARATOR;
	}
	public String getFileMapSeparator(){
		return FILE_MAP_SEPARATOR;
	}


	public RefAgencyGroup getFocusAgencyGroup() {
		return focusAgencyGroup;
	}


	public void setFocusAgencyGroup(RefAgencyGroup focusAgencyGroup) {
		this.focusAgencyGroup = focusAgencyGroup;
	}
}
