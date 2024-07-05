package csi.server.common.dto.SelectionListData;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.DisplayableObject;


public class SelectorBasics extends DisplayableObject implements IsSerializable, ExtendedDisplayInfo {

    private String _key = null;
    private String _name = null;
    private String _remarks = null;
    private int _ordinal = 0;
    
    public SelectorBasics() {

        super(DisplayMode.NORMAL);
    }

    public SelectorBasics(SelectorBasics itemIn, String newRemarksIn) {

        super(DisplayMode.NORMAL);

        _key = itemIn.getKey();
        _name = itemIn.getName();
        _remarks = newRemarksIn;
    }

    public SelectorBasics(String keyIn, String nameIn, String remarksIn) {

        super(DisplayMode.NORMAL);

        _key = keyIn;
        _name = nameIn;
        _remarks = remarksIn;
    }

    public SelectorBasics(String nameIn, String remarksIn) {

        super(DisplayMode.NORMAL);

        _name = nameIn;
        _remarks = remarksIn;
    }

    public SelectorBasics(SelectorBasics itemIn, String newRemarksIn, int ordinalIn) {

        super(DisplayMode.NORMAL);

        _key = itemIn.getKey();
        _name = itemIn.getName();
        _remarks = newRemarksIn;
        _ordinal = ordinalIn;
    }

    public SelectorBasics(String keyIn, String nameIn, String remarksIn, int ordinalIn) {

        super(DisplayMode.NORMAL);

        _key = keyIn;
        _name = nameIn;
        _remarks = remarksIn;
        _ordinal = ordinalIn;
    }

    public SelectorBasics(String nameIn, String remarksIn, int ordinalIn) {

        super(DisplayMode.NORMAL);

        _name = nameIn;
        _remarks = remarksIn;
        _ordinal = ordinalIn;
    }

    public String getKey() {
        
        return _key;
    }
    
    public void setKey(String keyIn) {
        
        _key = keyIn;
    }
    
    public String getName() {
        
        return _name;
    }

    public void setName(String nameIn) {
        
        _name = nameIn;
    }

    public String getRemarks() {

        return (null != _remarks) ? _remarks : "";
    }

    public void setRemarks(String remarksIn) {

        _remarks = remarksIn;
    }

    public int getOrdinal() {

        return _ordinal;
    }

    public void setOrdinal(int ordinalIn) {

        _ordinal = ordinalIn;
    }

    public String getDisplayName() {

        return ((null != _name) && (0 < _name.length())) ? _name : "????????";
    }

    public String getDisplayString() {

        return getDisplayName();
    }

    public String getDisplayString(int prefixSizeIn) {

        String myName = getDisplayName();
        int myOldSize =  myName.length();

        if (0 < prefixSizeIn) {

            int myNewSize = Math.max(0, myOldSize - prefixSizeIn);

            myName = (0 < myNewSize) ? myName.substring(myOldSize - myNewSize) : "";
        }
        return myName;
    }

    public String getTitleString() {
        
        return getDisplayName();
    }
    
    public String getDescriptionString() {
        
        return (null != _remarks) ? _remarks : "";
    }

    @Override
    public String getParentString() {
        // TODO Auto-generated method stub
        return null;
    }
}
