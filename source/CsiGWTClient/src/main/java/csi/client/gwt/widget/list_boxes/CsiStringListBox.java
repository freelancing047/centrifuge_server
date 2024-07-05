package csi.client.gwt.widget.list_boxes;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;

import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 1/18/2016.
 */
public class CsiStringListBox extends CsiListBox<String> implements BasicStringListBox {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiStringListBox(boolean isEnabledIn) {
        super();
        setEnabled(isEnabledIn);
    }

    public CsiStringListBox() {
        super();
    }

    public void addAll(Collection<String> valuesIn) {

        for (String myValue : valuesIn) {

            addItem(myValue);
        }
    }

    public void addAll(String[] valuesIn) {

        for (int i = 0; valuesIn.length > i; i++) {

            addItem(valuesIn[i]);
        }
    }

    public void addAllPairs(Collection<ValuePair<String, String>> optionsIn) {

        for (ValuePair<String, String> myOption : optionsIn) {

            addItem(myOption.getValue1(), myOption.getValue2());
        }
    }

    public void addAllPairs(String[][] optionsIn) {

        for (int i = 0; optionsIn.length > i; i++) {

            String[] myPair = optionsIn[i];
            addItem(myPair[0], myPair[1]);
        }
    }

    public void addItem(String valueIn) {

        addItem(valueIn, valueIn, DisplayMode.NORMAL);
    }

    public void addTitledItem(String titleIn, String valueIn) {

        addItem(titleIn, valueIn, valueIn, DisplayMode.NORMAL);
    }

    public void addItem(String valueIn, DisplayMode modeIn) {

        addItem(valueIn, valueIn, modeIn);
    }

    public void addTitledItem(String titleIn, String valueIn, DisplayMode modeIn) {

        addItem(titleIn, valueIn, valueIn, modeIn);
    }

    public Widget getWidget() {

        return this;
    }
}
