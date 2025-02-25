package com.bsp.fsccis.util;
/*
 * @(#)ServiceLocator.java
 *
 * Date Created: August 16, 2013
 * Created By: author
 *
 * Last Modified: August 16, 2013
 * Last Modified By: author
 */

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author author
 * @version 1
 */
public class FSCCISServiceLocator implements Serializable {
   
    private static final long   serialVersionUID = 1L;
    private static final String jndiPrefix = "java:global/FSCCIS/FSCCISEJB/";
   
    private static FSCCISServiceLocator instance;
   
    private InitialContext ic;

    private FSCCISServiceLocator() {
        try {
            ic = new InitialContext();
        } catch (NamingException namingErr) { 
            namingErr.printStackTrace();
        }
    }

    public static FSCCISServiceLocator getInstance() {
        return instance == null ? instance = new FSCCISServiceLocator() : instance;
    }

    public Object locateFacade(Class searchClass) {
        Object facadeObject = null;
       
        try {
            facadeObject = ic.lookup(jndiPrefix + searchClass.getSimpleName());
        } catch (NamingException namingErr) { 
            namingErr.printStackTrace();
        }
       
        return facadeObject;
    }
}