package csi.server.common.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;


public class UserDisplay implements IsSerializable {
    
    private Long _id;
    private String _name;
    private String _password;
    private String _firstName;
    private String _lastName;
    private String _email;
    private String _remarks;
    private Date _lastLogin;
    private Date _creationDate;
    private Date _expirationDate;
    private Boolean _perpetual;
    private Boolean _disabled;
    private Boolean _suspended;
    private String _groups;
    private String _clearance;

    public void setId(Long idIn) {
        _id = idIn;
    }

    public Long getId() {
        return _id;
    }

    public void setName(String nameIn) {
        _name = nameIn;
    }

    public String getName() {
        return _name;
    }

    public void setPassword(String passwordIn) {
        _password = passwordIn;
    }

    public String getPassword() {
        return _password;
    }

    public void setFirstName(String firstNameIn) {
        _firstName = firstNameIn;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setLastName(String lastNameIn) {
        _lastName = lastNameIn;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setEmail(String emailIn) {
        _email = emailIn;
    }

    public String getEmail() {
        return _email;
    }

    public void setRemarks(String remarksIn) {
        _remarks = remarksIn;
    }

    public String getRemarks() {
        return _remarks;
    }

    public void setLastLogin(Date lastLoginIn) {
        _lastLogin = lastLoginIn;
    }

    public Date getLastLogin() {
        return _lastLogin;
    }

    public void setCreationDate(Date creationDateIn) {
        _creationDate = creationDateIn;
    }

    public Date getCreationDate() {
        return _creationDate;
    }

    public void setExpirationDate(Date expirationDateIn) {
        _expirationDate = expirationDateIn;
    }

    public Date getExpirationDate() {
        return _expirationDate;
    }

    public void setPerpetual(Boolean perpetualIn) {
        if (null != perpetualIn) {
            _perpetual = perpetualIn;
        } else {
            _perpetual = false;
        }
    }

    public Boolean getPerpetual() {
        return _perpetual;
    }

    public void setDisabled(Boolean disabledIn) {
        if (null != disabledIn) {
            _disabled = disabledIn;
        } else {
            _disabled = false;
        }
    }

    public Boolean getDisabled() {
        return _disabled;
    }

    public void setSuspended(Boolean suspendedIn) {
        if (null != suspendedIn) {
            _suspended = suspendedIn;
        } else {
            _suspended = false;
        }
    }

    public Boolean getSuspended() {
        return _suspended;
    }

    public void setGroups(String groupsIn) {
        _groups = groupsIn;
    }

    public String getGroups() {
        return _groups;
    }

    public void setClearance(String clearanceIn) {
        _clearance = clearanceIn;
    }

    public String getClearance() {
        return _clearance;
    }
    
    public UserDisplay copyFrom(UserDisplay newInfoIn) {
        
        setId(newInfoIn.getId());
        setName(newInfoIn.getName());
        setPassword(newInfoIn.getPassword());
        setFirstName(newInfoIn.getFirstName());
        setLastName(newInfoIn.getLastName());
        setEmail(newInfoIn.getEmail());
        setRemarks(newInfoIn.getRemarks());
        setLastLogin(newInfoIn.getLastLogin());
        setCreationDate(newInfoIn.getCreationDate());
        setExpirationDate(newInfoIn.getExpirationDate());
        setPerpetual(newInfoIn.getPerpetual());
        setDisabled(newInfoIn.getDisabled());
        setSuspended(newInfoIn.getSuspended());
        setGroups(newInfoIn.getGroups());
        setClearance(newInfoIn.getClearance());

        return  this;
    }
}
