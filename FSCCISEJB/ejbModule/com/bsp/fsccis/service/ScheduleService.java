package com.bsp.fsccis.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.io.FileUtils;

import com.bsp.frprr.entity.FRPFilePath;
import com.bsp.frprr.service.FRPFileService;
import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgency;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisible;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisiblePK;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFilePermissionPK;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.SysConfiguration;
import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.properties.AutoUploadProperties;
import com.ibm.ws.management.sync.FolderSyncRequest;
import com.ibm.ws.webservices.xml.wassysapp.systemApp;

@Startup
@Singleton
public class ScheduleService {

	@EJB
	private GenericFacade facade;

	@EJB
	private ReportService service;

	@EJB
	private FRPFileService fileService;

	private RefFileProperty refFileProp;

	private static Logger logger = Logger.getLogger(ScheduleService.class.getName());

	private String sourceFiles;
	private String targetFolder;

	private String relativeLocation;

	private String rootDirectory;
	private RefFileProperty folder;
	private List<RefFileProperty> listForDelete;
	private List<RefFileProperty> checkRefFileProperty;
	private List<FRPFilePath> allTranFile;
	private RefAgencyGroup refAgencyGroup;
	private List<RefAgency> refAgencys;

	InputStream is = null;
	OutputStream os = null;

	Date today = new Date();

	public static final String AUTO_UPLOAD = "Auto_Upload";

	private String fileName;
	
	private String path;
	private RefAgencyGroup admin;

	private AutoUploadProperties props;
	
	private boolean errorLog = false;
	private String errorMsg = null;
	
	public GenericFacade getFacade() {
		return this.facade;
	}

	@Schedules({ 
		//@Schedule(hour = "*", minute = "*/1", persistent = false),
		@Schedule(hour = "5", minute = "0", second = "0", persistent = false)
	})
	public void uploadFiles() {
		//Upload_UKB,Upload_TB,Upload_RCB
		try{
			props = new AutoUploadProperties();
			
			if(props.initializeAutoUploadStatus("Upload_UKB")){
				logger.log(Level.INFO, "---------------------///UKB AUTO UPLOAD///---------------------------");
				uploadFiles("Upload_UKB");
				logger.log(Level.INFO, "---------------------///END UKB AUTO UPLOAD ///---------------------------");
			}
			
			if(props.initializeAutoUploadStatus("Upload_TB")){
				logger.log(Level.INFO, "---------------------///TB AUTO UPLOAD///---------------------------");
				uploadFiles("Upload_TB");
				logger.log(Level.INFO, "---------------------///END TB AUTO UPLOAD ///---------------------------");
			}
			
			if(props.initializeAutoUploadStatus("Upload_RCB")){
				logger.log(Level.INFO, "---------------------///RCB AUTO UPLOAD///---------------------------");
				uploadFiles("Upload_RCB");
				logger.log(Level.INFO, "---------------------///END RCB AUTO UPLOAD ///---------------------------");
			}
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		

		
	}
	
	@Schedules({ 
		//@Schedule(hour = "*", minute = "*/1", persistent = false),
		@Schedule(hour = "19", minute = "0", second = "0", persistent = false)
	})
	public void deleteFiles() {
		//Delete_UKB,Delete_TB,Delete_RCB
		
		try{
			props = new AutoUploadProperties();
			
			
			if(props.initializeAutoUploadStatus("Upload_UKB")){ 
				logger.log(Level.INFO, "---------------------///UKB DELETE///---------------------------");
				deleteFiles("Delete_UKB");
				logger.log(Level.INFO, "---------------------///END UKB DELETE ///---------------------------");
			}
			
			if(props.initializeAutoUploadStatus("Upload_TB")){
				logger.log(Level.INFO, "---------------------///TB DELETE///---------------------------");
				deleteFiles("Delete_TB");
				logger.log(Level.INFO, "---------------------///END TB DELETE ///---------------------------");
			}
			
			if(props.initializeAutoUploadStatus("Upload_RCB")){
				logger.log(Level.INFO, "---------------------///RCB DELETE///---------------------------");
				deleteFiles("Delete_RCB");
				logger.log(Level.INFO, "---------------------///END RCB DELETE ///---------------------------");
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
				

	}
	
	public void uploadFiles(String bankProfile) {
		logger.log(Level.INFO, "Init upload: ");

		props = new AutoUploadProperties();
		try {
			logger.log(Level.INFO, "Initialize properties");
			props.initializeAutoUploadProperties(bankProfile);

			int FirstBatchDay = Integer.valueOf(props.getFirstBatchDate());
		    int secondBatchInterval = Integer.valueOf(props.getSecondBatchDateInterval());
		      
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
			SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
			Date today = Calendar.getInstance().getTime();
			String dateToday = df.format(today);

				String firstBatchDate = null;
				String secondBatchDate = null;
				//String deleteBatchDate = null;
			System.out.println("uploadFiles - Today is: " + dateToday);
            Calendar c =   (Calendar) Calendar.getInstance();
            System.out.println("M :"+(Integer.valueOf(monthFormat.format(today))));
            System.out.println("Y :"+(Integer.valueOf(yearFormat.format(today))));
            c.set(Calendar.DAY_OF_MONTH, FirstBatchDay);
            Date dt = null;
            firstBatchDate = df.format(c.getTime());
            logger.log(Level.INFO, "uploadFiles - 1st Run: " + firstBatchDate);
            
            //dayOfFirstBatch is 1st batch day
            if(Integer.valueOf(dayFormat.format(today)) < FirstBatchDay)
            	c.set(c.MONTH, (Integer.valueOf(monthFormat.format(today))-2));
			//	c.add(Calendar.DATE, secondBatchInterval);
			//	dt = c.getTime();
			//	secondBatchDate = df.format(c.getTime());
				
			//} else if (Integer.valueOf(dayFormat.format(today)) > FirstBatchDay) {
				c.add(c.DATE, secondBatchInterval);
				dt = c.getTime();
				secondBatchDate = df.format(c.getTime());	
			//} 
			
			logger.log(Level.INFO, "uploadFiles - 2nd Run: " + secondBatchDate);

			if (firstBatchDate != null && firstBatchDate.equals(dateToday)) {
				logger.log(Level.INFO, "uploadFiles - First batch run ");
				initializeUpload(bankProfile, firstBatchDate, FirstBatchDay ,secondBatchInterval);

			} else if (secondBatchDate != null && secondBatchDate.equals(dateToday)) {
				logger.log(Level.INFO, "uploadFiles - Second batch run ");
				initializeUpload(bankProfile, secondBatchDate, FirstBatchDay, secondBatchInterval);
			}

			if (errorLog) {

				// Audit logs
				AuditTrail trail = new AuditTrail("Auto Upload", AuditTrail.ACTION_ADD);
				trail.setDetails(errorMsg);
				trail.setAgencyGroupId(admin);
				trail.setCdate(new Date());
				trail.setCtime(new Date());
				trail.setCuser(AUTO_UPLOAD);

				service.getFacade().logAuditTrail(trail);
			} else if (props.isErrLogMsg()) {

				// Audit logs
				AuditTrail trail = new AuditTrail("Auto Upload", AuditTrail.ACTION_ADD);
				trail.setDetails(props.getErrMsg());
				trail.setAgencyGroupId(admin);
				trail.setCdate(new Date());
				trail.setCtime(new Date());
				trail.setCuser(AUTO_UPLOAD);

				service.getFacade().logAuditTrail(trail);
			}

			logger.log(Level.INFO, "--- schedule task completed ---");

		} catch (Exception e) {
			logger.log(Level.INFO, "uploading file exception... " + e.toString());

		}
	}

	public void initializeUpload(String bankProfile, String endDate, int firstBatchDay, int secondBatchInterval) throws ParseException {
		logger.log(Level.INFO, "INITIALIZE AUTO UPLOAD ");
		
		try {
			//ROOT - get root directory
			rootDirectory = service.getFacade().getEntityManager()
					.createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", SysConfiguration.ROOT_FOLDER_DIRECTORY).getSingleResult();
			
			File rootdir = new File(rootDirectory);
			//logger.log(Level.INFO, "rootdir.getAbsolutePath(): " + rootdir.getAbsolutePath());
			
			//ADMIN - get admin
			admin = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyGroupShortnameAdmin", RefAgencyGroup.class)
					.setParameter("agencyGroupShortname", props.getAgencyGroupShortNameAdmin()).getSingleResult();
			logger.log(Level.INFO,
					"initializeUpload - Admin: " + admin.getAgencyGroupName() + " - " + admin.getAgencyGroupShortname());
			
			//Get dates for FRPFiles
			logger.log(Level.INFO,"firstBatchDay: " + firstBatchDay + " secondBatchInterval: " + secondBatchInterval);
			String startDate = getStartDate(firstBatchDay, secondBatchInterval);
			
			// source FRPFilePath			
			allTranFile = fileService.findPath(bankProfile,startDate,endDate);
			
			
			
			for (FRPFilePath sourcePath : allTranFile) {
				
				fileName = sourcePath.getFileName();
				sourceFiles = sourcePath.getFilePath();
				
				logger.log(Level.INFO, "-----------------------------------");
				logger.log(Level.INFO, "FILE NAME: " + fileName);
				logger.log(Level.INFO, "FILE PATH: " + sourceFiles);
				logger.log(Level.INFO, "-----------------------------------");
				
				if (sourceFiles != null && !sourceFiles.isEmpty() && sourceFiles.contains("DATA")) {
					
					String[] substringsfolderLocation = sourceFiles.split("DATA");
					
					String relativeLoc= null;
					String[] fileNameHolder = null;
					
					relativeLocation = substringsfolderLocation[1];
					
					if(relativeLocation.contains("/")){
						
						String NewPath =  targetFolder.replace("\\", "/");
						 fileNameHolder = relativeLocation.split("/");
						 
					} else {
						
						String NewPath =  relativeLocation.replace("\\", "/");
						 fileNameHolder = NewPath.split("/");
						 
					}
					logger.log(Level.INFO, "substrings.length "+ fileNameHolder.length);
					for(int j=1;j<=fileNameHolder.length-1;j++){
					
						if(j == 1) {
							relativeLoc = "/FOLDER "+admin.getAgencyGroupId()+"/"+"Folder 1";
						} 
						else {
							relativeLoc +="/"+"Folder 1";
						}
						
						System.out.println("PARAM 0: " + fileNameHolder[j]);
						System.out.println("PARAM 1: " + j + " : " + "PARAM 2: " + relativeLoc);
					
						
						String lastFolder= "";
						String folders[] = relativeLoc.split("/");
						for(int count = 0 ; count <folders.length ; count ++)
						{
							if (count == 1) 
								lastFolder = "/" + folders[count];
							
							else if (count == folders.length-1)
								lastFolder = lastFolder + "/" + folders[count].substring(0,6);
							else
								lastFolder = lastFolder + "/" + folders[count];
						}
						System.out.println("PARAM 1: " + j + " : " + "PARAM 3: " + lastFolder);
						checkRefFileProperty = service.getFacade().getEntityManager()
                                .createNamedQuery("RefFileProperty.findFolders", RefFileProperty.class)
                                .setParameter(1,j)
                                .setParameter(2,lastFolder + "%")
                                .getResultList();
						
						logger.log(Level.INFO, "findFolders RESULT: checkRefFileProperty: " + checkRefFileProperty.size());
						
						if(checkRefFileProperty.size() >= 1) {
							int g=1;
							 int iterator = returnLastNumber(checkRefFileProperty, lastFolder);
				              for(int b=1;b<=iterator;b++){
				            	  
				            	    System.out.println("ITERATOR: " + iterator);
				                	System.out.println("TEST FILENAME part 1: "+checkRefFileProperty.get(b-1).getFileName().equals(fileNameHolder[j]));
				            	  
				                	boolean isMatch = false;
				                	for(RefFileProperty refFile : checkRefFileProperty)
									{
				                		System.out.println(">>>> "+ refFile.getFileName() +" -- " + fileNameHolder[j]);
										if(refFile.getFileName().equals(fileNameHolder[j]))
										{
											g = returnLastNumbers(refFile, lastFolder);
											isMatch = true;
											break;
										}
										
									}
				                	
				                	 if(!isMatch){
										
										//String relLoc = relativeLoc;
										//String[] testLoc = relLoc.split("/");
		                                //String[] getNextNumber = testLoc[testLoc.length-1].split(" ");
		                               //int nextFolderNumber = Integer.valueOf(getNextNumber[1]) +1;
				            		  int nextFolderNumber = Integer.valueOf(iterator) +1;
				            		  
		                                System.out.println(nextFolderNumber);
		                                 
		                                //if(b == 1){
		                                //	  relativeLoc = "/FOLDER "+admin.getAgencyGroupId()+"/"+"Folder "+nextFolderNumber+"";
		                                //	  break;
		                                //} else {
		                                	String holder = relativeLoc;
		                                	  relativeLoc = "";
		                                 	  
		                                	  relativeLoc = holder.substring(0, holder.length()-1)+nextFolderNumber+"";
		                                	  break;
		                                //}
		                                 
									} else if (isMatch){
										
										System.out.println("b: " + b);
										System.out.println("iterator: " + iterator);
										
										//if (b == 1) {
										//	relativeLoc = "/FOLDER "+admin.getAgencyGroupId()+"/"+"Folder "+ b +"";
										//	break;
											
										//} else {
											String holder = relativeLoc;
											relativeLoc = "";
											
											 relativeLoc = holder.substring(0, holder.length()-1)+ g +"";
		                                	  break;
										//}
									}
				              }
						}
						logger.log(Level.INFO, "initializeUpload - fileNameHolder :"+ fileNameHolder[j]); 
			    		  logger.log(Level.INFO, "initializeUpload - relativeLocHolder :" + relativeLoc);
			    		 
					}
					 String savingFolder = rootdir.getAbsolutePath().replace("\\", "/") + relativeLoc;
					 sendFiles(bankProfile, relativeLoc, savingFolder, fileNameHolder, sourceFiles);
				
				} else {
					
					logger.log(Level.INFO, "No available source to process");
				}	
				
			}
			
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, "NullPointer Exception " + e.getMessage());
			e.printStackTrace();
		
		}
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sendFiles(String bankProfile, String targetLocation, String savingFolder, String[] fileNameHolder,String sourceFilesPath) {
		
		logger.log(Level.SEVERE, "---------sendFiles - init------------");
		logger.log(Level.SEVERE, "Initialize sendFiles");
		logger.log(Level.SEVERE, "sendFiles - bankProfile " + bankProfile);
		logger.log(Level.SEVERE, "sendFiles - targetLocation " + targetLocation);
		logger.log(Level.SEVERE, "sendFiles - savingFolder " + savingFolder);
		logger.log(Level.SEVERE, "---------sendFiles - init------------");
		
		//if (sourceFiles != null && !sourceFiles.isEmpty()) {
		//	logger.log(Level.INFO, "sendFiles - Sending Files... : " + sourceFiles);
			
			File sFile = new File(sourceFilesPath); //For QA/PROD 
		
			//local-test
			//File sFile = null;
		
			//if (bankProfile.equals("Upload_UKB")) {
			//	sFile = new File("//10.2.38.116/Shared Folder/Credex/BSP TEMPLATE/fiportaldata/DATA/UKB");
			//} else if (bankProfile.equals("Upload_TB")) {
			//	sFile = new File("D:/SOURCE/TB");
			//} else if (bankProfile.equals("Upload_RCB")) {
			//	sFile = new File("D:/SOURCE/RCB");
			//}
			
			logger.log(Level.INFO, "sendFiles - USING LOCAL SOURCE: " + sFile.exists());
			
			File[] sourceFiles = sFile.listFiles();
			logger.log(Level.INFO, "sendFiles - Test number of files on location folder :" + sourceFiles.length);
			
			for (File fSource : sourceFiles) {
				logger.log(Level.INFO, "sendFiles - filename from source " + fSource.getName());

				//Validation of the file if excel
				if (fSource.getName().contains("xlsx") || fSource.getName().contains("xls")) {
					//logger.log(Level.INFO, "sendFiles - Validate fileName to source: " + fileName + " : " + fSource.getName() + " : "
					//		+ fileName.equalsIgnoreCase(fSource.getName()));
					
					if (fileName.equalsIgnoreCase(fSource.getName())) {
						logger.log(Level.INFO, "sendFiles - Source Original File Name: " + fSource.getName());
						
						String fileType = fSource.getName().substring(fSource.getName().indexOf("."),
								fSource.getName().length());
						logger.log(Level.INFO, "---fileType: " + fileType);
						
						String fName = fSource.getName().substring(0, fSource.getName().lastIndexOf("."));
						logger.log(Level.INFO, "---fileName: " + fName);
						
						DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
						String dateFormat = targetFormat.format(new Date(fSource.lastModified()));
						logger.log(Level.INFO, "---dateFormat: " + dateFormat);
						
						//Modified File Name with Date
						String bnkProf = bankProfile.substring(bankProfile.indexOf("_")+1, bankProfile.length());
						File fTargePath = new File(new File(targetLocation), fName + "_" + dateFormat +"_"+bnkProf+ fileType);
						
						logger.log(Level.INFO, "sendFiles - Target Modified File Name: " + fTargePath.getName());
						logger.log(Level.INFO, "sendFiles - Target Modified File source: " + fTargePath.getPath());
						
						try {
							
							logger.log(Level.INFO, "-------sendFiles - Inserting RefFileProperty-------");
							RefFileProperty refFileProp = new RefFileProperty();
							
							refFileProp.setCuser(AUTO_UPLOAD);
							
							//ADMIN
							refAgencyGroup = service.getFacade().getEntityManager()
									.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
									.setParameter("agencyGroupId", admin.getAgencyGroupId()).getSingleResult();
							logger.log(Level.INFO, "sendFiles - refAgencyGroupId: " + refAgencyGroup.getAgencyId());
							
							refFileProp.setOwnerGroupId(refAgencyGroup);
							refFileProp.setFileType(RefFileProperty.TYPE_XLS);
							refFileProp.setFileName(fTargePath.getName());
							refFileProp.setRelativeLocation(targetLocation+"/"+fTargePath.getName());
							
							logger.log(Level.INFO, "-----sendFiles - part 2------");
							logger.log(Level.INFO, "sendFiles - refFileProp Cuser:  " + refFileProp.getCuser());
							logger.log(Level.INFO, "sendFiles - refFileProp OwnerGroupId:  " + refFileProp.getOwnerGroupId().getAgencyId());
							logger.log(Level.INFO, "sendFiles - refFileProp FileType:  " + refFileProp.getFileType());
							logger.log(Level.INFO, "sendFiles - refFileProp FileName:  " + refFileProp.getFileName());
							logger.log(Level.INFO, "sendFiles - refFileProp RelativeLocation:  " + refFileProp.getRelativeLocation());
							logger.log(Level.INFO, "-----sendFiles - part 2------");
							
							//Checking if file exist
							checkRefFileProperty = service.getFacade().getEntityManager()
									.createNamedQuery("RefFileProperty.findByFileNameAndRelativeLocation",
											RefFileProperty.class)
									.setParameter("fileName", refFileProp.getFileName())
									.setParameter("relativeLocation", refFileProp.getRelativeLocation())
									.getResultList();
							
							logger.log(Level.INFO,
									"sendFiles - refFileProp.getFilePropertyId()1 : " + refFileProp.getFilePropertyId());

							if (checkRefFileProperty.size() == 0) {
								logger.log(Level.INFO, "sendFiles - Duplicates: " + checkRefFileProperty.size());
								
								service.getFacade().createAutoUpload(refFileProp, refAgencyGroup, AUTO_UPLOAD);
								
								logger.log(Level.INFO,
										"sendFiles - refFileProp.getFilePropertyId()2: " + refFileProp.getFilePropertyId());
								
								refAgencys = service.getFacade().getEntityManager()
										.createNamedQuery("RefAgency.findByAgencyShortnameFRP",RefAgency.class)
										.setParameter(1, props.getViewer1())
										.setParameter(2, props.getViewer2())
										.getResultList();
								
								
								insertFilePermission(refFileProp, refAgencys);
								insertAgencyGroupFolder(refAgencys);
								insertRelativeLoc(refFileProp.getFilePropertyId(),fileNameHolder);
								copyFileUsingStream(fSource, fTargePath.getName(), targetLocation,savingFolder);
								
								
								
								logger.log(Level.INFO, "-----Done Upload. Files from UKB source copied-----");
								
							} else {
								
								logger.log(Level.INFO, "File not Uploaded ");
								logger.log(Level.INFO, "Existing No. of Files: " + checkRefFileProperty.size());
								
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							logger.log(Level.SEVERE, "Failed to insert Files. " + e.getMessage());
						}
					}
				
				}
			}
		//}

	}



	private void insertAgencyGroupFolder(List<RefAgency> refAgencys2) {
		
		//Check if folder is visible
		logger.log(Level.INFO,
				"INSERT FOLDER VISIBLE: " + refAgencys2);
		
		for (RefAgency refAgencyVisible : refAgencys2) {
			
			RefAgencyGroupFolderVisible refFileGroupFolderVisible = new RefAgencyGroupFolderVisible();
			RefAgencyGroupFolderVisiblePK refFileGroupFolderVisiblePK = new RefAgencyGroupFolderVisiblePK();
			
			RefAgencyGroup refAgencyGroupVisible = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyIdandAgencyGroupShortname",
							RefAgencyGroup.class)
					.setParameter("agencyId", refAgencyVisible)
					.setParameter("agencyGroupShortname", props.getAgencyGroupShortNameViewer())
					.getSingleResult();
			
			List<RefAgencyGroupFolderVisible> refAgencyGroupFolderVisible = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroupFolderVisible.findByOwnerAgencyGroupTargetAgency",
							RefAgencyGroupFolderVisible.class)
					.setParameter("ownerGroupId", admin)
					.setParameter("targetGroupId", refAgencyGroupVisible)
					.getResultList();
			
			
			if (refAgencyGroupFolderVisible.size() == 0) {
				
				
				//FOLDER VISIBLE - Set Folder Visible
				refFileGroupFolderVisiblePK.setOwnerGroupId(admin.getAgencyGroupId());
				refFileGroupFolderVisiblePK.setTargetGroupId(refAgencyGroupVisible.getAgencyGroupId());
				
				refFileGroupFolderVisible.setRefAgencyGroupFolderVisiblePK(refFileGroupFolderVisiblePK);
				refFileGroupFolderVisible.setOwnerGroupId(admin);
				refFileGroupFolderVisible.setTargetGroupId(refAgencyGroupVisible);
				refFileGroupFolderVisible.setFolderVisible((short)1);
				refFileGroupFolderVisible.setCuser(AUTO_UPLOAD);
				
				
				logger.log(Level.INFO, 
						"sendFiles - refFileGroupFolderVisible filePropertyId: "+ refFileGroupFolderVisible.getOwnerGroupId().getDefaultId());
				logger.log(Level.INFO,
						"sendFiles - refFileGroupFolderVisible targetGroupId: " + refFileGroupFolderVisible.getTargetGroupId());
				logger.log(Level.INFO,
						"sendFiles - refFileGroupFolderVisible ownerGroupId: " + refFileGroupFolderVisible.getFolderVisible());
				logger.log(Level.INFO, 
						"sendFiles - refFileGroupFolderVisible cuser: " + refFileGroupFolderVisible.getCuser());
				
				service.getFacade().createAutoUpload(refFileGroupFolderVisible,  refAgencyGroupVisible, AUTO_UPLOAD);

			} else {
					
				//RefAgencyGroupFolderVisible refFileGroupFolderVisible2 = new RefAgencyGroupFolderVisible();
				
				refFileGroupFolderVisible = refAgencyGroupFolderVisible.get(0);
				
				System.out.println("RESULT OWNER: " + refAgencyGroupFolderVisible.get(0).getOwnerGroupId() + "  " 
				+ " TARGET: " + refAgencyGroupFolderVisible.get(0).getTargetGroupId());
					
					if (refFileGroupFolderVisible.getFolderVisible() != 1) {
						
						refFileGroupFolderVisible.setFolderVisible((short)1);
						refFileGroupFolderVisible.setCuser(AUTO_UPLOAD);
						
						service.getFacade().editAutoUpload(refFileGroupFolderVisible);

					}

			}
			
			
		}
		
	}

	private void insertFilePermission(RefFileProperty refFileProp2, List<RefAgency> refAgency) {
		
		//VIEWER - Set File Permission
		logger.log(Level.INFO,
				"INSERT FILE PERMISSION: " + refAgency);
		
		for (RefAgency refAgencyTarget : refAgency) {
			
			RefFilePermission refFilePermission = new RefFilePermission();
			RefFilePermissionPK refFilePermPK = new RefFilePermissionPK();
			
			logger.log(Level.INFO,
					"refAgencys LOOP ");
			
			RefAgencyGroup refAgencyGroupTarget = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyIdandAgencyGroupShortname",
							RefAgencyGroup.class)
					.setParameter("agencyId", refAgencyTarget)
					.setParameter("agencyGroupShortname", props.getAgencyGroupShortNameViewer())
					.getSingleResult();
			
			logger.log(Level.INFO,
					"refAgencyGroup result: " + refAgencyGroupTarget);
			
			
			//RefFilePermission for viewer
			refFilePermPK.setFilePropertyId(refFileProp2.getFilePropertyId());
			refFilePermPK.setTargetGroupId(refAgencyGroupTarget.getAgencyGroupId());
			
			refFilePermission.setRefFilePermissionPK(refFilePermPK);
			refFilePermission.setRefFileProperty(refFileProp2);
			refFilePermission.setTargetGroupId(refAgencyGroupTarget);
			refFilePermission.setOwnerGroupId(refAgencyGroup);
			refFilePermission.setVisible(RefFilePermission.VISIBLE_TRUE);
			refFilePermission.setCuser(AUTO_UPLOAD);
			
			logger.log(Level.INFO, 
					"sendFiles - refFilePermission filePropertyId: "+ refFilePermission.getRefFileProperty().getDefaultId());
			logger.log(Level.INFO,
					"sendFiles - refFilePermission targetGroupId: " + refFilePermission.getTargetGroupId());
			logger.log(Level.INFO,
					"sendFiles - refFilePermission ownerGroupId: " + refFilePermission.getOwnerGroupId());
			logger.log(Level.INFO, 
					"sendFiles - refFilePermission visible: " + refFilePermission.getVisible());
			logger.log(Level.INFO, 
					"sendFiles - refFilePermission cuser: " + refFilePermission.getCuser());
			
			//error new flush exception
			service.getFacade().createAutoUpload(refFilePermission, refAgencyGroupTarget, AUTO_UPLOAD);

		}
		
	}

	private void copyFileUsingStream(File fSource, String fileName, String targetDesti,String savingFolder) {
		logger.log(Level.INFO, "INTIALIZE copyFileUsingStream: ");
		logger.log(Level.INFO, "copyFileUsingStream - fSource : " + fSource);
		logger.log(Level.INFO, "copyFileUsingStream - fTarget : " + fileName);
		logger.log(Level.INFO, "copyFileUsingStream - targetDesti : " + targetDesti);
		//logger.log(Level.INFO, "copyFileUsingStream - savingFolder : " + savingFolder);
		
		try {
			File destinationPath = new File(savingFolder);
			if (destinationPath.exists()) {
				logger.log(Level.INFO, "copyFileUsingStream - Directory is existing : ");
				is = new FileInputStream(fSource);
				os = new FileOutputStream(destinationPath+"/"+fileName);
				byte[] buffer = new byte[1024];
				int length;

				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} else {
				logger.log(Level.INFO, "copyFileUsingStream - Directory is not existing  :");
				destinationPath.mkdirs();
				is = new FileInputStream(fSource);
				os = new FileOutputStream(destinationPath+"/"+fileName);
				byte[] buffer = new byte[1024];
				int length;

				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}

		} catch (Exception ex) {
			logger.log(Level.INFO, "copyFileUsingStream - Unable to copy file: " + ex.getMessage());
		} finally {
			try {
				is.close();
				os.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	public void insertRelativeLoc(Integer pk,String[] fileNameHolder) {
		logger.log(Level.INFO, "-------------Inserting RefFileProperty relative loc to DB-------------");
		
		String relativeLocHolder = "";
		//String[] substrings = null;
		logger.log(Level.INFO, "insertRelativeLoc - fileNameHolder " + fileNameHolder.length);
		
		for (int i = 1; i <= fileNameHolder.length-1;i++) {
			//System.out.println("insertRelativeLoc - substrings " + fileNameHolder[i]);
			System.out.println("insertRelativeLoc - fileNameHolder: "+fileNameHolder[i]);
			
			RefFileProperty refFileProp = new RefFileProperty();
			refFileProp.setCuser(AUTO_UPLOAD);
			
			refAgencyGroup = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
					.setParameter("agencyGroupId", admin.getAgencyGroupId()).getSingleResult();
			//logger.log(Level.INFO, "insertRelativeLoc - refAgencyGroupId: " + refAgencyGroup.getAgencyId());
			
			refFileProp.setOwnerGroupId(refAgencyGroup);
			refFileProp.setFileType(RefFileProperty.TYPE_FOLDER);
			refFileProp.setFileName(fileNameHolder[i]);
			
			if(i == 1){
				relativeLocHolder = "/FOLDER "+ admin.getAgencyGroupId() + "/" + "Folder 1";
			
			} else {
				 relativeLocHolder += "/" + "Folder 1";
			}
			refFileProp.setRelativeLocation(relativeLocHolder); 
			
			System.out.println("NEW RELATIVE LOC: " + refFileProp.getRelativeLocation());
			
			/*checkRefFileProperty = service.getFacade().getEntityManager()
                    .createNamedQuery("RefFileProperty.findByRelativeLocation", RefFileProperty.class)
                    .setParameter("relativeLocation",refFileProp.getRelativeLocation())
                    //.setParameter("fileName",fileNameHolder[i])
                    .getResultList();*/
			
			System.out.println("PART 2: PARAM 1: " + i + " : " + "PARAM 2: " + refFileProp.getRelativeLocation());
			
			
			String lastFolder= "";
			String folders[] = refFileProp.getRelativeLocation().split("/");
			for(int count = 0 ; count <folders.length ; count ++)
			{
				if (count == 1) 
					lastFolder = "/" + folders[count];
				
				else if (count == folders.length-1)
					lastFolder = lastFolder + "/" + folders[count].substring(0,6);
				else
					lastFolder = lastFolder + "/" + folders[count];
			}
			
			System.out.println("PARAM 1: " + i + " : " + "PARAM 3: " + lastFolder);
			checkRefFileProperty = service.getFacade().getEntityManager()
                    .createNamedQuery("RefFileProperty.findFolders", RefFileProperty.class)
                    .setParameter(1,i)
                    .setParameter(2, lastFolder + "%")
                    .getResultList();
			
			logger.log(Level.INFO, "findFolders RESULT: checkRefFileProperty: " + checkRefFileProperty.size());
			
			if(checkRefFileProperty.size() == 0){
				
				logger.log(Level.INFO,
						"INSERT part 1: " + refFileProp.getPk() + " : " + refFileProp.getCuser() + " : "
								+ refFileProp.getOwnerGroupId().getAgencyGroupId() + " : " + refFileProp.getFileType()
								+ " : " + refFileProp.getFileName() + " : " + refFileProp.getRelativeLocation());
				
				service.getFacade().createAutoUpload(refFileProp, refAgencyGroup, AUTO_UPLOAD);
				
			} else if(checkRefFileProperty.size() >= 1) {
				
				int g=1;
                int iterator = returnLastNumber(checkRefFileProperty, lastFolder);
                for(int b=1;b<=iterator;b++){
                	
                	System.out.println("ITERATOR: " + iterator);
                	System.out.println("TEST FILENAME part 2 :"+checkRefFileProperty.get(b-1).getFileName().equals(refFileProp.getFileName()));
                	boolean isMatch = false;
                	for(RefFileProperty refFile : checkRefFileProperty)
					{
                		System.out.println(">>>> "+ refFile.getFileName() +" -- " + fileNameHolder[i]);
						if(refFile.getFileName().equals(fileNameHolder[i]))
						{
							g = returnLastNumbers(refFile, lastFolder);
							isMatch = true;
							break;
						}
						//g++;
					}
                	
                	 if(!isMatch){
//                         String relLoc = refFileProp.getRelativeLocation();
//                         String[] testLoc = relLoc.split("/");
//                         String[] getNextNumber = testLoc[testLoc.length-1].split(" ");
                         int nextFolderNumber = Integer.valueOf(iterator) +1;
                         System.out.println(nextFolderNumber);
                        
                         //if(b == 1){
                       	 // relativeLocHolder = "/FOLDER "+admin.getAgencyGroupId()+"/"+"Folder "+nextFolderNumber+"";
                       	  
                         //}else{
                       	  String holder = relativeLocHolder;
                       	  relativeLocHolder = "";
                       	  
                       	  relativeLocHolder = holder.substring(0, holder.length()-1)+nextFolderNumber+"";
                       	  
                         //}
                         
                         refFileProp.setRelativeLocation(relativeLocHolder); 
                         
                         logger.log(Level.INFO,
         						"INSERT part 2: " + refFileProp.getPk() + " : " + refFileProp.getCuser() + " : "
         								+ refFileProp.getOwnerGroupId().getAgencyGroupId() + " : " + refFileProp.getFileType()
         								+ " : " + refFileProp.getFileName() + " : " + refFileProp.getRelativeLocation());
                         
                         service.getFacade().createAutoUpload(refFileProp, refAgencyGroup, AUTO_UPLOAD);
                         break;

               } else if (isMatch) {
            	   
            	   System.out.println("b: " + b);
					System.out.println("iterator: " + iterator);
					
					 //if(b == 1){
					//	 relativeLocHolder = "/FOLDER "+admin.getAgencyGroupId()+"/"+"Folder "+ b +"";
					 //} else {
						 String holder = relativeLocHolder;
                      	  relativeLocHolder = "";
                      	  
                      	  relativeLocHolder = holder.substring(0, holder.length()-1)+ g +"";
					 }
					 
					 refFileProp.setRelativeLocation(relativeLocHolder); 
					 break;
               //}
                }
                
                   
			}
		}
		
		
	}

	public void deleteFiles(String bankProfile){
		logger.log(Level.INFO, "Init deleteFiles");
		
		props = new AutoUploadProperties();
		
		try {
			logger.log(Level.INFO, "Initialize Properties");
			props.initializeAutoUploadProperties(bankProfile);
			
			
			int FirstBatchDay = Integer.valueOf(props.getFirstBatchDate());
			int secondBatchInterval = Integer.valueOf(props.getSecondBatchDateInterval());
			
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
			SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
			SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
			Date today = Calendar.getInstance().getTime();
			String dateToday = df.format(today);
			
			
			String firstBatchDate = null;
			String secondBatchDate = null;
			String deleteBatchDate = null;
			
			System.out.println("Today is: " + dateToday);
			Calendar c = (Calendar) Calendar.getInstance();
			System.out.println("M: " + (Integer.valueOf(monthFormat.format(today))));
			System.out.println("Y: " + (Integer.valueOf(yearFormat.format(today))));
			
			c.set(Calendar.DAY_OF_MONTH, FirstBatchDay);
			
			Date dt = null;
			
			firstBatchDate = df.format(c.getTime());
			logger.log(Level.INFO,"1st Run: " + firstBatchDate);
			
			if(Integer.valueOf(dayFormat.format(today)) < FirstBatchDay)
				c.set(c.MONTH, (Integer.valueOf(monthFormat.format(today))-2));
			//	c.add(Calendar.DATE, secondBatchInterval);
			//	dt = c.getTime();
			//	secondBatchDate = df.format(c.getTime());
				
			//} else if (Integer.valueOf(dayFormat.format(today)) > FirstBatchDay) {
				c.add(c.DATE, secondBatchInterval);
				dt = c.getTime();
				secondBatchDate = df.format(c.getTime());	
			//}
			
			logger.log(Level.INFO,"2nd Run: " + secondBatchDate);
			
			c.add(Calendar.DATE, Integer.valueOf(props.getDeleteDateInterval()));
			deleteBatchDate = df.format(c.getTime());
			logger.log(Level.INFO,"Delete Run: " + deleteBatchDate);
			
			System.out.println(deleteBatchDate != null);			//must be true
			System.out.println(deleteBatchDate.equals(dateToday));	//must be true
			
			if (deleteBatchDate != null && deleteBatchDate.equals(dateToday)) {
				logger.log(Level.INFO, "deleteBatchDate is equal to Date Today");
				
				//initialize delete file next method
				deleteFiles(deleteBatchDate, bankProfile);
			}
			
			if (errorLog) {
				
				//AuditLogs
				AuditTrail trail = new AuditTrail("Auto Upload", AuditTrail.ACTION_DELETE);
				trail.setDetails(errorMsg);
				trail.setAgencyGroupId(admin);
				trail.setCdate(today);
				trail.setCtime(today);
				trail.setCuser(AUTO_UPLOAD);
				
				service.getFacade().logAuditTrail(trail);
			}
			
			logger.log(Level.INFO, "--- Delete schedule task completed ---");
			
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteFiles(String triggerDate,String bankProfile) throws ParseException {
		logger.log(Level.INFO, "Initialize deleteFiles");
		
		try {
			
			rootDirectory = service.getFacade().getEntityManager()
					.createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", SysConfiguration.ROOT_FOLDER_DIRECTORY).getSingleResult();
			logger.log(Level.INFO, "rootDirectory: " + rootDirectory);
			
			File rootdir = new File(rootDirectory);
			logger.log(Level.INFO, "root Dir: " + rootdir.getAbsolutePath());
			
			admin = service.getFacade().getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyGroupShortnameAdmin", RefAgencyGroup.class)
					.setParameter("agencyGroupShortname", props.getAgencyGroupShortNameAdmin()).getSingleResult();
			
			logger.log(Level.INFO, "Admin: " + admin.getAgencyGroupName() + " - " + admin.getAgencyGroupShortname());
			logger.log(Level.INFO, "Trigger Date: " + triggerDate);
			
			System.out.println(bankProfile.substring(bankProfile.indexOf('_') +1, bankProfile.length()));
			
			listForDelete = service.getFacade().getEntityManager()
					.createNamedQuery("RefFileProperty.findByLessCdate", RefFileProperty.class)
					.setParameter("cdate", today)
					.setParameter("fileName", "%"+bankProfile.substring(bankProfile.indexOf('_') +1, bankProfile.length())+"%").getResultList();
			
			logger.log(Level.INFO, "Number of files to be deleted " + listForDelete.size());
			
			if(listForDelete.size() > 0) {
				for(int i=0; i < listForDelete.size(); i++) {
					logger.log(Level.SEVERE, "For Delete Property id: " + listForDelete.get(i).getFilePropertyId());
					
					if (listForDelete.get(i).getOwnerGroupId().getAgencyGroupId().equals(admin.getAgencyGroupId())) {
						
						String bnk;
						String bnkProf = bankProfile.substring(bankProfile.indexOf("_")+1, bankProfile.length()); //UKB, TB, RCB
						String[] test = listForDelete.get(i).getRelativeLocation().split("_");
						bnk = test[test.length-1];
						String newBnk = bnk.substring(0, bnk.indexOf("."));
						logger.log(Level.SEVERE, "newBnk: " + newBnk + " : " + bnkProf);
						if(newBnk.equals(bnkProf)) {
						if (listForDelete.get(i).getFileName().contains("xlsx") || listForDelete.get(i).getFileName().contains("xls")) {
							logger.log(Level.INFO, "File Folder Path For: " + rootdir.getAbsolutePath().replace("\\", "/") 
									+ listForDelete.get(i).getRelativeLocation());
							
							targetFolder = rootdir.getAbsolutePath().replace("\\", "/") + listForDelete.get(i).getRelativeLocation().replace("\\", "/");
							path = listForDelete.get(i).getRelativeLocation();
							
							String relativeLocHolder = path;
							String fileNameHolder = listForDelete.get(i).getFileName();
							
							logger.log(Level.INFO, "fileNameHolder: " + fileNameHolder);
							logger.log(Level.INFO, "path: " + path);
							
							logger.log(Level.INFO, "targetFolder: " + targetFolder);
							logger.log(Level.INFO, "relativeLocHolder: " + relativeLocHolder);
							
							delete(fileNameHolder, path, rootdir.getAbsolutePath().replace("\\", "/"));
						}
					}
						
				} else {
					errorMsg = "Auto Upload: Agency group invalid.";
					
					logger.log(Level.INFO, errorMsg);
					
					errorLog = true;
						
				}
			}
				
		} else {
			logger.log(Level.SEVERE, "");
		}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
}
	
	public void delete(String fileNameHolder,String tFolder,String absolutePath) {
		logger.log(Level.INFO, "Initialize Final Delete");
		
		logger.log(Level.INFO, "relativeLoc: " + tFolder);
		
		
		List<RefFileProperty> forDelete = null;
		
		//For Folders/subFolders
		String fileFolderPath = "";
		String fileFolderPath2 = "";
		String fileFolderPath3 = "";
		List<RefFileProperty> forDeleteFolders;
		
		try {
			
			forDelete = service.getFacade().getEntityManager()
					.createNamedQuery("RefFileProperty.findByRelativeLocationAndFileName", RefFileProperty.class)
					.setParameter("relativeLocation", tFolder)
					.setParameter("fileName", fileNameHolder).getResultList();
			
					for(RefFileProperty refFileProperty : forDelete) {
						
						if (refFileProperty.getCuser().equalsIgnoreCase(AUTO_UPLOAD)) {
							logger.log(Level.INFO, "File selected for deletion: " + refFileProperty.getFileName());
							logger.log(Level.INFO, "Deleting files...");
							
							//get folder parts
							//path sample: /FOLDER 15/Folder 1/Folder 1/Folder 1
							try {

								String[] tFolderSplit = path.split("/");
								String fileFolderRoot = tFolderSplit[1];
								String fileFolderRoot2 = tFolderSplit[2];
								String fileFolderRoot3 = tFolderSplit[3];
								String fileFolderRoot4 = tFolderSplit[4];
								
								fileFolderPath = ("/" + fileFolderRoot + "/" + fileFolderRoot2 + "/" + fileFolderRoot3
										+ "/" + fileFolderRoot4);
								System.out.println("File Path: " + fileFolderPath);
								
								fileFolderPath2 = ("/" + fileFolderRoot + "/" + fileFolderRoot2 + "/" + fileFolderRoot3);
								System.out.println("File Path: " + fileFolderPath2);
								
								fileFolderPath3 = ("/" + fileFolderRoot + "/" + fileFolderRoot2);
								System.out.println("File Path: " + fileFolderPath3);
								
								forDeleteFolders = service.getFacade().getEntityManager()
										.createNamedQuery("RefFileProperty.findByRelativeLocationAutoUpload", RefFileProperty.class)
										.setParameter("relativeLocation", ("%"+fileFolderPath+"%")).getResultList();
								
								logger.log(Level.INFO, "Number of folders/files to be deleted " + forDeleteFolders.size());
								
								for (RefFileProperty refFile : forDeleteFolders) {
									
									service.deleteAutoUpload(refFile, AUTO_UPLOAD);
									forceDeleteFiles(absolutePath+fileFolderPath);

								}
								
								//Remaining folder - delete root folder
								java.io.File root = new java.io.File(absolutePath + fileFolderPath2);
								
								if(root.isDirectory() && root.list().length == 0){
									logger.log(Level.INFO,"LAST CHECKING: Directory is empty");
									System.out.println(root.isDirectory());
									
									forDeleteFolders = service.getFacade().getEntityManager()
											.createNamedQuery("RefFileProperty.findByRelativeLocationAutoUpload", RefFileProperty.class)
											.setParameter("relativeLocation", ("%"+fileFolderPath3+"%")).getResultList();
									
									logger.log(Level.INFO, "LAST PART: Number of folders/files to be deleted " + forDeleteFolders.size());
									
									for (RefFileProperty refFile1 : forDeleteFolders) {
										
										service.deleteAutoUpload(refFile1, AUTO_UPLOAD);
										forceDeleteFiles(absolutePath+fileFolderPath3);
									}
									
								} else {
									logger.log(Level.INFO,"LAST CHECKING: Directory is not empty");
									System.out.println(root.isDirectory());
								}


							} catch (Exception e) {
								
								logger.log(Level.SEVERE, 
										"Failed to delete. " + e.getMessage());
								e.printStackTrace();
								
								logger.log(Level.SEVERE,
										"Failed to delete file data: " + refFileProperty.getFileName());
							}
						}
						
					}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Error in deletion of file" + e.getMessage());
		}
		
	}
	

	private void forceDeleteFiles(String tFolder) {
		logger.log(Level.INFO, "target Folder for delete :" + tFolder);
		File fTarget = new File(tFolder);
		logger.log(Level.INFO, "fTarget.exists() :"+fTarget.exists());
		if (fTarget.exists()) {
			try {
				FileUtils.forceDelete(fTarget);
			} catch (IOException E) {
				E.printStackTrace();
			}
		}

	}
	
	public int returnLastNumber(List<RefFileProperty> checkRefFileProperty,String relLoc){
		int lastNum =0;
		int temp =0;
		for(RefFileProperty refFile : checkRefFileProperty)
		{
			String number = refFile.getRelativeLocation().substring(relLoc.length()+1,refFile.getRelativeLocation().length());
	
			System.out.println("STRING number : "+number);
    		lastNum = Integer.valueOf(number);
    		
    		if (temp < lastNum) {
    			temp =lastNum;
    		}
		}
	System.out.println("last number : "+temp);
		return temp;
		
	}
	
	public int returnLastNumbers(RefFileProperty reffile,String relLoc){
		int lastNum =0;
		int temp =0;
//		for(RefFileProperty refFile : checkRefFileProperty)
//		{
			String number = reffile.getRelativeLocation().substring(relLoc.length()+1,reffile.getRelativeLocation().length());
			System.out.println("STRING number : "+number);
    		lastNum = Integer.valueOf(number);
    		
    		if (temp < lastNum) {
    			temp =lastNum;
    		}
//		}
	System.out.println("last number : "+temp);
		return temp;
		
	}
	
	public String getStartDate(int fBatchDate, int secondBatchInterval) throws ParseException, NullPointerException {

		int stbatchday = fBatchDate;
		int scndinterval = secondBatchInterval;

		String firstBatchDate = null;
		String secondBatchDate = null;
		String firstBatchDatePrev = null;
		String secondBatchDatePrev = null;
		String startDate = null;

		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

		//String sDate = "02-04-2023";
		//Date today = new SimpleDateFormat("MM-dd-yyyy").parse(sDate);
		Date today = Calendar.getInstance().getTime();
		Date dt = null;

		String dateToday = df.format(today);
		System.out.println("Today is: " + dateToday); // date to use

		// First Batch
		Calendar c = (Calendar) Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, stbatchday);
		firstBatchDate = df.format(c.getTime());
		System.out.println("First batch run: " + firstBatchDate);

		// Frist batch of previous month
		Calendar cprev1 = (Calendar) Calendar.getInstance();
		cprev1.set(Calendar.DAY_OF_MONTH, stbatchday);
		cprev1.add(Calendar.MONTH, -1);
		firstBatchDatePrev = df.format(cprev1.getTime());
		System.out.println("Frist batch of previous month batch: " + firstBatchDatePrev);

		// Second Batch
		Calendar c2 = (Calendar) Calendar.getInstance();
		c2.set(Calendar.DAY_OF_MONTH, stbatchday + scndinterval);
		secondBatchDate = df.format(c2.getTime());
		System.out.println("Second batch run: " + secondBatchDate);

		// Frist batch of previous month
		Calendar cprev2 = (Calendar) Calendar.getInstance();
		cprev2.add(Calendar.MONTH, -1);
		cprev2.set(Calendar.DAY_OF_MONTH, stbatchday + scndinterval);

		secondBatchDatePrev = df.format(cprev2.getTime());
		System.out.println("Second batch of previous month batch: " + secondBatchDatePrev);

		//System.out.println("compare");
		//System.out.println(dateToday);
		//System.out.println(firstBatchDate);
		//System.out.println("-----");
		//System.out.println(" ");

		//System.out.println("compare2");
		//System.out.println(dateToday);
		//System.out.println(secondBatchDatePrev);
		//System.out.println("-----");
		//System.out.println(" ");

		
		// First Batch run
		if (dateToday.equals(firstBatchDate) &&
				Integer.valueOf(dayFormat.format(today)) <= stbatchday){
			
			startDate = secondBatchDatePrev;
			
			Date dateExceed = new SimpleDateFormat("MM-dd-yyyy").parse(startDate);
			
			if (today.compareTo(dateExceed) < 0) {
				
				startDate = firstBatchDatePrev;
				
				System.out.println("RCB scenerio condition");
			}
			
			System.out.println("1st condition result: " + startDate);
			
		// SecondBatch of Previous Run	
		} else if (dateToday.equals(secondBatchDatePrev) &&
				Integer.valueOf(dayFormat.format(today)) <= stbatchday) {
			
			startDate = firstBatchDatePrev;

			System.out.println("2nd condition result: " + startDate);
			
		// 
		} else if (dateToday.equals(secondBatchDatePrev) &&
				Integer.valueOf(dayFormat.format(today)) > stbatchday) {
			
			startDate = firstBatchDatePrev;
			
			System.out.println("3rd condition result: " + startDate);
			
		} else if (dateToday.equals(secondBatchDate) &&
				Integer.valueOf(dayFormat.format(today)) > stbatchday) {
			
			startDate = firstBatchDate;
			
			System.out.println("4th condition result: " + startDate);
			
		}
		
		System.out.println("startDate to use: " + startDate);

		return startDate;
	}

	
	public String getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public RefFileProperty getRefFileProp() {
		return refFileProp;
	}

	public void setRefFileProp(RefFileProperty refFileProp) {
		this.refFileProp = refFileProp;
	}

	public String getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(String sourceFiles) {
		this.sourceFiles = sourceFiles;
	}

	public String getTargetFolder() {
		return targetFolder;
	}

	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}

	public String getRelativeLocation() {
		return relativeLocation;
	}

	public void setRelativeLocation(String relativeLocation) {
		this.relativeLocation = relativeLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	

}
