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

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "TRUSTED_GROUPS", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TrustedGroups.findAll", query = "SELECT t FROM TrustedGroups t"),
    @NamedQuery(name = "TrustedGroups.findTargetGroupsByOwner", query = "SELECT t.targetGroup FROM TrustedGroups t WHERE t.ownerGroup = :ownerGroup"),
    @NamedQuery(name = "TrustedGroups.findByOwnerGroupId", query = "SELECT t FROM TrustedGroups t WHERE t.trustedGroupsPK.ownerGroupId = :ownerGroup"),
    @NamedQuery(name = "TrustedGroups.findByTargetGroupId", query = "SELECT t FROM TrustedGroups t WHERE t.trustedGroupsPK.targetGroupId = :targetGroupId"),
    @NamedQuery(name = "TrustedGroups.findByCdate", query = "SELECT t FROM TrustedGroups t WHERE t.cdate = :cdate"),
    @NamedQuery(name = "TrustedGroups.findByCtime", query = "SELECT t FROM TrustedGroups t WHERE t.ctime = :ctime"),
    @NamedQuery(name = "TrustedGroups.findByCuser", query = "SELECT t FROM TrustedGroups t WHERE t.cuser = :cuser")})
public class TrustedGroups implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TrustedGroupsPK trustedGroupsPK;
    @Column(name = "CDATE")
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Column(name = "CTIME")
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(name = "CUSER", length = 15)
    private String cuser;
    @JoinColumn(name = "TARGET_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup targetGroup;
    @JoinColumn(name = "OWNER_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RefAgencyGroup ownerGroup;

    public TrustedGroups() {
    }

    public TrustedGroups(TrustedGroupsPK trustedGroupsPK) {
        this.trustedGroupsPK = trustedGroupsPK;
    }

    public TrustedGroups(int ownerGroupId, int targetGroupId) {
        this.trustedGroupsPK = new TrustedGroupsPK(ownerGroupId, targetGroupId);
    }

    public TrustedGroupsPK getTrustedGroupsPK() {
        return trustedGroupsPK;
    }

    public void setTrustedGroupsPK(TrustedGroupsPK trustedGroupsPK) {
        this.trustedGroupsPK = trustedGroupsPK;
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

    public RefAgencyGroup getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(RefAgencyGroup targetGroup) {
        this.targetGroup = targetGroup;
    }

    public RefAgencyGroup getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(RefAgencyGroup ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trustedGroupsPK != null ? trustedGroupsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrustedGroups)) {
            return false;
        }
        TrustedGroups other = (TrustedGroups) object;
        if ((this.trustedGroupsPK == null && other.trustedGroupsPK != null) || (this.trustedGroupsPK != null && !this.trustedGroupsPK.equals(other.trustedGroupsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.TrustedGroups[ trustedGroupsPK=" + trustedGroupsPK + " ]";
    }
    
}
