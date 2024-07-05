/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
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


import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

import csi.server.common.dto.CustomPagingResultBean;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.ws.actions.PagingInfo;
import csi.shared.core.util.IntCollection;
import csi.shared.gwt.viz.table.TableSearchRequest;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface TableActionsServiceProtocol extends VortexService {

    public PagingLoadResult<Map<String, String>> gwtGetTableData(String dvUuid, String vizUuid,
                                                                 PagingLoadConfig loadConfig) throws CentrifugeException, IOException;


    public int[] getAllIds(String dvUuid, String visUuid) throws CentrifugeException;


    CustomPagingResultBean<List<?>> gwtGetTableDataList(String dvUuid, String vizUuid, PagingLoadConfig loadConfig)
            throws CentrifugeException, IOException;
    
    public PagingInfo getRowCount(String dvUuid, String vizUuid) throws CentrifugeException;


    public Integer searchTable(TableSearchRequest request, PagingLoadConfig loadConfig)
            throws CentrifugeException, IOException;


    String retrieveCopyText(List<FieldDef> list, int startRow, int endRow, List<? extends SortInfo> sort,
            String dvUuid, String vizUuid) throws CentrifugeException;



}
