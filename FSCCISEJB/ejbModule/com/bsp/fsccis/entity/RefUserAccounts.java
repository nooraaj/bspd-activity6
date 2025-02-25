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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.bsp.fsccis.entity.tag.AbstractCrudEntity;
import com.bsp.fsccis.entity.tag.BSPTimeStampable;
import com.bsp.fsccis.entity.tag.CRUDTag;
import com.bsp.fsccis.entity.tag.DisplayTag2;
import com.bsp.fsccis.entity.tag.PKTag;
import com.bsp.fsccis.entity.tag.PKTag_GetMax;

/**
 *
 * @author MallillinJG
 */
@Entity
@Table(name = "REF_USER_ACCOUNTS", catalog = "", schema = "FSCC-IS", uniqueConstraints = @UniqueConstraint(columnNames ="USER_NAME"))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RefUserAccounts.findAll", query = "SELECT r FROM RefUserAccounts r"),
    @NamedQuery(name = "RefUserAccounts.findRefAgencyByUserName", query = "SELECT r.refAgencyGroupId FROM RefUserAccounts r WHERE r.userName = :userName"),
    @NamedQuery(name = "RefUserAccounts.findByUserId", query = "SELECT r FROM RefUserAccounts r WHERE r.userId = :userId"),
    @NamedQuery(name = "RefUserAccounts.findByUserName", query = "SELECT r FROM RefUserAccounts r WHERE r.userName = :userName"),
    @NamedQuery(name = "RefUserAccounts.findByUserPw", query = "SELECT r FROM RefUserAccounts r WHERE r.userPw = :userPw"),
    @NamedQuery(name = "RefUserAccounts.findByEmail", query = "SELECT r FROM RefUserAccounts r WHERE r.email = :email"),
    @NamedQuery(name = "RefUserAccounts.findByLastName", query = "SELECT r FROM RefUserAccounts r WHERE r.lastName = :lastName"),
    @NamedQuery(name = "RefUserAccounts.findByFirstName", query = "SELECT r FROM RefUserAccounts r WHERE r.firstName = :firstName"),
    @NamedQuery(name = "RefUserAccounts.findByMiddleName", query = "SELECT r FROM RefUserAccounts r WHERE r.middleName = :middleName"),
    @NamedQuery(name = "RefUserAccounts.findByLoginAttempts", query = "SELECT r FROM RefUserAccounts r WHERE r.loginAttempts = :loginAttempts"),
    @NamedQuery(name = "RefUserAccounts.findByAccountStatus", query = "SELECT r FROM RefUserAccounts r WHERE r.accountStatus = :accountStatus"),
    @NamedQuery(name = "RefUserAccounts.findBySecurityQuestionAnswer", query = "SELECT r FROM RefUserAccounts r WHERE r.securityQuestionAnswer = :securityQuestionAnswer"),
    @NamedQuery(name = "RefUserAccounts.findByPwDateExpiration", query = "SELECT r FROM RefUserAccounts r WHERE r.pwDateExpiration = :pwDateExpiration"),
    @NamedQuery(name = "RefUserAccounts.findByCdate", query = "SELECT r FROM RefUserAccounts r WHERE r.cdate = :cdate"),
    @NamedQuery(name = "RefUserAccounts.findByCtime", query = "SELECT r FROM RefUserAccounts r WHERE r.ctime = :ctime"),
    @NamedQuery(name = "RefUserAccounts.findByCuser", query = "SELECT r FROM RefUserAccounts r WHERE r.cuser = :cuser"),
    @NamedQuery(name = "RefUserAccounts.findByAuthDefault", query = "SELECT r FROM RefUserAccounts r WHERE r.authDefault = :authDefault")})
public class RefUserAccounts extends AbstractCrudEntity implements Serializable,PKTag_GetMax<Long>,BSPTimeStampable,CRUDTag,DisplayTag2,PKTag {

	public static final short DEFAULT_ATTEMPT_CNT = 0;
	public static final short STATUS_ACTIVE = 1;
	public static final short STATUS_INACTIVE = 2;
	public static final int MAX_LOGIN_ATTEMPT = 3;
	public static final int AUTH_DEFAULT_INACTIVE = 0;
	public static final int AUTH_DEFAULT_ACTIVE = 1;
	public static final int AUTH_DEFAULT_RESET = 2;
	
	private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    @Size(max = 15)
    @Column(name = "USER_NAME", length = 15)
    private String userName;
    @Size(max = 64)
    @Column(name = "USER_PW", length = 64)
    private String userPw;
    @Size(max = 100)
    @Column(name = "EMAIL")
    private String email;
    @Size(max = 50)
    @Column(name = "LAST_NAME", length = 50)
    private String lastName;
    @Size(max = 50)
    @Column(name = "FIRST_NAME", length = 50)
    private String firstName;
    @Size(max = 50)
    @Column(name = "MIDDLE_NAME", length = 50)
    private String middleName;
    @Column(name = "LOGIN_ATTEMPTS")
    private Short loginAttempts = 0;
    @Column(name = "ACCOUNT_STATUS")
    private Short accountStatus;
    @Size(max = 25)
    @Column(name = "SECURITY_QUESTION_ANSWER", length = 25)
    private String securityQuestionAnswer;
    @Column(name = "PW_DATE_EXPIRATION")
    @Temporal(TemporalType.DATE)
    private Date pwDateExpiration;
    @Temporal(TemporalType.DATE)
    private Date cdate;
    @Temporal(TemporalType.TIME)
    private Date ctime;
    @Size(max = 15)
    @Column(length = 15)
    private String cuser; 
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "refUserAccounts")
    private List<RefUserRole> refUserRoleList;
    @JoinColumn(name = "SECURITY_QUESTION_ID", referencedColumnName = "SECURITY_QUESTION_ID")
    @ManyToOne
    private RefSecurityQuestion securityQuestionId;
    @JoinColumn(name = "AGENCY_GROUP_ID", referencedColumnName = "AGENCY_GROUP_ID")
    @ManyToOne
    private RefAgencyGroup refAgencyGroupId;
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @ManyToOne
    private RefAgency agencyId;
    @Column(name="AUTH_DEFAULT")
    private Short authDefault;
    
    public RefUserAccounts() {
    }

    public RefUserAccounts(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Short getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Short loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public Short getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Short accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getSecurityQuestionAnswer() {
        return securityQuestionAnswer;
    }

    public void setSecurityQuestionAnswer(String securityQuestionAnswer) {
        this.securityQuestionAnswer = securityQuestionAnswer;
    }

    public Date getPwDateExpiration() {
        return pwDateExpiration;
    }

    public void setPwDateExpiration(Date pwDateExpiration) {
        this.pwDateExpiration = pwDateExpiration;
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

    public RefSecurityQuestion getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(RefSecurityQuestion securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RefUserAccounts)) {
            return false;
        }
        RefUserAccounts other = (RefUserAccounts) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsp.fsccis.entity.RefUserAccounts[ userId=" + userId + " ]";
    }
    

    @Transient
	private String displayRoleList;
    @XmlTransient
	public String getDisplayRoleList() {
		return displayRoleList;
	}

	public void setDisplayRoleList(String displayRoleList) {
		this.displayRoleList = displayRoleList;
	}

	@Override
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		if(refAgencyGroupId != null){
			sb.append("AGENCY:").append(refAgencyGroupId.getAgencyGroupShortname()).append(";");
		}
		sb.append("USER:").append(userName).append(";");
		return sb.toString();
	}

	@Override
	public Object getPk() {
		return this.userId;
	}

	@Override
	public void setIdToGenerate(Long id) {
		this.userId = id;
		
	}

	@Override
	public String getPKColumnName() {
		return "USER_ID";
	}

	@Override
	public String getTableName() {
		return "REF_USER_ACCOUNTS";
	}

	@Override
	public Long getDefaultId() {
		return 1L;
	}

	public RefAgencyGroup getRefAgencyGroupId() {
		return refAgencyGroupId;
	}

	public void setRefAgencyGroupId(RefAgencyGroup refAgencyGroupId) {
		this.refAgencyGroupId = refAgencyGroupId;
	}

	public RefAgency getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(RefAgency agencyId) {
		this.agencyId = agencyId;
	}

	public Short getAuthDefault() {
		return authDefault;
	}

	public void setAuthDefault(Short authDefault) {
		this.authDefault = authDefault;
	}
}
