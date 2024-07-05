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

import com.sencha.gxt.core.client.ValueProvider;

import csi.server.common.model.FieldDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PreviewItemValueProvider implements ValueProvider<PreviewItem, String> {

    private FieldDef fieldDef;

    public PreviewItemValueProvider(FieldDef fieldDef) {
        super();
        this.fieldDef = fieldDef;
    }

    @Override
    public String getValue(PreviewItem item) {
        return item.getValue(fieldDef);
    }

    @Override
    public void setValue(PreviewItem object, String value) {
        // noop
    }

    @Override
    public String getPath() {
        return null;
    }

}
