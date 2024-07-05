package csi.client.gwt.widget.input_boxes;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.widget.boot.Dialog;

public class FilteredPasswordTextBox extends FilteredStringInput {

    private static final char _bullet = (char)0x2022;

    private char[] _password = new char[256];
    private int _charCount = 0;
    private boolean _monitoring = false;

    public FilteredPasswordTextBox() {

    }

    public String getPassword() {

        if (!_monitoring) {

            filterValue();
        }
        return (0 < _charCount) ? String.valueOf(_password, 0, _charCount) : null;
    }

    @Override
    public boolean isValid() {

        boolean myValidFlag = true;
        String myTest = getPassword();

        if ((null != myTest) && (0 < myTest.length())) {

            myValidFlag = true;

        } else if (_isRequired) {

            myValidFlag = false;
        }
        if (myValidFlag) {

            if (null != colorChangingLabel) {

                colorChangingLabel.getElement().getStyle().setColor(_okColor);
            }
            getElement().getStyle().setColor(Dialog.txtLabelColor);

        } else {

            if (null != colorChangingLabel) {

                colorChangingLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
            }
            getElement().getStyle().setColor(Dialog.txtErrorColor);
        }
        return myValidFlag;
    }

    @Override
    public void restrictValue() {

    }

    public void filterValue() {

        int myCount = 0;
        boolean myChangeFlag = false;
        String myTest = getText();
        StringBuilder myBuffer = new StringBuilder();

        if ((null != myTest) && (0 < myTest.length())) {

            char[] myCharacterArray = myTest.toCharArray();

            for (int i = 0; myCharacterArray.length > i; i++) {

                char myCharacter = myCharacterArray[i];

                if (_bullet != myCharacter) {

                    myChangeFlag = true;
                    if (' ' < myCharacter) {

                        _password[myCount++] = myCharacter;
                        myBuffer.append(_bullet);
                    }

                } else {

                    myBuffer.append(_bullet);
                    myCount++;
                }
            }
        }
        if (myChangeFlag || (myCount != _charCount)) {

            setText((0 < myCount) ? myBuffer.toString() : null);
        }
        _charCount = myCount;
    }

    public void suspendMonitoring() {

        _monitoring = false;
    }

    public void beginMonitoring() {

        if (! _monitoring) {

            _monitoring = true;
            checkValue();
        }
    }

    private void checkValue() {

        if (_monitoring ) {

            filterValue();
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValue();
                }
            });
        }
    }
}
