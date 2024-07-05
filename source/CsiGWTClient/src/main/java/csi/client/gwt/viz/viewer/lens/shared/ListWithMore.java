package csi.client.gwt.viz.viewer.lens.shared;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.theme.gray.client.toolbar.GrayPagingToolBarAppearance;
import com.sencha.gxt.theme.gray.client.toolbar.GrayToolBarAppearance;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import csi.client.gwt.WebMain;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.table.CellHoverEvent;
import csi.client.gwt.viz.table.TableView;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.LoadCallback;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.business.visualization.viewer.dto.ViewerGridHeader;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.service.api.ViewerActionServiceProtocol;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;

public class ListWithMore extends Composite implements Pinnable {

    private final FluidContainer widget;
    private final List<String> things;
    private Objective objective;
    private String lensDef;
    private String type;

    public ListWithMore(List<String> things, Objective objective, String lensDef, String type, int itemLimit, Map<String, Integer> counts, Map<String, Integer> occurs, int total, String title) {
        this.things = things;
        this.objective = objective;
        this.lensDef = lensDef;
        this.type = type;
        widget = new FluidContainer();
        populateList(this.things, itemLimit, counts, occurs, total, title);
        initWidget(widget);
        if (this.things.size() > 5) {
//            createMoreButton();
        }

    }

    void populateList(List<String> things, int itemLimit, Map<String, Integer> counts, Map<String, Integer> occurs, int total, final String title) {
        {
            final FluidRow row = new FluidRow();
            Column c1 = new Column(8);
            Column c2 = new Column(2);
            Column c3 = new Column(2);
            c1.getElement().getStyle().setProperty("minHeight", "0");
            c2.getElement().getStyle().setProperty("minHeight", "0");
            c3.getElement().getStyle().setProperty("minHeight", "0");
            row.add(c1);
            row.add(c2);
            row.add(c3);
            InlineLabel occur = new InlineLabel("Occur");
            c3.add(occur);
            InlineLabel count = new InlineLabel("Count");
            c2.add(count);
            InlineLabel label = new InlineLabel("Value");
            c1.add(label);
            occur.getElement().getStyle().setColor("#999");
            count.getElement().getStyle().setColor("#999");
            label.getElement().getStyle().setColor("#999");
            c2.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
            c3.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
//            label.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
//            count.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
//            occur.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
            row.getElement().getStyle().setMarginBottom(-4, Style.Unit.PX);
            row.getElement().getStyle().setMarginTop(-4, Style.Unit.PX);
            widget.add(row);
        }
        for (int i = 0; i < things.size() && i < itemLimit; i++) {
            String s = things.get(i);
            final FluidRow row = new FluidRow();
            Column c1 = new Column(8);
            Column c2 = new Column(2);
            Column c3 = new Column(2);
            c1.getElement().getStyle().setProperty("minHeight", "0");
            c2.getElement().getStyle().setProperty("minHeight", "0");
            c3.getElement().getStyle().setProperty("minHeight", "0");
            row.add(c1);
            row.add(c2);
            row.add(c3);
            int integer = counts.get(s);
            String countString;
            if(integer>10e12){
                integer = IntMath.divide(integer , (int) 10e12, RoundingMode.DOWN);
                countString = integer + "T+";
            }else if(integer>10e9){
                integer = IntMath.divide(integer , (int) 10e9, RoundingMode.DOWN);
                countString = integer + "G+";
            }else if(integer>10e6){
                integer = IntMath.divide(integer , (int) 10e6, RoundingMode.DOWN);
                countString = integer+"M+";
            }else if(integer>10e3){
                integer = IntMath.divide(integer , (int) 10e3, RoundingMode.DOWN);
                countString = integer+"k+";
            }else{
                countString = integer+"";
            }
            InlineLabel w = new InlineLabel(countString);
                c2.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
                w.getElement().getStyle().setColor("#999");
                c2.add(w);
                InlineLabel w1 = new InlineLabel(occurs.get(s) + "");
                c3.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
                w1.getElement().getStyle().setColor("#999");
                c3.add(w1);
            final Style rowStyle = row.getElement().getStyle();
            row.addBitlessDomHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    row.getElement().getStyle().setBackgroundColor("#F6F6F6");
                    w.getElement().getStyle().setColor("#333");
                    w1.getElement().getStyle().setColor("#333");
                }
            }, MouseOverEvent.getType());
            row.addBitlessDomHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    row.getElement().getStyle().setBackgroundColor("#FFF");
                    w.getElement().getStyle().setColor("#999");
                    w1.getElement().getStyle().setColor("#999");

                }
            }, MouseOutEvent.getType());
            rowStyle.setPaddingLeft(4, Style.Unit.PX);
            rowStyle.setMarginLeft(-4, Style.Unit.PX);
            rowStyle.setPaddingTop(3, Style.Unit.PX);
            rowStyle.setLineHeight(14, Style.Unit.PX);
            rowStyle.setProperty("transition", "background-color .15s ease-out");
            rowStyle.setProperty("transitionDelay", ".05s");
            final InlineLabel inlineLabel = new InlineLabel(s);
            inlineLabel.setTitle(s);


            Style style = inlineLabel.getElement().getStyle();
            style.setTextOverflow(Style.TextOverflow.ELLIPSIS);
            style.setOverflow(Style.Overflow.HIDDEN);
            style.setDisplay(Style.Display.INLINE_BLOCK);
            style.setWhiteSpace(Style.WhiteSpace.NOWRAP);
            c1.add(inlineLabel);

            widget.add(row);
            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
                    if (inlineLabel.getOffsetWidth() > c1.getOffsetWidth()) {
                        final Button b = new Button();
                        b.setType(ButtonType.LINK);
                        b.setIcon(IconType.REMOVE);
                        b.getElement().getStyle().setPosition(Style.Position.RELATIVE);
                        b.getElement().getStyle().setDisplay(Style.Display.INLINE);
                        b.getElement().getStyle().setRight(0, Style.Unit.PX);


                        final TextArea ta = new TextArea();
                        ta.getElement().getStyle().setBackgroundColor("rgba(0,0,0,0)");
                        ta.getElement().getStyle().setCursor(Style.Cursor.TEXT);
                        ta.setWidth("85%");
                        ta.getElement().getStyle().setColor("#333");
                        ta.getElement().getStyle().setProperty("border", "none");
                        ta.getElement().getStyle().setProperty("borderTop", "1px solid #DDD");
                        ta.getElement().getStyle().setProperty("borderBottom", "1px solid #DDD");
                        ta.getElement().getStyle().setProperty("boxShadow", "none");
                        ta.setTitle(s);
                        ta.setVisibleLines(4);
                        ta.setValue(s);
                        ta.setReadOnly(true);
                        ta.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                if (!event.getNativeEvent().getShiftKey()) {
                                    event.stopPropagation();
                                }
                            }
                        });

                        row.addBitlessDomHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                if (!c1.getElement().getStyle().getDisplay().equals("none")) {
                                    c1.getElement().getStyle().setDisplay(Style.Display.NONE);
                                    c2.getElement().getStyle().setDisplay(Style.Display.NONE);
                                    c3.getElement().getStyle().setDisplay(Style.Display.NONE);
                                    row.add(ta);
                                    row.add(b);
                                } else {
                                    ta.removeFromParent();
                                    b.removeFromParent();
                                    c1.add(inlineLabel);
                                    c1.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                                    c2.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                                    c3.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                                    row.getElement().getStyle().setBackgroundColor("#FFF");
                                    w.getElement().getStyle().setColor("#999");
                                    w1.getElement().getStyle().setColor("#999");

                                }
                                event.stopPropagation();

                            }
                        }, ClickEvent.getType());

                    }
                    style.setProperty("maxWidth", "90%");

                }
            });
        }
        Row moreRow = new Row();
        moreRow.getElement().getStyle().setPaddingLeft(16, Style.Unit.PX);
        Button moreButton = new Button("All "+ total +" Values");
        moreButton.setType(ButtonType.LINK);
        moreRow.add(moreButton);
        moreButton.addClickHandler(new ClickHandler() {

            private FluidContainer w;
            MoreGrid moreGrid;

            @Override
            public void onClick(ClickEvent event) {

                VortexFuture<ViewerGridConfig> future1 = WebMain.injector.getVortex().createFuture();
                future1.addEventHandler(new AbstractVortexEventHandler<ViewerGridConfig>() {


                    @Override
                    public void onSuccess(ViewerGridConfig result) {
                        moreGrid = new MoreGrid(result, objective, type, lensDef);
                        w.add(moreGrid);

                    }
                });
                //FIXME: TOKEN needs to be dynamic
                String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();
                future1.execute(ViewerActionServiceProtocol.class).getGridConfig(objective, lensDef, dvuuid);
                Dialog modal = new Dialog();
                Button  export = modal.getActionButton();
                Button close = modal.getCancelButton();
                close.setText("Close");
                close.setType(ButtonType.DEFAULT);
                export.setText("Export");
                modal.hideOnAction();
                modal.hideOnCancel();
                modal.hideTitleCloseButton();
                export.setIcon(IconType.DOWNLOAD_ALT);
                export.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        FilterPagingLoadConfig loadConfig = moreGrid.getGridComponentManager().getLoader().getLastLoadConfig();
                            String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();
                            WebMain.injector.getVortex().execute(createDownloadCallback(ExportType.CSV), ViewerActionServiceProtocol.class).exportMoreGrid(objective, lensDef , loadConfig, dvuuid);
                        }

                    private Callback<String> createDownloadCallback(final ExportType exportType) {
                        return fileToken -> DownloadHelper.download("download",
                                exportType.getFileSuffix(),
                                fileToken);
                    }
                });
                modal.setCloseVisible(true);
                w = new FluidContainer();
                w.getElement().getStyle().setPaddingLeft(0, Style.Unit.PX);
                w.getElement().getStyle().setPaddingRight(0, Style.Unit.PX);
                modal.setHeight("388px");
                modal.setWidth("562px");
                modal.add(w);
//                modal.add(export);
                modal.setTitle(title);
                modal.show();
            }
                private Grid getMoreGrid() {
                    return moreGrid.getTableGrid();
                }

        });
        widget.add(moreRow);

    }

    /*private void createMoreButton() {
        Button moreButton = new Button("more...");
        moreButton.setType(ButtonType.LINK);


        moreButton.addClickHandler(new ClickHandler() {

            private FluidContainer w;

            @Override
            public void onClick(ClickEvent event) {

                VortexFuture<ViewerGridConfig> future1 = WebMain.injector.getVortex().createFuture();
                future1.addEventHandler(new AbstractVortexEventHandler<ViewerGridConfig>() {
                    @Override
                    public void onSuccess(ViewerGridConfig result) {
                        MoreGrid moreGrid = new MoreGrid(result, objective, type, null);
                        w.add(moreGrid);

                    }
                });
                //FIXME: TOKEN needs to be dynamic
                future1.execute(ViewerActionServiceProtocol.class).getGridConfig(objective, null, type);
                CsiModal modal = new CsiModal();
                modal.setCloseVisible(true);
                w = new FluidContainer();
                modal.add(w);
                modal.setTitle("Hello World");
                modal.show();
            }
        });
        FluidRow row = new FluidRow();
        row.add(moreButton);
        widget.add(row);
    }*/

    static class MoreGrid extends Composite {

        private ViewerGridConfig gridConfig;

        public RemoteLoadingGridComponentManager<List<?>> getGridComponentManager() {
            return gridComponentManager;
        }

        private RemoteLoadingGridComponentManager<List<?>> gridComponentManager;
        private List<FieldDef> visibleFields = Lists.newArrayList();

        public ResizeableGrid<List<?>> getTableGrid() {
            return tableGrid;
        }

        private ResizeableGrid<List<?>> tableGrid;
        private HashMap<String, FieldDef> columnFields;
        private PagingToolBar toolBar;
        private GridContainer gridContainer;
        private Objective objective;
        private String token;
        private String lensDef;

        MoreGrid(ViewerGridConfig result, Objective o, String token, String lensDef) {
            objective = o;
            this.token = token;
            this.lensDef = lensDef;
            gridContainer = new GridContainer();
            gridContainer.setHeight("280px");
            gridContainer.setWidth("540px");
            this.gridConfig = result;
            initWidget(gridContainer);
            initialize(5);

        }

        private void initSelectionInfo() {
            //TODO:??????????
            IntPrimitiveSelection selInfo = new IntPrimitiveSelection();
            HashSet<Integer> ids = new HashSet<Integer>();
            if (!selInfo.isCleared()) {
                ids.addAll(selInfo.getSelectedItems());
            }
        }

        private void initToolBar() {

            if (toolBar == null) {
                PagingToolBar.PagingToolBarAppearance appearance = new GrayPagingToolBarAppearance();
                ToolBar.ToolBarAppearance toolBarAppearance = new GrayToolBarAppearance();
                toolBar = new PagingToolBar(toolBarAppearance, appearance, DEFAULT_PAGE_SIZE);
                /*toolBar.addPageHandler(new PageEventHandler(){

                    @Override
                    public void onPage(PageEvent event) {

                        int page = event.getActivePage();

                        if(tableGrid.getView() == null){
                            return;
                        }
                        gridComponentManager.getLoader().load();

                    }}, PageEvent.type);*/
            }
            toolBar.setActivePage(1);
            gridContainer.setPager(toolBar);

        }

        public void initialize(long totalRecords) {
            initFields();

            initGrid();

            PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> myLoader = gridComponentManager.getLoader();
//TODO:????
            //            myLoader.addLoadExceptionHandler(handleLoadException);

            List<ColumnConfig<List<?>, ?>> colList = initColumns();
            initMoreGrid(colList);

            initSelectionInfo();
            initToolBar();

            gridContainer.setGrid(tableGrid);


            myLoader.load();

            tableGrid.addHandler(new CellHoverEvent.CellHoverHandler() {
                @Override
                public void onCellHover(CellHoverEvent event) {
                    Element cell = tableGrid.getView().getCell(event.getRowIndex(), event.getCellIndex());
                    if (TextMetrics.get().getWidth(cell.getInnerText()) > cell.getClientWidth()) {
                        String msg = "";
                        //force a line break in the tooltip if /n is in the data
                        msg = cell.getInnerText().replace("\n", "<br/>");
                        tableGrid.setToolTip(msg);
                    } else {
                        tableGrid.hideToolTip();
                    }
                }
            }, CellHoverEvent.getType());


        }


        //TODO: fixed?
        private void initFields() {
            List<ViewerGridHeader> headers = gridConfig.getHeaders();
            visibleFields = new ArrayList<FieldDef>();
            for (ViewerGridHeader vfld : headers) {
                if (vfld.isVisible()) {
                    visibleFields.add(vfld.getFieldDef());
                }
            }
        }


        private List<ColumnConfig<List<?>, ?>> initColumns() {
            List<ColumnConfig<List<?>, ?>> colList = new ArrayList<ColumnConfig<List<?>, ?>>();
            int idx = 1;
            // don't show SelectionModel in context menu in column header

            List<ColumnConfig<List<?>, ?>> columnConfigs = Lists.newArrayList();
            for (FieldDef def : visibleFields) {

                int width = 100;


                GridValueProvider<Object> valueProvider = new GridValueProvider<Object>(def);
                valueProvider.setPosition(idx++);


                ColumnConfig<List<?>, Object> col = new ColumnConfig<List<?>, Object>(
                        valueProvider, width, def.getFieldName());
                col.setResizable(true);

                if (CsiDataType.Integer.equals(def.getValueType()) || CsiDataType.Number.equals(def.getValueType())) {
                    col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
                }
                columnConfigs.add(col);
            }

            return columnConfigs;
        }

        private void initMoreGrid(List<ColumnConfig<List<?>, ?>> colList) {

            ListStore<List<?>> rowStore = gridComponentManager.getStore();
            ColumnModel<List<?>> cm = new ColumnModel<List<?>>(colList);
            for (ColumnConfig<List<?>, ?> column : cm.getColumns()) {
                column.setHideable(false);
            }
            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
            tableGrid.getView().setAutoExpandColumn(cm.getColumn(0));

                }
            });
            tableGrid = new ResizeableGrid<List<?>>(rowStore, cm);
            tableGrid.setSelectionModel(null);
            setTableGridSettings();

        }

        private void setTableGridSettings() {
            tableGrid.setLazyRowRender(0);
            tableGrid.setId("table-grid");
            tableGrid.setBorders(false);
            tableGrid.setColumnReordering(true);
            tableGrid.setColumnResize(true);
            tableGrid.setLoadMask(false);


            //view
            tableGrid.getView().setStripeRows(true);
            tableGrid.getView().setColumnLines(true);
            tableGrid.getView().setAdjustForHScroll(true);
            tableGrid.getView().setTrackMouseOver(true);

            PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> loader = gridComponentManager.getLoader();
            loader.addSortInfo(new SortInfoBean("count", SortDir.DESC));
            loader.addSortInfo(new SortInfoBean("occur", SortDir.DESC));
            loader.addSortInfo(new SortInfoBean("value", SortDir.ASC));
            tableGrid.setLoader(loader  );
        }

        private void initGrid() {
            GridComponentFactory componentFactory = WebMain.injector.getGridFactory();

            gridComponentManager = componentFactory.createRemoteLoading(new GenericModelKeyProvider(),
                    ViewerActionServiceProtocol.class,
                    new LoadCallback<ViewerActionServiceProtocol, List<?>>() {


                        @Override
                        public PagingLoadResult<List<?>> onLoadCallback(
                                ViewerActionServiceProtocol vortexService, FilterPagingLoadConfig loadConfig) {
                            try {
                                String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();
                                return vortexService.getGridData(objective, lensDef , token, loadConfig, dvuuid);
                            } catch (Exception myException) {

                                displayLoadingError(myException);
                            }
                            return null;
                        }
                    }
            );


        }

        private void displayLoadingError(Exception myException) {
//TODO:?
        }

        private static class GenericModelKeyProvider implements ModelKeyProvider<List<?>> {

            @Override
            public String getKey(List<?> item) {
//                return new CsiUUID().toString();
                return item.get(1).toString();

            }
        }


    }

    private static class GridValueProvider<T> implements ValueProvider<List<?>, T> {
        private final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss a";
        private Function resolveType;
        private int position = -1;
        private String defUuid;

        GridValueProvider(FieldDef def) {
            defUuid = def.getUuid();

            CsiDataType valueType = def.getValueType();

            if (CsiDataType.DateTime.equals(valueType)) {
                resolveType = ((Function<String, String>) s -> TableView.getDateTimeFormatString(s, DATE_TIME_FORMAT));
            }
/*
            if (CsiDataType.Integer.equals(valueType)) {
                resolveType = (new Function<String, Double>() {

                    @Override
                    public Double apply(String s) {
                        Double value = null;
                        if (s != null) {
                            try {
                                value = new Double(s);
                            } catch (Exception e) {
                            }
                        }
                        return value;
                    }
                });
            }

            if (CsiDataType.Number.equals(valueType)) {
                resolveType = (new Function<String, Double>() {

                    public Double apply(String s) {
                        Double value = null;
                        if (s != null) {
                            try {
                                value = new Double(s);
                            } catch (Exception e) {
                            }
                        }
                        return value;
                    }
                });
            }
*/

            if (resolveType == null) {
                resolveType = (new Function<T, String>() {

                    @Override
                    public String apply(T s) {
                        if (s == null) {
                            return "";
                        }
                        return s.toString();
                    }
                });
            }
        }

        public T getValue(List<?> object) {
            if (position >= 0)
                return (T) resolveType.apply(object.get(position));
            else
                return null;
        }

        public void setValue(List<?> object, T value) {
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getPath() {
            return defUuid;
        }
    }
}
