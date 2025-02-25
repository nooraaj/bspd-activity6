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
@Table(name = "REF_SECURITY_QUESTION", catalog = "", schema = "FSCC-IS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RefSecurityQuestion.findAll", query = "SELECT r FROM RefSecurityQuestion r"),
    @NamedQuery(name = "RefSecurityQuestion.findBySecurityQuestionId", query = "SELECT r FROM RefSecurityQuestion r WHERE r.securityQuestionId = :securityQuestionId"),
    @NamedQuery(name = "RefSecurityQuestion.findByQuestion", query = "SELECT r FROM RefSecurityQuestion r WHERE r.question = :question"),
    @NamedQuery(name = "RefSecurityQuestion.findByCdate", query = "SELECT r FROM RefSecurityQuestion r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefSecurityQuestion.findByCtime", query = "SELECT r FROM RefSecurityQuestion r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefSecurityQuestion.findByCuser", query = "SELECT r FROM RefSecurityQuestion r WHERE r.cuser = :cuser")})
public class RefSecurityQuestion implements Serializable,BSPTimeStampable,PKTag_GetMax<Integer>,DisplayTag,PKTag {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "SECURITY_QUESTION_ID", nullable = false)
    private Integer securityQuestionId;
    @Size(max = 100)
    @Column(length = 100)
    private String question;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser;
    @OneToMany(mappedBy = "securityQuestionId")
    private List<RefUserAccounts> refUserAccountsList;

    public RefSecurityQuestion() {
    }

    public RefSecurityQuestion(Integer securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public Integer getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(Integer securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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
    public List<RefUserAccounts> getRefUserAccountsList() {
        return refUserAccountsList;
    }

    public void setRefUserAccountsList(List<RefUserAccounts> refUserAccountsList) {
        this.refUserAccountsList = refUserAccountsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (securityQuestionId != null ? securityQuestionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RefSecurityQuestion)) {
            return false;
        }
        RefSecurityQuestion other = (RefSecurityQuestion) object;
        if ((this.securityQuestionId == null && other.securityQuestionId != null) || (this.securityQuestionId != null && !this.securityQuestionId.equals(other.securityQuestionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefSecurityQuestion[ securityQuestionId=" + securityQuestionId + " ]";
    }

	@Override
	public void setIdToGenerate(Integer id) {
		this.securityQuestionId = id;
	}

	@Override
	public String getPKColumnName() {
		return "SECURITY_QUESTION_ID";
	}

	@Override
	public String getTableName() {
		return "REF_SECURITY_QUESTION";
	}

	@Override
	public Integer getDefaultId() {
		return 1;
	}

	@Override
	public String getDisplayName() {
		return this.question;
	}

	@Override
	public Object getPk() {
		return securityQuestionId;
	}
}
