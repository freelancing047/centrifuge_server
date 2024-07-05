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
package csi.client.gwt.widget.gxt.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.base.client.field.TriggerFieldDefaultAppearance;

import csi.client.gwt.widget.gxt.form.TriggerBaseCell.TriggerCellAppearance;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TriggerMenuCellAppearance extends TriggerFieldDefaultAppearance implements TriggerCellAppearance {

    public interface TriggerMenuCellResources extends TriggerFieldResources {

//        @Source({ "com/sencha/gxt/theme/base/client/field/ValueBaseField.css",
//                "com/sencha/gxt/theme/base/client/field/TextField.css",
//                "com/sencha/gxt/theme/base/client/field/TriggerField.css" })
//        TriggerMenuStyle css();

        @Source("menuArrow.png")
        ImageResource triggerArrow();

        @Source("menuArrowFocus.png")
        ImageResource triggerArrowOver();

        @Source("menuArrowClick.png")
        ImageResource triggerArrowClick();

        @Source("menuArrowFocus.png")
        ImageResource triggerArrowFocus();
    }

    // Note: Without this interface extension, the image resources defined above get applied to 
    // the default TriggerFieldResource css and end up changing all defaults!
    public interface TriggerMenuStyle extends TriggerFieldStyle {

    }

    public TriggerMenuCellAppearance() {
        super(GWT.<TriggerMenuCellResources> create(TriggerMenuCellResources.class));
    }

    @Override
    public XElement getInputElement(Element parent) {
        return parent.<XElement> cast().selectNode("div"); //$NON-NLS-1$
    }

    @Override
    protected void renderInput(SafeHtmlBuilder shb, String value, SafeStyles inputStyles,
            com.sencha.gxt.cell.core.client.form.FieldCell.FieldAppearanceOptions options) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div "); //$NON-NLS-1$

        if (options.getName() != null) {
            // if set, escape the name property so it is a valid attribute
            sb.append("name='").append(SafeHtmlUtils.htmlEscape(options.getName())).append("' "); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int width = options.getWidth() == -1 ? 140 : options.getWidth();
        sb.append("style='width: ").append(width).append("px;' "); //$NON-NLS-1$ //$NON-NLS-2$

        sb.append(">").append(SafeHtmlUtils.htmlEscape(value)).append("</div>"); //$NON-NLS-1$ //$NON-NLS-2$
        shb.append(SafeHtmlUtils.fromTrustedString(sb.toString()));
    }
}
