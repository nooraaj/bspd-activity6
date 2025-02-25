package com.bsp.frprr.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "FRPRR_TRAN_FILE", catalog = "", schema = "FRPRR")
@XmlRootElement
@NamedNativeQueries({
	@NamedNativeQuery(name = "FPRRTransFile.findPath", 
			query = "SELECT TRANS_ID,TRANS_FILE_ID, FILE_NAME, FILE_PATH "
			+ "FROM FRPRR.FRPRR_TRAN_FILE WHERE TRANS_ID IN ( "
			+ "SELECT DISTINCT A.TRANS_ID FROM FRPRR.FRPRR_TRANSACTION A "
			+ "WHERE "
			+ "FILE_PATH LIKE ?1 "
			+ "AND A.LOG_TIME = ("
			+ "SELECT  MAX(LOG_TIME) "
			+ "FROM FRPRR.FRPRR_TRANSACTION "
			+ "WHERE  TRANS_STATUS = A.TRANS_STATUS "
			+ "AND FI_CODE = A.FI_CODE AND TRDATE = A.TRDATE "
			+ "GROUP BY TRANS_STATUS,FI_CODE, TRDATE) "
			+ "AND A.TRANS_STATUS = 5) AND DATE(LOG_TIME) >= ?2 AND DATE(LOG_TIME) <= ?3"
			+ "ORDER BY TRANS_ID")})

public class FRPFilePath implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Basic(optional = false)
	@NotNull
	@Column(name = "TRANS_ID", nullable = false)
	private Integer transId;
	@Id
	@Basic(optional = false)
	@NotNull
	@Column(name = "TRANS_FILE_ID", nullable = false)
	private Integer transFileId;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 500)
	@Column(name = "FILE_NAME", nullable = false, length = 500)
	private String fileName;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 1000)
	@Column(name = "FILE_PATH", nullable = false, length = 1000)
	private String filePath;
	
	@Column(name = "LOG_TIME", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date logTime;
	
    @Column(name="VERSION_ID")
    private Integer versionId;

	public Integer getTransId() {
		return transId;
	}

	public void setTransId(Integer transId) {
		this.transId = transId;
	}

	public Integer getTransFileId() {
		return transFileId;
	}

	public void setTransFileId(Integer transFileId) {
		this.transFileId = transFileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}
}
