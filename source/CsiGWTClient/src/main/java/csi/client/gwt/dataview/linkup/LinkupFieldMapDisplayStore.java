package csi.client.gwt.dataview.linkup;

import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;

public class LinkupFieldMapDisplayStore implements DataStoreColumnAccess {

    public String dataViewField = null;               // column 1
    public static String elipsis = ".  .  .  .  .  .  .  .  .  .  .  ."; //$NON-NLS-1$
    public String templateField = null;                 // column 3
    
    public LinkupFieldMapDisplayStore(String dataViewFieldIn, String templateFieldIn) {
        dataViewField = dataViewFieldIn;
        templateField = templateFieldIn;
    }
    
    public String getKey() {
        return dataViewField;
    }
    
    public String getLabel() {
        return dataViewField;
    }

    public String getDataViewField() {
        return dataViewField;
    }

    public String getElipsis() {
        return elipsis;
    }

    public String getTemplateField() {
        return dataViewField;
    }

    public Object getColumnData(int columnIn) {
        
        Object myData = null;
        
        switch (columnIn) {
            
            case 0:
                
                myData = dataViewField;
                break;
                
            case 2:
                
                myData = templateField;
                break;                
        }
        return myData;
    }
    
    public void setColumnData(int columnIn, Object dataIn) {
        
        switch (columnIn) {
            
            case 0:
                
                if ((null == dataIn) || (dataIn instanceof String)) {
                    
                    dataViewField = (String)dataIn;
                }
                break;
                
            case 2:
                
                if ((null == dataIn) || (dataIn instanceof String)) {
                    
                    templateField = (String)dataIn;
                }
                break;                
        }
    }

    public boolean isSelected() {

        return true;
    }
}
