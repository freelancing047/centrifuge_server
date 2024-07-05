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

import csi.server.common.enumerations.CsiDataType;

/**
 * Allows definition of the value of the predicate.
 * @author Centrifuge Systems, Inc.
 *
 */
public interface PredicateFragment {

    /**
     * @param value Primitive value (or wrapper of primitive) or Date
     * @return
     */
    public <T> Predicate value(T value);

    /**
     * @param column Another column from a table.
     * @return
     */
    public Predicate column(Column column);

    /**
     * @param values List of values for use in IN clause
     * @param dataType Type of the value
     * @return
     */
    public Predicate list(List<? extends Object> values, CsiDataType dataType);

    /**
     * @param selectSql A select query 
     * @return
     */
    public Predicate subselect(SelectSQL selectSql);

    /**
     * @param arbitrarySQL A SQL to use for sub-select.
     */
    public Predicate subSelect(String arbitrarySQL);
    
    /**
     * @param value 
     * @param dataType Data type to coerce the value to.
     * @return
     */
    public Predicate value(Object value, CsiDataType dataType);

    Predicate value(Object value, CsiDataType dataType, boolean nullCheck);

    
}
