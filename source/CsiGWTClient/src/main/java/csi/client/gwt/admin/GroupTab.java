package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.buttons.GreenButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.enumerations.GroupType;


public class GroupTab extends AdminTab {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface GroupTabUiBinder extends UiBinder<Widget, GroupTab> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    LayoutPanel container;

    @UiField
    GridContainer gridContainer;
    @UiField
    RadioButton searchCheckBox;
    @UiField
    TextBox searchTextBox;
    @UiField
    RadioButton allCheckBox;
    @UiField
    Label memberLabel;
    @UiField
    CsiStringListBox filterListBox;
    @UiField
    Label capcoSpacer;
    @UiField
    Label capcoLabel;
    @UiField
    CyanButton getButton;
    @UiField
    BlueButton newButton;
    @UiField
    BlueButton editButton;
    @UiField
    RedButton deleteButton;
    @UiField
    GreenButton groupAddButton;
    @UiField
    RedButton groupRemoveButton;
    @UiField
    Label bottomLabel;
    @UiField
    CsiStringListBox groupListBox;
    @UiField
    HorizontalPanel topContainer;
    @UiField
    HorizontalPanel bottomContainer;
    @UiField
    CyanButton clearButton;
    @UiField
    RadioButton replaceButton;
    @UiField
    RadioButton augmentButton;
    @UiField
    Label itemsReturnedLabel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static GroupTabUiBinder uiBinder = GWT.create(GroupTabUiBinder.class);

    private ClickHandler _externalHandler = null;

    GroupTab(GroupType typeIn, ClickHandler handlerIn) {

        super(GroupType.SHARING.equals(typeIn) ? 1 : 2);

        add(uiBinder.createAndBindUi(this));
        _externalHandler = handlerIn;
        if (GroupType.SHARING.equals(typeIn)) {

            bottomLabel.setText(_constants.membership());
            memberLabel.setText(_constants.membersOf());
            capcoLabel.setVisible(false);
            capcoSpacer.setVisible(false);

        } else {

            bottomLabel.setText(_constants.access());
            memberLabel.setText(_constants.providing());
            capcoLabel.setVisible(true);
            capcoSpacer.setVisible(false);
        }
        initialize();
    }

    @Override
    public TextBox getSearchTextBox() {
        return searchTextBox;
    }

    @Override
    public RadioButton getAllRadioButton() {
        return allCheckBox;
    }

    @Override
    public RadioButton getSearchRadioButton() {
        return searchCheckBox;
    }

    @Override
    public CyanButton getRetrievalButton() {
        return getButton;
    }

    @Override
    public BlueButton getNewButton() {
        return newButton;
    }

    @Override
    protected void initialize() {

        super.initialize();

        container.setWidgetTopHeight(topContainer, 0, Style.Unit.PX, 75, Style.Unit.PX);
        container.setWidgetTopBottom(gridContainer, 75, Style.Unit.PX, 60, Style.Unit.PX);
        container.setWidgetBottomHeight(bottomContainer, 0, Style.Unit.PX, 60, Style.Unit.PX);
    }

    @Override
    protected void wireInHandlers() {
        
        getButton.addClickHandler(_externalHandler);
        searchCheckBox.addClickHandler(radioClickHandler);
        allCheckBox.addClickHandler(radioClickHandler);
        searchTextBox.addKeyUpHandler(keyboardHandler);
        searchTextBox.addDropHandler(dropHandler);
        
        onShow();
    }

    public void resetFilterList() {

        filterListBox.setSelectedIndex(0);
    }

    public void resetGroupList() {

        groupListBox.setSelectedIndex(0);
    }
}
