package csi.client.gwt.viz.graph.node.settings.tooltip;

import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Fieldset;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.viz.graph.node.settings.NodeSettings;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipItem;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipItems;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.model.FieldDef;

public class TooltipTab {

    interface NodeTooltipPropertyAccess extends PropertyAccess<NodeTooltip> {

        ModelKeyProvider<NodeTooltip> key();

        ValueProvider<NodeTooltip, String> displayName();

        ValueProvider<NodeTooltip, Boolean> fixed();

        ValueProvider<NodeTooltip, String> value();

        ValueProvider<NodeTooltip, FieldDef> fieldDef();

        ValueProvider<NodeTooltip, Void> voidfn();
    }

    interface SpecificUiBinder extends UiBinder<Tab, TooltipTab> {
    }

    public class tooltipSelectionChangeHandler implements SelectionHandler<NodeTooltip> {

        @Override
        public void onSelection(SelectionEvent<NodeTooltip> event) {
            detailsColumn.setVisible(true);
            NodeTooltip tooltip = event.getSelectedItem();
            nameTextBox.setValue(tooltip.getDisplayName());
            TooltipType tooltipType = tooltip.getType();
            if (tooltipType == null) {
                tooltipType = TooltipType.FIXED;
            }
            typeListBox.setValue(tooltipType.toString(), true);
            updateValueInputs();
            valueTextBox.setValue(tooltip.getValue());
            fieldFDCB.setValue(tooltip.getFieldDef());
            functionComboBox.setValue(tooltip.getFunction());
            functionComboBox.setAllowTextSelection(false);
            functionComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
            functionComboBox.addStyleName("string-combo-style");
            graphAttributeComboBox.setValue(tooltip.getGraphAttribute());
            showAsLinkCB.setValue(tooltip.isLink());
            if (tooltip.isLink()) {
                setIsLink();
            }
            else{
                setNotLink();
            }
            boolean isLinkFixed = tooltip.getLinkType().equals(AnchorLinkType.FIXED);
            fixedLinkRB.setValue(isLinkFixed);
            if (isLinkFixed) {
                setLinkFixed();
            }

            boolean isDynLink = tooltip.getLinkType().equals(AnchorLinkType.DYNAMIC);
            dynamicLinkRB.setValue(isDynLink);
            if (isDynLink) {
                setLinkDynamic();
            }
            boolean isLinkNeither = tooltip.getLinkType().equals(AnchorLinkType.SIMPLE);
            neitherLinkRB.setValue(isLinkNeither);
            if (isLinkNeither) {
                setLinkNeither();
            }
            linkTextBox.setValue(tooltip.getLinkText());
            linkFDCB.setValue(tooltip.getLinkFeildDef());
            hideEmptyCheckBox.setValue(tooltip.hideEmpty());
            updateTooltipFunctions(tooltip.getFieldDef());
        }
    }

    @UiField
    CheckBox hideEmptyCheckBox;
    @UiField
    ControlGroup functionControlGroup;
    @UiField
    ControlGroup valueControlGroup;
    @UiField
    ControlGroup fieldControlGroup;
    @UiField
    TextBox valueTextBox;
    private Grid<NodeTooltip> grid;
    private NodeSettings nodeSettings;
    @UiField
    TextBox linkTextBox;
    @UiField
    RadioButton dynamicLinkRB;
    @UiField
    RadioButton fixedLinkRB;
    @UiField
    GridContainer gridContainer;

    private static final NodeTooltipPropertyAccess propertyAccess = GWT.create(NodeTooltipPropertyAccess.class);
    private Tab tab;
    private static int counter = 0; // TODO: DELETE ME
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    @UiField
    FieldDefComboBox linkFDCB;
    @UiField
    FieldDefComboBox fieldFDCB;
    @UiField
    TextBox nameTextBox;
    @UiField
    StringComboBox typeListBox;
    @UiField(provided = true)
    ComboBox<TooltipFunction> functionComboBox;
    @UiField
    RadioButton neitherLinkRB;
    @UiField
    CheckBox showAsLinkCB;
    @UiField(provided = true)
    ComboBox<ToolTipItem> graphAttributeComboBox;

    @UiField
    ControlGroup graphAttributeControlGroup;

    @UiField
    Column detailsColumn;
    @UiField
    Fieldset detailsFieldset;
    @UiField
    ControlGroup optionsControlGroup;

    public TooltipTab(NodeSettings nodeSettings) {
        // TODO: move to new nested class
        functionComboBox = new ComboBox<>(new ComboBoxCell<>(
                new ListStore<>(new ModelKeyProvider<TooltipFunction>() {

                    @Override
                    public String getKey(TooltipFunction item) {
                        return item.toString();
                    }
                }), new LabelProvider<TooltipFunction>() {

                    @Override
                    public String getLabel(TooltipFunction item) {
                        return item.getDisplayString();
                    }
                }));
        functionComboBox.getStore().addAll(Lists.newArrayList(TooltipFunction.values()));

        functionComboBox.addSelectionHandler(new SelectionHandler<TooltipFunction>() {

            @Override
            public void onSelection(SelectionEvent<TooltipFunction> event) {
                NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
                if (tooltip == null) {
                    return;
                }
                tooltip.setFunction(event.getSelectedItem());

            }
        });

        graphAttributeComboBox = new ComboBox<>(new ComboBoxCell<>(new ListStore<>(
                new ModelKeyProvider<ToolTipItem>() {

                    @Override
                    public String getKey(ToolTipItem item) {
                        return item.getLabel();
                    }

                }), new LabelProvider<ToolTipItem>() {

            @Override
            public String getLabel(ToolTipItem item) {
                return item.toString();
            }
        }));

        graphAttributeComboBox.getStore().addAll(Lists.newArrayList(ToolTipItems.values()));
        graphAttributeComboBox.addValueChangeHandler(new ValueChangeHandler<ToolTipItem>() {

            @Override
            public void onValueChange(ValueChangeEvent<ToolTipItem> event) {
                NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
                if (tooltip == null) {
                    return;
                }
                tooltip.setGraphAttribute(event.getValue());

            }
        });

        tab = uiBinder.createAndBindUi(this);
        this.nodeSettings = nodeSettings;
        createGrid();
        createTooltipTypes();
        createTooltipFunctions();
        neitherLinkRB.setValue(true);

        populateFieldFDCB(nodeSettings);
        populateLinkFDCB(nodeSettings);

        detailsFieldset.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
    }

    public void add(NodeTooltip tooltip) {
        grid.getStore().add(tooltip);
    }

    private void createGrid() {
        final GridComponentManager<NodeTooltip> manager = WebMain.injector.getGridFactory()
                .create(propertyAccess.key());

        ColumnConfig<NodeTooltip, String> dragCol = manager.create(propertyAccess.displayName(), 20, "", false, true);
        dragCol.setCell(DragCell.<String> create());
        dragCol.setResizable(false);

        // Display Name

        ColumnConfig<NodeTooltip, String> displayAsCol = manager.create(propertyAccess.displayName(), 80, CentrifugeConstantsLocator.get().name(),
                false, true);
        TextCell cell2 = new TextCell();
        displayAsCol.setCell(cell2);

        ColumnConfig<NodeTooltip, String> valueToolipCol = manager.create(propertyAccess.value(), 120, CentrifugeConstantsLocator.get().value(), false,
                true);
        TextCell cell3 = new TextCell();
        valueToolipCol.setCell(cell3);

        final ColumnConfig<NodeTooltip, Void> deleteToolipCol = manager.create(propertyAccess.voidfn(), 15, "", false,
                true);
        IconCell icon = new IconCell(IconType.REMOVE);
        deleteToolipCol.setCell(icon);

        List<ColumnConfig<NodeTooltip, ?>> columns = manager.getColumnConfigList();
        ColumnModel<NodeTooltip> cm = new ColumnModel<>(columns);
        ListStore<NodeTooltip> gridStore = manager.getStore();
        grid = new ResizeableGrid<>(gridStore, cm);
        grid.getView().setAutoExpandColumn(valueToolipCol);
        grid.getStore().setAutoCommit(true);
        GridHelper.setDraggableRowsDefaults(grid);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionHandler(new tooltipSelectionChangeHandler());
        grid.addCellClickHandler(event -> {
            ListStore<NodeTooltip> store = grid.getStore();
            int rowIndex = event.getRowIndex();
            int cellIndex = event.getCellIndex();
            int delColIndex = grid.getColumnModel().indexOf(deleteToolipCol);
            if (cellIndex == delColIndex) {
                store.remove(rowIndex);
                detailsColumn.setVisible(false);
            }
        });

        gridContainer.setGrid(grid);
    }

    private void createTooltipFunctions() {

    }

    private void createTooltipTypes() {
        TooltipType[] tooltipTypes = TooltipType.values();
        for (TooltipType tooltipType : tooltipTypes) {
            //FIXME: Temporary blacklisting
            if(tooltipType.GRAPH_ATTRIBUTE.equals(tooltipType)){
                continue;
            }
            typeListBox.getStore().add(tooltipType.toString());
        }
    }

    public Tab getTab() {
        return tab;
    }

    public List<NodeTooltip> getTooltips() {
        return grid.getStore().getAll();

    }

    @UiHandler("addButton")
    public void onAddButtonDyn(ClickEvent e) {
        NodeTooltip tooltip = new NodeTooltip();
        tooltip.setDisplayName(CentrifugeConstantsLocator.get().tooltip() + counter);
        tooltip.setValue(CentrifugeConstantsLocator.get().value() + counter++);
        tooltip.setType(TooltipType.DYNAMIC);
        tooltip.setFieldDef(fieldFDCB.getStore().get(0));
        
        add(tooltip);
        grid.getSelectionModel().select(false, tooltip);
    }

    @UiHandler("dynamicLinkRB")
    public void onDynamicLinkRB(ClickEvent e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        if (dynamicLinkRB.getValue()) {
            tooltip.setLinkType(AnchorLinkType.DYNAMIC);
            setLinkDynamic();
        }

    }

    @UiHandler("fieldFDCB")
    public void onFieldChange(ValueChangeEvent<FieldDef> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setField(e.getValue());
        updateTooltipFunctions(e.getValue());
    }

    @UiHandler("fixedLinkRB")
    public void onFixedLinkRB(ClickEvent e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        if (fixedLinkRB.getValue()) {
            tooltip.setLinkType(AnchorLinkType.FIXED);
            setLinkFixed();
        }
    }

    public void onFunctionChange(ValueChangeEvent<TooltipFunction> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setFunction(e.getValue());
    }

    @UiHandler("hideEmptyCheckBox")
    public void onHideEmptyCheckBox(ClickEvent e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setHideEmpty(hideEmptyCheckBox.getValue());
    }

    @UiHandler("linkTextBox")
    public void onLinkTextBox(ValueChangeEvent<String> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setLinkText(e.getValue());
    }

    @UiHandler("nameTextBox")
    public void onNameChange(ValueChangeEvent<String> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setDisplayName(e.getValue());
        grid.getStore().update(tooltip);
    }

    @UiHandler("neitherLinkRB")
    public void onNeitherLinkRB(ClickEvent e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        if (neitherLinkRB.getValue()) {
            tooltip.setLinkType(AnchorLinkType.SIMPLE);
            setLinkNeither();
        }

    }

//    @UiHandler("showAsLinkCB")
//    public void onShowAsLinkCB(ClickEvent e) {
//        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
//        if (tooltip == null) {
//            return;
//        }
//        tooltip.setLink(showAsLinkCB.getValue());
//    }

    @UiHandler("showAsLinkCB")
    public void onShowAsLinkCB(ValueChangeEvent<Boolean> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        tooltip.setLink(e.getValue());
        if (e.getValue()) {
            setIsLink();
            neitherLinkRB.setValue(true,true);
            onNeitherLinkRB(null);
        } else {
            setNotLink();
            tooltip.setLinkType(null);
        }
    }

    @UiHandler("typeListBox")
    public void onTypeChange(SelectionEvent<String> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        if (tooltip == null) {
            return;
        }
        String itemText = e.getSelectedItem();
        TooltipType[] tooltipTypes = TooltipType.values();
        for (TooltipType tooltipType : tooltipTypes) {
            if (itemText.equals(tooltipType.toString())) {
                tooltip.setType(tooltipType);
                updateValueInputs();
                grid.getStore().update(tooltip);
            }
        }
    }

    @UiHandler("valueTextBox")
    public void onValueChange(ValueChangeEvent<String> e) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        tooltip.setValue(e.getValue());
        grid.getStore().update(tooltip);
    }

    private void populateFieldFDCB(NodeSettings nodeSettings) {
        fieldFDCB.getStore().addAll(
                FieldDefUtils.getAllSortedFields(
                        nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        fieldFDCB.addSelectionHandler(new SelectionHandler<FieldDef>() {
            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                FieldDef selectedItem = event.getSelectedItem();
                updateTooltipFunctions(selectedItem);

            }
        });
        fieldFDCB.addValueChangeHandler(new ValueChangeHandler<FieldDef>() {

            @Override
            public void onValueChange(ValueChangeEvent<FieldDef> event) {
            }
        });
    }

    public void updateTooltipFunctions(FieldDef field) {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        ListStore<TooltipFunction> store = functionComboBox.getStore();
        store.clear();
        TooltipFunction oldFunction = functionComboBox.getValue();
        for (TooltipFunction tooltipFunction : TooltipFunction.values()) {
            if (field != null && field.getValueType() !=null &&tooltipFunction.isSupported(field.getValueType())) {
                store.add(tooltipFunction);
            }
        }
        if (oldFunction != null && store.indexOf(oldFunction) != -1) {
            functionComboBox.setValue(oldFunction);
            tooltip.setFunction(oldFunction);
        } else if (!store.getAll().isEmpty()) {
            TooltipFunction value = store.get(0);
            tooltip.setFunction(value);
            functionComboBox.setValue(value);
        }
    }

    private void populateLinkFDCB(NodeSettings nodeSettings) {
        linkFDCB.getStore().addAll(
                FieldDefUtils.getAllSortedFields(
                        nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        linkFDCB.addValueChangeHandler(new ValueChangeHandler<FieldDef>() {

            @Override
            public void onValueChange(ValueChangeEvent<FieldDef> event) {
                NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
                if (tooltip == null) {
                    return;
                }
                tooltip.setLinkFeildDef(event.getValue());
            }
        });
    }

    public void setIsLink() {
        fixedLinkRB.setEnabled(true);
        neitherLinkRB.setEnabled(true);
        dynamicLinkRB.setEnabled(true);
    }

    public void setLinkDynamic() {
        linkTextBox.setVisible(false);
        linkFDCB.setVisible(true);
    }

    public void setLinkFixed() {
        linkTextBox.setVisible(true);
        linkFDCB.setVisible(false);
    }

    public void setLinkNeither() {
        linkTextBox.setVisible(false);
        linkFDCB.setVisible(false);
    }

    public void setNotLink() {
        fixedLinkRB.setEnabled(false);
        neitherLinkRB.setEnabled(false);
        dynamicLinkRB.setEnabled(false);
    }

    public void updateValueInputs() {
        NodeTooltip tooltip = grid.getSelectionModel().getSelectedItem();
        switch (tooltip.getType()) {
            case COMPUTED:
                valueControlGroup.setVisible(false);
                fieldControlGroup.setVisible(true);
                functionControlGroup.setVisible(true);
                graphAttributeControlGroup.setVisible(false);
                optionsControlGroup.setVisible(false);
                break;
            case DYNAMIC:
                valueControlGroup.setVisible(false);
                fieldControlGroup.setVisible(true);
                functionControlGroup.setVisible(false);
                graphAttributeControlGroup.setVisible(false);
                optionsControlGroup.setVisible(true);
                break;
            case FIXED:
                valueControlGroup.setVisible(true);
                fieldControlGroup.setVisible(false);
                functionControlGroup.setVisible(false);
                graphAttributeControlGroup.setVisible(false);
                optionsControlGroup.setVisible(true);
                //
                valueTextBox.setValue(tooltip.getValue());
                break;
            case GRAPH_ATTRIBUTE:
                valueControlGroup.setVisible(false);
                fieldControlGroup.setVisible(false);
                functionControlGroup.setVisible(false);
                graphAttributeControlGroup.setVisible(true);
                optionsControlGroup.setVisible(true);
                break;
            default:
                break;
        }
    }
}
