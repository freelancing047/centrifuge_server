package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.ListView;

import csi.client.gwt.events.HoverEvent;
import csi.client.gwt.events.HoverEventHandler;
import csi.client.gwt.widget.cells.readonly.DisplayListCell;
import csi.server.common.dto.SelectionListData.ExtendedInfo;
import csi.server.common.util.StringUtil;

/*
 * When combined with DisplayListCell, this class supports disabling of individual items
 * as well as focusing on an individual item.
 */
public class CsiListView<T extends ExtendedInfo> extends ListView<T, String> {


    public CsiListView(ListStore dataStoreIn, ValueProvider valueProviderIn, Cell<String> displayListCellReloaded) {
        super(dataStoreIn, valueProviderIn, displayListCellReloaded);
        ((DisplayListCell<T>)getCell()).setListStore(dataStoreIn);
    }

    public interface HoverCallBack<T> {

        public void onHoverChange(T hoverTargetIn, boolean isOverIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public Comparator<T> stringComparator = new Comparator<T>() {
        @Override
        public int compare(T itemOneIn, T itemTwoIn) {

            if ((null != itemOneIn) && (null!= itemOneIn.getDisplayString())) {
                if ((null != itemTwoIn) && (null!= itemTwoIn.getDisplayString())) {
                    return StringUtil.compareFuzzyCharacterString(itemOneIn.getDisplayString(),
                            itemTwoIn.getDisplayString(), true);
                } else {
                    return 1;
                }
            } else if ((null != itemTwoIn) && (null!= itemTwoIn.getDisplayString())) {
                return -1;
            }
            return 0;
        }
    };

    public Comparator<T> ordinalComparator = new Comparator<T>() {
        @Override
        public int compare(T itemOneIn, T itemTwoIn) {

            int myValueOne = (null != itemOneIn) ? itemOneIn.getOrdinal() : -1;
            int myValueTwo = (null != itemTwoIn) ? itemTwoIn.getOrdinal() : -1;

            return (Math.max(-1, Math.min(1, myValueOne - myValueTwo)));
        }
    };

    private T _emptyValue = null;
    private boolean _wasEmpty = false;
    private boolean _ordered = false;
    private HoverCallBack<T> _hoverCallBack = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiListView() {
        
        this(new ExtendedInfoKeyProvider<T>());
    }

    public CsiListView(ExtendedInfoKeyProvider<T> keyProviderIn) {
        
        this(new ListStore<T>(keyProviderIn), new ExtendedInfoValueProvider<T>());
    }

    @SuppressWarnings("unchecked")
    public CsiListView(ListStore<T> dataStoreIn) {

        super(dataStoreIn, new ExtendedInfoValueProvider<T>(), new DisplayListCell<T>());

        ((DisplayListCell<T>)getCell()).setListStore(dataStoreIn);
    }

    @SuppressWarnings("unchecked")
    public CsiListView(ListStore<T> dataStoreIn, ValueProvider<T, String> valueProviderIn) {

        super(dataStoreIn, valueProviderIn, new DisplayListCell<T>());

        ((DisplayListCell<T>)getCell()).setListStore(dataStoreIn);
    }

    public void setHoverCallBack(HoverCallBack<T> hoverCallBackIn) {

        _hoverCallBack = hoverCallBackIn;
    }

    public void setFocus(int indexIn) {
        
        focusItem(indexIn);
    }

    public void sortAscending() {

        getStore().clearSortInfo();
        getStore().addSortInfo(new Store.StoreSortInfo<T>(stringComparator, SortDir.ASC));
        refresh();
    }

    public void sortDescending() {

        getStore().clearSortInfo();
        getStore().addSortInfo(new Store.StoreSortInfo<T>(stringComparator, SortDir.DESC));
        refresh();
    }

    public void noSorting() {

        getStore().clearSortInfo();
        getStore().addSortInfo(new Store.StoreSortInfo<T>(ordinalComparator, SortDir.ASC));
        refresh();
    }

    public void add(T itemIn) {

        getStore().add(itemIn);
    }

    public void remove(T itemIn) {

        getStore().remove(itemIn);
    }

    public void clear() {

        getStore().clear();
    }

    public List<T> getAll() {

        return getStore().getAll();
    }

    public long size() {

        return getStore().size();
    }

    public Integer scrollToAndSelect(String displayValueIn) {

        return scrollToAndSelect(displayValueIn, true);
    }

    public Integer scrollToAndSelect(String displayValueIn, boolean exactMatchIn) {

        Integer myResult = scrollTo(displayValueIn, exactMatchIn);

        if (null != myResult) {

            getSelectionModel().select(myResult, false);
            setFocus(myResult);
        }
        return myResult;
    }

    public Integer scrollTo(String displayValueIn) {

        return scrollTo(displayValueIn, true);
    }

    public Integer scrollToFirst(String displayValueIn) {

        return scrollTo(displayValueIn, false);
    }

    @Override
    public void onBrowserEvent(Event eventIn) {
        
        if (isEnabled()) {
            
            XElement myTarget = eventIn.getEventTarget().cast();
            int myIndex = findElementIndex(getAppearance().findElement(myTarget));
            ListStore<T> myDataStore = getStore();
            T myItem = myDataStore.get(myIndex);

            if ((null != _hoverCallBack) && (null != myItem)) {

                if ("mouseover" == eventIn.getType()) {

                    _hoverCallBack.onHoverChange(myItem, true);

                } else if ("mouseout" == eventIn.getType()) {

                    _hoverCallBack.onHoverChange(myItem, false);
                }
            }
            if ((null == myItem) || ((!myItem.isDisabled()) && (!myItem.isComponent()))) {
    
                super.onBrowserEvent(eventIn);
            }
        }
    }

    public void refresh() {

        if (null != _emptyValue) {

            if (0 == this.getStore().size()) {

                this.getStore().add(_emptyValue);
                _wasEmpty = true;

            } else if (_wasEmpty && (1 < this.getStore().size())) {

                this.getStore().remove(_emptyValue);
                _wasEmpty = false;
            }
        }
        super.refresh();
    }

    public void setEmptyValue(T displayValueIn) {

        _emptyValue = displayValueIn;
    }

    private Integer scrollTo(String displayValueIn, boolean checkRemainderIn) {

        Integer myResult = null;

        if ((null != displayValueIn) && (0 < displayValueIn.length())) {

            myResult = locateMatchingElement(displayValueIn, checkRemainderIn);

            if (null != myResult) {

                getElement((int)size() - 1).scrollIntoView(getElement(), true);
                getElement(myResult).scrollIntoView(getElement(), true);
            }
        }
        if (null == myResult) {

            getElement(0).scrollIntoView(getElement(), true);
        }
        return myResult;
    }

    private Integer locateMatchingElement(String displayValueIn, boolean checkRemainderIn) {

        Integer myMatch = null;
        int myLimit = getStore().size();

        for (int myIndex = 0 ; myLimit > myIndex; myIndex++) {

            T myItem = getStore().get(myIndex);
            String myDisplayValue = myItem.getDisplayString();

            if ((null != myDisplayValue) && (0 < myDisplayValue.length())) {

                int myResult = StringUtil.compareCaselessCharacterString(displayValueIn, myDisplayValue, checkRemainderIn);

                if (0 == myResult) {

                    myMatch = myIndex;
                    break;

                } else if (_ordered && (-1 == myResult)) {

                    break;
                }
            }
        }
        return myMatch;
    }
}


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                                     Support Classes                                    //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

class ExtendedInfoValueProvider<T extends ExtendedInfo> implements ValueProvider<T, String>{

    @Override
    public String getValue(T objectIn) {

        return objectIn.getDisplayString();
    }

    // Unsupported operation
    @Override
    public void setValue(T objectIn, String valueIn) {
    }

    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedInfoKeyProvider<T extends ExtendedInfo> implements ModelKeyProvider<T>{

    @Override
    public String getKey(T objectIn) {

        return objectIn.getKey();
    }
}
