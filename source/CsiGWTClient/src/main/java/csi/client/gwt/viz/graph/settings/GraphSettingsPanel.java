package csi.client.gwt.viz.graph.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.FormType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.ColorPalette;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.NodeProxyFactory;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputEvent;
import csi.client.gwt.viz.shared.filter.CreateEditFilterDialog;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.ResourceBasicsComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.drawing.*;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.visualization.graph.HierarchicalLayoutOrientation;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;
import csi.shared.core.visualization.graph.GraphLayout;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class GraphSettingsPanel extends Composite implements GraphSettings.View {

    private static final String[] background = {"FEFEFE", "EEEEEE", "DDDDDD", "CCCCCC", "BBBBBB", "AAAAAA", "999999",//NON-NLS
            "888888", "777777", "666666", "555555", "444444", "333333", "222222", "111111", "000000", "FFDDDD",//NON-NLS
            "FFBBBB", "FFFFDD", "FFFFBB", "DDFFDD", "BBFFBB", "DDFFFF", "BBFFFF", "DDDDFF", "BBFFFF", "FFDDFF",//NON-NLS
            "FFBBFF", "CC0000", "FAFAFA", "00BBDD", "EAEAEA"};//NON-NLS
    private static final String ITERATIONS = "iterations";//NON-NLS
    //    private static String[] layouts = { MenuKeyConstant.CENTRIFUGE.toString(),
    //                                       MenuKeyConstant.CIRCULAR.toString(),
    //                                       MenuKeyConstant.FORCE_DIRECTED.toString(),
    //                                       MenuKeyConstant.LINEAR_HIERARCHY.toString(),
    //                                       MenuKeyConstant.RADIAL.toString(),
    //                                       MenuKeyConstant.SCRAMBLE_AND_PLACE.toString(),
    //                                       MenuKeyConstant.GRID.toString(),
    //                                       MenuKeyConstant.APPLY_FORCE.toString() };
    private static final String MORE_ITERATIONS = "moreIterations";//NON-NLS
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField(provided = true)
    public DataViewDef dataViewDef;
    // NOTE: I'm not using this object, but it is required for the uibinder.
    @UiField(provided = true)
    public RelGraphViewDef visualizationDef = new RelGraphViewDef();
    //    @UiField
    //    FluidContainer detailsContainer;
    @UiField
    DrawingPanel drawingPanel;
    //    @UiField
    //    HTMLPanel mainLeft;
    @UiField
    Column mainRight;
    @UiField
    VisualizationSettingsModal modal;
    @UiField
    AbsolutePanel panel;
    //    @UiField
    //    NavSearch searchField;
    @UiField
    TextBox visualizationTitle;
    @UiField
    StringComboBox filterListBox;
    @UiField
    GraphLayoutComboBox layoutListBox;
    @UiField
    ResourceBasicsComboBox themeListBox;
    @UiField
    CentrifugeConstantsLocator i18n;
    @UiField
    FieldDefComboBox fieldDefComboBox;
    @UiField
    Button addButton;
    private String defaultTheme = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultTheme();
    private DrawNode previousOverNode;
    private Layer background_Layer;
    private Rectangle backgroundFill;
    private DrawImage copyOfNodeUserDragsOnDragLayer;
    private Layer dragNodeLayer;
    //private FieldDefGrid fieldDefGrid;
    private GraphSettings graphSettings;
    private LinkLayer linkLayer;
    private NodeLayer nodeLayer;
    private DrawNode nodeUserDrags;
    private TextBox iterationsTextBox;
    private GraphLayoutComboBox defaultLayoutListBox;
    private TextBox moreIterationsTextBox;
    private int iterationsFutureValue;
    private int moreIterationsFutureValue;
    private String defaultLayoutFutureValue;
    private HierarchicalLayoutOrientation hierarchicalLayoutOrientation;
    private HTMLPanel backgroundColorButton;
    private ColorPalette colorPalette;
    private BiMap<Integer, Filter> positionToFilterMap = HashBiMap.create();
    private FluidContainer detailsInfo;

    public GraphSettingsPanel(GraphSettings graphSettings) {
        this.graphSettings = graphSettings;
        dataViewDef = this.graphSettings.getDataViewDef();
        visualizationDef = this.graphSettings.getVisualizationDef();
        modal = uiBinder.createAndBindUi(this);

        visualizationTitle.setWidth("150px");
        filterListBox.setWidth("140px");
        layoutListBox.setWidth("140px");
        themeListBox.setWidth("140px");

        // modal.add(footer.asModalFooter());
        createAdvancedControls();
        background_Layer = new Layer();
        linkLayer = new LinkLayer(this);
        nodeLayer = new NodeLayer(this);
        dragNodeLayer = new DragNodeLayer(this);

        drawingPanel.addLayer(background_Layer);
        drawingPanel.addLayer(linkLayer);
        drawingPanel.addLayer(nodeLayer);
        drawingPanel.addLayer(dragNodeLayer);

        dragNodeLayer.setVisible(false);
        fieldDefComboBox.setAllowMultiselect(true);
        fieldDefComboBox.getStore().addAll(graphSettings.getDataViewDef().getModelDef().getFieldDefs());

        fieldDefComboBox.setValue(fieldDefComboBox.getStore().get(0));
        fieldDefComboBox.getListView().getElement().getStyle().setOpacity(0.85);

        //        fieldDefGrid = new FieldDefGrid(graphSettings.getDataViewDef().getModelDef());
        //        fieldDefGrid.addStyleName("graph-setting-field-grid");//NON-NLS
        //        fieldDefGrid.setColumnFieldUpdater(new FieldDefGridSpy(this));
        //        fieldDefGrid.filter("");


        panel.add(drawingPanel, 0, 0);

        addThemeChangeHandler();
        //addSearchHandler();

        // hideDetails();
        drawingPanel.bringToFront(nodeLayer);
        redraw();
        createBackgroundLayer();

        initFilters();
        setFilter(graphSettings.visualizationDef.getFilter());

        initLayouts();
        setLayout(graphSettings.visualizationDef.getLayout());

        initAddButton();
    }

    private void initAddButton() {
        addButton.setIcon(IconType.CIRCLE_ARROW_DOWN);
        addButton.setType(ButtonType.LINK);
        addButton.setSize(ButtonSize.LARGE);
        final Style buttonStyle = addButton.getElement().getStyle();
        buttonStyle.setFontSize(25.0D, Unit.PX);
        buttonStyle.setTextDecoration(TextDecoration.NONE);
        buttonStyle.setPaddingLeft(0, Unit.PX);
        buttonStyle.setPaddingTop(0, Unit.PX);
        buttonStyle.setMarginBottom(0, Unit.PX);
        buttonStyle.setMarginTop(2, Unit.PX);
        buttonStyle.setPosition(Position.ABSOLUTE);
        buttonStyle.setLeft(254, Unit.PX);

        addButton.addClickHandler(event -> {
            if (fieldDefComboBox.getValue() != null) {
                graphSettings.addNode(fieldDefComboBox.getValue());
                fieldDefComboBox.incrementSelected();
                fieldDefComboBox.kill();
            }
        });

        fieldDefComboBox.addSelectionHandler(event -> {
            final FieldDef fieldDef = event.getSelectedItem();
            if (fieldDef != null) {
                graphSettings.addNode(fieldDef);
            }
            fieldDefComboBox.incrementSelected();

        });
    }

    private void createAdvancedControls() {
        Button advancedButton = new Button(CentrifugeConstantsLocator.get().graphSettingsView_advancedButton());
        modal.addLeftControl(advancedButton);
        final Dialog dialog = new Dialog();
        {
            dialog.hideOnAction();
            dialog.hideOnCancel();
            Form form = new Form();
            dialog.add(form);
            form.setType(FormType.HORIZONTAL);

            Fieldset fieldset = new Fieldset();
            form.add(fieldset);
            {
                ControlGroup controlGroup = new ControlGroup();
                fieldset.add(controlGroup);
                ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().numberOfLayoutIterations());
                controlGroup.add(controlLabel);
                iterationsTextBox = new TextBox();
                try {
                    iterationsFutureValue = Integer.parseInt(graphSettings.getModel().getPropertyValue(ITERATIONS));
                } catch (NumberFormatException e) {
                    //FIXME: make default customizable
                    iterationsFutureValue = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultLayoutIterations();
                }
                iterationsTextBox.setValue(Integer.toString(iterationsFutureValue, 10));
                iterationsTextBox.addBitlessDomHandler(inputEvent -> {
                    try {
                        iterationsFutureValue = Integer.parseInt(iterationsTextBox.getValue());
                    } catch (NumberFormatException e) {
                        //                            iterationsTextBox.setValue(Integer.toString(iterationsFutureValue,10));
                    }
                }, InputEvent.getType());
                Controls controls = new Controls();
                controls.add(iterationsTextBox);
                controlGroup.add(controls);
            }
            {
                ControlGroup controlGroup = new ControlGroup();
                fieldset.add(controlGroup);
                ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().incrementalLayoutIterations());
                controlGroup.add(controlLabel);
                moreIterationsTextBox = new TextBox();
                //FIXME: park values somewhere reasonable
                moreIterationsTextBox.setValue(graphSettings.getModel().getPropertyValue(MORE_ITERATIONS));
                moreIterationsTextBox.addBitlessDomHandler(inputEvent -> {
                    try {
                        moreIterationsFutureValue = Integer.parseInt(moreIterationsTextBox.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                }, InputEvent.getType());
                Controls controls = new Controls();
                controls.add(moreIterationsTextBox);
                controlGroup.add(controls);
            }
            {
                ControlGroup controlGroup = new ControlGroup();
                //                fieldset.add(controlGroup);
                ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().defaultLayout());
                controlGroup.add(controlLabel);
                defaultLayoutListBox = new GraphLayoutComboBox();

                {
                    //Read value from model
                    String defaultLayout = graphSettings.getModel().getPropertyValue("defaultLayout");
                    if (!Strings.isNullOrEmpty(defaultLayout))
                        defaultLayoutListBox.setValue(GraphLayout.valueOf(defaultLayout));
                }
                {
                    //Add change handler to layoutListBox
                    defaultLayoutListBox.addBitlessDomHandler(inputEvent -> defaultLayoutFutureValue = defaultLayoutListBox.getValue().getName(), InputEvent.getType());
                }
                Controls controls = new Controls();
                controls.add(defaultLayoutListBox);
                controlGroup.add(controls);
            }
            {
                ControlGroup controlGroup = new ControlGroup();
                fieldset.add(controlGroup);
                ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().background());
                controlGroup.add(controlLabel);
                backgroundColorButton = new HTMLPanel("");
                {
                    Style style = backgroundColorButton.getElement().getStyle();
                    //            		style.setMarginLeft(5, Unit.PX);
                    style.setWidth(25, Unit.PX);
                    style.setHeight(25, Unit.PX);
                    style.setMarginBottom(-10, Unit.PX);
                    style.setBorderWidth(1, Unit.PX);
                    style.setBorderStyle(BorderStyle.SOLID);
                    style.setBorderColor("#CCC");
                    style.setProperty("borderRadius", "3px");
                    style.setDisplay(Display.INLINE_BLOCK);
                }
                colorPalette = new ColorPalette(background, background);
                colorPalette.setStyleName("graph-settings-color-pallette");
                colorPalette.setPosition(220, 100);
                colorPalette.setVisible(false);
                addColorChooserHandlers();
                Controls controls = new Controls();
                controls.add(backgroundColorButton);
                controls.add(colorPalette);
                controlGroup.add(controls);
            }
            {
                ControlGroup controlGroup = new ControlGroup();
                fieldset.add(controlGroup);
                ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().hierarchyLayoutLabel());
                controlGroup.add(controlLabel);
                RadioButton rbLeftToRight = new RadioButton("Orientation");
                rbLeftToRight.setText(CentrifugeConstantsLocator.get().hierarchyLayoutLeftToRight());
                rbLeftToRight.setValue(false);
                RadioButton rbTopToBottom = new RadioButton("Orientation");
                rbTopToBottom.setText(CentrifugeConstantsLocator.get().hierarchyLayoutTopToBottom());
                rbTopToBottom.setValue(false);
                RadioButton rbRightToLeft = new RadioButton("Orientation");
                rbRightToLeft.setText(CentrifugeConstantsLocator.get().hierarchyLayoutRightToLeft());
                rbRightToLeft.setValue(false);
                RadioButton rbBottomToTop = new RadioButton("Orientation");
                rbBottomToTop.setText(CentrifugeConstantsLocator.get().hierarchyLayoutBottomToTop());
                rbBottomToTop.setValue(false);
                String layoutOrientation = graphSettings.getModel().getPropertyValue("layoutOrientation");
                if (layoutOrientation == null) {
                    hierarchicalLayoutOrientation = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultHierarchicalLayoutOrientation();
                } else {
                    hierarchicalLayoutOrientation = HierarchicalLayoutOrientation.valueOf(layoutOrientation);
                }
                switch (hierarchicalLayoutOrientation) {
                    case LEFT_TO_RIGHT:
                        rbLeftToRight.setValue(true);
                        break;
                    case TOP_TO_BOTTOM:
                        rbTopToBottom.setValue(true);
                        break;
                    case RIGHT_TO_LEFT:
                        rbRightToLeft.setValue(true);
                        break;
                    case BOTTOM_TO_TOP:
                        rbBottomToTop.setValue(true);
                        break;
                }
                ClickHandler clickHandler = event -> {
                    if (rbLeftToRight.getValue()) {
                        hierarchicalLayoutOrientation = HierarchicalLayoutOrientation.LEFT_TO_RIGHT;
                    } else if (rbTopToBottom.getValue()) {
                        hierarchicalLayoutOrientation = HierarchicalLayoutOrientation.TOP_TO_BOTTOM;
                    } else if (rbRightToLeft.getValue()) {
                        hierarchicalLayoutOrientation = HierarchicalLayoutOrientation.RIGHT_TO_LEFT;
                    } else if (rbBottomToTop.getValue()) {
                        hierarchicalLayoutOrientation = HierarchicalLayoutOrientation.BOTTOM_TO_TOP;
                    }
                };
                rbLeftToRight.addClickHandler(clickHandler);
                rbTopToBottom.addClickHandler(clickHandler);
                rbRightToLeft.addClickHandler(clickHandler);
                rbBottomToTop.addClickHandler(clickHandler);
                Controls controls = new Controls();
                controls.add(rbLeftToRight);
                controls.add(rbTopToBottom);
                controls.add(rbRightToLeft);
                controls.add(rbBottomToTop);
                controlGroup.add(controls);
            }
            dialog.getActionButton().addClickHandler(event -> {
                graphSettings.getModel().setPropertyValue(ITERATIONS, Integer.toString(iterationsFutureValue));
                if (moreIterationsFutureValue > 0) {
                    graphSettings.getModel().setPropertyValue(MORE_ITERATIONS, Integer.toString(moreIterationsFutureValue));
                } else {
                    graphSettings.getModel().setPropertyValue(MORE_ITERATIONS, null);
                }
                graphSettings.getModel().setPropertyValue("defaultLayout", defaultLayoutFutureValue);
                graphSettings.getModel().setPropertyValue("layoutOrientation", hierarchicalLayoutOrientation.toString());
            });
        }
        advancedButton.addClickHandler(event -> dialog.show());

    }

    private void addColorChooserHandlers() {
        // DIV elements are not currently exposing an addClickHandler method. Would prefer to use @UiHandler
        backgroundColorButton.addDomHandler(new ColorSwatchClickHandler(this), ClickEvent.getType());
        colorPalette.addSelectionHandler(new ColorChooserClickHandler2());
    }

    void dragMove(MouseMoveEvent event) {
        Optional<Renderable> hitTest = nodeLayer.hitTest(event.getX(), event.getY());
        if (previousOverNode != null) {
            previousOverNode.setStroke(null);
        }
        if (hitTest.isPresent()) {
            Renderable renderable = hitTest.get();
            if (renderable instanceof DrawNode) {
                DrawNode drawNode = (DrawNode) renderable;
                if (drawNode.getNodeProxy() == nodeUserDrags.getNodeProxy()) {
                    drawNode.setStroke(CssColor.make("blue"));//NON-NLS
                } else {
                    drawNode.setStroke(CssColor.make("green"));//NON-NLS
                }
                previousOverNode = drawNode;

            }
        }

    }

    @Override
    public void addLink(final LinkProxy linkProxy) {
        checkNotNull(linkProxy);
        final NodeProxyFactory nodeProxyFactory = graphSettings.getNodeProxyFactory();

        if (graphSettings.getCurrentTheme() != null) {
            applyTheme(linkProxy, nodeProxyFactory, graphSettings.getCurrentTheme());
        } else {
            graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {
                @Override
                public void onSuccess(GraphTheme result) {
                    applyTheme(linkProxy, nodeProxyFactory, result);
                }
            });
        }

    }

    private String findThemeColor(LinkProxy linkProxy, GraphTheme result) {
        String currentType = GraphSettings.getStaticTextFromAttributeDef(linkProxy.getTypeAttributeDef());
        if (currentType == null) {
            currentType = CentrifugeConstantsLocator.get().graph_defaultLinkType();
        }
        if (result != null) {
            LinkStyle linkStyle = result.findLinkStyle(currentType);
            if (linkStyle != null && !linkProxy.isColorOverride()) {
                return linkStyle.getColor() + "";
            }
        }
        return null;
    }

    @Override
    public void addNode(final NodeProxy nodeProxy) { // WRITE_ME
        // add to node layer
        // add to shadow node layer
        // render node layer
        // render shadow node layer
        if (graphSettings.getCurrentTheme() != null) {
            createVisualNode(nodeProxy, graphSettings.getCurrentTheme());
        } else {


            graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

                @Override
                public void onSuccess(GraphTheme result) {

                    createVisualNode(nodeProxy, result);
                }


            });
        }


    }

    //    private void addSearchHandler() {
    //        searchField.getTextBox().addKeyUpHandler(new FieldDefSearchHandler(this)); // move to field
    //    }

    private Image maybeRenderNode(GraphTheme result, NodeProxy nodeProxy) {
        if ((nodeProxy.getShape() == ShapeType.NONE) && Strings.isNullOrEmpty(nodeProxy.getIconURI())) {
            Image image = new Image();
            image.setUrl("img/LegendItem_TEXT.png");
            return image;
        }
        return nodeProxy.getRenderedIcon(result);
    }

    private void createVisualNode(final NodeProxy nodeProxy, GraphTheme result) {
        final Image image = maybeRenderNode(result, nodeProxy);

        image.addLoadHandler(event -> {
            DrawNode drawNode = new DrawNode(nodeProxy);
            nodeLayer.addNode(drawNode);
            drawNode.fromImage(ImageElement.as(image.getElement()));
            // drawImage.makeDraggable();
            image.removeFromParent();
            redraw();
        });
        RootPanel.get().add(image);// need to ensure loadhandler fires
    }

    @Override
    public void addTheme(ResourceBasics graphTheme) {// OK
        themeListBox.getStore().add(graphTheme);
    }

    public void updateTheme(final ResourceBasics item) {

        graphSettings.setTheme(item);

        if (graphSettings.getCurrentTheme() != null) {
            refreshView(graphSettings.getCurrentTheme());
        } else {
            graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

                @Override
                public void onSuccess(GraphTheme result) {

                    refreshView(result);


                }
            });
        }
    }

    private void addThemeChangeHandler() {
        themeListBox.addSelectionHandler(event -> updateTheme(event.getSelectedItem()));

        themeListBox.addKeyUpHandler(event -> {
            String text = themeListBox.getText();
            ResourceBasics theme = null;
            boolean doUpdate = false;
            if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                if (text == null || text.equals("")) {
                    theme = null;
                } else {
                    boolean found = false;
                    List<ResourceBasics> themes = themeListBox.getStore().getAll();
                    for (ResourceBasics resourceBasics : themes) {
                        if (text.equals(resourceBasics.getName())) {
                            theme = resourceBasics;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        theme = null;
                    }
                }
                doUpdate = true;
            } else {
                List<ResourceBasics> themes = themeListBox.getStore().getAll();
                for (ResourceBasics resourceBasics : themes) {
                    if (text.equals(resourceBasics.getName())) {
                        theme = resourceBasics;
                        doUpdate = true;
                        break;
                    }
                }
            }

            if (doUpdate) {
                themeListBox.select(theme);
                themeListBox.setValue(theme, false);
                themeListBox.setText(theme.getName());
                updateTheme(theme);
            }
        });

        themeListBox.addBlurHandler(event -> {
            String text = themeListBox.getText();
            boolean doUpdate;
            ResourceBasics theme = themeListBox.getCurrentValue();
            if (text == null || text.isEmpty()) {
                doUpdate = true;
            } else {
                List<ResourceBasics> themes = themeListBox.getStore().getAll();
                doUpdate = true;
                for (ResourceBasics resourceBasics : themes) {
                    if (text.equals(resourceBasics.getName())) {
                        doUpdate = false;
                        break;

                    }
                }
            }

            if (doUpdate) {
                themeListBox.select(theme);
                themeListBox.setValue(theme, false);
                if (theme == null) {
                    themeListBox.setText("");
                } else {
                    themeListBox.setText(theme.getName());
                }
                updateTheme(theme);
            }
        });
    }

    @Override
    public void clear() {
        nodeLayer.clear();
        linkLayer.clear();

    }

    @Override
    public void clearThemes() {
        themeListBox.getStore().clear();
    }

    private void createBackgroundLayer() { // FIXME:

    }

    private void initFilters() {
        filterListBox.getStore().clear();
        List<Filter> filters = graphSettings.dataViewDef.getFilters();
        int position = 1;
        filterListBox.getStore().add(CentrifugeConstantsLocator.get().bundleFunctionNone()); //$NON-NLS-1$
        for (Filter filter : filters) {
            positionToFilterMap.put(position++, filter);
            filterListBox.getStore().add(filter.getName());
        }
        filterListBox.getStore().add(CentrifugeConstantsLocator.get().kmlExportDialognewFilterDropBox()); //$NON-NLS-1$
    }

    @UiHandler("filterListBox")
    void onSelection(SelectionEvent<String> event) {
        onFilterListBox();
    }

    private void onFilterListBox() {
        if (filterListBox.getItemCount() == filterListBox.getSelectedIndex() + 1) {
            new CreateEditFilterDialog(null, null, graphSettings.getDataViewUuid(),
                    filter -> {
                        initFilters();
                        setFilter(filter);
                        graphSettings.visualizationDef.setFilter(filter);
                    }, () -> graphSettings.visualizationDef.setFilter(null)).show();
        } else if (filterListBox.getSelectedIndex() == 0) {
            graphSettings.visualizationDef.setFilter(null);
        } else {
            graphSettings.visualizationDef.setFilter(positionToFilterMap.get(filterListBox.getSelectedIndex()));
        }
    }

    private void setFilter(Filter filter) {
        Integer index = positionToFilterMap.inverse().get(filter);
        if (index != null) {
            filterListBox.setSelectedIndex(index);
        } else {
            filterListBox.setSelectedIndex(0);
        }
    }

    private void initLayouts() {
        if (layoutListBox.getStore().size() == 0)
            for (GraphLayout layout : GraphLayout.values()) {
                //We don't add applyforce since that's force directed
                if (GraphLayout.applyForce != layout && GraphLayout.scramble != layout)
                    layoutListBox.getStore().add(layout);
            }
    }

    @UiHandler("layoutListBox")
    void onLayoutSelection(SelectionEvent<GraphLayout> event) {
        GraphLayout layout = event.getSelectedItem();
        graphSettings.getVisualizationDef().setLayout(layout);
        modelSave();
        modelSaveSettings();
        VortexFuture<List<CsiMap<String, String>>> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).clearGraphBeforeLoad(graphSettings.getVisualizationDef().getUuid(), layout.toString());
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
    }

    private void modelSave() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).saveGraph(graphSettings.getVisualizationDef().getUuid());
        } catch (IOException | CentrifugeException e) {
            e.printStackTrace();
        }
    }

    private void modelSaveSettings() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(graphSettings.getVisualizationDef(), graphSettings.getDataViewUuid(), false);
        } catch (Exception myException) {
            Dialog.showException(myException);
        }
    }

    private void setLayout(GraphLayout layout) {
        if (layout != null) {
            layoutListBox.setValue(layout);
        } else {
            layoutListBox.setSelectedIndex(0);
        }
    }

    public NodeLayer getNodeLayer() {
        return nodeLayer;
    }

    void endDrag(MouseUpEvent event) { // FIXME:
        nodeLayer.setVisible(true);
        linkLayer.setVisible(true);
        dragNodeLayer.setVisible(false);
        // shadowNodeLayer.setVisible(false);
        Optional<Renderable> hitTest = nodeLayer.hitTest(event.getX(), event.getY());
        if (!hitTest.isPresent()) {
            nodeUserDrags.setDrawX(copyOfNodeUserDragsOnDragLayer.getDrawX());
            nodeUserDrags.setDrawY(copyOfNodeUserDragsOnDragLayer.getDrawY());
        } else {
            Renderable renderable = hitTest.get();
            if (renderable instanceof DrawNode) {
                DrawNode drawNode = (DrawNode) renderable;
                if (drawNode.getNodeProxy() == nodeUserDrags.getNodeProxy()) {
                    graphSettings.editNode(nodeUserDrags.getNodeProxy());
                } else {
                    graphSettings.addLink(nodeUserDrags.getNodeProxy(), drawNode.getNodeProxy());
                }
            }
        }
    }

    //    @Override
    //    public Panel getDetailsContainer() {
    //        while (detailsContainer.getWidgetCount() != 0) {
    //            detailsContainer.remove(0);// always remove the first child
    //        }
    //        return detailsContainer;
    //    }

    GraphSettings getGraphSettings() {
        return graphSettings;
    }

    @Override
    public VisualizationSettingsModal getModal() {
        return modal;
    }

    @Override
    public void hide() {// OK
        modal.hide();
    }

    //    public void hideDetails() {
    //        detailsContainer.getElement().getStyle().setOpacity(0);
    //        detailsContainer.removeFromParent();
    //        //fieldDefGrid.getElement().getStyle().setOpacity(1);
    //    }

    @Override
    public void redraw() { // TODO: lets think about this
        drawingPanel.render();
    }

    @Override
    public void removeAllNodes() {
        nodeLayer.clear();
    }

    @Override
    public void setCurrentBackgroundColor(String color) {
        backgroundColorButton.getElement().getStyle().setBackgroundColor(color);
        if (backgroundFill == null) {
            //TODO: perhaps a Rectangle that takes percentages would be better suited here. At the moment we know the width and 10k is much greater.
            backgroundFill = new Rectangle(0, 0, 10000, 10000);
            background_Layer.addItem(backgroundFill);
        }
        backgroundFill.setFillStyle(CssColor.make(color));
    }

    @Override
    public void setLoadAfterSave(boolean value) {
        // TODO:
        // footer.setLoadAfterSave(value);
    }

    @Override
    public void setTheme(GraphTheme graphTheme) {// OK
        if (graphTheme == null) {
            themeListBox.clear();
        } else {

            for (ResourceBasics resource : themeListBox.getStore().getAll()) {
                if (resource.getUuid().equals(graphTheme.getUuid())) {
                    themeListBox.setValue(resource, true);
                    break;
                }
            }
        }

        if (themeListBox.getValue() == null && defaultTheme != null && graphSettings.getThemeUuid() != null && graphSettings.getThemeUuid().equals(defaultTheme)) {

            boolean found = false;
            for (ResourceBasics resource : themeListBox.getStore().getAll()) {
                if (resource.getName().equals(defaultTheme)) {
                    themeListBox.setValue(resource, true);
                    updateTheme(resource);
                    found = true;
                    break;
                }
            }


            if (!found) {
                graphSettings.setThemeUuid(null);
            }

        }


    }

    @Override
    public void setVisualizationTitle(String visualizationTitle) { // OK
        this.visualizationTitle.setValue(visualizationTitle);
    }

    @UiHandler("visualizationTitle")
    public void onTitleChange(ValueChangeEvent<String> event) {
        graphSettings.getModel().setName(event.getValue());
    }

    @Override
    public void show() {
        modal.show();
    }

    //    @Override
    //    public void showDetails() { // OK
    ////        mainLeft.add(detailsContainer);
    ////        fieldDefGrid.getElement().getStyle().setOpacity(0);
    //        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
    //
    //            @Override
    //            public void execute() {
    //                detailsContainer.getElement().getStyle().setOpacity(1);
    //
    //            }
    //        });
    //    }

    void startDragNode(DrawNode drawNode) { // FIXME:
        checkNotNull(drawNode);
        // hide nodeLayer and linkLayer, show dragNodeLayer and shadowNodeLayer
        // shadowNodeLayer.setVisible(true);
        dragNodeLayer.setVisible(true);
        // nodeLayer.setVisible(false);
        nodeUserDrags = drawNode;
        drawNode.getNodeProxy();
        copyOfNodeUserDragsOnDragLayer = drawNode.copyAsDrawImage();
        copyOfNodeUserDragsOnDragLayer.makeDraggable();
        dragNodeLayer.clear();
        dragNodeLayer.addItem(copyOfNodeUserDragsOnDragLayer);
        copyOfNodeUserDragsOnDragLayer
                .dragStart((int) (copyOfNodeUserDragsOnDragLayer.getDrawX() + (copyOfNodeUserDragsOnDragLayer
                                .getImageWidth() / 2D)),
                        (int) (copyOfNodeUserDragsOnDragLayer.getDrawY() + (copyOfNodeUserDragsOnDragLayer
                                .getImageWidth() / 2D)));
        // drawingPanel.bringToFront(shadowNodeLayer);
        drawingPanel.bringToFront(dragNodeLayer);
    }

    private void toggleColorMenu() {
        colorPalette.setVisible(!colorPalette.isVisible());
    }

    public void editLink(LinkProxy linkProxy) {
        graphSettings.editLink(linkProxy);

    }

    @Override
    public void setRenderThreshold(int renderThreshold) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLoadOnStartup(boolean value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showDetails() {

        if (detailsInfo != null) {
            //detailsInfo.setVisible(true);
        }
    }

    @Override
    public Panel getDetailsContainer(NodeProxy nodeProxy) {
        checkNotNull(nodeProxy);
        this.detailsInfo = new FluidContainer();

        this.detailsInfo.setWidth("250px");
        this.detailsInfo.setHeight("325px");

        detailsInfo.setVisible(false);
        panel.add(detailsInfo);

        detailsInfo.getElement().getStyle().setPosition(Position.ABSOLUTE);
        detailsInfo.addStyleName("graph-settings-node-info");
        detailsInfo.addStyleName("overlay");
        detailsInfo.addStyleName("graph-setting-detail-panel");
        if (nodeProxy.getX() > .5) {
            detailsInfo.getElement().getStyle().setLeft(0, Unit.PX);
        } else {
            detailsInfo.getElement().getStyle().setRight(0, Unit.PX);
        }

        return detailsInfo;
    }

    //    private static class FieldDefSearchHandler implements KeyUpHandler {
    //
    //        private GraphSettingsPanel graphSettingsPanel;
    //
    //        public FieldDefSearchHandler(GraphSettingsPanel graphSettingsPanel) {
    //            this.graphSettingsPanel = graphSettingsPanel;
    //        }
    //
    //        @Override
    //        public void onKeyUp(KeyUpEvent event) {
    //            String text = graphSettingsPanel.searchField.getTextBox().getText();
    //            graphSettingsPanel.fieldDefGrid.filter(text);
    //        }
    //    }

    void hideDetails() {
        if (detailsInfo != null) {
            this.detailsInfo.setVisible(false);
            this.detailsInfo.removeFromParent();
            this.detailsInfo = null;
        }
    }

    private void applyTheme(final LinkProxy linkProxy, final NodeProxyFactory nodeProxyFactory, GraphTheme result) {
        String themeOverride = findThemeColor(linkProxy, result);
        DrawLink edge = new DrawLink(nodeProxyFactory.create(linkProxy.getNode1()), nodeProxyFactory.create(linkProxy.getNode2()), linkProxy, themeOverride);
        linkLayer.addItem(edge);
        graphSettings.getView().redraw();
    }

    private void refreshView(GraphTheme result) {
        List<Renderable> renderables = nodeLayer.getRenderables();
        nodeLayer.clear();
        for (Renderable renderable : renderables) {
            if (renderable instanceof DrawNode) {
                NodeProxy nodeProxy = ((DrawNode) renderable).getNodeProxy();
                nodeProxy.apply(graphSettings);
                addNode(nodeProxy);
            }
        }

        renderables = linkLayer.getRenderables();
        linkLayer.clear();
        for (Renderable renderable : renderables) {
            if (renderable instanceof DrawLink) {
                LinkProxy linkProxy = ((DrawLink) renderable).getLinkProxy();
                linkProxy.apply(graphSettings);
                addLink(linkProxy);
            }
        }

        drawingPanel.render();
    }

    interface MyUiBinder extends UiBinder<VisualizationSettingsModal, GraphSettingsPanel> {
    }

    private static class ColorSwatchClickHandler implements ClickHandler {

        private GraphSettingsPanel graphSettingsPanel;

        ColorSwatchClickHandler(GraphSettingsPanel graphSettingsPanel) {
            this.graphSettingsPanel = graphSettingsPanel;
        }

        @Override
        public void onClick(ClickEvent event) {
            graphSettingsPanel.toggleColorMenu();
        }
    }

    private class ColorChooserClickHandler2 implements SelectionHandler<String> {

        @Override
        public void onSelection(SelectionEvent<String> event) {
            if (event.getSelectedItem() != null) {
                Color color = ClientColorHelper.get().makeFromHex(event.getSelectedItem());
                // update displayed values
                backgroundColorButton.getElement().getStyle().setBackgroundColor("#" + event.getSelectedItem());
                // update draw area
                backgroundFill.setFillStyle(CssColor.make("#" + event.getSelectedItem()));
                if (color.getLuma() > .5) {
                    nodeLayer.setOnDark(false);
                } else {
                    nodeLayer.setOnDark(true);
                }
                // update model
                GraphSettingsModel model = graphSettings.getModel();
                int intColor = color.getIntColor();
                if (intColor == 16777215) {
                    intColor = 16711422;
                }
                model.setPropertyValue("csi.relgraph.backgroundColor", String.valueOf(intColor));//NON-NLS
                drawingPanel.render();
                colorPalette.setVisible(false);
            }
        }
    }
}
