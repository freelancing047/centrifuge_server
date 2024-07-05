package csi.server.common.dto;

import java.util.List;

import csi.security.AccessControlEntry;


public class SharingInfo extends OwnershipInfo {

    private Long _readerCount;
    private Long _writerCount;
    private Long _destroyerCount;
    
    public SharingInfo() {
        
    }
    
    public SharingInfo(String uuidIn, String nameIn, String remarksIn, String ownerIn, Long readerCountIn, Long writerCountIn, Long destroyerCountIn) {
        
        super(uuidIn, nameIn, remarksIn, ownerIn);
        
        _readerCount = readerCountIn;
        _writerCount = writerCountIn;
        _destroyerCount = destroyerCountIn;
    }

    public SharingInfo(String uuidIn, String nameIn, String remarksIn, String ownerIn, List<AccessControlEntry> listIn) {

        super(uuidIn, nameIn, remarksIn, ownerIn);

        processAcl(listIn);
    }

    public SharingInfo(String[] dataIn, List<AccessControlEntry> listIn) {

        super(dataIn);

        processAcl(listIn);
    }

    public void setReaderCount(Long readerCountIn) {
        
        _readerCount = readerCountIn;
    }
    
    public Long getReaderCount() {
        
        return _readerCount;
    }
    
    public void setWriterCount(Long writerCountIn) {
        
        _writerCount = writerCountIn;
    }
    
    public Long getWriterCount() {
        
        return _writerCount;
    }
    
    public void setDestroyerCount(Long destroyerCountIn) {
        
        _destroyerCount = destroyerCountIn;
    }
    
    public Long getDestroyerCount() {
        
        return _destroyerCount;
    }

    private void processAcl(List<AccessControlEntry> listIn) {

        _readerCount = 0L;
        _writerCount = 0L;
        _destroyerCount = 0L;

        for (AccessControlEntry myItem : listIn) {

            switch (myItem.getAccessType()) {

                case READ:

                    _readerCount++;
                    break;

                case EDIT:

                    _writerCount++;
                    break;

                case DELETE:

                    _destroyerCount++;
                    break;

                default:

                    break;
            }
        }
    }
}
