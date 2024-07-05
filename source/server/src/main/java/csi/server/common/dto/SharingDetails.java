package csi.server.common.dto;

import java.util.ArrayList;
import java.util.List;

import csi.security.AccessControlEntry;


public class SharingDetails extends OwnershipInfo {

    private List<String> _readers;
    private List<String> _writers;
    private List<String> _destroyers;
    
    public SharingDetails() {
        
    }
    
    public SharingDetails(String uuidIn, String nameIn, String remarksIn, String ownerIn, List<AccessControlEntry> listIn) {
        
        super(uuidIn, nameIn, remarksIn, ownerIn);
        
        _readers = new ArrayList<String>();
        _writers = new ArrayList<String>();
        _destroyers = new ArrayList<String>();
        
        for (AccessControlEntry myItem : listIn) {
            
            switch (myItem.getAccessType()) {
                
                case READ:
                    
                    _readers.add(myItem.getRoleName());
                    break;
                
                case EDIT:
                    
                    _writers.add(myItem.getRoleName());
                    break;
                    
                case DELETE:
                    
                    _destroyers.add(myItem.getRoleName());
                    break;
                    
                default:
                    
                    break;
            }
        }
    }
    
    public void setReaders(List<String> readersIn) {
        
        _readers = readersIn;
    }
    
    public List<String> getReaders() {
        
        return _readers;
    }
    
    public void setWriters(List<String> writersIn) {
        
        _writers = writersIn;
    }
    
    public List<String> getWriters() {
        
        return _writers;
    }
    
    public void setDestroyers(List<String> destroyersIn) {
        
        _destroyers = destroyersIn;
    }
    
    public List<String> getDestroyers() {
        
        return _destroyers;
    }
}
