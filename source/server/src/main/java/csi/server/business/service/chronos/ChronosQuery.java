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
package csi.server.business.service.chronos;

import java.util.List;

import csi.server.common.model.BundledFieldReference;
import csi.server.util.sql.SelectSQL;

/**
 * A packaging of ordered fields and sql. The field position corresponds to the SQL columns.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChronosQuery {

    private SelectSQL sql;
    private List<BundledFieldReference> fields;

    public SelectSQL getSql() {
        return sql;
    }

    public void setSql(SelectSQL sql) {
        this.sql = sql;
    }

    public List<BundledFieldReference> getFields() {
        return fields;
    }

    public void setFields(List<BundledFieldReference> fields) {
        this.fields = fields;
    }

    /**
     * @param start
     * @return 1 based index of field in the SQL column select list, 0 if unavailable.
     */
    public int getIndexOf(BundledFieldReference start) {
        return fields.indexOf(start) + 1;
    }

}
