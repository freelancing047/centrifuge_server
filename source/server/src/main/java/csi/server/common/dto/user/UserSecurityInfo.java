package csi.server.common.dto.user;

import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;


public class UserSecurityInfo implements IsSerializable {

    String _adminGroup;
    String _csoGroup;
    String _originatorGroup;
    String _viewerGroup;
    String _everyoneGroup;
    String _adminUser;
    String _csoUser;
    String _name;
    Set<String> _userRoles;
    boolean _admin;
    boolean _security;
    boolean _restricted;
    boolean _canSetSecurity;
    boolean _doCapco;
    boolean _doTags;
    boolean _iconAdmin;
    boolean _fieldListAdmin;

    public void setAdminGroup(String adminGroupIn) {
        _adminGroup = adminGroupIn;
    }

    public String getAdminGroup() {
        return _adminGroup;
    }

    public void setCsoGroup(String csoGroupIn) {
        _csoGroup = csoGroupIn;
    }

    public String getCsoGroup() {
        return _csoGroup;
    }

    public void setOriginatorGroup(String originatorGroupIn) {
        _originatorGroup = originatorGroupIn;
    }

    public String getOriginatorGroup() {
        return _originatorGroup;
    }

    public void setViewerGroup(String viewerGroupIn) {
        _viewerGroup = viewerGroupIn;
    }

    public String getViewerGroup() {
        return _viewerGroup;
    }

    public void setEveryoneGroup(String everyoneGroupIn) {
        _everyoneGroup = everyoneGroupIn;
    }

    public String getEveryoneGroup() {
        return _everyoneGroup;
    }

    public void setAdminUser(String adminUserIn) {
        _adminUser = adminUserIn;
    }

    public String getAdminUser() {
        return _adminUser;
    }

    public void setCsoUser(String csoUserIn) {
        _csoUser = csoUserIn;
    }

    public String getCsoUser() {
        return _csoUser;
    }

    public void setName(String nameIn) {
        _name = nameIn;
    }

    public String getName() {
        return _name;
    }

    public void setUserRoles(Set<String> userRolesIn) {
        _userRoles = userRolesIn;
    }

    public Set<String> getUserRoles() {
        return _userRoles;
    }

    public void setAdmin(boolean adminIn) {
        _admin = adminIn;
    }

    public boolean isAdmin() {
        return _admin;
    }

    public void setSecurity(boolean securityIn) {
        _security = securityIn;
    }

    public boolean isSecurity() {
        return _security;
    }

    public void setRestricted(boolean restrictedIn) {
        _restricted = restrictedIn;
    }

    public boolean isRestricted() {
        return _restricted;
    }

    public void setCanSetSecurity(boolean canSetSecurityIn) {
        _canSetSecurity = canSetSecurityIn;
    }

    public boolean getCanSetSecurity() {
        return _canSetSecurity;
    }

    public void setDoCapco(boolean doCapcoIn) {
        _doCapco = doCapcoIn;
    }

    public boolean getDoCapco() {
        return _doCapco;
    }

    public void setDoTags(boolean doTagsIn) {
        _doTags = doTagsIn;
    }

    public boolean getDoTags() {
        return _doTags;
    }

    public void setIconAdmin(boolean iconAdminIn) {
        _iconAdmin = iconAdminIn;
    }

    public boolean getIconAdmin() {
        return _iconAdmin;
    }

    public void setFieldListAdmin(boolean fieldListAdminIn) {
        _fieldListAdmin = fieldListAdminIn;
    }

    public boolean getFieldListAdmin() {
        return _fieldListAdmin;
    }

    public void addRole(String roleIn) {
        
        if (null == _userRoles) {

            _userRoles = new TreeSet<>();
        }
        if (! _userRoles.contains(roleIn)) {

            _userRoles.add(roleIn);
        }
    }
}
