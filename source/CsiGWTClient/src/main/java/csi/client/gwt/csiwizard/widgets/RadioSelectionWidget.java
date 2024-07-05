package csi.client.gwt.csiwizard.widgets;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ValuePair;


public class RadioSelectionWidget extends AbstractInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected enum Integrity {

        BROKEN,
        OK_SO_FAR,
        COMPLETE
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    RadioButton[] radioButtons = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private Integer _selection = null;
    private boolean _monitoring = false;
    private int[] _mapping = null;
    private int _mapSize = 0;
    private ChoiceMadeEventHandler _callback;
    private HandlerRegistration[] _registration = null;
    private int _errorButton = -1;


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

    public RadioSelectionWidget(List<ValuePair<String, Boolean>> buttonListIn, int defaultIn, ChoiceMadeEventHandler callbackIn) {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(buttonListIn, defaultIn);
    }

    public RadioSelectionWidget(String[] buttonListIn, int defaultIn, ChoiceMadeEventHandler callbackIn) {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(buttonListIn, defaultIn);
    }

    public RadioSelectionWidget(String[] buttonListIn, Boolean[] choicesIn,
                                int defaultIn, ChoiceMadeEventHandler callbackIn) {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(buttonListIn, choicesIn, defaultIn);
    }

    public RadioSelectionWidget(String[] buttonListIn, Boolean[] choicesIn, boolean[] enabledIn,
                                int defaultIn, ChoiceMadeEventHandler callbackIn) {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(buttonListIn, choicesIn, enabledIn, defaultIn);
    }

    public RadioSelectionWidget(List<ValuePair<String, Boolean>> buttonListIn, int defaultIn) {

        this(buttonListIn, defaultIn, null);
    }

    public RadioSelectionWidget(String[] buttonListIn, int defaultIn) {

        this(buttonListIn, defaultIn, null);
    }

    public RadioSelectionWidget(String[] buttonListIn, Boolean[] choicesIn, int defaultIn) {

        this(buttonListIn, choicesIn, defaultIn, null);
    }

    public RadioSelectionWidget(String[] buttonListIn, Boolean[] choicesIn, boolean[] enabledIn, int defaultIn) {

        this(buttonListIn, choicesIn, enabledIn, defaultIn, null);
    }

    public void enable(int radioIndexIn) {

        if ((0 < radioIndexIn) && (radioButtons.length > radioIndexIn)) {

            radioButtons[radioIndexIn].setEnabled(true);
        }
    }

    public void disable(int radioIndexIn) {

        if ((0 < radioIndexIn) && (radioButtons.length > radioIndexIn)) {

            radioButtons[radioIndexIn].setEnabled(false);
        }
    }

    public void show(int radioIndexIn) {

        if ((0 < radioIndexIn) && (radioButtons.length > radioIndexIn)) {

            radioButtons[radioIndexIn].setVisible(true);
        }
    }

    public void hide(int radioIndexIn) {

        if ((0 < radioIndexIn) && (radioButtons.length > radioIndexIn)) {

            radioButtons[radioIndexIn].setVisible(false);
        }
    }

    public void setChoice(int radioIndexIn) {

        if ((0 < radioIndexIn) && (radioButtons.length > radioIndexIn)) {

            radioButtons[radioIndexIn].setValue(true, true);
        }
    }

    public int getChoice() {

        int myChoice = -1;

        if (null != _mapping) {

            if ((0 <= _selection) && (_mapSize > _selection)) {

                myChoice = _mapping[_selection];
            }

        } else {

            myChoice = _selection;
        }

        return myChoice;
    }

    public void grabFocus() {

//        fileSelector.setFocus(true);
    }

    @Override
    public String getText() throws CentrifugeException {

        return null;
    }

    @Override
    public void resetValue() {

//        fileSelector.setText(_default);
//        reportValidity(checkIntegrity(fileSelector.getText()));
    }

    @Override
    public boolean isValid() {

        return (null != _selection);
    }

    public int getRequiredHeight() {

        return (null != radioButtons) ? Dialog.intTextBoxHeight * radioButtons.length : 0;
    }

    @Override
    public int getRequestedHeight() {

        return super.getRequestedHeight();
    }

    public boolean atReset() {

        return !isValid();
    }

    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        if (! _monitoring) {

            _monitoring = true;
            checkValidity();
        }
    }

    @Override
    public void setValue(String valueIn) {

//        fileSelector.setText(valueIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject(List<ValuePair<String, Boolean>> buttonListIn, int defaultIn) {

        _validator = null;

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(buttonListIn, defaultIn);

        //
        // Wire in the handlers
        //
        wireInHandlers();
    }

    //
    //
    //
    protected void initializeObject(String[] buttonListIn, int defaultIn) {

        _validator = null;

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(buttonListIn, defaultIn);

        //
        // Wire in the handlers
        //
        wireInHandlers();
    }

    //
    //
    //
    protected void initializeObject(String[] buttonListIn, Boolean[] choicesIn, final int defaultIn) {

        List<ValuePair<String, Boolean>> myList = new ArrayList<ValuePair<String, Boolean>>(buttonListIn.length);
        int myDefault = -1;
        int myLimit = Math.min(buttonListIn.length, choicesIn.length);

        _mapping = new int[myLimit];

        if ((null != choicesIn) && (0 < myLimit)) {

            for (int i = 0; myLimit > i; i++) {

                if ((null == choicesIn[i]) || choicesIn[i]) {

                    if (defaultIn == i) {

                        myDefault = _mapSize;

                    } else if ((-1 == myDefault) && (null != choicesIn[i]) && choicesIn[i]) {

                        myDefault = _mapSize;
                    }

                    _mapping[_mapSize++] = i;
                    myList.add(new ValuePair<String, Boolean>(buttonListIn[i], false));
                }
            }
            initializeObject(myList, myDefault);

        } else {

            initializeObject(buttonListIn, myDefault);
        }
    }

    //
    //
    //
    protected void initializeObject(String[] buttonListIn, Boolean[] choicesIn, boolean[] enabledIn, final int defaultIn) {

        initializeObject(buttonListIn, choicesIn, defaultIn);

        for (int i = 0; radioButtons.length > i; i++) {

            if (_mapSize > i) {

                int myOffset = _mapping[i];

                if (enabledIn.length > myOffset) {

                    if (!enabledIn[myOffset]) {

                        if (radioButtons[i].getValue()) {

                            radioButtons[i].getElement().getStyle().setColor("red");
                            _errorButton = i;

                        } else {

                            removeRadioButton(i);
                        }
                    }
                    radioButtons[i].setEnabled(enabledIn[myOffset]);
                }
            }
        }
    }

    protected void wireInHandlers() {

    }

    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHeight = Dialog.intTextBoxHeight;
        int myTop = _displayTop;

        for (int i = 0; radioButtons.length > i; i++) {

            if (radioButtons[i].isVisible()) {

                setWidgetTopHeight(radioButtons[i], myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
                setWidgetLeftWidth(radioButtons[i], 0, Style.Unit.PX, myWidth, Style.Unit.PX);

                myTop += myHeight;
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createWidgets(List<ValuePair<String, Boolean>> buttonListIn, int defaultIn) {

        if ((null != buttonListIn) && (0 < buttonListIn.size())) {

            int myCount = buttonListIn.size();

            radioButtons = new RadioButton[myCount];
            _registration = new HandlerRegistration[myCount];

            for (int i = 0; myCount > i; i++) {

                ValuePair<String, Boolean> myButtonDef = buttonListIn.get(i);

                add(createRadioButton(i, "selection", myButtonDef.getValue1(), myButtonDef.getValue2()));
            }
            if ((0 <= defaultIn) && (buttonListIn.size() > defaultIn)) {

                radioButtons[defaultIn].setValue(true);
                _selection = defaultIn;
            }
        }
    }

    private void createWidgets(String[] buttonListIn, int defaultIn) {

        if ((null != buttonListIn) && (0 < buttonListIn.length)) {

            int myCount = buttonListIn.length;

            radioButtons = new RadioButton[myCount];
            _registration = new HandlerRegistration[myCount];

            for (int i = 0; myCount > i; i++) {

                add(createRadioButton(i, "selection", buttonListIn[i], true));
            }
            if ((0 <= defaultIn) && (buttonListIn.length > defaultIn)) {

                radioButtons[defaultIn].setValue(true);
                _selection = defaultIn;
            }
        }
    }

    private RadioButton createRadioButton(final int indexIn, String groupIn, String labelIn, boolean isEnabledIn) {

        radioButtons[indexIn] = new RadioButton(groupIn, labelIn);
        _registration[indexIn] = radioButtons[indexIn].addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                _selection = indexIn;
                checkColor();
                if (null != _callback) {

                    _callback.onChoiceMade(new ChoiceMadeEvent(_selection));
                }
            }
        });
        return radioButtons[indexIn];
    }

    private void checkColor() {

        if (0 <= _errorButton) {

            radioButtons[_errorButton].getElement().getStyle().setColor("gray");
        }
        _errorButton = -1;
    }

    private void removeRadioButton(final int indexIn) {

        _registration[indexIn].removeHandler();
        radioButtons[indexIn].setVisible(false);
        radioButtons[indexIn].removeFromParent();
    }

    private void checkValidity() {

        reportValidity();

        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
}
