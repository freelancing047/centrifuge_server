/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.widget.combo_boxes;

import com.github.gwtbootstrap.client.ui.Collapse;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.*;
import com.sencha.gxt.widget.core.client.cell.HandlerManagerContext;
import com.sencha.gxt.widget.core.client.event.CollapseEvent;
import com.sencha.gxt.widget.core.client.event.ExpandEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.WebMain;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.model.FieldDef;
import csi.shared.core.util.Native;

/**
 * i still can't decide of my other combo box is worth doing at all.
 *
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class FieldDefComboBox extends ComboBox<FieldDef> {

    boolean allowMultiselect = false;
    private HandlerRegistration handlerRegistration;

    interface ComboBoxTemplates extends XTemplates {
        @XTemplate("<span title=\"{name}\"><img width=\"16\" height=\"15\" src=\"{fieldUri}\"/>&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;<span style=\"color:{color}\">{name}</span></span>")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String color, String name);
    }

    static interface FieldDefProperty extends PropertyAccess<FieldDef> {

        ModelKeyProvider<FieldDef> uuid();
    }

    private static FieldDefProperty fieldDefProperty = GWT.create(FieldDefProperty.class);
    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
    private ClientStartupInfo clientStartupInfo = WebMain.getClientStartupInfo();
    HandlerRegistration multiSelectHandlerRegistraion;


    public boolean isAllowMultiselect() {
        return allowMultiselect;
    }

    public void setAllowMultiselect(boolean allowMultiselect) {
        this.allowMultiselect = allowMultiselect;
        // if we enable it, ensure we have it setup
        if(this.allowMultiselect) {
            setupMultiSelect();
        }
    }

    public FieldDefComboBox() {
        super(
                new ListStore<>(fieldDefProperty.uuid()),
                item -> item.getFieldName(),
                new AbstractSafeHtmlRenderer<FieldDef>() {

                    public SafeHtml render(FieldDef item) {
                        SafeUri fieldUri = FieldDefUtils.getFieldTypeImage(item.getFieldType()).getSafeUri();
                        SafeUri dataUri = FieldDefUtils.getDataTypeImage(item.getValueType()).getSafeUri();
                        return comboBoxTemplates.field(fieldUri, dataUri, item.getFieldType().getColor(), item.getFieldName());
                    }
                }
        );

        addStyleName("string-combo-style");
        initialize();
    }

    HandlerRegistration collapseHandler;
    HandlerRegistration trigger;
    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        setSortOrder();
        setupMultiSelect();
    }
    FieldDef selectedItem;
    public void setupMultiSelect() {

        // keeps the dialog expanded
        if(multiSelectHandlerRegistraion != null){
            multiSelectHandlerRegistraion.removeHandler();
        }

        multiSelectHandlerRegistraion = this.addSelectionHandler(event -> {
            scrollTop1 = getCell().getListView().getElement().getScroll().getScrollTop();
            expand();
            selectedItem = event.getSelectedItem();
        });

        // we don't want more than one
        if(trigger != null){
            trigger.removeHandler();
        }

        // this will hide the panel, and add the handlers
        trigger = this.addTriggerClickHandler(triggerClickEvent -> {
            if(this.isExpanded()){
                kill();
            }
            // this works..
            this.getCell().getListView().getElement().getScroll().setScrollTop(scrollTop1);
        });

        final MouseMoveHandler handler = new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                Element parentElement = getListView().getElement().getParentElement();
                int relativeX = event.getRelativeX(parentElement);
                int relativeY = event.getRelativeY(parentElement);
                if (relativeX < 0 || relativeX > parentElement.getOffsetWidth() ||
                relativeY < -50 || relativeY > parentElement.getOffsetHeight()) {
                    kill();
                    if (handlerRegistration != null) {
                        handlerRegistration.removeHandler();
                    }
                }

            }
        };
        handlerRegistration = RootPanel.get().addDomHandler(handler, MouseMoveEvent.getType());

    }

    // i think i could do this with stopping the event from bubbling up
    public void kill() {
        // otherwise we mess with the events for no reason.
        if(this.isExpanded()) {
            this.collapse();
            if (multiSelectHandlerRegistraion != null && collapseHandler != null) {
                multiSelectHandlerRegistraion.removeHandler();
                collapseHandler.removeHandler();
                setupMultiSelect();
            }
        }
    }

    int scrollTop;
    int scrollTop1;
    public void expand() {
        if (collapseHandler != null) {
            collapseHandler.removeHandler();
        }
        setSelectedIndex(getStore().indexOf(selectedItem));
        collapseHandler = this.addCollapseHandler(collapseEvent -> {
            if (allowMultiselect) {
                Cell.Context context = this.createContext();
//                getListView().getElement().getScroll().setScrollTop(scrollTop1);
//                }, 400);

                getCell().expand(context, this.getElement(), this.valueUpdater, this.getValue());

            }
        });
    }

    /**
     * Gets the default sort order from ClientStartupInfo and sorts the items in the combobox in that order.
     */
    public void setSortOrder() {
        Store.StoreSortInfo<FieldDef> sortInfo = new Store.StoreSortInfo<FieldDef>(clientStartupInfo.isSortAlphabetically() ? FieldDefUtils.SORT_ALPHABETIC : FieldDefUtils.SORT_ORDINAL, SortDir.ASC);
        this.getStore().clearSortInfo();
        this.getStore().addSortInfo(sortInfo);
    }


    public int getItemCount() {
        return getStore().size();
    }

    public void incrementSelected() {
        FieldDef a = getStore().get((this.getSelectedIndex() + 1) % this.getItemCount());

        this.select(a);
        this.setValue(a);
        this.setText(a.getFieldName());

    }

    public int getSelectedIndex() {
        return getStore().indexOf(getCurrentValue());
    }

    public void setSelectedIndex(int i) {
        setValue(getStore().get(i));
    }

    public String getCurrentCellText() {

        return ((ComboBoxEditCell<FieldDef>) getCell()).getSelectionText();
    }

    public void removeDefaultStyle() {
        this.removeStyleName("string-combo-style");
    }
}
