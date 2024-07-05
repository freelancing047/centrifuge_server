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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface HasAliasedSQLRepresentation extends HasSQLRepresentation {

    /**
     * @return SQL statement that utilizes aliases (e.g. for a table, a table alias is added, for columns, 
     * an alias is added (note: default behavior of columns is to use table alias reference).
     */
    public String getAliasedSQL();
}
