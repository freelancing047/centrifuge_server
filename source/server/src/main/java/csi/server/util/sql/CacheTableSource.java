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

import csi.server.common.model.FieldDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface CacheTableSource extends TableSource {

    /**
     * @param fieldDef
     * @return Column for the field definition. Each call returns a new column (with a distinct alias) so that the
     * same field-def can be present multiple times in the select list. 
     */
    public Column getColumn(FieldDef fieldDef);

    /**
     * @return Column representing the internal id-column of the cache table. Does not allow aggregate functions.
     */
    public Column getIdColumn();
    

    /**
     * @return Column representing the internal id-column of the cache table, but allows for aggregates
     */
    public Column getRawIdColumn();
}
