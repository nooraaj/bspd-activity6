package com.bsp.fsccis.properties;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;

import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.service.ReportService;
import com.bsp.fsccis.service.ScheduleService;
import com.tivoli.pd.jasn1.boolean32;

public class AutoUploadProperties {
	
	@EJB
	private GenericFacade facade;

	@EJB
	private ReportService service;
	
	private static Logger logger = Logger.getLogger(ScheduleService.class.getName());


	
	private String firstBatchDate;
	private boolean runStatus = false;
	private String secondBatchDateInterval;
	private String dateFormat;
	
	private String deleteDateInterval;
	
	private String agencyGroupShortNameAdmin;
	private String agencyGroupShortNameViewer;
	
	private String errMsg = null;
	private boolean errLogMsg = false;
	
	
	private String folderUKB;
	
	private String viewer1;
	private String viewer2;
	
	public GenericFacade getFacade() {
		return this.facade;
	}
	
	public void initializeAutoUploadProperties(String propName) throws ParseException, NullPointerException {
	
		Properties properties = new Properties();
		
		try {
			logger.log(Level.INFO, "--- Properties file reading ---");
			InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("fsf-eis.properties");
			
			properties.load(stream);
			logger.log(Level.INFO, "propName: "+propName);
			dateFormat = properties.getProperty("sched.date.format");
			
			if (propName.equals("Upload_UKB")) {
				firstBatchDate = properties.getProperty("first.batch.date.ukb");
				secondBatchDateInterval = properties.getProperty("second.batch.date.ukb.interval");
				agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
				agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
				viewer1 = properties.getProperty("agency.viewer1");
				viewer2 = properties.getProperty("agency.viewer2");
				
				checkProperties("forUpload");
				
				logger.log(Level.INFO, "dateFormat Properties: " + dateFormat);
				logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
				logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
				logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
				logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
				
			}   else if (propName.equals("Upload_TB")) {
				logger.log(Level.INFO, "---Delete properties---");
				firstBatchDate = properties.getProperty("first.batch.date.tb");
				secondBatchDateInterval = properties.getProperty("second.batch.date.tb.interval");
				agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
				agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
				viewer1 = properties.getProperty("agency.viewer1");
				viewer2 = properties.getProperty("agency.viewer2");
				
				checkProperties("forUpload");
				
				logger.log(Level.INFO, "dateFormat Properties: " + dateFormat);
				logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
				logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
				logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
				logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
				
			}
				else if (propName.equals("Upload_RCB")) {
					logger.log(Level.INFO, "---Delete properties---");
					
					firstBatchDate = properties.getProperty("first.batch.date.rcb");
					secondBatchDateInterval = properties.getProperty("second.batch.date.rcb.interval");
					agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
					agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
					viewer1 = properties.getProperty("agency.viewer1");
					viewer2 = properties.getProperty("agency.viewer2");
					
					checkProperties("forUpload");
					
					logger.log(Level.INFO, "dateFormat Properties: " + dateFormat);
					logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
					logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
					logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
					logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
			}
			else if (propName.equals("Delete_UKB")) {
				logger.log(Level.INFO, "--- Delete properties ---");
				
				firstBatchDate = properties.getProperty("first.batch.date.ukb");
				secondBatchDateInterval = properties.getProperty("second.batch.date.ukb.interval");
				deleteDateInterval = properties.getProperty("delete.batch.date.ukb.interval");
				agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
				agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
				viewer1 = properties.getProperty("agency.viewer1");
				viewer2 = properties.getProperty("agency.viewer2");
				
//				folderUKB = properties.getProperty("folder.name.ukb");
//				logger.log(Level.INFO, "folderUKB Properties: " + folderUKB);
				checkProperties("forDelete");
				logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
				logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
				logger.log(Level.INFO, "deleteDateInterval Properties: " + deleteDateInterval);
				logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
				logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
				
			} 
			else if (propName.equals("Delete_TB")) {
				logger.log(Level.INFO, "---Delete properties---");
				
				firstBatchDate = properties.getProperty("first.batch.date.tb");
				secondBatchDateInterval = properties.getProperty("second.batch.date.tb.interval");
				deleteDateInterval = properties.getProperty("delete.batch.date.tb.interval");
				agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
				agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
				viewer1 = properties.getProperty("agency.viewer1");
				viewer2 = properties.getProperty("agency.viewer2");
				
				checkProperties("forDelete");
				logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
				logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
				logger.log(Level.INFO, "deleteDateInterval Properties: " + deleteDateInterval);
				logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
				logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
				
			} 
			else if (propName.equals("Delete_RCB")) {
				logger.log(Level.INFO, "---Delete properties---");
				
				firstBatchDate = properties.getProperty("first.batch.date.rcb");
				secondBatchDateInterval = properties.getProperty("second.batch.date.rcb.interval");
				deleteDateInterval = properties.getProperty("delete.batch.date.rcb.interval");
				agencyGroupShortNameAdmin = properties.getProperty("agency.group.shortname.admin");
				agencyGroupShortNameViewer = properties.getProperty("agency.group.shortname.viewer");
				viewer1 = properties.getProperty("agency.viewer1");
				viewer2 = properties.getProperty("agency.viewer2");
				
				checkProperties("forDelete");
				logger.log(Level.INFO, "firstBatchDate Properties: " + firstBatchDate);
				logger.log(Level.INFO, "secondBatchDateInterval Properties: " + secondBatchDateInterval);
				logger.log(Level.INFO, "deleteDateInterval Properties: " + deleteDateInterval);
				logger.log(Level.INFO, "agencyGroupShortNameAdmin Properties: " + agencyGroupShortNameAdmin);
				logger.log(Level.INFO, "agencyGroupShortNameViewer Properties: " + agencyGroupShortNameViewer);
				
			} 
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	public boolean initializeAutoUploadStatus(String propName) throws ParseException, NullPointerException {
		
		Properties properties = new Properties();
		
		try {
			logger.log(Level.INFO, "--- Properties file reading for status ---");
			InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("fsf-eis.properties");
			
			properties.load(stream);
			if (propName.equals("Upload_UKB")) {
				if(properties.getProperty("ukb.status").equals("active")){
					runStatus = true;
				} else {
					logger.log(Level.INFO, "---UKB is Inactive---");
				}
			}else if (propName.equals("Upload_TB")){
				if(properties.getProperty("tb.status").equals("active")){
					runStatus = true;
				} else {
					logger.log(Level.INFO, "---TB is Inactive---");
				}
			}else if (propName.equals("Upload_RCB")){
				if(properties.getProperty("rcb.status").equals("active")){
					runStatus = true;
				} else {
					logger.log(Level.INFO, "---RCB is Inactive---");
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return runStatus;
	}

	private void checkProperties(String process) {
		
		if (process.equals("forUpload")) {
			
			if (firstBatchDate == null) {
				
				errMsg = "Missing firstBatchDate in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} else if (secondBatchDateInterval == null) {
				
				errMsg = "Missing secondBatchDateInterval in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} 
			else if (agencyGroupShortNameAdmin == null) {
				
				errMsg = "Missing agencyGroupShortNameAdmin in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} else if (agencyGroupShortNameViewer == null) {
				
				errMsg = "Missing agencyGroupShortNameViewer in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
			}
			
		} else if (process.equals("forDelete")) {
				if (firstBatchDate == null) {
				
				errMsg = "Missing firstBatchDate in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} else if (secondBatchDateInterval == null) {
				
				errMsg = "Missing secondBatchDateInterval in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} 
			if (deleteDateInterval == null) {
				
				errMsg = "Missing deleteDateInterval in properties.";
				logger.log(Level.SEVERE, errMsg);
				
				errLogMsg = true;
				
			} else if (agencyGroupShortNameAdmin == null) {
				
				errMsg = "Missing agencyGroupShortNameAdmin in properties.";
				logger.log(Level.SEVERE, errMsg);
				errLogMsg = true;
			}
		}
		
	}

	public String getFirstBatchDate() {
		return firstBatchDate;
	}

	public void setFirstBatchDate(String firstBatchDate) {
		this.firstBatchDate = firstBatchDate;
	}

	public String getSecondBatchDateInterval() {
		return secondBatchDateInterval;
	}

	public void setSecondBatchDateInterval(String secondBatchDateInterval) {
		this.secondBatchDateInterval = secondBatchDateInterval;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getAgencyGroupShortNameAdmin() {
		return agencyGroupShortNameAdmin;
	}

	public void setAgencyGroupShortNameAdmin(String agencyGroupShortNameAdmin) {
		this.agencyGroupShortNameAdmin = agencyGroupShortNameAdmin;
	}

	public String getAgencyGroupShortNameViewer() {
		return agencyGroupShortNameViewer;
	}

	public void setAgencyGroupShortNameViewer(String agencyGroupShortNameViewer) {
		this.agencyGroupShortNameViewer = agencyGroupShortNameViewer;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public boolean isErrLogMsg() {
		return errLogMsg;
	}

	public void setErrLogMsg(boolean errLogMsg) {
		this.errLogMsg = errLogMsg;
	}

	public String getDeleteDateInterval() {
		return deleteDateInterval;
	}

	public void setDeleteDateInterval(String deleteDateInterval) {
		this.deleteDateInterval = deleteDateInterval;
	}
	
	
	public String getFolderUKB() {
		return folderUKB;
	}

	public void setFolderUKB(String folderUKB) {
		this.folderUKB = folderUKB;
	}

	public String getViewer1() {
		return viewer1;
	}

	public void setViewer1(String viewer1) {
		this.viewer1 = viewer1;
	}

	public String getViewer2() {
		return viewer2;
	}

	public void setViewer2(String viewer2) {
		this.viewer2 = viewer2;
	}
	
}
