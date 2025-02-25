/**
 * Filename:    TwoFactorUtil.java
 * Description:
 *
 * Created:  04 24, 17 3:35:18 PM
 * Creator:  PULUMBARITDS
 * Modified:
 * Modifier:
 * Remarks:  [Please create a short description of what you modified]
 *
 */
package com.bsp.fsccis.bean.util;

import com.entrust.identityGuard.authenticationManagement.wsv11.AuthenticateGenericChallengeCallParms;
import com.entrust.identityGuard.authenticationManagement.wsv11.AuthenticationServiceBindingStub;
import com.entrust.identityGuard.authenticationManagement.wsv11.AuthenticationService_ServiceLocator;
import com.entrust.identityGuard.authenticationManagement.wsv11.AuthenticationType;
import com.entrust.identityGuard.authenticationManagement.wsv11.GenericAuthenticateParms;
import com.entrust.identityGuard.authenticationManagement.wsv11.GenericAuthenticateResponse;
import com.entrust.identityGuard.authenticationManagement.wsv11.GenericChallenge;
import com.entrust.identityGuard.authenticationManagement.wsv11.GenericChallengeParms;
import com.entrust.identityGuard.authenticationManagement.wsv11.GetGenericChallengeCallParms;
import com.entrust.identityGuard.authenticationManagement.wsv11.NameValue;
import com.entrust.identityGuard.authenticationManagement.wsv11.Response;
import com.entrust.identityGuard.authenticationManagement.wsv11.TokenChallenge;
import com.entrust.identityGuard.common.ws.TestConnection;
import com.entrust.identityGuard.common.ws.TestConnectionImpl;
import com.entrust.identityGuard.common.ws.TimeInterval;
import com.entrust.identityGuard.common.ws.URIFailoverFactory;
import com.entrust.identityGuard.failover.wsv11.AdminCredentialsImpl;
import com.entrust.identityGuard.failover.wsv11.AdminFailoverService_ServiceLocator;
import com.entrust.identityGuard.failover.wsv11.AuthenticationFailoverService_ServiceLocator;
import com.entrust.identityGuard.failover.wsv11.FailoverCallConfigurator;
import com.entrust.identityGuard.userManagement.wsv11.AdminServiceBindingStub;
import com.entrust.identityGuard.userManagement.wsv11.AdminServiceFault;
import com.entrust.identityGuard.userManagement.wsv11.AdminService_ServiceLocator;
import com.entrust.identityGuard.userManagement.wsv11.LoginCallParms;
import com.entrust.identityGuard.userManagement.wsv11.LoginParms;
import com.entrust.identityGuard.userManagement.wsv11.State;
import com.entrust.identityGuard.userManagement.wsv11.TokenDeleteCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserCreateCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserDeleteCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserFilter;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPCreateCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPDeleteCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPFilter;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPGetCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPInfo;
import com.entrust.identityGuard.userManagement.wsv11.UserOTPParms;
import com.entrust.identityGuard.userManagement.wsv11.UserParms;
import com.entrust.identityGuard.userManagement.wsv11.UserPasswordCreateCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserPasswordParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenActivateCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenActivateCompleteCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenActivateResult;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenCreateCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenDeleteCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenFilter;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenInfo;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenListCallParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenListResult;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenParms;
import com.entrust.identityGuard.userManagement.wsv11.UserTokenSetCallParms;

import ph.gov.bsp.crypt.Cryptography;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;

import com.bsp.fsccis.authentication.TwoFactorAuthenticationProperties;
import com.bsp.fsccis.bean.util.TwoFactorException;
//import ph.gov.bsp.fitpro.util.PropertyLoader;
//import ph.gov.bsp.crypt.Cryptography;

public class TwoFactorUtil {

	private TwoFactorAuthenticationProperties props;
	
    public AdminServiceBindingStub getAdminBinding() {
        AdminService_ServiceLocator locator = null;
        AdminServiceBindingStub adminBinding = null;
        // Create a new binding using the URL just created:
        try {
        	
        	initializeProperties();
        	
            String url = props.getAdmin();
            String[] urls = url.split(";");

            String ADMIN_SERVICE_URL = urls[0];
            String username = props.getUsername();
            String password = Cryptography.base64decode(props.getPassword());
            
            URL adminServiceUrl = new URL(ADMIN_SERVICE_URL);
            locator = new AdminService_ServiceLocator();
            adminBinding = (AdminServiceBindingStub) locator.getAdminService(adminServiceUrl);
            adminBinding.setMaintainSession(true);

            LoginParms loginParms = new LoginParms();
            loginParms.setAdminId(username);
            loginParms.setPassword(password);

            LoginCallParms callParms = new LoginCallParms();
            callParms.setParms(loginParms);
            adminBinding.login(callParms);

            System.out.println("*****************************************");
            System.out.println("Connected.");
            System.out.println("*****************************************");
        } catch (ServiceException ex) {
            ex.printStackTrace();
             System.out.println("ServiceException: ."+ex.toString());
            return null;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            System.out.println("MalformedException: ."+ex.toString());
            return null;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.out.println("RemoteException: ."+ex.toString());
            return null;
        }

        return adminBinding;
    }

    private AdminServiceBindingStub getAdminBinding(boolean withFailOver) {
        if (!withFailOver) {
            return getAdminBinding();
        }

        initializeProperties();
        
        int retries = Integer.parseInt(props.getRetries());

        String username = props.getUsername();
        String password = Cryptography.base64decode(
                props.getPassword());

        // How long before allowing a failed service to be retried in seconds
        // (optional)
        long holdOffTime = 600;
        // How long to wait before attempting to revert back to the primary
        // service (optional)
        long restorePrimaryTime = 3600;

        AdminServiceBindingStub binding = null;
        // Create a new binding using the URL just created:

        System.out.println("Loading URL configuration...");

        String url = props.getAdmin();
        String[] urls = url.split(";");

        System.out.println("Test Connection Implementation...");

        TestConnection testCon = new TestConnectionImpl();

        System.out.println("Failover Factory Initialization...");

        URIFailoverFactory failover = new URIFailoverFactory(urls,
                new TimeInterval(restorePrimaryTime), new TimeInterval(holdOffTime), testCon);

        System.out.println("Admin Credentials...");

        AdminCredentialsImpl cred = new AdminCredentialsImpl(username, password);

        System.out.println("Failover Configuration...");

        FailoverCallConfigurator fcc = new FailoverCallConfigurator(cred, retries, 500);

        System.out.println("Service Locator...");

        AdminFailoverService_ServiceLocator adminFailoverServiceLocator
                = new AdminFailoverService_ServiceLocator(failover, fcc);

        try {
            binding = (AdminServiceBindingStub) adminFailoverServiceLocator.getAdminService();
            binding.setMaintainSession(true);

            cred.setBinding(binding);
        } catch (ServiceException ex) {
            Logger.getLogger(TwoFactorUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return binding;
    }

    private AuthenticationServiceBindingStub getAuthBinding() throws ServiceException {
        try {
        	
        	initializeProperties();
        	
            String url = props.getAuth();
            String[] urls = url.split(";");

            String AUTH_SERVICE_URL = urls[0];
            URL authServiceUrl = new URL(AUTH_SERVICE_URL);
            AuthenticationService_ServiceLocator locator
                    = new AuthenticationService_ServiceLocator();
            locator.setAuthenticationServiceEndpointAddress(AUTH_SERVICE_URL);

            AuthenticationServiceBindingStub binding
                    = (AuthenticationServiceBindingStub) locator.getAuthenticationService(authServiceUrl);

            return binding;
        } catch (ServiceException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private AuthenticationServiceBindingStub getAuthBinding(boolean withFailOver) throws ServiceException {
        if (!withFailOver) {
            return getAuthBinding();
        }
        
        initializeProperties();

        int retries = Integer.parseInt(props.getRetries());

        //AuthenticationService_ServiceLocator locator = null;
        AuthenticationServiceBindingStub binding = null;

        String url = props.getAuth();
        String[] urls = url.split(";");
        TestConnection testCon = new TestConnectionImpl();
        URIFailoverFactory failover = new URIFailoverFactory(urls,
                new TimeInterval(3600), new TimeInterval(600), testCon);
        FailoverCallConfigurator failoverConfig = new FailoverCallConfigurator(retries, 500);
        AuthenticationFailoverService_ServiceLocator failoverServiceLocator
                = new AuthenticationFailoverService_ServiceLocator(failover, failoverConfig);
        binding = (AuthenticationServiceBindingStub) failoverServiceLocator.getAuthenticationService();
        return binding;
    }

    public void activateToken(String userId, String serialNumber, String vendor, String tokenSet, String registrationCode)
            throws RemoteException {
    	
    	initializeProperties();
    	
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        NameValue regCode = new NameValue("registrationCode", registrationCode);

        UserTokenFilter utf = new UserTokenFilter();
        utf.setSerialNumber(serialNumber);
        utf.setVendorId(vendor);
        utf.setTokenSets(new String[]{tokenSet});

        UserTokenActivateCompleteCallParms utaccp
                = new UserTokenActivateCompleteCallParms(userId, utf, new NameValue[]{regCode});

        adminBinding.userTokenActivateComplete(utaccp);
    }

    public void activateToken(String userId, String serialNumber, String registrationCode)
            throws RemoteException {
    	
    	initializeProperties();
    	
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();
        String vendor = props.getVendor();
        String tokens = props.getTokens();
        NameValue regCode = new NameValue("registrationCode", registrationCode);

        UserTokenFilter utf = new UserTokenFilter();
        utf.setSerialNumber(serialNumber);
        utf.setVendorId(vendor);
        utf.setTokenSets(new String[]{tokens});

        UserTokenActivateCompleteCallParms utaccp
                = new UserTokenActivateCompleteCallParms(userId, utf, new NameValue[]{regCode});

        adminBinding.userTokenActivateComplete(utaccp);
    }

    /* Throws an error if the user has an existing user.
     *
     */
    public void createOTP(String userId, long seconds)
            throws RemoteException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        // The following code generates the OTP for a given user ID
        // The OTP is not delivered to end user
        UserOTPParms userOTPParms = new UserOTPParms();
        userOTPParms.setLifetime(seconds * 1000); //expiration (milliseconds

        UserOTPCreateCallParms userOTPCreateCallParms = new UserOTPCreateCallParms();
        userOTPCreateCallParms.setUserid(userId);
        userOTPCreateCallParms.setParms(userOTPParms);
        UserOTPInfo[] otp = adminBinding.userOTPCreate(userOTPCreateCallParms);
    }

    /* The following code retrieves an existing OTP for a given user ID.
     * Note that if the user has more than one OTP, the oldest one is
     * retrieved. It is also possible that the user doesn't have any
     * OTPs.
     */
    public void retrieveOTP(String userId) {
        try {
            //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

            AdminServiceBindingStub adminBinding = getAdminBinding();

            UserOTPGetCallParms userOTPGetCallParms = new UserOTPGetCallParms();
            userOTPGetCallParms.setUserid(userId);
            userOTPGetCallParms.setFilter(new UserOTPFilter());
            UserOTPInfo[] otp = adminBinding.userOTPGet(userOTPGetCallParms);

            // Make sure the user has an OTP
            if (otp != null && otp.length == 1) {
                // Only able to get the OTP value if
                // admin user has the userOtpView permission
                String otpString = otp[0].getOTP();
                if (otpString != null) {
                    // Deliver the OTP to the user using an
                    // application-specific mechanism
                    System.out.println("OTP for user " + userId + " is [" + otpString + "]");
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(TwoFactorUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* The following code retrieves an existing OTP for a given user ID.
     * Note that if the user has more than one OTP, the oldest one is
     * retrieved. It is also possible that the user doesn't have any
     * OTPs.
     */
    public void deleteOTP(String userId) {
        try {
            //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

            AdminServiceBindingStub adminBinding = getAdminBinding();

            UserOTPDeleteCallParms userOTPDelCallParms = new UserOTPDeleteCallParms();
            userOTPDelCallParms.setUserid(userId);
            userOTPDelCallParms.setFilter(new UserOTPFilter());

            adminBinding.userOTPDelete(userOTPDelCallParms);
        } catch (RemoteException ex) {
            Logger.getLogger(TwoFactorUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String createToken(String userId) throws RemoteException {
    	
    	initializeProperties();
    	
        //String vendor = "Entrust Soft Token";
        String vendor = props.getVendor();
        //String tokens = "fiportaltoken";
        String tokens = props.getTokens();

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub binding = getAdminBinding();

        System.out.println("Creating token.");

        UserTokenParms userTokenParms = new UserTokenParms();
        //userTokenParms.setState(State.HOLD_PENDING);
        userTokenParms.setState(State.CURRENT);
        userTokenParms.setTokenSet(tokens);

        UserTokenCreateCallParms utccp = new UserTokenCreateCallParms();
        utccp.setParms(userTokenParms);
        utccp.setUserid(userId);
        utccp.setVendorId(vendor);

        return binding.userTokenCreate(utccp);
    }

    public void createUser(String userId, String fullName, String alias)
            throws AdminServiceFault, TwoFactorException, RemoteException {
        try {
        	
        	initializeProperties();
        	
            //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));
            AdminServiceBindingStub binding = getAdminBinding();

            if (binding == null) {
                System.out.println("Unable to connect to registration order");
                System.out.println("Creating user with userId [" + userId + "]");
                System.out.println("Name [" + fullName + "]");
                System.out.println("Alias [" + alias + "]");
                throw new TwoFactorException("Unable to connect to registration server.");
            }

            // Create and register a user
            UserParms userParms = new UserParms();
            userParms.setGroup(props.getGroup());
            userParms.setFullName(fullName);
            userParms.setUserid(userId);
            userParms.setAliases(new String[]{alias});

            UserCreateCallParms callParms = new UserCreateCallParms();
            callParms.setUserid(userId);
            callParms.setParms(userParms);            
            binding.userCreate(callParms);
            
        } catch (AdminServiceFault ex) {
            System.out.println("Error adminservice creation: "+ex.toString());
            throw ex;
        }catch (TwoFactorException ex){
             System.out.println("Error TwoFactor creation: "+ex.toString());
        }catch (RemoteException ex){
             System.out.println("Error Remote creation: "+ex.toString());       
        }
    }

    /**
     *
     * @param userId
     * @param password
     * @throws RemoteException
     */
    public void createPassword(String userId, String password) throws RemoteException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        UserPasswordParms upp = new UserPasswordParms();
        upp.setAutoGenerate(Boolean.FALSE);
        upp.setChangeRequired(Boolean.FALSE);
        upp.setClearPasswordHistory(Boolean.TRUE);
        upp.setClearRetrievablePassword(Boolean.FALSE);
        upp.setDaysToExpiry(0);
        upp.setPassword(password);

        UserPasswordCreateCallParms upccp = new UserPasswordCreateCallParms();
        upccp.setUserid(userId);
        upccp.setParms(upp);

        adminBinding.userPasswordCreate(upccp);

    }

    public void retrievePassword(String userId) {

    }

    public String createToken(String userId, String vendorId, String tokenSet) throws RemoteException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        UserTokenParms userTokenParms = new UserTokenParms();
        //userTokenParms.setState(State.HOLD_PENDING);
        userTokenParms.setState(State.CURRENT);
        userTokenParms.setTokenSet(tokenSet);

        UserTokenCreateCallParms utccp = new UserTokenCreateCallParms();
        utccp.setParms(userTokenParms);
        utccp.setUserid(userId);
        utccp.setVendorId(vendorId);

        return adminBinding.userTokenCreate(utccp);
    }

    /**
     *
     * Deletes an unassigned token (No user attached to the token).
     *
     * @param serialNo
     * @throws RemoteException
     *
     */
    public void deleteToken(String serialNo) throws RemoteException {
    	
    	initializeProperties();
    	
        //String vendor = "Entrust Soft Token";
        String vendor = props.getVendor();

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        TokenDeleteCallParms tdcp = new TokenDeleteCallParms(vendor, serialNo);

        adminBinding.tokenDelete(tdcp);
    }

    /**
     * Deletes assigned token of the user
     *
     * @param userId String id of user
     * @param serialNo String serial number of token
     * @throws java.rmi.RemoteException
     */
    public void deleteUserToken(String userId, String serialNo) throws RemoteException {
    	
    	initializeProperties();
    	
        //String vendor = "Entrust Soft Token";
        String vendor = props.getVendor();
        //String tokens = "fiportaltoken";
        String tokens = props.getTokens();

        UserTokenFilter utf = new UserTokenFilter();
        utf.setSerialNumber(serialNo);
        utf.setVendorId(vendor);
        utf.setTokenSets(new String[]{tokens});

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        UserTokenDeleteCallParms utdcp = new UserTokenDeleteCallParms(userId, utf);

        adminBinding.userTokenDelete(utdcp);
    }
    
    public void deleteUserAllToken(String userId) throws RemoteException {
    	
    	initializeProperties();
    	
        //String vendor = "Entrust Soft Token";
        String vendor = props.getVendor();
        //String tokens = "fiportaltoken";
        String tokens = props.getTokens();
        // 
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));
        
        AdminServiceBindingStub adminBinding = getAdminBinding();
        
        try {
            UserFilter uf = new UserFilter();
            uf.setTokenStates(new State[]{State.HOLD_PENDING, State.CURRENT, State.HOLD, State.PENDING, State.UNKNOWN});
            uf.setTokenVendorId(vendor);
            uf.setUserid(userId);

            UserTokenListCallParms utlcp = new UserTokenListCallParms(uf);
            UserTokenListResult utlr = adminBinding.userTokenList(utlcp);
            UserTokenInfo[] uti = utlr.getTokens();

            if (uti != null) {
                return;
            }
            
            for (UserTokenInfo userTokenInfo : uti) {
                String serialNo = userTokenInfo.getSerialNumber();

                UserTokenFilter utf = new UserTokenFilter();
                utf.setSerialNumber(serialNo);
                utf.setVendorId(vendor);
                utf.setTokenSets(new String[]{tokens});

                UserTokenDeleteCallParms utdcp = new UserTokenDeleteCallParms(userId, utf);

                adminBinding.userTokenDelete(utdcp);
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteUser(String userId) {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();
        UserDeleteCallParms udcp = new UserDeleteCallParms(userId);

        try {
            adminBinding.userDelete(udcp);
        } catch (RemoteException ex) {
            Logger.getLogger(TwoFactorUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param userId
     * @param serialNumber
     * @return activation code
     * @throws RemoteException
     */
    public String getActivationCode(String userId, String serialNumber) throws RemoteException {
    	
    	initializeProperties();
    	
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        //String vendor = "Entrust Soft Token";
        String vendor = props.getVendor();

        // The following code changes the state of a user token to PENDING
        UserTokenFilter userTokenFilter = new UserTokenFilter();
        userTokenFilter.setSerialNumber(serialNumber);
        userTokenFilter.setVendorId(vendor);

        UserTokenParms userTokenParms = new UserTokenParms();
        //userTokenParms.setState(State.HOLD_PENDING);
        userTokenParms.setState(State.CURRENT);

        UserTokenActivateCallParms utacp = new UserTokenActivateCallParms();
        utacp.setFilter(userTokenFilter);
        utacp.setParms(userTokenParms);
        utacp.setUserid(userId);

        UserTokenActivateResult utar = adminBinding.userTokenActivate(utacp);
        return utar.getActivationCode();
    }

    /**
     *
     * @param userId
     * @return String serialNo
     * @throws RemoteException
     * @throws ph.gov.bsp.test.exception.TwoFactorException - if result is null
     */
    public String getTokenSerialForUser(String userId) throws RemoteException,
            TwoFactorException {
    	
    	initializeProperties();
    	
        //String vendorId = "Entrust Soft Token";
        String vendorId = props.getVendor();
        String serialNo = "";

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        try {
            UserFilter uf = new UserFilter();
            uf.setTokenStates(new State[]{State.HOLD_PENDING, State.CURRENT});
            uf.setTokenVendorId(vendorId);
            uf.setUserid(userId);

            UserTokenListCallParms utlcp = new UserTokenListCallParms(uf);

            UserTokenListResult utlr = adminBinding.userTokenList(utlcp);

            UserTokenInfo[] uti = utlr.getTokens();

            if (uti != null) {
                if (uti.length > 0) {
                    UserTokenInfo userTokenInfo = uti[0];

                    serialNo = userTokenInfo.getSerialNumber();

                    System.out.println("SERIAL #:" + serialNo);
                }
            } else {
                throw new TwoFactorException("No serial number found for user [" + userId + "]");
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        return serialNo;
    }

    /**
     *
     * @param userId
     * @param state
     * @return String serialNo
     * @throws RemoteException
     * @throws ph.gov.bsp.idg.exception.TwoFactorException
     */
    public String getCurrentTokenSerialForUser(String userId) throws RemoteException,
            TwoFactorException {
    	
    	initializeProperties();
    	
        //String vendor = "Entrust Soft Token";
        String vendorId = props.getVendor();
        String serialNo = "";

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        try {
            UserFilter uf = new UserFilter();
            uf.setTokenStates(new State[]{State.CURRENT});
            uf.setTokenVendorId(vendorId);
            uf.setUserid(userId);

            UserTokenListCallParms utlcp = new UserTokenListCallParms(uf);

            UserTokenListResult utlr = adminBinding.userTokenList(utlcp);

            UserTokenInfo[] uti = utlr.getTokens();

            if (uti != null) {
                if (uti.length > 0) {
                    UserTokenInfo userTokenInfo = uti[0];

                    serialNo = userTokenInfo.getSerialNumber();

                    System.out.println("SERIAL #:" + serialNo);
                }
            } else {
                throw new TwoFactorException("No serial number found for user [" + userId + "]");
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        return serialNo;
    }

    /**
     *
     * @param userId
     * @param serialNumber
     * @param vendor
     * @return activation code
     * @throws RemoteException
     */
    public String getActivationCode(String userId, String serialNumber, String vendor) throws RemoteException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        // The following code changes the state of a user token to PENDING
        UserTokenFilter userTokenFilter = new UserTokenFilter();
        userTokenFilter.setSerialNumber(serialNumber);
        userTokenFilter.setVendorId(vendor);

        UserTokenParms userTokenParms = new UserTokenParms();
        //userTokenParms.setState(State.HOLD_PENDING);
        userTokenParms.setState(State.CURRENT);

        UserTokenActivateCallParms utacp = new UserTokenActivateCallParms();
        utacp.setFilter(userTokenFilter);
        utacp.setParms(userTokenParms);
        utacp.setUserid(userId);

        UserTokenActivateResult utar = adminBinding.userTokenActivate(utacp);
        return utar.getActivationCode();
    }

    public void changeTokenState(String userId, String serialNo, State state) throws RemoteException {
    	
    	initializeProperties();
    	
        //String vendorId = "Entrust Soft Token";
        String vendorId = props.getVendor();

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();

        // The following code changes the state of a user token to PENDING
        UserTokenFilter userTokenFilter = new UserTokenFilter();
        userTokenFilter.setSerialNumber(serialNo);
        userTokenFilter.setVendorId(vendorId);
        UserTokenParms userTokenParms = new UserTokenParms();
        userTokenParms.setState(state);

        UserTokenSetCallParms userTokenSetCallParms = new UserTokenSetCallParms();
        userTokenSetCallParms.setUserid(userId);
        userTokenSetCallParms.setFilter(userTokenFilter);
        userTokenSetCallParms.setParms(userTokenParms);

        adminBinding.userTokenSet(userTokenSetCallParms);
    }

    public void resetToken(String userId, String serialNumber, String tokenVendor) throws RemoteException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AdminServiceBindingStub adminBinding = getAdminBinding();
        // The following code changes the state of a user token to PENDING
        UserTokenFilter userTokenFilter = new UserTokenFilter();
        userTokenFilter.setSerialNumber(serialNumber);
        // if not specified, the token vendor defaults to the default token vendor
        userTokenFilter.setVendorId(tokenVendor);
        UserTokenParms userTokenParms = new UserTokenParms();
        userTokenParms.setState(State.PENDING);

        UserTokenSetCallParms userTokenSetCallParms = new UserTokenSetCallParms();
        userTokenSetCallParms.setUserid(userId);
        userTokenSetCallParms.setFilter(userTokenFilter);
        userTokenSetCallParms.setParms(userTokenParms);
        adminBinding.userTokenSet(userTokenSetCallParms);
    }

    public String getAuthenticationCode(String userId, String vendorId)
            throws RemoteException, ServiceException {
        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));

        AuthenticationServiceBindingStub authBinding = getAuthBinding();

        AuthenticationType authType = AuthenticationType.TOKENRO;

        // Create the generic challenge parameters as follows:
        GenericChallengeParms genericChallengeParms = new GenericChallengeParms();
        genericChallengeParms.setAuthenticationType(authType);
        // If OTP is chosen, assume user has a default delivery mechanism
        genericChallengeParms.setUseDefaultDelivery(Boolean.FALSE);
        // Get the generic challenge
        GetGenericChallengeCallParms callParms = new GetGenericChallengeCallParms();
        callParms.setUserId(userId);
        callParms.setParms(genericChallengeParms);

        // Fill up the authentication parameters and response with user input
        GenericAuthenticateParms authParms = new GenericAuthenticateParms();
        //Response sResponse = new Response();

        // Process the challenge:
        authType = AuthenticationType.TOKENRO;

        GenericChallenge genericChallenge = authBinding.getGenericChallenge(callParms);

        String challenge = "";

        if (authType.equals(AuthenticationType.TOKENCR)) {
            TokenChallenge tokenChallenge = genericChallenge.getTokenChallenge();
            // Get the token challenge
            // For simplicity, we assume the user only has one token
            challenge = "For the challenge " + tokenChallenge.getChallenge() + ", enter your response for the token with serial # " + tokenChallenge.getTokens()[0].getSerialNumber() + ": ";
        } else if (authType.equals(AuthenticationType.TOKENRO)) {
            TokenChallenge tokenChallenge = genericChallenge.getTokenChallenge();
            // For simplicity, we assume user only has one response only token
            challenge = "Enter your response for the token with serial number " + tokenChallenge.getTokens()[0].getSerialNumber() + ": ";
        }

        return challenge;
    }

    public boolean authenticate(String userId, String answer)
            throws ServiceException, RemoteException {
        System.out.println("Answer of user[" + userId + "]=" + answer);

        Response resp = new Response();
        resp.setResponse(new String[]{answer});

        //boolean withFailOver = Boolean.parseBoolean(PropertyLoader.getProperty("idg.fi.failover"));
        AuthenticationServiceBindingStub authBinding = getAuthBinding();

        // Fill up the authentication parameters and response with user input
        GenericAuthenticateParms authParms = new GenericAuthenticateParms();

        authParms.setAuthenticationType(AuthenticationType.TOKENRO);
        AuthenticateGenericChallengeCallParms authCallParms
                = new AuthenticateGenericChallengeCallParms();
        authCallParms.setUserId(userId);
        authCallParms.setParms(authParms);
        authCallParms.setResponse(resp);

        GenericAuthenticateResponse authResponse
                = authBinding.authenticateGenericChallenge(authCallParms);
        String name = authResponse.getFullName();

        return name != null;
    }

    private static boolean ping(String url, int timeout) {
        try (Socket socket = new Socket()) {
            URI uri = new URI(url);

            String host = uri.getHost();
            int port = uri.getPort();

            System.out.println("*****************************************");
            System.out.println(host + " with port " + port);
            System.out.println("*****************************************");

            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
    
	public void initializeProperties() {
		
		props = new TwoFactorAuthenticationProperties();
		try {
			
			props.initialize();
			
			/*LOGGER.info("2FA Properties Retrieved: " + props.getAdmin() + ", " + props.getAuth() + ", " + props.getRedirect() + ", " 
					+ props.getLogin() + ", " + props.getRetries() + ", " + props.getUsername() + ", "
					+ props.getPassword() + ", " + props.getVendor() + ", " + props.getTokens() + ", "
					+ props.getGroup() + ", " + props.getDevtest());*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

