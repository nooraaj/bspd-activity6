package com.bsp.fsccis.bean.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
//import org.primefaces.model.SortOrder;

import com.bsp.fsccis.entity.tag.LazyTag;

public abstract class GenericLazyDataModel<T> extends LazyDataModel<T> {
	
	private static final Logger LOGGER = Logger.getLogger(GenericLazyDataModel.class.getSimpleName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected EntityManager em;
	protected Class<T> entityClass;

	protected String tableName;
	protected String schema;
	public GenericLazyDataModel(EntityManager em, Class<T> entityClass) {
		this.em = em;
		this.entityClass = entityClass;
		
		LOGGER.info("EntityClass: " + entityClass);

        tableName = entityClass.getAnnotation(Table.class).name();

        if(tableName == null || tableName.isEmpty()){
        	tableName = entityClass.getSimpleName().toUpperCase();
        }
        
        schema = entityClass.getAnnotation(Table.class).schema();
        if(schema != null && !schema.isEmpty()){
        	schema = "\"" + schema + "\"";
        	schema += ".";
        }
		int result = count(null);
		LOGGER.info("result: " + result);
        this.setRowCount(result);
	}
	
	private int count(String whereClause){
//		LOGGER.info("whereClause: " + whereClause);
		if(whereClause == null || whereClause.isEmpty()){
			return ((Integer)em.createNativeQuery("SELECT COUNT(*) FROM " + schema + "\"" + tableName + "\"").getSingleResult()); 
		}else{
			return ((Integer)em.createNativeQuery("SELECT COUNT(*) FROM " + schema + "\"" + tableName + "\" WHERE " + whereClause).getSingleResult());
		}
	}
	
	@Override
	public int getPageSize() {
		return 10;
	}

	private boolean whereStringEmpty = true;
	protected Map<String, FilterMeta> filters;
	
	//public List<T> load(int first, int pageSize, String sortField,
	//		SortOrder sortOrder, Map<String, Object> filters) {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> filters) {
		
		first++;
		setWhereStringEmpty(true);
		
		//LOGGER.info("1ST" + filters);
		//LOGGER.info("2ND" + filters.keySet());
		
		for(FilterMeta s : filters.values()){
			LOGGER.info(s.getField() + "-->" + s.getFilterValue());
			
		}
		
		this.filters = filters;
		
		StringBuilder sb = new StringBuilder();
		StringBuilder innerQuery = new StringBuilder();
		
		Object o = null;
		Constructor<T> constructor;
		try {
			constructor = entityClass.getConstructor(new Class<?>[]{});
			o = constructor.newInstance(new Object[]{});
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Problem initializing data. Page most likely will crash");
		}
		if(o == null){
			return new ArrayList<T>();
		}

		sb.append("SELECT rownum");
		if (o instanceof LazyTag) {
			LazyTag lazy = (LazyTag) o;
			if(!lazy.pkColumnNames().isEmpty()){
				sb.append(",");
			}
			boolean isColListEmpty = true;
			for(int i=0;i<lazy.pkColumnNames().size();i++){
				if(!isColListEmpty){
					sb.append(",");
				}
				sb.append("tmp.\"").append(lazy.pkColumnNames().get(i)).append("\"");
				isColListEmpty = false;
			}
		}
		sb.append(" FROM ( SELECT ROW_NUMBER() OVER() AS rownum, tmp1.*");
		sb.append(" FROM (SELECT * FROM ").append(schema).append("\"").append(tableName).append("\"");
		innerQuery.append("SELECT * FROM ").append(schema).append("\"").append(tableName).append("\"");
		
		StringBuilder sb1 = new StringBuilder();
		Field[] fieldList = o.getClass().getDeclaredFields();
		if(filters != null && !filters.isEmpty()){
			for(String key : filters.keySet()){	
				LOGGER.info("FIND: " + key);
				
				for(Field field : fieldList){
					if(field.getName().equals(key)){
						LOGGER.info("FIELD: " + field.getName());
						Class type = field.getType();
						Column column = field.getAnnotation(Column.class);
						if (column != null) {
							String name = (column.name() != null && !column.name().isEmpty()) ? column.name().toUpperCase():field.getName().toUpperCase();
							
							if(!isWhereStringEmpty()){
								sb1.append(" AND ");
							}
							if(type.getSimpleName().equals(Integer.class.getSimpleName())){
								sb1.append(" \"").append(name).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
							} else if(type.getSimpleName().equals(Short.class.getSimpleName())){
								sb1.append(" \"").append(name).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
							} else if(type.getSimpleName().equals(Long.class.getSimpleName())){
								sb1.append(" \"").append(name).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
							} else if(type.getSimpleName().equals(String.class.getSimpleName())){
								sb1.append(" UPPER(\"").append(name).append("\") LIKE '%").append(filters.get(key).getFilterValue().toString().toUpperCase()).append("%'");
							}
							setWhereStringEmpty(false);
						}else{
							JoinColumn jColumn = field.getAnnotation(JoinColumn.class);
							if(jColumn != null){
								String name = (jColumn.name() != null && !jColumn.name().isEmpty()) ? jColumn.name().toUpperCase():field.getName().toUpperCase();

								if(!isWhereStringEmpty()){
									sb1.append(" AND ");
								}
								//ASSUMPTION if @JoinColumn is for Number.class only not strings;
								sb1.append(" \"").append(name).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
								setWhereStringEmpty(false);
							}else{
								Transient tColumn = field.getAnnotation(Transient.class);
								if(tColumn == null){
									if(!isWhereStringEmpty()){
										sb1.append(" AND ");
									}
									if(type.getSimpleName().equals(Integer.class.getSimpleName())){
										sb1.append(" \"").append(key.toUpperCase()).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
									} else if(type.getSimpleName().equals(Short.class.getSimpleName())){
										sb1.append(" \"").append(key.toUpperCase()).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
									} else if(type.getSimpleName().equals(Long.class.getSimpleName())){
										sb1.append(" \"").append(key.toUpperCase()).append("\" IN (").append(filters.get(key).getFilterValue()).append(") ");
									} else if(type.getSimpleName().equals(String.class.getSimpleName())){
										sb1.append(" UPPER(\"").append(key.toUpperCase()).append("\") LIKE '%").append(filters.get(key).getFilterValue().toString().toUpperCase()).append("%'");
									}
									setWhereStringEmpty(false);
								}
							}
						}
					}
				}
			}
		}
		
		//LOGGER.info("filters: {agencyGroupId=1} " + filters);
		//LOGGER.info("sb1: AGENCY_GROUP_ID IN (1) " + sb1);
		
		customInnerWhereClause(filters,sb1);
				
		if(!isWhereStringEmpty()){
			sb.append(" WHERE ").append(sb1.toString());
			innerQuery.append(" WHERE ").append(sb1.toString());
		}
		
		int result = count(sb1.toString());
		LOGGER.info("result: " + result);
        this.setRowCount(result);

		StringBuilder order = new StringBuilder();
		customOrderBy(filters,order);
		innerQuery.append(order);
//		LOGGER.info("innerQuery: " + innerQuery.toString());
		getInnerQuery(innerQuery.toString());
		
		sb.append(order);
		sb.append(" ) AS tmp1");
		
		sb.append(" ) AS tmp WHERE rownum >= ").append(first).append(" AND rownum < ").append(first + pageSize);

		customWhereFilter(filters,sb);
		LOGGER.info("QUERY: " + sb.toString());
		
		setResultObjArr(em.createNativeQuery(sb.toString()).getResultList());

		if (getResult() == null) {
			setResult(new ArrayList<T>());
		}else{
			getResult().clear();
		}
		processResultObjArr(); 
		return getResult();
	}
	
	public void getInnerQuery(String innerQuery) {
		
	}

	public void customInnerWhereClause(Map<String, FilterMeta> filters2,
			StringBuilder sb) {}
	
	public void customOrderBy(Map<String, FilterMeta> filters2,
			StringBuilder sb) {}


	public void customWhereFilter(Map<String, FilterMeta> filters2, StringBuilder sb) {
	}


	public abstract void processResultObjArr();
	
	protected List<T> getResult() {
		return result;
	}

	protected void setResult(List<T> result) {
		this.result = result;
	}
	protected List<Object[]> getResultObjArr() {
		return resultObjArr;
	}

	protected void setResultObjArr(List<Object[]> resultObjArr) {
		this.resultObjArr = resultObjArr;
	}
	public boolean isWhereStringEmpty() {
		return whereStringEmpty;
	}


	public void setWhereStringEmpty(boolean whereStringEmpty) {
		this.whereStringEmpty = whereStringEmpty;
	}


	private List<Object[]> resultObjArr;
	private List<T> result;
}
