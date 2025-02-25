package com.bsp.frprr.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;


import com.bsp.frprr.entity.FRPFilePath;
import com.bsp.fsccis.facade.GenericFacade;

@Stateless
public class FRPFileService {
	private static final Logger LOGGER = Logger.getLogger(FRPFileService.class
			.getSimpleName());

	@EJB
	GenericFacade facade;
	private String defaultPassword;
	public GenericFacade getFacade() {
		return this.facade;
	}
	
	public List<FRPFilePath> getFRPFiles(){
		LOGGER.info("getFRPFiles");
		List<FRPFilePath> result = new ArrayList<FRPFilePath>(
				facade.findAll(FRPFilePath.class));

		for (FRPFilePath frprr : result) {
			frprr.getFilePath().replace("\\", "/");
			LOGGER.info("Filepath: " + frprr.getFilePath().replace("\\", "/") );
		}

		return result;
		
	}
	
	public List<FRPFilePath> findPath(String bankProfile, String startDate, String endDate) throws ParseException {
		LOGGER.info("getFRPFiles: findPath");

		//System.out.println("Modified Date Scope Test: ");
		//startDate = "12-18-2019";
		//endDate = "01-19-2021";

		SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");

		Date date1 = format1.parse(startDate);
		Date date2 = format1.parse(endDate);

		startDate = format2.format(date1);
		endDate = format2.format(date2);

		System.out.println(startDate);
		System.out.println(endDate);

		List<FRPFilePath> result = new ArrayList<FRPFilePath>();

		result = facade.getEntityManager().createNamedQuery("FPRRTransFile.findPath", FRPFilePath.class)
				.setParameter(1, "%" + bankProfile.substring(7) + "%")
				.setParameter(2, startDate)
				.setParameter(3, endDate).getResultList();

		LOGGER.info("getFRPFiles size: " + result.size());

		return result;

	}
	
}
