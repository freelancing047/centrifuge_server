package csi.server.common.util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.dto.AuthDO;
import csi.server.common.enumerations.JdbcDriverParameterKey;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;


public class AuthorizationObject {

    private Map<String, AuthDO> _authorizationMap = null;
    private ConnectionDef _connection = null;
    private String _localId = null;
    private String _resourceName = null;

    public AuthorizationObject(Map<String, AuthDO> authorizationMapIn, DataSourceDef sourceIn) {
        
        _authorizationMap = authorizationMapIn;
        
        if (null != sourceIn) {
            
            _localId = sourceIn.getLocalId();
            _connection = sourceIn.getConnection();
            _resourceName = sourceIn.getName();
        }
    }
    
    public String getResourceName() {

        return ((null != _resourceName) && (0 < _resourceName.length())) ? _resourceName : getConnectionName();
    }

    private String getConnectionName() {

        String myConnectionName = (null != _connection) ? _connection.getName() : null;

        return ((null != myConnectionName) && (0 < myConnectionName.length())) ? myConnectionName : null;
    }

    public String getUsername(String keyIn) {

        AuthDO myAuthentication = getAuthorization(keyIn);

        return (null != myAuthentication) ? myAuthentication.getUsername() : null;
    }

    public String getPassword(String keyIn) {

        AuthDO myAuthentication = getAuthorization(keyIn);

        return (null != myAuthentication) ? myAuthentication.getPassword() : null;
    }

    public String getUsername() {

        return (null != _connection) ? getUsername(_connection.getType()) : null;
    }

    public String getPassword() {

        return (null != _connection) ? getUsername(_connection.getType()) : null;
    }

    public boolean isUsernameLocked() {
        
        return false;
    }
    
    public void updateCredentials(String usernameIn, String passwordIn) {
        
        if (null != _connection) {
            
            Map<String, String> myMap = _connection.getProperties().getPropertiesMap();
            
            myMap.put(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey(), usernameIn);
            myMap.put(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey(), passwordIn);
            
            _connection.getProperties().refreshProperties();
        }
            
        if ((null != _authorizationMap) && (null != _localId)) {
            
            _authorizationMap.put(_localId, new AuthDO(_localId, usernameIn, passwordIn));
        }
    }

    public static void updateConnectionCredentials(DataSourceDef sourceIn,
                                            Map<String, AuthDO> authorizationMapIn) {
        
        String myKey = sourceIn.getLocalId();
        
        if ((null != authorizationMapIn)
                && authorizationMapIn.containsKey(myKey)) {
            
            ConnectionDef myConnection = sourceIn.getConnection();
            AuthDO myCredentials = authorizationMapIn.get(myKey);
            
            if (null != myConnection) {
                
                Map<String, String> myMap = myConnection.getProperties().getPropertiesMap();
                
                myMap.put(JdbcDriverParameterKey.RUNTIME_USERNAME.getKey(), myCredentials.getUsername());
                myMap.put(JdbcDriverParameterKey.RUNTIME_PASSWORD.getKey(), myCredentials.getPassword());
                
                myConnection.getProperties().refreshProperties();
            }
        }
    }

    public static void updateConnectionCredentials(DataWrapper dataWrapperIn,
                                            Map<String, AuthDO> authorizationMapIn) {
        
        updateConnectionCredentials(dataWrapperIn.getDataSources(), authorizationMapIn);
    }
    
    public static void updateConnectionCredentials(List<DataSourceDef> sourceListIn,
                                            Map<String, AuthDO> authorizationMapIn) {
        
        for (DataSourceDef mySource : sourceListIn) {
            
            updateConnectionCredentials(mySource, authorizationMapIn);
        }
    }

    public static Map<String, AuthDO> buildCredentialsMap(List<AuthDO> listIn) {

        Map<String, AuthDO> myMap = new TreeMap<String, AuthDO>();

		if (null != listIn) {
		
			for (AuthDO myCredentials : listIn) {

				myMap.put(myCredentials.getDsLocalId(), myCredentials);
			}
		}
        return myMap;
    }

    private AuthDO getAuthorization(String keyIn) {

        return ((null != keyIn) && (null != _authorizationMap)) ? _authorizationMap.get(keyIn) : null;
    }
}
