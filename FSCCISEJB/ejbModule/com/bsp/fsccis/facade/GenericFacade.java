package com.bsp.fsccis.facade;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.Custom_GetMax;
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;
import com.bsp.fsccis.entity.tag.WhereTag;
import com.bsp.fsccis.util.AuditTrailUtil;
import com.bsp.fsccis.util.ClassBean;
import com.bsp.fsccis.util.FSCCISServiceLocator;
import com.bsp.fsccis.util.GetNextIdUtil;

@Stateless
public class GenericFacade {

	@PersistenceContext(unitName = "FSCCISEJB_PU")
	private EntityManager em;

	public static final Logger LOGGER = Logger.getLogger(GenericFacade.class
			.getSimpleName());

	public static final String AUTO_UPLOAD = "auto_upload";
	
	public EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void create(Object entity) {

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void createAutoUpload(Object entity, RefAgencyGroup refAgencyGroup,String folderName) {

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
		
		trail.setAgencyGroupId(refAgencyGroup);
		
		if (entity instanceof DisplayTag) {
		DisplayTag d = (DisplayTag) entity;
			trail.setDetails(d.getDisplayName());
		} else {
			
			RefFileProperty refFileProp = (RefFileProperty) entity;
			trail.setDetails(refFileProp.getFileName() + " " + folderName);
		}

		RefAgencyGroup agency = getEntityManager()
				.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
				.setParameter("agencyGroupId", 1)
				.getSingleResult();
		
		trail.setAgencyGroupId(agency);
		
		trail.setCdate(((BSPTimeStampable) entity).getCdate());
		trail.setCtime(((BSPTimeStampable) entity).getCtime());
		trail.setCuser(refAgencyGroup.getCuser());

		logAuditTrail(trail);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void batchCreate(List<Object> list) {
		Date date = new Date();
		LOGGER.info("batchCreate()");
		for (Object entity : list) {
			if (entity instanceof PKTag_GetMax) {
				PKTag_GetMax pk = (PKTag_GetMax) entity;
				pk.setIdToGenerate(GetNextIdUtil.getNextId(pk, em));
			}

			if (entity instanceof BSPTimeStampable) {
				BSPTimeStampable bts = (BSPTimeStampable) entity;
				bts.setCdate(date);
				bts.setCtime(date);
			}

			getEntityManager().persist(entity);
		}

		getEntityManager().flush();
		getEntityManager().clear();
	}

	public void edit(Object entity) {

		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable bts = (BSPTimeStampable) entity;
			Date date = new Date();
			bts.setCdate(date);
			bts.setCtime(date);
		}

		String diff = AuditTrailUtil.getInstance().getDifference(entity);
		getEntityManager().merge(entity);
		LOGGER.info("Diff: " + diff);

		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_EDIT);
		trail.setDetails(diff);
		
		RefAgencyGroup agency = getEntityManager().createNamedQuery("RefUserAccounts.findRefAgencyByUserName", RefAgencyGroup.class).setParameter("userName", ((BSPTimeStampable) entity).getCuser()).getSingleResult();
		trail.setAgencyGroupId(agency);
		
		trail.setCdate(((BSPTimeStampable) entity).getCdate());
		trail.setCtime(((BSPTimeStampable) entity).getCtime());
		trail.setCuser(((BSPTimeStampable) entity).getCuser());
		
		logAuditTrail(trail);
	}

	public void edit(Object entity, String auditTrailComment) {
		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable bts = (BSPTimeStampable) entity;
			Date date = new Date();
			bts.setCdate(date);
			bts.setCtime(date);
		}

		String diff = AuditTrailUtil.getInstance().getDifference(entity);
		getEntityManager().merge(entity);
		LOGGER.info("Diff: " + diff);

		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_EDIT);
		trail.setDetails(diff);
		
		RefAgencyGroup agency = getEntityManager().createNamedQuery("RefUserAccounts.findRefAgencyByUserName", RefAgencyGroup.class).setParameter("userName", ((BSPTimeStampable) entity).getCuser()).getSingleResult();
		trail.setAgencyGroupId(agency);
		
		trail.setCdate(((BSPTimeStampable) entity).getCdate());
		trail.setCtime(((BSPTimeStampable) entity).getCtime());
		trail.setCuser(((BSPTimeStampable) entity).getCuser());

		logAuditTrail(trail);
	}
	
	public void editAutoUpload(Object entity) {

		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable bts = (BSPTimeStampable) entity;
			Date date = new Date();
			bts.setCdate(date);
			bts.setCtime(date);
		}

		String diff = AuditTrailUtil.getInstance().getDifference(entity);
		getEntityManager().merge(entity);
		LOGGER.info("Diff: " + diff);

		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_EDIT);
		trail.setDetails(diff);
		
		//RefAgencyGroup agency = getEntityManager()
		//		.createNamedQuery("RefUserAccounts.findRefAgencyByUserName", RefAgencyGroup.class)
		//		.setParameter("userName", ((BSPTimeStampable) entity).getCuser())
		//		.getSingleResult();
		
		RefAgencyGroup agency = getEntityManager()
				.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
				.setParameter("agencyGroupId", 1)
				.getSingleResult();
		
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
	
	public void logAuditTrail(String table, int action, String details, RefAgencyGroup agencyId, String cuser) {
		AuditTrail trail = null;
		try{
			trail = new AuditTrail(table, action);
			trail.setDetails(details);
			trail.setAgencyGroupId(agencyId);
			trail.setCdate(new Date());
			trail.setCtime(new Date());
			trail.setCuser(cuser);
			
			logAuditTrail(trail);
		}catch(Exception e){
			LOGGER.info("FAILED TO LOG: " + trail.getDisplayName());
		}
	}

	public void detach(Object entity) {
		getEntityManager().detach(entity);
	}
	
	@Resource SessionContext ctx;

	public void remove(Object entity) {
		getEntityManager().remove(getEntityManager().merge(entity));
		
		//TODO: remove whole block of audit trail. make calling ejb log specific trail
		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_DELETE);
		
		if (entity instanceof DisplayTag) {
			DisplayTag disp = (DisplayTag) entity;
			trail.setDetails(disp.getDisplayName());
		}

		
		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable timestamp = (BSPTimeStampable) entity;

			trail.setCdate(timestamp.getCdate());
			trail.setCtime(timestamp.getCtime());
			trail.setCuser(ctx.getCallerPrincipal().getName());
			
			RefAgencyGroup agency = new RefAgencyGroup();
			System.out.println(" " + ((BSPTimeStampable) entity).getCuser());
			
			
			if (((BSPTimeStampable) entity).getCuser().equalsIgnoreCase(AUTO_UPLOAD)) {
				
				agency = getEntityManager()
						.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
						.setParameter("agencyGroupId", 1)
						.getSingleResult();
				
			} else {
				
				agency = getEntityManager().createNamedQuery("RefUserAccounts.findRefAgencyByUserName", RefAgencyGroup.class).setParameter("userName", ((BSPTimeStampable) entity).getCuser()).getSingleResult();
				
			}
			trail.setAgencyGroupId(agency);
		}
		
		logAuditTrail(trail);
		//remove util here
		
	}
	public void removeUploaded(Object entity, String folderName) {
		
		getEntityManager().remove(getEntityManager().merge(entity));
		
		//TODO: remove whole block of audit trail. make calling ejb log specific trail
		AuditTrail trail = new AuditTrail(ClassBean.getInstance()
				.getClassMap().get(entity.getClass().getSimpleName())
				.getDisplayName(), AuditTrail.ACTION_DELETE);
		
		if (entity instanceof DisplayTag) {
			DisplayTag disp = (DisplayTag) entity;
			trail.setDetails(disp.getDisplayName());
		} else {
			
			RefFileProperty refFileProp = (RefFileProperty) entity;
			trail.setDetails(refFileProp.getFileName() + " " + folderName);
		}
		
		if (entity instanceof BSPTimeStampable) {
			BSPTimeStampable timestamp = (BSPTimeStampable) entity;

			trail.setCdate(timestamp.getCdate());
			trail.setCtime(timestamp.getCtime());
			trail.setCuser("system");
			
			RefAgencyGroup agency = getEntityManager()
					.createNamedQuery("RefAgencyGroup.findByAgencyGroupId", RefAgencyGroup.class)
					.setParameter("agencyGroupId", 1)
					.getSingleResult();
			
			trail.setAgencyGroupId(agency);
		}
		
		logAuditTrail(trail);
		//remove util here
		
	}
	
	public <T> T find(Object id, Class<T> entityClass) {
		return getEntityManager().find(entityClass, id);
	}
	
	public <T> T findById(int id, Class<T> entityClass) {
		return getEntityManager().find(entityClass, id);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> List<T> findAll(Class<T> entityClass) {
		javax.persistence.criteria.CriteriaQuery cq = getEntityManager()
				.getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		return getEntityManager().createQuery(cq).getResultList();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> List<T> findRange(int[] range, Class<T> entityClass) {
		javax.persistence.criteria.CriteriaQuery cq = getEntityManager()
				.getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		javax.persistence.Query q = getEntityManager().createQuery(cq);
		q.setMaxResults(range[1] - range[0]);
		q.setFirstResult(range[0]);
		return q.getResultList();
	}
}
