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
package csi.client.gwt.widget.misc;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.visualization.AbstractAttributeDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class AttributeDefinitionNameCell<M extends AbstractAttributeDefinition> extends AbstractCell<M> {

    interface FieldTemplate extends XTemplates {

        @XTemplate("<span class=\"attributeComboType\">[{type}]</span>&nbsp;"
                + "<img width=\"16\" height=\"15\" src=\"{fieldUri}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\">&nbsp;&nbsp;{name}")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String name, String type);

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{fieldUri}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\">&nbsp;&nbsp;{name}")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String name);

    }

    private static final FieldTemplate fieldTemplate = GWT.create(FieldTemplate.class);

    private boolean includeItemDefinition = true;

    public AttributeDefinitionNameCell() {
        super();
    }

    public boolean isIncludeItemDefinition() {
        return includeItemDefinition;
    }

    public void setIncludeItemDefinition(boolean includeItemDefinition) {
        this.includeItemDefinition = includeItemDefinition;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, AbstractAttributeDefinition item,
            SafeHtmlBuilder sb) {
        sb.append(toHtml(item, isIncludeItemDefinition()));
    }

    public static SafeHtml toHtml(AbstractAttributeDefinition item, boolean includeItemDefinition) {
        SafeUri fieldUri = FieldDefUtils.getFieldTypeImage(item.getFieldDef().getFieldType()).getSafeUri();
        SafeUri dataUri = FieldDefUtils.getDataTypeImage(item.getDerivedType()).getSafeUri();
        if (includeItemDefinition) {
            return fieldTemplate.field(fieldUri, dataUri, item.getComposedName(), item.getDefinitionName());
        } else {
            return fieldTemplate.field(fieldUri, dataUri, item.getComposedName());
        }
    }
}
