package csi.client.gwt.widget.list_boxes;

import java.util.Collection;

import com.google.gwt.user.client.ui.RequiresResize;

import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/19/2016.
 */
public class ExtendedStringListBox extends CsiStringListBox implements RequiresResize {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiDynamicStringListBox _parent = null;
    private Collection<ValuePair<String, String>> _baseList = null;
    private String _userValue = null;
    private CsiOverlayTextBox.ValidationMode _validationMode = CsiOverlayTextBox.ValidationMode.NOTHING;
    private int _userPromptIndex = -1;
    private int _userValueIndex = -1;
    private String _userDisplay = i18n.userValue();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ExtendedStringListBox(CsiDynamicStringListBox parentIn) {

        super();

        _parent = parentIn;
    }

    public void initializeDropdown(CsiOverlayTextBox.ValidationMode validationModeIn, Collection<ValuePair<String, String>> listIn) {

        _validationMode = (null!= validationModeIn) ? validationModeIn : CsiOverlayTextBox.ValidationMode.NOTHING;
        _baseList = listIn;

        reloadList();
        setSelectedIndex(0);
    }

    @Override
    public void forwardSelectionEvent(final int indexIn) {

        if (indexIn == _userPromptIndex) {

        } else {

            super.forwardSelectionEvent(indexIn);
        }
    }

    @Override
    public void setValue(CsiStringStoreItem<String> itemIn, boolean fireEventsIn, boolean redrawIn) {

        super.setValue(itemIn, fireEventsIn, redrawIn);

        if (getSelectedIndex() == _userPromptIndex) {

            _parent.activateInput();
        }
    }

    @Override
    public boolean isSelectable(final int indexIn) {

        if (super.isSelectable(indexIn)) {

            if (indexIn == _userPromptIndex) {

                _cell.collapseList();
                _parent.activateInput();

            } else {

                return true;
            }
        }
        return false;
    }

    @Override
    public void onResize() {
        setWidth(getParent().getOffsetWidth());
    }
    
    public void updateList(String userValueIn) {

        _userValue = userValueIn;
        clear();
        reloadList();
        setSelectedIndex(_userValueIndex);
    }

    public void reloadList() {

        addAllPairs(_baseList);

        if ((null != _userValue) && (0 < _userValue.length())) {

            _userValueIndex = getItemCount();
            addItem(i18n.csiUserValue_Display(_userDisplay, _userValue), _userValue);
        }
        if (!CsiOverlayTextBox.ValidationMode.NOTHING.equals(_validationMode)) {

            _userPromptIndex = getItemCount();
            addItem(i18n.csiUserValue_Prompt(), "<(???)>");
        }
    }

    public void setUserDisplay(String displayIn) {

        _userDisplay = displayIn;
    }
}
