package csi.client.gwt.edit_sources.right_panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
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
import csi.client.gwt.edit_sources.dialogs.common.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.UUID;
import csi.server.common.model.query.QueryParameterDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 12/5/2017.
 */
public class ParameterListEditor extends ResizeComposite {

    interface QueryParameterDefPropertyAccess extends PropertyAccess<QueryParameterDefReference> {

        @Editor.Path("parameter.uuid")
        public ModelKeyProvider<QueryParameterDefReference> key();

        public ValueProvider<QueryParameterDefReference, QueryParameterDef> parameter();

        public ValueProvider<QueryParameterDefReference, String> prompt();

        public ValueProvider<QueryParameterDefReference, String> type();

    }

    interface SpecificUiBinder extends UiBinder<Widget, ParameterListEditor> {
    }

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField(provided = true)
    String addButton = i18n.inputParameterListDialogAddButton(); //$NON-NLS-1$

    @UiField(provided = true)
    String deleteButton = i18n.inputParameterListDialogDeleteButton(); //$NON-NLS-1$

    private static QueryParameterDefPropertyAccess propertyAccess = GWT.create(QueryParameterDefPropertyAccess.class);

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    @UiField
    FullSizeLayoutPanel topPanel;
    @UiField
    MiniBlueButton buttonAdd;
    @UiField
    MiniRedButton buttonDelete;
    @UiField
    GridContainer gridContainer;

    @Override
    public Widget asWidget() {

        return topPanel;
    }

    private Grid<QueryParameterDefReference> grid;

    private ParameterPresenter _parameterPresenter;

    public ParameterListEditor(ParameterPresenter parameterPresenterIn) {
        super();
        _parameterPresenter = parameterPresenterIn;

        initWidget(uiBinder.createAndBindUi(this));
        initGrid();
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
        qpDialog.show();
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

                if ((null != myParameter) && (!myParameter.isSystemParam()) && (!myParameter.isInUse())) {

                    myEnabledFlag = true;
                }
            }
        }

        buttonDelete.setEnabled(myEnabledFlag);
    }
}
