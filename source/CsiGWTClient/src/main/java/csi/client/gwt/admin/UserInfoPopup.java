package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.csiwizard.panels.PairedListPanel;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.client.gwt.widget.input_boxes.FilteredPasswordTextBox;
import csi.client.gwt.widget.input_boxes.FilteredTrimmedTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;
import csi.server.common.dto.UserDisplay;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.GroupType;

public class UserInfoPopup implements HasHandlers {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, UserInfoPopup> {
    }

    interface StringProperties extends PropertyAccess<StringEntry> {

        ModelKeyProvider<StringEntry> key();
        
        @Path("value")
        LabelProvider<StringEntry> label();
        
        ValueProvider<StringEntry, String> value();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    FilteredTrimmedTextBox infoUsernameTextBox;
    @UiField
    FilteredPasswordTextBox infoPasswordTextBox;
    @UiField
    TextBox infoFirstNameTextBox;
    @UiField
    TextBox infoLastNameTextBox;
    @UiField
    TextBox infoEmailTextBox;
    @UiField
    TextBox infoRemarksTextBox;
    @UiField
    FilteredDateTimeBox infoExpirationDatePicker;
    @UiField
    CheckBox infoDisabledCheckBox;
    @UiField
    CheckBox infoPerpetualCheckBox;
    @UiField
    CheckBox infoSuspendedCheckBox;
    @UiField
    LayoutPanel separatorPanel;
    @UiField
    HorizontalPanel labelPanel;
    @UiField
    PairedListPanel<StringEntry> pairedListWidget;
    @UiField
    TextArea instructionTextArea;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    
    private static final String _txtTitle = _constants.administrationDialogs_UserPopupTitle();
    private static final String _txtHelpPath = _constants.administrationDialogs_UserPopupHelpTarget();
    private static final String _txtNewInstructions = _constants.userInfoPopup_NewInstructions();
    private static final String _txtUpdateInstructions = _constants.userInfoPopup_UpdateInstructions();
    private static final String _txtGroupInstructions = _constants.userInfoPopup_GroupInstructions();
    private static final String _txtCsoInstructions = _constants.userInfoPopup_CsoInstructions();
    private static final String _txtDualInstructions = _constants.userInfoPopup_DualInstructions();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private HandlerManager _handlerManager;
    
    private ValidatingDialog dialog;

    private UserDisplay _userInfo;
    private SharedItems _shared;
    private String _username = null;
    private Date _tomorrow = null;
    
    private List<StringEntry> _sharingSelection;
    private List<StringEntry> _securitySelection;
    private GroupType _groupDisplay = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleSharingRadioClick
    = new ClickHandler() {
        
        public void onClick(ClickEvent eventIn) {
            
            prepareSharingDisplay();
        }
    };

    private ClickHandler handleSecurityRadioClick
    = new ClickHandler() {
        
        public void onClick(ClickEvent eventIn) {
            
            prepareSecurityDisplay();
        }
    };
    
    private DataChangeEventHandler handlePasswordVerification
    =  new DataChangeEventHandler() {
        public void onDataChange(DataChangeEvent eventIn) {
            
            _userInfo.setPassword((String)eventIn.getData());
            
            fireEvent(new DataChangeEvent(_userInfo));
            infoPasswordTextBox.suspendMonitoring();
            dialog.hide();
        }
    };

    private ClickHandler handleCancelButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            infoPasswordTextBox.suspendMonitoring();
            dialog.hide();
        }
    };

    private ClickHandler handleAddButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            updateUserInfo();
            String myPassword = infoPasswordTextBox.getPassword();

            if ((null != myPassword) && (0 < myPassword.length())) {

                String myTitle = _constants.administrationDialogs_ConfirmPassword(_userInfo.getName());
                PasswordPopup myDialog = new PasswordPopup(myTitle, myPassword);

                myDialog.addDataChangeEventHandler(handlePasswordVerification);

                myDialog.show();
            }
            else {

                fireEvent(new DataChangeEvent(_userInfo));
                infoPasswordTextBox.suspendMonitoring();
                dialog.hide();
            }
        }
    };

    private ValueChangeHandler<Boolean> handleDisabledCheckBoxValueChange
    = new ValueChangeHandler<Boolean>() {
        
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> eventIn) {
            
            Date myTest = infoExpirationDatePicker.getValue();
            
            if (eventIn.getValue()) {
            
                infoPerpetualCheckBox.setValue(false);
            }
            if ((null != myTest) && _tomorrow.after(myTest)) {
                
                infoExpirationDatePicker.setValue(null);
            }

            infoExpirationDatePicker.setEnabled((!eventIn.getValue()) && (!infoPerpetualCheckBox.getValue()));
        }
        
    };

    private ValueChangeHandler<Boolean> handleSuspendedCheckBoxValueChange
            = new ValueChangeHandler<Boolean>() {

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> eventIn) {

            Date myTest = infoExpirationDatePicker.getValue();

            if (eventIn.getValue()) {

                infoPerpetualCheckBox.setValue(false);
            }
            if ((null != myTest) && _tomorrow.after(myTest)) {

                infoExpirationDatePicker.setValue(null);
            }

            infoExpirationDatePicker.setEnabled((!eventIn.getValue()) && (!infoPerpetualCheckBox.getValue()));
        }

    };
    
    private ValueChangeHandler<Boolean> handlePerpetualCheckBoxValueChange
    = new ValueChangeHandler<Boolean>() {
        
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> eventIn) {
            
            Date myTest = infoExpirationDatePicker.getValue();

            if (eventIn.getValue()) {
                
                infoDisabledCheckBox.setValue(false);
                infoSuspendedCheckBox.setValue(false);
            }
            if ((null != myTest) && _tomorrow.after(myTest)) {
                
                infoExpirationDatePicker.setValue(null);
            }

            infoExpirationDatePicker.setEnabled((!eventIn.getValue()) && (!infoDisabledCheckBox.getValue()));
        }
        
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public UserInfoPopup(UserDisplay userDataIn, SharedItems sharedIn, int userSlotsIn) {
        
       initializeDate();
       
        _handlerManager = new HandlerManager(this);

        dialog = uiBinder.createAndBindUi(this);
        
        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);
        _userInfo = userDataIn;
        _shared = sharedIn;

        createWidgets(userSlotsIn);
        infoPasswordTextBox.beginMonitoring();
    }

    public void show() {

        dialog.show(60);
        setFocus();
    }
    
    public UserDisplay getUserInfo() {
        
        return _userInfo;
    }

    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addDataChangeEventHandler(
            DataChangeEventHandler handler) {
        return _handlerManager.addHandler(DataChangeEvent.type, handler);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void setFocus() {

        if (infoUsernameTextBox.isEnabled()) {
            
            dialog.setFocus(infoUsernameTextBox);
        
        } else {
            
            dialog.setFocus(infoPasswordTextBox);
        }
    }
    
    private boolean updateUserInfo() {
        
        boolean myNewDataFlag = false;
        
        if (null == _userInfo) {
            
            _userInfo = new UserDisplay();
            _userInfo.setId(null);
            myNewDataFlag = true;
        }
        
        _userInfo.setName(infoUsernameTextBox.getValue());
        _userInfo.setPassword(infoPasswordTextBox.getValue());
        _userInfo.setFirstName(infoFirstNameTextBox.getValue());
        _userInfo.setLastName(infoLastNameTextBox.getValue());
        _userInfo.setEmail(infoEmailTextBox.getValue());
        _userInfo.setRemarks(infoRemarksTextBox.getValue());
        
        _userInfo.setDisabled(infoDisabledCheckBox.getValue());
        _userInfo.setPerpetual(infoPerpetualCheckBox.getValue());
        _userInfo.setSuspended(infoSuspendedCheckBox.getValue());
        _userInfo.setExpirationDate(infoExpirationDatePicker.getValue());

        extractSelection();
        
        _userInfo.setGroups(_shared.formatList(null, _sharingSelection, null));
        _userInfo.setClearance(_shared.formatList(null, _securitySelection, null));
        
        return myNewDataFlag;
    }
    /*
        private static final String _txtNewInstructions = _constants.userInfoPopup_NewInstructions();
    private static final String _txtUpdateInstructions = _constants.userInfoPopup_UpdateInstructions();
    private static final String _txtGroupInstructions = _constants.userInfoPopup_GroupInstructions();
    private static final String _txtRoleInstructions = _constants.userInfoPopup_RoleInstructions();
    private static final String _txtDualInstructions = _constants.userInfoPopup_DualInstructions();

     */
    private void createWidgets(int userSlotsIn) {
        
//        UserSecurityInfo myViewerInfo = _shared._userInfo;

        instructionTextArea.setReadOnly(true);
        instructionTextArea.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        instructionTextArea.getElement().getStyle().setProperty("resize", "none");
        instructionTextArea.getElement().getStyle().setBackgroundColor("white");
        instructionTextArea.getElement().getStyle().setBorderColor("white");
        instructionTextArea.getElement().getStyle().setColor(Dialog.txtInfoColor);

        _sharingSelection = new ArrayList<StringEntry>();
        _securitySelection = new ArrayList<StringEntry>();

        try {
            
            String myUserString = null;
            String myInitialInstructions;

            infoExpirationDatePicker.setFormat("mm/dd/yyyy");
            infoExpirationDatePicker.setAutoClose(true);
            infoExpirationDatePicker.setAlternateSize(AlternateSize.MINI);
            infoExpirationDatePicker.setMinView(HasViewMode.ViewMode.MONTH);
            infoExpirationDatePicker.setValue(null);
            infoExpirationDatePicker.setRequired(false);
            infoExpirationDatePicker.setHighlightToday(false);

            if (null != _userInfo) {
                
                String myGroupString = _userInfo.getGroups();
                String myClearanceString = _userInfo.getClearance();

                myInitialInstructions = _txtUpdateInstructions;
                myUserString = _userInfo.getName();
                
                if ((null != myGroupString) && (0 < myGroupString.length())) {
                    
                    String[] myGroups = myGroupString.split(", ");
                    
                    if (0 < myGroups.length) {
                        
                        for (String myGroup : myGroups) {
                            
                            _sharingSelection.add(new StringEntry(myGroup));
                        }
                    }
                }
                
                if ((null != myClearanceString) && (0 < myClearanceString.length())) {
                    
                    String[] myClearances = myClearanceString.split(", ");
                    
                    if (0 < myClearances.length) {
                        
                        for (String myClearance : myClearances) {
                            
                            _securitySelection.add(new StringEntry(myClearance));
                        }
                    }
                }
                
                if ((null != myUserString) && (0 < myUserString.length())) {
                    _username = myUserString;
                    infoUsernameTextBox.setText(_username);
                    infoUsernameTextBox.setEnabled(false);
                }
                
                if ((null != _userInfo.getFirstName()) && (0 < _userInfo.getFirstName().length())) {
                    infoFirstNameTextBox.setText(_userInfo.getFirstName());
                }
                
                if ((null != _userInfo.getLastName()) && (0 < _userInfo.getLastName().length())) {
                    infoLastNameTextBox.setText(_userInfo.getLastName());
                }
                
                if ((null != _userInfo.getEmail()) && (0 < _userInfo.getEmail().length())) {
                    infoEmailTextBox.setText(_userInfo.getEmail());
                }
                
                if ((null != _userInfo.getRemarks()) && (0 < _userInfo.getRemarks().length())) {
                    infoRemarksTextBox.setText(_userInfo.getRemarks());
                }
                infoDisabledCheckBox.setValue(_userInfo.getDisabled());
                infoPerpetualCheckBox.setValue(_userInfo.getPerpetual());
                infoSuspendedCheckBox.setValue(_userInfo.getSuspended());
                infoExpirationDatePicker.setValue(_userInfo.getExpirationDate());

                if ((0 >= userSlotsIn) && (_userInfo.getDisabled())) {

                    infoDisabledCheckBox.setEnabled(false);
                    infoPerpetualCheckBox.setEnabled(false);
                    infoSuspendedCheckBox.setEnabled(false);
                }

            } else {

                myInitialInstructions = _txtNewInstructions;
                _sharingSelection.add(new StringEntry(_shared._everyoneGroup));

                if (0 >= userSlotsIn) {

                    infoDisabledCheckBox.setValue(true);
                    infoDisabledCheckBox.setEnabled(false);
                    infoSuspendedCheckBox.setValue(false);
                    infoSuspendedCheckBox.setEnabled(false);
                    infoPerpetualCheckBox.setValue(false);
                    infoPerpetualCheckBox.setEnabled(false);
                }
            }
            
            if (_shared._adminUser.equals(myUserString) || _shared._securityUser.equals(myUserString)) {
                
                infoDisabledCheckBox.setValue(false);
                infoDisabledCheckBox.setEnabled(false);
                infoSuspendedCheckBox.setValue(false);
                infoSuspendedCheckBox.setEnabled(false);
                infoPerpetualCheckBox.setValue(true);
                infoPerpetualCheckBox.setEnabled(false);
                infoExpirationDatePicker.setValue(null);
                infoExpirationDatePicker.setEnabled(false);

            } else {

                infoDisabledCheckBox.addValueChangeHandler(handleDisabledCheckBoxValueChange);
                infoPerpetualCheckBox.addValueChangeHandler(handlePerpetualCheckBoxValueChange);
                infoSuspendedCheckBox.addValueChangeHandler(handleSuspendedCheckBoxValueChange);
                infoExpirationDatePicker.setEnabled((!infoDisabledCheckBox.getValue()) && (!infoPerpetualCheckBox.getValue()) && (!infoSuspendedCheckBox.getValue()));
                infoExpirationDatePicker.setMinimum(_tomorrow);
            }
            
            dialog.addObject(infoExpirationDatePicker, true);

            if (_shared.provideSharing()) {
                
                prepareSharingDisplay();
                
            } else {
                
                infoUsernameTextBox.setEnabled(false);
                infoPasswordTextBox.setEnabled(false);
                infoFirstNameTextBox.setEnabled(false);
                infoLastNameTextBox.setEnabled(false);
                infoEmailTextBox.setEnabled(false);
                infoRemarksTextBox.setEnabled(false);
                infoExpirationDatePicker.setEnabled(false);
                infoDisabledCheckBox.setEnabled(false);
                infoSuspendedCheckBox.setEnabled(false);
                infoPerpetualCheckBox.setEnabled(false);
            }

            separatorPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor);
            
            if (_shared.provideSecurity()) {
                
                Label myLabel = new Label("+++");
                RadioButton mySharing = new RadioButton("DualList", _constants.administrationDialogs_EditGroups());
                RadioButton mySecurity = new RadioButton("DualList", _constants.administrationDialogs_EditClearances());
                
                myLabel.getElement().getStyle().setColor(Dialog.txtPanelColor);

                if (_shared.provideSharing()) {
 
                    mySecurity.setValue(false);
                    mySharing.setValue(true);

                } else {

                    mySharing.setValue(false);
                    mySecurity.setValue(true);

                    prepareSecurityDisplay();
                }
                
                mySharing.addClickHandler(handleSharingRadioClick);
                mySecurity.addClickHandler(handleSecurityRadioClick);
                
                labelPanel.add(mySharing);
                labelPanel.add(myLabel);
                labelPanel.add(mySecurity);

                if (_shared.provideSharing()) {

                    instructionTextArea.setText(_constants.userInfoPopup_Instructions(myInitialInstructions, _txtDualInstructions));

                } else {

                    instructionTextArea.setText(_txtCsoInstructions);
                }

            } else if (_shared.provideSharing()) {
                
                Label myLabel = new Label(_constants.administrationDialogs_UserGroupsPrompt());
                
                myLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
                
                labelPanel.add(myLabel);
                instructionTextArea.setText(_constants.userInfoPopup_Instructions(myInitialInstructions, _txtGroupInstructions));
            }

            dialog.getActionButton().addClickHandler(handleAddButtonClick);
            dialog.getCancelButton().addClickHandler(handleCancelButtonClick);

            if (infoUsernameTextBox.isEnabled()) {

                infoPasswordTextBox.setRequired(false);
                dialog.addObject(infoPasswordTextBox, true);

                infoUsernameTextBox.setRejectionMap(_shared._roleMap);
                infoUsernameTextBox.setMode(ValidityCheckCapable.Mode.LOWERCASE);
                infoUsernameTextBox.setRequired(true);
                
                dialog.addObject(infoUsernameTextBox, true);
            }
            
        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
    
    private void prepareSharingDisplay() {
        
        Collection<StringEntry> myFullSharingList = _shared.getGroupList(GroupType.SHARING);
        String myUserString = (null != _userInfo) ? _userInfo.getName() : null;
        
        extractSelection();
        
        pairedListWidget.labelLeftColumn(_constants.administrationDialogs_UnselectedGroups());
        pairedListWidget.labelRightColumn(_constants.administrationDialogs_SelectedGroups());
        pairedListWidget.loadData(myFullSharingList, _sharingSelection);
        
        if (!_shared.provideSharing()){
            
            pairedListWidget.disableAll();
         }

        if (_shared.provideSecurity()) {
            
            pairedListWidget.enable(_shared._securityGroup);
            
        } else {
            
            pairedListWidget.disable(_shared._securityGroup);
        }
        
        if (_shared._adminUser.equals(myUserString)) {
            
            pairedListWidget.disableOnRight(_shared._adminGroup);
            pairedListWidget.disableAllOnLeft();
            
        } else if (_shared._securityUser.equals(myUserString)) {
            
            pairedListWidget.disableOnRight(_shared._securityGroup);
            pairedListWidget.disableAllOnLeft();
        }
        pairedListWidget.disable(_shared._everyoneGroup);
        _groupDisplay = GroupType.SHARING;
    }
    
    private void prepareSecurityDisplay() {
        
        Collection<StringEntry> myFullSecurityList = _shared.getGroupList(GroupType.SECURITY);
        
        extractSelection();
 
        pairedListWidget.labelLeftColumn(_constants.administrationDialogs_UnselectedClearances());
        pairedListWidget.labelRightColumn(_constants.administrationDialogs_SelectedClearances());
        pairedListWidget.loadData(myFullSecurityList, _securitySelection);
        _groupDisplay = GroupType.SECURITY;
    }
    
    private void extractSelection() {
        
        if (GroupType.SHARING.equals(_groupDisplay)) {
            
            _sharingSelection.clear();
            _sharingSelection.addAll(pairedListWidget.getListOnRight());
            
        } else if (GroupType.SECURITY.equals(_groupDisplay)) {
            
            _securitySelection.clear();
            _securitySelection.addAll(pairedListWidget.getListOnRight());
        }
    }
    
    private void initializeDate() {
        
        _tomorrow = new Date();
        
        CalendarUtil.resetTime(_tomorrow);
        
        CalendarUtil.addDaysToDate(_tomorrow, 1);
    }
}
