package csi.client.gwt.viz.graph.tab.link;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.viz.graph.tab.SessionPersistentColumnList;
import csi.client.gwt.viz.graph.tab.node.Filters;
import csi.client.gwt.viz.graph.tab.node.LabelControl;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.combo_boxes.CommonNumberComboBox;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.server.business.service.GraphActionsService;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.common.dto.graph.gwt.EdgeListDTO.EdgeListFieldNames;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class LinksTabImpl extends SessionPersistentColumnList {

    private static final String LABEL_STYLE = "filter-label-close-button";//NON-NLS
    private static final String LABEL_VISIBLE_STYLE = "filter-label-close-button-visible";//NON-NLS
    private static final EdgeListDTOProperties linksProps = GWT.create(EdgeListDTOProperties.class);
    private static final String FALSE_STRING = "false";//NON-NLS
    private static final String TRUE_STRING = "true";//NON-NLS
    private static final String BOOLEAN_STRING = "boolean";//NON-NLS
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    NavLink addSelectNavLink;
    @UiField(provided = true)
    GridContainer gridContainer;
    @UiField
    FluidRow controlLayer;
    @UiField
    NavLink hideNavLink;
    @UiField
    CommonNumberComboBox itemsToShowListBox;
    @UiField
    Tab linksTab;
    @UiField
    Button removeButton;
    @UiField
    NavLink removeSelectNavLink;
    @UiField
    TextBox searchTextBox;
    @UiField
    NavLink selectNavLink;
    @UiField
    NavLink showNavLink;
    @UiField(provided = true)
    LayoutPanel layoutPanel;
    Column tableColumn;
    @UiField
    NavLink selectedFilter;
    @UiField
    NavLink unselectedFilter;
    @UiField
    NavLink plunkedFilter;
    @UiField
    NavLink unplunkedFilter;
    @UiField
    NavLink hiddenFilter;
    @UiField
    NavLink visibleFilter;
    @UiField
    NavLink annotationFilter;
    @UiField
    NavLink unannotationFilter;
    @UiField
    NavLink exportLinksList;

    //filters
    int unselectedState = 0;
    int unplunkedState = 0;
    int visibleState = 0;
    int hiddenState = 0;
    int plunkedState = 0;
    int selectedState = 0;
    int annotationState = 0;
    int unannotationState = 0;
    //Where tags for filters appear
    @UiField
    Controls tagCloud;
    @UiField
    Dropdown filterCombo;
    //Icon for filters
    @UiField
    com.github.gwtbootstrap.client.ui.InputAddOn filterIcon;
    @UiField
    DropdownButton actionButton;
    private RemoteLoadingGridComponentManager<EdgeListDTO> gridComponentManager;
    private Graph graph;
    private Grid<EdgeListDTO> resultsGrid;
    private GraphTab tab;
    private PagingToolBar toolBar;

    public LinksTabImpl(final Graph graph) {
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
        layoutPanel.setWidth("100%");
        layoutPanel.setHeight("100%");

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
            }else{
                tagCloud.setVisible(false);
                filterCombo.setVisible(true);
                filterIcon.setVisible(true);
            }
        }
    }

    public void refresh() {
        toolBar.refresh();
        showHideFilters();
    }

    // this should be a function that takes two arguments and whatever else for each filter, they are legit dupes.
    // the only thing is that those ints laying around randomlty in this file are really annoyuing
    protected Optional<FilterConfigBean> getFilter(String field, int currentState) {
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

    protected Optional<FilterConfigBean> getAnnotationFilter() {
        return getFilter(EdgeListFieldNames.FIELD_ANNOTATION, annotationState);
    }

    protected Optional<FilterConfigBean> getUnannotationFilter() {
        return getFilter(EdgeListFieldNames.FIELD_ANNOTATION, unannotationState);
    }

    protected Optional<FilterConfigBean> getUnplunkedFilter() {
        return getFilter(EdgeListFieldNames.FIELD_PLUNKED, unplunkedState);
    }

    protected Optional<FilterConfigBean> getVisibleFilter() {
        return getFilter(EdgeListFieldNames.FIELD_HIDDEN, visibleState);
    }

    protected Optional<FilterConfigBean> getUnselectedFilter() {
        return getFilter(EdgeListFieldNames.FIELD_SELECTED, unselectedState);
    }

    protected Optional<FilterConfigBean> getPlunkedFilter() {
        return getFilter(EdgeListFieldNames.FIELD_PLUNKED, plunkedState);
    }

    protected Optional<FilterConfigBean> getHiddenFilter() {
        return getFilter(EdgeListFieldNames.FIELD_HIDDEN, hiddenState);
    }

    protected Optional<FilterConfigBean> getSelectedFilter() {
        return getFilter(EdgeListFieldNames.FIELD_SELECTED, selectedState);
    }

    public Tab getTab() {
        return tab;
    }


    @SuppressWarnings("unchecked")
    private void onGraphLoad() {
        ValueProvider<EdgeListDTO, String> typeValProvider = new ValueProvider<EdgeListDTO, String>() {
            @Override
            public String getValue(EdgeListDTO edgeListDTO) {
                if (edgeListDTO.getAllTypesAsString() != null) {
                    return edgeListDTO.getAllTypesAsString();
                } else {
                    return "";
                }
            }

            @Override
            public void setValue(EdgeListDTO edgeListDTO, String s) {
                //
            }

            @Override
            public String getPath() {
                return "types";
            }
        };

        List<ColumnConfig<EdgeListDTO, ?>> columnList = new ArrayList<>();

        IdentityValueProvider<EdgeListDTO> identity = new IdentityValueProvider<>();
        final CsiCheckboxSelectionModel<EdgeListDTO> sm = new CsiCheckboxSelectionModel<>(identity);

        sm.addSelectionChangedHandler(selectionChangedEvent -> {
            if (resultsGrid.getSelectionModel().getSelectedItems().size()==0) {
                actionButton.getTriggerWidget().setEnabled(false);
            }else{
                actionButton.getTriggerWidget().setEnabled(true);
            }
        });

        // source
        ColumnConfig<EdgeListDTO, String> sourceCol = new ColumnConfig<>(linksProps.source(), 200, CentrifugeConstantsLocator.get().linkTabColumn_source());
        // target
        ColumnConfig<EdgeListDTO, String> targetCol = new ColumnConfig<>(linksProps.target(), 200, CentrifugeConstantsLocator.get().linkTabColumn_target());
        // type
        ColumnConfig<EdgeListDTO, String> typeCol = new ColumnConfig<>(typeValProvider, 200, CentrifugeConstantsLocator.get().linkTabColumn_type());
        // label
        ColumnConfig<EdgeListDTO, String> labelCol = new ColumnConfig<>(linksProps.label(), 200, CentrifugeConstantsLocator.get().linkTabColumn_label());

        // hidden
        ColumnConfig<EdgeListDTO, Boolean> hiddenCol = new ColumnConfig<>(linksProps.hidden(), 200, CentrifugeConstantsLocator.get().linkTabColumn_hidden());
        // selected
        ColumnConfig<EdgeListDTO, Boolean> selectedCol = new ColumnConfig<>(linksProps.selected(), 200, CentrifugeConstantsLocator.get().linkTabColumn_selected());

        ColumnConfig<EdgeListDTO, Double> size = new ColumnConfig<>(linksProps.width(), 200, CentrifugeConstantsLocator.get().linkTabColumn_size());

        ColumnConfig<EdgeListDTO, Boolean> annotationCol = new ColumnConfig<>(linksProps.annotation(), 200, CentrifugeConstantsLocator.get().linkTabColumn_comment());

        ColumnConfig<EdgeListDTO, Double> opacity = new ColumnConfig<>(linksProps.opacity(), 200, CentrifugeConstantsLocator.get().linkTabColumn_opacity());

        columnList.add(sm.getColumn());
        columnList.add(sourceCol);
        columnList.add(targetCol);
        columnList.add(typeCol);
        columnList.add(labelCol);
        columnList.add(hiddenCol);
        columnList.add(selectedCol);
        columnList.add(size);
        columnList.add(annotationCol);
        columnList.add(opacity);

        selectedCol.setHidden(true);
        annotationCol.setHidden(true);
        opacity.setHidden(true);
        size.setHidden(true);
        hiddenCol.setHidden(true);

        selectedCol.setFixed(true);
        annotationCol.setFixed(true);
        hiddenCol.setFixed(true);

        selectedCol.setGroupable(false);
        annotationCol.setGroupable(false);
        hiddenCol.setGroupable(false);


        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        getLoader(componentFactory);

        ColumnModel<EdgeListDTO> cm = getExistingColumnModel() == null ? new ColumnModel<>(columnList) : (ColumnModel<EdgeListDTO>) getExistingColumnModel();

        ListStore<EdgeListDTO> linksStore = gridComponentManager.getStore();

        resultsGrid = new ResizeableGrid<>(linksStore, cm);

        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<EdgeListDTO>> loader = gridComponentManager.getLoader();

        loader.addSortInfo(new SortInfoBean(EdgeListDTO.EdgeListFieldNames.FIELD_SOURCE, SortDir.ASC));
        resultsGrid.setLoader(loader);
        resultsGrid.setSelectionModel(sm);

        StringFilter<EdgeListDTO> sourceFilter = new StringFilter<>(linksProps.source());
        StringFilter<EdgeListDTO> targetFilter = new StringFilter<>(linksProps.target());
        StringFilter<EdgeListDTO> labelFilter = new StringFilter<>(linksProps.label());
        StringFilter<EdgeListDTO> typeFilter = new StringFilter<>(typeValProvider);
        GridFilters<EdgeListDTO> filters = gridComponentManager.getGridFilters();
        filters.initPlugin(resultsGrid);
        filters.setLocal(false);
        filters.addFilter(sourceFilter);
        filters.addFilter(targetFilter);
        filters.addFilter(labelFilter);
        filters.addFilter(typeFilter);
        // filters.addFilter(hiddenFilter);

        gridComponentManager.getLoader().addLoadHandler(event -> toolBar.setEnabled(true));

        final GroupingView<EdgeListDTO> view = new GroupingView<>();
        view.setShowGroupedColumn(false);
        ColumnConfig<EdgeListDTO, String> oldTypeCol = (ColumnConfig<EdgeListDTO, String>) getTypeColumn(CentrifugeConstantsLocator.get().linkTabColumn_type());
        view.groupBy(oldTypeCol == null ? typeCol : oldTypeCol);

        resultsGrid.setView(view);
//        resultsGrid.getView().setAutoExpandColumn(sourceCol);
        resultsGrid.getView().setStripeRows(true);
        resultsGrid.getView().setColumnLines(true);
        resultsGrid.setBorders(false);
        resultsGrid.setColumnReordering(true);
        resultsGrid.setLoadMask(true);
        // Toolbar customization

        itemsToShowListBox.addStyleOnOver(itemsToShowListBox.getElement(), "cursor:pointer;");
        if (itemsToShowListBox.getValue() != null) {
            toolBar = gridComponentManager.getPagingToolbar(Integer.parseInt(itemsToShowListBox.getValue()));
        } else {
            toolBar = gridComponentManager.getPagingToolbar(50);
        }
        gridContainer.setPager(toolBar);
        gridContainer.setGrid(resultsGrid);
        toolBar.setEnabled(true);
        Label pageSizeLabel = new Label(CentrifugeConstantsLocator.get().pageSize());
        pageSizeLabel.getElement().getStyle().setPadding(2, Unit.PX);
        toolBar.insert(pageSizeLabel, toolBar.getWidgetCount() - 1);
        itemsToShowListBox.getElement().getStyle().setFontSize(11, Unit.PX);
        itemsToShowListBox.setVisible(true);
        toolBar.insert(itemsToShowListBox, toolBar.getWidgetCount() - 1);
        toolBar.refresh();

        actionButton.getTriggerWidget().setEnabled(false);


        cm.addColumnHiddenChangeHandler(colHide -> saveColumnModel(resultsGrid.getColumnModel()));
        cm.addColumnMoveHandler(colMove -> saveColumnModel(resultsGrid.getColumnModel()));


    }

    private void getLoader(GridComponentFactory componentFactory) {
        gridComponentManager = componentFactory.createRemoteLoading(linksProps.key(), GraphActionServiceProtocol.class,
                (vortexService, loadConfig) -> {
                    List<FilterConfig> filters = loadConfig.getFilters();
                    FilterConfigBean filterConfigBean = new FilterConfigBean();
                    filterConfigBean.setField("source_or_target");
                    filterConfigBean.setType("string");
                    filterConfigBean.setValue(searchTextBox.getValue());
                    filters.add(filterConfigBean);
                    Optional<FilterConfigBean> selectedFilter = getSelectedFilter();
                    if (selectedFilter.isPresent()) {
                        filters.add(selectedFilter.get());
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
                    return vortexService.getPageableEdgeListing(graph.getUuid(), loadConfig);
                });
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
        } else {
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
        } else {
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
        } else {
            handleDelete(Filters.VISIBLE, true);
        }
    }

    @UiHandler("plunkedFilter")
    void onPlunkedFilter(ClickEvent event) {
        if (plunkedState == 0) {
            plunkedState = 1;
            tagCloud.add(createLabel(Filters.PLUNKED));
            refresh();
            plunkedFilter.setBaseIcon(IconType.CHECK);
        } else {
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
        } else {
            handleDelete(Filters.UNPLUNKED, true);
        }
    }

    @UiHandler("annotationFilter")
    void onAnnotationFilter(ClickEvent event) {

        if (annotationState == 0) {
            annotationState = 1;
            tagCloud.add(createLabel(Filters.COMMENTED));
            refresh();
            annotationFilter.setBaseIcon(IconType.CHECK);
        } else {
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
        } else {
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
        annotationState = 0;
        unannotationState = 0;

        for (Filters filter : Filters.values()) {

            handleDelete(filter, false);

        }
        refresh();

        showHideFilters();
    }

    private void handleDelete(Filters filter, boolean refresh) {
        int tags = tagCloud.getWidgetCount();

        for (int ii = tags - 1; ii >= 0; ii--) {
            Widget widget = tagCloud.getWidget(ii);
            if ((widget != null) && (widget instanceof LabelControl)) {
                LabelControl labelControl = (LabelControl) widget;
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
            case BUNDLED:
                break;
            case UNBUNDLED:
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

    private LabelControl createLabel(Filters filterType) {

        final LabelControl control = new LabelControl(filterType);
        control.getElement().getStyle().setDisplay(Display.INLINE);
        Label label = new Label();

        label.setType(LabelType.INFO);
        label.addStyleName("filter-label");//NON-NLS
        String labelText = "";
        switch (filterType) {
            case SELECTED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_selected();
                break;
            case UNSELECTED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_unselected();
                break;
            case HIDDEN:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_hidden();
                break;
            case VISIBLE:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_visible();
                break;
            case PLUNKED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_plunked();
                break;
            case UNPLUNKED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_unplunked();
                break;
            case BUNDLED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_bundled();
                break;
            case UNBUNDLED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_unbundled();
                break;
            case COMMENTED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_commented();
                break;
            case UNCOMMENTED:
                labelText = CentrifugeConstantsLocator.get().linkTabFilterLabel_uncommented();
                break;
        }
        label.setText(labelText);

        final Anchor closeLabel = new Anchor("x");//NON-NLS
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
            }
        };
    }

    @UiHandler("hideNavLink")
    void onHideLinks(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> linkIds = Lists.newArrayList();
        List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (EdgeListDTO edgeListDTO : selectedItems) {
            linkIds.add(edgeListDTO.getID());
        }
        future.execute(GraphActionServiceProtocol.class).hideLinkById(graph.getUuid(), linkIds);
        graph.getGraphSurface().refresh(future);
        graph.refreshTabs(future);
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
        toolBar.refresh();
    }

    @UiHandler("searchTextBox")
    void onSearchChange(ValueChangeEvent<String> e) {
        toolBar.refresh();
    }

    @UiHandler("selectNavLink")
    void onSelectLinks(ClickEvent event) {
        VortexFuture<SelectionModel> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> linkIds = Lists.newArrayList();
        List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (EdgeListDTO edgeListDTO : selectedItems) {
            linkIds.add(edgeListDTO.getID());
        }
        GraphRequest request = new GraphRequest();
        request.links.addAll(linkIds);
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
    void onShowLinks(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> linkIds = Lists.newArrayList();
        List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (EdgeListDTO edgeListDTO : selectedItems) {
            linkIds.add(edgeListDTO.getID());
        }
        future.execute(GraphActionServiceProtocol.class).unhideLinkById(graph.getUuid(), linkIds);
        graph.getGraphSurface().refresh(future);
    }

    @UiHandler("addSelectNavLink")
    void onAddSelectLinks(ClickEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> edgeIds = Lists.newArrayList();
        List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (EdgeListDTO edgeListDTO : selectedItems) {
            edgeIds.add(edgeListDTO.getID());
        }
        GraphRequest request = new GraphRequest();
        request.links.addAll(edgeIds);
        try {
            future.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), false, false, false, request);
        } catch (CentrifugeException e) {
        }
        graph.getGraphSurface().refresh(future);
    }

    @UiHandler("removeSelectNavLink")
    void onRemoveSelectLinks(ClickEvent event) {
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
                ArrayList<Integer> edgeIds = Lists.newArrayList();
                List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
                for (EdgeListDTO edgeListDTO : selectedItems) {
                    edgeIds.add(edgeListDTO.getID());
                }
                result.links.removeAll(edgeIds);
                request.links.addAll(result.links);
                try {
                    future2.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), false, false, true,
                            request);
                } catch (CentrifugeException e) {
                }
                graph.getGraphSurface().refresh(future2);
            }
        });
    }

    @UiHandler("removeButton")
    public void onClick(ClickEvent event) {
        searchTextBox.setValue("");
        toolBar.refresh();
    }

    interface MyUiBinder extends UiBinder<GraphTab, LinksTabImpl> {
    }


    private List<String> getVisibleColumns() {
        List<String> visibleColumns = new ArrayList<String>();
        List<ColumnConfig<EdgeListDTO, ?>> columns = resultsGrid.getColumnModel().getColumns();
        for (ColumnConfig<EdgeListDTO, ?> a : columns) {
            if (!a.isHidden()) {
                // if path is empty, we don't care.
                if (!a.getPath().isEmpty()) {
                    visibleColumns.add(a.getPath());
                }
            }
        }
        return visibleColumns;
    }

    @UiHandler("exportLinksList")
    void onExportLinks(ClickEvent event) {
        FilterPagingLoadConfig config = gridComponentManager.getLoader().getLastLoadConfig();

        config.setFilters(getFilters(config.getFilters()));

        List<EdgeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();

        List<String> visibleCols = getVisibleColumns();

        if (selectedItems.isEmpty()) {
            WebMain.injector.getVortex().execute(createDownloadCallback(ExportType.CSV), GraphActionServiceProtocol.class).exportLinkList(graph.getUuid(), config, visibleCols);
        } else {
            WebMain.injector.getVortex().execute(createDownloadCallback(ExportType.CSV), ExportActionsServiceProtocol.class).exportLinksList(selectedItems, visibleCols);
        }


    }

    private Callback<String> createDownloadCallback(final ExportType exportType) {
        return fileToken -> DownloadHelper.download(graph.getName() + "_Links",
                exportType.getFileSuffix(),
                fileToken);
    }

    private List<FilterConfig> getFilters(List<FilterConfig> filters) {
        FilterConfigBean filterConfigBean = new FilterConfigBean();
        filterConfigBean.setField("source_or_target");
        filterConfigBean.setType("string");
        filterConfigBean.setValue(searchTextBox.getValue());
        filters.add(filterConfigBean);

        Optional<FilterConfigBean> selectedFilter = getSelectedFilter();
        if (selectedFilter.isPresent()) {
            filters.add(selectedFilter.get());
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

        return filters;
    }


}
