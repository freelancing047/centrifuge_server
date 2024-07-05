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

import csi.server.common.model.SortOrder;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface SelectSQL {

    /**
     * @param columns Columns to select.
     * @return Self reference
     */
    public SelectSQL select(Column... columns);

    /**
     * @param columns Columns to group by. These should be the same object reference that was added to select and
     * should be added to select before being added to group-by.
     * @return
     */
    public SelectSQL groupBy(Column... columns);

    /**
     * @param column Column to order by. This should be the same object reference that was added to select and should
     * be added to select before being added to order-by.
     * @param sortOrder
     * @return
     */
    public SelectSQL orderBy(Column column, SortOrder sortOrder);

    /**
     * @param predicates each of the predicates is ANDed together.
     * @return
     */
    public SelectSQL where(Predicate... predicates);

    /**
     * @param predicates each of the predicates is ANDed together. The columns referenced in the predicates MUST 
     * appear in the group-by clause.
     * @return self reference
     */
    public SelectSQL having(Predicate... predicates);

    public SelectSQL offset(int offset);

    public SelectSQL limit(int limit);

    /**
     * @return Executes the query and returns an in-memory result set.
     */
    public SelectResultSet execute();
    
    public <T> T scroll(ScrollCallback<T> callback);

    /**
     * Sets the DISTINCT clause for the select.
     * @return self reference
     */
    public SelectSQL distinct();
}
