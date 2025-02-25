/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.bean.util;

/**
 *
 * @author PULUMBARITDS
 */
public class TwoFactorException extends Exception {

    /**
     * Creates a new instance of <code>TwoFactorException</code> without detail
     * message.
     */
    public TwoFactorException() {
    }

    /**
     * Constructs an instance of <code>TwoFactorException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TwoFactorException(String msg) {
        super(msg);
    }
}
