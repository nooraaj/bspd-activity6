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

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "SYS_ROLE", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SysRole.findAll", query = "SELECT r FROM SysRole r"),
    @NamedQuery(name = "SysRole.findByRoleId", query = "SELECT r FROM SysRole r WHERE r.roleId = :roleId"),
    @NamedQuery(name = "SysRole.findByRoleName", query = "SELECT r FROM SysRole r WHERE r.roleName = :roleName"),
    @NamedQuery(name = "SysRole.findByCdate", query = "SELECT r FROM SysRole r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "SysRole.findByCtime", query = "SELECT r FROM SysRole r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "SysRole.findByCuser", query = "SELECT r FROM SysRole r WHERE r.cuser = :cuser"),
    @NamedQuery(name = "SysRole.findNotRoleId", query = "SELECT r FROM SysRole r WHERE r.roleId <> :roleId")})
public class SysRole implements Serializable, BSPTimeStampable,DisplayTag {
	
	public static final int ACCOUNT_MANAGER = 2;
	public static final int SYSTEM_ADMIN = 1;
	public static final int NON_ADMIN = 3;
	
	public static final String ACCOUNT_MANAGER_NAME = "Account Manager";
	public static final String AGENCY_FILE_ADMIN = "Agency File Admin";
	public static final String AGENCY_FILE_UPLOADER = "Uploader";
	public static final String USER = "Viewer";

	private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ROLE_ID", nullable = false)
    private Integer roleId;
    @Size(max = 20)
    @Column(name = "ROLE_NAME", length = 20)
    private String roleName;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sysRoles")
    private List<RefUserRole> refUserRoleList;

    public SysRole() {
    }

    public SysRole(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
    public List<RefUserRole> getRefUserRoleList() {
        return refUserRoleList;
    }

    public void setRefUserRoleList(List<RefUserRole> refUserRoleList) {
        this.refUserRoleList = refUserRoleList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (roleId != null ? roleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SysRole)) {
            return false;
        }
        SysRole other = (SysRole) object;
        if ((this.roleId == null && other.roleId != null) || (this.roleId != null && !this.roleId.equals(other.roleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccus.entity.SysRoles[ roleId=" + roleId + " ]";
    } 

	@Override
	public String getDisplayName() {
		return this.roleName;
	}
}
