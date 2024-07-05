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

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface SubSelectTableSource extends TableSource {

    /**
     * @return List of columns in the sub-select query.
     */
    public List<? extends Column> getSubSelectColumns();

    /**
     * @param subSelectColumn Column in the sub-select
     * @return Column that references column in subselect query.
     */
    public Column getColumn(Column subSelectColumn);
}
