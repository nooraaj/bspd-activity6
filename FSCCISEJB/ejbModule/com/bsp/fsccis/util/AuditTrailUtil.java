package com.bsp.fsccis.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;

import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.DisplayTag2;
import com.bsp.fsccis.facade.FinderFacade;
import com.bsp.fsccis.facade.GenericFacade;

public class AuditTrailUtil {

	private static final Logger LOGGER = Logger.getLogger(AuditTrailUtil.class
			.getSimpleName());

	private FinderFacade finder;
	private GenericFacade facade;

	private static AuditTrailUtil instance;

	private AuditTrailUtil() {
	}

	public static AuditTrailUtil getInstance() {
		return instance == null ? instance = new AuditTrailUtil() : instance;
	}
	
	public String getDifference(Object entity) {
		LOGGER.info("getDifference()" + entity.getClass());
		LOGGER.info("0");
		FSCCISServiceLocator locator = FSCCISServiceLocator.getInstance();
		LOGGER.info("0.1");
		finder = (FinderFacade) locator.locateFacade(FinderFacade.class);
		LOGGER.info("0.2");
		StringBuilder sb = new StringBuilder();
		if (entity instanceof DisplayTag2) {
			DisplayTag2 disp = (DisplayTag2) entity;
			sb.append(disp.getDisplayName()).append(" - ");
		}
		try {
			Object fromDb = null;

			Field[] fieldList = entity.getClass().getDeclaredFields();
			LOGGER.info("1");
			for (Field field : fieldList) {
				Id id = field.getAnnotation(Id.class);
				if (id != null) {
					Method oMethod = (new PropertyDescriptor(field.getName(),
							entity.getClass())).getReadMethod();
					Object idValue = oMethod.invoke(entity, new Object[] {});
					fromDb = finder.find(idValue, entity.getClass());
				} else {
					EmbeddedId embId = field.getAnnotation(EmbeddedId.class);
					if (embId != null) {
						Method oMethod = (new PropertyDescriptor(
								field.getName(), entity.getClass()))
								.getReadMethod();
						Object idValue = oMethod
								.invoke(entity, new Object[] {});
						fromDb = finder.find(idValue, entity.getClass());
					}
				}
			}
			LOGGER.info("2");
			if (fromDb != null) {
				LOGGER.info("3");
				for (Field field : fieldList) {
					LOGGER.info("4 " + fieldList);
					if (field.getName().toLowerCase().contains("cdate")
							|| field.getName().toLowerCase().contains("ctime")
							|| field.getName().toLowerCase().contains("cuser")) {
						continue;
					}
//					LOGGER.info("field.getName(): " + field.getName());
					Column column = field.getAnnotation(Column.class);
					Object o1 = null;
					Object o2 = null;
					if (column != null) {
						String name = (column.name() != null && !column.name()
								.isEmpty()) ? column.name().toUpperCase()
								: field.getName().toUpperCase();
//						LOGGER.info("name: " + name);
						
						Object methodSrc = entity != null ? entity:fromDb;
								
						Method oMethod = (new PropertyDescriptor(
										field.getName(), methodSrc.getClass()))
										.getReadMethod();
						o1 = oMethod.invoke(entity, new Object[] {});
						o2 = oMethod.invoke(fromDb, new Object[] {});
						print(sb, o1, o2, name);
					} else {
						JoinColumn jColumn = field
								.getAnnotation(JoinColumn.class);
						if (jColumn != null) {
							String name = (jColumn.name() != null && !jColumn
									.name().isEmpty()) ? jColumn.name()
									.toUpperCase() : field.getName()
									.toUpperCase();
									
							Object methodSrc = entity != null ? entity:fromDb;
							
							Method oMethod = (new PropertyDescriptor(
									field.getName(), methodSrc.getClass()))
									.getReadMethod();
							o1 = oMethod.invoke(entity, new Object[] {});
							o2 = oMethod.invoke(fromDb, new Object[] {});
							print(sb, o1, o2, name);
						}
						else{
							JoinColumns jColumns = field
									.getAnnotation(JoinColumns.class);
							if(jColumns != null){
								Field tempField = field;
								Object tempEntity = entity;
								Object tempFromDb = fromDb;
								
//								LOGGER.info("field: " + field.getName());
								List<JoinColumn> jColumnList = Arrays.asList(jColumns.value()); 
								
								int ctr= 0;
								Stack<Object> entityStack = new Stack<Object>();
								Stack<Object> fromDbStack = new Stack<Object>();
								
								while(!jColumnList.isEmpty()){
									if(ctr >= jColumnList.size()){
										break;
									}
									if(field != null){
//										LOGGER.info("Field to Retrieve: " + field.getName());
										if(entity != null){
//											LOGGER.info("Source Entity1:" + entity.getClass());
											Method oMethod = (new PropertyDescriptor(
													field.getName(), entity.getClass()))
													.getReadMethod();
											o1 = oMethod.invoke(entity, new Object[] {});
										}else{
											o1 = null;
										}
										
										if(fromDb != null){
//											LOGGER.info("Source Entity2:" + fromDb.getClass());
											Method oMethod = (new PropertyDescriptor(
													field.getName(), fromDb.getClass()))
													.getReadMethod();
											o2 = oMethod.invoke(fromDb, new Object[] {});
										}else{
											o2 = null;
										}
									}
									
//									LOGGER.info("entityPush: " + o1);
//									LOGGER.info("dbPush: " + o2);
									
									entityStack.push(o1);
									fromDbStack.push(o2);
									
									boolean fieldUpdatedInO1 = false;
									
									if(o1 != null){
										field = locateField(o1.getClass().getDeclaredFields(),jColumnList);
										if(field != null){
//											LOGGER.info("TEST: " + field.getName());
											entity = o1;
										}else{
											entity = null;
										}
										fieldUpdatedInO1 = true;
									}else{
										entity = null;
									}
									
									if(o2 != null){
										Field field1 = null;
										if(field != null && fieldUpdatedInO1){
											field1 = locateField(o2.getClass().getDeclaredFields(),jColumnList);
										}else{
											field1 = locateField(o2.getClass().getDeclaredFields(),jColumnList);
											field = field1;
										}
										if(field1 != null){
//											LOGGER.info("TEST: " + field1.getName());
											fromDb = o2;
										}else{
											fromDb = null;
										}
									}else{
										fromDb = null;
									}
//									LOGGER.info("---------");
									ctr++;
								}
								
//								LOGGER.info("ColumnList Size: " + jColumnList.size());
//								LOGGER.info("EntityStack Size:" + entityStack.size());
//								LOGGER.info("FromDBStack Size: " + fromDbStack.size());
								
								for(JoinColumn jcol : jColumnList){
									if(!entityStack.isEmpty()){
										o1 = entityStack.pop();
//										LOGGER.info("pop o1: " + o1);
									}
									
									if(!fromDbStack.isEmpty()){
										o2 = fromDbStack.pop();
//										LOGGER.info("pop o2: " + o2);
									}
									
									print(sb, o1, o2, jcol.name());
								}
								
								field = tempField;
								entity = tempEntity;
								fromDb = tempFromDb;

							}
						}
					}
				}
			} else {
				LOGGER.info("Can't find original entity");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			LOGGER.info("ERROR: " + e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			LOGGER.info("ERROR: " + e.getMessage());
		} catch (IntrospectionException e) {
			e.printStackTrace();
			LOGGER.info("ERROR: " + e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			LOGGER.info("ERROR: " + e.getMessage());
		}
		return sb.toString();
	}

	private Field locateField(Field[] fieldList1, List<JoinColumn> jColumnList) {
		Field field = null;
		for(Field field1 : fieldList1){
			JoinColumn jColumn1 = field1
					.getAnnotation(JoinColumn.class);
			if (jColumn1 != null) {
				for(JoinColumn jcol : jColumnList){
					if(jcol.name().equals(jColumn1.name())){
//						LOGGER.info("located field: " + field1.getName());
						return field = field1;
					}
				}
				
			}else{
				JoinColumns jColumns1 = field1
						.getAnnotation(JoinColumns.class);
				if (jColumns1 != null) {
					for(JoinColumn jcol1 : jColumns1.value()){
						for(JoinColumn jcol : jColumnList){
							if(jcol.name().equals(jcol1.name())){
//								LOGGER.info("located field: " + field1.getName());
								return field = field1;
							}
						}
					}
				}
				
			}
		}
		return field;
	}

	private void print(StringBuilder sb, Object o1, Object o2, String name) {

		// LOGGER.info("o1: " + o1);
		// LOGGER.info("o2: " + o2);
		if (o1 == null && o2 == null) {
			return;
		}
		if ((o1 == null && o2 != null) || (o2 == null && o1 != null)
				|| !o1.equals(o2)) {
			sb.append(name).append(":FROM:");

			if (o2 != null) {
				if (o2 instanceof DisplayTag) {
					DisplayTag disp = (DisplayTag) o2;
					sb.append(disp.getDisplayName());
				} else {
					sb.append(o2.toString());
				}
			} else {
				sb.append("NULL");
			}
			sb.append(":TO:");
			if (o1 != null) {
				if (o1 instanceof DisplayTag) {
					DisplayTag disp = (DisplayTag) o1;
					sb.append(disp.getDisplayName());
				} else {
					sb.append(o1.toString());
				}
			} else {
				sb.append("NULL");
			}
			sb.append(";");
		}
	}
}
