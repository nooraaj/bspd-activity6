package com.bsp.fsccis.util;

import java.util.HashMap;
import java.util.Map;

import com.bsp.fsccis.entity.AuditTrail;
import com.bsp.fsccis.entity.RefAgency;
import com.bsp.fsccis.entity.RefAgencyGroup;
import com.bsp.fsccis.entity.RefAgencyGroupFolderVisible;
import com.bsp.fsccis.entity.RefFilePermission;
import com.bsp.fsccis.entity.RefFileProperty;
import com.bsp.fsccis.entity.SysConfiguration;
import com.bsp.fsccis.entity.SysRole;
import com.bsp.fsccis.entity.RefSecurityQuestion;
import com.bsp.fsccis.entity.RefUserAccounts;

public class ClassBean{

	private static Map<String, ClassDisplayName> classMap;
    private static ClassBean instance;

    public static ClassBean getInstance() {
        return instance == null ? instance = new ClassBean() : instance;
    }
	public ClassBean(){
		classMap = new HashMap<String, ClassDisplayName>();
		classMap.put("AuditTrail", new ClassDisplayName(AuditTrail.class,"Audit Trail"));
		classMap.put("RefAgency", new ClassDisplayName(RefAgency.class,"Agency"));
		classMap.put("RefAgencyGroup", new ClassDisplayName(RefAgencyGroup.class,"Agency Group"));
		classMap.put("RefFilePermission", new ClassDisplayName(RefFilePermission.class,"File Permission"));
		classMap.put("RefFileProperty", new ClassDisplayName(RefFileProperty.class,"File/Folder"));
		classMap.put("SysConfiguration", new ClassDisplayName(SysConfiguration.class,"System Configuration"));
		classMap.put("SysRole", new ClassDisplayName(SysRole.class,"System Roles"));
		classMap.put("RefUserAccounts", new ClassDisplayName(RefUserAccounts.class,"User Accounts"));
		classMap.put("RefSecurityQuestion", new ClassDisplayName(RefSecurityQuestion.class, "Security Question"));
		classMap.put("RefUserRole", new ClassDisplayName(RefSecurityQuestion.class, "User Roles"));
		classMap.put("RefAgencyGroupFolderVisible", new ClassDisplayName(RefAgencyGroupFolderVisible.class,"Agency Group Folder Visible"));
	}
	
	public Map<String, ClassDisplayName> getClassMap() {
		return classMap;
	}

}
