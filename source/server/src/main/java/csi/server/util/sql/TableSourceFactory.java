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
package csi.server.util.sql;

import csi.server.common.model.dataview.DataView;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface TableSourceFactory {

    /**
     * @param dataView
     * @return Table source that works with the cache table of a dataview.
     */
    public CacheTableSource create(DataView dataView);

    /**
     * @param tableName
     * @return Table source against an arbitrary table.
     */
    public TableSource create(String tableName);

    /**
     * @param select
     * @return Table source that uses the output of another select as its table.
     */
    public SubSelectTableSource create(SelectSQL select);
}
