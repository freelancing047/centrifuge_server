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
package csi.client.gwt.viz.chart.settings;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.visualization.chart.SortDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SortDefinitionNameCell extends AbstractCell<SortDefinition> {

    interface FieldTemplate extends XTemplates {

        @XTemplate("<span class=\"attributeComboType\">[{type}]</span>&nbsp;"
                + "<img width=\"16\" height=\"15\" src=\"{fieldUri}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\">&nbsp;&nbsp;{name}")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String name, String type);

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{fieldUri}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\">&nbsp;&nbsp;{name}")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String name);

        @XTemplate("<span class=\"attributeComboType\">[{measure}]</span>&nbsp;{countStar}")
        SafeHtml countStar(String measure, String countStar);

    }

    private static final FieldTemplate fieldTemplate = GWT.create(FieldTemplate.class);

    public SortDefinitionNameCell() {
        super();
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, SortDefinition item, SafeHtmlBuilder sb) {
        if (item.isCountStar()) {
            sb.append(fieldTemplate.countStar(CentrifugeConstantsLocator.get().measure(), CentrifugeConstantsLocator.get().countStar()));
        } else {
            SafeUri fieldUri = FieldDefUtils.getFieldTypeImage(
                    item.getChartAttributeDefinition().getFieldDef().getFieldType()).getSafeUri();
            SafeUri dataUri = FieldDefUtils.getDataTypeImage(item.getChartAttributeDefinition().getDerivedType())
                    .getSafeUri();
            sb.append(fieldTemplate.field(fieldUri, dataUri, item.getChartAttributeDefinition().getComposedName(), item
                    .getChartAttributeDefinition().getDefinitionName()));
        }
    }
}
