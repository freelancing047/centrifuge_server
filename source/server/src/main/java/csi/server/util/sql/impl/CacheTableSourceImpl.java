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
package csi.server.util.sql.impl;

import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.util.CacheUtil;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CacheTableSourceImpl extends AbstractTableSource implements CacheTableSource {

    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public CacheTableSourceImpl(DataView dataView) {
        tableName = CacheUtil.getQuotedCacheTableName(dataView.getUuid());
    }

    @Override
    public Column getColumn(FieldDef fieldDef) {
        return new CacheColumn(this, fieldDef);
    }
    
    @Override
    public Column getIdColumn() {
        return new InternalIdColumn(this);
    }
    
    @Override
    public Column getRawIdColumn() {
        return new CacheColumn(this, null){
            
            @Override
            public String getSQLWithoutTableAlias() {
                return "internal_id";
            }
            
            @Override
            public String getSQL() {
                
                String fieldExpression = getTableSource().getAlias() + "." + getSQLWithoutTableAlias();

                return getAggregatedBundledColumnExpression(fieldExpression);
            }

            
        };
    }

    @Override
    public String getSQL() {
        return getTableName();
    }
}
