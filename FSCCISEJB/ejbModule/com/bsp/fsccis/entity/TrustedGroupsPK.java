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
 * @author MallillinJG
 */
@Embeddable
public class TrustedGroupsPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "OWNER_GROUP_ID", nullable = false)
    private int ownerGroupId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TARGET_GROUP_ID", nullable = false)
    private int targetGroupId;

    public TrustedGroupsPK() {
    }

    public TrustedGroupsPK(int ownerGroupId, int targetGroupId) {
        this.ownerGroupId = ownerGroupId;
        this.targetGroupId = targetGroupId;
    }

    public int getOwnerGroupId() {
        return ownerGroupId;
    }

    public void setOwnerGroupId(int ownerGroupId) {
        this.ownerGroupId = ownerGroupId;
    }

    public int getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(int targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) ownerGroupId;
        hash += (int) targetGroupId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrustedGroupsPK)) {
            return false;
        }
        TrustedGroupsPK other = (TrustedGroupsPK) object;
        if (this.ownerGroupId != other.ownerGroupId) {
            return false;
        }
        if (this.targetGroupId != other.targetGroupId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.TrustedGroupsPK[ ownerGroupId=" + ownerGroupId + ", targetGroupId=" + targetGroupId + " ]";
    }
    
}
