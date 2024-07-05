package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.Collection;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
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

import csi.client.gwt.csiwizard.panels.PairedListPanel;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.input_boxes.FilteredTrimmedTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.DataOperation;
import csi.server.common.enumerations.GroupType;

public class SharingInfoPopup implements HasHandlers, GroupInfoPopup {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, SharingInfoPopup> {
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
    FilteredTrimmedTextBox infoGroupNameTextBox;
    @UiField
    TextBox infoRemarksTextBox;
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
    
    private static String _txtTitle = _constants.administrationDialogs_SharingPopupTitle();
    private static String _txtHelpPath = _constants.administrationDialogs_SharingPopupHelpTarget();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private HandlerManager _handlerManager;
    
    private ValidatingDialog dialog;

    private GroupDisplay _groupInfo;
    private SharedItems _shared;

    public SharingInfoPopup(GroupDisplay groupDataIn, SharedItems sharedIn) {
        
        _handlerManager = new HandlerManager(this);

        dialog = uiBinder.createAndBindUi(this);
        
        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);
        
        dialog.hideOnCancel();

        _groupInfo = groupDataIn;
        _shared = sharedIn;

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
        
        _groupInfo.setType(GroupType.SHARING);
        _groupInfo.setName(infoGroupNameTextBox.getValue());
        _groupInfo.setRemarks(infoRemarksTextBox.getText());
        _groupInfo.setExternal(askRealm.getValue());
        _groupInfo.setParentGroups(_shared.formatList(null, pairedListWidget.getListOnRight(), null));
        
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
        
        Collection<StringEntry> myFullList = _shared.getGroupList(GroupType.SHARING);
        Collection<StringEntry> mySelection = new ArrayList<StringEntry>();

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
            instructionTextArea.setText(_constants.sharingInfoPopup_Instructions(_constants.sharingInfoPopup_UpdateInstructions()));

        } else {

            askRealm.setValue(false);
            instructionTextArea.setText(_constants.sharingInfoPopup_Instructions(_constants.sharingInfoPopup_NewInstructions()));
        }
        
        if (infoGroupNameTextBox.isEnabled()) {
            
            infoGroupNameTextBox.setRejectionMap(_shared._roleMap);
            infoGroupNameTextBox.setMode(ValidityCheckCapable.Mode.LOWERCASE);
            infoGroupNameTextBox.setRequired(true);
            
            dialog.addObject(infoGroupNameTextBox, true);
        }
        
        pairedListLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);

        pairedListLabel.setText(_constants.administrationDialogs_GroupGroupsPrompt());
        pairedListWidget.labelLeftColumn(_constants.administrationDialogs_UnselectedGroups());
        pairedListWidget.labelRightColumn(_constants.administrationDialogs_SelectedGroups());

        pairedListWidget.loadData(myFullList, mySelection);
        pairedListWidget.removeOnLeft(_shared._adminGroup);
        pairedListWidget.removeOnLeft(_shared._securityGroup);
        
        if (null != _groupInfo) {
            
            pairedListWidget.removeOnLeft(_groupInfo.getName());
        }

        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                boolean myNewFlag = updateGroupInfo();
                fireEvent(new DataChangeEvent(_groupInfo, (myNewFlag ? DataOperation.CREATE : DataOperation.UPDATE)));
                dialog.hide();
            }
        });
    }
}
