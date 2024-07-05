package csi.server.common.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public class OwnershipInfo implements IsSerializable, Serializable {

    private String _uuid;
    private String _name;
    private String _remarks;
    private String _owner;
    
    public OwnershipInfo() {
        
    }

    public OwnershipInfo(String uuidIn, String nameIn, String remarksIn, String ownerIn) {

        _uuid = uuidIn;
        _name = nameIn;
        _remarks = remarksIn;
        _owner = ownerIn;
    }

    public OwnershipInfo(String[] dataIn) {

        _uuid = dataIn[0];
        _name = dataIn[1];
        _remarks = dataIn[2];
        _owner = dataIn[3];
    }

    public void setUuid(String uuidIn) {
        _uuid = uuidIn;
    }

    public String getUuid() {
        return _uuid;
    }

    public void setName(String nameIn) {
        
        _name = nameIn;
    }
    
    public String getName() {
        
        return _name;
    }
    
    public void setRemarks(String remarksIn) {
        
        _remarks = remarksIn;
    }
    
    public String getRemarks() {
        
        return _remarks;
    }
    
    public void setOwner(String ownerIn) {
        
        _owner = ownerIn;
    }
    
    public String getOwner() {
        
        return _owner;
    }

    public void copy(OwnershipInfo sourceIn) {

        setName(sourceIn.getName());
        setRemarks(sourceIn.getRemarks());
        setOwner(sourceIn.getOwner());
    }
}
