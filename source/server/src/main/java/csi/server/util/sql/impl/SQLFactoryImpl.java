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

import csi.server.util.sql.Predicate;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.TableSource;
import csi.server.util.sql.TableSourceFactory;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SQLFactoryImpl implements SQLFactory {

    @Override
    public SelectSQL createSelect(TableSource table) {
        return new SelectSQLImpl(table);
    }

    @Override
    public TableSourceFactory getTableSourceFactory() {
        return new TableSourceFactoryImpl();
    }

    @Override
    public Predicate predicate(boolean conjunctive) {
        return new StarterPredicate(conjunctive);
    }
}
