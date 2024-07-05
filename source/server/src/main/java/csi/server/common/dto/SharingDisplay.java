package csi.server.common.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.security.AccessControlEntry;
import csi.server.common.util.Flags;
import csi.server.common.util.StringUtil;


public class SharingDisplay extends OwnershipInfo {

    private String _readers;
    private String _writers;
    private String _destroyers;
    private Date _createDate;
    private Date _accessDate;
    private Date _changeDate;
    private Long _size;
    private Integer _useCount;
    private Flags _flags;

    public SharingDisplay() {
        
    }

    public SharingDisplay(String uuidIn, String nameIn, String remarksIn, Date createDateIn, Date accessDateIn,
                          Date changeDateIn, Long sizeIn, Integer useCountIn, Integer flagsIn, String ownerIn,
                          List<AccessControlEntry> listIn) {

        super(uuidIn, nameIn, remarksIn, ownerIn);

        _createDate = createDateIn;
        _accessDate = accessDateIn;
        _changeDate = changeDateIn;
        _size = sizeIn;
        _useCount = useCountIn;
        _flags = new Flags(flagsIn);
        processAcl(listIn);
    }

    public SharingDisplay(String uuidIn, String nameIn, String remarksIn, Date createDateIn, Date accessDateIn,
                          Date changeDateIn, Long sizeIn, Integer useCountIn, Integer flagsIn, String ownerIn,
                          String[] aclIn) {

        super(uuidIn, nameIn, remarksIn, ownerIn);

        _createDate = createDateIn;
        _accessDate = accessDateIn;
        _changeDate = changeDateIn;
        _size = sizeIn;
        _useCount = useCountIn;
        _flags = new Flags(flagsIn);
        _readers = ((null != aclIn) && (0 < aclIn.length)) ? aclIn[0] : null;
        _writers = ((null != aclIn) && (1 < aclIn.length)) ? aclIn[1] : null;
        _destroyers = ((null != aclIn) && (2 < aclIn.length)) ? aclIn[2] : null;
    }

    public SharingDisplay(String uuidIn, String ownerIn, String readersIn, String writersIn, String destroyersIn) {

        super(uuidIn, null, null, ownerIn);

        _flags = new Flags(0);
        _readers = readersIn;
        _writers = writersIn;
        _destroyers = destroyersIn;
    }

    public SharingDisplay(String[] dataIn, List<AccessControlEntry> listIn) {

        super(dataIn);

        _flags = new Flags(0);
        processAcl(listIn);
    }

    public void setReaders(String readersIn) {
        
        _readers = readersIn;
    }
    
    public String getReaders() {
        
        return _readers;
    }
    
    public List<String> getReaderList() {
        
        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), _readers, ',');
    }
    
    public void setWriters(String writersIn) {
        
        _writers = writersIn;
    }
    
    public String getWriters() {
        
        return _writers;
    }
    
    public List<String> getWriterList() {
        
        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), _writers, ',');
    }
    
    public void setDestroyers(String destroyersIn) {
        
        _destroyers = destroyersIn;
    }

    public String getDestroyers() {

        return _destroyers;
    }

    public List<String> getDestroyerList() {

        return (List<String>)StringUtil.extractInto(new ArrayList<String>(), _destroyers, ',');
    }

    public Date getCreateDate() {

        return _createDate;
    }

    public void setCreateDate(Date createDateIn) {

        _createDate = createDateIn;
    }

    public Date getAccessDate() {

        return _accessDate;
    }

    public void setAccessDate(Date accessDateIn) {

        _accessDate = accessDateIn;
    }

    public Date getChangeDate() {

        return _changeDate;
    }

    public void setChangeDate(Date changeDateIn) {

        _changeDate = changeDateIn;
    }

    public void setSize(Long sizeIn) {

        _size = sizeIn;
    }

    public Long getSize() {

        return _size;
    }

    public Integer getUseCount() {

        return _useCount;
    }

    public void setUseCount(Integer useCountIn) {

        _useCount = useCountIn;
    }

    public Flags getFlags() {

        return _flags;
    }

    public void setFlags(Flags flagsIn) {

        _flags = flagsIn;
    }

    public void updateAccess(SharingDisplay newDataIn) {

        setName(newDataIn.getName());
        setOwner(newDataIn.getOwner());
        setReaders(newDataIn.getReaders());
        setWriters(newDataIn.getWriters());
        setDestroyers(newDataIn.getDestroyers());
    }

    public void copy(SharingDisplay sourceIn) {

        super.copy(sourceIn);
        setReaders(sourceIn.getReaders());
        setWriters(sourceIn.getWriters());
        setDestroyers(sourceIn.getDestroyers());
        setCreateDate(sourceIn.getCreateDate());
        setAccessDate(sourceIn.getAccessDate());
        setChangeDate(sourceIn.getChangeDate());
        setSize(sourceIn.getSize());
        setUseCount(sourceIn.getUseCount());
    }

    public void copy(SharingDisplay sourceIn, boolean hasBaseIn, boolean hasAclIn) {

        if (hasBaseIn) {

            setOwner(sourceIn.getOwner());
            setName(sourceIn.getName());
            setCreateDate(sourceIn.getCreateDate());
            setAccessDate(sourceIn.getAccessDate());
            setChangeDate(sourceIn.getChangeDate());
            setSize(sourceIn.getSize());
            setUseCount(sourceIn.getUseCount());
        }
        if (hasAclIn) {

            if (null != sourceIn.getOwner()) {

                setOwner(sourceIn.getOwner());
            }
            setReaders(sourceIn.getReaders());
            setWriters(sourceIn.getWriters());
            setDestroyers(sourceIn.getDestroyers());
        }
    }

    public void merge(SharingDisplay sourceIn) {

        super.copy(sourceIn);
        if (null != sourceIn.getReaders()) {

            setReaders(sourceIn.getReaders());
        }
        if (null != sourceIn.getWriters()) {

            setWriters(sourceIn.getWriters());
        }
        if (null != sourceIn.getDestroyers()) {

            setDestroyers(sourceIn.getDestroyers());
        }
        if (null != sourceIn.getCreateDate()) {

            setCreateDate(sourceIn.getCreateDate());
        }
        if (null != sourceIn.getAccessDate()) {

            setAccessDate(sourceIn.getAccessDate());
        }
        if (null != sourceIn.getChangeDate()) {

            setChangeDate(sourceIn.getChangeDate());
        }
        if (null != sourceIn.getSize()) {

            setSize(sourceIn.getSize());
        }
        if (null != sourceIn.getUseCount()) {

            setUseCount(sourceIn.getUseCount());
        }
    }

    private void processAcl(List<AccessControlEntry> listIn) {

        if (null != listIn) {

            StringBuilder myReaderBuffer = new StringBuilder();
            StringBuilder myWriterBuffer = new StringBuilder();
            StringBuilder myDestroyerBuffer = new StringBuilder();

            for (AccessControlEntry myItem : listIn) {

                switch (myItem.getAccessType()) {

                    case READ:

                        myReaderBuffer.append(',');
                        myReaderBuffer.append(myItem.getRoleName());
                        break;

                    case EDIT:

                        myWriterBuffer.append(',');
                        myWriterBuffer.append(myItem.getRoleName());
                        break;

                    case DELETE:

                        myDestroyerBuffer.append(',');
                        myDestroyerBuffer.append(myItem.getRoleName());
                        break;

                    default:

                        break;
                }
            }
            _readers = (0 < myReaderBuffer.length()) ? myReaderBuffer.toString().substring(1) : "";
            _writers = (0 < myWriterBuffer.length()) ? myWriterBuffer.toString().substring(1) : "";
            _destroyers = (0 < myDestroyerBuffer.length()) ? myDestroyerBuffer.toString().substring(1) : "";

        } else {

            _readers = "";
            _writers = "";
            _destroyers = "";
        }
    }
}
