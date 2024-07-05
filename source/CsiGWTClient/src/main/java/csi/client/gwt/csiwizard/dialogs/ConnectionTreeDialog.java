package csi.client.gwt.csiwizard.dialogs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ConnectionTreePanel;
import csi.client.gwt.edit_sources.presenters.EditAdHocDataPresenter;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.uploader.FileSelectorDialog;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.*;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 6/21/2017.
 */
public class ConnectionTreeDialog extends WizardDialog implements ConnectionTreePanel.TableCallBack {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ConnectionTreePanel panel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtConnectorSelectionInfo = _constants.dataviewFromScratchWizard_InfoString();

    private boolean _isInstalledTable = false;
    private ConnectionTreeDialog _this = this;
    private AdHocDataSource _container = null;
    private EditAdHocDataPresenter _presenter = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler tableInstaller = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                suspendMonitoring();
                (new FileSelectorDialog(_this, handleInstallComplete, null)).show();

            } catch (Exception myException) {

                Display.error("ConnectionTreeDialog", 1, myException);
            }
        }
    };

    private TransferCompleteEventHandler handleInstallComplete
            = new TransferCompleteEventHandler() {

        @Override
        public void onTransferComplete(TransferCompleteEvent eventIn) {

            try {

                reDisplayDataSourceTree(tableInstaller, eventIn.getItemList());
                resumeMonitoring();

            } catch (Exception myException) {

                Display.error("ConnectionTreeDialog", 2, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ConnectionTreeDialog(AdHocDataSource containerIn, WizardInterface priorDialogIn,
                                String titleIn, String helpIn, boolean isInstalledTableIn)
            throws CentrifugeException {

        super(priorDialogIn, new ConnectionTreePanel(priorDialogIn, containerIn.getDataSources().get(0),
                                                     new ArrayList<QueryParameterDef>(), null, new ArrayList<String>()),
                titleIn, helpIn, _constants.dataviewFromScratchWizard_ConnectionTreePanel(Dialog.txtNextButton));
        _container = containerIn;
        _presenter = new EditAdHocDataPresenter(_container, null);
        _isInstalledTable = isInstalledTableIn;
        panel = (ConnectionTreePanel)getCurrentPanel();

        if (_isInstalledTable) {

            reDisplayDataSourceTree(tableInstaller, new ArrayList<>());
        }
    }

    private ClickHandler handleBadDataTypes = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            handleResponse(panel.getSqlTable());
        }
    };

    private List<ColumnDef> validateColumnDataTypes(SqlTableDef tableIn) {

        List<ColumnDef> myColumnList = new ArrayList<>();
        StringBuilder myErrorBuffer = new StringBuilder();

        if (null != tableIn) {

            myColumnList = new ArrayList<>();

            for (ColumnDef myColumn : tableIn.getColumns()) {

                if (CsiDataType.Unsupported.equals(myColumn.getCsiType())) {

                    if (0 == myErrorBuffer.length()) {

                        myErrorBuffer.append("The following columns had unrecognized data types and will not be loaded:\n");

                    } else {

                        myErrorBuffer.append(", ");
                    }
                    myErrorBuffer.append(Format.value(myColumn.getName()));

                } else {

                    myColumnList.add(myColumn);
                }
            }
            tableIn.setColumns(myColumnList);
            if (0 < myErrorBuffer.length()) {

                Display.error("ConnectionTreeDialog", myErrorBuffer.toString(), handleBadDataTypes);
            }

        } else {

            Display.error("ConnectionTreeDialog", "Unable to access selected table!");
        }
        return (0 == myErrorBuffer.length()) ? myColumnList : null;
    }

    public void handleResponse(SqlTableDef tableIn) {

        try {

            List<ColumnDef> myColumnList = validateColumnDataTypes(tableIn);

            if (null != myColumnList) {

                if (0 < myColumnList.size()) {

                    UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();
                    boolean mySetSecurityOk = (null != myUserInfo) ? myUserInfo.getCanSetSecurity() : false;

                    fillContainer();

                    if (mySetSecurityOk
                            && ((!_isInstalledTable) && (WebMain.getClientStartupInfo().isEnforceSecurityTags()
                                || WebMain.getClientStartupInfo().isProvideTagBanners()))) {

                        (new SecurityTagsDialog(_presenter, this, getDialogTitle(), getDialogHelp())).show();

                    } else if (mySetSecurityOk
                                && ((!_isInstalledTable) && (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                                    || WebMain.getClientStartupInfo().isProvideCapcoBanners()))) {

                        (new CapcoDialog(_presenter, this, getDialogTitle(), getDialogHelp())).show();

                    } else {

                        _presenter.saveResults(this);
                    }

                } else {

                    Display.error("ConnectionTreeDialog", "No useable columns found in source data!");
                }
            }

        } catch (Exception myException) {

            Display.error("ConnectionTreeDialog", 4, myException);
        }
        hideWatchBox();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void reDisplayDataSourceTree(ClickHandler handlerIn, List<String> highLightListIn)
            throws CentrifugeException {

        panel.reformatPanel(_container.getDataSources().get(0),
                            _container.getParameterList(), handlerIn, highLightListIn);
    }

    @Override
    protected void createPanel() {

    }

    @Override
    protected void execute() {

        showWatchBox("Retrieving column list.");

        try {

            panel.getSqlTableWithColumns(this);

        } catch (Exception myException) {

            Display.error("ConnectionTreeDialog", 5, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void fillContainer() {

        DataSourceDef mySource = panel.getDataSource();
        SqlTableDef myTable = panel.getSqlTable();
        Integer myRowLimit = panel.getRowLimit();
        DataSetOp myTree = new DataSetOp();
        FieldListAccess myAccess = _container.getFieldListAccess();
        int myFieldNumber = 0;

        myTree.setTableDef(myTable);
        myTree.createName(1);
        if (null != myTable) {

            List<ColumnDef> myColumnList = myTable.getColumns();

            myTable.setLocalId(CsiUUID.randomUUID());

            if (null != myColumnList) {

                for (ColumnDef myColumn : myColumnList) {

                    myColumn.setSelected(true);
                }
            }
            myTable.setSource(mySource);

            for (ColumnDef myColumn : myTable.getColumns()) {

                FieldDef myField = new FieldDef(FieldType.COLUMN_REF);
                CsiMap<String, String> myProperties = new CsiMap<String, String>();

                myField.setOrdinal(myFieldNumber);
                myField.setFinalSort(myFieldNumber++);

                myField.setColumnLocalId(myColumn.getLocalId());
                myField.setFieldName(myColumn.getColumnName());
                myField.setValueType(myColumn.getCsiType());

                myField.setFunctionType(FunctionType.NONE);
                myField.setClientProperties(myProperties);

                myField.setDsLocalId(mySource.getLocalId());
                myField.setTableLocalId(myTable.getLocalId());

                myAccess.addFieldDef(myField);
            }
        }
        _container.getDataDefinition().setRowLimit(myRowLimit);
        _container.getDataDefinition().setDataTree(myTree);
        _container.getDataDefinition().getDataSources().add(mySource);
    }
}
