/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
@Table(name = "REF_AGENCY", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RefAgency.findAll", query = "SELECT r FROM RefAgency r"),
    @NamedQuery(name = "RefAgency.findAllAgencyShortname", query = "SELECT r.agencyShortname FROM RefAgency r ORDER BY r.agencyShortname"),
    @NamedQuery(name = "RefAgency.findAllOrderByNm", query = "SELECT r FROM RefAgency r ORDER BY r.agencyShortname"),
    @NamedQuery(name = "RefAgency.findByAgencyId", query = "SELECT r FROM RefAgency r WHERE r.agencyId = :agencyId"),
    @NamedQuery(name = "RefAgency.findByAgencyName", query = "SELECT r FROM RefAgency r WHERE r.agencyName = :agencyName"),
    @NamedQuery(name = "RefAgency.findByAgencyShortname", query = "SELECT r FROM RefAgency r WHERE r.agencyShortname = :agencyShortname"),
    @NamedQuery(name = "RefAgency.findByCdate", query = "SELECT r FROM RefAgency r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefAgency.findByCtime", query = "SELECT r FROM RefAgency r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefAgency.findByCuser", query = "SELECT r FROM RefAgency r WHERE r.cuser = :cuser"),
    @NamedQuery(name = "RefAgency.findByAgencyShortnameFRP", query = "SELECT r FROM RefAgency r WHERE r.agencyShortname IN ( ?1, ?2 )")})
public class RefAgency implements Serializable,BSPTimeStampable,PKTag_GetMax<Integer>,DisplayTag,PKTag {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "AGENCY_ID", nullable = false)
    private Integer agencyId;
    @Size(max = 100)
    @Column(name = "AGENCY_NAME", length = 100)
    private String agencyName;
    @Size(max = 50)
    @Column(name = "AGENCY_SHORTNAME", length = 50)
    private String agencyShortname;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @OneToMany(mappedBy = "agencyId")
    private List<RefAgencyGroup> refAgencyGroupList;
    @OneToMany(mappedBy = "agencyId")
    private List<RefUserAccounts> refUserAccountsList;

    public RefAgency() {
    }

    public RefAgency(Integer agencyId) {
        this.agencyId = agencyId;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencyShortname() {
        return agencyShortname;
    }

    public void setAgencyShortname(String agencyShortname) {
        this.agencyShortname = agencyShortname;
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
    public List<RefAgencyGroup> getRefAgencyGroupList() {
        return refAgencyGroupList;
    }

    public void setRefAgencyGroupList(List<RefAgencyGroup> refAgencyGroupList) {
        this.refAgencyGroupList = refAgencyGroupList;
    }

    @XmlTransient
    public List<RefUserAccounts> getRefUserAccountsList() {
        return refUserAccountsList;
    }

    public void setRefUserAccountsList(List<RefUserAccounts> refUserAccountsList) {
        this.refUserAccountsList = refUserAccountsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (agencyId != null ? agencyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RefAgency)) {
            return false;
        }
        RefAgency other = (RefAgency) object;
        if ((this.agencyId == null && other.agencyId != null) || (this.agencyId != null && !this.agencyId.equals(other.agencyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefAgency[ agencyId=" + agencyId + " ]";
    }

	@Override
	public Object getPk() {
		return this.agencyId;
	}

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		return sb.append(this.agencyName).append("(").append(this.agencyShortname).append("): ").toString();
	}

	@Override
	public void setIdToGenerate(Integer id) {
		this.agencyId = id;
	}

	@Override
	public String getPKColumnName() {
		return "AGENCY_ID";
	}

	@Override
	public String getTableName() {
		return "REF_AGENCY";
	}

	@Override
	public Integer getDefaultId() {
		return 1;
	}
    
}
