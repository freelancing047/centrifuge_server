package csi.client.gwt.widget.list_boxes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.interfaces.SortingEnum;

/**
 * Created by centrifuge on 4/10/2016.
 */
public class SortListBox<T extends SortingEnum> extends CsiListBox<T> {


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

    public SortListBox() {

    }

    @Override
    public void disable() {

        super.clearDisplay();
        super.disable();
    }

    public List<T> listAvailableItems() {

        List<T> myReturnList = getNonNullValueList();
        SortingEnum mySelection = getSelectedValue();

        if (null != mySelection) {

            SortingEnum myPartner = mySelection.getPartner();

            myReturnList.remove(mySelection);
            myReturnList.remove(myPartner);
        }
        return myReturnList;
    }

    public List<T> initializeList(List<T> listIn) {

        List<T> myReturnList = null;

        if ((null != listIn) && (0 < listIn.size())) {

            T mySelection = getSelectedValue();

            clear();
            addItem(" ", null);

            if (null != mySelection) {

                int mySelectedIndex = 0;

                List<T> myNewList = new ArrayList<T>(listIn.size());

                int myOrdinalOne = mySelection.ordinal();
                int myOrdinalTwo = mySelection.getPartner().ordinal();

                for (int i = 0; listIn.size() > i; i++) {

                    T myValue = listIn.get(i);

                    addItem(myValue.getLabel(), myValue);
                    if (myValue.ordinal() == myOrdinalOne) {

                        mySelectedIndex = i + 1;

                    } else if (myValue.ordinal() != myOrdinalTwo) {

                        myNewList.add(myValue);
                    }
                }
                if (0 < mySelectedIndex) {

                    myReturnList = myNewList;
                    setSelectedIndex(mySelectedIndex);
                }

            } else {

                for (T myValue : listIn) {

                    addItem(myValue.getLabel(), myValue);
                }
            }
            setEnabled(true);

        } else {

            setEnabled(false);
        }
        return myReturnList;
    }
}
