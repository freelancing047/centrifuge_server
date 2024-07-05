package csi.client.gwt.csi_resource;


import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.events.ResourceSelectionEvent;
import csi.client.gwt.events.ResourceSelectionEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.Button;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                   Selector Used for DataView and Template Selection                    //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public abstract class ResourceSelectorDialog extends WatchingParent {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ResourceSelectorDialogUiBinder extends UiBinder<Widget, ResourceSelectorDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    Dialog dialog;

    @UiField(provided = true)
    protected ResourceSelectorPanel<ResourceBasics> selectorWidget;

    protected ResourceSelectorDialog thisDialog = null;

    protected Button selectButton;
    protected Button cancelButton;
    protected Button nextButton;
    protected Button filterButton;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static ResourceSelectorDialogUiBinder uiBinder = GWT.create(ResourceSelectorDialogUiBinder.class);

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected final String _txtSelectionListLabel = _constants.resourceSelector_ListLabel();
    protected final String _txtSelectionNameLabel = _constants.resourceSelector_NameLabel();
    protected final String _txtSelectionRemarksLabel = _constants.resourceSelector_RemarksLabel();

    protected String _txtDialogTitleString = null;
    protected String _txtOverwriteDialogTitleString = null;
    protected String _txtOverwriteDialogInfoString = null;
    protected Integer _buttonWidth = 60;

    private AclResourceType _resourceType = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverWriteIn);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Forward response to request for overwrite resource list to resource widget
    // -- wired in during initialization
    //
    protected VortexEventHandler<List<List<ResourceBasics>>> handleTripleListRequestResponse;

    //
    // Forward response to request for open resource list to resource widget
    // -- wired in during initialization
    //
    protected VortexEventHandler<List<ResourceBasics>> handleSingleListRequestResponse;

    protected VortexEventHandler<List<ResourceBasics>> handleFilteredListRequestResponse;

    //
    // Handle clicking the Cancel button
    //
    protected ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                onCancel();
                //
                // Hide dialog
                //
                destroy();

            } catch (Exception myException) {

                Display.error("ResourceSelectorDialog", 1, myException);
            }
        }
    };

    //
    // Handle clicking the Edit Filters button
    //
    /*
    protected ClickHandler handleFilterButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            (new ResourceFilterListDialog()).show(thisDialog);
        }
    };
    */
    //
    // Handle selection made from resource list
    //
    protected ResourceSelectionEventHandler<ResourceBasics> handleResourceSelection
            = new ResourceSelectionEventHandler<ResourceBasics>() {
        @Override
        public void onResourceSelection(ResourceSelectionEvent<ResourceBasics> eventIn) {

            try {

                ResourceBasics mySelection = eventIn.getSelection();

                if (null == mySelection) {

                    mySelection = new ResourceBasics(eventIn.getName(), eventIn.getRemarks());
                }

                handleSelectionMade(mySelection, eventIn.forceOverwrite());

            } catch (Exception myException) {

                Display.error("ResourceSelectorDialog", 2, myException);
            }
        }
    };

    protected void onCancel() {

    }

    //
    // Handle results from validity check
    //
    protected ValidityReportEventHandler handleValidityReportEvent = new ValidityReportEventHandler() {
        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {

            try {

                selectButton.setEnabled(eventIn.getValidFlag());


            } catch (Exception myException) {

                Display.error("ResourceSelectorDialog", 3, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////



    public ResourceSelectorDialog(SelectorMode modeIn) {

        this (modeIn, null);
    }

    public ResourceSelectorDialog(SelectorMode modeIn, String prefixIn) {

        try {

            thisDialog = this;

            //
            // Initialize the display objects
            //
            initializeObject(modeIn, prefixIn);

            //
            // Wire in event handlers
            //
            selectorWidget.addResourceSelectionEventHandler(handleResourceSelection);
            selectorWidget.addValidityReportEventHandler(handleValidityReportEvent);
            handleTripleListRequestResponse = selectorWidget.handleTripleListRequestResponse;
            handleSingleListRequestResponse = selectorWidget.handleSingleListRequestResponse;
            handleFilteredListRequestResponse = selectorWidget.handleFilteredListRequestResponse;

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 4, myException);
        }
    }

    public void setCurrentName(String nameIn) {

        try {

            selectorWidget.setCurrentName(nameIn);

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 5, myException);
        }
    }

    public void rejectAllConflicts() {

        try {

            selectorWidget.rejectAllConflicts();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 6, myException);
        }
    }

    public void suppressFilter() {

        try {

            selectorWidget.suppressFilter();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 7, myException);
        }
    }

    public void setTripleList(List<List<ResourceBasics>> listIn) {

        try {

            selectorWidget.handleTripleListRequestResponse.onSuccess(listIn);

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 8, myException);
        }
    }

    public void setSingleList(List<ResourceBasics> listIn) {

        try {

            selectorWidget.handleSingleListRequestResponse.onSuccess(listIn);

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 9, myException);
        }
    }

    public void setDefaultChoice(String nameIn) {

        try {

            selectorWidget.setPreSelect(nameIn);

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 10, myException);
        }
    }

    public void setButtonWidth(Integer widthIn) {

        _buttonWidth = widthIn;
    }

    public void show(AclResourceType resourceTypeIn, String helpHyperlinkIn, String infoStringIn, String buttonTextIn) {

        try {

            initializeDisplay(resourceTypeIn, helpHyperlinkIn, infoStringIn, buttonTextIn);
            dialog.show(_buttonWidth);
            selectorWidget.selectInput();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 11, myException);
        }
    }

    public void unlockDisplay() {

        try {

            if(selectButton != null)
                selectButton.setEnabled(true);
            selectorWidget.enableInput();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 12, myException);
        }
    }

    public void show(AclResourceType resourceTypeIn, String helpHyperlinkIn, String infoStringIn) {

        try {

            initializeDisplay(resourceTypeIn, helpHyperlinkIn, infoStringIn, null);
            dialog.show(_buttonWidth);
            selectorWidget.selectInput();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 13, myException);
        }
    }

    public void show() {

        try {

            dialog.show();
            selectorWidget.selectInput();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 14, myException);
        }
    }

    public void hide() {

        try {

            dialog.hide();

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 15, myException);
        }
    }

    public void destroy() {

        try {

            selectorWidget.destroy();
            dialog.destroy();

            dialog = null;
            selectorWidget = null;
            selectButton = null;
            cancelButton = null;

            _txtDialogTitleString = null;
            _txtOverwriteDialogTitleString = null;
            _txtOverwriteDialogInfoString = null;
            _resourceType = null;

        } catch (Exception myException) {

            Display.error("ResourceSelectorDialog", 16, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject(SelectorMode modeIn, String prefixIn) {

        selectorWidget = new ResourceSelectorPanel<ResourceBasics>(this, modeIn, prefixIn);
        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        uiBinder.createAndBindUi(this);

        //
        // Set up the dialog cancel button
        //
        cancelButton = dialog.getCancelButton();
        cancelButton.setText(Dialog.txtCancelButton);
        cancelButton.setVisible(true);
        cancelButton.setEnabled(true);
        cancelButton.addClickHandler(handleCancelButtonClick);

        //
        // Set up the dialog save button
        //
        selectButton = dialog.getActionButton();
        selectButton.addClickHandler(selectorWidget.handleSelectButtonClick);


        //
        // Set up the list filter edit button
        //
        // filterButton = (Button)dialog.addLeftControl(new CyanButton("Edit Filters"));
        // filterButton.addClickHandler(handleFilterButtonClick);
    }

    //
    //
    //
    protected void initializeDisplay(AclResourceType resourceTypeIn, String helpHyperlinkIn, String infoStringIn, String buttonTextIn) {

        //
        //
        //
        _resourceType = resourceTypeIn;
        _txtDialogTitleString = _constants.resourceSelector_DialogTitle(_resourceType.getLabel());

        // Format the dialog title bar
        //
        dialog.defineHeader(_txtDialogTitleString, helpHyperlinkIn, true);

        //
        // Set up the dialog save button
        //
        selectButton.setText((null != buttonTextIn) ? buttonTextIn : Dialog.txtSaveButton);
        selectButton.setVisible(true);
        selectButton.setEnabled(false);

        //
        // Set up the selection widget
        //
        selectorWidget.initializeDisplay(_resourceType, infoStringIn);

        if(_resourceType != null && filterButton != null)
            filterButton.setVisible(_resourceType.canBeFiltered());
    }
}
