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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface Column extends PredicateLHS, BundledColumn, AggregateColumn {

    /**
     * @param alias The alias to use for this column. It is the caller's responsibility to ensure the alias is 
     * unique. Use this only to provide a specific alias. Otherwise an alias is automatically generated for the column.
     */
    public void setAlias(String alias);

    public void setAliasEnabled(boolean b);
}
