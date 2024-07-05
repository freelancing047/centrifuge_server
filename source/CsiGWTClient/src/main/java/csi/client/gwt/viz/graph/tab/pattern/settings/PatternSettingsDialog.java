package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsActivity;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.viz.graph.tab.pattern.settings.TypeGrid.TypeGridEventHandler;
import csi.client.gwt.viz.graph.tab.pattern.settings.criterion.CriterionPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.drawing.DrawImage;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.graphics.shapes.ShapeType;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class PatternSettingsDialog implements PatternSettingsView {
    private static PatternSettingsDialogUiBinder ourUiBinder = (PatternSettingsDialogUiBinder) GWT.create(PatternSettingsDialogUiBinder.class);
    @UiField
    Dialog dialog;
    @UiField
    GridContainer patternGridContainer;
    @UiField
    DrawingPanel drawingPanel;
    @UiField
    AbsolutePanel drawingPanelContainer;
    @UiField
    FluidContainer typeContainer;
    @UiField
    FluidContainer leftContainer;
    @UiField
    FluidRow level1row;
    @UiField
    FluidRow level2row;
    @UiField
    FluidRow level3row;
    @UiField
    SimplePanel level2SimplePanel;
    @UiField
    SimplePanel level3SimplePanel;
    @UiField
    Heading dialogHeading;
    @UiField
    com.github.gwtbootstrap.client.ui.Button addPattern;
    @UiField
    com.github.gwtbootstrap.client.ui.Button deletePattern;
    @UiField
    FluidContainer mainContainer;
    @UiField
    TextBox patternNameTextBox;
    @UiField
    com.github.gwtbootstrap.client.ui.Button copyPattern;
    @UiField
    CheckBox requireDistinctNodesCheckBox;
    @UiField
    CheckBox requireDistinctLinksCheckBox;
    private PatternSettings patternSettings;
    private PatternSettingsActivity activity;
    private Grid<GraphPattern> patternGrid;
    private Layer background_Layer;
    private PatternLinkLayer linkLayer;
    private PatternNodeLayer nodeLayer;
    private PatternDragNodeLayer dragNodeLayer;
    private TypeGrid typeGrid;
    private DrawPatternNode nodeUserDrags;
    private DrawImage copyOfNodeUserDragsOnDragLayer;
    private List<DrawPatternNode> drawNodes = Lists.newArrayList();

    public PatternSettingsDialog(PatternSettings patternSettings) {
        this.patternSettings = patternSettings;
        ourUiBinder.createAndBindUi(this);
        initDialog();
        initPatternGrid();
        initTypeGrid();
        initDrawingPanel();
        level2SimplePanel.getElement().getStyle().setBackgroundColor("#FBFBFB");//NON-NLS
        leftContainer.getElement().getStyle().setPadding(0.0D, Unit.PX);
        leftContainer.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        hideCriteria();
        initPatternNameBox();

        //TODO:move to method
        patternNameTextBox.getElement().getStyle().setMarginLeft(20, Unit.PX);
        copyPattern.getElement().getStyle().setFloat(Style.Float.RIGHT);
        copyPattern.setType(ButtonType.LINK);
        copyPattern.setIconSize(IconSize.TWO_TIMES);
        deletePattern.setIconSize(IconSize.TWO_TIMES);
    }

    public void onPatternNameInput() {
        activity.setPatternName(patternNameTextBox.getValue());
    }

    @UiHandler("copyPattern")
    void onCopy(ClickEvent event) {
        activity.copyPattern();
    }
    private void initPatternNameBox() {
        patternNameTextBox.addBitlessDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent inputEvent) {
                onPatternNameInput();
            }
        }, InputEvent.getType());
    }

    private void initTypeGrid() {
        typeGrid = TypeGrid.build();
        typeGrid.addHandler(new TypeGridEventHandler() {
            @Override
            void onAdd(GraphType type) {
                activity.addToPattern(type);
            }
        });
        {
            typeContainer.getElement().getStyle().setPadding(0, Unit.PX);
        }
        typeContainer.add(typeGrid);
    }

    private void initDrawingPanel() {
        drawingPanelContainer.getElement().getStyle().setBorderColor("black");//NON-NLS
        drawingPanelContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        initLayers();
        drawingPanel.addLayer(background_Layer);
        drawingPanel.addLayer(linkLayer);
        drawingPanel.addLayer(nodeLayer);
        drawingPanel.addLayer(dragNodeLayer);
        drawingPanel.render();
    }

    private void initLayers() {
        background_Layer = initBackgroundLayer();
        linkLayer = new PatternLinkLayer(this);
        nodeLayer = initPatternNodeLayer();
        dragNodeLayer = new PatternDragNodeLayer(this);
    }

    private PatternNodeLayer initPatternNodeLayer() {
        PatternNodeLayer layer = new PatternNodeLayer(this);
        return layer;
    }

    private Layer initBackgroundLayer() {
        Layer layer = new Layer();
        Rectangle rectangle = new Rectangle(0.0D, 0.0D, 10000.0D, 10000.0D);
        rectangle.setFillStyle(CssColor.make("#F8F8F8"));//NON-NLS
        rectangle.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                patternSettings.hideCriteria();
            }
        });
        layer.addItem(rectangle);
        return layer;
    }

    private void initPatternGrid() {
        patternGrid = GraphPatternGrid.build();
        patternGridContainer.setGrid(patternGrid);
        patternGrid.getSelectionModel().addSelectionHandler(new SelectionHandler<GraphPattern>() {
            @Override
            public void onSelection(SelectionEvent<GraphPattern> event) {
                activity.editPattern(event.getSelectedItem());
            }
        });
    }

    private void initDialog() {
        initActionButton();
        initCancelButton();
        initHeading();
        mainContainer.getElement().getStyle().setOverflow(Overflow.HIDDEN);
    }

    private void initHeading() {
        Style style = dialogHeading.getElement().getStyle();
        style.setDisplay(Style.Display.INLINE);
        addPattern.setType(ButtonType.LINK);
        addPattern.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                activity.createNewPattern();
            }
        });
        deletePattern.setIcon(IconType.TRASH);
        deletePattern.setType(ButtonType.LINK);
        deletePattern.getElement().getStyle().setFloat(Style.Float.RIGHT);
    }

    @UiHandler("deletePattern")
    public void onDeletePattern(ClickEvent event) {
        activity.deletePattern();
    }

    private void initActionButton() {
        Button actionButton = dialog.getActionButton();
        actionButton.setText((CentrifugeConstantsLocator.get()).search());
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                activity.search();
            }
        });
    }

    private void initCancelButton() {
        Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(CentrifugeConstantsLocator.get().close());
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                activity.close();
            }
        });
    }

    @Override
    public void bind(PatternSettingsActivity activity) {
        this.activity = activity;
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void hide() {
        dialog.hide();
    }

    @Override
    public void addPattern(GraphPattern graphPattern) {
        patternGrid.getStore().add(graphPattern);
    }

    @Override
    public void addType(GraphType type) {
        typeGrid.add(type);
    }

    @Override
    public void startDragNode(DrawPatternNode node) {
        Preconditions.checkNotNull(node);
        patternSettings.hideCriteria();
        if(activity.allowLinkDrawing()) {
            dragNodeLayer.setVisible(true);
            nodeUserDrags = node;
            copyOfNodeUserDragsOnDragLayer = node.copyAsDrawImage();
            copyOfNodeUserDragsOnDragLayer.makeDraggable();
            dragNodeLayer.clear();
            dragNodeLayer.addItem(copyOfNodeUserDragsOnDragLayer);
            copyOfNodeUserDragsOnDragLayer.dragStart((int) (copyOfNodeUserDragsOnDragLayer.getDrawX() + copyOfNodeUserDragsOnDragLayer.getImageWidth() / 2.0D), (int) (copyOfNodeUserDragsOnDragLayer.getDrawY() + copyOfNodeUserDragsOnDragLayer.getImageWidth() / 2.0D));
            drawingPanel.bringToFront(dragNodeLayer);
        }
    }

    @Override
    public void hideDetails() {
        activity.hideNodeDetails();
        //FIXME: this could be more efficient
        for (DrawPatternNode drawNode : drawNodes) {
            drawNode.setOver(false);
        }
    }

    @Override
    public void showDetails(DrawPatternNode node) {
        activity.showNodeDetails(node);
        node.setOver(true);
    }

    @Override
    public void addNodeToPattern(final PatternNode node) {
        GraphType type = null;
        for (GraphType graphType : typeGrid.getTypeMap().values()) {
            //FIXME:need better logic here
            if (node.appliesToType(graphType.getName())) {
                type = graphType;
            }
        }
        Image image2 = null;
        if (type == null) {
            image2 = new Image();
            GraphImpl.getRenderedIcon(null, ShapeType.CIRCLE, 0, 40, 1.08, image2);
        } else if (type instanceof GraphNodeType) {

            GraphNodeType nodeType = (GraphNodeType) type;
            image2 = nodeType.getImage(40);
        }
        final DrawPatternNode drawNode = new DrawPatternNode(node);
        if (image2 != null) {
            final Image image = image2;
            image.getElement().getStyle().setDisplay(Style.Display.INLINE);
            image.getElement().getStyle().setTop(-10000, Unit.PX);
            image.getElement().getStyle().setLeft(-10000, Unit.PX);
            drawNodes.add(drawNode);
            image2.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    drawNode.fromImage(ImageElement.as(image.getElement()));
                    nodeLayer.addNode(drawNode);
                    image.removeFromParent();
                    drawingPanel.render();
                }
            });
            RootPanel.get().add(image);
        }
    }

    @Override
    public void showCriteria(CriteriaPanel criteriaPanel) {
        level2SimplePanel.setWidget(criteriaPanel);
        showCriteria();
    }

    @Override
    public void hideCriteria() {
        for (DrawPatternNode drawNode : drawNodes) {
            drawNode.setEdit(false);
        }
        for (Renderable renderable : linkLayer.getRenderables()) {
            if (renderable instanceof DrawPatternLink) {
                DrawPatternLink drawPatternLink = (DrawPatternLink) renderable;
                drawPatternLink.setEdit(false);
            }
        }
        level1row.setVisible(true);
        level2row.setVisible(false);
        level3row.setVisible(false);
        drawingPanel.render();
    }

    @Override
    public void pinDetails(PatternNode node) {
        activity.pinDetails(node);
    }

    @Override
    public void showCriterion(CriterionPanel criterionPanel) {
        level3SimplePanel.setWidget(criterionPanel);
        level1row.setVisible(false);
        level2row.setVisible(false);
        level3row.setVisible(true);
    }

    @Override
    public void showCriteria() {
        level1row.setVisible(false);
        level2row.setVisible(true);
        level3row.setVisible(false);
    }

    @Override
    public void endNodeDrag(MouseUpEvent event) {
        nodeLayer.setVisible(true);
        linkLayer.setVisible(true);
        dragNodeLayer.setVisible(false);
        Optional hitTest = nodeLayer.hitTest((double) event.getX(), (double) event.getY());
        if (!hitTest.isPresent()) {
            nodeUserDrags.setDrawX(copyOfNodeUserDragsOnDragLayer.getDrawX());
            nodeUserDrags.setDrawY(copyOfNodeUserDragsOnDragLayer.getDrawY());
        } else {
            Renderable renderable = (Renderable) hitTest.get();
            if (renderable instanceof DrawPatternNode) {
                DrawPatternNode drawNode = (DrawPatternNode) renderable;
                if (drawNode.getNode() == nodeUserDrags.getNode()) {
                    activity.pinDetails(nodeUserDrags.getNode());
                } else {
                    activity.addLink(nodeUserDrags.getNode(), drawNode.getNode());
                }
            }
        }

    }

    @Override
    public void dragMove(MouseMoveEvent event) {
    }

    @Override
    public void addLinkToPattern(PatternLink patternLink) {
        PatternNode node1 = patternLink.getNode1();
        PatternNode node2 = patternLink.getNode2();
        DrawPatternNode dpn1 = null;
        DrawPatternNode dpn2 = null;
        Iterator i$ = drawNodes.iterator();

        while (i$.hasNext()) {
            DrawPatternNode drawNode = (DrawPatternNode) i$.next();
            if (drawNode.getNode() == node1) {
                dpn1 = drawNode;
            } else if (drawNode.getNode() == node2) {
                dpn2 = drawNode;
            }
        }

        DrawPatternLink link = new DrawPatternLink(dpn1, dpn2);
        link.setLink(patternLink);
        linkLayer.addItem(link);
    }

    @Override
    public void clearPattern() {
        nodeLayer.removeAll();
        linkLayer.removeAll();
        drawNodes.clear();
        drawingPanel.render();
    }

    @Override
    public void setPatternName(String name) {
        patternNameTextBox.setValue(name);
    }

    @Override
    public void removePattern(GraphPattern pattern) {
        patternGrid.getStore().remove(pattern);
    }

    @Override
    public void setPatterns(List<GraphPattern> patterns) {
        ListStore<GraphPattern> store = patternGrid.getStore();
        store.clear();
        store.addAll(patterns);
    }

    @Override
    public void selectPattern(GraphPattern editPattern) {
        patternGrid.getSelectionModel().select(editPattern, false);
    }

    @Override
    public void setEditing(HasPatternCriteria item) {
        for (DrawPatternNode drawNode : drawNodes) {
            if (drawNode.getNode() == item) {
                drawNode.setEdit(true);
                return;
            }
        }
        for (Renderable renderable : linkLayer.getRenderables()) {
            if (renderable instanceof DrawPatternLink) {
                DrawPatternLink drawPatternLink = (DrawPatternLink) renderable;
                if (drawPatternLink.getLink() == item) {
                    drawPatternLink.setEdit(true);
                    drawingPanel.render();
                    return;
                }
            }
        }
    }

    @Override
    public void editLink(PatternLink link) {
        activity.editLink(link);
    }

    @Override
    public void updatePattern(GraphPattern pattern) {
        if (pattern != null) {
            patternGrid.getStore().update(pattern);
        }
    }

    @Override
    public void setRequireDistinctNodes(boolean requireDistinctNodes) {
        requireDistinctNodesCheckBox.setValue(requireDistinctNodes);
    }

    @Override
    public void setRequireDistinctLinks(boolean requireDistinctLinks) {
        requireDistinctLinksCheckBox.setValue(requireDistinctLinks);
    }

    @UiHandler("requireDistinctNodesCheckBox")
    void onRequireDistinctNodesCheckBoxClick(ClickEvent event) {
        activity.setRequireDistinctNodes(requireDistinctNodesCheckBox.getValue());
    }

    @UiHandler("requireDistinctLinksCheckBox")
    void onRequireDistinctLinksCheckBoxClick(ClickEvent event) {
        activity.setRequireDistinctLinks(requireDistinctLinksCheckBox.getValue());
    }

    interface TypeModelProperties extends PropertyAccess<GraphType> {
        ValueProvider<GraphType, String> name();

        ModelKeyProvider<GraphType> uuid();

        ValueProvider<GraphType, Void> voidFn();
    }

    interface PatternModelProperties extends PropertyAccess<GraphPattern> {
        ValueProvider<GraphPattern, String> name();

        ModelKeyProvider<GraphPattern> uuid();
    }

    interface PatternSettingsDialogUiBinder extends UiBinder<Widget, PatternSettingsDialog> {
    }

    private static class GraphPatternGrid extends ResizeableGrid<GraphPattern> {
        private static PatternModelProperties patternModelProps = (PatternModelProperties) GWT.create(PatternModelProperties.class);

        public GraphPatternGrid(ListStore<GraphPattern> patternSettingsModelStore, ColumnModel<GraphPattern> columnModel) {
            super(patternSettingsModelStore, columnModel);
        }

        public static GraphPatternGrid build() {
            ColumnModel columnModel = initColumns();
            ListStore patternSettingsModelStore = new ListStore(patternModelProps.uuid());
            GraphPatternGrid grid = new GraphPatternGrid(patternSettingsModelStore, columnModel);
            styleGrid(grid);
            return grid;
        }

        public static void styleGrid(GraphPatternGrid grid) {
            GridView view = grid.getView();
            view.setShowDirtyCells(false);
            view.setSortingEnabled(false);
            view.setAdjustForHScroll(true);
            view.setTrackMouseOver(false);
            grid.setColumnReordering(false);
            grid.setColumnResize(false);
            grid.setAllowTextSelection(false);
            grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

        private static ColumnModel<GraphPattern> initColumns() {
            ArrayList columnConfigs = Lists.newArrayList();
            ColumnConfig nameColumn = initNameColumn();
            columnConfigs.add(nameColumn);
            return new ColumnModel(columnConfigs);
        }

        private static ColumnConfig<GraphPattern, String> initNameColumn() {
            ColumnConfig nameColumn = new ColumnConfig(patternModelProps.name());
            nameColumn.setHeader(CentrifugeConstantsLocator.get().name());
            nameColumn.setWidth(130);
            return nameColumn;
        }
    }
}
