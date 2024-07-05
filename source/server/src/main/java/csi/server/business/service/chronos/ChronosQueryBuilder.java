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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.visualization.chronos.ChronosSettings;
import csi.server.common.model.visualization.chronos.ChronosViewDef;
import csi.server.common.model.visualization.chronos.EventDefinition;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.spi.SelectSQLSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChronosQueryBuilder extends AbstractQueryBuilder<ChronosViewDef> {
   private static final Logger LOG = LogManager.getLogger(ChronosQueryBuilder.class);

    /**
     * @return A wrapper around the SQL and a list of field references to compute column position of fields.
     */
    public ChronosQuery getQuery() {
        ChronosSettings settings = getViewDef().getChronosSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        // Make a set of all field references in the settings.
        Set<BundledFieldReference> fields = new HashSet<BundledFieldReference>();
        // From events
        for (EventDefinition eventDefinition : settings.getEvents()) {
            fields.addAll(eventDefinition.getFields());
        }
        List<BundledFieldReference> fieldsList = new ArrayList<BundledFieldReference>(fields);

        for (BundledFieldReference reference : fieldsList) {
            Column column = tableSource.getColumn(reference.getFieldDef())
                    .with(reference.getBundleFunction())
                    .withBundleParams(reference.getStringParamters());
            sql.select(column);
        }

        sql.where(getFilterActionsService().getPredicate(getViewDef(), getDataView(), tableSource));

        LOG.info(((SelectSQLSpi) sql).getSQL());

        ChronosQuery cq = new ChronosQuery();
        cq.setFields(fieldsList);
        cq.setSql(sql);
        return cq;
    }
}
