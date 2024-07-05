package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class AuthDO implements IsSerializable {

    private String _dsLocalId;
    private String _username;
    private String _password;
    
    public AuthDO() {
        
    }
    
    public AuthDO(String dsLocalIdIn, String usernameIn, String passwordIn) {
        
        _dsLocalId = dsLocalIdIn;
        _username = usernameIn;
        _password = passwordIn;
    }

    public String getDsLocalId() {
        return _dsLocalId;
    }

    public void setDsLocalId(String localIdIn) {
        _dsLocalId = localIdIn;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String usernameIn) {
        _username = usernameIn;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String passwordIn) {
        _password = passwordIn;
    }
}
