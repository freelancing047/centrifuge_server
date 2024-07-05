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

import java.util.List;

import csi.server.common.dto.AuthDO;
import csi.server.common.dto.ConnectionTestResults;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.CustomQueryDO;
import csi.server.common.dto.KeyValueItem;
import csi.server.common.dto.PreviewResponse;
import csi.server.common.dto.Response;
import csi.server.common.dto.TestQueryResponse;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface TestActionsServiceProtocol extends VortexService {
	
    public List<KeyValueItem> listConnectionTypes() throws CentrifugeException;


    public List<DriverBasics> listConnectionDescriptors() throws CentrifugeException;


    public CsiMap<String, String> testConnection(ConnectionDef dsdef) throws CentrifugeException;


    public ConnectionTestResults testAllSources(String uuid) throws CentrifugeException;

    
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(String uuidIn, List<AuthDO> credentialsIn);

    
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(DataView dataviewIn, List<AuthDO> credentialsIn);

    
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(DataViewDef templateIn, List<AuthDO> credentialsIn);

    public Response<String, List<QueryParameterDef>> getLaunchRequirements(AdHocDataSource sourceIn, List<AuthDO> credentialsIn);
/*
    public ConnectionTestResults testAllConnections(String uuid, String templateName, String dvname)
            throws CentrifugeException;
*/
//    public TableSelectionSetDTO getTables(ConnectionDef conDef) throws CentrifugeException;

    public CsiMap<String, String> testConnections(DataViewDef dvdef);


    public CsiMap<String, String> testDataSetConnections(DataSetOp op) throws CentrifugeException;


    public QueryDef createCustomQuery(SqlTableDef tableDefIn, List<String> requiredColumnsIn,
                                      List<QueryParameterDef> parametersIn) throws CentrifugeException;


    public TestQueryResponse testCustomQuery(CustomQueryDO customQuery) throws CentrifugeException;


    public Response<String, List<String>> listTableTypes(ConnectionDef dsdef, String catalog, String schema, String keyIn);


    public Response<String, List<String>> listCatalogs(ConnectionDef dsdef, String keyIn);


//    public Response<String, List<CsiMap<String, String>>> listSchemaMap(String catalog, ConnectionDef dsdef, String keyIn);
    
    public Response<String, List<String>> listSchemas(ConnectionDef dsdef, String catalog, String keyIn);


    public Response<String, List<ColumnDef>> listTableColumns(ConnectionDef connectionIn, SqlTableDef tableIn, String keyIn);


    public Response<String, List<SqlTableDef>> listTableDefs(ConnectionDef dsdef, String catalog,
                                                              String schema, String type, String keyIn);


//    public List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table)
//            throws CentrifugeException;


    public List<String> listQueryParameters(QueryDef queryDef) throws CentrifugeException;

/*
    public List<QueryParameterDef> listDVRequiredParameters(String dvUuid) throws CentrifugeException;


    public List<QueryParameterDef> listRequiredParameters(DataViewDef dvdef);


    public List<QueryParameterDef> listRequiredParameters2(String uuid, String templateName, String dvname,
            String vizUuid) throws CentrifugeException;
*/

    public PreviewResponse previewData(DataViewDef dvdef);
}
