package csi.client.gwt.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.uibinder.client.UiField;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.BooleanResponse;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.list_boxes.SortListBox;
import csi.client.gwt.widget.list_boxes.SortingSet;
import csi.server.common.dto.system.FilteredUserRequest;
import csi.server.common.enumerations.UserSortMode;
import csi.server.common.enumerations.UserSortMode;
import csi.server.common.util.StringUtil;

import java.util.List;

/**
 * Created by centrifuge on 3/13/2018.
 */
public class UserListFilterDialog implements ValidityCheck {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, UserListFilterDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValidatingDialog dialog;

    private Button selectButton;
    private Button cancelButton;

    @UiField
    DialogInfoTextArea instructionTextArea;

    @UiField
    CheckBox userName1;
    @UiField
    CheckBox firstName1;
    @UiField
    CheckBox lastName1;
    @UiField
    CheckBox email1;
    @UiField
    CheckBox userName2;
    @UiField
    CheckBox firstName2;
    @UiField
    CheckBox lastName2;
    @UiField
    CheckBox email2;

    @UiField
    FilteredTextBox matchTextBox1;
    @UiField
    FilteredTextBox rejectTextBox1;
    @UiField
    FilteredTextBox matchTextBox2;
    @UiField
    FilteredTextBox rejectTextBox2;

    FilteredTextBox matchTextBox4 = new FilteredTextBox();

    FilteredTextBox rejectTextBox4 = new FilteredTextBox();
    @UiField
    SortListBox firstSort;
    @UiField
    SortListBox secondSort;
    @UiField
    SortListBox thirdSort;
    @UiField
    SortListBox fourthSort;

    private CheckBox[][] columnCheckBox;
    private FilteredTextBox[] patternTextBox;
    private SortingSet<UserSortMode> sortSelectionSet;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private BooleanResponse _handler = null;
    private FilteredUserRequest _filteredUserRequest = null;
    private String _txtDialogTitleString = null;
    private Integer _buttonWidth = 60;
    private boolean _isReady = false;
    private boolean _displayRequested = false;
    private boolean _isAdHoc;
    private boolean _okToApply = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking the Cancel button
    //
    private ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                exit(false);

            } catch (Exception myException) {

                Dialog.showException("UserListFilterDialog", 1, myException);
            }
        }
    };

    //
    // Handle clicking the Apply button
    //
    private ClickHandler handleApplyButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                extractDataFromDisplay();
                exit(true);

            } catch (Exception myException) {

                Dialog.showException("UserListFilterDialog", 2, myException);
            }
        }
    };

    //
    // Handle results from validity check
    //
    private ValidityReportEventHandler handleValidityReportEvent
            = new ValidityReportEventHandler() {
        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {

            try {

                selectButton.setEnabled(eventIn.getValidFlag());

            } catch (Exception myException) {

                Dialog.showException("UserListFilterDialog", 3, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public UserListFilterDialog(FilteredUserRequest filteredUserRequestIn, BooleanResponse handlerIn, boolean enforceRestrictionsIn) {

        _filteredUserRequest = filteredUserRequestIn;
        _handler = handlerIn;
        initializeObject();
    }

    public void show() {

        dialog.show();
    }

    public void hide() {

        dialog.hide();
    }

    public void destroy() {

        try {

            dialog.destroy();

            dialog = null;
            selectButton = null;
            cancelButton = null;

            _txtDialogTitleString = null;

        } catch (Exception myException) {

            Dialog.showException("UserListFilterDialog", 8, myException);
        }
    }

    @Override
    public void checkValidity() {

        boolean myOkToApply = checkStatus();

        if (myOkToApply != _okToApply) {

            _okToApply = myOkToApply;
            selectButton.setEnabled(_okToApply);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private void initializeObject() {

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        dialog = uiBinder.createAndBindUi(this);

        //
        // Load data into display
        //
        initializeDisplay();

        //
        // Set up the dialog cancel button
        //
        cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(handleCancelButtonClick);

        //
        // Set up the dialog save button
        //
        selectButton = dialog.getActionButton();
        selectButton.addClickHandler(handleApplyButtonClick);
        selectButton.setEnabled(_okToApply);
        dialog.beginMonitoring();
    }

    private void initializeDisplay() {

        _txtDialogTitleString = _constants.userFilter_UserListFilterDialog_Title();
        instructionTextArea.setText(_constants.userFilterDialog_Instructions());

        userName1.setText(_constants.userFilterDialog_usernameCheckbox1());
        firstName1.setText(_constants.userFilterDialog_firstnameCheckbox1());
        lastName1.setText(_constants.userFilterDialog_lastnameCheckbox1());
        email1.setText(_constants.userFilterDialog_emailCheckbox1());
        userName2.setText(_constants.userFilterDialog_usernameCheckbox2());
        firstName2.setText(_constants.userFilterDialog_firstnameCheckbox2());
        lastName2.setText(_constants.userFilterDialog_lastnameCheckbox2());
        email2.setText(_constants.userFilterDialog_emailCheckbox2());

        // Format the dialog title bar
        //
        dialog.defineHeader(_txtDialogTitleString, (String) null, true);

        if (null != _filteredUserRequest) {

            int[] myMasks = _filteredUserRequest.getMasks();
            String[][] myPatterns = _filteredUserRequest.getPatterns();
            UserSortMode[] mySorts = _filteredUserRequest.getSorts();
            List<UserSortMode> mySortList = UserSortMode.list();
            int myLimit = Math.min(null != myMasks ? myMasks.length : 0, null != myPatterns ? myPatterns.length : 0);

            columnCheckBox = new CheckBox[][] { new CheckBox[]{userName1, firstName1, lastName1, email1},
                                                new CheckBox[]{userName2, firstName2, lastName2, email2} };
            patternTextBox = new FilteredTextBox[] {matchTextBox1, rejectTextBox1,
                                                    matchTextBox2, rejectTextBox2};
            for (int i = 0; myLimit > i; i++) {

                CheckBox[] myCheckBoxes = columnCheckBox[i];
                int myIndex = 2 * i;
                int myMask = myMasks[i];

                patternTextBox[myIndex++].setText(StringUtil.patternFromSql(myPatterns[i][0]));
                patternTextBox[myIndex].setText(StringUtil.patternFromSql(myPatterns[i][1]));

                for (int j = 0; myCheckBoxes.length > j; j++) {

                    myCheckBoxes[j].setValue(0 != (1 & myMask));
                    myMask >>= 1;
                }
            }
            sortSelectionSet
                    = new SortingSet<UserSortMode>(new SortListBox[]{firstSort, secondSort, thirdSort, fourthSort},
                                                    UserSortMode.list(), mySorts);
        }
    }

    private void extractDataFromDisplay() {

        if (null != _filteredUserRequest) {

            if (null != patternTextBox) {

                String[][] myPatterns = new String[2][];
                int[] myMasks = new int[2];

                for (int i = 0; 2 > i; i++) {

                    CheckBox[] myCheckBoxes = columnCheckBox[i];
                    int myMask = 0;

                    myPatterns[i] = new String[2];
                    myPatterns[i][0] = StringUtil.patternToSql(patternTextBox[i * 2].getText());
                    myPatterns[i][1] = StringUtil.patternToSql(patternTextBox[(i * 2) + 1].getText());

                    for (int j = 0; myCheckBoxes.length > j; j++) {

                        if (myCheckBoxes[j].getValue()) {

                            myMask |= (1 << j);
                        }
                    }
                    myMasks[i] = myMask;
                }
                _filteredUserRequest.setMasks(myMasks);
                _filteredUserRequest.setPatterns(myPatterns);
            }
            if (null != sortSelectionSet) {

                UserSortMode[] mySorts = new UserSortMode[4];

                for (int i = 0; mySorts.length > i; i++) {

                    mySorts[i] = sortSelectionSet.getSelection(i);
                }
                _filteredUserRequest.setSorts(mySorts);
            }
        }
    }

    private void exit(boolean statusIn) {

        if (null != _handler) {

            _handler.onClick(statusIn);
        }
        destroy();
    }

    private boolean checkStatus() {

        boolean myTarget1 = userName1.getValue() || firstName1.getValue() || lastName1.getValue() || email1.getValue();
        boolean myTarget2 = userName2.getValue() || firstName2.getValue() || lastName2.getValue() || email2.getValue();

        if (myTarget1 || myTarget2) {

            String myMatch1 = matchTextBox1.getText().trim();
            String myMatch2 = matchTextBox2.getText().trim();
            String myReject1 = rejectTextBox1.getText().trim();
            String myReject2 = rejectTextBox2.getText().trim();
            boolean myPattern1 = ((null != myMatch1) && (0 < myMatch1.length())) || ((null != myReject1) && (0 < myReject1.length()));
            boolean myPattern2 = ((null != myMatch2) && (0 < myMatch2.length())) || ((null != myReject2) && (0 < myReject2.length()));

            return (myPattern1 == myTarget1) && (myPattern2 == myTarget2);

        } else {

            return false;
        }
    }
}
