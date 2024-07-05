package csi.client.gwt.viz.graph.link.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.dnd.core.client.ListViewDragSource;
import com.sencha.gxt.dnd.core.client.ListViewDropTarget;
import com.sencha.gxt.widget.core.client.ListView;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.NodeProxyFactory;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.LinkDirectionComboBox;
import csi.server.common.dto.FieldConstraints;
import csi.server.common.dto.FilterConstraintsRequest;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.gwt.viz.graph.LinkDirection;

public class LinkDirectionTab {

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public class DirectionValue {

        private String key;
        private String name;

        public DirectionValue(String key, String name) {
            this.key = key;
            this.name = name;

        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }

    }

    interface SpecificUiBinder extends UiBinder<Tab, LinkDirectionTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    interface DirectionValueProperties extends PropertyAccess<DirectionValue> {

        @Path("key")
        ModelKeyProvider<DirectionValue> key();

        @Path("name")
        ValueProvider<DirectionValue, String> name();

    }

    private Tab tab;

    private LinkSettings linkSettings;

    private LinkSettingsPresenter presenter;

    @UiField
    Column column1;
    @UiField
    Column column2;
    @UiField
    Column column3;
    @UiField
    Column c2;
    @UiField
    Column c3;
    @UiField
    FieldDefComboBox valueFDCB;

    private ListView<DirectionValue, String> list1;

    private ListView<DirectionValue, String> list2;

    private ListView<DirectionValue, String> list3;

    public Tab getTab() {
        return tab;
    }

    public LinkDirectionTab(final LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
        tab = uiBinder.createAndBindUi(this);

        DirectionValueProperties props = GWT.create(DirectionValueProperties.class);
        ListStore<DirectionValue> store = new ListStore<DirectionValue>(props.key());
        store.addSortInfo(new StoreSortInfo<LinkDirectionTab.DirectionValue>(props.name(), SortDir.ASC));
        list1 = new ListView<LinkDirectionTab.DirectionValue, String>(store, props.name());

        store = new ListStore<DirectionValue>(props.key());
        store.addSortInfo(new StoreSortInfo<LinkDirectionTab.DirectionValue>(props.name(), SortDir.ASC));
        list2 = new ListView<LinkDirectionTab.DirectionValue, String>(store, props.name());

        store = new ListStore<DirectionValue>(props.key());
        store.addSortInfo(new StoreSortInfo<LinkDirectionTab.DirectionValue>(props.name(), SortDir.ASC));
        list3 = new ListView<LinkDirectionTab.DirectionValue, String>(store, props.name());

        new ListViewDragSource<DirectionValue>(list1);
        new ListViewDragSource<DirectionValue>(list2);
        new ListViewDragSource<DirectionValue>(list3);

        new ListViewDropTarget<DirectionValue>(list1);
        new ListViewDropTarget<DirectionValue>(list2);
        new ListViewDropTarget<DirectionValue>(list3);

        list1.setHeight(150);
        list2.setHeight(185);
        list3.setHeight(185);

        column1.add(list1);
        column2.add(list2);
        column3.add(list3);

        if(linkSettings.getGraphSettings().getCurrentTheme() != null){
            updateWithTheme(linkSettings, linkSettings.getGraphSettings().getCurrentTheme());
        } else {
        linkSettings.getGraphSettings().getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

            @Override
            public void onSuccess(GraphTheme result) {
                updateWithTheme(linkSettings, result);
            }

            
        });
        }
        
        
        List<FieldDef> fieldsOfType = FieldDefUtils.getSortedNonStaticFields(linkSettings.getGraphSettings().getDataViewDef()
                .getModelDef(), SortOrder.ALPHABETIC);
        for (FieldDef fieldDef : fieldsOfType) {
            if (fieldDef.getValueType().equals(CsiDataType.String)) {
                valueFDCB.getStore().add(fieldDef);
            }
        }

        valueFDCB.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                populateListValues(linkSettings, event.getSelectedItem());
            }

        });
        filter = new StoreFilter<LinkDirectionTab.DirectionValue>() {

            @Override
            public boolean select(Store<DirectionValue> store, DirectionValue parent, DirectionValue item) {

                return item.getName().toLowerCase().contains(filterString.toLowerCase());
            }
        };
        list1.getStore().addFilter(filter);

        modeListBox.addSelectionHandler(new SelectionHandler<LinkDirection>() {

            @Override
            public void onSelection(SelectionEvent<LinkDirection> event) {
                presenter.setDirectionMode(event.getSelectedItem());
            }
        });
    }
    public void updateWithTheme(final LinkSettings linkSettings, GraphTheme result) {
        NodeProxyFactory nodeProxyFactory = linkSettings.getGraphSettings().getNodeProxyFactory();
        LinkProxy _linkProxy = linkSettings.getModel().linkProxy;
        NodeProxy node1 = nodeProxyFactory.create(_linkProxy.getNode1());
        NodeProxy node2 = nodeProxyFactory.create(_linkProxy.getNode2());
        final Image image = node1.getRenderedIcon(result);
        final Image image2 = node2.getRenderedIcon(result);
        final Image image3 = node2.getRenderedIcon(result);
        final Image image4 = node1.getRenderedIcon(result);

        c2.add(image);
        Image arrowImage1 = new Image("img/rightArrowLarge.png");
        arrowImage1.setWidth("60px");
        arrowImage1.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
        c2.add(arrowImage1);
        c2.add(image2);
        c2.add(new Label(_linkProxy.getNode1().getName() + " " + _constants.linkDirection_to() + " " + _linkProxy.getNode2().getName()));
        c3.add(image3);
        Image arrowImage2 = new Image("img/rightArrowLarge.png");
        arrowImage2.setWidth("60px");
        arrowImage2.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
        c3.add(arrowImage2);
        c3.add(image4);
        c3.add(new Label(_linkProxy.getNode2().getName() + " " + _constants.linkDirection_to() + " " + _linkProxy.getNode1().getName()));
    }
    @UiField
    TextBox searchTextBox;

    protected String filterString;

    private StoreFilter<LinkDirectionTab.DirectionValue> filter;

    public void bind(LinkSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("searchTextBox")
    public void onSearch(KeyUpEvent e) {
        filterString = searchTextBox.getValue();
        list1.getStore().setEnableFilters(false);
        list1.getStore().setEnableFilters(true);

    }

    public void setDirection(LinkDirectionDef direction) {
        LinkDirection ld = direction.getLinkDirection();
        modeListBox.setDirectionDef(direction);
        modeListBox.redraw();
        modeListBox.setValue(ld, true);
        if (ld.equals(LinkDirection.DYNAMIC)) {
            valueFDCB.setValue(direction.getFieldDef(), true, true);
            valueFDCB.setVisible(true);
            column2.setVisible(true);
            column3.setVisible(true);
            searchTextBox.setVisible(true);
            list1.setVisible(true);
            forwardValues = direction.getForwardValues();
            reverseValues = direction.getReverseValues();
            // populate list1 asynchronusly
            populateListValues(linkSettings, direction.getFieldDef());
        } else {
            list1.setVisible(false);
            searchTextBox.setVisible(false);
            valueFDCB.setVisible(false);
            column2.setVisible(false);
            column3.setVisible(false);
        }

        //        modeListBox.getStore().clear();
        //        modeListBox.getStore().add(0, _constants.linkDirection_undirected());
        //        modeListBox.getStore().add(1, direction.getNode1Name() + " " + _constants.linkDirection_to() + " " + direction.getNode2Name());
        //        modeListBox.getStore().add(2, direction.getNode2Name() + " " + _constants.linkDirection_to() + " " + direction.getNode1Name());
        //        modeListBox.getStore().add(3, _constants.dynamic());
    }

    @UiField
    LinkDirectionComboBox modeListBox;

    private ArrayList<String> forwardValues;

    private ArrayList<String> reverseValues;

    private void showError() {
        ErrorDialog errorDialog = new ErrorDialog(_constants.linkDirection_invalidSelectionTitle(),
                _constants.linkDirection_invalidSelectionMessage());
        errorDialog.show();
    }

    public ArrayList<String> getForwardValues() {
        List<DirectionValue> all = list2.getStore().getAll();
        ArrayList<String> out = Lists.newArrayList();
        for (DirectionValue directionValue : all) {
            out.add(directionValue.getName());
        }
        return out;
    }

    public ArrayList<String> getReverseValues() {
        List<DirectionValue> all = list3.getStore().getAll();
        ArrayList<String> out = Lists.newArrayList();
        for (DirectionValue directionValue : all) {
            out.add(directionValue.getName());
        }
        return out;
    }

    public LinkSettingsPresenter getPresenter() {
        return presenter;
    }

    private void populateListValues(final LinkSettings linkSettings, final FieldDef selectedItem) {
        if (mapofValuesInField.containsKey(selectedItem)) {
            List<String> list = mapofValuesInField.get(selectedItem);
            if (list.size() == 0) {
                showError();
            }
            setStores(list);
            return;
        }

        VortexFuture<List<FieldConstraints>> createFuture = WebMain.injector.getVortex().createFuture();
        FilterConstraintsRequest request = new FilterConstraintsRequest();
        request.dvUuid = linkSettings.getGraphSettings().getDataViewUuid();
        request.vizUuid = linkSettings.getGraphSettings().getUuid();
        request.caseSensitive = false;
        request.limit = 50;
        try {
            createFuture.execute(VisualizationActionsServiceProtocol.class).getFilterConstraints(request,selectedItem);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        createFuture.addEventHandler(new AbstractVortexEventHandler<List<FieldConstraints>>() {

            @Override
            public void onSuccess(List<FieldConstraints> result) {
                if(result.isEmpty()){
                    return;
                }
                FieldConstraints fieldConstraints = result.get(0);
                mapofValuesInField.put(selectedItem, fieldConstraints.availableValues);
                if (fieldConstraints.availableValues.size() == 0) {
                    showError();
                }
                List<String> availableValues = fieldConstraints.availableValues;
                setStores(availableValues);
                getPresenter().setDirectionField(selectedItem);
            }
        });
    }

    private Map<FieldDef, List<String>> mapofValuesInField = Maps.newHashMap();

    private void setStores(List<String> availableValues) {

        list1.getStore().clear();
        list2.getStore().clear();
        list3.getStore().clear();

        for (String s : availableValues) {
            if (forwardValues.contains(s) || reverseValues.contains(s)) {
                continue;
            }
            list1.getStore().add(new DirectionValue(s, s));
        }
        for (String string : forwardValues) {
            DirectionValue item = new DirectionValue(string, string);
            list2.getStore().add(item);
        }

        for (String string : reverseValues) {
            DirectionValue item = new DirectionValue(string, string);
            list3.getStore().add(item);
        }
    }

}
