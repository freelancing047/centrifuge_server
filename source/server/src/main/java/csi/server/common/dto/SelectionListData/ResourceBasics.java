package csi.server.common.dto.SelectionListData;

import java.util.Date;
import java.util.List;

import csi.server.common.dto.SharingDisplay;

public class ResourceBasics extends SelectorBasics {
    private String _owner = null;
    private Date _lastAccess;
    private Long _size;
    private boolean _isOwner = false;

    public ResourceBasics() {

        super();
    }

    public ResourceBasics(String uuidIn, String nameIn, String remarksIn, String ownerIn) {

        super(uuidIn, nameIn, remarksIn);

        _owner = ownerIn;
        _lastAccess = new Date();
    }

    public ResourceBasics(SharingDisplay itemIn) {

        super(itemIn.getUuid(), itemIn.getName(), itemIn.getRemarks());

        _owner = itemIn.getOwner();
        _lastAccess = new Date();
    }

    public ResourceBasics(ResourceBasics itemIn, String newRemarksIn) {

        super(itemIn, newRemarksIn);

        _owner = itemIn.getOwner();
        _isOwner = itemIn.isOwner();
        _lastAccess = new Date();
    }

    public ResourceBasics(String nameIn, String remarksIn) {

        super(nameIn, remarksIn);

       _lastAccess = new Date();
    }

    public ResourceBasics(String uuidIn, String nameIn, String remarksIn) {

        super(uuidIn, nameIn, remarksIn);
    }

    public ResourceBasics(String uuidIn, String nameIn, String remarksIn, Date lastAccessIn, String ownerIn, Long sizeIn, String userIn) {

        super(uuidIn, nameIn, remarksIn);

        _lastAccess = lastAccessIn;
        _owner = ownerIn;
        _isOwner = ((null != userIn) && userIn.equals(_owner));
        _size = sizeIn;
    }

    public ResourceBasics(Object[] valueArrayIn, String userIn) {

        super((String)valueArrayIn[0], (String)valueArrayIn[1], (String)valueArrayIn[2]);

        _lastAccess = (Date)valueArrayIn[3];
        _owner = (String)valueArrayIn[4];
        _isOwner = ((null != userIn) && userIn.equals(_owner));
        if (5 < valueArrayIn.length) {

            _size = (Long)valueArrayIn[5];
        }
    }

    public String getUuid() {

        return getKey();
    }

    public void setLastAccess(Date lastAccessIn) {

        _lastAccess = lastAccessIn;
    }

    public Date getLastAccess() {

        return _lastAccess;
    }

    public void setSize(Long sizeIn) {

        _size = sizeIn;
    }

    public Long getSize() {

        return _size;
    }

    public String getOwner() {

        return _owner;
    }

    public void setOwner(String ownerIn) {

        _owner = ownerIn;
    }

    public boolean getIsOwner() {

        return _isOwner;
    }

    public void setIsOwner(boolean isOwnerIn) {

        _isOwner = isOwnerIn;
    }

    public boolean isOwner() {

        return _isOwner;
    }

    public String getOwnerDisplayName() {

        return ((null != _owner) && (0 < _owner.length())) ? _owner : "<no owner>";
    }

    @Override
    public String getDisplayString() {

//        return ((!_isOwner)  ? (getOwnerDisplayName() + ".") : "") + getDisplayName();
        return (getDisplayName() + ((!_isOwner)  ? (" [" + getOwnerDisplayName() + "]") : ""));
    }

    @Override
    public String getTitleString() {

        return getOwnerDisplayName() + "." + getDisplayName();
    }

   @SuppressWarnings("unchecked")
   public static List<ResourceBasics> loadResults(String userIn, List<Object[]> listIn, List<ResourceBasics> listOutIn) {
      if (listIn != null) {
         for (Object[] myResults : listIn) {
            listOutIn.add(new ResourceBasics(myResults, userIn));
         }
      }
      return listOutIn;
   }
}
