package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.buttons.GreenButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.dto.user.UserSecurityInfo;


public class UserTab extends AdminTab {

    interface UserTabUiBinder extends UiBinder<Widget, UserTab> {
    }

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
    Label filterLabel;
    @UiField
    Label combinedFilterLabel;
    @UiField
    CsiStringListBox sharingFilterListBox;
    @UiField
    CsiStringListBox securityFilterListBox;
    @UiField
    CyanButton getButton;
    @UiField
    BlueButton newButton;
    @UiField
    BlueButton editButton;
    @UiField
    GreenButton activateButton;
    @UiField
    RedButton deactivateButton;
    @UiField
    RedButton deleteButton;
    @UiField
    GreenButton groupAddButton;
    @UiField
    RedButton groupRemoveButton;
    @UiField
    CsiStringListBox sharingListBox;
    @UiField
    CsiStringListBox securityListBox;
    @UiField
    RadioButton sharingCheckBox;
    @UiField
    RadioButton securityCheckBox;
    @UiField
    HorizontalPanel showForSecurity_1;
    @UiField
    HorizontalPanel showForSecurity_2;
    @UiField
    HorizontalPanel showForSecurity_3;
    @UiField
    CheckBox activePerpetualCB;
    @UiField
    CheckBox activeTemporaryCB;
    @UiField
    CheckBox disabledCB;
    @UiField
    CheckBox suspendedCB;
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

    private static UserTabUiBinder uiBinder = GWT.create(UserTabUiBinder.class);

    private ClickHandler _externalHandler = null;

    private UserSecurityInfo _userInfo;
    private boolean _targetSharing = true;


    protected ClickHandler sharingClickHandler = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            securityListBox.setEnabled(false);
            sharingListBox.setEnabled(true);
        }
    };

    protected ClickHandler securityClickHandler = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            sharingListBox.setEnabled(false);
            securityListBox.setEnabled(true);
        }
    };


    UserTab(UserSecurityInfo userInfoIn, ClickHandler handlerIn) {
        super(0);
        
        _userInfo = userInfoIn;
        
        add(uiBinder.createAndBindUi(this));
        _externalHandler = handlerIn;
        initialize();
    }

    @Override
    public void onShow() {

        if (_targetSharing) {
            
            sharingCheckBox.setValue(true,  true);
            
        } else {
            
            securityCheckBox.setValue(true, true);
        }
        super.onShow();
    }

    @Override
    public void onHide() {

        _targetSharing = sharingCheckBox.getValue();
        super.onHide();
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

    public List<Boolean> getUserFlags() {

        List<Boolean> myFlags = new ArrayList<Boolean>();

        myFlags.add(activePerpetualCB.getValue());
        myFlags.add(activeTemporaryCB.getValue());
        myFlags.add(disabledCB.getValue());
        myFlags.add(suspendedCB.getValue());

        return myFlags;
    }

    @Override
    protected void initialize() {

        super.initialize();

        container.setWidgetTopHeight(topContainer, 0, Style.Unit.PX, 75, Style.Unit.PX);
        container.setWidgetTopBottom(gridContainer, 75, Style.Unit.PX, 60, Style.Unit.PX);
        container.setWidgetBottomHeight(bottomContainer, 0, Style.Unit.PX, 60, Style.Unit.PX);

        sharingFilterListBox.setEnabled(true);
        sharingFilterListBox.setVisible(true);
        sharingListBox.setEnabled(true);
        sharingListBox.setVisible(true);

        securityFilterListBox.setEnabled(_userInfo.isSecurity());
        securityFilterListBox.setVisible(_userInfo.isSecurity());
        securityListBox.setEnabled(_userInfo.isSecurity());
        securityListBox.setVisible(_userInfo.isSecurity());

        showForSecurity_1.setVisible(_userInfo.isSecurity());
        showForSecurity_2.setVisible(_userInfo.isSecurity());
        showForSecurity_3.setVisible(_userInfo.isSecurity());

        securityListBox.setEnabled(false);
        sharingListBox.setEnabled(true);
    }

    @Override
    protected void wireInHandlers() {

        getButton.addClickHandler(_externalHandler);
        searchCheckBox.addClickHandler(radioClickHandler);
        allCheckBox.addClickHandler(radioClickHandler);
        searchTextBox.addKeyUpHandler(keyboardHandler);
        searchTextBox.addDropHandler(dropHandler);
        sharingCheckBox.addClickHandler(sharingClickHandler);
        securityCheckBox.addClickHandler(securityClickHandler);

        onShow();
    }

    public void resetSharingFilterList() {

        sharingFilterListBox.setSelectedIndex(0);
    }

    public void resetSecurityFilterList() {

        securityFilterListBox.setSelectedIndex(0);
    }

    public void resetSharingList() {

        sharingListBox.setSelectedIndex(0);
    }

    public void resetSecurityList() {

        securityListBox.setSelectedIndex(0);
    }
}
