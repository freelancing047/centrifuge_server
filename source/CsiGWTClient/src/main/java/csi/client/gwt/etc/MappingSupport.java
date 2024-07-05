package csi.client.gwt.etc;

import com.sencha.gxt.data.shared.ListStore;

import csi.server.common.dto.SelectionListData.ExtendedInfo;
import csi.server.common.exception.CentrifugeException;


public class MappingSupport<S extends ExtendedInfo, T extends ExtendedInfo> {
    
    private ListStore<S> _leftOriginalData;
    private ListStore<T> _rightOriginalData;
    
    private ListStore<S> _leftData;
    private ListStore<T> _rightData;
    private ListStore<DataPairDisplay<S, T>> _mappingData;
    
    private boolean _singleLeft = true;
    private boolean _singleRight = true;
    private boolean _requireAllLeft = false;
    private boolean _requireAllRight = false;
    private boolean _disableLeft = false;
    private boolean _disableRight = false;
    
    public MappingSupport(ListStore<S> leftDataIn, ListStore<T> rightDataIn,
            ListStore<DataPairDisplay<S, T>> mappingDataIn,
            boolean singleLeftIn, boolean singleRightIn, boolean requireAllLeftIn,
            boolean requireAllRightIn, boolean disableLeftIn, boolean disableRightIn) throws CentrifugeException {
        
        if ((null != leftDataIn) && (null != rightDataIn) && (null != mappingDataIn)) {
            
            _leftData = leftDataIn;
            _rightData = rightDataIn;
            _mappingData = mappingDataIn;

            _singleLeft = singleLeftIn;
            _singleRight = singleRightIn;
            _requireAllLeft = requireAllLeftIn;
            _requireAllRight = requireAllRightIn;
            _disableLeft = disableLeftIn;
            _disableRight = disableRightIn;
            
            initializeValues();
            
        } else {
            
            throw new CentrifugeException("");
        }
    }
    
    public void addLeftItem(S itemIn) {

        if (null == _leftOriginalData.findModel(itemIn)) {
            
            _leftOriginalData.add(itemIn);
        }

        if (null == _leftData.findModel(itemIn)) {
            
            _leftData.add(itemIn);
        }
    }
    
    public void addRightItem(T itemIn) {

        if (null == _rightOriginalData.findModel(itemIn)) {
            
            _rightOriginalData.add(itemIn);
        }

        if (null == _rightData.findModel(itemIn)) {
            
            _rightData.add(itemIn);
        }
    }
    
    public void unmapPair(DataPairDisplay<S, T> itemPairIn) {

        S myLeftItem = itemPairIn.getItemOne();
        T myRightItem = itemPairIn.getItemTwo();
        
        _mappingData.remove(itemPairIn);
        
        if (null != _leftOriginalData.findModel(myLeftItem)) {
            
            if (null == _leftData.findModel(myLeftItem)) {
                
                _leftData.add(myLeftItem);
            }
        }
        
        if (null != _rightOriginalData.findModel(myRightItem)) {
            
            if (null == _rightData.findModel(myRightItem)) {
                
                _rightData.add(myRightItem);
            }
        }
    }
    
    public DataPairDisplay<S, T> mapPair(S leftItemIn, T rightItemIn) {

        DataPairDisplay<S, T> myItem = new DataPairDisplay<S, T>(leftItemIn, rightItemIn);
        boolean myDoMap = isOkToMap(myItem);
        
        if (myDoMap) {
            
            _mappingData.add(myItem);
            adjustSourceData(leftItemIn, rightItemIn);
        }

        return myDoMap ? myItem : null;
    }
    
    public boolean isOkToMap(S leftItemIn, T rightItemIn) {
        
        return isOkToMap(new DataPairDisplay<S, T>(leftItemIn, rightItemIn));
    }
    
    public boolean isReady() {
        
        boolean myOk = true;
        
        if (_requireAllLeft) {
            
            if (_disableLeft) {
                
                for (S myItem : _leftData.getAll()) {
                    
                    if (!myItem.isDisabled()) {
                        
                        myOk = false;
                        break;
                    }
                }
                
            } else {
                
                myOk = (0 == _leftData.size());
            }
        }
        
        if (myOk && _requireAllRight) {
            
            if (_disableRight) {
                
                for (T myItem : _rightData.getAll()) {
                    
                    if (!myItem.isDisabled()) {
                        
                        myOk = false;
                        break;
                    }
                }
                
            } else {
                
                myOk = (0 == _rightData.size());
            }
        }
        return myOk;
    }
    
    private void initializeValues() {
        
        _leftOriginalData = new ListStore<S>(_leftData.getKeyProvider());
        _leftOriginalData.addAll(_leftData.getAll());
        _rightOriginalData = new ListStore<T>(_rightData.getKeyProvider());
        _rightOriginalData.addAll(_rightData.getAll());
        
        for (DataPairDisplay<S, T> myItemPair : _mappingData.getAll()) {
            
            S myLeftItem = myItemPair.getItemOne();
            T myRightItem = myItemPair.getItemTwo();
            
            adjustSourceData(myLeftItem, myRightItem);
        }
    }
    
    private boolean isOkToMap(DataPairDisplay<S, T> itemIn) {
        
        return (null == _mappingData.findModel(itemIn));
    }
    
    private void adjustSourceData(S leftItemIn, T rightItemIn) {
        
        
        if (_singleLeft) {
            
            if (_disableLeft) {
                
                leftItemIn.disable();
                
            } else {
                
                _leftData.remove(leftItemIn);
            }
        }
        
        if (_singleRight) {
            
            if (_disableRight) {
                
                rightItemIn.disable();
                
            } else {
                
                _rightData.remove(rightItemIn);
            }
        }
    }
}
