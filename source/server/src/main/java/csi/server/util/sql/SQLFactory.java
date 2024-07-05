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
public interface SQLFactory {

    /**
     * @param table
     * @return Select query constructor.
     */
    public SelectSQL createSelect(TableSource table);

    /**
     * @return Table source factory to create table sources.
     */
    public TableSourceFactory getTableSourceFactory();

    /**
     * @param conjunctive if true, will set it up for a conjunctive case wherein if there are no predicates that are
     * combined with this and this predicate is ANDed with something else or added to the where clause, the predicate 
     * will turn to 1 == 1 or false for disjunctive case wherein it will turn to 1 != 1. 
     * @return A predicate that can be used to start a conjunctive or disjunctive clause.
     */
    public Predicate predicate(boolean conjunctive);
}
