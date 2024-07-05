package csi.server.common.identity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/*
 *
 * NB: The resulting DDL causes us some pain in populating--via SQL--the default
 * users.  Any changes to this class, User, or Group need to be reflected
 * in source/resources/CreateUsers.jdbc SQL script!
 *
 */

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Users")
public class User extends Role {

    private String infoId;

    private String display;

    private String firstName;

    private String lastName;

    private String remark;

    private String password;

    private String email;

    @Deprecated
    private Date lastLogin;
    @Transient
    private LocalDateTime lastLoginDateTime;

    @Deprecated
    private Date creationDate;
    @Transient
    private LocalDateTime creationDateTime;

    @Deprecated
    private Date activateDate;
    @Transient
    private LocalDateTime activateDateTime;

    @Deprecated
    private Date expirationDate;
    @Transient
    private LocalDateTime expirationDateTime;

    private boolean perpetual = true;

    private Boolean disabled;

    private Boolean suspended;

    public void setInfoId(String infoIdIn) {

        infoId = infoIdIn;
    }

    public String getInfoId() {

        return infoId;
    }

   @Deprecated
   public Date getActivateDate() {
      return activateDate;
   }
   @Transient
   public LocalDateTime getActivateDateTime() {
      return activateDateTime;
   }

   @Deprecated
   public void setActivateDate(final Date activateDate) {
      activateDateTime = (activateDate == null) ? null : LocalDateTime.ofInstant(activateDate.toInstant(), ZoneId.systemDefault());
      this.activateDate = activateDate;
   }
   @Transient
   public void setActivateDateTime(final LocalDateTime activateDateTime) {
      activateDate = (activateDateTime == null) ? null : Date.from(activateDateTime.atZone(ZoneId.systemDefault()).toInstant());
      this.activateDateTime = activateDateTime;
   }

    @Deprecated
    public Date getExpirationDate() {
        return expirationDate;
    }
    @Transient
    public LocalDateTime getExpirationDateTime() {
       return expirationDateTime;
    }

    @Deprecated
    public void setExpirationDate(final Date expirationDate) {
       expirationDateTime = (expirationDate == null) ? null : LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
       this.expirationDate = expirationDate;
    }
    @Transient
    public void setExpirationDateTime(final LocalDateTime expirationDateTime) {
       expirationDate = (expirationDateTime == null) ? null : Date.from(expirationDateTime.atZone(ZoneId.systemDefault()).toInstant());
       this.expirationDateTime = expirationDateTime;
    }

    @Deprecated
    public Date getCreationDate() {
        return creationDate;
    }
    @Transient
    public LocalDateTime getCreationDateTime() {
       return creationDateTime;
    }

    @Deprecated
    public void setCreationDate(final Date creationDate) {
       creationDateTime = (creationDate == null) ? null : LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
       this.creationDate = creationDate;
    }
    @Transient
    public void setCreationDateTime(final LocalDateTime creationDateTime) {
       creationDate = (creationDateTime == null) ? null : Date.from(creationDateTime.atZone(ZoneId.systemDefault()).toInstant());
       this.creationDateTime = creationDateTime;
    }

    @Deprecated
    public Date getLastLogin() {
        return lastLogin;
    }
    @Transient
    public LocalDateTime getLastLoginDateTime() {
       return lastLoginDateTime;
    }

    @Deprecated
    public void setLastLogin(final Date lastLogin) {
       lastLoginDateTime = (lastLogin == null) ? null : LocalDateTime.ofInstant(lastLogin.toInstant(), ZoneId.systemDefault());
       this.lastLogin = lastLogin;
    }
    @Transient
    public void setLastLoginDateTime(final LocalDateTime lastLoginDateTime) {
       lastLogin = (lastLoginDateTime == null) ? null : Date.from(lastLoginDateTime.atZone(ZoneId.systemDefault()).toInstant());
       this.lastLoginDateTime = lastLoginDateTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(length = 512)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String displayIn) {
        this.display = displayIn;
    }

    public Boolean isPerpetual() {
        return perpetual;
    }

    public void setPerpetual(Boolean perpetual) {
       this.perpetual = (perpetual == null) || perpetual.booleanValue();
    }

    @Column(nullable = true)
    public boolean isDisabled() {
        return (disabled != null) && disabled.booleanValue();
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = (disabled == null) ? Boolean.FALSE : disabled;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = (suspended == null) ? Boolean.FALSE : suspended;
    }

    @Column(nullable = true)
    public boolean isSuspended() {
        return (suspended != null) && suspended.booleanValue();
    }

    public String toString() {
        return firstName + " " + lastName;
    }

    @PrePersist
    @PreUpdate
    private void enforceConstraints() {
        if (disabled == null) {
            disabled = Boolean.FALSE;
        }
    }

    @Transient
    public void updateFrom(final User other) {
       setInfoId(other.getInfoId());
       setDisplay(other.getDisplay());
       setFirstName(other.getFirstName());
       setLastName(other.getLastName());
       setRemark(other.getRemark());
       setPassword(other.getPassword());
       setEmail(other.getEmail());
       setLastLogin(other.getLastLogin());
       setCreationDate(other.getCreationDate());
       setActivateDate(other.getActivateDate());
       setExpirationDate(other.getExpirationDate());
       setPerpetual(other.isPerpetual());
       setDisabled(Boolean.valueOf(other.isDisabled()));
       setSuspended(Boolean.valueOf(other.isSuspended()));
    }
}
