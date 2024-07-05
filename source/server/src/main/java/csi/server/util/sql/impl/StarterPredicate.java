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

import java.util.List;

import com.google.common.collect.Lists;

import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class StarterPredicate implements PredicateSpi {

    private boolean conjunctive;
    private PredicateSpi addition;

    public StarterPredicate(boolean conjunctive) {
        this.conjunctive = conjunctive;
    }

    @Override
    public Predicate or(Predicate predicate) {
        addition = (PredicateSpi) predicate;
        return predicate;
    }

    @Override
    public Predicate and(Predicate predicate) {
        addition = (PredicateSpi) predicate;
        return predicate;
    }

    @Override
    public Predicate negate() {
        // Noop
        return this;
    }

    @Override
    public String getAliasedSQL() {
        if (addition != null) {
            return addition.getAliasedSQL();
        } else if (conjunctive) {
            return " 1 = 1 ";
        } else {
            return " 1 != 1 ";
        }
    }

    @Override
    public String getSQL() {
        if (addition != null) {
            return addition.getAliasedSQL();
        } else if (conjunctive) {
            return " 1 = 1 ";
        } else {
            return " 1 != 1 ";
        }
    }

    @Override
    public List<Column> getColumns() {
        if (addition != null) {
            return addition.getColumns();
        } else {
            return Lists.newArrayList();
        }
    }
}
