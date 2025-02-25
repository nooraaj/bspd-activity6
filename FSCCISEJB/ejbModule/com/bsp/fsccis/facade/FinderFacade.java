package com.bsp.fsccis.facade;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class FinderFacade{

	@PersistenceContext(unitName = "FSCCISEJB_PU")
	private EntityManager em;

	public static final Logger LOGGER = Logger.getLogger(FinderFacade.class
			.getSimpleName());

	public <T> T find(Object id, Class<T> entityClass) {
		return em.find(entityClass, id);
	}

}
