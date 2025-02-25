package com.bsp.fsccis.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import ph.gov.bsp.utils.av.ICAPException;
import ph.gov.bsp.utils.av.ICAPModel;
import ph.gov.bsp.utils.av.ICAPServiceUtil;


import com.bsp.fsccis.facade.GenericFacade;
import com.bsp.fsccis.util.FSCCISServiceLocator;

@Stateless
public class AVService {
	private static final Logger LOGGER = Logger.getLogger(AVService.class.getSimpleName());

	@EJB
	GenericFacade facade;

	public void scan(FileInputStream fis) throws FileNotFoundException, IOException, ICAPException {
		LOGGER.info("scan");
		ICAPModel icapModel = null;
		try {
			LOGGER.info("Retrieve av.scan.address...");
			String ipAddress = facade.getEntityManager()
					.createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", "av.scan.address").getSingleResult();
			LOGGER.info("Retrieve port...");
			String port = facade.getEntityManager().createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", "av.scan.port1").getSingleResult();
			LOGGER.info("Initialize ICAP...");
			icapModel = new ICAPModel();
			LOGGER.info("Setting parameters...");
			icapModel.setIpAddress(ipAddress);
			icapModel.setPort(Integer.parseInt(port));
			icapModel.setService("avscan");
			LOGGER.info("IP: " + ipAddress);
			LOGGER.info("PORT: " + port);
		} catch (Exception e) {
			LOGGER.severe("ERROR: " + e);
		}

		ICAPServiceUtil.scan(icapModel, fis);
	}

	public void scan(File file) throws FileNotFoundException, IOException, ICAPException {
		LOGGER.info("scan");
		ICAPModel icapModel = null;
		try {
			LOGGER.info("Retrieve av.scan.address...");
			String ipAddress = facade.getEntityManager()
					.createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", "av.scan.address").getSingleResult();
			LOGGER.info("Retrieve port...");
			String port = facade.getEntityManager().createNamedQuery("SysConfiguration.findValueByName", String.class)
					.setParameter("name", "av.scan.port1").getSingleResult();
			LOGGER.info("Initialize ICAP...");
			icapModel = new ICAPModel();
			LOGGER.info("Setting parameters...");
			icapModel.setIpAddress(ipAddress);
			icapModel.setPort(Integer.parseInt(port));
			icapModel.setService("avscan");
			LOGGER.info("IP: " + ipAddress);
			LOGGER.info("PORT: " + port);
		} catch (Exception e) {
			LOGGER.severe("ERROR: " + e);
		}

		ICAPServiceUtil.scan(icapModel, new FileInputStream(file));

	}
}
