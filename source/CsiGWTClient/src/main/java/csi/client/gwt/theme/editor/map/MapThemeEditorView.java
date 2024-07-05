package csi.client.gwt.theme.editor.map;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.theme.editor.FilterTextbox;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.PlaceStyleComboBox;
import csi.client.gwt.widget.combo_boxes.ShapeComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.misc.EmptyValueProvider;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;

public class MapThemeEditorView{


    private static MapThemeEditorViewUiBinder uiBinder = GWT.create(MapThemeEditorViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    PlaceStyleProperties placeProps = GWT.create(PlaceStyleProperties.class);
    AssociationStyleProperties associationProps = GWT.create(AssociationStyleProperties.class);
    
    @UiField
    Dialog dialog;
    
    @UiField
    TextBox name;

    @UiField
    Button addButton;
    @UiField
    Button addAssociationButton;
        
    @UiField
    GridContainer placeGridContainer;
    @UiField
    GridContainer associationGridContainer;
    
    @UiField
    ShapeComboBox shapeComboBox;
    

    @UiField(provided=true)
    PlaceStyleComboBox bundleStyle;
        
    ResizeableGrid<PlaceStyle> placeGrid;
    ResizeableGrid<AssociationStyle> associationGrid;


    @UiField
    FilterTextbox placeFilterTextbox;

    @UiField
    FilterTextbox associationFilterTextbox;


    interface MapThemeEditorViewUiBinder extends UiBinder<Widget, MapThemeEditorView> {
    }
    
    public MapThemeEditorView(final MapThemeEditorPresenter presenter){

        bundleStyle = new PlaceStyleComboBox();
        //we aren't ready for this feature yet
        bundleStyle.setVisible(false);
        uiBinder.createAndBindUi(this);

        dialog.setTitle(i18n.themeEditor_map_title());
                
        csi.client.gwt.widget.buttons.Button actionButton = dialog.getActionButton();
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                placeFilterTextbox.clearFilter();
                associationFilterTextbox.clearFilter();
                presenter.saveModel(placeGrid.getStore().getAll(), associationGrid.getStore().getAll(), name.getText());
                dialog.hide();
            }
        });


        name.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.setName(name.getText());
        }});


        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.cancel());
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.discardChanges();
                dialog.hide();
            }
        });

        List<ColumnConfig<PlaceStyle, ?>> placeColumnConfigs = Lists.newArrayList();
        ColumnConfig<PlaceStyle, String> placeNameColumn = new ColumnConfig<PlaceStyle, String>(placeProps.name());
        placeNameColumn.setHeader(i18n.name()); //$NON-NLS-1$
        placeNameColumn.setWidth(170);
        placeColumnConfigs.add(placeNameColumn);
        
        final ColumnConfig<PlaceStyle, Void> placeDeleteColumn = new ColumnConfig<PlaceStyle, Void>(new EmptyValueProvider<PlaceStyle>());
        IconCell deleteCell = new IconCell(IconType.REMOVE);
        deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        placeDeleteColumn.setCell(deleteCell);
        placeDeleteColumn.setWidth(24);
            
        final ColumnConfig<PlaceStyle, Void> placeEditColumn = new ColumnConfig<PlaceStyle, Void>(new EmptyValueProvider<PlaceStyle>());
        IconCell editCell = new IconCell(IconType.PENCIL);
        editCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
        placeEditColumn.setCell(editCell);
        placeEditColumn.setWidth(24);
        placeColumnConfigs.add(placeEditColumn);
        placeColumnConfigs.add(placeDeleteColumn);
        
        ColumnModel<PlaceStyle> placeColumnModel = new ColumnModel<PlaceStyle>(placeColumnConfigs);
        placeGrid = new ResizeableGrid<PlaceStyle>(new ListStore<PlaceStyle>(placeProps.uuid()), placeColumnModel);
        

        placeGridContainer.setGrid(placeGrid);
        placeGridContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

        List<ColumnConfig<AssociationStyle, ?>> associationColumnConfigs = Lists.newArrayList();        
        ColumnConfig<AssociationStyle, String> associationNameColumn = new ColumnConfig<AssociationStyle, String>(associationProps.name());
        associationNameColumn.setHeader(i18n.name()); //$NON-NLS-1$
        associationNameColumn.setWidth(170);
        associationColumnConfigs.add(associationNameColumn);
        
        final ColumnConfig<AssociationStyle, Void> associationDeleteColumn = new ColumnConfig<AssociationStyle, Void>(new EmptyValueProvider<AssociationStyle>());
        IconCell associationDeleteCell = new IconCell(IconType.REMOVE);
        associationDeleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        associationDeleteColumn.setCell(associationDeleteCell);
        associationDeleteColumn.setWidth(24);
            
        final ColumnConfig<AssociationStyle, Void> associationEditColumn = new ColumnConfig<AssociationStyle, Void>(new EmptyValueProvider<AssociationStyle>());
        IconCell associationEditCell = new IconCell(IconType.PENCIL);
        associationEditCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
        associationEditColumn.setCell(associationEditCell);
        associationEditColumn.setWidth(24);
        associationColumnConfigs.add(associationEditColumn);
        associationColumnConfigs.add(associationDeleteColumn);
        
        ColumnModel<AssociationStyle> associationColumnModel = new ColumnModel<AssociationStyle>(associationColumnConfigs);
        associationGrid = new ResizeableGrid<AssociationStyle>(new ListStore<AssociationStyle>(associationProps.uuid()), associationColumnModel);
        
        associationGridContainer.setGrid(associationGrid);

        associationGridContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

        placeFilterTextbox.setAttachedStore(placeGrid.getStore());
        associationFilterTextbox.setAttachedStore(associationGrid.getStore());

        placeGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<PlaceStyle> store = placeGrid.getStore();
                PlaceStyle  placeStyle = store.get(rowIndex);
                int delColIndex = placeGrid.getColumnModel().indexOf(placeDeleteColumn);
                if (cellIndex == delColIndex) {
                    store.remove(rowIndex);
                    if(bundleStyle.getValue() != null && bundleStyle.getValue().equals(placeStyle)){
                        bundleStyle.clear();
                    }
                    
                    bundleStyle.getStore().remove(placeStyle);
                    presenter.deletePlaceStyle(placeStyle);
                }
            }
        });
        placeGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<PlaceStyle> store = placeGrid.getStore();
                PlaceStyle placeStyle = store.get(rowIndex);
                int editColIndex = placeGrid.getColumnModel().indexOf(placeEditColumn);
                if (cellIndex == editColIndex) {
                    presenter.editPlaceStyle(placeStyle);
                }
            }
        });
        
        associationGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<AssociationStyle> store = associationGrid.getStore();
                AssociationStyle  associationStyle = store.get(rowIndex);
                int delColIndex = associationGrid.getColumnModel().indexOf(associationDeleteColumn);
                if (cellIndex == delColIndex) {
                    store.remove(rowIndex);

                    presenter.deleteAssociationStyle(associationStyle);
                }
            }
        });
        associationGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<AssociationStyle> store = associationGrid.getStore();
                AssociationStyle associationStyle = store.get(rowIndex);
                int editColIndex = associationGrid.getColumnModel().indexOf(associationEditColumn);
                if (cellIndex == editColIndex) {
                    presenter.editAssociationStyle(associationStyle);
                }
            }
        });
        
        addButton.setIcon(IconType.PLUS);
        addButton.setType(ButtonType.LINK);
        addButton.setSize(ButtonSize.DEFAULT);
        addButton.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        addButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                presenter.editPlaceStyle(null);
            }
            
        });
        
        addAssociationButton.setIcon(IconType.PLUS);
        addAssociationButton.setType(ButtonType.LINK);
        addAssociationButton.setSize(ButtonSize.DEFAULT);
        addAssociationButton.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        addAssociationButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                presenter.editAssociationStyle(null);
            }
            
        });
        
        bundleStyle.addSelectionHandler(new SelectionHandler<PlaceStyle>(){

            @Override
            public void onSelection(SelectionEvent<PlaceStyle> event) {
                presenter.setBundleStyle(event.getSelectedItem());
        }});
        
        shapeComboBox.addSelectionHandler(new SelectionHandler<ShapeType>(){

            @Override
            public void onSelection(SelectionEvent<ShapeType> event) {
                presenter.setDefaultShape(event.getSelectedItem());
        }});
        
        shapeComboBox.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if(KeyCodes.KEY_ENTER == event.getNativeKeyCode() 
                        || KeyCodes.KEY_BACKSPACE == event.getNativeKeyCode() 
                        || KeyCodes.KEY_DELETE == event.getNativeKeyCode()){
                    
                    if(shapeComboBox.getText().isEmpty()){
                        presenter.setDefaultShape(null);
                    }
                    
                }
            }
        });

        dialog.setWidth("550px");
        dialog.setHeight("575px");

        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {
                dialog.setBodyHeight("470px");
                dialog.setBodyWidth("500px");
            }});
        
    }
    
    public void display(MapTheme mapTheme) {
        
        name.setText(mapTheme.getName());
        
        
        if(mapTheme.getPlaceStyles() != null){
            placeGrid.getStore().replaceAll(mapTheme.getPlaceStyles());
            bundleStyle.getStore().replaceAll(mapTheme.getPlaceStyles());
            bundleStyle.setValue(mapTheme.getBundleStyle(), false);
        }
        
        if(mapTheme.getAssociationStyles() != null)
            associationGrid.getStore().replaceAll(mapTheme.getAssociationStyles());

        placeGrid.getView().refresh(false);
        associationGrid.getView().refresh(false);
        
        if (mapTheme.getDefaultShape() != null) {
        	shapeComboBox.select(mapTheme.getDefaultShape());
        	shapeComboBox.setText(mapTheme.getDefaultShape().toString());
        }else{
            shapeComboBox.clear();
        }
    }
    
   

    interface PlaceStyleProperties extends PropertyAccess<PlaceStyle> {
        ValueProvider<PlaceStyle, String> name();
        
        ModelKeyProvider<PlaceStyle> uuid();

    }
    
    interface AssociationStyleProperties extends PropertyAccess<AssociationStyle> {

        ValueProvider<AssociationStyle, String> name();
                
        ModelKeyProvider<AssociationStyle> uuid();

    }

    public void show() {
        dialog.show();
    }

    
    
    
    
}
