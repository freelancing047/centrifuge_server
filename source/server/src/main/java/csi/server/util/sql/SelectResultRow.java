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
 * An in-memory result set.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface SelectResultRow {

    /**
     * @param columnName Name of the column (column alias).
     * @return Value
     */
    public <T> T getValue(String columnName);

    /**
     * @param index 0 based index of column
     * @return Value
     */
    public <T> T getValue(int index);

}
