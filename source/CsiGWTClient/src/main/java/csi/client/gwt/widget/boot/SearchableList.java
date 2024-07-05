package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;

import csi.client.gwt.util.BooleanResponse;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.ui.form.SearchButton;
import csi.server.common.dto.SelectionListData.ExtendedInfo;

/**
 * Created by centrifuge on 9/13/2016.
 */
public class SearchableList<T extends ExtendedInfo> extends LabeledListView {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private TextBox searchBox;
    private SearchButton searchButton;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    String _searchText = "";
    boolean _searchActive = false;
    String _listLabel = "Unselected Items";
    String _listTitle = "Click & type to go to entry.";
    boolean _mouseOver = false;

    HandlerRegistration _focusHandler = null;
    HandlerRegistration _blurHandler = null;
    HandlerRegistration _clickHandler = null;
    HandlerRegistration _keyboardHandler = null;

    BooleanResponse _handler = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected FocusHandler processFocusLabel = new FocusHandler() {

        @Override
        public void onFocus(FocusEvent event) {

            processLabelClick.onClick(null);
        }
    };

    protected BlurHandler handleSearchFocus = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent eventIn) {

            checkSearch(false);

            labelSelectionList(null);
            _blurHandler.removeHandler();
            _clickHandler = searchBox.addClickHandler(processLabelClick);
            _searchActive = false;
        }
    };

    protected ClickHandler processLabelClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            searchBox.setText(_searchText);
            searchBox.setFocus(true);
            searchBox.selectAll();
            _clickHandler.removeHandler();
            _blurHandler = searchBox.addBlurHandler(handleSearchFocus);
            _keyboardHandler = searchBox.addKeyDownHandler(handleKeyboard);
            _searchActive = true;
            checkSearch(true);
        }
    };

    protected KeyDownHandler handleKeyboard = new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent eventIn) {

            if ((eventIn.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                || (eventIn.getNativeKeyCode() == KeyCodes.KEY_DOWN)) {

                _searchText = searchBox.getText();

                DeferredCommand.add(new Command() {
                    public void execute() {

                        if (null == listView.scrollToAndSelect(_searchText, false)) {

                            searchBox.setFocus(false);
                        }
                    }
                });
            }
        }
    };

    protected MouseOverHandler handleMouseOver = new MouseOverHandler() {
        @Override
        public void onMouseOver(MouseOverEvent event) {

            _mouseOver = true;
            labelSelectionList(null);
        }
    };

    protected MouseOutHandler handleMouseOut = new MouseOutHandler() {
        @Override
        public void onMouseOut(MouseOutEvent event) {

            _mouseOver = false;
            labelSelectionList(null);
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SearchableList() {

        this(null, false);
    }

    public SearchableList(boolean clearFlagsIn) {

        this(null, clearFlagsIn);
    }

    public SearchableList(BooleanResponse handlerIn, boolean clearFlagsIn) {

        super(clearFlagsIn);
        _handler = handlerIn;
        createWidgets();
        layoutDisplay();
        wireInHandlers();
    }

    public void setSearchButton(boolean valueIn) {

        if ((null != _handler) &&(null != searchButton)) {

            searchButton.setFiltered(valueIn);
        }
    }

    public void labelSelectionList(String labelIn) {

        if (null != labelIn) {

            _listLabel = labelIn;
        }
        if (!_searchActive) {

            if (_mouseOver) {

                searchBox.getElement().getStyle().setBackgroundColor("white");
                searchBox.setText(_searchText);

            } else {

                searchBox.getElement().getStyle().setBackgroundColor("#E0E0E0");
                searchBox.setText(_listLabel);
            }
            searchBox.setTitle(_listTitle);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void createWidgets() {

        searchBox = new TextBox();
        labelSelectionList(null);
        searchBox.addMouseOverHandler(handleMouseOver);
        searchBox.addMouseOutHandler(handleMouseOut);
        add(searchBox);
        if (null != _handler) {

            searchButton = new SearchButton(_handler);
            searchButton.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            searchButton.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            searchButton.getElement().getStyle().setBorderColor("#C0C0C0");
            add(searchButton);
        }
        add(listView);
    }

    @Override
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHeight = getHeight();

        if ((0 < myWidth) && (0 < myHeight)) {

            int myButtonWidth = 36;
            int myButtonHeight = 30;

            int myListPanelTop = Dialog.intTextBoxHeight;
            int myListPanelHeight = myHeight - Dialog.intTextBoxHeight;
            int myListHeight = myListPanelHeight - 2;
            int myTextBoxWidth = (null != _handler) ? (myWidth - myButtonWidth) : myWidth;
            int myTextBoxHeight = Dialog.intTextBoxHeight - 2;

            setWidgetTopHeight(searchBox, 0, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            setWidgetLeftWidth(searchBox, 0, Style.Unit.PX, myTextBoxWidth, Style.Unit.PX);
            searchBox.setPixelSize(myTextBoxWidth, myTextBoxHeight);
            searchBox.getElement().getStyle().setPaddingTop(0, Style.Unit.PX);
            searchBox.getElement().getStyle().setPaddingBottom(0, Style.Unit.PX);

            if (null != _handler) {

                setWidgetTopHeight(searchButton, 0, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
                setWidgetRightWidth(searchButton, 0, Style.Unit.PX, myButtonWidth, Style.Unit.PX);
                searchButton.setPixelSize(myButtonWidth, myButtonHeight);
            }
            setWidgetTopHeight(listView, myListPanelTop, Style.Unit.PX, myListPanelHeight, Style.Unit.PX);
            listView.setHeight(myListHeight);
        }
    }

    protected void wireInHandlers() {

        _clickHandler = searchBox.addClickHandler(processLabelClick);
        _focusHandler = searchBox.addFocusHandler(processFocusLabel);

    }

    protected void checkSearch(boolean forceIn) {

        if (_searchActive) {

            final String myText = searchBox.getText();

            if ((null != myText) && (null != listView) && (0 < listView.size())
                    && (forceIn || (!myText.equals(_searchText)))) {

                _searchText = myText;
                listView.scrollToFirst(myText);
            }
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkSearch(false);
                }
            });
        }
    }
}
