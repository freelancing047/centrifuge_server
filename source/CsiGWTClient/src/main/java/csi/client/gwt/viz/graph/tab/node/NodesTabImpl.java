package csi.client.gwt.viz.graph.tab.node;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.DataViewWidget;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.viz.graph.tab.SessionPersistentColumnList;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.combo_boxes.CommonNumberComboBox;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.layout.window.WindowLayout;
import csi.client.gwt.worksheet.tab.WorksheetTabPanel;
import csi.server.business.service.GraphActionsService;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO.NodeListFieldNames;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class NodesTabImpl extends SessionPersistentColumnList {

	public static final SafeStyles CLEAR_TEXT = new SafeStyles() {
        @Override
        public String asString() {
            return "color: rgba(0,0,0,0)";//NON-NLS //$NON-NLS-1$
        }
    };
    public static final SafeStyles NO_STYLE = new SafeStyles() {
        @Override
        public String asString() {
            return ""; //$NON-NLS-1$
        }
    };


    private static final NodeListProperties nodeProps = GWT.create(NodeListProperties.class);

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final String LABEL_STYLE = "filter-label-close-button";//NON-NLS //$NON-NLS-1$
    private static final String LABEL_VISIBLE_STYLE = "filter-label-close-button-visible";//NON-NLS //$NON-NLS-1$
    private static final String TRUE_STRING = i18n.nodesTabTrueValue(); //$NON-NLS-1$
    private static final String BOOLEAN_STRING = i18n.nodesTabBooleanLabel(); //$NON-NLS-1$
    private static final String FALSE_STRING = i18n.nodesTabFalseValue(); //$NON-NLS-1$
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    NavLink addSelectNavLink;
    //filters
    int unbundledState = 0;
    int unselectedState = 0;
    int unplunkedState = 0;
    int visibleState = 0;
    int hiddenState = 0;
    int plunkedState = 0;
    int selectedState = 0;
    int bundledState = 0;
    int annotationState = 0;
    int isBundleState = 0;
    int unannotationState = 0;
    @UiField
    NavLink bundleNavLink;
    @UiField(provided = true)
    GridContainer gridContainer;
    @UiField
    FluidRow controlLayer;
    @UiField
    NavLink hideNavLink;
    @UiField
    CommonNumberComboBox itemsToShowListBox;
    @UiField
    Button removeButton;
    @UiField
    NavLink removeSelectNavLink;
    Button resizeButton;
    @UiField
    TextBox searchTextBox;
    @UiField
    NavLink selectNavLink;
    @UiField
    NavLink showNavLink;
    Column tableColumn;
    @UiField
    NavLink unbundleNavLink;
    @UiField
    NavLink zoomNavLink;
    @UiField(provided = true)
    LayoutPanel layoutPanel;
    @UiField
    NavLink selectedFilter;
    @UiField
    NavLink unselectedFilter;
    @UiField
    NavLink bundledFilter;
    @UiField
    NavLink unbundledFilter;
    @UiField
    NavLink plunkedFilter;
    @UiField
    NavLink unplunkedFilter;
    @UiField
    NavLink hiddenFilter;
    @UiField
    NavLink visibleFilter;
    @UiField
    NavLink isBundleFilter;
    @UiField
    NavLink annotationFilter;
    @UiField
    NavLink unannotationFilter;
    //Where tags for filters appear
    @UiField
    Controls tagCloud;
    @UiField
    Dropdown filterCombo;

    @UiField
    NavLink exportNodesList;
    //Icon for filters
    @UiField
    com.github.gwtbootstrap.client.ui.InputAddOn filterIcon;
    @UiField
    DropdownButton actionsButton;
    private ColumnConfig<NodeListDTO, Double> eigenvectorCol;
    private ColumnConfig<NodeListDTO, Double> degreesCol;
    private ColumnConfig<NodeListDTO, Double> closenessCol;
    private ColumnConfig<NodeListDTO, Double> betweennessCol;
    private CsiCheckboxSelectionModel<NodeListDTO> sm;
    private List<NodeListDTO> selection = new ArrayList<>();
    private boolean changeSelected = true;
    private Graph graph;
    private RemoteLoadingGridComponentManager<NodeListDTO> gridComponentManager;
    private StringFilter<NodeListDTO> labelFilter;
    private StringFilter<NodeListDTO> typeMenuFilter;
    private Grid<NodeListDTO> resultsGrid;
    private GraphTab tab;
    private PagingToolBar toolBar;
    private ListStore<String> typeFilterStore;

    public NodesTabImpl(final Graph graph) {
        this.graph = graph;

        gridContainer = new GridContainer(45);
        gridContainer.setPager(null);// save space for the pager
        layoutPanel = new LayoutPanel() {
            @Override
            public void onResize() {
                showHideFilters();
                super.onResize();
            }


        };
        layoutPanel.setWidth("100%"); //$NON-NLS-1$
        layoutPanel.setHeight("100%"); //$NON-NLS-1$

        tab = uiBinder.createAndBindUi(this);
        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new GraphEventHandler() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                onGraphLoad();
            }
        });
        controlLayer.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
        tab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                graph.showControlBar(false);
            }
        });

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                filterIcon.setTitle(CentrifugeConstantsLocator.get().filters());

            }
        });

    }


    public void refresh() {
        toolBar.refresh();

        List<NodeListDTO> nodes = resultsGrid.getStore().getAll();
        for (NodeListDTO node : nodes) {
            for (NodeListDTO selected : selection) {
                if (node.getID() == selected.getID()) {
                    node.setSelected(true);
                }
            }
        }


        showHideFilters();
    }

    public void showSnaColumns() {
        closenessCol.setColumnTextStyle(NO_STYLE);
        eigenvectorCol.setColumnTextStyle(NO_STYLE);
        betweennessCol.setColumnTextStyle(NO_STYLE);

    }

    private void showHideFilters() {
        int width = layoutPanel.getOffsetWidth();
        if (width < 465) {

            tagCloud.setVisible(false);
            filterCombo.setVisible(false);
            filterIcon.setVisible(false);

        } else {

            tagCloud.setVisible(true);
            int givenSpace = width - 480;

            if (tagCloud.getOffsetWidth() < givenSpace) {

                tagCloud.setVisible(true);
                filterCombo.setVisible(true);
                filterIcon.setVisible(true);
            }
            //        	else if(width<610){
            //			//TODO: make it so filters can collapse in a cool way
            //            	collapseFilters();
            //
            //            }
            else {

                tagCloud.setVisible(false);
                filterCombo.setVisible(true);
                filterIcon.setVisible(true);
            }
        }
    }

    // this mehtod is really nice..
    private void collapseFilters() {

	}



    // this should be a function that takes two arguments and whatever else for each filter, they are legit dupes.
    // the only thing is that those ints laying around randomlty in this file are really annoyuing
    protected Optional<FilterConfigBean> getFilter(NodeListFieldNames field, int currentState){
        FilterConfigBean filterConfigBean = new FilterConfigBean();
        filterConfigBean.setField(field.toString());
        filterConfigBean.setType(BOOLEAN_STRING);
        switch (currentState) {
            case 1:
                filterConfigBean.setValue(TRUE_STRING);
                break;
            case 2:
                filterConfigBean.setValue(FALSE_STRING);
                break;
            default:
                return Optional.absent();
        }
        return Optional.of(filterConfigBean);
    }

    protected Optional<FilterConfigBean> getUnbundledFilter() {
        return getFilter(NodeListFieldNames.BUNDLED, unbundledState);
    }

    protected Optional<FilterConfigBean> getUnplunkedFilter() {
        return getFilter(NodeListFieldNames.PLUNKED, unplunkedState);
    }

    protected Optional<FilterConfigBean> getAnnotationFilter() {
        return getFilter(NodeListFieldNames.ANNOTATION, annotationState);
    }

    protected Optional<FilterConfigBean> getUnannotationFilter() {
        return getFilter(NodeListFieldNames.ANNOTATION, unannotationState);
    }

    protected Optional<FilterConfigBean> getVisibleFilter() {
        return getFilter(NodeListFieldNames.HIDDEN, visibleState);
    }

    protected Optional<FilterConfigBean> getUnselectedFilter() {
        return getFilter(NodeListFieldNames.SELECTED, unselectedState);
    }

    protected Optional<FilterConfigBean> getBundledFilter() {
        return getFilter(NodeListFieldNames.BUNDLED, bundledState);
    }

    protected Optional<FilterConfigBean> getPlunkedFilter() {
        return getFilter(NodeListFieldNames.PLUNKED, plunkedState);
    }

    protected Optional<FilterConfigBean> getHiddenFilter() {
        return getFilter(NodeListFieldNames.HIDDEN, hiddenState);
    }

    protected Optional<FilterConfigBean> getSelectedFilter() {
        return getFilter(NodeListFieldNames.SELECTED, selectedState);
    }

    public Tab getTab() {
        return tab;
    }

    @UiHandler("selectedFilter")
    void onSelectedFilter(ClickEvent event) {

        if (selectedState == 0) {
            selectedState = 1;
            tagCloud.add(createLabel(Filters.SELECTED));
            refresh();
            selectedFilter.setBaseIcon(IconType.CHECK);
        } else {
            handleDelete(Filters.SELECTED, true);
        }
    }

    @UiHandler("unselectedFilter")
    void onUnselectedFilter(ClickEvent event) {
        if (unselectedState == 0) {
            unselectedState = 2;
            tagCloud.add(createLabel(Filters.UNSELECTED));
            refresh();
            unselectedFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.UNSELECTED, true);
        }
    }

    @UiHandler("hiddenFilter")
    void onHiddenFilter(ClickEvent event) {
        if (hiddenState == 0) {
            tagCloud.add(createLabel(Filters.HIDDEN));
            hiddenState = 1;
            refresh();
            hiddenFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.HIDDEN, true);
        }
    }

    @UiHandler("visibleFilter")
    void onVisibleFilter(ClickEvent event) {
        if (visibleState == 0) {
            visibleState = 2;
            tagCloud.add(createLabel(Filters.VISIBLE));
            refresh();
            visibleFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.VISIBLE, true);
        }
    }

    @UiHandler("bundledFilter")
    void onBundledFilter(ClickEvent event) {
        if (bundledState == 0) {
            bundledState = 1;
            tagCloud.add(createLabel(Filters.BUNDLED));
            refresh();
            bundledFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.BUNDLED, true);
        }
    }

    @UiHandler("unbundledFilter")
    void onUnbundledFilter(ClickEvent event) {
        if (unbundledState == 0) {
            unbundledState = 2;
            tagCloud.add(createLabel(Filters.UNBUNDLED));
            refresh();
            unbundledFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.UNBUNDLED, true);
        }
    }

    @UiHandler("plunkedFilter")
    void onPlunkedFilter(ClickEvent event) {
        if (plunkedState == 0) {
            plunkedState = 1;
            tagCloud.add(createLabel(Filters.PLUNKED));
            refresh();
            plunkedFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.PLUNKED, true);
        }
    }

    @UiHandler("unplunkedFilter")
    void onUnplunkedFilter(ClickEvent event) {
        if (unplunkedState == 0) {
            unplunkedState = 2;
            tagCloud.add(createLabel(Filters.UNPLUNKED));
            refresh();
            unplunkedFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.UNPLUNKED, true);
        }
    }
    @UiHandler("isBundleFilter")
    void onIsBundleFilter(ClickEvent event) {

        if (isBundleState == 0) {
            isBundleState = 1;
            tagCloud.add(createLabel(Filters.IS_BUNDLE));
            refresh();
            isBundleFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.IS_BUNDLE, true);
        }
    }

    @UiHandler("annotationFilter")
    void onAnnotationFilter(ClickEvent event) {

        if (annotationState == 0) {
            annotationState = 1;
            tagCloud.add(createLabel(Filters.COMMENTED));
            refresh();
            annotationFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.COMMENTED, true);
        }
    }

    @UiHandler("unannotationFilter")
    void onUnannotationFilter(ClickEvent event) {

        if (unannotationState == 0) {
            unannotationState = 2;
            tagCloud.add(createLabel(Filters.UNCOMMENTED));
            refresh();
            unannotationFilter.setBaseIcon(IconType.CHECK);
        } else{
            handleDelete(Filters.UNCOMMENTED, true);
        }
    }

    @UiHandler("removeFilters")
    void clearAllFilters(ClickEvent event) {

        selectedState = 0;
        unselectedState = 0;
        hiddenState = 0;
        visibleState = 0;
        plunkedState = 0;
        unplunkedState = 0;
        bundledState = 0;
        unbundledState = 0;
        annotationState = 0;
        unannotationState = 0;

        for (Filters filter : Filters.values()) {

            handleDelete(filter, false);

        }
        refresh();

        showHideFilters();
    }

    private LabelControl createLabel(Filters filterType) {

        final LabelControl control = new LabelControl(filterType);
        control.getElement().getStyle().setDisplay(Display.INLINE);
        Label label = new Label();

        label.setType(LabelType.INFO);
        label.addStyleName("filter-label");//NON-NLS //$NON-NLS-1$
        if (filterType == Filters.UNPLUNKED) {
            label.setText(i18n.nodesTabDataLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.PLUNKED) {
            label.setText(i18n.nodesTabUserLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.SELECTED) {
            label.setText(i18n.nodesTabSelectedLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.UNSELECTED) {
            label.setText(i18n.nodesTabUnselectedLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.BUNDLED) {
            label.setText(i18n.nodesTabBundledLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.UNBUNDLED) {
            label.setText(i18n.nodesTabUnBundledLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.VISIBLE) {
            label.setText(i18n.nodesTabVisibleLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.HIDDEN) {
            label.setText(i18n.nodesTabHiddenLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.IS_BUNDLE) {
            label.setText(i18n.nodesTabIsBundleLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.COMMENTED) {
            label.setText(i18n.nodesTabCommentedLabel()); //$NON-NLS-1$
        } else if (filterType == Filters.UNCOMMENTED) {
            label.setText(i18n.nodesTabUncommentedLabel()); //$NON-NLS-1$
        } else {
            label.setText(filterType.toString());
        }
        final Anchor closeLabel = new Anchor("x"); //$NON-NLS-1$
        closeLabel.addStyleName(LABEL_STYLE);

        MouseOverHandler overHandler = createMouseOverHandler(closeLabel);
        MouseOutHandler outHandler = createMouseOutHandler(closeLabel);
        closeLabel.addMouseUpHandler(createDeleteHandler(control, filterType));

        label.addMouseOverHandler(overHandler);
        label.addMouseOutHandler(outHandler);

        closeLabel.addMouseOverHandler(overHandler);
        closeLabel.addMouseOutHandler(outHandler);

        control.add(label);
        control.add(closeLabel);
        return control;
    }

    private MouseUpHandler createDeleteHandler(final Controls control, final Filters filterType) {
        return new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                handleDelete(control, filterType, true);

            }

        };
    }

    private MouseOutHandler createMouseOutHandler(final Widget label) {
        return new MouseOutHandler() {

			@Override
            public void onMouseOut(MouseOutEvent event) {
                label.removeStyleName(LABEL_VISIBLE_STYLE);
                label.addStyleName(LABEL_STYLE);
            }
        };
    }

    private MouseOverHandler createMouseOverHandler(final Widget label) {
        return new MouseOverHandler() {

			@Override
            public void onMouseOver(MouseOverEvent event) {
                label.removeStyleName(LABEL_STYLE);
                label.addStyleName(LABEL_VISIBLE_STYLE);
            }};
	}

    @UiHandler("addSelectNavLink")
    void onAddSelectNodes(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            nodeIds.add(nodeListDTO.getID());
        }
        GraphRequest request = new GraphRequest();
        request.nodes.addAll(nodeIds);
        try {
            future.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), false, false, false, request);
        } catch (CentrifugeException e) {
        }
        graph.getGraphSurface().refresh(future);
    }

    private void handleDelete(Filters filter, boolean refresh) {
        int tags = tagCloud.getWidgetCount();

        for (int ii = tags - 1; ii >= 0; ii--) {
            Widget widget = tagCloud.getWidget(ii);
            if (widget != null && widget instanceof LabelControl) {
                LabelControl labelControl = ((LabelControl) widget);
                if (labelControl.getFilter() == filter) {
                    handleDelete(labelControl, filter, refresh);
                }
            }
        }

    }

    private void handleDelete(final Controls control, final Filters filterType, boolean refresh) {
        control.setVisible(false);
        control.removeFromParent();

        switch (filterType) {
            case SELECTED:
                selectedState = 0;
                selectedFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case UNSELECTED:
                unselectedState = 0;
                unselectedFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case BUNDLED:
                bundledState = 0;
                bundledFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case UNBUNDLED:
                unbundledState = 0;
                unbundledFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case VISIBLE:
                visibleState = 0;
                visibleFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case HIDDEN:
                hiddenState = 0;
                hiddenFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case PLUNKED:
                plunkedState = 0;
                plunkedFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case UNPLUNKED:
                unplunkedState = 0;
                unplunkedFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case IS_BUNDLE:
                isBundleState = 0;
                bundledFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case COMMENTED:
                annotationState = 0;
                annotationFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            case UNCOMMENTED:
                unannotationState = 0;
                unannotationFilter.setBaseIcon(IconType.CHECK_EMPTY);
                break;
            default:
                break;
        }

        if (refresh) {
            refresh();
            showHideFilters();
        }
    }

    @UiHandler("bundleNavLink")
    void onBundleNodes(ClickEvent event) {
        //verify a valid bundle could be created.
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            String bundleNodeLabel = nodeListDTO.getBundleNodeLabel();
            if (!"-".equals(bundleNodeLabel)) { //$NON-NLS-1$
                new ErrorDialog(i18n.nodesTabBundleErrorTitle(), i18n.nodesTabBundleErrorMessage()).show(); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
        }
        BundleDialog bundleDialog = new BundleDialog(graph, this);
        bundleDialog.asDialog().show();
    }

    @UiHandler("removeButton")
    public void onClick(ClickEvent event) {
        searchTextBox.setValue(""); //$NON-NLS-1$
        refresh();
    }

    @SuppressWarnings("unchecked")
    private void onGraphLoad() {
        IdentityValueProvider<NodeListDTO> identity = new IdentityValueProvider<>();
        if(sm == null)
            sm = new CsiCheckboxSelectionModel<>(identity);

        // anchored
        ColumnConfig<NodeListDTO, Boolean> anchoredCol = new ColumnConfig<>(nodeProps.anchored(),
                200, i18n.nodesTabAnchoredColumn()); //$NON-NLS-1$
        // betweenness
        betweennessCol = new ColumnConfig<>(
                nodeProps.betweenness(), 200, i18n.nodesTabBetweennessColumn()); //$NON-NLS-1$
        betweennessCol.setColumnTextStyle(CLEAR_TEXT);
        // bundled
        ColumnConfig<NodeListDTO, Boolean> bundleCol = new ColumnConfig<>(nodeProps.bundled(), 200,
                i18n.nodesTabBundledColumn()); //$NON-NLS-1$
        // bundleNodeLabel
        ColumnConfig<NodeListDTO, String> bundleNameCol = new ColumnConfig<>(
                nodeProps.bundleNodeLabel(), 200, i18n.nodesTabBudleNameColumn()); //$NON-NLS-1$
        // closeness
        closenessCol = new ColumnConfig<>(nodeProps.closeness(),
                200, i18n.nodesTabClosenessColumn()); //$NON-NLS-1$
        closenessCol.setColumnTextStyle(CLEAR_TEXT);
        // component
        ColumnConfig<NodeListDTO, Integer> componentCol = new ColumnConfig<>(nodeProps.component(),
                200, i18n.nodesTabComponentColumn()); //$NON-NLS-1$
        // degrees
        degreesCol = new ColumnConfig<>(nodeProps.degrees(), 200,
                i18n.nodesTabDegreeColumn()); //$NON-NLS-1$
        // eigenvector
        eigenvectorCol = new ColumnConfig<>(
                nodeProps.eigenvector(), 200, i18n.nodesTabEigenvectorColumn()); //$NON-NLS-1$
        eigenvectorCol.setColumnTextStyle(CLEAR_TEXT);
        // hidden
        ColumnConfig<NodeListDTO, Boolean> hiddenCol = new ColumnConfig<>(nodeProps.hidden(), 200,
                i18n.nodesTabHiddenColumn()); //$NON-NLS-1$
        // hideLabels
        ColumnConfig<NodeListDTO, Boolean> hideLabelsCol = new ColumnConfig<>(
                nodeProps.hideLabels(), 200, i18n.nodesTabHideLabelsColumn()); //$NON-NLS-1$
        // label
        ColumnConfig<NodeListDTO, String> nameCol = new ColumnConfig<>(nodeProps.label(), 200,i18n.nodesTabLabelColumn()); //$NON-NLS-1$
        // selected
        ColumnConfig<NodeListDTO, Boolean> selectedCol = new ColumnConfig<>(nodeProps.selected(),
                200, i18n.nodesTabSelectedColumn()); //$NON-NLS-1$
        // type
        ColumnConfig<NodeListDTO, String> typeCol = new ColumnConfig<>(nodeProps.type(), 200, i18n.nodesTabTypeColumn()); //$NON-NLS-1$
        // visible neighbors
        ColumnConfig<NodeListDTO, Integer> visibleNeighborsCol = new ColumnConfig<>(
                nodeProps.visibleNeighbors(), 200, i18n.nodesTabVisibleNeighborsColumn()); //$NON-NLS-1$
        // visualized
        ColumnConfig<NodeListDTO, Boolean> visualizedCol = new ColumnConfig<>(
                nodeProps.visualized(), 200, i18n.nodesTabVisualizedColumn()); //$NON-NLS-1$

        ColumnConfig<NodeListDTO, Boolean> annotationCol = new ColumnConfig<>(
                nodeProps.annotation(), 200, i18n.nodesTabCommentColumn()); //$NON-NLS-1$
        ColumnConfig<NodeListDTO, Boolean> isBundleCol = new ColumnConfig<>(
                nodeProps.isBundle(), 200, i18n.nodesTabIsBundleColumn()); //$NON-NLS-1$

        // need to i18n
        ColumnConfig<NodeListDTO, Double> sizeColumn = new ColumnConfig<>(nodeProps.size(), 200, i18n.nodesTabSize()); //$NON-NLS-1$

        ColumnConfig<NodeListDTO, Double> opacityCol = new ColumnConfig<>(nodeProps.transparency(), 200, i18n.nodesTabOpacity());


        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        gridComponentManager = componentFactory.createRemoteLoading(nodeProps.typedKey(),
                GraphActionServiceProtocol.class, (vortexService, loadConfig) -> {
                    List<FilterConfig> filters = loadConfig.getFilters();
                    FilterConfigBean filterConfigBean = new FilterConfigBean();
                    filterConfigBean.setField("label"); //$NON-NLS-1$
                    filterConfigBean.setType("string"); //$NON-NLS-1$
                    filterConfigBean.setValue(searchTextBox.getValue());
                    filters.add(filterConfigBean);
                    Optional<FilterConfigBean> selectedFilter = getSelectedFilter();
                    if (selectedFilter.isPresent()) {
                        filters.add(selectedFilter.get());
                    }
                    Optional<FilterConfigBean> bundledFilter = getBundledFilter();
                    if (bundledFilter.isPresent()) {
                        filters.add(bundledFilter.get());
                    }
                    Optional<FilterConfigBean> hiddenFilter = getHiddenFilter();
                    if (hiddenFilter.isPresent()) {
                        filters.add(hiddenFilter.get());
                    }
                    Optional<FilterConfigBean> plunkedFilter = getPlunkedFilter();
                    if (plunkedFilter.isPresent()) {
                        filters.add(plunkedFilter.get());
                    }
                    Optional<FilterConfigBean> unselectedFilter = getUnselectedFilter();
                    if (unselectedFilter.isPresent()) {
                        filters.add(unselectedFilter.get());
                    }
                    Optional<FilterConfigBean> unbundledFilter = getUnbundledFilter();
                    if (unbundledFilter.isPresent()) {
                        filters.add(unbundledFilter.get());
                    }
                    Optional<FilterConfigBean> visibleFilter = getVisibleFilter();
                    if (visibleFilter.isPresent()) {
                        filters.add(visibleFilter.get());
                    }
                    Optional<FilterConfigBean> unplunkedFilter = getUnplunkedFilter();
                    if (unplunkedFilter.isPresent()) {
                        filters.add(unplunkedFilter.get());
                    }
                    Optional<FilterConfigBean> unannotationFilter = getUnannotationFilter();
                    if (unannotationFilter.isPresent()) {
                        filters.add(unannotationFilter.get());
                    }
                    Optional<FilterConfigBean> annotationFilter = getAnnotationFilter();
                    if (annotationFilter.isPresent()) {
                        filters.add(annotationFilter.get());
                    }
                    Optional<FilterConfigBean> isBundleFilter = getIsBundleFilter();
                    if (isBundleFilter.isPresent()) {
                        filters.add(isBundleFilter.get());
                    }

                    selection = resultsGrid.getSelectionModel().getSelectedItems();

                    return vortexService.getPageableNodeListing(graph.getUuid(), loadConfig);

                });

        List<ColumnConfig<NodeListDTO, ?>> columnList = new ArrayList<>();
        columnList.add(sm.getColumn());
        columnList.add(nameCol);
        columnList.add(typeCol);
        columnList.add(bundleNameCol);
        columnList.add(betweennessCol);
        columnList.add(closenessCol);
        columnList.add(eigenvectorCol);
        columnList.add(degreesCol);
        columnList.add(visibleNeighborsCol);
        columnList.add(componentCol);
        columnList.add(bundleCol);
        columnList.add(selectedCol);
        columnList.add(anchoredCol);
        columnList.add(hideLabelsCol);
        columnList.add(hiddenCol);
        columnList.add(visualizedCol);
        columnList.add(annotationCol);
        columnList.add(isBundleCol);
        columnList.add(sizeColumn);
        columnList.add(opacityCol);

        sizeColumn.setHidden(true);
        sizeColumn.setFixed(true);
        sizeColumn.setGroupable(false);

        opacityCol.setHidden(true);
        opacityCol.setFixed(true);
        opacityCol.setGroupable(false);

        annotationCol.setHidden(true);
        annotationCol.setFixed(true);
        annotationCol.setGroupable(false);

        isBundleCol.setHidden(true);
        isBundleCol.setFixed(true);
        isBundleCol.setGroupable(false);

        bundleCol.setHidden(true);
        bundleCol.setFixed(true);
        bundleCol.setGroupable(false);

        anchoredCol.setHidden(true);
        anchoredCol.setFixed(true);
        anchoredCol.setGroupable(false);

        visualizedCol.setHidden(true);
        visualizedCol.setFixed(true);
        visualizedCol.setGroupable(false);

        selectedCol.setHidden(true);
        selectedCol.setFixed(true);
        selectedCol.setGroupable(false);

        hiddenCol.setHidden(true);
        hiddenCol.setFixed(true);
        hiddenCol.setGroupable(false);

        hideLabelsCol.setHidden(true);
        hideLabelsCol.setFixed(true);
        hideLabelsCol.setGroupable(false);

        ColumnModel<NodeListDTO> cm = getExistingColumnModel() == null ? new ColumnModel<>(columnList) : (ColumnModel<NodeListDTO>) getExistingColumnModel();

        ListStore<NodeListDTO> nodesStore = gridComponentManager.getStore();
        resultsGrid = new ResizeableGrid<>(nodesStore, cm);
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<NodeListDTO>> loader = gridComponentManager.getLoader();
        loader.addSortInfo(new SortInfoBean(NodeListDTO.NodeListFieldNames.LABEL.toString(), SortDir.ASC));
        resultsGrid.setLoader(loader);
        resultsGrid.setSelectionModel(sm);

        labelFilter = new StringFilter<>(nodeProps.label());
        typeMenuFilter = new StringFilter<>(nodeProps.type());
        GridFilters<NodeListDTO> filters = gridComponentManager.getGridFilters();
        NumericFilter<NodeListDTO, Integer> neighborFilter = new NumericFilter<>(nodeProps.visibleNeighbors(), new IntegerPropertyEditor());

        filters.initPlugin(resultsGrid);
        filters.setLocal(false);
        filters.addFilter(typeMenuFilter);
        filters.addFilter(labelFilter);
        filters.addFilter(neighborFilter);

        sm.addSelectionChangedHandler(event -> {
            if(resultsGrid.getSelectionModel().getSelectedItems().size() == 0){
                actionsButton.getTriggerWidget().setEnabled(false);
            }else{
                actionsButton.getTriggerWidget().setEnabled(true);
            }
            if (changeSelected) {
                selection = event.getSelection();
            } else {
                changeSelected = true;
            }

        });
        // TODO: Ask Wally how to get full list of node-types for a given graph.
        // We populate the type-filter using any row returned. We use the loader to get at that data.
        gridComponentManager.getLoader().addLoadHandler(
                new LoadHandler<FilterPagingLoadConfig, PagingLoadResult<NodeListDTO>>() {
                    private DataViewPresenter dataViewPresenter;
                    private WorksheetPresenter worksheet;
                    private HandlerRegistration handlerRegistration;

                    @Override
                    public void onLoad(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<NodeListDTO>> event) {
//                        if ((typeFilterStore.size() == 0) && (event.getLoadResult().getData().size() > 0)) {
//                            // FIXME: Need call to get all types in a graph.
//                            // NodeListDTO dto = event.getLoadResult().getData().get(0);
//                            // for (GraphTypesDTO graphTypeDTO : dto.getTypes()) {
//                            // typeFilterStore.add(graphTypeDTO.getType());
//                            // }
//                        }
                        if (graph.getDataview() instanceof DirectedPresenter) {
                            toolBar.setEnabled(true);
                        } else {
                            dataViewPresenter = (DataViewPresenter) graph.getDataview();
                            VizPanel vizPanel = (VizPanel) graph.getChrome();
                            worksheet = vizPanel.getWorksheet();
                            String parentWorksheetName = worksheet.getName();
                            String activeWorksheetName = dataViewPresenter.getActiveWorksheet().getName();
                            if (parentWorksheetName.equals(activeWorksheetName)) {
                                toolBar.setEnabled(true);
                            } else {
                                DataViewWidget dataViewWidget = (DataViewWidget) dataViewPresenter.getView();
                                if(handlerRegistration != null) {
                                    handlerRegistration.removeHandler();
                                }
                                handlerRegistration = dataViewWidget.addSelectionHandler(new SelectionHandler() {

                                    @Override
                                    public void onSelection(SelectionEvent event) {
                                        WorksheetTabPanel source = (WorksheetTabPanel) event.getSource();
                                        WindowLayout selectedItem = (WindowLayout) event.getSelectedItem();
                                        String parentWorksheetName = worksheet.getName();
                                        String activeWorksheetName = selectedItem.getName();
                                        if (parentWorksheetName.equals(activeWorksheetName)) {
                                            toolBar.setEnabled(true);
                                            toolBar.refresh();
                                            handlerRegistration.removeHandler();
                                        }
                                    }

                                });
                            }
                        }
                    }
                });

        final GroupingView<NodeListDTO> view = new GroupingView<>();
        view.setShowGroupedColumn(false);

        ColumnConfig<NodeListDTO, String> oldTypeCol = (ColumnConfig<NodeListDTO, String>) getTypeColumn(CentrifugeConstantsLocator.get().nodesTabTypeColumn());
        view.groupBy(oldTypeCol == null ? typeCol : oldTypeCol);
        typeCol.setGroupable(true);
        view.setAutoFill(true);

        resultsGrid.setView(view);
//        resultsGrid.getView().setAutoExpandColumn(nameCol);
        resultsGrid.getView().setStripeRows(true);
        resultsGrid.getView().setColumnLines(true);
        resultsGrid.setBorders(false);
        resultsGrid.setColumnReordering(true);
        resultsGrid.setLoadMask(true);
        resultsGrid.setAllowTextSelection(true);

        sm.getColumn().setGroupable(false);
        sm.getColumn().setHideable(false);

        if(itemsToShowListBox.getValue() != null) {
            toolBar = gridComponentManager.getPagingToolbar(Integer.parseInt(itemsToShowListBox.getValue()));
        }else{
            itemsToShowListBox.setSelectedIndex(1);
            toolBar = gridComponentManager.getPagingToolbar(Integer.parseInt(itemsToShowListBox.getValue()));
        }
        toolBar.setEnabled(true);

        com.google.gwt.user.client.ui.Label pageSizeLabel = new com.google.gwt.user.client.ui.Label(i18n.nodesTabPageSizeLabel()); //$NON-NLS-1$
        pageSizeLabel.getElement().getStyle().setPadding(2, Unit.PX);
        toolBar.insert(pageSizeLabel, toolBar.getWidgetCount() - 1);

        gridContainer.setPager(toolBar);
        gridContainer.setGrid(resultsGrid);

        itemsToShowListBox.getElement().getStyle().setFontSize(11, Unit.PX);
        itemsToShowListBox.setVisible(true);
        toolBar.insert(itemsToShowListBox, toolBar.getWidgetCount() - 1);
        //i think this should be okay.
        actionsButton.getTriggerWidget().setEnabled(false);

        cm.addColumnHiddenChangeHandler(colHide -> saveColumnModel(resultsGrid.getColumnModel()));
        cm.addColumnMoveHandler(colMove ->  saveColumnModel(resultsGrid.getColumnModel()));
    }

    @UiHandler("hideNavLink")
    void onHideNodes(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            nodeIds.add(nodeListDTO.getID());
        }
        future.execute(GraphActionServiceProtocol.class).hideNodeById(graph.getUuid(), nodeIds);
        graph.getGraphSurface().refresh(future);
    }

    @UiHandler("itemsToShowListBox")
    void onItemsToShowChange(SelectionEvent<String> event) {
        int pageSize = 50;
        try {
            pageSize = Integer.parseInt(event.getSelectedItem());
        } catch (NumberFormatException e) {
        }
        toolBar.setPageSize(pageSize);
        toolBar.bind(gridComponentManager.getLoader());
        refresh();
    }

    @UiHandler("removeSelectNavLink")
    void onRemoveSelectNodes(ClickEvent event) {
        VortexFuture<SelectionModel> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).getSelectionModel(GraphActionsService.DEFAULT_SELECTION,
                    graph.getUuid());
        } catch (CentrifugeException e) {
        }
        future.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                VortexFuture<Void> future2 = WebMain.injector.getVortex().createFuture();
                GraphRequest request = new GraphRequest();
                ArrayList<Integer> nodeIds = Lists.newArrayList();
                List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
                for (NodeListDTO nodeListDTO : selectedItems) {
                    nodeIds.add(nodeListDTO.getID());
                }
                result.nodes.removeAll(nodeIds);
                request.nodes.addAll(result.nodes);
                try {
                    future2.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), false, true, false,
                            request);
                } catch (CentrifugeException e) {
                }
                graph.getGraphSurface().refresh(future2);
            }
        });
    }

    @UiHandler("searchTextBox")
    void onSearchKeyEvent(KeyDownEvent e) {
        if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER)
            refresh();
    }

    //
    // @UiHandler("searchTextBox")
    // void onSearchChange(ValueChangeEvent<String> e) {
    // refresh();
    // }

    @UiHandler("selectNavLink")
    void onSelectNodes(ClickEvent event) {
        VortexFuture<SelectionModel> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            nodeIds.add(nodeListDTO.getID());
        }
        GraphRequest request = new GraphRequest();
        request.nodes.addAll(nodeIds);
        try {
            future.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), true, true, true, request);
        } catch (CentrifugeException e) {
        }
        graph.getGraphSurface().refresh(future);
        future.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                graph.fireEvent(GraphEvents.SELECTION_CHANGED);
            }
        });
    }

    @UiHandler("showNavLink")
    void onShowNodes(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            nodeIds.add(nodeListDTO.getID());
        }
        future.execute(GraphActionServiceProtocol.class).unhideNodeById(graph.getUuid(), nodeIds);
        graph.getGraphSurface().refresh(future);
    }

    @UiHandler("unbundleNavLink")
    void onUnbundleNodes(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (NodeListDTO nodeListDTO : selectedItems) {
            nodeIds.add(nodeListDTO.getID());
        }
        try {
            future.execute(GraphActionServiceProtocol.class).unbundleNodesById(graph.getUuid(), nodeIds);
        } catch (CentrifugeException e) {
        }
        graph.getGraphSurface().refresh(future);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {

                graph.getLegend().load();
                graph.getGraphSurface().getToolTipManager().removeAllToolTips();
            }
        });
        graph.refreshTabs(future);
    }

    @UiHandler("zoomNavLink")
    void onZoomNodes(ClickEvent event) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        VortexFuture<Void> createFuture = WebMain.injector.getVortex().createFuture();
        List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }
        //FIXME: This logic belongs on the sever. Need new endpoint.
        if (selectedItems.size() == 1) {
            NodeListDTO node = selectedItems.get(0);
            createFuture.execute(GraphActionServiceProtocol.class).fitToRegion(graph.getUuid(),
                    (int) (getNodeXorBundleX(node) - 40 * node.getSize() * 4), (int) (getNodeYorBundleY(node) - 40 * node.getSize() * 4),
                    (int) (40 * node.getSize() * 8), (int) (40 * node.getSize() * 8));
        } else {
            for (NodeListDTO nodeListDTO : selectedItems) {
                double x = getNodeXorBundleX(nodeListDTO);
                if (minX > x) {
                    minX = (int) x;
                }
                if (maxX < x) {
                    maxX = (int) x;
                }
                double y = getNodeYorBundleY(nodeListDTO);
                if (minY > y) {
                    minY = (int) y;
                }
                if (maxY < y) {
                    maxY = (int) y;
                }
            }
            createFuture.execute(GraphActionServiceProtocol.class).fitToRegion(graph.getUuid(), minX, minY,
                    maxX - minX, maxY - minY);
        }
        graph.getGraphSurface().refresh(createFuture);
    }

    private double getNodeYorBundleY(NodeListDTO nodeListDTO) {
        if (nodeListDTO.isBundled()) {
            return nodeListDTO.getBundleY();
        }
        return nodeListDTO.getY();
    }

    private double getNodeXorBundleX(NodeListDTO nodeListDTO) {
        if (nodeListDTO.isBundled()) {
            return nodeListDTO.getBundleX();
        }
        return nodeListDTO.getX();
    }

    public Grid<NodeListDTO> getResultsGrid() {
        return resultsGrid;
    }

    interface MyUiBinder extends UiBinder<GraphTab, NodesTabImpl> {
    }
    
    protected class StringModelKeyProvider implements ModelKeyProvider<String>{
        @Override
        public String getKey(String item) {
            return item.toString();
        }
    }


    private List<String> getVisibleColumns(){
        List<String> visibleColumns = new ArrayList<String>();
        List<ColumnConfig<NodeListDTO, ?>> columns = resultsGrid.getColumnModel().getColumns();
        for(ColumnConfig<NodeListDTO, ?> a : columns ){
            if(!a.isHidden()){
                if(!a.getPath().isEmpty()) {
                    visibleColumns.add(a.getPath());
                }
            }
        }
        return visibleColumns;
    }


    @UiHandler("exportNodesList")
    void onExportNodesList(ClickEvent event){
//        Info.display("Exporting Nodes..", "Download of CSV file with contents of the list will begin shortly...");
        FilterPagingLoadConfig lastLoadConfig = gridComponentManager.getLoader().getLastLoadConfig();
        List<FilterConfig> filters = lastLoadConfig.getFilters();
        lastLoadConfig.setFilters(getAllFilters(filters));
        List<NodeListDTO> selItems = resultsGrid.getSelectionModel().getSelectedItems();

        List<String> visible = getVisibleColumns();

        if(selItems.isEmpty()){
            WebMain.injector.getVortex().execute(createDownloadCallback(ExportType.CSV), GraphActionServiceProtocol.class).exportNodeList(graph.getUuid(), lastLoadConfig, visible);
        }else{
            WebMain.injector.getVortex().execute(createDownloadCallback(ExportType.CSV), ExportActionsServiceProtocol.class).exportNodesList(selItems, visible);
        }
    }

    private List<FilterConfig> getAllFilters(List<FilterConfig> filters){
        FilterConfigBean filterConfigBean = new FilterConfigBean();
        filterConfigBean.setField("label"); //$NON-NLS-1$
        filterConfigBean.setType("string"); //$NON-NLS-1$
        filterConfigBean.setValue(searchTextBox.getValue());
        filters.add(filterConfigBean);
        Optional<FilterConfigBean> selectedFilter = getSelectedFilter();
        if (selectedFilter.isPresent()) {
            filters.add(selectedFilter.get());
        }
        Optional<FilterConfigBean> bundledFilter = getBundledFilter();
        if (bundledFilter.isPresent()) {
            filters.add(bundledFilter.get());
        }
        Optional<FilterConfigBean> hiddenFilter = getHiddenFilter();
        if (hiddenFilter.isPresent()) {
            filters.add(hiddenFilter.get());
        }
        Optional<FilterConfigBean> plunkedFilter = getPlunkedFilter();
        if (plunkedFilter.isPresent()) {
            filters.add(plunkedFilter.get());
        }
        Optional<FilterConfigBean> unselectedFilter = getUnselectedFilter();
        if (unselectedFilter.isPresent()) {
            filters.add(unselectedFilter.get());
        }
        Optional<FilterConfigBean> unbundledFilter = getUnbundledFilter();
        if (unbundledFilter.isPresent()) {
            filters.add(unbundledFilter.get());
        }
        Optional<FilterConfigBean> visibleFilter = getVisibleFilter();
        if (visibleFilter.isPresent()) {
            filters.add(visibleFilter.get());
        }
        Optional<FilterConfigBean> unplunkedFilter = getUnplunkedFilter();
        if (unplunkedFilter.isPresent()) {
            filters.add(unplunkedFilter.get());
        }
        Optional<FilterConfigBean> unannotationFilter = getUnannotationFilter();
        if (unannotationFilter.isPresent()) {
            filters.add(unannotationFilter.get());
        }
        Optional<FilterConfigBean> annotationFilter = getAnnotationFilter();
        if (annotationFilter.isPresent()) {
            filters.add(annotationFilter.get());
        }
        Optional<FilterConfigBean> isBundleFilter = getIsBundleFilter();
        if (isBundleFilter.isPresent()) {
            filters.add(isBundleFilter.get());
        }
        return filters;
    }

    private Optional<FilterConfigBean> getIsBundleFilter() {
        return getFilter(NodeListFieldNames.IS_BUNDLE, isBundleState);
    }


    private Callback<String> createDownloadCallback(final ExportType exportType) {
        return fileToken -> DownloadHelper.download(graph.getName() + "_Nodes",
                exportType.getFileSuffix(),
                fileToken);
    }
}