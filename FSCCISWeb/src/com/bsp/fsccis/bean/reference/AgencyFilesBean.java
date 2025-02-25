package com.bsp.fsccis.bean.reference;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
//import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.TreeUtils;

import ph.gov.bsp.utils.av.ICAPException;

import com.bsp.fsccis.bean.crud.GenericCRUDBean;
import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.bean.util.FileObject;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.SysRole;
import com.bsp.fsccis.service.AVService;
import com.bsp.fsccis.service.PermissionService;
import com.bsp.fsccis.service.ReportService;
import com.bsp.fsccis.util.FileSizeUtil;

@Named("agencyFilesBean")
@ViewScoped
public class AgencyFilesBean extends GenericCRUDBean<RefFileProperty> implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(AgencyFilesBean.class
			.getSimpleName());

	@Inject
	private AuthBean auth;

	@EJB
	private ReportService service;
	
	@EJB
	private AVService avService;

	private TreeNode root;
	private TreeNode selectedNode;
	
	private RefFileProperty forDelete;
	
	private String agencyStorageUtilization;
	
	private List<RefFileProperty> selectedFiles;
	
	private RefAgencyGroup focusAgencyGroup;
	private List<RefAgencyGroup> agencyGroupList;
	
	private boolean includeDisabled = false;

	@PostConstruct
	public void init() {
		LOGGER.info("init");

		if(auth.getAgencyGroup() == null){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Initialization Error",
					"Your user account has not yet been assigned in an agency. Contact your User Account Manager."));
			return;
		} 
		
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		
		updateAgencyGrouplist();
		focusAgencyGroup = auth.getAgencyGroup();
		
		if(request.isUserInRole(SysRole.AGENCY_FILE_ADMIN) || request.isUserInRole(SysRole.AGENCY_FILE_UPLOADER)){
			configAgencyFiles();
		}

		service.getFacade().logAuditTrail("Agency Files",AuditTrail.ACTION_VIEW,"Agency Files",auth.getAgencyGroup(),auth.getCuser());
	}
	
	public void updateAgencyGrouplist(){
		if(includeDisabled){
			agencyGroupList = new ArrayList<RefAgencyGroup>(service.getFacade().findAll(RefAgencyGroup.class));
		}else{
			agencyGroupList = new ArrayList<RefAgencyGroup>(service.getFacade().getEntityManager().createNamedQuery("RefAgencyGroup.findAllNoDisabled", RefAgencyGroup.class).getResultList());
		}
	}
	
	private boolean folderVisible;

	@EJB
	PermissionService permService;
//	CRUDSingleton crud;
	
	@Override
	public void add() throws Exception {
		permService.addNewFileWithPermission(getEntity(),auth.getAgencyGroup(),auth.getCuser());
	}
	
	private List<RefFileProperty> allFolders;
	private String agencyRootDirectory;

	public void configAgencyFiles() {
		LOGGER.info("configAgencyFiles()");
		if(focusAgencyGroup != null){
			
			if(!focusAgencyGroup.equals(auth.getAgencyGroup()) && auth.getAgencyGroup().getAccessLevel() != RefAgencyGroup.SYSTEM_OWNER){
				PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("View Other Agency Folders",
						"System owners alone can view other agency folders."));
				return;
			}
	
			root = new DefaultTreeNode("root", null);
	
			TreeNode report = new DefaultTreeNode("report", "Reports", root);
			
			allFolders = service
					.getFacade()
					.getEntityManager()
					.createNamedQuery("RefFileProperty.findAllAgencyGroupFolders",
							RefFileProperty.class).setParameter("ownerGroupId", focusAgencyGroup).getResultList();
	
			if (service.getRootDirectory() != null
					&& !service.getRootDirectory().isEmpty()) {
				agencyRootDirectory = service.getRootDirectory() + File.separatorChar + RefFileProperty.ROOT_AGENCY_GROUP_FOLDER_NAME_PREFIX + focusAgencyGroup.getAgencyGroupId();
				File rootdir = new File(agencyRootDirectory);
				LOGGER.info("rootDir: " + rootdir.getAbsolutePath());
				
				listFilesForFolder(
						new FileObject(agencyRootDirectory, null), report);
				sortFolderTree();
			}
			updateStorageUtilization();	
		}
		setEntityList(new ArrayList<RefFileProperty>(1));
	}
	
	private void sortFolderTree(){
		System.out.println("sortFolderTree()");
		TreeUtils.sortNode(root, new Comparator<TreeNode>() {

			@Override
			public int compare(TreeNode object1, TreeNode object2) {
				if (object1.getData() instanceof FileObject && object2.getData() instanceof FileObject) {
					FileObject f1 = (FileObject) object1.getData();
					FileObject f2 = (FileObject) object2.getData();
					System.out.println(f1.getDisplayName());
					System.out.println(f2.getDisplayName());
					return f1.getDisplayName().toLowerCase().compareTo(f2.getDisplayName().toLowerCase());
				}
				return 0;
			}
		});
	}
	
	private long usage;
	
	private void updateStorageUtilization(){
		usage = FileUtils.sizeOfDirectory(new File(agencyRootDirectory));
		
		agencyStorageUtilization = FileSizeUtil.formatSizeToString(usage) + " used.";
	}

	private void listFilesForFolder(final FileObject folder, TreeNode parent) {
		LOGGER.info("listFileForFolder(): " + folder.getAbsolutePath());
		try {
			for (File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {

					String relativePath = service
							.retrieveRelativeFilePath(fileEntry.getPath());

					if(auth.getTestMode()){
						relativePath = relativePath.replace("\\", "/");
						LOGGER.info("find: relative path: " + relativePath);
					}
					
					RefFileProperty focusRfp = null;

					for (RefFileProperty rfp : allFolders) {
						if (rfp.getRelativeLocation().equals(relativePath)) {
							focusRfp = rfp;
						}
					}

					if (focusRfp != null) {
						FileObject fo = new FileObject(fileEntry,
								focusRfp.getFileName());
						TreeNode child = new DefaultTreeNode("folder", fo, parent);
						listFilesForFolder(fo, child);
					} else {
						LOGGER.severe("File not found in DB: "
								+ fileEntry.getPath());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public void onNodeSelect(NodeSelectEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Selected", event.getTreeNode().toString());
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public static final String FOLDER = "folder";
	public static final String REPORT = "report";

	public void addFolder() {
		LOGGER.info(auth.getAgencyGroup().getAgencyGroupShortname());
		LOGGER.info(focusAgencyGroup.getAgencyGroupShortname());
		
		if(auth.getAgencyGroup() != null && focusAgencyGroup != null && !auth.getAgencyGroup().equals(focusAgencyGroup)){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Add Folder",
					"You can't add folders to agency groups you don't belong."));
			return;
		} 
		
		String folderName = null;

		FileObject parentFileObject = null;
		if (selectedNode.getType() == REPORT) {
			parentFileObject = new FileObject(agencyRootDirectory, null);
		} else {
			parentFileObject = new FileObject(
					((FileObject) selectedNode.getData()).getPath(),
					((FileObject) selectedNode.getData()).getDisplayName());
		}

		if (selectedNode != null) {
			LOGGER.info("Focus Folder: " + parentFileObject.getPath());
			folderName = getAvailableFolderName(parentFileObject);
			LOGGER.info("1");
		}
		LOGGER.info("2");
		FileObject newFolderFile = new FileObject(parentFileObject.getPath()
				+ FileObject.separatorChar + folderName, folderName);
		LOGGER.info("newFolderFile: " + newFolderFile.getAbsolutePath());
		if (!newFolderFile.exists()) {
			LOGGER.info("3");
			if (newFolderFile.mkdir()) {
				LOGGER.info("4");
				RefFileProperty rfp = new RefFileProperty(null,
						RefFileProperty.TYPE_FOLDER, folderName,
						service.retrieveRelativeFilePath(newFolderFile
								.getPath()));
				if(auth.getTestMode()){
					rfp.setRelativeLocation(rfp.getRelativeLocation().replace("\\", "/"));
				}
				rfp.setOwnerGroupId(auth.getAgencyGroup());
				rfp.setCuser(auth.getCuser());

				LOGGER.info("FOLDER NAME: " + rfp.getFileName());
				LOGGER.info("REL LOCATION: " + rfp.getRelativeLocation());

				try {
					service.getFacade().create(rfp);
					new DefaultTreeNode("folder", newFolderFile, selectedNode);
				} catch (Throwable e) {
					if (e.getMessage()
							.contains(
									"org.apache.openjpa.persistence.EntityExistsException")) {
						FacesContext
								.getCurrentInstance()
								.addMessage(
										null,
										new FacesMessage("Error Add",
												"Folder name should be unique per directory."));
					} else {
						FacesContext
								.getCurrentInstance()
								.addMessage(
										null,
										new FacesMessage("Error Add",
												"Problem Encountered in Adding New Entity"));
					}
					newFolderFile.delete();
				}
			}
		}else{
			LOGGER.info("what??");
		}
		
	}

	public void editFolder(FileObject focusFileObject) {
		if (focusFileObject != null) {

			if(auth.getAgencyGroup() != null && focusAgencyGroup != null && !auth.getAgencyGroup().equals(focusAgencyGroup)){
				PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Edit Folder",
						"You can't edit a folder to agency groups you don't belong."));
				return;
			} 
			
			String prevName = focusFileObject.getName();
			String newName = focusFileObject.getDisplayName();
			LOGGER.info("PREV NAME: " + prevName);
			LOGGER.info("NEW: " + newName);

			try {
				RefFileProperty rfpToEdit = null;

				if(auth.getTestMode()){
					rfpToEdit = service.lookupFileInDB(
							focusFileObject.getPath().replace("\\", "/"));
				}else{
					rfpToEdit = service.lookupFileInDB(
							focusFileObject.getPath());
				}
				
				if (rfpToEdit != null) {
					rfpToEdit.setFileName(focusFileObject.getDisplayName());
					rfpToEdit.setCuser(auth.getCuser());
					LOGGER.info("TO EDIT: " + rfpToEdit.getDisplayName());
					service.getFacade().edit(rfpToEdit);
				} else {
					focusFileObject.setDisplayName(focusFileObject.getName());
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Edit", "Failed to edit folder name"));
				}

			} catch (Throwable e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());

				if (e.getMessage().contains(
						"javax.persistence.NoResultException")) {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Edit", "Folder referenced not found."));
				} else {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Edit", "Failed to edit folder name"));
				}
			}
			sortFolderTree();
			
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Edit", "Select one folder to edit"));
		}
	}

	private String getAvailableFolderName(FileObject parentFolder) {

		String folderName = "Folder";
		String[] filename = parentFolder.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		});

		List<String> filenameList = Arrays.asList(filename);

		for (int i = 1; i < 50; i++) {
			if (!filenameList.contains(folderName + " " + i)) {
				return folderName + " " + i;
			}
		}

		return folderName + " 1";
	}
	
	public void download(RefFileProperty rfp){
		LOGGER.info("download: " + rfp.getDisplayName());
		File toDownload = new File(service.getRootDirectory() + File.separatorChar + rfp.getRelativeLocation());

		//TODO: scan file
		boolean noProblemFound = false;
		
		if(!auth.getTestMode() && avService != null){
			try {
				if(toDownload.exists()){
					try {
						avService.scan(toDownload);
						noProblemFound = true;
					} catch (ICAPException e) {
						noProblemFound = false;
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE,"ERROR: " + e);
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "File Download",
						"Problem encountered while retrieving file.");
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}
		}else if(auth.getTestMode()){
			noProblemFound = true;
		}
		
		if(noProblemFound){
			try {
				Faces.sendFile(toDownload, true);
				String displayFolder = "";
				try{
					LOGGER.info("Eto: " + toDownload);
					if(toDownload.exists()){
						File folderFile = toDownload.getParentFile();
						
						RefFileProperty folder = null;
						if(auth.getTestMode()){
							
							List<RefFileProperty> resultList = service.getFacade()
									.getEntityManager()
									.createNamedQuery("RefFileProperty.findByRelativeLocation",
											RefFileProperty.class)
									.setParameter("relativeLocation",
											service.retrieveRelativeFilePath(folderFile.getAbsoluteFile().toString().replace("\\", "/"))).getResultList();

							RefFileProperty result = null;
							if (!resultList.isEmpty()) {
								folder = resultList.get(0);
							}
							
						}else{
							folder = service.lookupFileInDB(service.retrieveRelativeFilePath(folderFile.getAbsoluteFile().toString()));
						}
						
						if(folder != null && folder.getFileType() == RefFileProperty.TYPE_FOLDER){
							displayFolder = "Source: " + folder.getOwnerGroupId().getAgencyGroupShortname() + ", Folder: " + folder.getFileName();
						}
					}
				}catch(Exception e){}
				String logString = "";
				if(displayFolder.isEmpty()){
					logString = "File: " + rfp.getFileName();
				}else{
					logString =  displayFolder + ", File: " + rfp.getFileName();
				}

				service.getFacade().logAuditTrail("Agency Files", AuditTrail.ACTION_DOWNLOAD_FILE, logString, auth.getAgencyGroup(), auth.getCuser());
			} catch (IOException e) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Download File",
						"Failed to perform action.");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}else{
			LOGGER.severe("PROBLEM DETECTED");
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Download File",
					"Failed to perform action. File is corrupted.");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
		
		
	}
	
	public void removeSelectedFiles(){
		if(selectedFiles != null && !selectedFiles.isEmpty()){
			for(RefFileProperty forDelete : selectedFiles){
				System.out.println("For Delete: " + selectedFiles);
				this.forDelete = forDelete;
				delete();
			}
			selectedFiles.clear();
		}
	}
	
	public void delete(){
		LOGGER.info("delete");
		if(forDelete != null){
			File fileToDelete = new File(service.getRootDirectory() + File.separatorChar + forDelete.getRelativeLocation());
			if(fileToDelete.isFile()){
				LOGGER.info("2");
				if(fileToDelete.delete()){ 
					try{
						service.delete(forDelete);
						getEntityList().remove(forDelete);
					}catch(Exception e){
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
								"Delete File",
								"Failed to delete DB data.");
						FacesContext.getCurrentInstance().addMessage(null, message);
						LOGGER.severe("Failed to delete DB data: " + forDelete.getDisplayName());
					}
				}else{
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Delete File",
							"Failed to perform action.");
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}else{
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Delete File",
						"Select one file to delete.");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}else{
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Delete File",
					"Select one file to delete.");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
		updateStorageUtilization();
	}
	
	public void deleteFolder(){

		if (selectedNode != null && selectedNode.getType().equals(FOLDER)) {
			FileObject folderToDelete = ((FileObject) selectedNode.getData());
			RefFileProperty rfp = null;
			
			if(folderToDelete.list() != null && folderToDelete.list().length > 0){
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Delete Folder",
						"Cannot delete folder. Delete contents first.");
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}
			try{
				if(auth.getTestMode()){
					rfp = service.lookupFileInDB(folderToDelete.getPath().replace("\\", "/"));
				}else{
					rfp = service.lookupFileInDB(folderToDelete.getPath());
				}
				if(rfp != null && folderToDelete.delete()){
					try{
						service.getFacade().remove(rfp);
						TreeNode parent = selectedNode.getParent();
						
						parent.getChildren().remove(selectedNode);
						
//						initMenuPanel();
					}catch(Exception e){
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
								"Delete Folder",
								"Failed to delete DB data.");
						FacesContext.getCurrentInstance().addMessage(null, message);
						LOGGER.severe("Failed to delete DB data: " + rfp.getDisplayName());
					}
				}else{
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Delete Folder",
							"Failed to perform action.");
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}catch(Exception e){
				generateFileLookupError(e, "Delete Folder", "No DB data for file found");
			}
			selectedNode = null;
		} else {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Delete Folder",
					"Invalid selection. Please select a folder to delete.");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}

//		traverseTree(root);
	}
	
	private void generateFileLookupError(Exception e,String module,String message){
			e.printStackTrace();
			LOGGER.severe(e.getMessage());

			if (e.getMessage() != null && e.getMessage().contains(
					"javax.persistence.NoResultException")) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						module,
						message);
				FacesContext.getCurrentInstance().addMessage(null, m);
				LOGGER.severe(message);
			} else {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						module,
						"Problem encountered in module " + module);
				FacesContext.getCurrentInstance().addMessage(null, m);
			}
	}

	public void viewFolderContent() {
		if (selectedNode != null && selectedNode.getType().equals(FOLDER)) {
			FileObject parentFolder = ((FileObject) selectedNode.getData());

			try{
				RefFileProperty rfpFolder = null;
				
				if(auth.getTestMode()){
					rfpFolder = service.lookupFileInDB(parentFolder.getPath().replace("\\", "/"));
				}else{
					rfpFolder = service.lookupFileInDB(parentFolder.getPath());
				}
						
				
				if(rfpFolder != null){
					File folder = new File(service.getRootDirectory() + File.separatorChar + rfpFolder.getRelativeLocation());
					RefFileProperty rfp = null;
					
					getEntityList().clear();
					
					FilenameFilter PPT_PDF_XLS_ONLY = new FilenameFilter() {
						
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
					
					for(File file : folder.listFiles(PPT_PDF_XLS_ONLY)){
						LOGGER.info("FILE: " + file.getName());
						try{
							if(auth.getTestMode()){
								rfp = service.lookupFileInDB(file.getPath().replace("\\", "/"));
							}else{
								rfp = service.lookupFileInDB(file.getPath());
							}
							
							String size = getFileSizeString(file);
							
							if(rfp != null){
								rfp.setSize(size);
							}
						}catch(Exception e){
							generateFileLookupError(e, "View Folder", "No DB data for file found.");
						}
						
						if(rfp != null){
							getEntityList().add(rfp);
						}
					}
					if(getEntityList() != null && !getEntityList().isEmpty()){
						Collections.sort(getEntityList(), new Comparator<RefFileProperty>() {

							@Override
							public int compare(RefFileProperty object1,
									RefFileProperty object2) {
								return object1.getFileName().compareTo(object2.getFileName());
							}
						});
					}
					service.getFacade().logAuditTrail("View Folder",AuditTrail.ACTION_VIEW,rfpFolder.getFileName(),auth.getAgencyGroup(),auth.getCuser());
				}else{
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"View Folder",
							"Folder not found.");
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}catch(Throwable e){
				e.printStackTrace();
				LOGGER.severe(e.getMessage());

				if (e.getMessage() != null && e.getMessage().contains(
						"javax.persistence.NoResultException")) {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"View Folder",
							"Folder not found.");
					FacesContext.getCurrentInstance().addMessage(null, message);
				} else {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"View Folder",
							"Problem encountered viewing folder.");
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
				
			}
		} else {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"View Folder",
					"Invalid selection. Please select a folder to view.");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	private String getFileSizeString(File file) {
		return FileSizeUtil.formatSizeToString(file.length());
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");

	public void handleFileUpload(FileUploadEvent event) {

		LOGGER.info("handleFileUpload");

		if (selectedNode == null || selectedNode.getType().equals(REPORT)) {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "File Upload",
					"Select which folder to upload the file.");
			FacesContext.getCurrentInstance().addMessage(null, message);
			PrimeFaces.current().executeScript("PF('statusDialog').hide()");
			return;
		}
		
		if(event.getFile() != null && event.getFile().getFileName().length() > 35){
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "File Upload",
					"Filename too long: " + event.getFile().getFileName() + ". Max: 35 characters.");
			FacesContext.getCurrentInstance().addMessage(null, message);
			PrimeFaces.current().executeScript("PF('statusDialog').hide()");
			return;
		}
		String newFilename = FilenameUtils.getBaseName(event.getFile().getFileName()) + "_" +
				sdf.format(new Date()) + "." + FilenameUtils.getExtension(event.getFile().getFileName());
		
		boolean noProblemFound = false;
		
		if(avService != null){
			LOGGER.info("AV Service Available");
			
			File newFile = new File(
					((FileObject) selectedNode.getData()).getPath()
							+ File.separatorChar
							+ newFilename);
			try {
				FileUtils.writeByteArrayToFile(newFile, event.getFile()
						.getContent());
				if(!auth.getTestMode()){
					if(newFile.exists()){
						try {
							LOGGER.info("File Uploaded to server... Scanning");
							avService.scan(newFile);
							LOGGER.info("No Problem Found");
							noProblemFound = true;
						} catch (ICAPException e) {
							noProblemFound = false;
							LOGGER.severe("Problem found in files... Deleting");
							if (!newFile.delete()) {
								LOGGER.info("Failed to delete created file in the fileserver...");
							}
							LOGGER.log(Level.SEVERE,"ERROR1: " + e);
							FacesMessage message = new FacesMessage(
									FacesMessage.SEVERITY_FATAL, "File Upload Failed", "Please check file " + event.getFile()
											.getFileName() + " as it may be locked or corrupted.");
							FacesContext.getCurrentInstance().addMessage(null, message);
							PrimeFaces.current().executeScript("PF('statusDialog').hide()");
							return;	
						}
					}
				}else{
					noProblemFound = true;
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,"ERROR2: " + e);
				LOGGER.log(Level.SEVERE,"ERROR2.1: " + e.getMessage(),e);
				
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "File Upload",
						"Problem encountered writing file.");
				FacesContext.getCurrentInstance().addMessage(null, message);

				if (!newFile.delete()) {
					LOGGER.info("Failed to delete created file in the fileserver...");
				}
				PrimeFaces.current().executeScript("PF('statusDialog').hide()");
				return;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,"ERROR3: " + e);
				LOGGER.log(Level.SEVERE,"ERROR3.1: " + e.getMessage());
				
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "File Upload",
						"Problem encountered uploading file.");
				FacesContext.getCurrentInstance().addMessage(null, message);

				if (!newFile.delete()) {
					LOGGER.info("Failed to delete created file in the fileserver...");
				}
				PrimeFaces.current().executeScript("PF('statusDialog').hide()");
				return;
			}
		}else{
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "File Upload",
					"Problem encountered uploading file.");
			FacesContext.getCurrentInstance().addMessage(null, message);
			PrimeFaces.current().executeScript("PF('statusDialog').hide()");
			return;
		}

		if (noProblemFound) {
			initNewEntity();

			short type = -1;

			if (newFilename.toLowerCase().endsWith("xls")
					|| newFilename.toLowerCase()
							.endsWith("xlsx")) {
				type = RefFileProperty.TYPE_XLS;
			} else if (newFilename.toLowerCase()
					.endsWith("ppt")
					|| newFilename.toLowerCase()
							.endsWith("pptx")) {
				type = RefFileProperty.TYPE_PPT;
			} else if (newFilename.toLowerCase()
					.endsWith("pdf")) {
				type = RefFileProperty.TYPE_PDF;
			} else if (newFilename.toLowerCase()
					.endsWith("doc")
					|| newFilename.toLowerCase()
							.endsWith("docx")) {
				type = RefFileProperty.TYPE_DOC;
			} else {
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "File Upload",
						"Unsupported file type. Can support only .PDF/.XLS/XLSX/.PPT/.PPTX");
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}

			try {
				File newFile = new File(
						((FileObject) selectedNode.getData()).getPath()
								+ File.separatorChar
								+ newFilename);
				FileUtils.writeByteArrayToFile(newFile, event.getFile()
						.getContent());

				if (newFile.exists()) {
					try {
						RefFileProperty rfp = null;
						try{
							if(auth.getTestMode()){
								rfp = service.lookupFileInDB(newFile.getPath().replace("\\", "/"));
							}else{
								rfp = service.lookupFileInDB(newFile.getPath());
							}
						}catch(Throwable e){
							if (e.getMessage() != null && e.getMessage().contains(
									"javax.persistence.NoResultException")) {
								LOGGER.info("Upload new file.");
							}
						}
						
						if(rfp != null){
							//overwrite data
							try{
								setEntity(rfp);
								getEntity().setCuser(auth.getCuser());
								service.getFacade().edit(getEntity());
								
								//refresh list
								getEntityList().remove(rfp);
								
								rfp.setSize(getFileSizeString(newFile));
								
								getEntityList().add(rfp);
								LOGGER.info("Overwrite existing file...");
							}catch(Exception e){

								FacesMessage message = new FacesMessage(
										FacesMessage.SEVERITY_ERROR, "File Upload",
										"Problem encountered overwriting existing file.");
								FacesContext.getCurrentInstance().addMessage(null,
										message);
							}
						}else{
							//new file
							getEntity().setFileName(newFilename);
							getEntity().setFileType(type);
	
							String relativeLocation = service
									.retrieveRelativeFilePath(newFile.getPath());
							LOGGER.info("relLoc: " + relativeLocation);
							LOGGER.info("filename: " + getEntity().getFileName());
							
							if(auth.getTestMode()){
								getEntity().setRelativeLocation(relativeLocation.replace("\\", "/"));
							}else{
								getEntity().setRelativeLocation(relativeLocation);
							}
	
							getEntity().setOwnerGroupId(auth.getAgencyGroup());
							getEntity().setCuser(auth.getCuser());
							getEntity().setSize(getFileSizeString(newFile));
							
							add();
							getEntityList().add(getEntity());
						}
						
					} catch (Exception e) {
						LOGGER.info("Problem saving file data to database. Deleting file...");

						if (!newFile.delete()) {
							LOGGER.info("Failed to delete created file in the fileserver...");
						}

						FacesMessage message = new FacesMessage(
								FacesMessage.SEVERITY_ERROR, "File Upload",
								"Problem encountered uploading file.");
						FacesContext.getCurrentInstance().addMessage(null,
								message);

						PrimeFaces.current().executeScript("PF('statusDialog').hide()");
						return;
					}
				} else {
					LOGGER.info("File not created");
					FacesMessage message = new FacesMessage(
							FacesMessage.SEVERITY_ERROR, "File Upload",
							"Problem encountered uploading file.");
					FacesContext.getCurrentInstance().addMessage(null, message);
					PrimeFaces.current().executeScript("PF('statusDialog').hide()");
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "File Upload",
						"Problem encountered uploading file.");
				FacesContext.getCurrentInstance().addMessage(null, message);
				PrimeFaces.current().executeScript("PF('statusDialog').hide()");
				return;
			}

			LOGGER.info("DIR: "
					+ ((FileObject) selectedNode.getData()).getPath());
		} else {
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_FATAL, "File Upload Failed", "Please check file " + event.getFile()
							.getFileName() + " as it may be locked or corrupted.");
			FacesContext.getCurrentInstance().addMessage(null, message);

			PrimeFaces.current().executeScript("PF('statusDialog').hide()");
			return;
		}
		updateStorageUtilization();
		PrimeFaces.current().executeScript("PF('statusDialog').hide()");
	}
	
	public void nodeExpand(NodeExpandEvent event) {
	    event.getTreeNode().setExpanded(true);
//	    traverseTree(root);
	}

	public void nodeCollapse(NodeCollapseEvent event) {
	    event.getTreeNode().setExpanded(false);     
//	    traverseTree(root);
	}
	
//	private void traverseTree(TreeNode node){
//		if(!node.isLeaf()){
//			if (node.getData() instanceof FileObject) {
//				FileObject fileObject = (FileObject) node.getData();
//				LOGGER.info("N: " + fileObject.getDisplayName() + " STAT: " + node.isExpanded());
//			}else{
//				LOGGER.info("N: " + node.getData() + " STAT: " + node.isExpanded());
//			}
//			for(TreeNode child : node.getChildren()){
//				traverseTree(child);
//			}
//		}else{
//			if (node.getData() instanceof FileObject) {
//				FileObject fileObject = (FileObject) node.getData();
//				LOGGER.info("N: " + fileObject.getDisplayName() + " STAT: " + node.isExpanded());
//			}else{
//				LOGGER.info("N: " + node.getData() + " STAT: " + node.isExpanded());
//			}
//			
//		}
//	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected Class<RefFileProperty> getEntityClass() {
		return RefFileProperty.class;
	}

//	public MenuModel getMenuModel() {
//		return menuModel;
//	}
//
//	public void setMenuModel(MenuModel menuModel) {
//		this.menuModel = menuModel;
//	}

	public boolean isFolderVisible() {
		return folderVisible;
	}

	public void setFolderVisible(boolean folderVisible) {
		this.folderVisible = folderVisible;
	}

	public RefFileProperty getForDelete() {
		return forDelete;
	}

	public void setForDelete(RefFileProperty forDelete) {
		this.forDelete = forDelete;
	}

	public String getAgencyStorageUtilization() {
		return agencyStorageUtilization;
	}

	public List<RefFileProperty> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<RefFileProperty> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public RefAgencyGroup getFocusAgencyGroup() {
		return focusAgencyGroup;
	}

	public void setFocusAgencyGroup(RefAgencyGroup focusAgencyGroup) {
		this.focusAgencyGroup = focusAgencyGroup;
	}

	public List<RefAgencyGroup> getAgencyGroupList() {
		return agencyGroupList;
	}

	public void setAgencyGroupList(List<RefAgencyGroup> agencyGroupList) {
		this.agencyGroupList = agencyGroupList;
	}

	public boolean isIncludeDisabled() {
		return includeDisabled;
	}

	public void setIncludeDisabled(boolean includeDisabled) {
		this.includeDisabled = includeDisabled;
	}

}
