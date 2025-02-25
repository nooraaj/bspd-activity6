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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
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
@Table(name = "REF_AGENCY_GROUP", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "RefAgencyGroup.findAllFromListNoDisabled", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyGroupId IN :agencyGroupId AND r.accessLevel <> " + RefAgencyGroup.DISABLED),
    @NamedQuery(name = "RefAgencyGroup.findAllNoDisabled", query = "SELECT r FROM RefAgencyGroup r WHERE r.accessLevel <> " + RefAgencyGroup.DISABLED),
    @NamedQuery(name = "RefAgencyGroup.findAll", query = "SELECT r FROM RefAgencyGroup r"),
    @NamedQuery(name = "RefAgencyGroup.findAllAgencyGroupShortname", query = "SELECT r.agencyGroupShortname FROM RefAgencyGroup r ORDER BY r.agencyGroupShortname"),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyId", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyId = :agencyId"),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyIdNoDisabled", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyId = :agencyId AND r.accessLevel <> " + RefAgencyGroup.DISABLED ),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyGroupId", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyGroupId = :agencyGroupId"),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyName", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyGroupName = :agencyName"),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyGroupShortname", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyGroupShortname = :agencyGroupShortname"),
    @NamedQuery(name = "RefAgencyGroup.findByCdate", query = "SELECT r FROM RefAgencyGroup r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefAgencyGroup.findByCtime", query = "SELECT r FROM RefAgencyGroup r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefAgencyGroup.findByCuser", query = "SELECT r FROM RefAgencyGroup r WHERE r.cuser = :cuser"),
    @NamedQuery(name = "RefAgencyGroup.findByAgencyGroupShortnameAdmin", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyGroupShortname = :agencyGroupShortname AND r.accessLevel =" + RefAgencyGroup.SYSTEM_OWNER),
	@NamedQuery(name = "RefAgencyGroup.findByAgencyIdandAgencyGroupShortname", query = "SELECT r FROM RefAgencyGroup r WHERE r.agencyId = :agencyId AND r.agencyGroupShortname = :agencyGroupShortname")})
public class RefAgencyGroup implements Serializable,BSPTimeStampable,PKTag_GetMax<Integer>,DisplayTag,PKTag {
	
	public static final int REGULAR = 0;
	public static final int SYSTEM_OWNER = 1;
	public static final int DISABLED = -1;
	
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "AGENCY_GROUP_ID", nullable = false)
    private Integer agencyGroupId;
    @Size(max = 100)
    @Column(name = "AGENCY_GROUP_NAME", length = 100)
    private String agencyGroupName;
    @Size(max = 50)
    @Column(name = "AGENCY_GROUP_SHORTNAME", length = 50)
    private String agencyGroupShortname;
    @Column(name = "ACCESS_LEVEL")
    private Integer accessLevel = REGULAR;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "refAgencyGroupId")
    private List<RefUserAccounts> refUserAccountList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "targetGroupId")
    private List<RefFilePermission> refFilePermissionList;
    

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "targetGroupId")
    private List<RefAgencyGroupFolderVisible> refAgencyFolderVisibleList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ownerGroupId")
    private List<RefAgencyGroupFolderVisible> refAgencyFolderVisibleList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ownerGroupId")
    private List<RefFileProperty> refFilePropertyList;

    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @ManyToOne
    private RefAgency agencyId;

    @Transient
    private List<String> permissionList;

    public RefAgencyGroup() {
    }

    public RefAgencyGroup(Integer agencyId) {
        this.agencyGroupId = agencyId;
    }

    public Integer getAgencyGroupId() {
        return agencyGroupId;
    }

    public void setAgencyGroupId(Integer agencyGroupId) {
        this.agencyGroupId = agencyGroupId;
    }

    public String getAgencyGroupName() {
        return agencyGroupName;
    }

    public void setAgencyGroupName(String agencyGroupName) {
        this.agencyGroupName = agencyGroupName;
    }

    public String getAgencyGroupShortname() {
        return agencyGroupShortname;
    }

    public void setAgencyGroupShortname(String agencyGroupShortname) {
        this.agencyGroupShortname = agencyGroupShortname;
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
    public List<RefAgencyGroupFolderVisible> getRefAgencyFolderVisibleList() {
        return refAgencyFolderVisibleList;
    }

    public void setRefAgencyFolderVisibleList(List<RefAgencyGroupFolderVisible> refAgencyFolderVisibleList) {
        this.refAgencyFolderVisibleList = refAgencyFolderVisibleList;
    }

    @XmlTransient
    public List<RefAgencyGroupFolderVisible> getRefAgencyFolderVisibleList1() {
        return refAgencyFolderVisibleList1;
    }

    public void setRefAgencyFolderVisibleList1(List<RefAgencyGroupFolderVisible> refAgencyFolderVisibleList1) {
        this.refAgencyFolderVisibleList1 = refAgencyFolderVisibleList1;
    }

    @XmlTransient
    public List<RefFileProperty> getRefFilePropertyList() {
        return refFilePropertyList;
    }

    public void setRefFilePropertyList(List<RefFileProperty> refFilePropertyList) {
        this.refFilePropertyList = refFilePropertyList;
    }

    @XmlTransient
    public List<RefUserAccounts> getRefUserAccountList() {
        return refUserAccountList;
    }

    public void setRefUserAccountList(List<RefUserAccounts> refUserAccountList) {
        this.refUserAccountList = refUserAccountList;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (agencyGroupId != null ? agencyGroupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RefAgencyGroup)) {
            return false;
        }
        RefAgencyGroup other = (RefAgencyGroup) object;
        if ((this.agencyGroupId == null && other.agencyGroupId != null) || (this.agencyGroupId != null && !this.agencyGroupId.equals(other.agencyGroupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefAgency[ agencyId=" + agencyGroupId + " ]";
    }
    
    public String toTitleString(){
    	return this.agencyId.getAgencyShortname().concat(" - ").concat(this.agencyGroupShortname);
    }

	@Override
	public Object getPk() {
		return this.agencyGroupId;
	}

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		return sb.append(this.agencyGroupName).append(" (").append(this.agencyGroupShortname).append("): ")
				.append(accessLevel == SYSTEM_OWNER ? " System Owner " :
						accessLevel == DISABLED ? "Disabled" : "Regular").toString();
	}

	@Override
	public void setIdToGenerate(Integer id) {
		this.agencyGroupId = id;
	}

	@Override
	public String getPKColumnName() {
		return "AGENCY_GROUP_ID";
	}

	@Override
	public String getTableName() {
		return "REF_AGENCY_GROUP";
	}

	@Override
	public Integer getDefaultId() {
		return 1;
	}

	public List<RefFilePermission> getRefFilePermissionList() {
		return refFilePermissionList;
	}

	public void setRefFilePermissionList(List<RefFilePermission> refFilePermissionList) {
		this.refFilePermissionList = refFilePermissionList;
	}

	public List<String> getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(List<String> permissionList) {
		this.permissionList = permissionList;
	}

	public Integer getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(Integer accessLevel) {
		this.accessLevel = accessLevel;
	}

	public RefAgency getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(RefAgency agencyId) {
		this.agencyId = agencyId;
	}
    
}
