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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class AliasFactory {

    private static final String PREFIX_ALIAS_TABLE = "t$";
    private static final String PREFIX_ALIAS_COLUMN = "c$";

    // FIXME: Atomic
    private static int tableAliasNumber = 0;
    private static int columnAliasNumber = 0;

    public static String getTableAlias() {
        return PREFIX_ALIAS_TABLE + tableAliasNumber++;
    }

    public static String getColumnAlias() {
        return PREFIX_ALIAS_COLUMN + columnAliasNumber++;
    }

    public static String getAliasFragment(String alias) {
        return " AS " + alias;
    }

    /**
     * For testing support.
     */
    static void reset() {
        tableAliasNumber = 0;
        columnAliasNumber = 0;
    }
}
