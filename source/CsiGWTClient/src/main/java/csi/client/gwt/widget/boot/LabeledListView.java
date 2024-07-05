package csi.client.gwt.widget.boot;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by centrifuge on 10/31/2017.
 */
public abstract class LabeledListView<T extends ExtendedDisplayInfo> extends CsiLayoutPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected CsiListView<T> listView;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<String, T> _itemMap;
    protected boolean _clearFlags = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Abstract Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void labelSelectionList(String labelIn);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public LabeledListView() {

        this(false);
    }

    public LabeledListView(boolean clearFlagsIn) {

        super();
        listView = new CsiListView<T>();
        _itemMap = new LinkedHashMap<String, T>();
        _clearFlags = clearFlagsIn;
    }

    public CsiListView<T> getListView() {

        return listView;
    }

    public void forceList(int indexIn) {

    }

    public List<T> removeAllActiveItems() {
        List<T> myList = new ArrayList<T>();
        Collection<T> values = Lists.newArrayList(_itemMap.values());
        for (T myItem : values) {
            if (!myItem.isDisabled()) {
                myList.add(myItem);
                listView.remove(myItem);
                _itemMap.remove(myItem.getDisplayString());
            }
        }
        return myList;
    }

    public List<T> removeSelectedItems() {

        List<T> myList = getSelectedItems();

        if ((null != myList) && (0 < myList.size())) {

            for (T myItem : myList) {

                listView.remove(myItem);
                _itemMap.remove(myItem.getDisplayString());
            }
        }
        return (null != myList) ? myList : new ArrayList<T>();
    }

    public void addAllItems(Collection<T> listIn) {

        if ((null != listIn) && (0 < listIn.size())) {

            for (T myItem : listIn) {

                addItem(myItem);
            }
        }
    }

    public void addItem(T itemIn) {

        if (null != itemIn) {

            if ((!_clearFlags) || (!itemIn.isError())) {

                String myKey = itemIn.getDisplayString();

                if (_clearFlags && itemIn.isSpecial()) {

                    itemIn.clearSpecial();
                }
                if (_itemMap.containsKey(myKey)) {

                    listView.remove(_itemMap.get(myKey));
                    _itemMap.remove(myKey);
                }
                _itemMap.put(myKey, itemIn);
                listView.add(itemIn);
            }
        }
    }

    public void removeItem(String itemIn) {

        removeItem(_itemMap.get(itemIn));
    }

    public void removeItem(T itemIn) {

        if ((null != itemIn) && (_itemMap.containsKey(itemIn.getDisplayString()))) {

            _itemMap.remove(itemIn.getDisplayString());
            listView.remove(itemIn);
        }
    }

    public void setEmptyValue(T itemIn) {

        if (null != listView) {

            listView.setEmptyValue(itemIn);
        }
    }

    public void enableAll() {

        for (T myItem : _itemMap.values()) {

            myItem.enable();
        }
    }

    public void disableAll() {

        for (T myItem : _itemMap.values()) {

            myItem.disable();
        }
    }

    public void enableItem(String itemIn) {

        if (null != itemIn) {

            T myItem = _itemMap.get(itemIn);

            if (null != myItem) {

                myItem.enable();
            }
        }
    }

    public void disableItem(String itemIn) {

        if (null != itemIn) {

            T myItem = _itemMap.get(itemIn);

            if (null != myItem) {

                myItem.disable();
            }
        }
    }

    public List<T> getSelectedItems() {

        return listView.getSelectionModel().getSelectedItems();
    }

    public void sortAscending() {

        listView.sortAscending();
    }

    public void sortDescending() {

        listView.sortDescending();
    }

    public void noSorting() {

        listView.noSorting();
    }

    public long size() {

        return listView.size();
    }

    public boolean isEmpty() {

        return listView.getSelectionModel().getSelectedItems().isEmpty();
    }

    public void refresh() {

        listView.refresh();
    }

    public List<T> getAllItems(int listIdIn) {

        if (0 == listIdIn) {

            return getAllItems();
        }
        return null;
    }

    public List<T>  getAllItems() {

        return new ArrayList<T>(_itemMap.values());
    }

    public List<Collection<T>> getAllItemCollections() {

        List<Collection<T>> myList = new ArrayList<Collection<T>>();

        myList.add(_itemMap.values());

        return myList;
    }

    public List<List<T>> getAllItemLists() {

        List<List<T>> myList = new ArrayList<List<T>>();

        myList.add(new ArrayList<T>(_itemMap.values()));

        return myList;
    }

    public void clearAll() {

        _itemMap.clear();
        listView.clear();
    }

    public HandlerRegistration addSelectionChangedHandler(SelectionChangedHandler<T> handlerIn) {

        return listView.getSelectionModel().addSelectionChangedHandler(handlerIn);
    }

    public boolean loadDataSet(List<Collection<T>> collectionIn) {

        clearAll();
        if (null != collectionIn) {

            addAllItems(collectionIn.get(0));
        }
        return true;
    }

    public boolean loadData(Collection<T> collectionIn) {

        clearAll();
        if (null != collectionIn) {

            addAllItems(collectionIn);
        }
        return true;
    }
}
