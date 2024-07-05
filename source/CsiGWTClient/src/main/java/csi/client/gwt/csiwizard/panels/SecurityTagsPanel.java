package csi.client.gwt.csiwizard.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.PairedStringList;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.list_boxes.CsiDynamicStringListBox;
import csi.client.gwt.widget.list_boxes.CsiOverlayTextBox;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.enumerations.CsiColumnDelimiter;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 9/20/2016.
 */
public class SecurityTagsPanel<T extends MapByDataType> extends AbstractWizardPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Label classificationSourceLabel;
    RadioButton determinedByUserRB;
    RadioButton determinedByDataUserRB;
    RadioButton determinedByDataRB;
    RadioButton noTagsRB;
    Label fieldLabel;
    RadioButton orTagsRB;
    RadioButton andTagsRB;
    Label multiTagModeLabel;
    Label baseLabel;
    Label delimiterLabel;
    TextBox baseString;
    PairedStringList fieldNamePL;
    CsiDynamicStringListBox delimiterDropDown;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtTitle = _constants.securityTagsDialog_DialogTitle();
    private static final String _txtClassificationSource = _constants.securityTagsDialog_ClassificationSource();
    private static final String _txtClassifyByUser = _constants.securityTagsDialog_ClassifyByUser();
    private static final String _txtClassifyByDataUser = _constants.securityTagsDialog_ClassifyByDataUser();
    private static final String _txtClassifyByData = _constants.securityTagsDialog_ClassifyByData();
    private static final String _txtClassifyDefault = _constants.securityTagsDialog_NoTags();
    private static final String _txtSecurityFieldsPrompt = _constants.securityTagsDialog_SecurityFieldsPrompt();
    private static final String _txtUserSecurityPrompt = _constants.securityTagsDialog_UserSecurityPrompt();
    private static final String _txtDelimiterLabel = _constants.securityTagsDialog_DelimiterPrompt();
    private static final String _txtOr = _constants.orLabel();
    private static final String _txtAnd = _constants.andLabel();
    private static final String _txtMultiTagLabel = _constants.securityTagsDialog_MultiTagMode();

    private static List<ValuePair<String, String>> _delimiterChoices = new ArrayList<ValuePair<String, String>>();
    private static ClientStartupInfo _clientInfo = null;
    private static String _inputDelimiter = null;

    static {

        _delimiterChoices.add(new ValuePair<String, String>("", null));
        _delimiterChoices.add(new ValuePair<String, String>("space(   )", " "));
        _delimiterChoices.add(new ValuePair<String, String>("dash( - )", "-"));

        for (int i = 0; CsiColumnDelimiter.values().length > i; i++) {

            _delimiterChoices.add(new ValuePair(CsiColumnDelimiter.values()[i].getLabel(),
                                                String.valueOf(CsiColumnDelimiter.values()[i].getCharacter())));
        }
    }

    private SecurityTagsInfo _localSecurity = null;
    private List<String> _fullFieldList = null;
    private List<String> _securityFieldList = null;
    private boolean _ready = false;

    private Map<String, T> _idMap = null;
    private Map<String, T> _nameMap = null;
    private boolean _monitoring = false;
    private boolean _baseEdit = false;
    private String _baseTagString = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ClickHandler handleUnclassifiedClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localSecurity) {

                adjustDisplay(CapcoSource.USE_DEFAULT);
            }
        }
    };

    public ClickHandler handleDataSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localSecurity) {

                adjustDisplay(CapcoSource.DATA_ONLY);
            }
        }
    };

    public ClickHandler handleUserSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localSecurity) {

                adjustDisplay(CapcoSource.USER_ONLY);
            }
            baseString.setFocus(true);
        }
    };

    public ClickHandler handleDataAndUserSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localSecurity) {

                adjustDisplay(CapcoSource.USER_AND_DATA);
            }
            baseString.setFocus(true);
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SecurityTagsPanel(CanBeShownParent parentDialogIn, List<T> listIn, SecurityTagsInfo securityInfoIn)
            throws CentrifugeException {

        super(parentDialogIn, _txtTitle, true);

        if (null == _clientInfo) {

            _clientInfo = WebMain.getClientStartupInfo();

            if (null != _clientInfo) {

                _inputDelimiter = _clientInfo.getTagInputDelimiter();
                if ((null != _inputDelimiter) && (0 < _inputDelimiter.length())) {

                    if (1 < _inputDelimiter.length()) {

                        _inputDelimiter = _inputDelimiter.substring(0, 1);
                    }

                } else {

                    _inputDelimiter = null;
                }
            }
        }
        _localSecurity = (null != securityInfoIn) ? securityInfoIn.fullClone() : new SecurityTagsInfo();

        if (null != _localSecurity) {

            List<String> myOldSecurityFields = _localSecurity.getColumnList();

            _nameMap = new TreeMap<String, T>();
            _idMap = new TreeMap<String, T>();
            _fullFieldList = new ArrayList<String>();
            _securityFieldList = new ArrayList<String>();
            _baseTagString = _localSecurity.getBaseTagString();

            for (T myItem : listIn) {

                if (null != myItem) {

                    if (CsiDataType.String.equals(myItem.getDataType())) {

                        String myLocalId = myItem.getLocalId();
                        String myNameId = myItem.getName();

                        if ((null != myLocalId) && (null != myNameId)) {

                            _idMap.put(myItem.getLocalId(), myItem);
                            _nameMap.put(myItem.getName(), myItem);
                            _fullFieldList.add(myItem.getName());
                        }
                    }
                }
            }
            if (null != myOldSecurityFields) {

                for (String myId : myOldSecurityFields) {

                    T myField = (null != myId) ? _idMap.get(myId) : null;

                    if (null != myField) {

                        _securityFieldList.add(myField.getName());
                    }
                }
            }
            initializeObject();
            setMode(_localSecurity.getMode());
            adjustDisplay(getMode());
            _ready = true;
        }
    }

    public SecurityTagsPanel(CanBeShownParent parentDialogIn, List<T> listIn)
            throws CentrifugeException {

        this(parentDialogIn, listIn, null);
    }

    public SecurityTagsInfo getResults() {

        _localSecurity.reset();
        _localSecurity.setColumns(buildFieldList());
        if (CapcoSource.USER_AND_DATA.equals(_localSecurity.getMode())
                || CapcoSource.USER_ONLY.equals(_localSecurity.getMode())) {

            _localSecurity.setBaseTagString(baseString.getText());
        }
        _localSecurity.setDelimiterString(delimiterDropDown.getSelectedValue());
        _localSecurity.setOrTags(orTagsRB.getValue());

        return _localSecurity;
    }

    @Override
    public String getDialogTitle() {

        return _txtTitle;
    }

    @Override
    public String getInstructions(String buttonIn) {

        return _constants.securityTagsDialog_Instructions(buttonIn, _txtClassifyByUser, _txtClassifyByData,
                _txtClassifyByDataUser, _txtClassifyDefault);
    }

    @Override
    public String getText() {

        StringBuilder mybuffer = new StringBuilder();

        if (determinedByDataUserRB.getValue() || determinedByDataRB.getValue()) {

            mybuffer.append("");
        }
        mybuffer.append("|");

        if (noTagsRB.getValue()) {

            mybuffer.append("UNCLASSIFIED");//$NON-NLS-1$ //$NON-NLS-2$

        } else if (determinedByDataUserRB.getValue() || determinedByUserRB.getValue()) {

            mybuffer.append("");
        }

        return mybuffer.toString();
    }

    @Override
    public String getPanelTitle() {

        return _txtTitle;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public void destroy() {

        removeFromParent();
    }

    @Override
    public void enableInput() {

        determinedByDataRB.setEnabled(true);
        noTagsRB.setEnabled(true);
    }

    @Override
    public boolean isOkToLeave() {

        boolean myStatus = delimiterDropDown.isReady();

        if (myStatus) {

            myStatus = noTagsRB.getValue();

            if (!myStatus) {

                if (determinedByUserRB.getValue()) {

                    myStatus = isUserOk();

                } else if (determinedByDataRB.getValue()) {

                    myStatus = isDataOk();

                } else if (determinedByDataUserRB.getValue()) {

                    myStatus = isUserOk() && isDataOk();
                }
            }
        }
        return myStatus;
    }

    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        try {

            _monitoring = true;
            checkValidity();

        } catch (Exception myException) {

            Dialog.showException("SecurityTagsPanel", myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        classificationSourceLabel = new Label(_txtClassificationSource);

        determinedByUserRB = new RadioButton("ClassificationSource", _txtClassifyByUser); //$NON-NLS-1$
        determinedByUserRB.setValue(false, false);
        determinedByDataUserRB = new RadioButton("ClassificationSource", _txtClassifyByDataUser); //$NON-NLS-1$
        determinedByDataUserRB.setValue(false, false);
        determinedByDataRB = new RadioButton("ClassificationSource", _txtClassifyByData); //$NON-NLS-1$
        determinedByDataRB.setValue(true, false);
        noTagsRB = new RadioButton("ClassificationSource", _txtClassifyDefault); //$NON-NLS-1$
        noTagsRB.setValue(false, false);
        orTagsRB = new RadioButton("MultiTagMode", _txtOr); //$NON-NLS-1$
        orTagsRB.setValue(true, false);
        andTagsRB = new RadioButton("MultiTagMode", _txtAnd); //$NON-NLS-1$
        andTagsRB.setValue(false, false);
        multiTagModeLabel = new Label(_txtMultiTagLabel);

        fieldLabel = new Label(_txtSecurityFieldsPrompt);

        baseLabel = new Label(_txtUserSecurityPrompt);
        baseString = new TextBox();
        delimiterLabel = new Label(_txtDelimiterLabel);
        delimiterDropDown = new CsiDynamicStringListBox();
        delimiterDropDown.initializeDropdown(CsiOverlayTextBox.ValidationMode.CHARACTER, _delimiterChoices);
        delimiterDropDown.setUserDisplay(_constants.user());
        if ((null != _inputDelimiter) && (0 < _inputDelimiter.length())) {

            delimiterDropDown.setSelectedValue(_inputDelimiter);
        }

        fieldNamePL = new PairedStringList(_fullFieldList, _securityFieldList);

        if ((null != _fullFieldList) && (0 < _fullFieldList.size())) {

            determinedByDataRB.setValue(true, false);
            noTagsRB.setValue(false, false);

        } else {

            determinedByDataRB.setValue(false, false);
            noTagsRB.setValue(true, false);

            determinedByDataRB.setEnabled(false);
            determinedByDataUserRB.setEnabled(false);
        }

        add(classificationSourceLabel);
        add(determinedByUserRB);
        add(determinedByDataUserRB);
        add(determinedByDataRB);
        add(noTagsRB);
        add(fieldNamePL);
        add(fieldLabel);
        add(orTagsRB);
        add(andTagsRB);
        add(multiTagModeLabel);
        add(baseLabel);
        add(baseString);
        add(delimiterLabel);
        add(delimiterDropDown);
    }

    @Override
    protected void layoutDisplay() {

        int myRequestedHeight = (16 * Dialog.intLabelHeight) + (3 * Dialog.intMargin);
        int myHeight = (myRequestedHeight <= _height) ? myRequestedHeight : _height;
        int mySpacing = (myHeight == myRequestedHeight) ? Dialog.intMargin : (_height - (16 * Dialog.intLabelHeight)) / 2;
// TODO:        int myTop = (_height - myHeight) / 2;
        int myTop = 0;
        int mySmallStep = Dialog.intLabelHeight;
        int myBigStep = (0 < mySpacing) ? (mySmallStep + mySpacing) : mySmallStep;
        int myCenterRight = (_width * 2) / 3;
        int myCenterLeft = _width / 3;
        int myListWidth = _width - (3 * Dialog.intMargin);
        int myListHeight = 5 * Dialog.intTextBoxHeight;
        int myLargeWidth = ((_width * 3) / 4) - 10;
        int mySmallWidth = (_width / 4) - 10;
        int myTextBoxWidth = myLargeWidth - ((7 * Dialog.intMargin) / 2);
        int myTextBoxHeight = Dialog.intLabelHeight;
        int myDropWidth = mySmallWidth;
        int myDropHeight = Dialog.intTextBoxHeight;

        setWidgetTopHeight(classificationSourceLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(classificationSourceLabel, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(determinedByUserRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(determinedByUserRB, Dialog.intMargin, Style.Unit.PX, myCenterRight, Style.Unit.PX);
        setWidgetTopHeight(determinedByDataUserRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(determinedByDataUserRB, myCenterLeft, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(determinedByDataRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(determinedByDataRB, Dialog.intMargin, Style.Unit.PX, myCenterRight, Style.Unit.PX);
        setWidgetTopHeight(noTagsRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(noTagsRB, myCenterLeft, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(fieldLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(fieldLabel, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(fieldNamePL, myTop, Style.Unit.PX, myListHeight, Style.Unit.PX);
        setWidgetLeftRight(fieldNamePL, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);
        fieldNamePL.setPixelSize(myListWidth, myListHeight);

        myTop += ((mySmallStep / 4) + myListHeight);
        setWidgetTopHeight(orTagsRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftWidth(orTagsRB, ((_width * 3) / 8), Style.Unit.PX, (_width / 8), Style.Unit.PX);
        setWidgetTopHeight(andTagsRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftWidth(andTagsRB, (_width / 2), Style.Unit.PX, (_width / 8), Style.Unit.PX);
        setWidgetTopHeight(multiTagModeLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(multiTagModeLabel, ((_width * 5) / 8), Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(baseLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftWidth(baseLabel, Dialog.intMargin, Style.Unit.PX, myLargeWidth, Style.Unit.PX);
        setWidgetTopHeight(delimiterLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetRightWidth(delimiterLabel, Dialog.intMargin, Style.Unit.PX, mySmallWidth, Style.Unit.PX);

        myTop += myBigStep;
        baseString.setPixelSize(myTextBoxWidth, myTextBoxHeight);
        setWidgetTopHeight(baseString, myTop, Style.Unit.PX, (Dialog.intTextBoxHeight + Dialog.intMargin), Style.Unit.PX);
        setWidgetLeftWidth(baseString, Dialog.intMargin, Style.Unit.PX, myLargeWidth, Style.Unit.PX);
        delimiterDropDown.setPixelSize(myDropWidth, myDropHeight);
        setWidgetTopHeight(delimiterDropDown, myTop, Style.Unit.PX, (Dialog.intTextBoxHeight + Dialog.intMargin), Style.Unit.PX);
        setWidgetRightWidth(delimiterDropDown, Dialog.intMargin, Style.Unit.PX, mySmallWidth, Style.Unit.PX);
    }

    @Override
    protected void wireInHandlers() {

        noTagsRB.addClickHandler(handleUnclassifiedClick);
        determinedByDataRB.addClickHandler(handleDataSourceClick);
        determinedByDataUserRB.addClickHandler(handleDataAndUserSourceClick);
        determinedByUserRB.addClickHandler(handleUserSourceClick);
    }

    private void setMode(CapcoSource modeIn) {

        determinedByDataRB.setValue(false);
        determinedByDataUserRB.setValue(false);
        determinedByUserRB.setValue(false);
        noTagsRB.setValue(false);

        switch (modeIn) {

            case DATA_ONLY:

                determinedByDataRB.setValue(true);
                break;

            case USER_AND_DATA:

                determinedByDataUserRB.setValue(true);
                break;

            case USER_ONLY:

                determinedByUserRB.setValue(true);
                break;

            default:

                noTagsRB.setValue(true);
                break;
        }
    }

    private CapcoSource getMode() {

        CapcoSource mySource = CapcoSource.USE_DEFAULT;

        if (determinedByDataRB.getValue()) {

            return CapcoSource.DATA_ONLY;

        } else if (determinedByDataUserRB.getValue()) {

            return CapcoSource.USER_AND_DATA;

        } else if (determinedByUserRB.getValue()) {

            return CapcoSource.USER_ONLY;
        }
        return mySource;
    }

    private void adjustDisplay(CapcoSource modeIn) {

        if (null != modeIn) {

            CapcoSource myOldMode = _localSecurity.getMode();

            switch (modeIn) {

                case DATA_ONLY:

                    fieldLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    fieldNamePL.setEnabled(true);
                    baseLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    baseString.setEnabled(false);
                    if (_baseEdit) {

                        _baseTagString = baseString.getText();
                    }
                    _baseEdit = false;
                    baseString.setText(null);
                    break;

                case USER_ONLY:

                    fieldLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    fieldNamePL.setEnabled(false);
                    baseLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    baseString.setEnabled(true);
                    if (!_baseEdit) {

                        baseString.setText(_baseTagString);
                    }
                    _baseEdit = true;
                    break;

                case USER_AND_DATA:

                    fieldLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    fieldNamePL.setEnabled(true);
                    baseLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    baseString.setEnabled(true);
                    if (!_baseEdit) {

                        baseString.setText(_baseTagString);
                    }
                    _baseEdit = true;
                    break;

                default:

                    fieldLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    fieldNamePL.setEnabled(false);
                    baseLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    baseString.setEnabled(false);
                    if (_baseEdit) {

                        _baseTagString = baseString.getText();
                    }
                    _baseEdit = false;
                    baseString.setText(null);
                    break;
            }
            _localSecurity.setMode(modeIn);
        }
    }

    private List<String> buildFieldList() {

        List<StringEntry> myDisplayList = fieldNamePL.getListOnRight();
        List<String> myListOut = new ArrayList<String>();

        if ((null != myDisplayList) && (0 < myDisplayList.size())) {

            for (StringEntry myName : myDisplayList) {

                T myItem = _nameMap.get(myName.getValue());

                if (null != myItem) {

//                    String myId = (myItem instanceof FieldDef) ? myItem.getLocalId() : myItem.getColumnKey();
                    myListOut.add(myItem.getColumnKey());
                }
            }
        }
        return myListOut;
    }

    private void checkValidity() {

        fireEvent(new ValidityReportEvent(isOkToLeave()));

        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }

    private boolean isUserOk() {

        return ((null != baseString.getText()) && (0 < baseString.getText().trim().length()));
    }

    private boolean isDataOk() {

        String myDelimiter = delimiterDropDown.isValid()
                                ? (0 < delimiterDropDown.getSelectedIndex())
                                        ? delimiterDropDown.getSelectedValue()
                                        : null
                                : null;

        return (0 < fieldNamePL.getListOnRight().size()) && (null != myDelimiter) && (0 < myDelimiter.length());
    }
}