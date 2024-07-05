package csi.client.gwt.etc;

import csi.server.common.dto.SelectionListData.ExtendedInfo;
import csi.server.common.enumerations.DisplayMode;



public class DataPairDisplay<S extends ExtendedInfo, T extends ExtendedInfo> implements IDataPairDisplay {
    
    private S _itemOne;
    private T _itemTwo;
    int _ordinal = 0;

    public DataPairDisplay() {

        _itemOne = null;
        _itemTwo = null;
    }

    public DataPairDisplay(S itemOneIn, T itemTwoIn) {

        _itemOne = itemOneIn;
        _itemTwo = itemTwoIn;
    }

    public DataPairDisplay(S itemOneIn, T itemTwoIn, int ordinalIn) {

        _itemOne = itemOneIn;
        _itemTwo = itemTwoIn;
        _ordinal = ordinalIn;
    }

    public S getItemOne() {
        
        return _itemOne;
    }
    
    public T getItemTwo() {
        
        return _itemTwo;
    }

    public int getOrdinal() {

        return _ordinal;
    }

    public String getKey() {
        
        return _itemOne.getKey() + _itemTwo.getKey();
    }
    
    public String getGroupValueOne() {
        
        return _itemOne.getParentString();
    }
    
    public String getGroupValueTwo() {
        
        return _itemTwo.getParentString();
    }
    
    public String getItemValueOne() {
        
        return _itemOne.getDisplayString();
    }
    
    public String getItemValueTwo() {
        
        return _itemTwo.getDisplayString();
    }

    @Override
    public String getParentString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTitleString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescriptionString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSpecial() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDisabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void enable() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disable() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DisplayMode getDisplayMode() {
        // TODO Auto-generated method stub
        return DisplayMode.NORMAL;
    }

    @Override
    public boolean isError() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComponent() {
        return false;
    }
}
