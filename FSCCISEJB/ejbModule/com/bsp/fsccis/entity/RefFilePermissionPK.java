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
public class RefFilePermissionPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "FILE_PROPERTY_ID", nullable = false)
    private int filePropertyId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TARGET_GROUP_ID", nullable = false)
    private int targetGroupId;

    public RefFilePermissionPK() {
    }

    public RefFilePermissionPK(int filePropertyId, int targetGroup) {
        this.filePropertyId = filePropertyId;
        this.targetGroupId = targetGroup;
    }

    public int getFilePropertyId() {
        return filePropertyId;
    }

    public void setFilePropertyId(int filePropertyId) {
        this.filePropertyId = filePropertyId;
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
        hash += (int) filePropertyId;
        hash += (int) targetGroupId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RefFilePermissionPK)) {
            return false;
        }
        RefFilePermissionPK other = (RefFilePermissionPK) object;
        if (this.filePropertyId != other.filePropertyId) {
            return false;
        }
        if (this.targetGroupId != other.targetGroupId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefFilePermissionPK[ filePropertyId=" + filePropertyId + ", targetGroupId=" + targetGroupId + " ]";
    }
    
}
