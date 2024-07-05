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
package csi.server.common.service.api;

import java.util.List;

import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.filter.FilterPickEntry;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface FilterActionsServiceProtocol extends VortexService {

    /**
     * Retrieves and returns a page of distinct values for selecting into filter pick list.
     * @param dataViewUuid
     * @param fieldReference
     * @param loadConfig
     * @return
     */
    public PagingLoadResult<FilterPickEntry> getPickList(String dataViewUuid, BundledFieldReference fieldReference,
            PagingLoadConfig loadConfig);

    /**
     * @param selectionQualifier Items to exclude
     * @return Unique items in field minus selection qualifier
     */
    public List<String> getPickListSelection(String dataViewUuid, BundledFieldReference fieldReference,
            List<String> selectionQualifier);

}
