package csi.client.gwt.widget.input_boxes;

/**
 * Created by centrifuge on 7/21/2016.
 */
public class CharacterBox extends FilteredTextBox {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean checkValue(String stringIn) {

        String myString = stringIn.trim();

        if (0 == myString.length()) {

            setText(" ");
        }
        else if (1 < myString.length()) {

            if ('\\' == myString.charAt(myString.length() - 2)) {

                setText(myString.substring(myString.length() - 2, myString.length()));

            } else {

                setText(myString.substring(myString.length() - 1, myString.length()));
            }
        }
        return true;
    }
}
