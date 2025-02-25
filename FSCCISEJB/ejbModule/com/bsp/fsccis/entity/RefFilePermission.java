/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.DisplayTag2;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_UserSet;

/**
 * 
 * @author MallillinJG
 */
@Entity
@Table(name = "REF_FILE_PERMISSION", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "RefFilePermission.findAll", query = "SELECT r FROM RefFilePermission r"),
		@NamedQuery(name = "RefFilePermission.findAllOwnerGroupIdAndIsVisible", query = "SELECT r.ownerGroupId.agencyGroupId FROM RefFilePermission r WHERE r.targetGroupId = :targetGroupId AND r.visible = " + RefFilePermission.VISIBLE_TRUE),
		@NamedQuery(name = "RefFilePermission.findByOwnerTargetAndIsVisible", query = "SELECT r FROM RefFilePermission r WHERE r.ownerGroupId = :ownerGroupId AND r.targetGroupId = :targetGroupId AND r.visible = " + RefFilePermission.VISIBLE_TRUE),
		@NamedQuery(name = "RefFilePermission.findByFilePropertyId", query = "SELECT r FROM RefFilePermission r WHERE r.refFilePermissionPK.filePropertyId = :filePropertyId"),
		@NamedQuery(name = "RefFilePermission.findByTargetAgency", query = "SELECT r FROM RefFilePermission r WHERE r.refFilePermissionPK.targetGroupId = :targetGroupId"),
		@NamedQuery(name = "RefFilePermission.findByVisible", query = "SELECT r FROM RefFilePermission r WHERE r.visible = :visible"),
		@NamedQuery(name = "RefFilePermission.findByCdate", query = "SELECT r FROM RefFilePermission r WHERE r.cdate = :cdate"),
		@NamedQuery(name = "RefFilePermission.findByCtime", query = "SELECT r FROM RefFilePermission r WHERE r.ctime = :ctime"),
		@NamedQuery(name = "RefFilePermission.findByCuser", query = "SELECT r FROM RefFilePermission r WHERE r.cuser = :cuser") })
public class RefFilePermission implements Serializable, BSPTimeStampable,
		PKTag, PKTag_UserSet, DisplayTag, DisplayTag2 {

	public static final short VISIBLE_TRUE = 1;
	public static final short VISIBLE_FALSE = 0;

	private static final long serialVersionUID = 1L;
	@EmbeddedId
	protected RefFilePermissionPK refFilePermissionPK;
	@Column()
	private Short visible = VISIBLE_TRUE;
	@Temporal(TemporalType.DATE)
	private Date cdate;
	@Temporal(TemporalType.TIME)
	private Date ctime;
	@Size(max = 15)
	@Column(length = 15)
	private String cuser;
	@JoinColumn(name = "FILE_PROPERTY_ID", referencedColumnName = "FILE_PROPERTY_ID", nullable = false, insertable = false, updatable = false)
	@ManyToOne(optional = false)
	private RefFileProperty refFileProperty;
	@JoinColumn(name = "TARGET_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false, insertable = false, updatable = false)
	@ManyToOne(optional = false)
	private RefAgencyGroup targetGroupId;
    @JoinColumn(name = "OWNER_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup ownerGroupId;

	public RefFilePermission() {
	}

	public RefFilePermission(RefFilePermissionPK refFilePermissionPK) {
		this.refFilePermissionPK = refFilePermissionPK;
	}

	public RefFilePermission(int filePropertyId, int agencyId) {
		this.refFilePermissionPK = new RefFilePermissionPK(filePropertyId,
				agencyId);
	}

	public RefFilePermissionPK getRefFilePermissionPK() {
		return refFilePermissionPK;
	}

	public void setRefFilePermissionPK(RefFilePermissionPK refFilePermissionPK) {
		this.refFilePermissionPK = refFilePermissionPK;
	}

	public Short getVisible() {
		return visible;
	}

	public void setVisible(Short visible) {
		this.visible = visible;
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

	public RefFileProperty getRefFileProperty() {
		return refFileProperty;
	}

	public void setRefFileProperty(RefFileProperty refFileProperty) {
		this.refFileProperty = refFileProperty;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (refFilePermissionPK != null ? refFilePermissionPK.hashCode()
				: 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RefFilePermission)) {
			return false;
		}
		RefFilePermission other = (RefFilePermission) object;
		if ((this.refFilePermissionPK == null && other.refFilePermissionPK != null)
				|| (this.refFilePermissionPK != null && !this.refFilePermissionPK
						.equals(other.refFilePermissionPK))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.bsp.fsccis.entity.RefFilePermission[ refFilePermissionPK="
				+ refFilePermissionPK + " ]";
	}

	public static final int NOT_VISIBLE = 0;
	public static final int VISIBLE = 1;

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		return sb.append("File Permission[ file=")
				.append(refFileProperty.getFileName()).append(", Owner_Agency=")
				.append(ownerGroupId.getAgencyGroupShortname()).append(", Target_Agency=")
				.append(targetGroupId.getAgencyGroupShortname()).append(" ] ")
				.toString();
	}

	@Override
	public Object getPk() {
		return this.refFilePermissionPK;
	}

	public RefAgencyGroup getTargetGroupId() {
		return targetGroupId;
	}

	public void setTargetGroupId(RefAgencyGroup targetGroupId) {
		this.targetGroupId = targetGroupId;
	}

	public RefAgencyGroup getOwnerGroupId() {
		return ownerGroupId;
	}

	public void setOwnerGroupId(RefAgencyGroup ownerGroupId) {
		this.ownerGroupId = ownerGroupId;
	}

}
