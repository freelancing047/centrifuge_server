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

import csi.server.util.sql.Column;
import csi.server.util.sql.PredicateFragment;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface PredicateFragmentSpi extends PredicateFragment {

    /**
     * @return
     */
    public String getSQLFragment();

    /**
     * @return SQL fragment wherein column uses table alias. 
     */
    public String getAliasedSQLFragment();

    /**
     * @return LHS column of this predicate fragment.
     */
    public Column getColumn();

    public String getRelationalOperator();

}
