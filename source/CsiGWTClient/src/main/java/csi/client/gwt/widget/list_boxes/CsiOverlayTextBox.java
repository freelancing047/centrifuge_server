package csi.client.gwt.widget.list_boxes;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.input_boxes.CharacterBox;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;

/**
 * Created by centrifuge on 7/19/2016.
 */
public class CsiOverlayTextBox extends FocusPanel {

    public enum ValidationMode {

        NOTHING,
        CHARACTER,
        STRING
    };

    private FilteredTextBox textBox;

    private static final int _borderWidth = 2;

    private CsiDynamicStringListBox _parent = null;
    private ValidationMode _validationMode = ValidationMode.NOTHING;
    private int _width = 100;
    private boolean _enabled = false;
    private boolean _closing = false;
    private boolean _monitoring = false;
    private CsiOverlayTextBox _this = this;
    private HandlerRegistration _keyDownHandlerRegistration = null;
    private HandlerRegistration _keyUpHandlerRegistration = null;
    private HandlerRegistration _keyPressHandlerRegistration = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private FocusListener _focusMonitor = new FocusListener() {

        @Override
        public void onFocus(Widget senderIn) {

        }

        @Override
        public void onLostFocus(Widget senderIn) {

            returnResultsAndClose();
        }
    };

    private KeyDownHandler handlePanelKeyDown
            = new KeyDownHandler() {

        @Override
        public void onKeyDown(KeyDownEvent eventIn) {

            if (_this.isVisible()) {

                if (KeyCodes.KEY_ENTER == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();

                } else if (KeyCodes.KEY_TAB == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();
                    returnResultsAndClose();

                } else if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();
                    tossResultsAndClose();
                }
            }
        }
    };

    private KeyUpHandler handlePanelKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            if (_this.isVisible()) {

                if ((KeyCodes.KEY_ENTER == eventIn.getNativeKeyCode())
                        || (KeyCodes.KEY_TAB == eventIn.getNativeKeyCode())
                        || (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode())) {

                    eventIn.stopPropagation();
                }
            }
        }
    };

    private KeyPressHandler handlePanelKeyPress
            = new KeyPressHandler() {

        @Override
        public void onKeyPress(KeyPressEvent eventIn) {

            if (_this.isVisible()) {

                if (KeyCodes.KEY_ENTER == eventIn.getCharCode()) {

                    eventIn.stopPropagation();
                    returnResultsAndClose();

                } else if (KeyCodes.KEY_TAB == eventIn.getCharCode()) {

                    eventIn.stopPropagation();

                } else if (KeyCodes.KEY_ESCAPE == eventIn.getCharCode()) {

                    eventIn.stopPropagation();
                }
            }
        }
    };

    private ClickHandler handleApply = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            _parent.updateMenu(textBox.getText());
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiOverlayTextBox(CsiDynamicStringListBox parentIn) {

        _parent = parentIn;
        createWidgets();
    }

    public void setEnabled(boolean enabledIn) {

        _enabled = enabledIn;
        if (_enabled) {

            _closing = false;
            addHandlers();
            textBox.addFocusListener(_focusMonitor);
            textBox.setFocus(true);
            beginMonitoring();

        } else {

            suspendMonitoring();
            textBox.removeFocusListener(_focusMonitor);
            removeHandlers();
        }
    }

    public void grabFocus() {

        textBox.setFocus(true);
    }

    public void setValidationMode(ValidationMode validationModeIn) {

        _validationMode = validationModeIn;
        createWidgets();
    }

    public void setWidth(String widthIn) {

        _width = extractValue(widthIn);
        textBox.formatBox(_width, _borderWidth);
    }

    public String getText() {

        return textBox.getText();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createWidgets() {

        clear();

        textBox = ValidationMode.CHARACTER.equals(_validationMode) ? new CharacterBox() : new FilteredTextBox();
        add(textBox);
        textBox.formatBox(_width, _borderWidth);
    }

    private void addHandlers() {

        if (null == _keyDownHandlerRegistration) {

            _keyDownHandlerRegistration = RootPanel.get().addDomHandler(handlePanelKeyDown, KeyDownEvent.getType());
        }
        if (null == _keyUpHandlerRegistration) {

            _keyUpHandlerRegistration = RootPanel.get().addDomHandler(handlePanelKeyUp, KeyUpEvent.getType());
        }
        if (null == _keyPressHandlerRegistration) {

            _keyPressHandlerRegistration = RootPanel.get().addDomHandler(handlePanelKeyPress, KeyPressEvent.getType());
        }
    }

    private void removeHandlers() {

        DeferredCommand.add(new Command() {
            public void execute() {
                if (null != _keyDownHandlerRegistration) {

                    _keyDownHandlerRegistration.removeHandler();
                    _keyDownHandlerRegistration = null;
                }
                if (null != _keyUpHandlerRegistration) {

                    _keyUpHandlerRegistration.removeHandler();
                    _keyUpHandlerRegistration = null;
                }
                if (null != _keyPressHandlerRegistration) {

                    _keyPressHandlerRegistration.removeHandler();
                    _keyPressHandlerRegistration = null;
                }
            }
        });
    }

    private void beginMonitoring() {

        _monitoring = true;
        checkValue();
    }

    private void suspendMonitoring() {

        _monitoring = false;
    }

    private boolean checkValue() {

        if (_monitoring) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValue();
                }
            });
        }
        return textBox.isValid();
    }

    private void returnResultsAndClose() {

        if (!_closing) {
            _closing = true;
            DeferredCommand.add(new Command() {
                public void execute() {
                    _parent.updateMenu(textBox.getText());
                    _parent.activateMenu();
                }
            });
        }
    }

    private void tossResultsAndClose() {

        if (!_closing) {
            _closing = true;
            DeferredCommand.add(new Command() {
                public void execute() {
                    _parent.updateMenu(null);
                    _parent.activateMenu();
                }
            });
        }
    }
}
