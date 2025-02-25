/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MallillinJG
 */
public class SHA256_UTF8_SALT {

    public static String encrypt(String passwd) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(passwd.getBytes("UTF-8"));
            for (int i = 0; i < hash.length; i++) {
                stringBuilder.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException ex) {
            return "";
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SHA256_UTF8_SALT.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringBuilder.toString();
    }
}
