package csi.client.gwt.theme.editor.graph;

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
import csi.client.gwt.widget.combo_boxes.NodeStyleComboBox;
import csi.client.gwt.widget.combo_boxes.ShapeComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.misc.EmptyValueProvider;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;

public class GraphThemeEditorView{


    private static GraphThemeEditorViewUiBinder uiBinder = GWT.create(GraphThemeEditorViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    NodeStyleProperties nodeProps = GWT.create(NodeStyleProperties.class);
    LinkStyleProperties linkProps = GWT.create(LinkStyleProperties.class);
    
    @UiField
    Dialog dialog;
    
    @UiField
    TextBox name;

    @UiField
    Button addButton;
    @UiField
    Button addLinkButton;
    
    @UiField(provided=true)
    NodeStyleComboBox bundleStyle;
    
    @UiField
    ShapeComboBox shapeComboBox;
    
    @UiField
    GridContainer nodeGridContainer;
    @UiField
    GridContainer linkGridContainer;
        
    ResizeableGrid<NodeStyle> nodeGrid;
    ResizeableGrid<LinkStyle> linkGrid;

    @UiField
    FilterTextbox nodeFilterTextbox;

    @UiField
    FilterTextbox linkFilterTextbox;

    interface GraphThemeEditorViewUiBinder extends UiBinder<Widget, GraphThemeEditorView> {
    }
    
    public GraphThemeEditorView(final GraphThemeEditorPresenter presenter){

        bundleStyle = new NodeStyleComboBox();
        
        uiBinder.createAndBindUi(this);
        
        dialog.setTitle(i18n.themeEditor_graph_title());
        
        
        csi.client.gwt.widget.buttons.Button actionButton = dialog.getActionButton();
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                nodeFilterTextbox.clearFilter();
                linkFilterTextbox.clearFilter();
                presenter.saveModel(nodeGrid.getStore().getAll(), linkGrid.getStore().getAll(), name.getText());
                dialog.hide();
            }
        });
        
        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.cancel());
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.discardChanges();
                dialog.hide();
//
            }

        });

        List<ColumnConfig<NodeStyle, ?>> nodeColumnConfigs = Lists.newArrayList();
        ColumnConfig<NodeStyle, String> nodeNameColumn = new ColumnConfig<NodeStyle, String>(nodeProps.name());
        nodeNameColumn.setHeader(i18n.name()); //$NON-NLS-1$
        nodeNameColumn.setWidth(170);
        nodeColumnConfigs.add(nodeNameColumn);
        
        final ColumnConfig<NodeStyle, Void> nodeDeleteColumn = new ColumnConfig<NodeStyle, Void>(new EmptyValueProvider<NodeStyle>());
        IconCell deleteCell = new IconCell(IconType.REMOVE);
        deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        nodeDeleteColumn.setCell(deleteCell);
        nodeDeleteColumn.setWidth(24);
            
        final ColumnConfig<NodeStyle, Void> nodeEditColumn = new ColumnConfig<NodeStyle, Void>(new EmptyValueProvider<NodeStyle>());
        IconCell editCell = new IconCell(IconType.PENCIL);
        editCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
        nodeEditColumn.setCell(editCell);
        nodeEditColumn.setWidth(24);


        nodeColumnConfigs.add(nodeEditColumn);
        nodeColumnConfigs.add(nodeDeleteColumn);
        
        ColumnModel<NodeStyle> nodeColumnModel = new ColumnModel<NodeStyle>(nodeColumnConfigs);
        nodeGrid = new ResizeableGrid<NodeStyle>(new ListStore<NodeStyle>(nodeProps.uuid()), nodeColumnModel);
        
        
        nodeGridContainer.setGrid(nodeGrid);
        nodeGridContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

        List<ColumnConfig<LinkStyle, ?>> linkColumnConfigs = Lists.newArrayList();        
        ColumnConfig<LinkStyle, String> linkNameColumn = new ColumnConfig<LinkStyle, String>(linkProps.name());
        linkNameColumn.setHeader(i18n.name()); //$NON-NLS-1$
        linkNameColumn.setWidth(170);
        linkColumnConfigs.add(linkNameColumn);
        
        final ColumnConfig<LinkStyle, Void> linkDeleteColumn = new ColumnConfig<LinkStyle, Void>(new EmptyValueProvider<LinkStyle>());
        IconCell linkDeleteCell = new IconCell(IconType.REMOVE);
        linkDeleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        linkDeleteColumn.setCell(linkDeleteCell);
        linkDeleteColumn.setWidth(24);
            
        final ColumnConfig<LinkStyle, Void> linkEditColumn = new ColumnConfig<LinkStyle, Void>(new EmptyValueProvider<LinkStyle>());
        IconCell linkEditCell = new IconCell(IconType.PENCIL);
        linkEditCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
        linkEditColumn.setCell(linkEditCell);
        linkEditColumn.setWidth(24);
        linkColumnConfigs.add(linkEditColumn);
        linkColumnConfigs.add(linkDeleteColumn);
        
        ColumnModel<LinkStyle> linkColumnModel = new ColumnModel<LinkStyle>(linkColumnConfigs);
        linkGrid = new ResizeableGrid<LinkStyle>(new ListStore<LinkStyle>(linkProps.uuid()), linkColumnModel);
        
        linkGridContainer.setGrid(linkGrid);

        linkGridContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

        nodeFilterTextbox.setAttachedStore(nodeGrid.getStore());
        linkFilterTextbox.setAttachedStore(linkGrid.getStore());

        nodeGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<NodeStyle> store = nodeGrid.getStore();
                NodeStyle  nodeStyle = store.get(rowIndex);
                int delColIndex = nodeGrid.getColumnModel().indexOf(nodeDeleteColumn);
                if (cellIndex == delColIndex) {
                    store.remove(rowIndex);
                    //presenter.deleteEvent(eventDefinition);

                    if(bundleStyle.getValue() != null && bundleStyle.getValue().equals(nodeStyle)){
                        bundleStyle.clear();
                    }
                    
                    bundleStyle.getStore().remove(nodeStyle);
                    presenter.deleteNodeStyle(nodeStyle);
                }
            }
        });
        nodeGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<NodeStyle> store = nodeGrid.getStore();
                NodeStyle nodeStyle = store.get(rowIndex);
                int editColIndex = nodeGrid.getColumnModel().indexOf(nodeEditColumn);
                if (cellIndex == editColIndex) {
                    presenter.editNodeStyle(nodeStyle);
                }
            }
        });
        
        linkGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<LinkStyle> store = linkGrid.getStore();
                LinkStyle  linkStyle = store.get(rowIndex);
                int delColIndex = linkGrid.getColumnModel().indexOf(linkDeleteColumn);
                if (cellIndex == delColIndex) {
                    store.remove(rowIndex);

                    presenter.deleteLinkStyle(linkStyle);
                }
            }
        });
        linkGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<LinkStyle> store = linkGrid.getStore();
                LinkStyle linkStyle = store.get(rowIndex);
                int editColIndex = linkGrid.getColumnModel().indexOf(linkEditColumn);
                if (cellIndex == editColIndex) {
                    presenter.editLinkStyle(linkStyle);
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
                
                presenter.editNodeStyle(null);
            }
            
        });
        
        addLinkButton.setIcon(IconType.PLUS);
        addLinkButton.setType(ButtonType.LINK);
        addLinkButton.setSize(ButtonSize.DEFAULT);
        addLinkButton.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        addLinkButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                presenter.editLinkStyle(null);
            }
            
        });
        
        bundleStyle.addSelectionHandler(new SelectionHandler<NodeStyle>(){

            @Override
            public void onSelection(SelectionEvent<NodeStyle> event) {
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
        
        name.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.setName(name.getText());
            }});

        dialog.setWidth("550px");
        dialog.setHeight("600px");
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {
                dialog.setBodyHeight("495px");
                dialog.setBodyWidth("500px");
            }});
    }


    public void display(GraphTheme graphTheme) {
        
        name.setText(graphTheme.getName());
        
        
        if(graphTheme.getNodeStyles() != null){
            nodeGrid.getStore().replaceAll(graphTheme.getNodeStyles());
            bundleStyle.getStore().replaceAll(graphTheme.getNodeStyles());
            bundleStyle.setValue(graphTheme.getBundleStyle(), false);
        }
        
        if(graphTheme.getDefaultShape() != null){
            shapeComboBox.setValue(graphTheme.getDefaultShape(), false);
        } else {
            shapeComboBox.clear();
        }
        
        if(graphTheme.getLinkStyles() != null)
            linkGrid.getStore().replaceAll(graphTheme.getLinkStyles());

        nodeGrid.getView().refresh(false);
        linkGrid.getView().refresh(false);
                   
    }
    
   

    interface NodeStyleProperties extends PropertyAccess<NodeStyle> {
        ValueProvider<NodeStyle, String> name();
        
        ModelKeyProvider<NodeStyle> uuid();

    }
    
    interface LinkStyleProperties extends PropertyAccess<LinkStyle> {

        ValueProvider<LinkStyle, String> name();
                
        ModelKeyProvider<LinkStyle> uuid();

    }

    public void show() {
        dialog.show();
    }

    
    
    
    
}
