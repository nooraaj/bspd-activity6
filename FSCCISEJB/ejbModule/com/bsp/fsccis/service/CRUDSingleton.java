package com.bsp.fsccis.service;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;
import com.bsp.fsccis.util.ClassBean;
import com.bsp.fsccis.util.GetNextIdUtil;

@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Singleton
public class CRUDSingleton { 
	private static final Logger LOGGER = Logger.getLogger(CRUDSingleton.class.getSimpleName());

	@PersistenceContext(unitName = "FSCCISEJB_PU")
	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Lock(LockType.WRITE)
	public void create(Object entity){
		LOGGER.info("create()");
		if (entity instanceof PKTag_GetMax) {
			PKTag_GetMax pk = (PKTag_GetMax) entity;
			pk.setIdToGenerate(GetNextIdUtil.getNextId(pk, em));
		}

		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable bts = (BSPTimeStampable) entity;
			Date date = new Date();
			bts.setCdate(date);
			bts.setCtime(date);
		}

		getEntityManager().persist(entity);
		getEntityManager().flush();
		getEntityManager().refresh(entity);
		
		
		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_ADD);
		
		if (entity instanceof DisplayTag) {
			DisplayTag d = (DisplayTag) entity;
			trail.setDetails(d.getDisplayName());
		}
		
		RefAgencyGroup agency = getEntityManager().createNamedQuery("RefUserAccounts.findRefAgencyByUserName", RefAgencyGroup.class).setParameter("userName", ((BSPTimeStampable) entity).getCuser()).getSingleResult();
		trail.setAgencyGroupId(agency);
		
		trail.setCdate(((BSPTimeStampable) entity).getCdate());
		trail.setCtime(((BSPTimeStampable) entity).getCtime());
		trail.setCuser(((BSPTimeStampable) entity).getCuser());

		logAuditTrail(trail);
	}
	
	public synchronized void logAuditTrail(AuditTrail trail) {
		try {
			getEntityManager().persist(trail);
			getEntityManager().flush();
			getEntityManager().refresh(trail);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE,"ERROR: " + e);
			LOGGER.info("LOGGING AUDIT TRAIL FAILED FOR: ("
					+ trail.getAuditTrailId() + ") " + trail.getDetails());
		}
	}
}
