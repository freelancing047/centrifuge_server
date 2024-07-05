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

import com.sencha.gxt.data.shared.ModelKeyProvider;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PreviewItemKeyProvider implements ModelKeyProvider<PreviewItem> {

    @Override
    public String getKey(PreviewItem item) {
        return item.getKey();
    }

}
