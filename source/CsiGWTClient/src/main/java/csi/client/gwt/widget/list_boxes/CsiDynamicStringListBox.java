package csi.client.gwt.widget.list_boxes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/15/2016.
 */
public class CsiDynamicStringListBox extends HorizontalPanel implements BasicStringListBox {

    ExtendedStringListBox listBox;
    CsiOverlayTextBox userInput;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private boolean _ignoreSize = false;
    private boolean _enabled = true;
    private int _width = 0;
    private int _height = 0;
    private int _requestedWidth = 0;
    private int _requestedHeight = 0;
    private CsiDynamicStringListBox _this = this;
    private SelectionChangedEvent.SelectionChangedHandler _changeHandler = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiDynamicStringListBox() {

        listBox = new ExtendedStringListBox(this);
        listBox.setVisible(true);
        listBox.setEnabled(_enabled);
        add(listBox);

        userInput = new CsiOverlayTextBox(this);
        userInput.setVisible(false);
        userInput.setEnabled(false);
        add(userInput);

        setVisible(true);
        layoutDisplay();
    }

    public void activateInput() {

        listBox.setVisible(false);
        listBox.setEnabled(false);

        userInput.setVisible(true);
        userInput.setEnabled(true);
        grabFocus();
    }

    public void activateMenu() {

        userInput.setVisible(false);
        userInput.setEnabled(false);

        listBox.setVisible(true);
        listBox.setEnabled(_enabled);
        grabFocus();
    }

    public void updateMenu(String valueIn) {

        if ((null != valueIn) && (0 < valueIn.length())) {

            setSelectedValue(valueIn);

        } else {

            listBox.setSelectedIndex(0);
        }
    }

    @Override
    public void setEnabled(boolean enabledIn) {

        _enabled = enabledIn;
        activateMenu();
    }

    @Override
    public void setWidth(String widthIn) {

        _requestedWidth = extractValue(widthIn);
        layoutDisplay();
    }

    @Override
    public void setHeight(String heightIn) {

        _requestedHeight = extractValue(heightIn);
        layoutDisplay();
    }

    @Override
    public void setPixelSize(int widthIn, int heightIn) {

        _requestedWidth = widthIn;
        _requestedHeight = heightIn;
        layoutDisplay();
    }

    public void clear() {

        listBox.clear();
    }

    public void addAll(Collection<String> optionsIn) {

        listBox.addAll(optionsIn);
    }

    public void addAll(String[] optionsIn) {

        listBox.addAll(optionsIn);
    }

    public void addAllPairs(Collection<ValuePair<String, String>> optionsIn) {

        listBox.addAllPairs(optionsIn);
    }

    public void addAllPairs(String[][] optionsIn) {

        listBox.addAllPairs(optionsIn);
    }

    public HandlerRegistration addSelectionChangedHandler(SelectionChangedEvent.SelectionChangedHandler handlerIn) {

        _changeHandler = handlerIn;
        return listBox.addSelectionChangedHandler(handlerIn);
    }

    public boolean setSelectedIndex(int indexIn) {

        return listBox.setSelectedIndex(indexIn);
    }

    public boolean setSelectedValue(String valueIn) {

        if (!listBox.setSelectedValue(valueIn)) {

            List<String> mySelectionList = new ArrayList<>();

            // Add value to the list and try again on next entry
            listBox.updateList(valueIn);
            mySelectionList.add(valueIn);
            _changeHandler.onSelectionChanged(new SelectionChangedEvent(mySelectionList));
        }
        return true;
    }

    public int getSelectedIndex() {

        return listBox.getSelectedIndex();
    }

    public String getSelectedValue() {

        return listBox.getSelectedValue();
    }

    public void initializeDropdown(CsiOverlayTextBox.ValidationMode validationModeIn, List<ValuePair<String, String>> optionsIn) {

        userInput.setValidationMode(validationModeIn);
        listBox.initializeDropdown(validationModeIn, optionsIn);
    }

    public Widget getWidget() {

        return this;
    }

    public void setUserDisplay(String displayIn) {

        listBox.setUserDisplay(displayIn);
    }

    public void grabFocus() {

        DeferredCommand.add(new Command() {
            public void execute() {
                if (userInput.isVisible()) {
                    userInput.grabFocus();
                } else {
                    listBox.focus();
                }
            }
        });
    }

    public boolean isReady() {

        return listBox.isVisible();
    }

    public boolean isValid() {

        return isReady() && (0 < listBox.getSelectedIndex());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void layoutDisplay() {

        if (!_ignoreSize) {

            int myWidth = (0 < _requestedWidth) ? _requestedWidth : getOffsetWidth();
            int myHeight = (0 < _requestedHeight) ? _requestedHeight :getOffsetHeight();

            _ignoreSize = true;
            _requestedWidth = 0;
            _requestedHeight = 0;

            if (0 < myWidth) {

                _width = myWidth;
            }
            if (0 < myHeight) {

                _height = myHeight;
            }
            if (0 < _width) {

                super.setWidth(Integer.toString(_width) + "px");
                listBox.setWidth(Integer.toString(_width) + "px");
                userInput.setWidth(Integer.toString(_width) + "px");
            }
            if (0 < _height) {

                super.setHeight(Integer.toString(_height) + "px");
                listBox.setHeight(Integer.toString(_height) + "px");
                userInput.setHeight(Integer.toString(_height) + "px");
            }

            _ignoreSize = false;
        }
    }

    protected int extractValue(String stringIn) {

        int myValue = 0;

        for (int i = 0; stringIn.length() > i; i++) {

            char myCharacter = stringIn.charAt(i);

            if (('0' > myCharacter) || ('9' < myCharacter)) {

                break;
            }
            myValue = (myValue * 10) + ((int)myCharacter - (int)'0');
        }
        return myValue;
    }
}
