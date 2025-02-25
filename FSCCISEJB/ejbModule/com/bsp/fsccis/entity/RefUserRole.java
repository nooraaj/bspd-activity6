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

import com.bsp.fsccis.entity.tag.AbstractCrudEntity;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.CRUDTag;
import com.bsp.fsccis.entity.tag.DisplayTag2;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_UserSet;
import com.bsp.fsccis.entity.tag.U_Enabled_Tag;

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "REF_USER_ROLE", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RefUserRole.findAll", query = "SELECT r FROM RefUserRole r"),
    @NamedQuery(name = "RefUserRole.findByUserId", query = "SELECT r FROM RefUserRole r WHERE r.refUserRolePK.userId = :userId"),
    @NamedQuery(name = "RefUserRole.findByRoleIdEnabled", query = "SELECT r FROM RefUserRole r WHERE r.refUserRolePK.roleId = :roleId and r.uEnabled = " + RefUserRole.ENABLED),
    @NamedQuery(name = "RefUserRole.findByRoleId", query = "SELECT r FROM RefUserRole r WHERE r.refUserRolePK.roleId = :roleId"),
    @NamedQuery(name = "RefUserRole.findByUEnabled", query = "SELECT r FROM RefUserRole r WHERE r.uEnabled = :uEnabled"),
    @NamedQuery(name = "RefUserRole.findByCdate", query = "SELECT r FROM RefUserRole r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefUserRole.findByCtime", query = "SELECT r FROM RefUserRole r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefUserRole.findByCuser", query = "SELECT r FROM RefUserRole r WHERE r.cuser = :cuser")})
public class RefUserRole extends AbstractCrudEntity implements Serializable,PKTag_UserSet,BSPTimeStampable,CRUDTag,DisplayTag2,PKTag, U_Enabled_Tag {

	private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected RefUserRolePK refUserRolePK;
    @Column(name = "U_ENABLED")
    private Short uEnabled = ENABLED;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RefUserAccounts refUserAccounts;
    @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private SysRole sysRoles;

    public RefUserRole() {
    }

    public RefUserRole(RefUserRolePK refUserRolePK) {
        this.refUserRolePK = refUserRolePK;
    }

    public RefUserRole(long userId, int roleId) {
        this.refUserRolePK = new RefUserRolePK(userId, roleId);
    }

    public RefUserRolePK getRefUserRolePK() {
        return refUserRolePK;
    }

    public void setRefUserRolePK(RefUserRolePK refUserRolePK) {
        this.refUserRolePK = refUserRolePK;
    }

    public Short getUEnabled() {
        return uEnabled;
    }

    public void setUEnabled(Short uEnabled) {
        this.uEnabled = uEnabled;
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

    public RefUserAccounts getRefUserAccounts() {
        return refUserAccounts;
    }

    public void setRefUserAccounts(RefUserAccounts refUserAccounts) {
        this.refUserAccounts = refUserAccounts;
    }

    public SysRole getSysRoles() {
        return sysRoles;
    }

    public void setSysRoles(SysRole sysRoles) {
        this.sysRoles = sysRoles;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (refUserRolePK != null ? refUserRolePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RefUserRole)) {
            return false;
        }
        RefUserRole other = (RefUserRole) object;
        if ((this.refUserRolePK == null && other.refUserRolePK != null) || (this.refUserRolePK != null && !this.refUserRolePK.equals(other.refUserRolePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefUserRole[ refUserRolePK=" + refUserRolePK + " ]";
    }

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		sb.append(refUserAccounts.getDisplayName()).append("-");
		sb.append("ROLE:").append(sysRoles.getDisplayName()).append(";");
		return sb.toString();
	}

	@Override
	public Object getPk() {
		return refUserRolePK;
	}
}
