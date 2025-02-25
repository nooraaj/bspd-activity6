package com.bsp.fsccis.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.bsp.fsccis.entity.tag.Custom_GetMax;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;
import com.bsp.fsccis.entity.tag.WhereTag;

public class GetNextIdUtil {
	private static final Logger LOGGER = Logger.getLogger(GetNextIdUtil.class.getSimpleName());

	public static Number getNextId(PKTag_GetMax<?> newEntity,EntityManager em) {
		Number nextId = null;
		try {
			StringBuilder query = new StringBuilder();
			if (newEntity instanceof Custom_GetMax) {
				Custom_GetMax custom = (Custom_GetMax) newEntity;
				query.append(" SELECT MAX(").append(custom.getMaxString())
						.append(") + 1 ");
				query.append(" FROM \"FSCC-IS\".\"")
						.append(newEntity.getTableName()).append("\" ");
			} else {
				query.append("SELECT MAX(\"").append(
						newEntity.getPKColumnName());
				query.append("\") + 1 FROM \"FSCC-IS\".\"")
						.append(newEntity.getTableName()).append("\"");
			}

			if (newEntity instanceof WhereTag) {
				WhereTag where = (WhereTag) newEntity;
				query.append(" WHERE ").append(where.getWhereClause());
			}

			LOGGER.info("getNextId(): " + query.toString());

			nextId = (Number) em.createNativeQuery(
					query.toString()).getSingleResult();

			if (nextId == null) {
				nextId = newEntity.getDefaultId();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Error:" + e);
		}

		return nextId;
	}
}
