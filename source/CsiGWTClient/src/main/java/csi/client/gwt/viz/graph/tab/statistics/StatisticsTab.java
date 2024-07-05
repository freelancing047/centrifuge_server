package csi.client.gwt.viz.graph.tab.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.business.visualization.graph.stat.GraphStatistic;
import csi.server.business.visualization.graph.stat.GraphStatisticalPopulation;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.common.service.api.GraphStatisticsServiceProtocol;

public class StatisticsTab {

    @UiField
    FluidRow controlLayer;
    @UiField(provided = true)
    GridContainer gridContainer;
    @UiField
    Button refreshButton;
    @UiField
    InlineLabel lastLoadedLabel;
    private Graph graph;
    private GraphTab tab;
    private ArrayList<GraphStatisticalPopulation> populations;
    private ArrayList<GraphStatistic> statistics;
    private HashBasedTable<GraphStatisticalPopulation, GraphStatistic, Double> table;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    interface MyUiBinder extends UiBinder<GraphTab, StatisticsTab> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public StatisticsTab(Graph graph) {
        gridContainer = new GridContainer();
        tab = uiBinder.createAndBindUi(this);
        this.graph = graph;
        controlLayer.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

    public void initialize() {
        VortexFuture<List<String>> future = WebMain.injector.getVortex().createFuture();
        future.execute(GraphActionServiceProtocol.class).getNodeTypes(graph.getUuid());
        future.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
            @Override
            public void onSuccess(final List<String> nodeResult) {
                populations = Lists.newArrayList();
                populations.add(new GraphStatisticalPopulation.NodePopulation());
                for (String s : nodeResult) {
                    populations.add(new GraphStatisticalPopulation.TypePopulation(s));
                }

                VortexFuture<List<String>> future = WebMain.injector.getVortex().createFuture();
                future.execute(GraphActionServiceProtocol.class).getLinkTypes(graph.getUuid());
                future.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
                    @Override
                    public void onSuccess(List<String> linkResult) {
                        populations.add(new GraphStatisticalPopulation.LinkPopulation());
                        for (String s : linkResult) {
                            populations.add(new GraphStatisticalPopulation.LinkTypePopulation(s));
                        }
                        statistics = Lists.newArrayList();
                        statistics.add(new GraphStatistic.VisibleGraphStatistic());
                        statistics.add(new GraphStatistic.TotalGraphStatistic());
                        WebMain.injector.getVortex().execute(new Callback<Table<GraphStatisticalPopulation, GraphStatistic, Double>>() {

                            @Override
                            public void onSuccess(Table<GraphStatisticalPopulation, GraphStatistic, Double> result) {
                                if (result instanceof HashBasedTable) {
                                    HashBasedTable<GraphStatisticalPopulation, GraphStatistic, Double> hashBasedTable = (HashBasedTable<GraphStatisticalPopulation, GraphStatistic, Double>) result;
                                    loadGridStats(hashBasedTable);
                                }

                            }
                        }, GraphStatisticsServiceProtocol.class).getGraphStatistics(statistics, populations, graph.getUuid());
                    }
                });
            }
        });


    }

    private void loadGridStats(HashBasedTable<GraphStatisticalPopulation, GraphStatistic, Double> table) {
        this.table = table;

        List<ColumnConfig<GraphStatisticalPopulation, ?>> columnConfigs = new ArrayList<ColumnConfig<GraphStatisticalPopulation, ?>>();
        ColumnConfig<GraphStatisticalPopulation, String> title = new ColumnConfig<>(new PopulationLabelProvider(), 200, _constants.statisticsTab_header_name());

        title.setComparator(String::compareTo);
        columnConfigs.add(title);

        SortedMap<String, GraphStatistic> sortedNameStaticMap = new TreeMap<String, GraphStatistic>();

        for (GraphStatistic statistic : table.columnKeySet()) {
        	sortedNameStaticMap.put(statistic.getName(), statistic);
        }

        for (String name : sortedNameStaticMap.keySet()) {
            StatisticalValueProvider valueProvider = new StatisticalValueProvider(sortedNameStaticMap.get(name));
            ColumnConfig<GraphStatisticalPopulation, Double> columnConfig = new ColumnConfig<GraphStatisticalPopulation, Double>(valueProvider, 100, name);
            columnConfigs.add(columnConfig);
        }

        ColumnModel<GraphStatisticalPopulation> columnModel = new ColumnModel<GraphStatisticalPopulation>(columnConfigs);
        ListStore<GraphStatisticalPopulation> store = new ListStore<GraphStatisticalPopulation>(new StatisticalModelKeyProvider());
        
        List<GraphStatisticalPopulation> sortedList = new ArrayList<GraphStatisticalPopulation>();
        for (GraphStatisticalPopulation graphStatisticalPopulation : table.rowKeySet()) {
            sortedList.add(graphStatisticalPopulation);
        }
        
        Collections.sort(sortedList, new Comparator<GraphStatisticalPopulation>(){

			@Override
			public int compare(GraphStatisticalPopulation o1, GraphStatisticalPopulation o2) {
				String label1 = o1.getLabel();
				String label2 = o2.getLabel();
				
				return label1.compareTo(label2);
				
			}});
        
        store.addAll(sortedList);
        ResizeableGrid<GraphStatisticalPopulation> grid = new ResizeableGrid<GraphStatisticalPopulation>(store, columnModel);
        gridContainer.setGrid(grid);
        
        //TODO: Externalize for i18n
        lastLoadedLabel.setText(CentrifugeConstantsLocator.get().statisticsTab_lastUpdated() +": "+ new Date().toString());
    }

    private class StatisticalValueProvider implements ValueProvider<GraphStatisticalPopulation, Double> {
        private final GraphStatistic statistic;

        public StatisticalValueProvider(GraphStatistic statistic) {

            this.statistic = statistic;
        }

        @Override
        public Double getValue(GraphStatisticalPopulation object) {
            return table.get(object, statistic);
        }

        @Override
        public void setValue(GraphStatisticalPopulation object, Double value) {

        }

        @Override
        public String getPath() {
            return "";
        }
    }

    public Tab getTab() {
        return tab;
    }


    private class StatisticalModelKeyProvider implements ModelKeyProvider<GraphStatisticalPopulation> {
        @Override
        public String getKey(GraphStatisticalPopulation item) {
            return item.getUUID();
        }
    }

    private class PopulationLabelProvider implements ValueProvider<GraphStatisticalPopulation, String> {
        @Override
        public String getValue(GraphStatisticalPopulation object) {
            return object.getLabel();
        }

        @Override
        public void setValue(GraphStatisticalPopulation object, String value) {

        }

        @Override
        public String getPath() {
            return "";
        }
    }

    @UiHandler("refreshButton")
    void onRefreshClick(ClickEvent e) {
        initialize();
    }

}
