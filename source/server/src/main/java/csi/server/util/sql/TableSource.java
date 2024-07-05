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

import csi.server.common.enumerations.CsiDataType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface TableSource extends NamedColumnSource {

    /**
     * @param constantValue 
     * @return A column with a literal value.
     */
    public <T> Column createConstantColumn(T constantValue);

    /**
     * @param constantValue
     * @param dataType
     * @return Column with literal value coerced to given type.
     */
    public <T> Column createConstantColumn(T constantValue, CsiDataType dataType);
    
    /**
     * @param columns One or more columns that need to be wrapped with distinct.
     * @return Distinct column that wraps the given columns. Note: This type of a column should not appear anywhere
     * other than the select clause and having clause.
     */
    public Column createDistinctColumn(Column ... columns);
}
