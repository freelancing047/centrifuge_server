package csi.client.gwt.mapper.grids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.mapper.menus.GridMenu;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.gxt.drag_n_drop.IntegratedGrid;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;

/**
 * Created by centrifuge on 3/25/2016.
 */
public abstract class SelectionGrid<T extends SelectionDataAccess<?>> extends IntegratedGrid<T> implements SortingGrid {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<String, Map<String, T>> _displayList;
    protected Map<String, T> _selectedItems = new HashMap<String, T>();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract Map<String, Map<String, T>> createDisplayMap();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SelectionGrid(ModelBuilder<T> modelBuilderIn, GridMenu<SelectionGrid<T>> gridMenuIn, CsiDropEventHandler handlerIn) {

        super(modelBuilderIn.genStore(), modelBuilderIn.genModel());
        init(gridMenuIn, handlerIn);
    }

    public SelectionGrid<T> initializeGrid() {

        _displayList = createDisplayMap();
        return this;
    }

    public Map<String, T> getSelectedItems() {

        return _selectedItems;
    }

    public Map<String, Map<String, T>> getDisplayList() {

        return _displayList;
    }

    public <S> void loadValues( List<TreeMap<S, T>> mapListIn) {

        store.clear();
        store.clearSortInfo();

        if ((null != mapListIn) && (0 < mapListIn.size())) {

            for (Map<?, T> myMap : mapListIn) {

                if (0 < myMap.size()) {

                    store.addAll(myMap.values());
                }
            }
        }
    }

    public <S> void loadReverseValues( List<TreeMap<S, T>> mapListIn) {

        store.clear();
        store.clearSortInfo();

        if ((null != mapListIn) && (0 < mapListIn.size())) {

            for (Map<?, T> myMap : mapListIn) {

                if (0 < myMap.size()) {

                    List<T> myList1 = new ArrayList<T>();

                    myList1.addAll(myMap.values());
                    for (int i = myList1.size() - 1; 0 <= i; i--) {

                        store.add(myList1.get(i));
                    }
                }
            }
        }
    }

    public void sortByOrdinal() {

        List<TreeMap<Integer, T>> myMapList = new ArrayList<TreeMap<Integer, T>>();

        if ((null != _displayList) && (0 < _displayList.size())) {

            for (Map<String, T> mySource : _displayList.values()) {

                TreeMap<Integer, T> myMap = new TreeMap<Integer, T>();

                for (T myItem : mySource.values()) {

                    if (null == _selectedItems.get(myItem.getKey())) {

                        myMap.put(myItem.getOrdinal(), myItem);
                    }
                }
                if (0 < myMap.size()) {

                    myMapList.add(myMap);
                }
            }
        }
        loadValues(myMapList);
        Display.grid(this, false);
    }

    public void sortAlphaAscending() {

        loadValues(sortAlpha());
        Display.grid(this, false);
    }

    public void sortAlphaDescending() {

        loadReverseValues(sortAlpha());
        Display.grid(this, false);
    }

    public T getDisplayItem(String groupIdIn, String itemIdIn){

        T myResult = null;

        if ((null != groupIdIn) && (null != itemIdIn)) {

            if ((null != _displayList) && (0 < _displayList.size())) {

                Map<String, T> myMap = _displayList.get(groupIdIn);

                if ((null != myMap) && (0 < myMap.size())) {

                    myResult = myMap.get(itemIdIn);
                }
            }
        }
        return myResult;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected List<TreeMap<String, T>> sortAlpha() {

        List<TreeMap<String, T>> myMapList = new ArrayList<TreeMap<String, T>>();

        if ((null != _displayList) && (0 < _displayList.size())) {

            for (Map<String, T> mySource : _displayList.values()) {

                TreeMap<String, T> myMap = new TreeMap<String, T>();

                for (T myItem : mySource.values()) {

                    if (null == _selectedItems.get(myItem.getKey())) {

                        myMap.put(myItem.getItemDisplayName(), myItem);
                    }
                }
                if (0 < myMap.size()) {

                    myMapList.add(myMap);
                }
            }
        }
        return myMapList;
    }

    protected void addToMap(Map<String, TreeMap<Integer, T>> mapIn, List<T> listIn) {

        if ((null != listIn) && (0 < listIn.size())) {

            for (T myItem : listIn) {

                TreeMap<Integer, T> myMap = mapIn.get(myItem.getKey());

                myMap.put(myItem.getOrdinal(), myItem);
            }
        }
    }

    protected void removeFromMap(Map<String, TreeMap<Integer, T>> mapIn, List<T> listIn) {

        if ((null != listIn) && (0 < listIn.size())) {

            for (T myItem : listIn) {

                TreeMap<Integer, T> myMap = mapIn.get(myItem.getKey());
                Integer myKey = myItem.getOrdinal();

                if (myMap.containsKey(myKey)) {

                    myMap.remove(myKey);
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void init(GridMenu<SelectionGrid<T>> gridMenuIn, CsiDropEventHandler handlerIn) {

        final GroupingView<T> myView = new GroupingView<T>(gridMenuIn);

        if (null != gridMenuIn) {

            gridMenuIn.setGrid(this);
        }

        myView.setShowGroupedColumn(false);
        myView.setForceFit(true);
        myView.groupBy(getColumnModel().getColumn(0));

        setView(myView);

        GridHelper.setReorderableRowsDefaults(this);
        getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        addCsiDropEventHandler(handlerIn);
    }

    private String concatNames(Collection<T> collectionIn) {

        StringBuilder myBuffer = new StringBuilder();
        Iterator<T> myIterator = collectionIn.iterator();

        while(myIterator.hasNext()) {

            myBuffer.append(myIterator.next().getName());
            myBuffer.append('|');
        }
        if (0 < myBuffer.length()) {

            myBuffer.setLength(myBuffer.length() - 1);
        }
        return myBuffer.toString();
    }

    private String concatNames(ListStore<T> collectionIn) {

        StringBuilder myBuffer = new StringBuilder();

        for (int i = 0; collectionIn.size() > i; i++) {

            myBuffer.append(collectionIn.get(i).getName());
            myBuffer.append('|');
        }
        if (0 < myBuffer.length()) {

            myBuffer.setLength(myBuffer.length() - 1);
        }
        return myBuffer.toString();
    }

    private void debug(String routineIn) {

        Map<String, T> myMap = ((null != _displayList) && (0 < _displayList.size())) ? _displayList.values().iterator().next() : null;
        T myItem = ((null != myMap) && (0 < myMap.size())) ? myMap.values().iterator().next() : null;
        String myType = (null != myItem) ? myItem.getClass().getName() : "? ? ?";
        Display.debug("(" + myType + ")" + routineIn + ":" + Integer.toString((null != _displayList) ? _displayList.size() : -1));
    }
}
