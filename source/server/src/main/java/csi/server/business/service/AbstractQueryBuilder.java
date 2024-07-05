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
package csi.server.business.service;

import csi.server.business.helper.DataCacheHelper;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.SelectSQL;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractQueryBuilder<V extends VisualizationDef> {

    protected static final String INTERNAL_ID_COLUMN_NAME = "internal_id";
    private DataView dataView;
    private V viewDef;
    private SQLFactory sqlFactory;
    private FilterActionsService filterActionsService;

    
    protected void applyFilters(CacheTableSource tableSource, SelectSQL sql, boolean ignoreBroadcast) {
        sql.where(getFilterActionsService().getPredicate(getViewDef(), getDataView(), tableSource));
        // Broadcast filter
        if (!ignoreBroadcast) {
            Predicate predicate = DataCacheHelper.getAttachFilterAsConditionPredicate(tableSource, getViewDef().getUuid(), getDataView());
            if (predicate != null) {
                sql.where(predicate);
            }
       }
        
    }
    public DataView getDataView() {
        return dataView;
    }

    public void setDataView(DataView dataView) {
        this.dataView = dataView;
    }

    public V getViewDef() {
        return viewDef;
    }

    public void setViewDef(V viewDef) {
        this.viewDef = viewDef;
    }

    public SQLFactory getSqlFactory() {
        return sqlFactory;
    }

    public void setSqlFactory(SQLFactory sqlFactory) {
        this.sqlFactory = sqlFactory;
    }

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }
}
