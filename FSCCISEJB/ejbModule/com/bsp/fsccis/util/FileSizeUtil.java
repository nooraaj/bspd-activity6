package com.bsp.fsccis.util;

import java.text.DecimalFormat;

public class FileSizeUtil {
	
	public static final String formatSizeToString(long filesize){
		//source impl: http://stackoverflow.com/a/5599842/2731186
		if(filesize <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(filesize)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(filesize/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
