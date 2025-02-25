/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author mallillinjg
 */
@Embeddable
public class RefUserRolePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "USER_ID", nullable = false)
    private long userId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ROLE_ID", nullable = false)
    private int roleId;

    public RefUserRolePK() {
    }

    public RefUserRolePK(long userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) userId;
        hash += (int) roleId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RefUserRolePK)) {
            return false;
        }
        RefUserRolePK other = (RefUserRolePK) object;
        if (this.userId != other.userId) {
            return false;
        }
        if (this.roleId != other.roleId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefUserRolePK[ userId=" + userId + ", roleId=" + roleId + " ]";
    }
    
}
