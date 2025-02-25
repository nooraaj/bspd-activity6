package com.bsp.fsccis.bean.util;

import java.io.File;

public class FileObject extends File{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String displayName;
	
	public FileObject(String pathname, String displayName) {
		super(pathname);
		this.displayName = displayName;
	}

	public FileObject(File fileEntry, String displayName) {
		super(fileEntry.getPath());
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	
}
