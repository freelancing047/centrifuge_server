package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;

import java.util.*;

/**
 * Created by centrifuge on 10/31/2017.
 */
public class SelectableList<T extends ExtendedDisplayInfo> extends LabeledListView<T> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CsiLayoutPanel headerPanel;
    HorizontalPanel packingPanel;
    RadioButton[] radioArray;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private HandlerRegistration _clickHandler = null;
    private List<Map<String, T>> _mapList;
    private int _activeIndex;
    private String[] _choices;
    private boolean _localControl = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SelectableList(String[] choicesIn, boolean localControlIn) {

        super();
        _choices = choicesIn;
        _localControl = localControlIn;
        _mapList = new ArrayList<Map<String, T>>(_choices.length);


        if ((null != _choices) && (0 < _choices.length)) {

            for (int i = 0; _choices.length > i; i++) {

                _mapList.add(new TreeMap<String, T>());
            }
        }
        createWidgets();
        layoutDisplay();
        wireInHandlers();
    }

    public CsiListView<T> getListView() {

        return listView;
    }

    public void labelSelectionList(String labelIn) {

    }

    @Override
    public boolean loadDataSet(List<Collection<T>> collectionIn) {

        if (null != collectionIn) {

            for (int i = 0; _mapList.size() > i; i++) {

                Map<String, T> myMap = _mapList.get(i);
                Collection<T> myCollection = null;

                myMap.clear();
                if (collectionIn.size() > i) {

                    myCollection = collectionIn.get(i);

                    if (null != myCollection) {

                        for (T myItem : myCollection) {

                            myMap.put(myItem.getDisplayString(), myItem);
                        }
                    }
                }
                if (radioArray[i].getValue()) {

                    activateList(i);
                }
            }
        }
        return true;
    }

    @Override
    public List<List<T>> getAllItemLists() {

        List<List<T>> myListOfLists = new ArrayList<List<T>>();

        if (null != _mapList) {

            for (int i = 0; _mapList.size() > i; i++) {

                Map<String, T> myMap = _mapList.get(i);
                List<T> myList = new ArrayList<T>();

                if ((null != myMap) && (myMap.size() > 0)) {

                    myList.addAll(myMap.values());
                }
                myListOfLists.add(myList);
            }
        }
        return myListOfLists;
    }

    @Override
    public void forceList(int indexIn) {

        if ((0 <= indexIn) && (radioArray.length > indexIn)) {

            radioArray[indexIn].setValue(true, true);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void createWidgets() {

        int myCount = (null != _choices) ? _choices.length : 0;

        headerPanel = new CsiLayoutPanel() {
            @Override
            protected void layoutDisplay() {

                getElement().getStyle().setBackgroundColor("#E0E0E0");
                getElement().getStyle().setBorderWidth(1.0, Style.Unit.PX);
                getElement().getStyle().setBorderColor("#C0C0C0");
                getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            }
        };
        packingPanel = new HorizontalPanel();
        radioArray = (0 < myCount) ? new RadioButton[myCount] : null;
        if (null != radioArray) {

            for (int i = 0; myCount > i; i++) {

                radioArray[i] = new RadioButton("choice", _choices[i]);
                radioArray[i].getElement().getStyle().setPaddingTop(5, Style.Unit.PX);
                radioArray[i].getElement().getStyle().setPaddingBottom(5, Style.Unit.PX);
                radioArray[i].getElement().getStyle().setPaddingLeft(30, Style.Unit.PX);
                radioArray[i].getElement().getStyle().setPaddingRight(10, Style.Unit.PX);
                packingPanel.add(radioArray[i]);
            }
            radioArray[0].setValue(true);
            CsiTabPanel.fixRadioButtons(radioArray);

            if (_localControl) {

                headerPanel.add(packingPanel);

            } else {

                headerPanel.add(new Label(_choices[0]));
            }
        }
        add(headerPanel);
        add(listView);
    }

    @Override
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHeight = getHeight();

        if ((0 < myWidth) && (0 < myHeight)) {

            int myListPanelTop = Dialog.intTextBoxHeight;
            int myListPanelHeight = myHeight - Dialog.intTextBoxHeight;
            int myListHeight = myListPanelHeight - 2;
            int myHeaderWidth = myWidth;
            int myHeaderHeight = Dialog.intTextBoxHeight;

            setWidgetTopHeight(headerPanel, 0, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            headerPanel.setPixelSize(myHeaderWidth - 2, myHeaderHeight);
            setWidgetTopHeight(listView, myListPanelTop, Style.Unit.PX, myListPanelHeight, Style.Unit.PX);
            listView.setHeight(myListHeight);
        }
    }

    protected void wireInHandlers() {

        for (int i = 0; radioArray.length > i; i++) {

            final int myIndex = i;

            radioArray[i].addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    if (myIndex != _activeIndex) {

                        activateList(myIndex);
                    }
                }
            });
        }
    }

    protected void activateList(int indexIn) {

        if ((0 <= indexIn) && (_mapList.size() > indexIn)) {

            listView.clear();
            _activeIndex = indexIn;
            _itemMap = _mapList.get(_activeIndex);
            for (T myItem : _itemMap.values()) {

                listView.add(myItem);
            }
            if (!_localControl) {

                headerPanel.clear();
                headerPanel.add(new Label(_choices[indexIn]));
            }
        }
    }
}
