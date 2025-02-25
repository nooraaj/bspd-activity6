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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.DisplayTag2;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_UserSet;

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "REF_AGENCY_GROUP_FOLDER_VISIBLE", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findAll", query = "SELECT r FROM RefAgencyGroupFolderVisible r"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByOwnerAgencyGroup", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.ownerGroupId = :ownerGroupId"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByTargetAgencyAndIsFolderVisible", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.targetGroupId = :targetGroupId AND r.folderVisible = " + RefAgencyGroupFolderVisible.VISIBLE),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByOwnerAgencyGroupTargetAgency", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.ownerGroupId = :ownerGroupId AND r.targetGroupId = :targetGroupId"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByFolderVisible", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.folderVisible = :folderVisible"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByCdate", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByCtime", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefAgencyGroupFolderVisible.findByCuser", query = "SELECT r FROM RefAgencyGroupFolderVisible r WHERE r.cuser = :cuser")})
public class RefAgencyGroupFolderVisible implements Serializable,BSPTimeStampable,PKTag_UserSet,DisplayTag2,PKTag {

    public static final short NOT_VISIBLE = 0;
    public static final short VISIBLE = 1;
	
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected RefAgencyGroupFolderVisiblePK refAgencyGroupFolderVisiblePK;
    @NotNull
    @Column(name = "FOLDER_VISIBLE", nullable = false)
    private short folderVisible = NOT_VISIBLE;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @JoinColumn(name = "TARGET_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup targetGroupId;
    @JoinColumn(name = "OWNER_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup ownerGroupId;

    public RefAgencyGroupFolderVisible() {
    }

    public RefAgencyGroupFolderVisible(RefAgencyGroupFolderVisiblePK refAgencyGroupFolderVisiblePK) {
        this.refAgencyGroupFolderVisiblePK = refAgencyGroupFolderVisiblePK;
    }

    public RefAgencyGroupFolderVisible(RefAgencyGroupFolderVisiblePK refAgencyGroupFolderVisiblePK, short folderVisible) {
        this.refAgencyGroupFolderVisiblePK = refAgencyGroupFolderVisiblePK;
        this.folderVisible = folderVisible;
    }

    public RefAgencyGroupFolderVisible(int ownerAgency, int targetAgency) {
        this.refAgencyGroupFolderVisiblePK = new RefAgencyGroupFolderVisiblePK(ownerAgency, targetAgency);
    }

	public RefAgencyGroupFolderVisiblePK getRefAgencyGroupFolderVisiblePK() {
		return this.refAgencyGroupFolderVisiblePK;
	}

	public void setRefAgencyGroupFolderVisiblePK(RefAgencyGroupFolderVisiblePK refAgencyGroupFolderVisiblePK) {
		this.refAgencyGroupFolderVisiblePK = refAgencyGroupFolderVisiblePK;
	}

    public short getFolderVisible() {
        return folderVisible;
    }

    public void setFolderVisible(short folderVisible) {
        this.folderVisible = folderVisible;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (refAgencyGroupFolderVisiblePK != null ? refAgencyGroupFolderVisiblePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RefAgencyGroupFolderVisible)) {
            return false;
        }
        RefAgencyGroupFolderVisible other = (RefAgencyGroupFolderVisible) object;
        if ((this.refAgencyGroupFolderVisiblePK == null && other.refAgencyGroupFolderVisiblePK != null) || (this.refAgencyGroupFolderVisiblePK != null && !this.refAgencyGroupFolderVisiblePK.equals(other.refAgencyGroupFolderVisiblePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefAgencyGroupFolderVisible[ refAgencyGroupFolderVisiblePK=" + refAgencyGroupFolderVisiblePK + " ]";
    }

	@Override
	public Object getPk() {
		return this.refAgencyGroupFolderVisiblePK;
	}

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		return sb.append("AGENCY_FOLDER_VISIBLE: [").append(ownerGroupId.getAgencyGroupShortname()).append("] Folders ").append((this.folderVisible == VISIBLE ? " VISIBLE to [":"NOT VISIBLE to [")).append(targetGroupId.getAgencyGroupShortname()).append("] Users").toString();
	}

}
