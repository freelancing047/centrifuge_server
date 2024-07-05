package csi.client.gwt.widget.list_boxes;

import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.UserSortMode;
import csi.server.common.interfaces.SortingEnum;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 3/20/2018.
 */
public class SortingSet<T extends SortingEnum<T>> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private List<SortListBox<T>> sortSelectionSet;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private List<T> _optionSet;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SortingSet(SortListBox<T>[] listBoxesIn, List<T> optionSetIn, T[] selectionSetIn) {

        _optionSet = optionSetIn;

        if ((null != listBoxesIn) && (0 < listBoxesIn.length)) {

            sortSelectionSet = new ArrayList<>();

            for (int i = 0; listBoxesIn.length > i; i++) {

                SortListBox<T> myListBox = listBoxesIn[i];
                final int mySortIndex = i;

                myListBox.addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<UserSortMode>() {

                    @Override
                    public void onSelectionChanged(SelectionChangedEvent<UserSortMode> event) {

                        handleSortSelection(mySortIndex);
                    }
                });
                myListBox.disable();
                sortSelectionSet.add(myListBox);
            }
            if ((null != selectionSetIn) && (0 < selectionSetIn.length)) {

                List<T> mySortList = _optionSet;
                int myLimit = Math.min(sortSelectionSet.size(), selectionSetIn.length);

                for (int i = 0; myLimit > i; i++) {

                    mySortList = sortSelectionSet.get(i).initializeList(mySortList);
                    sortSelectionSet.get(i).setSelectedValue(selectionSetIn[i]);
                }

            } else {

                for (int i = 0; sortSelectionSet.size() > i; i++) {

                    sortSelectionSet.get(0).initializeList(_optionSet);
                }
            }
        }
    }

    public T getSelection(int indexIn) {

        SortListBox<T> myListBox = ((0 <= indexIn) && (sortSelectionSet.size() > indexIn))
                                        ? sortSelectionSet.get(indexIn) : null;

        return (null != myListBox) ? myListBox.getSelectedValue() : null;
    }
/*
    private void handleSortSelection(int indexIn) {

        try {

            List<T> mySortList = sortSelectionSet.get(indexIn).listAvailableItems();

            if (null != mySortList) {

                for (int i = indexIn + 1; sortSelectionSet.size() > i; i++) {

                    mySortList = sortSelectionSet.get(i).initializeList(mySortList);
                }

            } else {

                for (int i = indexIn + 1; sortSelectionSet.size() > i; i++) {

                    sortSelectionSet.get(i).disable();
                }
            }

        } catch (Exception myException) {

            Dialog.showException("UserListFilterDialog", 4, myException);
        }
    }
*/
    private void handleSortSelection(int indexIn) {

        try {

            boolean myActiveFlag = true;
            boolean[] myInUse = new boolean[_optionSet.size() / 2];

            for (int i = 0; sortSelectionSet.size() > i; i++) {

                T mySelection = sortSelectionSet.get(i).getSelectedValue();

                if (null != mySelection) {

                    myInUse[mySelection.ordinal() / 2] = true;

                } else {

                    break;
                }
            }
            for (int i = 0; sortSelectionSet.size() > i; i++) {

                SortListBox<T> myListBox = sortSelectionSet.get(i);

                if (myActiveFlag) {

                    T mySelection = myListBox.getSelectedValue();
                    List<T> mySortList = new ArrayList<>();
                    final int myAvailableIndex = (null != mySelection) ? mySelection.ordinal() / 2 : -1;

                    for (int j = 0; myInUse.length > j; j++) {

                        if ((myAvailableIndex == j) || (!myInUse[j])) {

                            int myIndex = 2 * j;

                            mySortList.add(_optionSet.get(myIndex++));
                            mySortList.add(_optionSet.get(myIndex));
                        }
                    }
                    myListBox.initializeList(mySortList);
                    if (null == mySelection) {

                        myListBox.setSelectedIndex(0);
                        myActiveFlag = false;
                    }

                } else {

                    myListBox.disable();
                }
            }

        } catch (Exception myException) {

            Dialog.showException("SortingSet", 1, myException);
        }
    }
}
