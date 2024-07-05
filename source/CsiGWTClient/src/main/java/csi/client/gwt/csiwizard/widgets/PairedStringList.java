package csi.client.gwt.csiwizard.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.DisplayMode;

/**
 * Created by centrifuge on 9/2/2016.
 */
public class PairedStringList extends PairedListWidget<StringEntry> {

    public PairedStringList() {

        super();
    }

    public PairedStringList(Collection<String> fullListIn, Collection<String> selectedListIn) {

        super();
        initializeData(fullListIn, selectedListIn);
    }

    public void initializeData(Collection<String> fullListIn, Collection<String> selectedListIn) {

        List<StringEntry> myFullList = (null != fullListIn)
                                                ? new ArrayList<StringEntry>(fullListIn.size())
                                                : null;
        List<StringEntry> myselectedList = (null != selectedListIn)
                                                ? new ArrayList<StringEntry>(selectedListIn.size())
                                                : null;

        if (null != fullListIn) {

            for (String myItem : fullListIn) {

                myFullList.add(new StringEntry(myItem));
            }
        }

        if (null != selectedListIn) {

            for (String myItem : selectedListIn) {

                myselectedList.add(new StringEntry(myItem));
            }
        }
        super.loadData(myFullList, myselectedList);
    }

    public void setEmptyValue(String displayValueIn) {

        rightSelectionList.setEmptyValue(new StringEntry(displayValueIn, DisplayMode.DISABLED));
    }
}
