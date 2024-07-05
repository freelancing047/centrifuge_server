/** 
 *  Copyright (c) 2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.dataview.linkup;

import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;
import csi.server.common.model.FieldDef;


//
// Data store used for a single row of the field-to-field mapping grid
//
public class LinkupConditionalFieldMapStore implements DataStoreColumnAccess {
    
    private static int _keyBase = 0;
    
    private LinkupGridMapper<LinkupConditionalFieldMapStore> _parent;

    public FieldDef templateField = null;               // column 1
    public FieldDef mappedField = null;                 // column 2
    public Integer key;
    public Boolean active;

    public LinkupConditionalFieldMapStore(LinkupGridMapper<LinkupConditionalFieldMapStore> parentIn) {
        _parent = parentIn;
        key = _keyBase++ % 2000000;
        active = true;
    }
    
    public LinkupConditionalFieldMapStore(LinkupGridMapper<LinkupConditionalFieldMapStore> parentIn, FieldDef templateFieldIn, FieldDef mappedFieldIn) {
        _parent = parentIn;
        key = _keyBase++ % 2000000;
        templateField = templateFieldIn;
        mappedField = mappedFieldIn;
        active = true;
    }
    
    public void setColumn(Integer columnIn, Object objectIn) {
        
        if ((null != columnIn) && ((null == objectIn) || (objectIn instanceof FieldDef))) {
            
            switch (columnIn) {
                
                case 0:
                    
                    setTemplateField((FieldDef)objectIn);
                    break;
                    
                case 1:
                    
                    setMappedField((FieldDef)objectIn);
                    break;
            }
            if (null != _parent) {
                _parent.selectionChange(this);
            }
        }
    }

    public void setActive(Boolean activeIn) {
        active = activeIn;
        if (null != _parent) {
            _parent.selectionChange(this);
        }
    }

    public Boolean getActive() {
        return active;
    }

    public Integer getKey() {
        return key;
    }
    
    public String getTemplateFieldName() {
        return (null != templateField) ? templateField.getFieldName() : ""; //$NON-NLS-1$
    }
    
    public FieldDef getTemplateField() {
        return templateField;
    }
    
    public void setTemplateField(FieldDef templateFieldIn) {
        templateField = templateFieldIn;
        if (null != _parent) {
            _parent.selectionChange(this);
        }
   }
    
    public FieldDef getMappedField() {
        
        return mappedField;
    }
    
    public void setMappedField(FieldDef mappedFieldIn) {
        mappedField = mappedFieldIn;
        if (null != _parent) {
            _parent.selectionChange(this);
        }
    }

    public void enterEditMode(int columnIn) {
    }

    public void exitEditMode(int columnIn) {
    }

    public String getStyle(int columnIn) {
        String myStyle = null;
        return myStyle;
    }
    
    public Object getColumnData(int columnIn) {
        
        Object myData = null;
        
        switch (columnIn) {
            
            case 0:
                
                myData = templateField;
                break;
                
            case 1:
                
                myData = mappedField;
                break;                
        }
        return myData;
    }
    
    public void setColumnData(int columnIn, Object dataIn) {
        
        switch (columnIn) {
            
            case 0:
                
                if ((null == dataIn) || (dataIn instanceof FieldDef)) {
                    
                    templateField = (FieldDef)dataIn;
                }
                break;
                
            case 1:
                
                if ((null == dataIn) || (dataIn instanceof FieldDef)) {
                    
                    mappedField = (FieldDef)dataIn;
                }
                break;                
        }
    }

    public boolean isSelected() {

        return active;
    }
}
