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

import java.text.ParseException;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.gxt.form.TriggerMenuCell;
import csi.server.common.model.visualization.chart.LabelDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class LabelDefinitionCell extends TriggerMenuCell<LabelDefinition> {

    private LabelDefinitionRetriever retriever;
    private TextField labelFieldEditor = new TextField();

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    public interface LabelDefinitionRetriever {

        /**
         * @param key Key is some identifier that the implementer of this interface is able to use to associate and 
         * retrieve a LabelDefinition.
         * @return
         */
        public LabelDefinition forKey(Object key);
    }
    
    private final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public LabelDefinitionCell(LabelDefinitionRetriever labelDefinitionRetriever) {
        super();
        this.retriever = labelDefinitionRetriever;
        
        setPropertyEditor(new PropertyEditor<LabelDefinition>() {

            @Override
            public LabelDefinition parse(CharSequence text) throws ParseException {
                return null;
            }

            @Override
            public String render(LabelDefinition object) {
                    String labelDescription="";
            	if (!object.isStatic()) {
                    labelDescription =  i18n.labelDefinitionCellFieldName(); //$NON-NLS-1$
                }else {
                    labelDescription = object.getLabelDescription();
                }
                try {
                    if(getWidth() > 0) {
                        while (TextMetrics.get().getWidth(labelDescription) > getWidth()&& labelDescription.length()>0) {
                            labelDescription = labelDescription.substring(0, labelDescription.length() - 2);
                        }
                    } else {
                        labelDescription = "";
                    }
                } catch (Exception e) {

                }
                return labelDescription;
            }
        });

        MenuItem useFieldName = new MenuItem(_constants.labelDefinitionCell_useFieldName());
        useFieldName.addSelectionHandler(new SelectionHandler<Item>() {

            @Override
            public void onSelection(SelectionEvent<Item> event) {
                getLabelDefinition().setFieldName();
                getValueUpdater().update(getLabelDefinition());
            }
        });
        getMenu().add(useFieldName);
        MenuItem staticValueMenu = new MenuItem(_constants.labelDefinitionCell_useStaticValue());
        Menu subMenu = new Menu();
        subMenu.setMinWidth(190);
        staticValueMenu.setSubMenu(subMenu);
        HBoxLayoutContainer container = new HBoxLayoutContainer(HBoxLayoutAlign.MIDDLE);
        
        labelFieldEditor.addStyleName("compact"); //$NON-NLS-1$
        container.add(labelFieldEditor);
        Button button = new Button(_constants.labelDefinitionCell_ok(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //We use the <Field Name> if they don't type in a value
                String labelValue = labelFieldEditor.getValue().trim();
                if(Strings.isNullOrEmpty(labelValue)){
                    getLabelDefinition().setFieldName();
                } else {
                    getLabelDefinition().setStaticLabel(labelFieldEditor.getValue());
                }
                
                getValueUpdater().update(getLabelDefinition());
                getMenu().hide();
            }
        });
        button.setSize(ButtonSize.MINI);
        container.add(button);
        subMenu.add(container);

        getMenu().add(staticValueMenu);
    }

    private LabelDefinition getLabelDefinition() {
        return retriever.forKey(getCurrentContext().getKey());
    }

    @Override
    protected void onTriggerClick(com.google.gwt.cell.client.Cell.Context context, XElement parent, NativeEvent event,
            LabelDefinition value, ValueUpdater<LabelDefinition> updater) {
        super.onTriggerClick(context, parent, event, value, updater);
        LabelDefinition definition = getLabelDefinition();
        if (definition.isStatic()) {
            labelFieldEditor.setValue(definition.getStaticLabel());
        } else {
            labelFieldEditor.setValue(""); //$NON-NLS-1$
        }
    }
}
