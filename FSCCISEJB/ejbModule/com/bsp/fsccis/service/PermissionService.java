package com.bsp.fsccis.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;

@Stateless
public class PermissionService {
	@EJB
	CRUDSingleton crud;

	public void addNewFileWithPermission(RefFileProperty newFile, RefAgencyGroup ownerGroup, String cuser){
		crud.create(newFile);
		
		String query = "INSERT INTO \"FSCC-IS\".REF_FILE_PERMISSION "
				+ " (FILE_PROPERTY_ID, TARGET_GROUP_ID, OWNER_GROUP_ID, VISIBLE, CDATE, CTIME, CUSER) "
				+ " SELECT " + newFile.getFilePropertyId() + ",TARGET_GROUP_ID," + ownerGroup.getAgencyGroupId()
				+ "		," + RefFilePermission.VISIBLE + ",CURRENT_DATE,CURRENT_TIME,'" + cuser + "'" 
				+ " FROM \"FSCC-IS\".TRUSTED_GROUPS WHERE OWNER_GROUP_ID = " + ownerGroup.getAgencyGroupId();
		System.out.println(query);
		int result = crud.getEntityManager().createNativeQuery(query).executeUpdate();
	}
}
