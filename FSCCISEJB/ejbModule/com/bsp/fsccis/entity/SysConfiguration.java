/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsp.fsccis.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.DisplayTag;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "SYS_CONFIGURATION", catalog = "", schema = "FSCC-IS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"NAME"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SysConfiguration.findAll", query = "SELECT s FROM SysConfiguration s"),
    @NamedQuery(name = "SysConfiguration.findValueByName", query = "SELECT s.value FROM SysConfiguration s WHERE s.name = :name"),
    @NamedQuery(name = "SysConfiguration.findById", query = "SELECT s FROM SysConfiguration s WHERE s.id = :id"),
    @NamedQuery(name = "SysConfiguration.findByName", query = "SELECT s FROM SysConfiguration s WHERE s.name = :name"),
    @NamedQuery(name = "SysConfiguration.findByValue", query = "SELECT s FROM SysConfiguration s WHERE s.value = :value"),
    @NamedQuery(name = "SysConfiguration.findByCdate", query = "SELECT s FROM SysConfiguration s WHERE s.cdate = :cdate"),
    @NamedQuery(name = "SysConfiguration.findByCtime", query = "SELECT s FROM SysConfiguration s WHERE s.ctime = :ctime"),
    @NamedQuery(name = "SysConfiguration.findByCuser", query = "SELECT s FROM SysConfiguration s WHERE s.cuser = :cuser")})
public class SysConfiguration implements Serializable,BSPTimeStampable,PKTag_GetMax<Integer>,DisplayTag,PKTag {
	
	public static final String ROOT_FOLDER_DIRECTORY = "ROOT_FOLDER_DIRECTORY";
	public static final String PASSWORD_RETENTION = "PASSWORD_RETENTION";
	
	
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(nullable = false, length = 25)
    private String name;
    @Size(max = 250)
    @Column(length = 250)
    private String value;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;

    public SysConfiguration() {
    }

    public SysConfiguration(Integer id) {
        this.id = id;
    }

    public SysConfiguration(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SysConfiguration)) {
            return false;
        }
        SysConfiguration other = (SysConfiguration) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.SysConfiguration[ id=" + id + " ]";
    }

	@Override
	public Object getPk() {
		return this.id;
	}

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		sb.append("NAME: ").append(this.name).append("-");
		sb.append("VALUE: ").append(this.value);
		return sb.toString();
	}

	@Override
	public void setIdToGenerate(Integer id) {
		this.id = id;
	}

	@Override
	public String getPKColumnName() {
		return "ID";
	}

	@Override
	public String getTableName() {
		return "SYS_CONFIGURATION";
	}

	@Override
	public Integer getDefaultId() {
		return 1;
	}
    
}
