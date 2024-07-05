package csi.client.gwt.mapper.menus;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.grids.SelectionGrid;

public class AutoMapMenu<T1 extends SelectionDataAccess<?>, T2 extends SelectionDataAccess<?>> extends Menu {

    protected final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    protected SelectionGrid<T1> _leftGrid = null;
    protected SelectionGrid<T2> _rightGrid = null;
    protected MappingSupport<T1, T2> _mappingSupport;
    protected AutoMapCallbackHandler<T1, T2> _handler;

    public AutoMapMenu(SelectionGrid<T1> leftGridIn, SelectionGrid<T2> rightGridIn, MappingSupport<T1, T2> mappingSupportIn, AutoMapCallbackHandler<T1, T2> handlerIn) {

        super();

        _leftGrid = leftGridIn;
        _rightGrid = rightGridIn;
        _mappingSupport = mappingSupportIn;
        _handler = handlerIn;

        final MenuItem byExactNameItem = new MenuItem();
        final MenuItem byCaselessNameItem = new MenuItem();
        final MenuItem byTypeItem = new MenuItem();
        final MenuItem exactMatchItem = new MenuItem();
        final MenuItem nearMatchItem = new MenuItem();
        final MenuItem byPositionItem = new MenuItem();
        final MenuItem byRelativePositionItem = new MenuItem();

        byExactNameItem.setText(i18n.mapperMenu_AutoMap_ByExactNameItem()); //$NON-NLS-1$
        byExactNameItem.addSelectionHandler(byExactNameHandler);
        add(byExactNameItem);

        byCaselessNameItem.setText(i18n.mapperMenu_AutoMap_ByCaselessNameItem()); //$NON-NLS-1$
        byCaselessNameItem.addSelectionHandler(byCaselessNameHandler);
        add(byCaselessNameItem);

        byTypeItem.setText(i18n.mapperMenu_AutoMap_ByTypeItem()); //$NON-NLS-1$
        byTypeItem.addSelectionHandler(byTypeHandler);
        add(byTypeItem);

        exactMatchItem.setText(i18n.mapperMenu_AutoMap_ByExactMatchItem()); //$NON-NLS-1$
        exactMatchItem.addSelectionHandler(exactMatchHandler);
        add(exactMatchItem);

        nearMatchItem.setText(i18n.mapperMenu_AutoMap_ByNearMatchItem()); //$NON-NLS-1$
        nearMatchItem.addSelectionHandler(nearMatchHandler);
        add(nearMatchItem);

        byPositionItem.setText(i18n.mapperMenu_AutoMap_ByPositionItem()); //$NON-NLS-1$
        byPositionItem.addSelectionHandler(byPositionHandler);
        add(byPositionItem);

        byRelativePositionItem.setText(i18n.mapperMenu_AutoMap_ByRelativePositionItem()); //$NON-NLS-1$
        byRelativePositionItem.addSelectionHandler(byRelativePositionHandler);
        add(byRelativePositionItem);
    }

    public MappingSupport<T1, T2> getSupport() {

        return _mappingSupport;
    }

    private SelectionHandler byExactNameHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByExactName(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler byCaselessNameHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByCaselessName(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler byTypeHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByType(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler exactMatchHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByExactMatch(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler nearMatchHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByNearMatch(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler byPositionHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByPosition(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };

    private SelectionHandler byRelativePositionHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByRelativePosition(_leftGrid.getStore(), _rightGrid.getStore(),
                    getSelectedIndex(_leftGrid), getSelectedIndex(_rightGrid)));
        }
    };

    private int getSelectedIndex(SelectionGrid<? extends SelectionDataAccess<?>> gridIn) {

        int myIndex = 0;
        SelectionDataAccess<?> mySelection = gridIn.getSelectionModel().getSelectedItem();

        if (null != mySelection) {

            ListStore<? extends SelectionDataAccess<?>> myList = gridIn.getStore();
            String myKey = mySelection.getKey();

            if ((null != myList) && (null != myKey)) {

                for (int i = 0; myList.size() > i; i++) {

                    SelectionDataAccess<?> myItem = myList.get(i);

                    if (myKey.equals(myItem.getKey())) {

                        myIndex = i;
                        break;
                    }
                }
            }
        }
        return myIndex;
    }
}
