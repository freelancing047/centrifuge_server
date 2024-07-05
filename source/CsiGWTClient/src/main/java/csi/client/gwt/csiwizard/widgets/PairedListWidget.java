package csi.client.gwt.csiwizard.widgets;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.BooleanResponse;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.LabeledListView;
import csi.client.gwt.widget.boot.SearchableList;
import csi.client.gwt.widget.boot.SelectableList;
import csi.client.gwt.widget.buttons.Button;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 7/27/2015.
 */
public class PairedListWidget<T extends ExtendedDisplayInfo> extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SearchableList<T> leftSelectionList;
    LabeledListView<T> rightSelectionList;
    Button[] buttonArray;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String _leftSelectionLabel = "Unselected Items";
    protected String _rightSelectionLabel = "Selected Items";
    protected boolean _leftReady = false;
    protected boolean _rightReady = false;
    protected boolean _disabled = false;
    protected String[] _choices;
    protected boolean _localControl;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking the ">>" button
    //
    public ClickHandler shiftAllRight
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (_leftReady && _rightReady) {

                    rightSelectionList.addAllItems(leftSelectionList.removeAllActiveItems());
                    refresh();
                }

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 1, myException);
            }
        }
    };

    //
    // Handle clicking the ">" button
    //
    public ClickHandler shiftSelectionRight
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (_leftReady && _rightReady) {

                    rightSelectionList.addAllItems(leftSelectionList.removeSelectedItems());
                    refresh();
                }

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 2, myException);
            }
        }
    };

    //
    // Handle clicking the "<" button
    //
    public ClickHandler shiftSelectionLeft
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (_leftReady && _rightReady) {

                    leftSelectionList.addAllItems(rightSelectionList.removeSelectedItems());
                    refresh();
                }

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 3, myException);
            }
        }
    };

    //
    // Handle clicking the "<<" button
    //
    public ClickHandler shiftAllLeft
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (_leftReady && _rightReady) {

                    leftSelectionList.addAllItems(rightSelectionList.removeAllActiveItems());
                    refresh();
                }

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 4, myException);
            }
        }
    };

    //
    // Handle selection made from available list
    //
    protected SelectionChangedEvent.SelectionChangedHandler<T> handleLeftSelectionChange
            = new SelectionChangedEvent.SelectionChangedHandler<T>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<T> eventIn) {

            try {

                setButtons();

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 5, myException);
            }
        }
    };

    //
    // Handle selection made from selected list
    //
    protected SelectionChangedEvent.SelectionChangedHandler<T> handleRightSelectionChange
            = new SelectionChangedEvent.SelectionChangedHandler<T>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<T> eventIn) {

            try {

                setButtons();

            } catch (Exception myException) {

                Dialog.showException("PairedListWidget", 6, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public PairedListWidget(String[] choicesIn) {

        this(choicesIn, true, null);
    }

    public PairedListWidget(String[] choicesIn, boolean localControlIn) {

        this(choicesIn, localControlIn, null);
    }

    public PairedListWidget(BooleanResponse handlerIn) {

        this(null, false, handlerIn);
    }

    public PairedListWidget() {

        this(null, false, null);
    }

    public PairedListWidget(String[] choicesIn, boolean localControlIn, BooleanResponse handlerIn) {

        super();

        try {

            _choices = choicesIn;
            _localControl = localControlIn;
            initializeObject(handlerIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 7, myException);
        }
    }

    public void setSearchButton(boolean valueIn) {

        leftSelectionList.setSearchButton(valueIn);
    }

    public void loadDataSet(Collection<T> fullListIn, List<Collection<T>> selectedListsIn) {

        try {

            _leftReady = leftSelectionList.loadData(filterCollection(fullListIn, selectedListsIn));
            _rightReady = rightSelectionList.loadDataSet(selectedListsIn);
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 9, myException);
        }
    }

    public void loadData(Collection<T> fullListIn, Collection<T> selectedListIn) {

        try {

            _leftReady = leftSelectionList.loadData(filterCollection(fullListIn, selectedListIn));
            _rightReady = rightSelectionList.loadData(selectedListIn);
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 10, myException);
        }
    }

    public void replaceLeftData(Collection<T> fullListIn) {

        try {

            _leftReady = leftSelectionList.loadData(filterCollection(fullListIn,
                                                                        rightSelectionList.getAllItemCollections()));
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 10, myException);
        }
    }

    public void removeOnLeft(String displayIn) {

        try {

            if (null != leftSelectionList) {

                leftSelectionList.removeItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 11, myException);
        }
    }

    public void removeOnRight(String displayIn) {

        try {

            if (null != rightSelectionList) {

                rightSelectionList.removeItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 12, myException);
        }
    }

    public void enableOnLeft(String displayIn) {

        try {

            if (null != leftSelectionList) {

                leftSelectionList.enableItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 13, myException);
        }
    }

    public void disableOnLeft(String displayIn) {

        try {

            if (null != leftSelectionList) {

                leftSelectionList.disableItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 14, myException);
        }
    }

    public void enableAllOnLeft() {

        try {

            if (null != leftSelectionList) {

                leftSelectionList.enableAll();
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 15, myException);
        }
    }

    public void disableAllOnLeft() {

        try {

            if (null != leftSelectionList) {

                leftSelectionList.disableAll();
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 16, myException);
        }
    }

    public void enableOnRight(String displayIn) {

        try {

            if (null != rightSelectionList) {

                rightSelectionList.enableItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 17, myException);
        }
    }

    public void disableOnRight(String displayIn) {

        try {

            if (null != rightSelectionList) {

                rightSelectionList.enableItem(displayIn);
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 18, myException);
        }
    }

    public void enableAllOnRight() {

        try {

            if (null != rightSelectionList) {

                rightSelectionList.enableAll();
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 19, myException);
        }
    }

    public void disableAllOnRight() {

        try {

            if (null != rightSelectionList) {

                rightSelectionList.disableAll();
            }
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 20, myException);
        }
    }

    public void enable(String displayIn) {

        try {

            enableOnLeft(displayIn);
            enableOnRight(displayIn);
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 21, myException);
        }
    }

    public void disable(String displayIn) {

        try {

            disableOnLeft(displayIn);
            disableOnRight(displayIn);
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 22, myException);
        }
    }

    public void enableAll() {

        try {

            enableAllOnLeft();
            enableAllOnRight();
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 23, myException);
        }
    }

    public void disableAll() {

        try {

            disableAllOnLeft();
            disableAllOnRight();
            refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 24, myException);
        }
    }

    public List<T> getListOnLeft() {

        try {

            return leftSelectionList.getAllItems();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 25, myException);
        }
        return null;
    }

    public List<T> getListOnRight() {

        try {

            return rightSelectionList.getAllItems();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 26, myException);
        }
        return null;
    }

    public List<T> getListOnRight(int listIdIn) {

        try {

            return rightSelectionList.getAllItems(listIdIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 27, myException);
        }
        return null;
    }

    public List<List<T>> getAllListsOnRight() {

        try {

            return rightSelectionList.getAllItemLists();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 28, myException);
        }
        return null;
    }

    public int getLeftListCount() {

        return (null != leftSelectionList) ? leftSelectionList.getListView().getItemCount() : 0;
    }

    public int getRightListCount() {

        return (null != rightSelectionList) ? rightSelectionList.getListView().getItemCount() : 0;
    }

    public void labelLeftColumn(String labelIn) {

        try {

            _leftSelectionLabel = labelIn;
            leftSelectionList.labelSelectionList(_leftSelectionLabel);

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 29, myException);
        }
    }

    public void labelRightColumn(String labelIn) {

        try {

            _rightSelectionLabel = labelIn;
            rightSelectionList.labelSelectionList(_rightSelectionLabel);

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 30, myException);
        }
    }

    public void setEnabled(boolean enabledIn) {

        try {

            _disabled = !enabledIn;
            setButtons();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 31, myException);
        }
    }

    public void refresh() {

        try {

            leftSelectionList.refresh();
            rightSelectionList.refresh();
            setButtons();

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 32, myException);
        }
    }

    public void setEmptyValue(T displayValueIn) {

        try {

            rightSelectionList.setEmptyValue(displayValueIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListWidget", 33, myException);
        }
    }

    public void sortAscendingLeft() {

        leftSelectionList.sortAscending();
    }

    public void sortDescendingLeft() {

        leftSelectionList.sortDescending();
    }

    public void noSortingLeft() {

        leftSelectionList.noSorting();
    }

    public void sortAscendingRight() {

        rightSelectionList.sortAscending();
    }

    public void sortDescendingRight() {

        rightSelectionList.sortDescending();
    }

    public void noSortingRight() {

        rightSelectionList.noSorting();
    }

    public void forceList(int indexIn) {

        if ((null != _choices) && (!_localControl)) {

            rightSelectionList.forceList(indexIn);
        }
    }

    @Override
    public String getText() throws CentrifugeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void grabFocus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getButtonWidth() {

        return Dialog.intButtonHeight - 4;
    }

    @Override
    protected int getButtonHeight() {

        return ((2 * Dialog.intButtonHeight) / 3) - 2;
    }

    protected void createWidgets(BooleanResponse handlerIn) {

        createListViews(handlerIn);
        leftSelectionList.labelSelectionList(_leftSelectionLabel);
        add(leftSelectionList);
        rightSelectionList.labelSelectionList(_rightSelectionLabel);
        add(rightSelectionList);
        buttonArray = new Button[] {new Button(">>"), new Button(">"), new Button("<"), new Button("<<")};

        for (int i = 0; buttonArray.length > i; i++) {

            buttonArray[i].getElement().getStyle().setPadding(0, Style.Unit.PX);
            buttonArray[i].setPixelSize(getButtonWidth(), getButtonHeight());
            buttonArray[i].setEnabled(false);
            add(buttonArray[i]);
        }
    }

    @Override
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHeight = getHeight();
        int myButtonWidth = getButtonWidth() + 4;
        int myButtonHeight = getButtonHeight() + 4;
        int myButtonPanelWidth = (2 * Dialog.intMargin) + myButtonWidth;
        int myListPanelWidth = (myWidth - myButtonPanelWidth) / 2;
        int myLeftPanelLeft = 0;
        int myRightPanelLeft = myLeftPanelLeft + myListPanelWidth + myButtonPanelWidth;
        int myButtonLeft = myLeftPanelLeft + myListPanelWidth + Dialog.intMargin;

        int myListPanelTop = 0;
        int myListPanelHeight = myHeight - Dialog.intMargin;
        int myButtonSpacing = (myHeight - Dialog.intTextBoxHeight - Dialog.intMargin - (4 * myButtonHeight)) / 5;

        setWidgetTopHeight(leftSelectionList, myListPanelTop, Style.Unit.PX, myListPanelHeight, Style.Unit.PX);
        setWidgetLeftWidth(leftSelectionList, myLeftPanelLeft, Style.Unit.PX, myListPanelWidth, Style.Unit.PX);
        leftSelectionList.setPixelSize(myListPanelWidth, myListPanelHeight);

        setWidgetTopHeight(rightSelectionList, myListPanelTop, Style.Unit.PX, myListPanelHeight, Style.Unit.PX);
        setWidgetLeftWidth(rightSelectionList, myRightPanelLeft, Style.Unit.PX, myListPanelWidth, Style.Unit.PX);
        rightSelectionList.setPixelSize(myListPanelWidth, myListPanelHeight);

        for (int i = 0; 4 > i; i++) {

            int myButtonTop = myListPanelTop + Dialog.intTextBoxHeight + myButtonSpacing
                                + (i * (myButtonHeight + myButtonSpacing));

            setWidgetTopHeight(buttonArray[i], myButtonTop, Style.Unit.PX, myButtonHeight, Style.Unit.PX);
            setWidgetLeftWidth(buttonArray[i], myButtonLeft, Style.Unit.PX, myButtonWidth, Style.Unit.PX);
        }
    }

    protected void wireInHandlers() {

        buttonArray[0].addClickHandler(shiftAllRight);
        buttonArray[1].addClickHandler(shiftSelectionRight);
        buttonArray[2].addClickHandler(shiftSelectionLeft);
        buttonArray[3].addClickHandler(shiftAllLeft);

        leftSelectionList.addSelectionChangedHandler(handleLeftSelectionChange);
        rightSelectionList.addSelectionChangedHandler(handleRightSelectionChange);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeObject(BooleanResponse handlerIn) {

        createWidgets(handlerIn);
        layoutDisplay();
        wireInHandlers();
    }

    private void createListViews(BooleanResponse handlerIn) {

        Boolean _doAlpha = WebMain.getClientStartupInfo().isSortAlphabetically();

        leftSelectionList = new SearchableList<T>(handlerIn, true);
        if(_doAlpha) {
            leftSelectionList.sortAscending();
        }
        rightSelectionList = (null != _choices)
                                    ? new SelectableList<T>(_choices, _localControl)
                                    : new SearchableList<T>();
        refresh();
    }

    private void setButtons() {

        if (_leftReady && _rightReady && (!_disabled)) {

            if (0 < leftSelectionList.size()) {

                buttonArray[0].setEnabled(true);

                if (!leftSelectionList.isEmpty()) {

                    buttonArray[1].setEnabled(true);

                } else {

                    buttonArray[1].setEnabled(false);
                }
            } else {

                buttonArray[0].setEnabled(false);
                buttonArray[1].setEnabled(false);
            }

            if (0 < rightSelectionList.size()) {

                buttonArray[3].setEnabled(true);

                if (!rightSelectionList.isEmpty()) {

                    buttonArray[2].setEnabled(true);

                } else {

                    buttonArray[2].setEnabled(false);
                }
            } else {

                buttonArray[2].setEnabled(false);
                buttonArray[3].setEnabled(false);
            }

        } else if (null != buttonArray) {

            for (int i = 0; buttonArray.length > i; i++) {

                buttonArray[i].setEnabled(false);
            }
        }
    }

    @Override
    public void resetValue() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean atReset() {
        return false;
    }

    @Override
    public int getRequiredHeight() {
        return Dialog.intButtonHeight * 5;
    }

    protected Collection<T> filterCollection(Collection<T> fullListIn, List<Collection<T>> selectedListsIn) {

        Map<String, T> myMap = new LinkedHashMap<String, T>();

        if (null != fullListIn) {

            for (T myItem : fullListIn) {

                myMap.put(myItem.getKey(), myItem);
            }
        }
        if (null != selectedListsIn) {

            for (Collection<T> myList : selectedListsIn) {

                if (null != myList) {

                    Collection<T> mySelectionList = new ArrayList<T>(myList);

                    myList.clear();
                    for (T myItem : mySelectionList) {

                        String myKey = myItem.getKey();

                        if (myMap.containsKey(myKey)) {

                            myMap.remove(myKey);
                            myList.add(myItem);
                        }
                    }
                }
            }
        }
        return myMap.values();
    }

    protected Collection<T> filterCollection(Collection<T> fullListIn, Collection<T> selectedListIn) {

        List<Collection<T>> myList = new ArrayList<Collection<T>>();
        myList.add(selectedListIn);
        return filterCollection(fullListIn, myList);
    }
}
