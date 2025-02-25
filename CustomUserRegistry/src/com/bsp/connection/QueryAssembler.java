/**
 * Filename:    QueryAssembler.java
 * Description:
 *
 * Created:  Oct 1, 2010, 3:31:16 PM
 * Creator:  PULUMBARITDS
 * Modified:
 * Modifier:
 * Remarks:  [Please create a short description of what you modified]
 *
 */

package com.bsp.connection;


public class QueryAssembler {
    public static String parse(String[] arg0) {
        String result = "";

        for (int i = 0; i < arg0.length; i++) {
            result += arg0[i];

            if(i < (arg0.length -1)) {
                result += ", ";
            }
        }

        return result;
    }

    public static String parse(Object[] arg0) {
        String result = "";

        for (int i = 0; i < arg0.length; i++) {
            if(arg0[i] == null) {
                result += "'null'";
            } else {
                if(arg0[i] instanceof Integer) {
                    result += arg0[i];
                } else if(arg0[i] instanceof Double) {
                    result += arg0[i];
                } else if(arg0[i] instanceof Float) {
                    result += arg0[i];
                } else if(arg0[i] instanceof Long) {
                    result += arg0[i];
                } else if(arg0[i] instanceof Boolean) {
                    result += arg0[i];
                } else {
                    result += "'" + arg0[i].toString().replaceAll("'", "''") + "'";
                }
            }

            if(i < (arg0.length -1)) {
                result += ", ";
            }
        }

        return result;
    }

    public static String[] toArray(String arg0) {
        return arg0.split(", ");
    }

    public static String toUpdateString(String[] arg0) {
        String result = "";

        for (int i = 0; i < arg0.length; i++) {
            result += arg0[i] + " = ?";

            if(i < (arg0.length -1)) {
                result += ", ";
            }
        }

        return result;
    }

    public static String toWhereString(String[] arg0) {
        String result = "";

        for (int i = 0; i < arg0.length; i++) {
            result += arg0[i] + " = ?";

            if(i < (arg0.length -1)) {
                result += " AND ";
            }
        }

        return result;
    }

    public static String toWhereString(String[] arg0, boolean like) {
        String result = "";

        for (int i = 0; i < arg0.length; i++) {
            if(like) {
                result += arg0[i] + " LIKE ?";
            } else {
                result += arg0[i] + " = ?";
            }

            if(i < (arg0.length -1)) {
                result += " AND ";
            }
        }

        return result;
    }
}
