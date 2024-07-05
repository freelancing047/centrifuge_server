package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.Collection;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.panels.PairedListPanel;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.input_boxes.FilteredTrimmedTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.DataOperation;
import csi.server.common.enumerations.GroupType;

public class SecurityInfoPopup implements HasHandlers, GroupInfoPopup {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, SecurityInfoPopup> {
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
    FullSizeLayoutPanel topLevelPanel;
    @UiField
    FullSizeLayoutPanel layoutPanel;
    @UiField
    FullSizeLayoutPanel capcoPanel;
    @UiField
    FullSizeLayoutPanel impliedClearancePanel;
    @UiField
    FullSizeLayoutPanel infoPanel;
    @UiField
    FilteredTrimmedTextBox infoGroupNameTextBox;
    @UiField
    FilteredTextBox portionTextBox;
    @UiField
    TextBox infoRemarksTextBox;
    @UiField
    CsiStringListBox capcoListBox;
    @UiField
    Label pairedListLabel;
    @UiField
    PairedListPanel<StringEntry> pairedListWidget;
    @UiField
    TextArea instructionTextArea;
    @UiField
    CheckBox askRealm;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtTitle = _constants.administrationDialogs_SecurityPopupTitle();
    private static String _txtHelpPath = _constants.administrationDialogs_SecurityPopupHelpTarget();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private HandlerManager _handlerManager;

    private ValidatingDialog dialog;

    private GroupDisplay _groupInfo;
    private SharedItems _shared;
    private boolean _doCapco = false;
    private boolean _doLabel = false;
    private String _portionText = null;
    private CapcoSection _section = CapcoSection.OTHER;

    public SecurityInfoPopup(GroupDisplay groupDataIn, SharedItems sharedIn) {

        _handlerManager = new HandlerManager(this);
        _shared = sharedIn;
        _doCapco = _shared.doCapco();

        dialog = uiBinder.createAndBindUi(this);

        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);

        dialog.hideOnCancel();

        _groupInfo = groupDataIn;

        if (null != _groupInfo) {

            _section = _groupInfo.getSection();
            _doLabel = !CapcoSection.OTHER.equals(_section);
            if (_doLabel) {
                _portionText = _groupInfo.getPortionText();
            }
        }

        createWidgets();
    }

    public void show() {

        dialog.show(60);

        DeferredCommand.add(new Command() {
            public void execute() {
                setFocus();
            }
        });
    }

    public GroupDisplay getGroupInfo() {

        return _groupInfo;
    }

    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addDataChangeEventHandler(
            DataChangeEventHandler handler) {
        return _handlerManager.addHandler(DataChangeEvent.type, handler);
    }

    private boolean updateGroupInfo() {

        boolean myNewDataFlag = false;

        if (null == _groupInfo) {

            _groupInfo = new GroupDisplay();
            _groupInfo.setId(null);
            myNewDataFlag = true;
        }

        _groupInfo.setType(GroupType.SECURITY);
        _groupInfo.setName(infoGroupNameTextBox.getValue());
        _groupInfo.setRemarks(infoRemarksTextBox.getText());
        _groupInfo.setExternal(askRealm.getValue());
        _groupInfo.setParentGroups(_shared.formatList(null, pairedListWidget.getListOnRight(), null));
        _groupInfo.setSection(_section);
        _groupInfo.setPortionText(_doLabel ? portionTextBox.getText() : null);

        return myNewDataFlag;
    }

    private void setFocus() {

        if (infoGroupNameTextBox.isEnabled()) {

            infoGroupNameTextBox.setFocus(true);

        } else {

            infoRemarksTextBox.setFocus(true);
        }
    }

    private void createWidgets() {

        Collection<StringEntry> myFullList = _shared.getGroupList(GroupType.SECURITY);
        Collection<StringEntry> mySelection = new ArrayList<StringEntry>();
        String myInitialInstructions;
        String myExtendedInstructions = _doCapco ? _constants.capcoInfoPopup_Instructions() : "";

        instructionTextArea.setReadOnly(true);
        instructionTextArea.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        instructionTextArea.getElement().getStyle().setProperty("resize", "none");
        instructionTextArea.getElement().getStyle().setBackgroundColor("white");
        instructionTextArea.getElement().getStyle().setBorderColor("white");
        instructionTextArea.getElement().getStyle().setColor(Dialog.txtInfoColor);

        if (null != _groupInfo) {

            String myGroupString = _groupInfo.getParentGroups();

            askRealm.setValue(_groupInfo.getExternal());

            if ((null != myGroupString) && (0 < myGroupString.length())) {

                String[] myGroups = myGroupString.split(", ");

                if (0 < myGroups.length) {

                    for (String myGroup : myGroups) {

                        if ((null != myGroup) && (0 < myGroup.length())) {

                            mySelection.add(new StringEntry(myGroup));
                        }
                    }
                }
            }

            if ((null != _groupInfo.getName()) && (0 < _groupInfo.getName().length())) {
                infoGroupNameTextBox.setText(_groupInfo.getName());
                infoGroupNameTextBox.setEnabled(false);
            }

            if ((null != _groupInfo.getRemarks()) && (0 < _groupInfo.getRemarks().length())) {
                infoRemarksTextBox.setText(_groupInfo.getRemarks());
            }
            myInitialInstructions = _constants.securityInfoPopup_UpdateInstructions();

        } else {

            askRealm.setValue(false);
            myInitialInstructions = _constants.securityInfoPopup_NewInstructions();
        }
        instructionTextArea.setText(_constants.securityInfoPopup_Instructions(myInitialInstructions, myExtendedInstructions));

        if (infoGroupNameTextBox.isEnabled()) {

            infoGroupNameTextBox.setRejectionMap(_shared._roleMap);
            infoGroupNameTextBox.setMode(ValidityCheckCapable.Mode.LOWERCASE);
            infoGroupNameTextBox.setRequired(true);

            dialog.addObject(infoGroupNameTextBox, true);
        }

        pairedListLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);

        pairedListLabel.setText(_constants.administrationDialogs_EncapsulateClearancesPrompt());
        pairedListWidget.labelLeftColumn(_constants.administrationDialogs_UnselectedClearances());
        pairedListWidget.labelRightColumn(_constants.administrationDialogs_SelectedClearances());

        pairedListWidget.loadData(myFullList, mySelection);
        pairedListWidget.removeOnLeft(_shared._adminGroup);
        pairedListWidget.removeOnLeft(_shared._securityGroup);

        if (null != _groupInfo) {

            pairedListWidget.removeOnLeft(_groupInfo.getName());
        }

        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                try {

                    boolean myNewFlag = updateGroupInfo();
                    fireEvent(new DataChangeEvent(_groupInfo, (myNewFlag ? DataOperation.CREATE : DataOperation.UPDATE)));
                    dialog.hide();

                } catch (Exception myException) {

                    GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                    Dialog.showException(myException);
                }
            }
        });

        if (_doCapco) {

            showCapco();

        } else {

            hideCapco();
        }
    }

    private void showCapco() {

        capcoPanel.setVisible(true);

        layoutPanel.setWidgetTopHeight(impliedClearancePanel, 165, Style.Unit.PX, 275, Style.Unit.PX);
        layoutPanel.setWidgetTopHeight(infoPanel, 0, Style.Unit.PX, 440, Style.Unit.PX);
        topLevelPanel.setWidgetTopHeight(layoutPanel, 0, Style.Unit.PX, 440, Style.Unit.PX);
        topLevelPanel.setHeight("440px");

        dialog.setBodyHeight("440px");

        for (CapcoSection mySection : CapcoSection.values()) {

            capcoListBox.addItem(mySection.getLabel());
        }
        capcoListBox.setSelectedValue(_section.getLabel());

        capcoListBox.addSelectionChangedHandler(new SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {

                String myChoice = capcoListBox.getSelectedValue();
                CapcoSection mySection = (null != myChoice) ? CapcoSection.getFromLabel(myChoice) : null;

                recordSectionChoice(mySection);
            }
        });
        portionTextBox.setText(_portionText);
        recordSectionChoice(_section);
    }

    private void recordSectionChoice(CapcoSection sectionIn) {

        if (_doLabel) {

            _portionText = portionTextBox.getText();
        }

        _section = (null != sectionIn) ? sectionIn : CapcoSection.OTHER;
        _doLabel = !CapcoSection.OTHER.equals(_section);

        if (_doLabel) {

            portionTextBox.setText(_portionText);
            portionTextBox.setEnabled(true);

        } else {

            portionTextBox.setText(null);
            portionTextBox.setEnabled(false);
        }
    }

    private void hideCapco() {

        capcoPanel.setVisible(false);

        layoutPanel.setWidgetTopHeight(impliedClearancePanel, 90, Style.Unit.PX, 275, Style.Unit.PX);
        layoutPanel.setWidgetTopHeight(infoPanel, 0, Style.Unit.PX, 365, Style.Unit.PX);
        topLevelPanel.setWidgetTopHeight(layoutPanel, 0, Style.Unit.PX, 365, Style.Unit.PX);
        topLevelPanel.setHeight("365px");

        dialog.setBodyHeight("365px");
    }
}
