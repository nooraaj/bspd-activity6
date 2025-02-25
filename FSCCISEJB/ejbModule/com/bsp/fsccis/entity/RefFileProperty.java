/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;

/**
 * 
 * @author MallillinJG
 */
@Entity
@Table(name = "REF_FILE_PROPERTY", catalog = "", schema = "FSCC-IS", uniqueConstraints = { @UniqueConstraint(columnNames = { "RELATIVE_LOCATION" }) })
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "RefFileProperty.findAll", query = "SELECT r FROM RefFileProperty r"),
		@NamedQuery(name = "RefFileProperty.findByFileNameAndRelativeLocation", query = "SELECT r FROM RefFileProperty r WHERE r.fileName = :fileName AND r.relativeLocation = :relativeLocation"),
		@NamedQuery(name = "RefFileProperty.findByFilePropertyId", query = "SELECT r FROM RefFileProperty r WHERE r.filePropertyId = :filePropertyId"),
		@NamedQuery(name = "RefFileProperty.findByFileType", query = "SELECT r FROM RefFileProperty r WHERE r.fileType = :fileType"),
		@NamedQuery(name = "RefFileProperty.findAllAgencyGroupFolders", query = "SELECT r FROM RefFileProperty r WHERE r.ownerGroupId = :ownerGroupId AND r.fileType = "
				+ RefFileProperty.TYPE_FOLDER),
		@NamedQuery(name = "RefFileProperty.findAllAgencyGroupFiles", query = "SELECT r FROM RefFileProperty r WHERE r.ownerGroupId = :ownerGroupId AND (r.fileType = "
				+ RefFileProperty.TYPE_PDF
				+ " OR r.fileType = "
				+ RefFileProperty.TYPE_PPT
				+ " OR r.fileType = "
				+ RefFileProperty.TYPE_XLS
				+  " OR r.fileType = "
				+ RefFileProperty.TYPE_DOC + ")"),
		@NamedQuery(name = "RefFileProperty.findByFileName", query = "SELECT r FROM RefFileProperty r WHERE r.fileName = :fileName"),
		@NamedQuery(name = "RefFileProperty.findByRelativeLocation", query = "SELECT r FROM RefFileProperty r WHERE r.relativeLocation = :relativeLocation"),
		@NamedQuery(name = "RefFileProperty.findByRelativeLocationAutoUpload", query = "SELECT r FROM RefFileProperty r WHERE r.relativeLocation LIKE :relativeLocation"),
		@NamedQuery(name = "RefFileProperty.findByCdate", query = "SELECT r FROM RefFileProperty r WHERE r.cdate = :cdate"),
		@NamedQuery(name = "RefFileProperty.findByRelativeLocationAndFileName", query = "SELECT r FROM RefFileProperty r WHERE r.relativeLocation = :relativeLocation and  r.fileName = :fileName"),
		@NamedQuery(name = "RefFileProperty.findByLessCdate", query = "SELECT r FROM RefFileProperty r WHERE r.cdate < :cdate AND r.fileName LIKE :fileName AND r.fileType = "+ RefFileProperty.TYPE_XLS),
		@NamedQuery(name = "RefFileProperty.findByCtime", query = "SELECT r FROM RefFileProperty r WHERE r.ctime = :ctime"),
		@NamedQuery(name = "RefFileProperty.findByCuser", query = "SELECT r FROM RefFileProperty r WHERE r.cuser = :cuser"),
		@NamedQuery(name = "RefFileProperty.findByfileTypeAndFileName", query = "SELECT r FROM RefFileProperty r WHERE r.fileType = :fileType AND r.fileName = :fileName"),
		@NamedQuery(name = "RefFileProperty.findByOwnerGroupAndRelativeLocationAndFileName", query = "SELECT r FROM RefFileProperty r WHERE r.ownerGroupId = :ownerGroupId AND r.fileName = :fileName AND r.relativeLocation LIKE :relativeLocation")})
@NamedNativeQueries({
	@NamedNativeQuery(name = "RefFileProperty.findFolders", 
			query = "SELECT * "
			+ "FROM \"FSCC-IS\".REF_FILE_PROPERTY as a where"
			+ " ROUND ( "
			+ "( LENGTH(a.RELATIVE_LOCATION) "
			+ "- LENGTH( REPLACE ( a.RELATIVE_LOCATION, 'Folder','') ) "
			+ ") / LENGTH('Folder') "
			+ ") = ?1 "
			+ "AND "
			+ "a.RELATIVE_LOCATION LIKE ?2 AND a.FILE_TYPE =" + RefFileProperty.TYPE_FOLDER
			+ " ORDER BY CDATE, CTIME")})
public class RefFileProperty implements Serializable, BSPTimeStampable,
		PKTag_GetMax<Integer>, DisplayTag, PKTag {
	
	public static final String ROOT_AGENCY_GROUP_FOLDER_NAME_PREFIX = "FOLDER ";

	public static final short TYPE_FOLDER = 1;
	public static final short TYPE_PDF = 2;
	public static final short TYPE_XLS = 3;
	public static final short TYPE_PPT = 4;
	public static final short TYPE_DOC = 5;

	private static final long serialVersionUID = 1L;
	@Id
	@Basic(optional = false)
	@NotNull
	
	@Column(name = "FILE_PROPERTY_ID", nullable = false)
	private Integer filePropertyId;
	@Basic(optional = false)
	@NotNull
	@Column(name = "FILE_TYPE", nullable = false)
	private short fileType;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 50)
	@Column(name = "FILE_NAME", nullable = false, length = 50)
	private String fileName;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 1000)
	@Column(name = "RELATIVE_LOCATION", nullable = false, length = 1000)
	private String relativeLocation;
	@Temporal(TemporalType.DATE)
	private Date cdate;
	@Temporal(TemporalType.TIME)
	private Date ctime;
	@Size(max = 15)
	@Column(length = 15)
	private String cuser;
	@OneToMany(mappedBy = "refFileProperty")
	private List<RefFilePermission> refFilePermissionList;
    @JoinColumn(name = "OWNER_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup ownerGroupId;

	@Transient
	private String size;

	public RefFileProperty() {
	}

	public RefFileProperty(Integer filePropertyId) {
		this.filePropertyId = filePropertyId;
	}

	public RefFileProperty(Integer filePropertyId, short fileType,
			String fileName, String relativeLocation) {
		this.filePropertyId = filePropertyId;
		this.fileType = fileType;
		this.fileName = fileName;
		this.relativeLocation = relativeLocation;
	}

	public Integer getFilePropertyId() {
		return filePropertyId;
	}

	public void setFilePropertyId(Integer filePropertyId) {
		this.filePropertyId = filePropertyId;
	}

	public short getFileType() {
		return fileType;
	}

	public void setFileType(short fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRelativeLocation() {
		return relativeLocation;
	}

	public void setRelativeLocation(String relativeLocation) {
		this.relativeLocation = relativeLocation;
	}

	public Date getCdate() {
		return cdate;
	}

	public void setCdate(Date cdate) {
		this.cdate = cdate;
	}

	public Date getCtime() {
		return ctime;
	}

	public void setCtime(Date ctime) {
		this.ctime = ctime;
	}

	public String getCuser() {
		return cuser;
	}

	public void setCuser(String cuser) {
		this.cuser = cuser;
	}

	@XmlTransient
	public List<RefFilePermission> getRefFilePermissionList() {
		return refFilePermissionList;
	}

	public void setRefFilePermissionList(
			List<RefFilePermission> refFilePermissionList) {
		this.refFilePermissionList = refFilePermissionList;
	}

    public RefAgencyGroup getOwnerGroupId() {
        return ownerGroupId;
    }

    public void setOwnerGroupId(RefAgencyGroup ownerGroupId) {
        this.ownerGroupId = ownerGroupId;
    }

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (filePropertyId != null ? filePropertyId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RefFileProperty)) {
			return false;
		}
		RefFileProperty other = (RefFileProperty) object;
		if ((this.filePropertyId == null && other.filePropertyId != null)
				|| (this.filePropertyId != null && !this.filePropertyId
						.equals(other.filePropertyId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.bsp.fsccis.entity.RefFileProperty[ filePropertyId="
				+ filePropertyId + " ] " + this.relativeLocation;
	}

	@Override
	public Object getPk() {
		return this.filePropertyId;
	}

	@Override
	public String getDisplayName() {
		String fileTypeString = this.fileType == TYPE_FOLDER ? "FOLDER"
				: this.fileType == TYPE_PDF ? "PDF"
				: this.fileType == TYPE_PPT ? "PPT"
				: this.fileType == TYPE_XLS ? "XLS"
				: this.fileType == TYPE_DOC ? "DOC"
				: "UNSUPPORTED";
		StringBuilder sb = new StringBuilder();

		return sb.append("TYPE: ").append(fileTypeString)
//				.append(", LOCATION: ").append(this.relativeLocation)
				.append(", DISPLAY NAME: ").append(this.fileName).toString();
	}

	@Override
	public void setIdToGenerate(Integer id) {
		this.filePropertyId = id;
	}

	@Override
	public String getPKColumnName() {
		return "FILE_PROPERTY_ID";
	}

	@Override
	public String getTableName() {
		return "REF_FILE_PROPERTY";
	}

	@Override
	public Integer getDefaultId() {
		return 1;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
