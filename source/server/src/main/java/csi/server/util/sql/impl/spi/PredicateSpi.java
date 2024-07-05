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
package csi.server.util.sql.impl.spi;

import java.util.List;

import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface PredicateSpi extends Predicate, HasAliasedSQLRepresentation {

    /**
     * @return Columns represented in the LHS of this predicate (if this is a compound predicate, then each of the 
     * columns in the LHS).
     */
    public List<Column> getColumns();
    
}
