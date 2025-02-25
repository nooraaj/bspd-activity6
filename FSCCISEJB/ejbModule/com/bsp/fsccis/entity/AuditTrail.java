/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.LazyTag;
import com.bsp.fsccis.entity.tag.PKTag_AutoGen;


/**
 *
 * @author mallillinjg
 */
@Entity
@Table(name = "AUDIT_TRAIL", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AuditTrail.findAll", query = "SELECT c FROM AuditTrail c"),
    @NamedQuery(name = "AuditTrail.findByAuditTrailId", query = "SELECT c FROM AuditTrail c WHERE c.auditTrailId = :auditTrailId"),
    @NamedQuery(name = "AuditTrail.findByTable", query = "SELECT c FROM AuditTrail c WHERE c.table = :table"),
    @NamedQuery(name = "AuditTrail.findByAction", query = "SELECT c FROM AuditTrail c WHERE c.action = :action"),
    @NamedQuery(name = "AuditTrail.findByDetails", query = "SELECT c FROM AuditTrail c WHERE c.details = :details"),
    @NamedQuery(name = "AuditTrail.findByCdate", query = "SELECT c FROM AuditTrail c WHERE c.cdate = :cdate"),
    @NamedQuery(name = "AuditTrail.findByCtime", query = "SELECT c FROM AuditTrail c WHERE c.ctime = :ctime"),
    @NamedQuery(name = "AuditTrail.findByCuser", query = "SELECT c FROM AuditTrail c WHERE c.cuser = :cuser")})
public class AuditTrail implements Serializable,PKTag_AutoGen,BSPTimeStampable,LazyTag,DisplayTag {
	public static final int ACTION_ADD = 0;
	public static final int ACTION_EDIT = 1;
	public static final int ACTION_VIEW = 2;
	public static final int ACTION_LOGIN = 3;
	public static final int ACTION_LOGOUT = 4;
	public static final int ACTION_DOWNLOAD_FILE = 5;
	public static final int ACTION_DELETE = 6;
	
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_TRAIL_ID", nullable = false)
    private Long auditTrailId;
    @NotNull
    @Size(min = 1, max = 75)
    @Column(nullable = false, length = 75)
    private String table;
    @NotNull
    @Column()
    private Integer action;
    @Size(max = 2000)
    @Column(length = 2000)
    private String details;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @JoinColumn(name = "AGENCY_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID")
    @ManyToOne
    private RefAgencyGroup agencyGroupId;
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @ManyToOne
    private RefAgency agencyId;

    public AuditTrail() {
    }

    public AuditTrail(String table, Integer action) {
        this.table = table;
        this.action = action;
    }

    public Long getAuditTrailId() {
        return auditTrailId;
    }

    public void setAuditTrailId(Long auditTrailId) {
        this.auditTrailId = auditTrailId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (auditTrailId != null ? auditTrailId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AuditTrail)) {
            return false;
        }
        AuditTrail other = (AuditTrail) object;
        if ((this.auditTrailId == null && other.auditTrailId != null) || (this.auditTrailId != null && !this.auditTrailId.equals(other.auditTrailId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
//        return "com.bsp.fsccis.entity.AuditTrail[ auditTrailId=" + auditTrailId + " ]";
    	return this.table;
    }

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		return sb.append("AGENCY: ").append(this.agencyGroupId.getAgencyGroupShortname()).append(", TABLE: ").append(this.table).append(", ACTION: ").append(this.action).append(", DETAILS: ").append(this.details).toString();
	}

	@Override
	public List<String> pkColumnNames() {
		return Arrays.asList(new String[]{"AUDIT_TRAIL_ID"});
	}


    public RefAgencyGroup getAgencyGroupId() {
        return agencyGroupId;
    }

    public void setAgencyGroupId(RefAgencyGroup agencyGroupId) {
        this.agencyGroupId = agencyGroupId;
    }

	public RefAgency getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(RefAgency agencyId) {
		this.agencyId = agencyId;
	}
}
