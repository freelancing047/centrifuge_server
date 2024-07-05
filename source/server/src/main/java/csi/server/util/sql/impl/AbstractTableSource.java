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
package csi.server.util.sql.impl;

import java.util.Date;

import csi.server.common.enumerations.CsiDataType;
import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractTableSource implements TableSourceSpi {

    private String alias;

    @Override
    public String getAlias() {
        if (alias == null) {
            alias = AliasFactory.getTableAlias();
        }
        return alias;
    }

    @Override
    public String getAliasedSQL() {
        return getSQL() + AliasFactory.getAliasFragment(getAlias());
    }

    @Override
    public <T> Column createConstantColumn(T constantValue) {
        CsiDataType dataType = null;
        if (constantValue instanceof String) {
            dataType = CsiDataType.String;
        } else if (constantValue instanceof Boolean) {
            dataType = CsiDataType.Boolean;
        } else if (constantValue instanceof Date) {
            dataType = CsiDataType.DateTime;
        } else if (constantValue instanceof Number) {
            dataType = CsiDataType.Number;
        } else {
            dataType = CsiDataType.String;
        }
        return new ConstantColumn<T>(this, constantValue, dataType);
    }

    @Override
    public <T> Column createConstantColumn(T constantValue, CsiDataType dataType) {
        return new ConstantColumn<T>(this, constantValue, dataType);
    }

    @Override
    public Column create(String columnName, CsiDataType dataType) {
        throw new RuntimeException("Not yet implemented.");
    }

    @Override
    public Column createDistinctColumn(Column... columns) {
        return new DistinctColumn(this, columns);
    }
}
