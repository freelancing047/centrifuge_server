package csi.client.gwt.widget.input_boxes;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 8/29/2016.
 */
public class FilteredIntegerInput extends FilteredStringInput {

    public Integer getNumericValue() {

        String myText = getValue();

        return ((null != myText) && (0 < myText.length())) ? Integer.decode(myText) : null;
    }

    public void setValue(Integer valueIn) {

        if (null != valueIn) {

            setText(valueIn.toString());

        } else {

            setText(null);
        }
    }

    @Override
    protected boolean checkValue(String stringIn) {

        String myString = stringIn.trim();
        boolean isValid = (0 < myString.length());

        if (isValid) {

            for (int i = 0; myString.length() > i; i++) {

                char myChar = myString.charAt(i);

                if (('0' > myChar) || ('9' < myChar)) {

                    getElement().getStyle().setColor(Dialog.txtErrorColor);

                    isValid = false;
                    break;
                }
            }
        }

        return isValid;
    }

    @Override
    protected void restrictValue(String stringIn) {

        if ((null != stringIn) && (0 < stringIn.length())) {

            char[] myCharacterArray = stringIn.toCharArray();
            StringBuilder myBuffer = new StringBuilder();

            for (int i = 0; myCharacterArray.length > i; i++) {

                char myCharacter = myCharacterArray[i];

                if (('0' <= myCharacter) && ('9' >= myCharacter)) {

                    myBuffer.append(myCharacter);
                }
            }
            if (myBuffer.length() < myCharacterArray.length) {

                setValue(myBuffer.toString());

            } else {

                super.restrictValue(stringIn);
            }
        }
    }
}
