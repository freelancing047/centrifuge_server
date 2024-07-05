package csi.server.common.dto.SelectionListData;

/**
 * Created by centrifuge on 4/12/2018.
 */
public class OptionBasics extends SelectorBasics {

    private boolean defaultSelection = false;

    public OptionBasics() {

    }

    public OptionBasics(String keyIn, String nameIn, String remarksIn, boolean defaultSelectionIn) {

        super(keyIn, nameIn, remarksIn);
        defaultSelection = defaultSelectionIn;
    }

    public void setDefaultOption(boolean defaultSelectionIn) {

        defaultSelection = defaultSelectionIn;
    }

    public boolean getDefaultOption() {

        return defaultSelection;
    }
}
