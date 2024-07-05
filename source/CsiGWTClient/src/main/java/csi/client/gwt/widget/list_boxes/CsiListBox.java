package csi.client.gwt.widget.list_boxes;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CsiListView;
import csi.client.gwt.widget.cells.readonly.CsiListBoxCell;
import csi.server.common.enumerations.DisplayMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 1/25/2016.
 */
public class CsiListBox<T> extends ComboBox<CsiStringStoreItem<T>> implements ExtendedProcessing<CsiStringStoreItem<T>> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private boolean _forceTitle = true;
    private int _keyBase = 0;
    private Map<T, CsiStringStoreItem<T>> _dataMap = null;
    private CsiStringStoreItem<T> _nullItem = null;

    protected HandlerManager _handlerManager;
    protected CsiListBoxCell<CsiStringStoreItem<T>> _cell = null;
    protected CsiListView<CsiStringStoreItem<T>> _listView = null;
    protected ListStore<CsiStringStoreItem<T>> _dataStore = null;
    protected CsiStringStoreItem<T> _value = null;
    protected boolean _instantUpdate = false;
    protected CsiListBox<T> _this = this;
    protected boolean _blankOk = true;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    /*
    DomEvent.fireNativeEvent(Document.get().createKeyDownEvent(false, false, false, false, KeyCodes.KEY_ENTER), tb);
     */

    @Override
    public void onBrowserEvent(Event eventIn) {

        String myEventType = eventIn.getType();

        super.onBrowserEvent(eventIn);

        if ("keyup".equals(myEventType) || "keydown".equals(myEventType) || "keypress".equals(myEventType)) {

            int myKeyStroke = eventIn.getKeyCode();

            if ((KeyCodes.KEY_ENTER == myKeyStroke) || (KeyCodes.KEY_TAB == myKeyStroke)) {

                CsiStringStoreItem<T> mySelection = getPopUpSelection();

                eventIn.stopPropagation();
                if (null != mySelection) {

                    _cell.processSelection(mySelection);
                    forwardSelectionEvent(mySelection);
                }
                DeferredCommand.add(new Command() {
                    public void execute() {
                        _this.focus();
                    }
                });

            } else if (KeyCodes.KEY_ESCAPE == myKeyStroke) {

                eventIn.stopPropagation();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiListBox() {
        this(
                new LabelProvider<CsiStringStoreItem<T>>() {

                    @Override
                    public String getLabel(CsiStringStoreItem<T> item) {
                        return item.getDisplayString();
                    }
                },
                new CsiListView<CsiStringStoreItem<T>>()
        );
    }

    @Override
    public void setAllowBlank(boolean blankOkIn) {

        _blankOk = blankOkIn;
    }

    private CsiListBox(LabelProvider<CsiStringStoreItem<T>> labelProviderIn, CsiListView<CsiStringStoreItem<T>> listViewIn) {
        this(new CsiListBoxCell<CsiStringStoreItem<T>>(listViewIn.getStore(), labelProviderIn, listViewIn));
    }

    private CsiListBox(CsiListBoxCell<CsiStringStoreItem<T>> cellIn) {
        super(cellIn);
        _cell = cellIn;
        _cell.setHideTrigger(false);
        _dataStore = getStore();
        _listView = (CsiListView<CsiStringStoreItem<T>>)getListView();

        setValue(null);

        addSelectionHandler(new SelectionHandler<CsiStringStoreItem<T>>() {
            @Override
            public void onSelection(SelectionEvent<CsiStringStoreItem<T>> eventIn) {

                select(eventIn.getSelectedItem());
            }
        });
        addStyleName("string-combo-style");
        initialize();

        redraw();
    }

    public void addItem(String labelIn, T valueIn) {

        addItem(labelIn, valueIn, DisplayMode.NORMAL);
    }

    public void addItem(String labelIn, T valueIn, DisplayMode modeIn) {

        String myKey = Integer.toString(_keyBase++);
        String myLabel = ((null != labelIn) && (0 < labelIn.length())) ? labelIn.trim() : "";
        CsiStringStoreItem<T> myItem
                = new CsiStringStoreItem<T>(myKey, valueIn,
                (0 < myLabel.length()) ? myLabel : "? ? ?",
                modeIn);

        _dataStore.add(myItem);
        if (null != valueIn) {

            _dataMap.put(valueIn, myItem);

        } else {

            _nullItem = myItem;
        }
        if (null == _value) {

            setSelectedIndex(0);
        }
        _listView.refresh();
    }

    public void addItem(String titleIn, String labelIn, T valueIn, DisplayMode modeIn) {

        String myKey = Integer.toString(_keyBase++);
        String myLabel = ((null != labelIn) && (0 < labelIn.length())) ? labelIn.trim() : "";
        String myTitle = ((null != titleIn) && (0 < titleIn.length())) ? titleIn.trim() : "";
        CsiStringStoreItem<T> myItem
                = new CsiStringStoreItem<T>(myKey, valueIn,
                (0 < myLabel.length()) ? myLabel : "? ? ?",
                (0 < myTitle.length()) ? myTitle : null,
                modeIn);

        _dataStore.add(myItem);
        if (null != valueIn) {

            _dataMap.put(valueIn, myItem);

        } else {

            _nullItem = myItem;
        }
        if (null == _value) {

            setSelectedIndex(0);
        }
        _listView.refresh();
    }

    public void addItem(String descriptionIn, String titleIn, String labelIn, T valueIn, DisplayMode modeIn) {

        String myKey = Integer.toString(_keyBase++);
        String myLabel = ((null != labelIn) && (0 < labelIn.length())) ? labelIn.trim() : "";
        String myTitle = ((null != titleIn) && (0 < titleIn.length())) ? titleIn.trim() : "";
        String myDescription = ((null != descriptionIn) && (0 < descriptionIn.length())) ? descriptionIn.trim() : "";
        CsiStringStoreItem<T> myItem
                = new CsiStringStoreItem<T>(myKey, valueIn,
                (0 < myLabel.length()) ? myLabel : "? ? ?",
                (0 < myTitle.length()) ? myTitle : null,
                (0 < myDescription.length()) ? myDescription : null,
                modeIn);

        _dataStore.add(myItem);
        if (null != valueIn) {

            _dataMap.put(valueIn, myItem);

        } else {

            _nullItem = myItem;
        }
        if (null == _value) {

            setSelectedIndex(0);
        }
        _listView.refresh();
    }

    @Override
    public void clear() {

        _dataStore.clear();
        _dataMap.clear();
        _listView.refresh();
    }

    public void clearDisplay() {

        _dataStore.clear();
        _dataMap.clear();
        super.setValue(null);
        _listView.refresh();
    }

    public boolean setSelectedValue(T valueIn) {

        boolean mySuccess = false;
        CsiStringStoreItem<T> myItem = (null != valueIn) ? _dataMap.get(valueIn) : _nullItem;

        if (null != myItem) {

            super.setValue(myItem);
            mySuccess = true;
        }
        return mySuccess;
    }

    public T getSelectedValue() {

        CsiStringStoreItem<T> myValue = getValue();

        return (null != myValue) ? myValue.getValue() : null;
    }

    public String getSelectedText() {

        CsiStringStoreItem<T> myValue = getValue();

        return (null != myValue) ? myValue.getDisplayString() : null;
    }

    public String getItemText(int indexIn) {

        CsiStringStoreItem<T> myItem = null;

        if ((0 <= indexIn) && (_dataStore.size() > indexIn)) {

            myItem = _dataStore.get(indexIn);
        }

        return (null != myItem) ? myItem.getDisplayString() : null;
    }

    public HandlerRegistration addSelectionChangedHandler(
            SelectionChangedEvent.SelectionChangedHandler handler) {
        return _handlerManager.addHandler(SelectionChangedEvent.getType(), handler);
    }

    public int getItemCount() {
        return getStore().size();
    }

    public int getSelectedIndex() {

        CsiStringStoreItem<T> myValue = getValue();
        return (null != myValue) ? getStore().indexOf(myValue) : -1;
    }

    public boolean setSelectedIndex(int indexIn) {

        boolean mySuccess = false;
        if ((0 <= indexIn) && (_dataStore.size() > indexIn)) {

            setValue(_dataStore.get(indexIn));
            mySuccess = true;

        } else {

            setValue(null);
        }
        return mySuccess;
    }

    @Override
    public CsiStringStoreItem<T> getValue() {

        if (_instantUpdate) {

            return _value;

        } else {

            return super.getValue();
        }
    }

    public boolean isSelectable(int indexIn) {

        CsiStringStoreItem<T> myValue = ((0 <= indexIn) && (_dataStore.size() > indexIn)) ? _dataStore.get(indexIn) : null;

        return ((null != myValue) && (!(myValue.isComponent() || myValue.isDisabled())));
    }

    @Override
    public CsiStringStoreItem<T> getPopUpSelection() {

        CsiStringStoreItem<T> mySelection = null;

        if (null != _listView) {

            mySelection = _listView.getSelectionModel().getSelectedItem();
        }
        return mySelection;
    }

    public void forwardSelectionEvent(CsiStringStoreItem<T> selectionIn) {

        setValue(selectionIn, false, true);
        fireChangeEvent();
    }

    public void forwardSelectionEvent(int indexIn) {

        CsiStringStoreItem<T> myValue = ((0 <= indexIn) && (_dataStore.size() > indexIn))
                                            ? _dataStore.get(indexIn)
                                            : null;

        setValue(myValue, false, true);
        fireChangeEvent();
    }

    @Override
    public void setValue(CsiStringStoreItem<T> itemIn, boolean fireEventsIn, boolean redrawIn) {

        final CsiStringStoreItem<T> myOldValue = _value;

        if (_blankOk || (null != itemIn)) {

            super.setValue(itemIn, fireEventsIn, redrawIn);

            if (_instantUpdate) {

                _value = itemIn;
            }

        } else if (null != myOldValue) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    setValue(myOldValue, false, true);
                    _cell.setText(getElement(), myOldValue.getDisplayString());
                }
            });
        }
    }

    public List<T> getNonNullValueList() {

        List<T> myList = new ArrayList<T>(_dataStore.size());

        for (int i = 0; _dataStore.size() > i; i++) {

            CsiStringStoreItem<T> myItem = _dataStore.get(i);

            if (null != myItem.getValue()) {

                myList.add(myItem.getValue());
            }
        }
        return myList;
    }

    public List<T> getValueList() {

        List<T> myList = new ArrayList<T>(_dataStore.size());

        for (int i = 0; _dataStore.size() > i; i++) {

            CsiStringStoreItem<T> myItem = _dataStore.get(i);

            myList.add(myItem.getValue());
        }
        return myList;
    }

    public void sortAscending() {

        _listView.sortAscending();
    }

    public void sortDescending() {

        _listView.sortDescending();
    }

    public void noSorting() {

        _listView.noSorting();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void fireChangeEvent() {

        List<T> myChanges = (null != _value) ? new ArrayList<T>() : null;

        if (null != myChanges) {

            myChanges.add(_value.getValue());
        }
        if (null != _handlerManager){

            _handlerManager.fireEvent(new SelectionChangedEvent<T>(myChanges));
        }
    }

    protected void initialize(){

        _dataMap = new HashMap<T, CsiStringStoreItem<T>>();

        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);

        _cell.setParent(this);
        _handlerManager = new HandlerManager(this);
        _instantUpdate = true;
        setValue(null);
    }
}
