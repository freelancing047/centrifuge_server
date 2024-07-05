package csi.client.gwt.csiwizard.panels;

import java.util.ArrayList;
import java.util.Comparator;
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
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.security.CapcoInfo;

/**
 * Created by centrifuge on 11/3/2014.
 */
public class CapcoPanel<T extends MapByDataType> extends AbstractWizardPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Label classificationSourceLabel;
    RadioButton determinedByUserRB;
    RadioButton determinedByDataUserRB;
    RadioButton determinedByDataRB;
    RadioButton useDefaultRB;
    Label capcoFieldLabel;
    Label capcoUserLabel;
    TextBox capcoUserString;
    PairedStringList fieldNamePL;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtTitle = _constants.capcoDialog_DialogTitle();
    private static final String _txtClassificationSource = _constants.capcoDialog_ClassificationSource();
    private static final String _txtClassifyByUser = _constants.capcoDialog_ClassifyByUser();
    private static final String _txtClassifyByDataUser = _constants.capcoDialog_ClassifyByDataUser();
    private static final String _txtClassifyByData = _constants.capcoDialog_ClassifyByData();
    private static final String _txtClassifyDefault = _constants.capcoDialog_UseDefault();
    private static final String _txtSecurityFieldsPrompt = _constants.capcoDialog_SecurityFieldsPrompt();
    private static final String _txtUserSecurityPrompt = _constants.capcoDialog_UserSecurityPrompt();

    private static String _txtUsage = null;

    private CapcoInfo _localCapco = null;
    private List<String> _fullFieldList = null;
    private List<String> _capcoFieldList = null;
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

            if (null != _localCapco) {

                adjustDisplay(CapcoSource.USE_DEFAULT);
            }
        }
    };

    public ClickHandler handleDataSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localCapco) {

                adjustDisplay(CapcoSource.DATA_ONLY);
            }
        }
    };

    public ClickHandler handleUserSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localCapco) {

                adjustDisplay(CapcoSource.USER_ONLY);
            }
            capcoUserString.setFocus(true);
        }
    };

    public ClickHandler handleDataAndUserSourceClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _localCapco) {

                adjustDisplay(CapcoSource.USER_AND_DATA);
            }
            capcoUserString.setFocus(true);
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CapcoPanel(CanBeShownParent parentDialogIn, List<T> listIn, CapcoInfo capcoIn)
            throws CentrifugeException {

        super(parentDialogIn, _txtTitle, true);
        _localCapco = (null != capcoIn) ? capcoIn.fullClone() : new CapcoInfo();

        if (null != _localCapco) {

            List<String> myOldCapcoFields = _localCapco.getSecurityFields();

            _nameMap = new TreeMap<String, T>();
            _idMap = new TreeMap<String, T>();
            _fullFieldList = new ArrayList<String>();
            _capcoFieldList = new ArrayList<String>();
            _baseTagString = _localCapco.getUserPortion();

            for (T myItem : listIn) {

                if (null != myItem) {

                    if (CsiDataType.String.equals(myItem.getDataType())) {

                        String myLocalId = myItem.getLocalId();
                        String myNameId = myItem.getName();

                        if ((null != myLocalId) && (null != myNameId)) {

                            _idMap.put(myLocalId, myItem);
                            _nameMap.put(myNameId, myItem);
                            _fullFieldList.add(myItem.getName());
                        }
                    }
                }
            }
            if (null != myOldCapcoFields) {

                for (String myId : myOldCapcoFields) {

                    T myField = (null != myId) ? _idMap.get(myId) : null;

                    if (null != myField) {

                        _capcoFieldList.add(myField.getName());
                    }
                }
            }
            initializeObject();
            setMode(_localCapco.getMode());
            adjustDisplay(getMode());
            _ready = true;
        }
    }

    public CapcoPanel(CanBeShownParent parentDialogIn, List<T> listIn)
            throws CentrifugeException {

        this(parentDialogIn, listIn, null);
    }

    public CapcoInfo getResults() {

        _localCapco.setSecurityFields(buildCapcoList());
        if (CapcoSource.USER_AND_DATA.equals(_localCapco.getMode())
                || CapcoSource.USER_ONLY.equals(_localCapco.getMode())) {

            _localCapco.setUserPortion(capcoUserString.getText());
        }

        return _localCapco;
    }

    @Override
    public String getDialogTitle() {

        return _txtTitle;
    }

    @Override
    public String getInstructions(String buttonIn) {

        if (null == _txtUsage) {

            if (WebMain.getClientStartupInfo().isProvideBanners()) {

                if (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()) {

                    _txtUsage = _constants.bannerAndUserAccess();

                } else {

                    _txtUsage = _constants.banner();
                }

            } else if (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()) {

                _txtUsage = _constants.userAccess();
            }
        }
        return _constants.capcoDialog_Instructions(buttonIn, _txtUsage, _txtClassifyByUser, _txtClassifyByData,
                _txtClassifyByDataUser, _txtClassifyDefault);
    }

    @Override
    public String getPanelTitle() {

        return _txtTitle;
    }

    @Override
    public String getText() {

        StringBuilder mybuffer = new StringBuilder();

        if (determinedByDataUserRB.getValue() || determinedByDataRB.getValue()) {

            mybuffer.append("");
        }
        mybuffer.append("|");

        if (useDefaultRB.getValue()) {

            mybuffer.append("UNCLASSIFIED");//$NON-NLS-1$ //$NON-NLS-2$

        } else if (determinedByDataUserRB.getValue() || determinedByUserRB.getValue()) {

            mybuffer.append("");
        }

        return mybuffer.toString();
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
        useDefaultRB.setEnabled(true);
    }

    public List<String> getList() {

        return buildCapcoList();
    }

    @Override
    public boolean isOkToLeave() {

        boolean myStatus = useDefaultRB.getValue() ;

        if (!myStatus) {

            if (determinedByUserRB.getValue()) {

                myStatus = isUserOk();

            } else if (determinedByDataRB.getValue()) {

                myStatus = isDataOk();

            } else if (determinedByDataUserRB.getValue()) {

                myStatus = isUserOk() && isDataOk();
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

        int myListHeight = 4 * Dialog.intTextBoxHeight;
        int myFieldWidth = (_width - Dialog.intMargin) / 2;
        Comparator myComparator = (null != inputCellIn) ? inputCellIn.getComparator() : null;

        classificationSourceLabel = new Label(_txtClassificationSource);

        determinedByUserRB = new RadioButton("ClassificationSource", _txtClassifyByUser); //$NON-NLS-1$
        determinedByUserRB.setValue(false, false);
        determinedByDataUserRB = new RadioButton("ClassificationSource", _txtClassifyByDataUser); //$NON-NLS-1$
        determinedByDataUserRB.setValue(false, false);
        determinedByDataRB = new RadioButton("ClassificationSource", _txtClassifyByData); //$NON-NLS-1$
        determinedByDataRB.setValue(true, false);
        useDefaultRB = new RadioButton("ClassificationSource", _txtClassifyDefault); //$NON-NLS-1$
        useDefaultRB.setValue(false, false);

        capcoFieldLabel = new Label(_txtSecurityFieldsPrompt);

        capcoUserLabel = new Label(_txtUserSecurityPrompt);
        capcoUserString = new TextBox();

        fieldNamePL = new PairedStringList(_fullFieldList, _capcoFieldList);

        if ((null != _fullFieldList) && (0 < _fullFieldList.size())) {

            determinedByDataRB.setValue(true, false);
            useDefaultRB.setValue(false, false);

        } else {

            determinedByDataRB.setValue(false, false);
            useDefaultRB.setValue(true, false);

            determinedByDataRB.setEnabled(false);
            determinedByDataUserRB.setEnabled(false);
        }

        add(classificationSourceLabel);
        add(determinedByUserRB);
        add(determinedByDataUserRB);
        add(determinedByDataRB);
        add(useDefaultRB);
        add(fieldNamePL);
        add(capcoFieldLabel);
        add(capcoUserLabel);
        add(capcoUserString);
    }

    @Override
    protected void layoutDisplay() {

        int myRequestedHeight = (15 * Dialog.intLabelHeight) + (2 * Dialog.intMargin);
        int myHeight = (myRequestedHeight <= _height) ? myRequestedHeight : _height;
        int mySpacing = (myHeight == myRequestedHeight) ? Dialog.intMargin : (_height - (15 * Dialog.intLabelHeight)) / 2;
        int myTop = (_height - myHeight) / 2;
        int mySmallStep = Dialog.intLabelHeight;
        int myBigStep = (0 < mySpacing) ? (mySmallStep + mySpacing) : mySmallStep;
        int myCenterRight = (_width * 2) / 3;
        int myCenterLeft = _width / 3;
        int myListWidth = _width - (3 * Dialog.intMargin);
        int myListHeight = 5 * Dialog.intTextBoxHeight;
        int myTextBoxWidth = _width - ((7 * Dialog.intMargin) / 2);

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
        setWidgetTopHeight(useDefaultRB, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(useDefaultRB, myCenterLeft, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(capcoFieldLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(capcoFieldLabel, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(fieldNamePL, myTop, Style.Unit.PX, myListHeight, Style.Unit.PX);
        setWidgetLeftRight(fieldNamePL, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);
        fieldNamePL.setPixelSize(myListWidth, myListHeight);

        myTop += ((mySmallStep / 2) + myListHeight);
        setWidgetTopHeight(capcoUserLabel, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
        setWidgetLeftRight(capcoUserLabel, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);

        myTop += myBigStep;
        capcoUserString.setPixelSize(myTextBoxWidth, Dialog.intLabelHeight);
        setWidgetTopHeight(capcoUserString, myTop, Style.Unit.PX, (Dialog.intTextBoxHeight + Dialog.intMargin), Style.Unit.PX);
        setWidgetLeftRight(capcoUserString, Dialog.intMargin, Style.Unit.PX, Dialog.intMargin, Style.Unit.PX);
    }

    @Override
    protected void wireInHandlers() {

        useDefaultRB.addClickHandler(handleUnclassifiedClick);
        determinedByDataRB.addClickHandler(handleDataSourceClick);
        determinedByDataUserRB.addClickHandler(handleDataAndUserSourceClick);
        determinedByUserRB.addClickHandler(handleUserSourceClick);
    }

    private void setMode(CapcoSource modeIn) {

        determinedByDataRB.setValue(false);
        determinedByDataUserRB.setValue(false);
        determinedByUserRB.setValue(false);
        useDefaultRB.setValue(false);

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

                useDefaultRB.setValue(true);
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

            CapcoSource myOldMode = _localCapco.getMode();

            switch (modeIn) {

                case DATA_ONLY:

                    capcoFieldLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    fieldNamePL.setEnabled(true);
                    capcoUserLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    capcoUserString.setEnabled(false);
                    if (_baseEdit) {

                        _baseTagString = capcoUserString.getText();
                    }
                    _baseEdit = false;
                    capcoUserString.setText(null);
                    break;

                case USER_ONLY:

                    capcoFieldLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    fieldNamePL.setEnabled(false);
                    capcoUserLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    capcoUserString.setEnabled(true);
                    if (!_baseEdit) {

                        capcoUserString.setText(_baseTagString);
                    }
                    _baseEdit = true;
                    break;

                case USER_AND_DATA:

                    capcoFieldLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    fieldNamePL.setEnabled(true);
                    capcoUserLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    capcoUserString.setEnabled(true);
                    if (!_baseEdit) {

                        capcoUserString.setText(_baseTagString);
                    }
                    _baseEdit = true;
                    break;

                default:

                    capcoFieldLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    fieldNamePL.setEnabled(false);
                    capcoUserLabel.getElement().getStyle().setColor(Dialog.txtDisabledColor);
                    capcoUserString.setEnabled(false);
                    if (_baseEdit) {

                        _baseTagString = capcoUserString.getText();
                    }
                    _baseEdit = false;
                    capcoUserString.setText(null);
                    break;
            }
            _localCapco.setMode(modeIn);
        }
    }

    private List<String> buildCapcoList() {

        List<StringEntry> myDisplayList = fieldNamePL.getListOnRight();
        List<String> myListOut = new ArrayList<String>();

        if ((null != myDisplayList) && (0 < myDisplayList.size())) {

            for (StringEntry myName : myDisplayList) {

                T myItem = _nameMap.get(myName.getValue());

                if (null != myItem) {

                    myListOut.add(myItem.getLocalId());
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

        String myCapcoStringIn = capcoUserString.getText();

        if (null != myCapcoStringIn) {

            String myCapcoStringOut = myCapcoStringIn.trim().toUpperCase();
            capcoUserString.setText(myCapcoStringOut);

            return (0 < myCapcoStringOut.length());
        }
        return false;
    }

    private boolean isDataOk() {

        return (0 < fieldNamePL.getListOnRight().size());
    }
}
