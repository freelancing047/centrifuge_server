package csi.client.gwt.widget.input_boxes;

import java.util.Map;


public class SelectionTextBox<T> extends FilteredTextBox {
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<String, T> _selectionMap = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public SelectionTextBox() {

    }
    
    public SelectionTextBox(Map<String, T> selectionMapIn) {

        _selectionMap = selectionMapIn;
    }
    
    public SelectionTextBox(Map<String, ? extends Object> rejectionMapIn, Map<String, T> selectionMapIn) {

        _rejectionMap = rejectionMapIn;
        _selectionMap = selectionMapIn;
    }
    
    public void setSelectionMap(Map<String, T> selectionMapIn) {

        _selectionMap = selectionMapIn;
    }
    
    public T findMatch() {
        
        String myTest = getText();
            
        if ((null != _selectionMap) && (null != myTest) && (0 < myTest.length())) {
                
            if (Mode.LOWERCASE.equals(_mode)) {
                
               myTest = myTest.toLowerCase();
                    
            } else if (Mode.UPPERCASE.equals(_mode)) {
                
                myTest = myTest.toUpperCase();
            }
            return matchValue(myTest);
            
        } else if (_isRequired) {
            
            return null;
            
        } else {
            
            return null;
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected T matchValue(String nameIn) {
                    
        if (_selectionMap.containsKey(nameIn)) {
            
            return _selectionMap.get(nameIn);
            
        } else {
            
            return null;
        }
    }
}
