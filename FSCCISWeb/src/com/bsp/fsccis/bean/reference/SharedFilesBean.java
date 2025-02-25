package com.bsp.fsccis.bean.reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
//import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import ph.gov.bsp.utils.av.ICAPException;

import com.bsp.fsccis.bean.login.AuthBean;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisible;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.service.AVService;
import com.bsp.fsccis.service.ReportService;
import com.bsp.fsccis.util.FileSizeUtil;


@Named("sharedFilesBean")
@ViewScoped
public class SharedFilesBean implements Serializable {
	
	
	private static final Logger LOGGER = Logger.getLogger(SharedFilesBean.class.getSimpleName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Inject
	AuthBean auth;

	@EJB
	private ReportService service;
	
	@EJB
	private AVService avService;
	
	private Map<Integer,MenuModel> menuModelMap;
	private Map<Integer,Map<String,List<RefFileProperty>>> agencyGroupToFolderToFileMap;
	private Map<Integer,List<RefFileProperty>> displayList;
	private List<RefAgencyGroup> agencyGroupList;
	private List<RefAgencyGroupFolderVisible> agencyVisibilityList;
	
	private RefAgencyGroup focusAgencyGroupTab;

	private Boolean sampleViewMode = false;
	private Boolean check;
	
	private Map<Integer,Boolean> mapAgencyFolderVisible;
	
	private final String DEFAULT_MENU_STYLECLASS = "foldermenu";
	
	private List<RefFileProperty> selectedFiles;
	private Integer activeIndex = 0;
	
	private StreamedContent zFile;
	private StreamedContent zipFile;
	private boolean downloadCompleted;
	public static final String DOWNLOAD_COMPLETED= "DOWNLOAD_COMPLETED";
	
	private String username = "system";
	
	@PostConstruct
	public void init(){
		LOGGER.info("init()");
		
		Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		System.out.println("session: " + sessionMap);
		
		Object value = sessionMap.get(DOWNLOAD_COMPLETED);
		
		if(value!= null){
			System.out.println("notif: 1");
			//user = service.getUser(value.toString());
			sessionMap.remove(DOWNLOAD_COMPLETED);
			PrimeFaces context = PrimeFaces.current();
			context.dialog().showMessageDynamic(new FacesMessage("File Download",
					"File_Name123.zip has been successfully downloaded."));

			return;
		}
		
		if(auth.getAgencyGroup() == null){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Initialization Error",
					"Your user account has not yet been assigned in an agency group. Contact your User Account Manager."));
			return;
		}
		
		Map<String, String> parameterMap = (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String sampleView = parameterMap.get("sampleView");
		String owner = parameterMap.get("owner");
		
		RefAgencyGroup targetAgency = null;
		
		/**
		 * Two View Modes:
		 * 1. Normal
		 * 		- targetAgency will be the agency of the logged in user
		 * 		- tabs of all agencies will be displayed
		 * 2. Sample View Mode
		 * 		- targetAgency will be the agency specified in the 'sampleView' parameter
		 * 		- only the folders/files of the logged in user's agency will be displayed 
		 */

		
		
		
		if(sampleView != null && !sampleView.isEmpty()){
			agencyGroupList = service.getFacade().getEntityManager().
					createNamedQuery("RefAgencyGroup.findAllNoDisabled", RefAgencyGroup.class).
					getResultList();
			for(RefAgencyGroup selectedAgency : agencyGroupList){
				if(selectedAgency.getAgencyGroupShortname().equals(sampleView)){
					targetAgency = selectedAgency;
					sampleViewMode = true; 
					break;
				}
			}
			LOGGER.info("targetAgency1: " + targetAgency);
		}
		if(!sampleViewMode){
			targetAgency = auth.getAgencyGroup();
			LOGGER.info("targetAgency2: " + targetAgency);
			List<Integer> ownerGroupIdList = service.getFacade().getEntityManager().
					createNamedQuery("RefFilePermission.findAllOwnerGroupIdAndIsVisible", Integer.class).
					setParameter("targetGroupId", targetAgency).getResultList();
			LOGGER.info("TEST: " + ownerGroupIdList.toString());
			if(!ownerGroupIdList.isEmpty()){
				agencyGroupList = service.getFacade().getEntityManager().
						createNamedQuery("RefAgencyGroup.findAllFromListNoDisabled", RefAgencyGroup.class).setParameter("agencyGroupId", ownerGroupIdList).
						getResultList();
							
				LOGGER.info("TEST2: " + agencyGroupList.toString());
			}else{
				agencyGroupList = new ArrayList<RefAgencyGroup>();
			}
		}
		
		if(targetAgency == null){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Invalid Target Agency",
					"Target Agency Group not found or is disabled"));
			return;
		}
		
		RefAgencyGroup sampleViewOwnerAgency = null;
		
		if(owner != null && !owner.isEmpty()){
			for(RefAgencyGroup selectedAgency : agencyGroupList){
				if(selectedAgency.getAgencyGroupShortname().equals(owner)){
					sampleViewOwnerAgency = selectedAgency; 
					break;
				}
			}
		}
		
		if(sampleViewMode && sampleViewOwnerAgency == null){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Invalid Target Agency",
					"Owner Agency Group not found or is disabled"));
			return;
		}
		
		menuModelMap = new HashMap<Integer, MenuModel>();
		displayList = new HashMap<Integer, List<RefFileProperty>>();
		mapAgencyFolderVisible = new HashMap<Integer, Boolean>();
		
		agencyVisibilityList = service.getAgencyFolderVisibleByTargetAndIsFolderVisible(targetAgency);
		
		if(agencyGroupList != null && !agencyGroupList.isEmpty()){
			focusAgencyGroupTab = agencyGroupList.get(0);

			
			if(targetAgency != null){
				initMapAndMenuModels(targetAgency, sampleViewOwnerAgency);
			}
			
			//Set defaults
			//Set active tab index to first agency in the list
			activeIndex = agencyGroupList.indexOf(focusAgencyGroupTab);
			
			//if focus agency tab has no permission to view folders, show immediately all files viewable
			if(mapAgencyFolderVisible.get(focusAgencyGroupTab.getAgencyGroupId()) == false){
				entityList = new ArrayList<RefFileProperty>(displayList.get(focusAgencyGroupTab.getAgencyGroupId()));
			}
			
			//log view		
			if(sampleViewMode){
				service.getFacade().logAuditTrail("Sample View Mode",AuditTrail.ACTION_VIEW,"Owner: " + sampleViewOwnerAgency.getAgencyGroupShortname() + " Target: " + targetAgency.getAgencyGroupShortname(),auth.getAgencyGroup(),auth.getCuser());
			}else{
				service.getFacade().logAuditTrail("Shared Files",AuditTrail.ACTION_VIEW,"Shared Files",auth.getAgencyGroup(),auth.getCuser());
			}
		}
		
		if(downloadCompleted){
			PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage("Download File",
					"File_Name.zip has been downloaded."));
			return;
		}
		
	}

	public void selectedFilesMany()
	{
		System.out.println(selectedFiles.size() + "><><><");
	}

	private void initMapAndMenuModels(RefAgencyGroup targetAgency, RefAgencyGroup sampleViewOwnerAgency) {
		agencyGroupToFolderToFileMap = new HashMap<Integer, Map<String,List<RefFileProperty>>>();
		for(RefAgencyGroup rag : agencyGroupList){
			
			if(sampleViewMode && !rag.equals(sampleViewOwnerAgency)){
				//if in sampleViewMode, skip initialization of all agencies other than the agency associated with the logged in user
				continue;
			}else if(sampleViewMode){
				focusAgencyGroupTab = rag;
			}
			
			boolean agencyFolderVisible = false;
			for(RefAgencyGroupFolderVisible rafv : agencyVisibilityList){
				if(rafv.getOwnerGroupId().equals(rag) && rafv.getFolderVisible() == RefAgencyGroupFolderVisible.VISIBLE){
					agencyFolderVisible = true;
					break;
				}
			}
			
			LOGGER.info("AGENCY: " + rag.getAgencyGroupShortname() + " VISIBLE: " + agencyFolderVisible);
			mapAgencyFolderVisible.put(rag.getAgencyGroupId(), agencyFolderVisible);
			displayList.put(rag.getAgencyGroupId(), new ArrayList<RefFileProperty>());
			menuModelMap.put(rag.getAgencyGroupId(), new DefaultMenuModel());
			
			if(agencyFolderVisible){
				DefaultMenuItem menuItem;
				agencyGroupToFolderToFileMap.put(rag.getAgencyGroupId(), new HashMap<String, List<RefFileProperty>>(1));
				List<RefFilePermission> permissionList = service.getVisibleFilePermissionBy(rag, targetAgency);
				for(RefFilePermission rfp : permissionList){
					
					if(rfp.getVisible() == RefFilePermission.VISIBLE_FALSE){
						continue;
					}
					
					File file = new File(service.getRootDirectory() + File.separatorChar + rfp.getRefFileProperty().getRelativeLocation());
					
					rfp.getRefFileProperty().setSize(FileSizeUtil.formatSizeToString(file.length()));
					
					File parentFolder = file.getParentFile();
					String relativePath = service
							.retrieveRelativeFilePath(parentFolder.getPath());
					if(auth.getTestMode()){
						relativePath = relativePath.replace("\\", "/");
						LOGGER.info("find: relative path: " + relativePath);
					}
//						LOGGER.info("rfp.getRefFileProperty().getRelativeLocation(): " + rfp.getRefFileProperty().getRelativeLocation());
					if(rfp.getRefFileProperty().getRelativeLocation().startsWith(relativePath)){

						RefFileProperty folderRfp = null;
						if(auth.getTestMode()){
							folderRfp = service.lookupFileInDB(parentFolder.getPath().replace("\\", "/"));
						}else{
							folderRfp = service.lookupFileInDB(parentFolder.getPath());
						}
						
						if(folderRfp != null){
							LOGGER.info("ETO: " + folderRfp.getFileName());
							//menuItem = new DefaultMenuItem(folderRfp.getFileName(), "ui-close-folder");
							//menuItem.setAjax(true);
							//menuItem.setCommand("#{sharedFilesBean.showFolderContent('" + folderRfp.getFileName() + "')}");
							//menuItem.setUpdate(":form1");
							//menuItem.setStyleClass(DEFAULT_MENU_STYLECLASS);
							menuItem = DefaultMenuItem.builder()
									.value(folderRfp.getFileName())
									//.value("ui-close-folder")
									.ajax(true)
									.command("#{sharedFilesBean.showFolderContent('" + folderRfp.getFileName() + "')}")
						            .update(":form1")
						            .styleClass(DEFAULT_MENU_STYLECLASS)
						            .build();
							
							
							if(agencyGroupToFolderToFileMap.get(rag.getAgencyGroupId()).get(folderRfp.getFileName()) == null){
								agencyGroupToFolderToFileMap.get(rag.getAgencyGroupId()).put(folderRfp.getFileName(), new ArrayList<RefFileProperty>(1));
								//menuModelMap.get(rag.getAgencyGroupId()).addElement(menuItem);
								menuModelMap.get(rag.getAgencyGroupId()).getElements().add(menuItem);
							}
							
							agencyGroupToFolderToFileMap.get(rag.getAgencyGroupId()).get(folderRfp.getFileName()).add(rfp.getRefFileProperty());
						}else{
							LOGGER.info("Folder not found: " + parentFolder.getPath());
						}
					}
				}
				
				if(menuModelMap.get(rag.getAgencyGroupId()).getElements() == null || menuModelMap.get(rag.getAgencyGroupId()).getElements().size() == 0){
					//menuItem = new DefaultMenuItem("[NO FOLDERS FOUND]", "ui-icon-close");
					//menuModelMap.get(rag.getAgencyGroupId()).addElement(menuItem);
					menuItem = DefaultMenuItem.builder()
							.value("[NO FOLDERS FOUND]")
							.value("ui-icon-close")
							.build();
					
					menuModelMap.get(rag.getAgencyGroupId()).getElements().add(menuItem);
				}
			}else{
				for(RefFilePermission rfp : service.getVisibleFilePermissionBy(rag, targetAgency)){
					if(rfp.getVisible() == RefFilePermission.VISIBLE_TRUE){
						File file = new File(service.getRootDirectory() + File.separatorChar + rfp.getRefFileProperty().getRelativeLocation());
						rfp.getRefFileProperty().setSize(FileSizeUtil.formatSizeToString(file.length()));
						displayList.get(rag.getAgencyGroupId()).add(rfp.getRefFileProperty());
					}
				}
			}
			
			Collections.sort(menuModelMap.get(rag.getAgencyGroupId()).getElements(), new Comparator<MenuElement>() {
				
              @Override
              public int compare(MenuElement o1, MenuElement o2) {
            	  if (o1 instanceof DefaultMenuItem) {
					DefaultMenuItem dm1 = (DefaultMenuItem) o1;
					DefaultMenuItem dm2 = (DefaultMenuItem) o2;

	              	LOGGER.info("dm1: " + o1 + " o2: " + dm2);
	                  return dm1.getValue().toString().compareTo(dm2.getValue().toString());
				}
            	  return 0;
              }

          });
			
					
					
			
//			Map<String, List<RefFileProperty>> sortedList = new TreeMap<String, List<RefFileProperty>>(new Comparator<String>() {
//
//                @Override
//                public int compare(String o1, String o2) {
//                	LOGGER.info("o1: " + o1 + " o2: " + o2);
//                    return o2.compareTo(o1);
//                }
//
//            });
//			
//			sortedList.putAll(agencyGroupToFolderToFileMap.get(rag.getAgencyGroupId()));
//			
//			agencyGroupToFolderToFileMap.put(rag.getAgencyGroupId(), sortedList);
		}
	}
	
	
	public void cbxValue(RefFileProperty rfp){
		String details = check ? "true" : "false";
		LOGGER.info("cbxValue details: " + details);
		LOGGER.info("cbxValue: " + rfp.getDisplayName());
	}
	public void download(RefFileProperty rfp){
		LOGGER.info("download: " + rfp.getDisplayName());
		File toDownload = new File(service.getRootDirectory() + File.separatorChar + rfp.getRelativeLocation());
		System.out.println("1" + toDownload);
		boolean noProblemFound = false;
		
		if(!auth.getTestMode() && avService != null){
			System.out.println("2");
			try {
				if(toDownload.exists()){
					try {
						System.out.println("3");
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
			System.out.println("4");
		}
		
		if(noProblemFound){
			try {
				System.out.println("5");
				Faces.sendFile(toDownload, true);
				String displayFolder = "";
				try{
					LOGGER.info("Eto: " + toDownload);
					if(toDownload.exists()){
						System.out.println("6");
						File folderFile = toDownload.getParentFile();
						LOGGER.info("folderFile: " + folderFile.getAbsolutePath());
						LOGGER.info("folderFile: " + folderFile.getCanonicalPath());
						LOGGER.info("folderFile: " + folderFile.getPath());
						
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
					logString = displayFolder + ", File: " + rfp.getFileName();
				}
				
				service.getFacade().logAuditTrail("Shared Files", AuditTrail.ACTION_DOWNLOAD_FILE, logString, auth.getAgencyGroup(), auth.getCuser());
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
	
	private List<RefFileProperty> entityList;
	
	public void showFolderContent(String parentFolder){
		LOGGER.info("showFolderContent(): " + parentFolder);
		
		setEntityList(agencyGroupToFolderToFileMap.get(focusAgencyGroupTab.getAgencyGroupId()).get(parentFolder));
		
		for(MenuElement elem : menuModelMap.get(focusAgencyGroupTab.getAgencyGroupId()).getElements()){
			if (elem instanceof DefaultMenuItem) {
				DefaultMenuItem dmi = (DefaultMenuItem) elem;
				if(dmi.getValue().toString().equals(parentFolder)){
					dmi.setStyleClass(DEFAULT_MENU_STYLECLASS + " activeitem ui-state-active selectedfolder");
					dmi.setIcon("ui-open-folder");
				}else{
					dmi.setStyleClass(DEFAULT_MENU_STYLECLASS);
					dmi.setIcon("ui-close-folder");
				}
			}
		}
		if(sampleViewMode){
			service.getFacade().logAuditTrail("Sample View Mode", AuditTrail.ACTION_VIEW, "Folder: " + parentFolder, auth.getAgencyGroup(), auth.getCuser());
		}else{
			service.getFacade().logAuditTrail("Shared Files", AuditTrail.ACTION_VIEW, "Folder: " + parentFolder, auth.getAgencyGroup(), auth.getCuser());
		}
		
	}
	
//	public void dlselectedFiles(){
//		LOGGER.info("Carlo dito ");
//		if(selectedFiles != null && !selectedFiles.isEmpty()){
//			for(RefFileProperty forDelete : selectedFiles){
////				this.forDelete = forDelete;
//				LOGGER.info("selectedFiles(): " + forDelete.getFileName());
////				delete();
//			}
////			selectedFiles.clear();
//		}
//	}
	
	public void dlselectedFiles() throws IOException, Exception {
		LOGGER.info("Multiple Download");
		
		RefFileProperty refFile = null;
		ArrayList<File> files = new ArrayList<File>();
		
		if (selectedFiles != null && !selectedFiles.isEmpty()) {
			for (RefFileProperty forDownload : selectedFiles){
				System.out.println("File Names: " + forDownload.getFileName());
				
				refFile = forDownload;
				
				File toDownload = new File(service.getRootDirectory() 
						+ File.separatorChar + refFile.getRelativeLocation());
				
				files.add(toDownload);
				//download(forDownload);
			}
			
			System.out.println("Files to download: " + files.size());
			//done collecting selected files
		}
	
		if(files.size() != 0)
			 zip(files);
		
	}
	
	

	private File zip(ArrayList<File> files) {
		LOGGER.info("ZIP Download");
		//String filename = "C:\\Users\\GALVANMJ\\Downloads\\downloads.zip";
		//String filename = "C:\\FSCCIS\\downloads.zip";
		String home = System.getProperty("user.home");
		
		String[] path = selectedFiles.get(0).getRelativeLocation().split("/");
		String[] finalPaths = new String[path.length -1];
		
		for(String a : path)
			System.out.print(a);
		
		for(int count = 0 ;count < path.length-1 ; count ++)
		{
			if(count != path.length)
				finalPaths[count] = path[count];
		}
		
		System.out.println(String.join("/",finalPaths));
		String pathtoUse = String.join("/",finalPaths);
		
		//Folder name to be used
		RefFileProperty folderName = service.getFacade().getEntityManager()
				.createNamedQuery("RefFileProperty.findByRelativeLocation", RefFileProperty.class)
				.setParameter("relativeLocation", pathtoUse).getSingleResult();

		//LOGGER.info("RefFileProperty folderName: " + folderName.getFileName());
		
		//File Date
		 Date date = Calendar.getInstance().getTime();
		 DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		 String strDate = dateFormat.format(date); 
		 //File zipFile = new File(new File(home,"Downloads"),folderName.getFileName()
		 String finalName = folderName.getFileName()
					+ "_" + strDate + ".zip";
		 
		 File zipFile = new File(finalName.replaceAll(" ", "_"));
		
		System.out.println("Final Zip Name: " + zipFile);
		//final zip name with or without /downloads
		
		
		//create a buffer for reading the files
		byte[] buf = new byte[1024];
		
		try {
			//create ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
			System.out.println("ZipOutputStream: " + out);
			
			//compress the files
			for (int i=0; i<files.size(); i++) {
				//System.out.println("for loop 1");
				
				FileInputStream in = new FileInputStream(files.get(i).getAbsolutePath());
				System.out.println("FileInputStream: " + files.get(i).getAbsolutePath());
				
				// add ZIP entry to output stream
				out.putNextEntry(new ZipEntry(files.get(i).getName()));
				System.out.println("getName: " + files.get(i).getName());
				//System.out.println("for loop 2");
				
				// transfer bytes from the file to the ZIP file
	            int len;
	            while((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	            // complete the entry
	            out.closeEntry();
	            in.close();
			}
			// complete the ZIP file
			 out.close();
			 
			 

/*		ByteArrayOutputStream zipbaos = new ByteArrayOutputStream();
		        ZipOutputStream zos = new ZipOutputStream(zipbaos);
		        byte[] bytes = null;
		        for(File file : files){
		        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	            bytes = baos.toByteArray();
    	            final ZipEntry ze = new ZipEntry(file.getName());
    		    	zos.putNextEntry(ze);
        			zos.write(bytes, 0, bytes.length);
        			zos.closeEntry();
	        	}
		      
    			zos.close();
    			ByteArrayInputStream zipStream = new ByteArrayInputStream(zipbaos.toByteArray());
    			InputStream stream = zipStream;
    			fileDownloadView(stream, zipFile);*/
			
			//complete the ZIP file
	        System.out.println("Done! ZIPPING FILE/CHECK FILE NAME KUNG MAY LOC: " + zipFile);
	        
	        downloadZip(zipFile);
	        //fileDownloadView(zipFile);
	    	//selectedFiles.clear();	        
	    	return zipFile;
	    	
		} catch (IOException e) {
	        System.out.println(e.getMessage());
		}
		return null;
		
	}
	
/*	public void fileDownloadView(InputStream stream, File zipFile) {
	//public void fileDownloadView(File zipFile) {
	
		zFile = DefaultStreamedContent.builder()
				.name(zipFile.getName())
				.contentType("application/zip")
				.contentEncoding(Charsets.UTF_8.name())
				.stream(() -> stream)
				.build();
		
		System.out.println("zFile final result: " + zFile);
		System.out.println("zFile final result: " + zFile.getContentEncoding());
		System.out.println("zFile final result: " + zFile.getContentType());
		System.out.println("zFile final result: " + zFile.getName());
		System.out.println("zFile final result: " + zFile.getContentLength());
		System.out.println("zFile final result: " + zFile.getClass());
		System.out.println("zFile final result: " + zFile.getStream());
	}*/

/*	public void fileDownloadView(File zipFile) {
		
		System.out.println("name: " + zipFile.getName());
		System.out.println("stream: " + zipFile.getAbsolutePath());
		
		zFile = DefaultStreamedContent.builder()
				.name(zipFile.getName())
				.contentType("application/zip")
				.contentEncoding(Charsets.UTF_8.name())
				.stream(() -> FacesContext.getCurrentInstance().getExternalContext()
						.getResourceAsStream(zipFile.getAbsolutePath()))
				.build();
		
		System.out.println("END OF ZIP FILE PROCESS: " + zFile);
		
	}*/

	private void downloadZip(File zipFile) {
		LOGGER.info("download: " + zipFile);
		File toDownload = new File(zipFile.getAbsolutePath());
		System.out.println("1: " + toDownload);
		boolean noProblemFound = false;
		
		if(!auth.getTestMode() && avService != null){
			System.out.println(2);
			try {
				if(toDownload.exists()){
					System.out.println(3);
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
			System.out.println(4);
		}
		
		if(noProblemFound){
			System.out.println(5);
			try {
				Faces.sendFile(toDownload, true);
				String displayFolder = "";
				System.out.println(6);
				try{
					LOGGER.info("Eto: " + toDownload);
					if(toDownload.exists()){
						System.out.println(7);
						File folderFile = toDownload.getParentFile();
						LOGGER.info("folderFile: " + folderFile.getAbsolutePath());
						LOGGER.info("folderFile: " + folderFile.getCanonicalPath());
						LOGGER.info("folderFile: " + folderFile.getPath());
						
						//RefFileProperty folder = null;
						/*if(auth.getTestMode()){

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
						}*/
					}
				}catch(Exception e){}
				String logString = "";
				if(displayFolder.isEmpty()){
					logString = "File: " + toDownload.getName();
				}else{
					logString = displayFolder + ", File: " + toDownload.getName();
				}
				
				service.getFacade().logAuditTrail("Shared Files", AuditTrail.ACTION_DOWNLOAD_FILE, logString, auth.getAgencyGroup(), auth.getCuser());
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

	public void onTabChange(TabChangeEvent event) {
		Tab activeTab = event.getTab();
		String agencyShortname = activeTab.getTitle();
		LOGGER.info("onTabChange(): " + agencyShortname);
		
		//set menu of previous tabs to default
		for(MenuElement elem : menuModelMap.get(focusAgencyGroupTab.getAgencyGroupId()).getElements()){
			if (elem instanceof DefaultMenuItem) {
				DefaultMenuItem dmi = (DefaultMenuItem) elem;
				dmi.setStyleClass(DEFAULT_MENU_STYLECLASS);
				dmi.setIcon("ui-close-folder");
			}
		}
		
		for(RefAgencyGroup selectedAgency : agencyGroupList){
			LOGGER.info("selectedAgency.toTitleString():" + selectedAgency.toTitleString());
			if(selectedAgency.toTitleString().equals(agencyShortname)){
				focusAgencyGroupTab = selectedAgency;
				LOGGER.info("focusAgencyTab: " + selectedAgency.getAgencyGroupShortname());
				break;
			}
		}
		
		if(mapAgencyFolderVisible.get(focusAgencyGroupTab.getAgencyGroupId()) == false){
			entityList = new ArrayList<RefFileProperty>(displayList.get(focusAgencyGroupTab.getAgencyGroupId()));
		}else if(getEntityList() != null){
			setEntityList(new ArrayList<RefFileProperty>());
		}
	}
	
	public Map<Integer,MenuModel> getMenuModelMap() {
		return menuModelMap;
	}

	public void setMenuModelMap(Map<Integer,MenuModel> menuModelMap) {
		this.menuModelMap = menuModelMap;
	}

	public List<RefAgencyGroup> getAgencyGroupList() {
		return agencyGroupList;
	}

	public void setAgencyGroupList(List<RefAgencyGroup> agencyGroupList) {
		this.agencyGroupList = agencyGroupList;
	}

	public Map<Integer,Boolean> getMapAgencyFolderVisible() {
		return mapAgencyFolderVisible;
	}

	public void setMapAgencyFolderVisible(Map<Integer,Boolean> mapAgencyFolderVisible) {
		this.mapAgencyFolderVisible = mapAgencyFolderVisible;
	}

	public Map<Integer,List<RefFileProperty>> getDisplayList() {
		return displayList;
	}

	public void setDisplayList(Map<Integer,List<RefFileProperty>> displayList) {
		this.displayList = displayList;
	}

	public RefAgencyGroup getFocusAgencyGroupTab() {
		return focusAgencyGroupTab;
	}

	public void setFocusAgencyGroupTab(RefAgencyGroup focusAgencyGroupTab) {
		this.focusAgencyGroupTab = focusAgencyGroupTab;
	}

	public Boolean getSampleViewMode() {
		return sampleViewMode;
	}

	public void setSampleViewMode(Boolean sampleViewMode) {
		this.sampleViewMode = sampleViewMode;
	}

	public List<RefFileProperty> getEntityList() {
		return entityList;
	}

	public void setEntityList(List<RefFileProperty> entityList) {
		this.entityList = entityList;
	}

	public Integer getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}



	public List<RefFileProperty> getSelectedFiles() {
		return selectedFiles;
	}



	public void setSelectedFiles(List<RefFileProperty> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public StreamedContent getzFile() {
		return zFile;
	}

	public void setzFile(StreamedContent zFile) {
		this.zFile = zFile;
	}

	public StreamedContent getZipFile() {
		return zipFile;
	}

	public void setZipFile(StreamedContent zipFile) {
		this.zipFile = zipFile;
	}


	
	
	
}
