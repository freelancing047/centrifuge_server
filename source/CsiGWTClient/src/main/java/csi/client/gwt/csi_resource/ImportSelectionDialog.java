package csi.client.gwt.csi_resource;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ImportMappingPanel;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.gxt.grid.NameChangeAccess;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.resource.ImportItem;
import csi.server.common.dto.resource.ResourceConflictInfo;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.CsiUUID;
import csi.server.common.util.ValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 4/22/2019.
 */
public class ImportSelectionDialog extends WizardDialog implements NameChangeAccess {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ImportNamingDialog _namingDialog = null;
    ImportMappingPanel _panel;
    Button _importButton;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private MainPresenter _mainPresenter = null;

    OptionControl<?> _namingResource = null;

    private String _title;
    private String _help;
    private boolean _monitoring = false;
    private boolean _iAmOwner;
    private List<List<ResourceBasics>> _dataViewOverWrite = null;
    private List<List<ResourceBasics>> _templateOverWrite = null;
    private List<List<ResourceBasics>> _mapOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminDataViewOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminTemplateOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminMapOverWrite = null;
    private static final String instructions = "";


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ImportSelectionDialog(WizardDialog parentIn, String titleIn, String helpIn,
                                 List<ResourceConflictInfo> itemListIn,
                                 List<List<ResourceBasics>> dataViewOverWriteIn,
                                 List<List<ResourceBasics>> templateOverWriteIn,
                                 List<List<ResourceBasics>> mapOverWriteIn,
                                 Map<String, List<ResourceBasics>> adminDataViewOverWriteIn,
                                 Map<String, List<ResourceBasics>> adminTemplateOverWriteIn,
                                 Map<String, List<ResourceBasics>> adminMapOverWriteIn, boolean iAmOwnerIn) {

        super(parentIn,
                new ImportMappingPanel(itemListIn,
                                        OverWrite.createOverWriteCollection( dataViewOverWriteIn,
                                                                            templateOverWriteIn, mapOverWriteIn,
                                                                            adminDataViewOverWriteIn,
                                                                            adminTemplateOverWriteIn, adminMapOverWriteIn),
                                        iAmOwnerIn),
                titleIn, helpIn, instructions);
        setAsFinal();
        _panel = (ImportMappingPanel)getCurrentPanel();
        _panel.setParentDialog(this);
        _panel.setNameChanger(this);

        _title = titleIn;
        _help = helpIn;
        _iAmOwner = iAmOwnerIn;

        _dataViewOverWrite = dataViewOverWriteIn;
        _templateOverWrite = templateOverWriteIn;
        _mapOverWrite = mapOverWriteIn;
        _adminDataViewOverWrite = adminDataViewOverWriteIn;
        _adminTemplateOverWrite = adminTemplateOverWriteIn;
        _adminMapOverWrite = adminMapOverWriteIn;
    }

    @Override
    public void show() {

        super.show(_constants.dialog_ImportButton());
        _panel.forceLayout();
        _importButton = getActionButton();
        startMonitoring();
    }

    @Override
    public void hide() {

        stopMonitoring();
        super.hide();
    }

    @Override
    public void checkValidity() {

        if (_monitoring) {

            if (_panel.isOkToLeave() && (!_importButton.isEnabled())) {

                _importButton.setEnabled(true);

            } else if ((!_panel.isOkToLeave()) && _importButton.isEnabled()) {

                _importButton.setEnabled(false);
            }
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }

    @Override
    protected void createPanel() {

    }

    public void inputNameChange(OptionControl<?> resourceIn, List<String> listIn) {

        List<List<ResourceBasics>> myTripleList = null;
        AclResourceType myType = AclResourceType.getTypeFromDescriptor(resourceIn.getType());
        String myOwner = resourceIn.getOwner();
        String myName = resourceIn.getName();

        _namingDialog = null;
        _namingResource = resourceIn;

        if (AclResourceType.DATAVIEW.equals(myType)) {

            myTripleList = _iAmOwner
                                ? finalizeTripleList(_dataViewOverWrite, listIn)
                                : createTripleList(_adminDataViewOverWrite, listIn, myName) ;

        } else if (AclResourceType.TEMPLATE.equals(myType)) {

            myTripleList = _iAmOwner
                                ? finalizeTripleList(_templateOverWrite, listIn)
                                : createTripleList(_adminTemplateOverWrite, listIn, myName) ;

        } else if (AclResourceType.MAP_BASEMAP.equals(myType)) {

            myTripleList = _iAmOwner
                                ? finalizeTripleList(_mapOverWrite, listIn)
                                : createTripleList(_adminMapOverWrite, listIn, myName) ;
        }
        _namingDialog = new ImportNamingDialog(this, myType, myName, myTripleList);
        _namingDialog.show();
    }

    @Override
    public void showWithResults(KnowsParent childIn) {

        _namingResource.setName(_namingDialog.getName());
        _namingResource.setRemarks(_namingDialog.getRemarks());
        _namingDialog = null;
        show();
        _panel.refresh();
    }

    private List<List<ResourceBasics>> finalizeTripleList(List<List<ResourceBasics>> tripleListIn,
                                                          List<String> localListIn) {

        List<List<ResourceBasics>> myTripleList = new ArrayList<List<ResourceBasics>>();
        List<ResourceBasics> myDisplay = new ArrayList<ResourceBasics>();
        List<ResourceBasics> myRejects = new ArrayList<ResourceBasics>();
        List<ResourceBasics> myConflicts = new ArrayList<ResourceBasics>();

        if (null != tripleListIn) {

            switch (Math.max(tripleListIn.size(), 3)) {

                case 3:

                    myConflicts.addAll(tripleListIn.get(2));

                case 2:

                    myRejects.addAll(tripleListIn.get(1));

                case 1:

                    myDisplay.addAll(tripleListIn.get(0));

                case 0:

                    break;
            }
        }
        if ((null != localListIn) && (0 < localListIn.size())) {

            for (String myReject : localListIn) {

                myRejects.add(new ResourceBasics(CsiUUID.randomUUID(), myReject, null, getMainPresenter().getUserName()));
            }
        }
        myTripleList.add(myDisplay);
        myTripleList.add(myRejects);
        myTripleList.add(myConflicts);
        return myTripleList;
    }

    private List<List<ResourceBasics>> createTripleList(Map<String, List<ResourceBasics>> mapIn,
                                                        List<String> localListIn, String nameIn) {

        List<List<ResourceBasics>> myTripleList = new ArrayList<List<ResourceBasics>>();
        List<ResourceBasics> myDisplay = new ArrayList<ResourceBasics>();
        List<ResourceBasics> myRejects = new ArrayList<ResourceBasics>();
        List<ResourceBasics> myConflicts = new ArrayList<ResourceBasics>();
        List<ResourceBasics> myList = ((null != mapIn) && (0 < mapIn.size())) ? mapIn.get(nameIn) : null;

        if (null != myList) {

            myDisplay = myList;
            myConflicts = myList;
        }
        myTripleList.add(myDisplay);
        myTripleList.add(myRejects);
        myTripleList.add(myConflicts);
        return myTripleList;
    }

    @Override
    protected void execute() {

        List<ImportItem> myImportList = _panel.getImportRequestList();

        if (0 < myImportList.size()) {

            ((ImportDialog)getPriorDialog()).launchImport(myImportList);
        }
    }

    @Override
    protected void onPrevious() {

    }

    private void startMonitoring() {

        _monitoring = true;
        checkValidity();
    }

    private void stopMonitoring() {

        _monitoring = false;
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}
