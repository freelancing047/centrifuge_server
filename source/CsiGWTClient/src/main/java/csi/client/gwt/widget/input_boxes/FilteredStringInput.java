package csi.client.gwt.widget.input_boxes;

import java.util.Map;

import com.google.gwt.dom.client.Element;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 1/15/2015.
 */
public class FilteredStringInput extends FilteredTextBox {

    public FilteredStringInput() {

        super();
    }

    public FilteredStringInput(Element elementIn, String styleNameIn) {

        super(elementIn, styleNameIn);
    }

    public FilteredStringInput(Map<String, ? extends Object> rejectionMapIn) {

        super(rejectionMapIn);
    }

    public FilteredStringInput(Map<String, ? extends Object> rejectionMapIn, boolean ignoreException) {

        super(rejectionMapIn, ignoreException);
    }

    @Override
    protected boolean checkValue(String stringIn) {

        boolean isValid = super.checkValue(stringIn);

        if (isValid) {

            for (int i = 0; stringIn.length() > i; i++) {

                if (' ' >= stringIn.charAt(i)) {

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

                if (' ' < myCharacter) {

                    myBuffer.append(myCharacter);
                }
            }
            if (myBuffer.length() < myCharacterArray.length) {

                setText(myBuffer.toString());

            } else {

                super.restrictValue(stringIn);
            }
        }
    }
}
