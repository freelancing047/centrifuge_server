/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.dialogs.preview;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.FieldDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PreviewItem {

    private static int idCounter = 0;
    
    private int id;
    private CsiMap<String, String> data;
    
    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public PreviewItem(CsiMap<String, String> data) {
        super();
        this.data = data;
        this.id = idCounter++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return Integer.toString(getId());
    }

    public String getValue(FieldDef fieldDef) {
        for (String key : data.keySet()) {
            if (key.equals(fieldDef.getUuid())) {
                return data.get(key);
            }
        }
        throw new RuntimeException(i18n.previewItemException(fieldDef.getFieldName()) + " with id " //$NON-NLS-1$ //$NON-NLS-2$
                + fieldDef.getUuid());
    }

}
