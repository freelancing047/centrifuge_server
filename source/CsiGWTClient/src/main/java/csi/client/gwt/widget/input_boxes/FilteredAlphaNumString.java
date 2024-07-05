package csi.client.gwt.widget.input_boxes;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 4/27/2018.
 */
public class FilteredAlphaNumString extends FilteredTextBox {

    private int _charCount = 0;
    private boolean _monitoring = false;

    public FilteredAlphaNumString() {

    }

    @Override
    public boolean isValid() {

        boolean myValidFlag = true;
        String myTest = filterValue();

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

    public String filterValue() {

        int myCount = 0;
        boolean myChangeFlag = false;
        String myTest = getText();
        StringBuilder myBuffer = new StringBuilder();

        if ((null != myTest) && (0 < myTest.length())) {

            char[] myCharacterArray = myTest.toCharArray();

            for (int i = 0; myCharacterArray.length > i; i++) {

                char myCharacter = myCharacterArray[i];
                int myValue = (int)myCharacter;

                if ((47 < myValue) && (123 > myValue) && alphaNumMap[myValue]) {

                    myBuffer.append(myCharacter);

                } else {

                    myChangeFlag = true;
                }
            }
            myTest = myBuffer.toString();
            myCount = myTest.length();
            if (myChangeFlag || (myCount < _charCount)) {

                setText((0 < myCount) ? myTest : null);
            }
        }
        _charCount = myCount;
        return (0 < myTest.length()) ? myTest : null;
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
