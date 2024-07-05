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
package csi.client.gwt.edit_sources.dialogs.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.PreviewResponse;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.TestActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PreviewDialog {

    private Dialog dialog;
    private DataViewDef dataViewDef;

    @UiField
    LayoutPanel mainContainer;
    @UiField
    DivWidget gridWrapper;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    @UiField(provided = true)
	String title = i18n.previewDialogtitle(); //$NON-NLS-1$
    

    interface SpecificUiBinder extends UiBinder<Dialog, PreviewDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public PreviewDialog(DataViewDef dataViewDef) {
        super();
        this.dataViewDef = dataViewDef;

        dialog = uiBinder.createAndBindUi(this);
        dialog.getCancelButton().setVisible(false);
        dialog.getActionButton().setText(i18n.previewDialogCloseButton()); //$NON-NLS-1$
        dialog.hideOnAction();
        dialog.hideOnCancel();
    }

    public void show() {
        final MaskDialog mask = new MaskDialog(i18n.previewDialogMaskMessage()); //$NON-NLS-1$
        mask.show();
        WebMain.injector.getVortex().execute(new Callback<PreviewResponse>() {

            @Override
            public void onSuccess(PreviewResponse result) {
                mask.hide();
                if (result.getSuccess()) {
                    showPreviewData(result);
                } else {
                    InfoDialog info = new InfoDialog(i18n.previewDialogErrorMessage(), result.getErrorMsg()); //$NON-NLS-1$
                    info.setBodyWidth("400px"); //$NON-NLS-1$
                    info.setBodyHeight("50px"); //$NON-NLS-1$
                    info.show();
                }
            }
            
            
        }, TestActionsServiceProtocol.class).previewData(dataViewDef);
    }

    private void showPreviewData(PreviewResponse result) {
        ModelKeyProvider<PreviewItem> keyProvider = new PreviewItemKeyProvider();
        ListStore<PreviewItem> store = new ListStore<PreviewItem>(keyProvider);

        List<ColumnConfig<PreviewItem, ?>> columnConfigs = getColumnConfigs();
        ColumnModel<PreviewItem> cm = new ColumnModel<PreviewItem>(columnConfigs);

        Grid<PreviewItem> grid = new ResizeableGrid<PreviewItem>(store, cm);
        GridHelper.setDraggableRowsDefaults(grid);

        gridWrapper.add(grid);
        if (columnConfigs.size() > 4) {
            mainContainer.getWidgetContainerElement(gridWrapper).getStyle().setOverflow(Overflow.AUTO);
            gridWrapper.getElement().getStyle().setWidth(columnConfigs.size() * 150, Unit.PX);
        }
        grid.getStore().addAll(getPreviewItems(result));
        dialog.show();
    }

    private Collection<? extends PreviewItem> getPreviewItems(PreviewResponse result) {
        List<PreviewItem> list = new ArrayList<PreviewItem>();

        for (CsiMap<String, String> dataMap : result.getPreviewData()) {
            list.add(new PreviewItem(dataMap));
        }
        return list;
    }

    private List<ColumnConfig<PreviewItem, ?>> getColumnConfigs() {
        List<FieldDef> fieldDefs = dataViewDef.getFieldList();

        List<ColumnConfig<PreviewItem, ?>> ccList = new ArrayList<ColumnConfig<PreviewItem, ?>>();

        for (FieldDef fieldDef : fieldDefs) {
            ColumnConfig<PreviewItem, String> cc = new ColumnConfig<PreviewItem, String>(new PreviewItemValueProvider(
                    fieldDef), 150, fieldDef.getFieldName());
            cc.setSortable(false);
            ccList.add(cc);
        }

        return ccList;
    }

}