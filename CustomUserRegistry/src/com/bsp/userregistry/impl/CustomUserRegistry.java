package com.bsp.userregistry.impl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author MallillinJG
 */
import com.bsp.connection.DB2Connection;
import com.bsp.encryption.SHA256_UTF8_SALT;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;





import com.ibm.websphere.security.*;

import java.util.logging.Level;
import java.util.logging.Logger;

//----------------------------------------------------------------------
//The program provides the Regular Expression implementation 
//used in the sample for the custom user registry (FileRegistrySample). 
//The pattern matching in the sample uses this program to search for the 
//pattern (for users and groups).
//----------------------------------------------------------------------
public class CustomUserRegistry implements UserRegistry {

    private DB2Connection conn;
    private Properties config;
    private static final Logger log = Logger.getLogger(CustomUserRegistry.class.getName());
    private String realmName;
    private String moduleName;
    private String userTableName;
    private String userIdField;
    private String userNameField;
    private String passwordField;
    private String userGroupTableName;
    private String groupTableName;
    private String groupIdFieldName;
    private String groupNameFieldName;
    private String uEnabledFieldUsed;
    private String uEnabledFieldName;
    private String uEnabledIntId;

    /**
     * Default Constructor *
     */
    public CustomUserRegistry() throws java.rmi.RemoteException {
    }

    /**
     * Initializes the registry. This method is called when creating the
     * registry.
     *
     * @param props - The registry-specific properties with which to initialize
     * the custom registry
     * @exception CustomRegistryException if there is any registry-specific
     * problem
     *
     */
    @Override
    public void initialize(java.util.Properties props)
            throws CustomRegistryException {
        log.log(Level.INFO, "CustomUserRegistry initialize: ");
        this.config = props;
        log.log(Level.INFO, "Retrieving configuration from " + config.getProperty("config"));
        try {
			props.load(new FileInputStream(new File(config.getProperty("config"))));
			realmName = props.getProperty("realmName");

	        moduleName = props.getProperty("moduleName");

	        userTableName = props.getProperty("userTableName");
	        userIdField = props.getProperty("userIdField");
	        userNameField = props.getProperty("userNameField");
	        passwordField = props.getProperty("passwordField");

	        userGroupTableName = props.getProperty("userGroupTableName");

	        groupTableName = props.getProperty("groupTableName");
	        groupIdFieldName = props.getProperty("groupIdFieldName");
	        groupNameFieldName = props.getProperty("groupNameFieldName");
	        
	        
	        uEnabledFieldUsed = props.getProperty("uEnabledFieldUsed");
	        uEnabledFieldName = props.getProperty("uEnabledFieldName");
	        uEnabledIntId = props.getProperty("uEnabledIntId"); 
		} catch (FileNotFoundException e) {
			throw new CustomRegistryException("Configuration File not found: " + config.getProperty("config"));        
		} catch (IOException e) {
			throw new CustomRegistryException("Configuration File cannot be accessed: " + config.getProperty("config"));        
		}
        
        if (realmName == null
                || moduleName == null
                || userTableName == null
                || userIdField == null
                || userNameField == null
                || passwordField == null
                || userGroupTableName == null
                || groupTableName == null
                || groupIdFieldName == null
                || groupNameFieldName == null
                || uEnabledFieldUsed == null
                || uEnabledFieldName == null
                || uEnabledIntId == null) { 
        	log.log(Level.INFO,"Other required fields not found in {0} config file looking in the server config...",config.getProperty("config"));
        	
        	realmName = config.getProperty("realmName");

            moduleName = config.getProperty("moduleName");

            userTableName = config.getProperty("userTableName");
            userIdField = config.getProperty("userIdField");
            userNameField = config.getProperty("userNameField");
            passwordField = config.getProperty("passwordField");

            userGroupTableName = config.getProperty("userGroupTableName");

            groupTableName = config.getProperty("groupTableName");
            groupIdFieldName = config.getProperty("groupIdFieldName");
            groupNameFieldName = config.getProperty("groupNameFieldName");
            
            
            uEnabledFieldUsed = config.getProperty("uEnabledFieldUsed");
            uEnabledFieldName = config.getProperty("uEnabledFieldName");
            uEnabledIntId = config.getProperty("uEnabledIntId");
        	
            if (realmName == null
	                || moduleName == null
	                || userTableName == null
	                || userIdField == null
	                || userNameField == null
	                || passwordField == null
	                || userGroupTableName == null
	                || groupTableName == null
	                || groupIdFieldName == null
	                || groupNameFieldName == null
	                || uEnabledFieldUsed == null
	                || uEnabledFieldName == null
	                || uEnabledIntId == null) {
	        	throw new CustomRegistryException("Supply all fields: realmName,moduleName,userTableName,userIdField,userNameField,passwordField,userGroupTableName,groupTableName,groupIdFieldName,groupNameFieldName,uEnabledFieldUsed,uEnabledFieldName,uEnabledIntId");
	        }
        }
    }

    /**
     * Checks the password of the user. This method is called to authenticate a
     * user when the user's name and password are given.
     *
     * @param userSecurityName the name of user
     * @param password the password of the user
     * @return a valid userSecurityName. Normally this is the name of same user
     * whose password was checked but if the implementation wants to return any
     * other valid userSecurityName in the registry it can do so
     * @exception CheckPasswordFailedException if userSecurityName/ password
     * combination does not exist in the registry
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String checkPassword(String userSecurityName, String passwd)
            throws PasswordCheckFailedException,
            CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry checkPasswrd: {1} {2}", new Object[]{moduleName, userSecurityName, passwd});
        String username = "";

        try {
        	log.log(Level.INFO, "before connection"); 
            conn = new DB2Connection(config);
            log.log(Level.INFO, "after connection");
            passwd = SHA256_UTF8_SALT.encrypt(passwd);  
            log.log(Level.INFO, passwd); 
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT ");
            sb.append("\"").append(userNameField).append("\",");
            sb.append("\"").append(passwordField).append("\" ");
            sb.append(" FROM \"").append(userTableName).append("\"");
            sb.append(" WHERE \"").append(userNameField).append("\" = '").append(userSecurityName).append("'");
            sb.append(" AND \"").append(passwordField).append("\" = '").append(passwd).append("'");


            String query = sb.toString();
            
            log.log(Level.INFO, query); 
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    username = rs.getString(userNameField);
                }
            } else {
                throw new PasswordCheckFailedException("Password check failed for user: "
                        + userSecurityName);
            }
            conn.close();
            conn = null;
        } catch (Exception e) {
            throw new CustomRegistryException(e.getMessage(), e);
        }
        log.log(Level.INFO, "{0}CustomUserRegistry checkPassword(out): {1}", new Object[]{moduleName, username});
        return username;
    }

    /**
     * Maps an X.509 format certificate to a valid user in the registry. This is
     * used to map the name in the certificate supplied by a browser to a valid
     * userSecurityName in the registry
     *
     * @param cert the X509 certificate chain
     * @return The mapped name of the user userSecurityName
     * @exception CertificateMapNotSupportedException if the particular
     * certificate is not supported.
     * @exception CertificateMapFailedException if the mapping of the
     * certificate fails.
     * @exception CustomRegistryException if there is any registry -specific
     * problem
     *
     */
    @Override
    public String mapCertificate(X509Certificate[] cert)
            throws CertificateMapNotSupportedException,
            CertificateMapFailedException,
            CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry mapCertificate:", moduleName);
        String name = null;
        X509Certificate cert1 = cert[0];
        try {
            // map the SubjectDN in the certificate to a userID.
            name = cert1.getSubjectDN().getName();
        } catch (Exception ex) {
            throw new CertificateMapNotSupportedException(ex.getMessage(), ex);
        }

        if (!isValidUser(name)) {
            throw new CertificateMapFailedException("user: " + name
                    + " is not valid");
        }
        log.log(Level.INFO, "{0}CustomUserRegistry mapCertificate(out): {0}", new Object[]{moduleName, name});
        return name;
    }

    /**
     * Returns the realm of the registry.
     *
     * @return the realm. The realm is a registry-specific string indicating the
     * realm or domain for which this registry applies. For example, for OS/400
     * or AIX this would be the host name of the system whose user registry this
     * object represents. If null is returned by this method, realm defaults to
     * the value of moduleName + "CustomUserRegistryRealm". It is recommended
     * that you use your own value for realm.
     *
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String getRealm()
            throws CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry getRealm: ", moduleName);
        log.log(Level.INFO, "{0}CustomUserRegistry getRealm(out): {1}", new Object[]{moduleName, realmName});
        return realmName;
    }

    /**
     * Gets a list of users that match a pattern in the registry. The maximum
     * number of users returned is defined by the limit argument. This method is
     * called by the administrative console and scripting (command line) to make
     * the users in the registry available for adding them (users) to roles.
     *
     * @param pattern the pattern to match. (For example, a* will match all
     * userSecurityNames starting with a)
     * @param limit the maximum number of users that should be returned. This is
     * very useful in situations where there are thousands of users in the
     * registry and getting all of them at once is not practical. The default is
     * 100. A value of 0 implies get all the users and hence must be used with
     * care.
     * @return a Result object that contains the list of users requested and a
     * flag to indicate if more users exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public Result getUsers(String pattern, int limit)
            throws CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUsers: {1} {2}", new Object[]{moduleName, pattern, limit});
        List<String> allUsers = new ArrayList<String>();
        Result result = new Result();
        int count = 0;
        int newLimit = limit + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(userNameField).append("\"");
        sb.append(" FROM \"").append(userTableName).append("\"");

        log.info(sb.toString());
        
        String query = sb.toString();
//                "SELECT LOGINNAME FROM IIUSERPROFILE";
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                String user = rs.getString(userNameField);
                if (match(user, pattern)) {
                    allUsers.add(user);
                    if (limit != 0 && ++count == newLimit) {
                        allUsers.remove(user);
                        result.setHasMore();
                        break;
                    }
                }
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        log.log(Level.INFO, "{0}CustomUserRegistry getUsers(out): returned {1} users", new Object[]{moduleName, allUsers.size()});
//        for(String s : allUsers){
//            log.log(Level.INFO,  "{0}CustomUserRegistry {1}", new Object[]{moduleName,s});
//        }
        result.setList(allUsers);
        return result;
    }

    /**
     * Returns the display name for the user specified by userSecurityName.
     *
     * This method may be called only when the user information is displayed
     * (information purposes only, for example, in the administrative console)
     * and hence not used in the actual authentication or authorization
     * purposes. If there are no display names in the registry return null or
     * empty string.
     *
     * In WebSphere Application Server 4.x custom registry, if you had a display
     * name for the user and if it was different from the security name, the
     * display name was returned for the EJB methods getCallerPrincipal() and
     * the servlet methods getUserPrincipal() and getRemoteUser(). In WebSphere
     * Application Server Version 5.x and later, for the same methods, the
     * security name will be returned by default. This is the recommended way as
     * the display name is not unique and might create security holes. However,
     * for backward compatibility if you need the display name to be returned
     * set the property WAS_UseDisplayName to true.
     *
     * See the Information Center documentation for more information.
     *
     * @param userSecurityName the name of the user.
     * @return the display name for the user. The display name is a
     * registry-specific string that represents a descriptive, not necessarily
     * unique, name for a user. If a display name does not exist return null or
     * empty string.
     * @exception EntryNotFoundException if userSecurityName does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String getUserDisplayName(String userSecurityName)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUserDisplayName: {1}", new Object[]{moduleName, userSecurityName});

        if (!isValidUser(userSecurityName)) {
            EntryNotFoundException nsee = new EntryNotFoundException("user: "
                    + userSecurityName + " is not valid");
            throw nsee;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getUserDisplayname(out): {1}", new Object[]{moduleName, userSecurityName});
        return userSecurityName;
    }

    /**
     * Returns the unique ID for a userSecurityName. This method is called when
     * creating a credential for a user.
     *
     * @param userSecurityName - The name of the user.
     * @return The unique ID of the user. The unique ID for a user is the
     * stringified form of some unique, registry-specific, data that serves to
     * represent the user. For example, for the UNIX user registry, the unique
     * ID for a user can be the UID.
     * @exception EntryNotFoundException if userSecurityName does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String getUniqueUserId(String userSecurityName)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueUserId: {1}", new Object[]{moduleName, userSecurityName});
        String uniqueUsrId = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(userIdField).append("\"");
        sb.append(" FROM \"").append(userTableName).append("\"");
        sb.append(" WHERE \"").append(userNameField).append("\" = '").append(userSecurityName).append("'");

        String query = sb.toString();
//                "SELECT USERID FROM IIUSERPROFILE WHERE LOGINNAME = '" + userSecurityName + "'";
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                uniqueUsrId = rs.getString(userIdField);
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        if (uniqueUsrId == null) {
            EntryNotFoundException nsee =
                    new EntryNotFoundException("Cannot obtain uniqueId for user: "
                    + userSecurityName);
            throw nsee;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueUserId(out): {1}", new Object[]{moduleName, uniqueUsrId});
        return uniqueUsrId;
    }

    /**
     * Returns the name for a user given its unique ID.
     *
     * @param uniqueUserId - The unique ID of the user.
     * @return The userSecurityName of the user.
     * @exception EntryNotFoundException if the unique user ID does not exist.
     * @exception CustomRegistryException if there is any registry-specific
     * problem
     *
     */
    @Override
    public String getUserSecurityName(String uniqueUserId)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUserSecurityName: {1}", new Object[]{moduleName, uniqueUserId});
        String usrSecName = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(userNameField).append("\"");
        sb.append(" FROM \"").append(userTableName).append("\"");
        sb.append(" WHERE \"").append(userIdField).append("\" = ").append(uniqueUserId);

        String query = sb.toString();
//        "SELECT LOGINNAME FROM IIUSERPROFILE WHERE USERID = " + uniqueUserId;
        try {

            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                usrSecName = rs.getString(userNameField);
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        if (usrSecName == null) {
            EntryNotFoundException ex =
                    new EntryNotFoundException("Cannot obtain the user securityName for " + uniqueUserId);
            throw ex;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getUserSecurityName(out): {1}", new Object[]{moduleName, usrSecName});
        return usrSecName;

    }

    /**
     * Determines if the userSecurityName exists in the registry
     *
     * @param userSecurityName - The name of the user
     * @return True if the user is valid; otherwise false
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     * @exception RemoteException as this extends java.rmi.Remote interface
     *
     */
    @Override
    public boolean isValidUser(String userSecurityName)
            throws CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry isValidUser: {1}", new Object[]{moduleName, userSecurityName});
        boolean isValid = false;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(userNameField).append("\"");
        sb.append(" FROM \"").append(userTableName).append("\"");
        sb.append(" WHERE \"").append(userNameField).append("\" = '").append(userSecurityName).append("'");

        String query = sb.toString();
//        "SELECT LOGINNAME FROM IIUSERPROFILE WHERE LOGINNAME = " + userSecurityName;
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                isValid = true;
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        log.log(Level.INFO, "{0}CustomUserRegistry isValidUser(out): {1}", new Object[]{moduleName, isValid});
        return isValid;
    }

    /**
     * Gets a list of groups that match a pattern in the registry The maximum
     * number of groups returned is defined by the limit argument. This method
     * is called by administrative console and scripting (command line) to make
     * available the groups in the registry for adding them (groups) to roles.
     *
     * @param pattern the pattern to match. (For example, a* matches all
     * groupSecurityNames starting with a)
     * @param Limits the maximum number of groups to return This is very useful
     * in situations where there are thousands of groups in the registry and
     * getting all of them at once is not practical. The default is 100. A value
     * of 0 implies get all the groups and hence must be used with care.
     * @return A Result object that contains the list of groups requested and a
     * flag to indicate if more groups exist.
     * @exception CustomRegistryException if there is any registry-specific
     * problem
     *
     */
    @Override
    public Result getGroups(String pattern, int limit)
            throws CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry getGroups: {1} {2}", new Object[]{moduleName, pattern, limit});
        List<String> allGroups = new ArrayList<String>();
        Result result = new Result();
        int count = 0;
        int newLimit = limit + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(groupNameFieldName).append("\"");
        sb.append(" FROM \"").append(groupTableName).append("\"");

        String query = sb.toString();
//        "SELECT GROUPNAME FROM IIUSERGROUP";
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                String user = rs.getString(groupNameFieldName);
                if (match(user, pattern)) {
                    allGroups.add(user);
                    if (limit != 0 && ++count == newLimit) {
                        allGroups.remove(user);
                        result.setHasMore();
                        break;
                    }
                }
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        log.log(Level.INFO, "{0}CustomUserRegistry getGroups(out): ", moduleName);
        for (String s : allGroups) {
            log.log(Level.INFO, "{0}CustomUserRegistry {1}", new Object[]{moduleName, s});
        }
        result.setList(allGroups);
        return result;
    }

    /**
     * Returns the display name for the group specified by groupSecurityName.
     * For this version of WebSphere Application Server, the only usage of this
     * method is by the clients (administrative console and scripting) to
     * present a descriptive name of the user if it exists.
     *
     * @param groupSecurityName the name of the group.
     * @return the display name for the group. The display name is a
     * registry-specific string that represents a descriptive, not necessarily
     * unique, name for a group. If a display name does not exist return null or
     * empty string.
     * @exception EntryNotFoundException if groupSecurityName does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String getGroupDisplayName(String groupSecurityName)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupDisplayName: {1}", new Object[]{moduleName, groupSecurityName});

        if (!isValidGroup(groupSecurityName)) {
            EntryNotFoundException nsee = new EntryNotFoundException("group: "
                    + groupSecurityName + " is not valid");
            throw nsee;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupDisplayName(out): {1}", new Object[]{moduleName, groupSecurityName});
        return groupSecurityName;
    }

    /**
     * Returns the Unique ID for a group.
     *
     * @param groupSecurityName the name of the group.
     * @return The unique ID of the group. The unique ID for a group is the
     * stringified form of some unique, registry-specific, data that serves to
     * represent the group. For example, for the UNIX user registry, the unique
     * ID might be the GID.
     * @exception EntryNotFoundException if groupSecurityName does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     * @exception RemoteException as this extends java.rmi.Remote
     *
     */
    @Override
    public String getUniqueGroupId(String groupSecurityName)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueGroupId: {1}", new Object[]{moduleName, groupSecurityName});
        String uniqueGrpId = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(groupIdFieldName).append("\"");
        sb.append(" FROM \"").append(groupTableName).append("\"");
        sb.append(" WHERE \"").append(groupNameFieldName).append("\" = '").append(groupSecurityName).append("'");

        String query = sb.toString();        
        log.log(Level.INFO, "Query", new Object[]{moduleName, query});
//        "SELECT GROUPID FROM IIUSERGROUP WHERE GROUPNAME = '" + groupSecurityName + "'";
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                uniqueGrpId = rs.getString(groupIdFieldName);
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        if (uniqueGrpId == null) {
            EntryNotFoundException nsee =
                    new EntryNotFoundException("Cannot obtain the uniqueId for group: "
                    + groupSecurityName);
            throw nsee;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueGroupId(out): {1}", new Object[]{moduleName, uniqueGrpId});
        return uniqueGrpId;
    }

    /**
     * Returns the Unique IDs for all the groups that contain the unique ID of a
     * user. Called during creation of a user's credential.
     *
     * @param uniqueUserId the unique ID of the user.
     * @return A list of all the group unique IDs that the unique user ID
     * belongs to. The unique ID for an entry is the stringified form of some
     * unique, registry-specific, data that serves to represent the entry. For
     * example, for the UNIX user registry, the unique ID for a group might be
     * the GID and the Unique ID for the user might be the UID.
     * @exception EntryNotFoundException if uniqueUserId does not exist.
     * @exception CustomRegistryException if there is any registry-specific
     * problem
     *
     */ 
    @Override
    public List<String> getUniqueGroupIds(String uniqueUserId)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueGroupIds: {1}", new Object[]{moduleName, uniqueUserId});

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT \"").append(groupIdFieldName).append("\"");
        sb.append(" FROM \"").append(userGroupTableName).append("\"");
        sb.append(" WHERE \"").append(userIdField).append("\" = ").append(uniqueUserId);
        if(uEnabledFieldUsed != null && uEnabledFieldUsed.toUpperCase().equals("TRUE")){
            sb.append(" AND \"").append(uEnabledFieldName).append("\" = ").append(uEnabledIntId);
        }

        String query = sb.toString();
//        "SELECT DISTINCT GROUPID FROM IIUSERPROFILE WHERE USERID = " + uniqueUserId;
        System.out.println(query);
        List<String> uniqueGrpIds = new ArrayList<String>();
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                uniqueGrpIds.add(rs.getString(groupIdFieldName));
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getUniqueGroupIds(out): returned {1} group id/s", new Object[]{moduleName, uniqueGrpIds.size()});
        for(String s : uniqueGrpIds){
            log.log(Level.INFO,  "{0}CustomUserRegistry {1}", new Object[]{moduleName,s});
        }
        return uniqueGrpIds;
    }

    /**
     * Returns the name for a group given its unique ID.
     *
     * @param uniqueGroupId the unique ID of the group.
     * @return The name of the group.
     * @exception EntryNotFoundException if the uniqueGroupId does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public String getGroupSecurityName(String uniqueGroupId)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupSecurityName: {1}", new Object[]{moduleName, uniqueGroupId});
        String grpSecName = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(groupNameFieldName).append("\"");
        sb.append(" FROM \"").append(groupTableName).append("\"");
        sb.append(" WHERE \"").append(groupIdFieldName).append("\" = ").append(uniqueGroupId);


        String query = sb.toString();
//        "SELECT GROUPNAME FROM IIUSERGROUP WHERE GROUPID = " + uniqueGroupId;
        try {

            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                grpSecName = rs.getString(groupNameFieldName);
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }

        if (grpSecName == null) {
            EntryNotFoundException ex =
                    new EntryNotFoundException("Cannot obtain the group security name for: " + uniqueGroupId);
            throw ex;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupSecurityName(out): {1}", new Object[]{moduleName, grpSecName});
        return grpSecName;

    }

    /**
     * Determines if the groupSecurityName exists in the registry
     *
     * @param groupSecurityName the name of the group
     * @return True if the groups exists; otherwise false
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     *
     */
    @Override
    public boolean isValidGroup(String groupSecurityName)
            throws CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry isValidGroup: {1}", new Object[]{moduleName, groupSecurityName});
        boolean isValid = false;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(groupNameFieldName).append("\"");
        sb.append(" FROM \"").append(groupTableName).append("\"");
        sb.append(" WHERE \"").append(groupNameFieldName).append("\" = '").append(groupSecurityName).append("'");

        String query = sb.toString();
//        "SELECT GROUPNAME FROM IIUSERGROUP WHERE GROUPNAME = " + groupSecurityName;
        try {

            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            while (rs.next()) {
                isValid = true;
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }
        log.log(Level.INFO, "{0}CustomUserRegistry isValidGroup(out): {1}", new Object[]{moduleName, isValid});
        return isValid;
    }

    /**
     * Returns the securityNames of all the groups that contain the user
     *
     * This method is called by the administrative console and scripting
     * (command line) to verify that the user entered for RunAsRole mapping
     * belongs to that role in the roles to user mapping. Initially, the check
     * is done to see if the role contains the user. If the role does not
     * contain the user explicitly, this method is called to get the groups that
     * this user belongs to so that a check can be made on the groups that the
     * role contains.
     *
     * @param userSecurityName the name of the user
     * @return A list of all the group securityNames that the user belongs to.
     * @exception EntryNotFoundException if user does not exist.
     * @exception CustomRegistryException if there is any registry- specific
     * problem
     * @exception RemoteException as this extends the java.rmi.Remote interface
     *
     */
    @Override
    public List<String> getGroupsForUser(String userName)
            throws CustomRegistryException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupsForUser: {1}", new Object[]{moduleName, userName});

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT RLE.\"").append(groupNameFieldName).append("\"");
        sb.append(" FROM \"").append(userTableName).append("\" AS USR ");
        sb.append(" LEFT JOIN \"").append(userGroupTableName).append("\" USRROLE ON USR.\"").append(userIdField).append("\" = USRROLE.\"").append(userIdField).append("\"");
        sb.append(" LEFT JOIN \"").append(groupTableName).append("\" RLE ON USRROLE.\"").append(groupIdFieldName).append("\" = RLE.\"").append(groupIdFieldName).append("\"");
        sb.append(" WHERE USR.\"").append(userNameField).append("\" = '").append(userName).append("'");


        String query = sb.toString();
//        "select RLE.ROLENAME from SES_ODSSP.USERLOGIN as USR " +
//                        "    LEFT JOIN SES_ODSSP.USERROLES USRROLE ON USR.ID = USRROLE.USER_USERID " +
//                        "    LEFT JOIN SES_ODSSP.ROLES RLE ON USRROLE.ROLE_ROLEID = RLE.ID " +
//                        " WHERE USR.USERNAME = ''";
        log.info(query);
        List<String> grpsForUser = new ArrayList<String>();
        try {
            conn = new DB2Connection(config);
            conn.prepareQuery(query);
            conn.executeQuery();
            ResultSet rs = conn.getResult();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    grpsForUser.add(rs.getString(groupNameFieldName));
                }
            } else {
                throw new EntryNotFoundException("user: " + userName
                        + " is not valid");
            }
            conn.close();
            conn = null;
        } catch (Exception ex) {
            throw new CustomRegistryException(ex.getMessage(), ex);
        }
        log.log(Level.INFO, "{0}CustomUserRegistry getGroupsForUser(out):", moduleName);
        for (String s : grpsForUser) {
            log.log(Level.INFO, "{0}CustomUserRegistry {1}", new Object[]{moduleName, s});
        }
        return grpsForUser;
    }

    /**
     * Gets a list of users in a group.
     *
     * The maximum number of users returned is defined by the limit argument.
     *
     * This method is being used by the WebSphere Application Server Enterprise
     * process choreographer (Enterprise) when staff assignments are modeled
     * using groups.
     *
     * In rare situations, if you are working with a registry where getting all
     * the users from any of your groups is not practical (for example if there
     * are a large number of users) you can create the NotImplementedException
     * for that particular group. Make sure that if the process choreographer is
     * installed (or if installed later) the staff assignments are not modeled
     * using these particular groups. If there is no concern about returning the
     * users from groups in the registry it is recommended that this method be
     * implemented without creating the NotImplemented exception.
     *
     * @param groupSecurityName the name of the group
     * @param Limits the maximum number of users that should be returned. This
     * is very useful in situations where there are lots of users in the
     * registry and getting all of them at once is not practical. A value of 0
     * implies get all the users and hence must be used with care.
     * @return A Result object that contains the list of users requested and a
     * flag to indicate if more users exist.
     * @deprecated This method will be deprecated in future.
     * @exception NotImplementedException create this exception in rare
     * situations if it is not practical to get this information for any of the
     * group or groups from the registry.
     * @exception EntryNotFoundException if the group does not exist in the
     * registry
     * @exception CustomRegistryException if there is any registry-specific
     * problem
     *
     */
    @Override
    public Result getUsersForGroup(String groupSecurityName, int limit)
            throws NotImplementedException,
            EntryNotFoundException,
            CustomRegistryException {
        log.log(Level.INFO, "{0}CustomUserRegistry getUsersForGroup: {1} {2}", new Object[]{moduleName, groupSecurityName, limit});
        throw new NotImplementedException();
    }

    /**
     * This method is implemented internally by the WebSphere Application Server
     * code in this release. This method is not called for the custom registry
     * implementations for this release. Return null in the implementation.
     *
     *
     */
    @Override
    public com.ibm.websphere.security.cred.WSCredential createCredential(String userSecurityName)
            throws CustomRegistryException,
            NotImplementedException,
            EntryNotFoundException {
        log.log(Level.INFO, "{0}CustomUserRegistry createCredential: {1}", new Object[]{moduleName, userSecurityName});
        // This method is not called.
        log.log(Level.INFO, "{0}CustomUserRegistry createCredential(out): null", moduleName);
        return null;
    }

    private boolean match(String name, String pattern) {
        log.log(Level.INFO, "{0}CustomUserRegistry match: {1} {2}", new Object[]{moduleName, name, pattern});
        RegExpSample regexp = new RegExpSample(pattern);
        boolean matches = false;
        if (regexp.match(name)) {
            matches = true;
        }
        log.log(Level.INFO, "{0}CustomUserRegistry match(out): {1}", new Object[]{moduleName, matches});
        return matches;
    }
}

class RegExpSample {

    private boolean match(String s, int i, int j, int k) {
        for (; k < expr.length; k++) {
            label0:
            {
                Object obj = expr[k];
                if (obj == STAR) {
                    if (++k >= expr.length) {
                        return true;
                    }
                    if (expr[k] instanceof String) {
                        String s1 = (String) expr[k++];
                        int l = s1.length();
                        for (; (i = s.indexOf(s1, i)) >= 0; i++) {
                            if (match(s, i + l, j, k)) {
                                return true;
                            }
                        }

                        return false;
                    }
                    for (; i < j; i++) {
                        if (match(s, i, j, k)) {
                            return true;
                        }
                    }

                    return false;
                }
                if (obj == ANY) {
                    if (++i > j) {
                        return false;
                    }
                    break label0;
                }
                if (obj instanceof char[][]) {
                    if (i >= j) {
                        return false;
                    }
                    char c = s.charAt(i++);
                    char ac[][] = (char[][]) obj;
                    if (ac[0] == NOT) {
                        for (int j1 = 1; j1 < ac.length; j1++) {
                            if (ac[j1][0] <= c && c <= ac[j1][1]) {
                                return false;
                            }
                        }

                        break label0;
                    }
                    for (int k1 = 0; k1 < ac.length; k1++) {
                        if (ac[k1][0] <= c && c <= ac[k1][1]) {
                            break label0;
                        }
                    }

                    return false;
                }
                if (obj instanceof String) {
                    String s2 = (String) obj;
                    int i1 = s2.length();
                    if (!s.regionMatches(i, s2, 0, i1)) {
                        return false;
                    }
                    i += i1;
                }
            }
        }

        return i == j;
    }

    public boolean match(String s) {
        return match(s, 0, s.length(), 0);
    }

    public boolean match(String s, int i, int j) {
        return match(s, i, j, 0);
    }

    public RegExpSample(String s) {
        Vector vector = new Vector();
        int i = s.length();
        StringBuffer stringbuffer = null;
        Object obj = null;
        for (int j = 0; j < i; j++) {
            char c = s.charAt(j);
            switch (c) {
                case 63: /* '?' */
                    obj = ANY;
                    break;

                case 42: /* '*' */
                    obj = STAR;
                    break;

                case 91: /* '[' */
                    int k = ++j;
                    Vector vector1 = new Vector();
                    for (; j < i; j++) {
                        c = s.charAt(j);
                        if (j == k && c == '^') {
                            vector1.addElement(NOT);
                            continue;
                        }
                        if (c == '\\') {
                            if (j + 1 < i) {
                                c = s.charAt(++j);
                            }
                        } else if (c == ']') {
                            break;
                        }
                        char c1 = c;
                        if (j + 2 < i && s.charAt(j + 1) == '-') {
                            c1 = s.charAt(j += 2);
                        }
                        char ac1[] = {
                            c, c1
                        };
                        vector1.addElement(ac1);
                    }

                    char ac[][] = new char[vector1.size()][];
                    vector1.copyInto(ac);
                    obj = ac;
                    break;

                case 92: /* '\\' */
                    if (j + 1 < i) {
                        c = s.charAt(++j);
                    }
                    break;

            }
            if (obj != null) {
                if (stringbuffer != null) {
                    vector.addElement(stringbuffer.toString());
                    stringbuffer = null;
                }
                vector.addElement(obj);
                obj = null;
            } else {
                if (stringbuffer == null) {
                    stringbuffer = new StringBuffer();
                }
                stringbuffer.append(c);
            }
        }

        if (stringbuffer != null) {
            vector.addElement(stringbuffer.toString());
        }
        expr = new Object[vector.size()];
        vector.copyInto(expr);
    }
    static final char NOT[] = new char[2];
    static final Integer ANY = new Integer(0);
    static final Integer STAR = new Integer(1);
    Object expr[];
}
