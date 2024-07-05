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
package csi.client.gwt.edit_sources.dialogs.parameter;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterDefEditCell;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterDefReference;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterDialog;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterNameCell;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.UUID;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class InputParameterListDialog extends WatchingParent {

    interface SpecificUiBinder extends UiBinder<Dialog, InputParameterListDialog> {
    }

    interface QueryParameterDefPropertyAccess extends PropertyAccess<QueryParameterDefReference> {

        @Editor.Path("parameter.uuid")
        public ModelKeyProvider<QueryParameterDefReference> key();

        public ValueProvider<QueryParameterDefReference, QueryParameterDef> parameter();

        public ValueProvider<QueryParameterDefReference, String> prompt();

        public ValueProvider<QueryParameterDefReference, String> type();

    }

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	@UiField(provided = true)
	String addButton = i18n.inputParameterListDialogAddButton(); //$NON-NLS-1$
	
	@UiField(provided = true)
	String deleteButton = i18n.inputParameterListDialogDeleteButton(); //$NON-NLS-1$

    private static QueryParameterDefPropertyAccess propertyAccess = GWT.create(QueryParameterDefPropertyAccess.class);

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtDialogTitle = _constants.dataSourceEditor_EditParameterListTitle();
    private static final String _txtHelpTarget = _constants.dataSourceEditor_ParameterListHelpTarget();

    @UiField
    Dialog dialog;
    @UiField
    GridContainer gridContainer;
    @UiField
    MiniRedButton buttonDelete;
    @UiField
    MiniBlueButton buttonAdd;
    @UiField
    TextArea instructionTextArea;

    private Grid<QueryParameterDefReference> grid;

    private ParameterPresenter _parameterPresenter;

    public InputParameterListDialog(ParameterPresenter parameterPresenterIn) {
        super();
        _parameterPresenter = parameterPresenterIn;

        uiBinder.createAndBindUi(this);
        
        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);
        
        Button mySaveButton = dialog.getActionButton();
        
        mySaveButton.setText(Dialog.txtSaveButton);
        dialog.hideOnCancel();
        dialog.hideOnAction();

        initGrid();
        addHandlers();
    }

    private void addHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
            }
        });
    }

    public void show() {
        dialog.show(60);
    }

    public void hide() {
        dialog.hide();
    }

    public Button getActionButton()
    {
        return dialog.getActionButton();
    }


    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<QueryParameterDefReference> manager = WebMain.injector.getGridFactory().create(
                propertyAccess.key());
        ListStore<QueryParameterDefReference> gridStore = manager.getStore();

        ColumnConfig<QueryParameterDefReference, QueryParameterDef> nameCol = manager.create(
                propertyAccess.parameter(), 125, i18n.inputParameterListDialogNameColumn(),true, true); //$NON-NLS-1$
        nameCol.setCell(new QueryParameterNameCell());
        nameCol.setComparator(QueryParameterDefReference.getComparator());

        final ColumnConfig<QueryParameterDefReference, String> promptCol = manager.create(propertyAccess.prompt(), 200,
                i18n.customQueryDialog_GridCol_Prompt(), true, true);
        promptCol.setCell(new CsiTitleCell());

        final ColumnConfig<QueryParameterDefReference, String> typeCol = manager.create(propertyAccess.type(), 60,
                i18n.customQueryDialog_GridCol_Type(), true, true);
        typeCol.setCell(new CsiTitleCell());

        ColumnConfig<QueryParameterDefReference, QueryParameterDef> editCol = manager.create(
                propertyAccess.parameter(), 60, i18n.inputParameterListDialogEditColumn(),false, true); //$NON-NLS-1$
        editCol.setCell(new QueryParameterDefEditCell(_parameterPresenter.getCurrentNames()));
        editCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        List<ColumnConfig<QueryParameterDefReference, ?>> columns = manager.getColumnConfigList();
        ColumnModel<QueryParameterDefReference> cm = new ColumnModel<QueryParameterDefReference>(columns);

        gridStore.setAutoCommit(true);

        grid = new ResizeableGrid<QueryParameterDefReference>(gridStore, cm, true);
        grid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);

        for (QueryParameterDef param : _parameterPresenter.getParameters()) {
            grid.getStore().add(new QueryParameterDefReference().setParameter(param));
        }
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<QueryParameterDefReference>() {

            public void onSelectionChanged(SelectionChangedEvent<QueryParameterDefReference> eventIn) {

                enableDisableDeleteButton();
            }
        });
        // TODO: Provide ability to delete parameters that are not in use.
        buttonDelete.setVisible(true);
        enableDisableDeleteButton();
    }

    @UiHandler("buttonAdd")
    public void handleAddButton(ClickEvent event) {
        QueryParameterDef def = new QueryParameterDef();
        def.setLocalId(UUID.randomUUID());
        def.setType(CsiDataType.String);

        final QueryParameterDialog qpDialog = new QueryParameterDialog(QueryParameterDialog.Mode.CREATE, def, _parameterPresenter.getCurrentNames());
        qpDialog.setSaveClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                QueryParameterDef qpd = qpDialog.getQueryParameter();
                List<String> names = new ArrayList<String>();
                for (QueryParameterDefReference paramRef : grid.getStore().getAll()) {
                    names.add(paramRef.getParameter().getName());
                }
                String newName = UniqueNameUtil.getDistinctName(names, qpd.getName());
                qpd.setName(newName);
                QueryParameterDefReference myChoice = new QueryParameterDefReference();
                myChoice.setParameter(qpd);
                grid.getStore().add(myChoice);
                grid.getSelectionModel().select(false, myChoice);
                qpDialog.hide();
                _parameterPresenter.addParameter(qpd);
            }
        });
        qpDialog.show(this);
    }

    @UiHandler("buttonDelete")
    public void handleDeleteButton(ClickEvent event) {
        if (grid.getSelectionModel().getSelectedItems().size() == 0) {
            new InfoDialog(i18n.inputParameterListDialogDeleteTitle(), i18n.inputParameterListDialogDeleteMessage()).show(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            WarningDialog dialog = new WarningDialog(i18n.inputParameterListDialogDeleteTitle(), //$NON-NLS-1$
                    i18n.inputParameterListDialogDeleteConfirmation()); //$NON-NLS-1$
            dialog.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    List<QueryParameterDefReference> myList = new ArrayList<QueryParameterDefReference>();
                    for (QueryParameterDefReference ref : grid.getSelectionModel().getSelectedItems()) {
                        myList.add(ref);
                    }
                    for (QueryParameterDefReference ref : myList) {
                        _parameterPresenter.removeParameter(ref.getParameter());
                        grid.getStore().remove(ref);
                    }
                    enableDisableDeleteButton();
                }
            });
            dialog.show();
        }
    }

    private void enableDisableDeleteButton() {

        boolean myEnabledFlag = false;

        if (0 < grid.getStore().getAll().size()) {

            QueryParameterDefReference mySelection = grid.getSelectionModel().getSelectedItem();

            if (null != mySelection) {

                QueryParameterDef myParameter = mySelection.getParameter();

                myEnabledFlag = ((null != myParameter) && (!myParameter.isSystemParam()) && (!myParameter.isInUse()));
            }
        }

        buttonDelete.setEnabled(myEnabledFlag);
    }
}
