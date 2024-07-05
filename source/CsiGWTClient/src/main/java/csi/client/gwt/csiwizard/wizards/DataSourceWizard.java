package csi.client.gwt.csiwizard.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.WizardPanelType;
import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.panels.MultipleEntryWizardPanel;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.BooleanInputWidget;
import csi.client.gwt.csiwizard.widgets.EscapedTextInputWidget;
import csi.client.gwt.csiwizard.widgets.IntegerInputWidget;
import csi.client.gwt.csiwizard.widgets.PasswordInputWidget;
import csi.client.gwt.csiwizard.widgets.SqlInputWidget;
import csi.client.gwt.csiwizard.widgets.StackedInputWidget;
import csi.client.gwt.csiwizard.widgets.StringInputWidget;
import csi.client.gwt.csiwizard.widgets.TextInputWidget;
import csi.client.gwt.csiwizard.widgets.TypeSelectionWidget;
import csi.client.gwt.csiwizard.widgets.ValueInputWidget;
import csi.client.gwt.csiwizard.dialogs.ConnectionTreeDialog;
import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.events.RefreshRequiredEvent;
import csi.client.gwt.events.RefreshRequiredEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.KeyValueItem;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.dto.config.connection.ConfigItem;
import csi.server.common.dto.config.connection.DriverConfigInfo;
import csi.server.common.dto.config.connection.ListItem;
import csi.server.common.dto.config.connection.SelectListConfigItem;
import csi.server.common.enumerations.*;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.Property;
import csi.server.common.model.UUID;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.UserFileActionsServiceProtocol;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 6/21/2017.
 */
public class DataSourceWizard extends Wizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    /*
    HOSTNAME("csi.hostname", "csi.hostname", 0),
    PORT("csi.port", "csi.port", 1),
    RUNTIME_USERNAME("csi.runtime.username", "csi.runtime.username", 2),
    RUNTIME_PASSWORD("csi.runtime.password", "csi.runtime.password", 3),
    USERNAME("csi.username", "csi.username", 4),
    PASSWORD("csi.password", "csi.password", 5),
    INSTANCENAME("csi.instancename", "csi.instanceName", 6),
    DATABASENAME("csi.databasename", "csi.databaseName", 7),
    PARAMS_PREFIX("csi.params", "csi.params", 8),
    SCHEMA_USEEXISTING("csi.schema.useexisting", "csi.schema.useExisting", 9),
    SCHEMA_HASHEADERS("csi.schema.firstrowheaders", "csi.schema.firstRowHeaders", 10),
    SCHEMA_ROWDELIM("csi.schema.rowdelim", "csi.schema.rowDelim", 11),
    SCHEMA_CELLDELIM("csi.schema.celldelim", "csi.schema.cellDelim", 12),
    SCHEMA_TABLENAME("csi.schema.tablename", "csi.schema.tableName", 13),
    SCHEMA_XPATH("csi.schema.xpath", "csi.schema.xpath", 14),
    SCHEMA_COLUMNS("csi.schema.columns", "csi.schema.columns", 15),
    SCHEMA_CHARSET("csi.schema.charset", "csi.schema.charset", 16),
    SCHEMA_DATE_FORMAT("csi.schema.dateformat", "csi.schema.dateFormat", 17),
    SCHEMA_NAMESPACE_PREFIX("csi.schema.namespace", "csi.schema.namespace", 18),
    FILETOKEN("csi.filetoken", "csi.filetoken", 19),
    LOCALFILEPATH("csi.localFilepath", "csi.localFilePath", 20),
    REMOTEFILEPATH("csi.remotefilepath", "csi.remoteFilePath", 21),
    FILEPATH("csi.filepath", "csi.filePath", 22),
    QUERY_TABLE_NAME("query.tablename", "query.tableName", 23),
    PRE_QUERY("prequery", "preQuery", 24),
    POST_QUERY("postquery", "postQuery", 25),
    LEGACY_CONNECTION_STRING("csilegacyconnectionstring", "csiLegacyConnectionString", 26),
    ESCAPED_TEXT("escapedtext", "escapedText", 27),
    INPLACE("inplace", "inPlace", 28),
    DISTINCT_SOURCES("distinctsources", "distinctSources", 29),
    UNSUPPORTED("unsupported", "Unsupported", 30);
*/


    private static final String DATAFILES = "datafiles"; //$NON-NLS-1$

    private static final String USERFILES = "userfiles"; //$NON-NLS-1$

    private static final String FILE_TYPE = "fileType"; //$NON-NLS-1$

    private static final String TRUE = "true"; //$NON-NLS-1$

    private static final String PIPE = "|"; //$NON-NLS-1$

    private static final String PERIOD = "."; //$NON-NLS-1$

    private static final String EQUAL = "="; //$NON-NLS-1$

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final String[] _txtConnectorParameterPrompt = {

            _constants.connectorParameterPrompt_Hostname(),             // 0
            _constants.connectorParameterPrompt_Port(),                 // 1
            null,
            null,
            _constants.connectorParameterPrompt_Username(),             // 4
            _constants.connectorParameterPrompt_Password(),             // 5
            _constants.connectorParameterPrompt_InstanceName(),         // 6
            _constants.connectorParameterPrompt_DatabaseName(),         // 7
            null,
            _constants.connectorParameterPrompt_UseExisting(),          // 9
            _constants.connectorParameterPrompt_HasHeaders(),           // 10
            _constants.connectorParameterPrompt_RowDelimiter(),         // 11
            _constants.connectorParameterPrompt_CellDelimiter(),        // 12
            _constants.connectorParameterPrompt_TableName(),            // 13
            _constants.connectorParameterPrompt_XPath(),                // 14
            null,
            _constants.connectorParameterPrompt_Charset(),              // 16
            _constants.connectorParameterPrompt_DateFormat(),           // 17
            _constants.connectorParameterPrompt_Namespace(),            // 18
            _constants.connectorParameterPrompt_FileToken(),            // 19
            _constants.connectorParameterPrompt_LocalFilePath(),        // 20
            _constants.connectorParameterPrompt_RemoteFilePath(),       // 21
            _constants.connectorParameterPrompt_FilePath(),             // 22
            _constants.connectorParameterPrompt_QueryTableName(),       // 23
            _constants.connectorParameterPrompt_PreQueryCommand(),      // 24
            _constants.connectorParameterPrompt_PostQueryCommand(),     // 25
            _constants.connectorParameterPrompt_ConnectionString(),     // 26
            null,
            null,
            _constants.connectorParameterPrompt_EnterParameter(),       // 29
            null
    };

    private static final String[] _txtConnectorParameterTitle = {

            _constants.connectorParameterTitle_DbInfo(),                // 0
            _constants.connectorParameterTitle_DbInfo(),                // 1
            null,
            null,
            _constants.connectorParameterTitle_Credentials(),           // 4
            _constants.connectorParameterTitle_Credentials(),           // 5
            _constants.connectorParameterTitle_DbInfo(),                // 6
            _constants.connectorParameterTitle_DbInfo(),                // 7
            null,
            _constants.connectorParameterTitle_DbInfo(),                // 9
            _constants.connectorParameterTitle_FileFormat(),            // 10
            _constants.connectorParameterTitle_FileFormat(),            // 11
            _constants.connectorParameterTitle_FileFormat(),            // 12
            _constants.connectorParameterTitle_XmlSchema(),             // 13
            _constants.connectorParameterTitle_XmlSchema(),             // 14
            null,
            _constants.connectorParameterTitle_FileFormat(),            // 16
            _constants.connectorParameterTitle_FileFormat(),            // 17
            _constants.connectorParameterTitle_XmlSchema(),             // 18
            _constants.connectorParameterTitle_FileIdentification(),    // 19
            _constants.connectorParameterTitle_FileIdentification(),    // 20
            _constants.connectorParameterTitle_FileIdentification(),    // 21
            _constants.connectorParameterTitle_FileIdentification(),    // 22
            _constants.connectorParameterTitle_DbInfo(),                // 23
            _constants.connectorParameterTitle_DbInfo(),                // 24
            _constants.connectorParameterTitle_DbInfo(),                // 25
            _constants.connectorParameterTitle_DbInfo(),                // 26
            null,
            null,
            _constants.connectorParameterTitle_ParameterEntry(),        // 29
            null
    };

    private static final String _txtParameterNamePrompt = _constants.connectorParameterPrompt_ParameterName();
    private static final String _txtParameterValuePrompt = _constants.connectorParameterPrompt_ParameterValue();
    private static final String _txtColumnNamePrompt = _constants.connectorParameterPrompt_ColumnName();
    private static final String _txtColumnPathPrompt = _constants.connectorParameterPrompt_ColumnPath();
    private static final String _txtColumnTypePrompt = _constants.connectorParameterPrompt_ColumnType();
    private static final String _txtPreQueryPrompt = _constants.connectorParameterPrompt_PreQueryCommand();
    private static final String _txtPostQueryPrompt = _constants.connectorParameterPrompt_PostQueryCommand();

    private static final String _txtRequiredParameter = _constants.dataviewFromScratchWizard_RequiredConnectionParameter();
    private static final String _txtOptionalParameter = _constants.dataviewFromScratchWizard_OptionalConnectionParameter();
    private static final String _txtSingleParameter = _constants.dataviewFromScratchWizard_SingleParameter(Dialog.txtNextButton);
    private static final String _txtMultipleParameter = _constants.dataviewFromScratchWizard_MultipleParameter(Dialog.txtAddButton, Dialog.txtNextButton);
    private static final String _txtQueryPairSubtitle = _constants.dataviewFromScratchWizard_QueryPairSubtitle();
    private static final String _txtParameterListSubtitle = _constants.dataviewFromScratchWizard_ParameterListSubtitle();
    private static final String _txtXmlColumnsSubtitle = _constants.dataviewFromScratchWizard_XmlColumnsSubtitle();

    private static final String _delimeter = " :: "; //$NON-NLS-1$
    private static final String _splitToken = " :: "; //$NON-NLS-1$

    private List<ConfigItem> _configItemList = null;
    private int _parameterCount = 0;
    private DataChangeEventHandler _finishHandler = null;

    private List<Property> _connectionProperties = null;
    private String _preQueryString = null;
    private String _postQueryString = null;
    private String _connectionString = null;
    private String _driverKey = null;
    private boolean _inPlace = false;
    private boolean _singleTable = false;
    private boolean _simpleLoader = false;
    private String _baseName = "?"; //$NON-NLS-1$
    private DataSourceDef _dataSource = null;
    private WizardInterface _returnDialog = this;
    private AclResourceType _targetResource = null;
    private boolean _waiting = false;
    boolean _ready = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected RefreshRequiredEventHandler handleRefreshRequest
            = new RefreshRequiredEventHandler() {
        @Override
        public void onRefreshRequired(RefreshRequiredEvent eventIn) {

            if (_activePanel instanceof ResourceSelectorPanel) {

                ConfigItem myParameter = _configItemList.get(_activePanelIndex);
                List<String> myList = buildExtensionList(myParameter.getValidationOperations());
                retrieveFileList(((ResourceSelectorPanel)_activePanel).handleTripleListRequestResponse, myList);
            }
        }
    };

    private VortexEventHandler<Response<String, DriverConfigInfo>> handleConfigItemListResponse
            = new AbstractVortexEventHandler<Response<String, DriverConfigInfo>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("DataSourceWizard", 2, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, DriverConfigInfo> responseIn) {

            hideWatchBox();

            if (ResponseHandler.isSuccess(responseIn)) {

                DriverConfigInfo myConnectorInfo = responseIn.getResult();

                if (null != myConnectorInfo) {

                    _parameterCount = 0;

                    if (ConnectorSupport.getInstance().canEditConnection(_driverKey)) {

                        _parameterCount = constructParameterPanels(myConnectorInfo.getConfigItems());
                    }
                    _finalDisplayIndex = _parameterCount - 1;
                    _ready = true;
                    if (_waiting) {

                        activateWizard();
                    }
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSourceWizard(String titleIn, String helpIn,
                            DataSourceDef dataSourceIn, DataChangeEventHandler handlerIn) {

        super(titleIn, helpIn, null, null);
        _finishHandler = handlerIn;
        extractDataSourceInformation(dataSourceIn);
    }

    public DataSourceWizard(WizardInterface priorDialogIn, String titleIn,
                            String helpIn, DriverBasics selectionIn, DataChangeEventHandler handlerIn) {

        super(titleIn, helpIn, null, priorDialogIn);
        if (ConnectorSupport.getInstance().canEditConnection(selectionIn.getKey())) {

            _parameterCount = constructParameterPanels(selectionIn.getDriverInfo().getConfigItems());
        }
        _finishHandler = handlerIn;
        processSelection(selectionIn);
        _finalDisplayIndex = _parameterCount - 1;
        _ready = true;
    }

    public DataSourceWizard(AclResourceType targetResourceIn, WizardInterface priorDialogIn,
                            String titleIn, String helpIn, DriverBasics selectionIn)
            throws CentrifugeException {

        super(titleIn, helpIn, null, priorDialogIn);
        _targetResource = targetResourceIn;
        if (ConnectorSupport.getInstance().canEditConnection(selectionIn.getKey())) {

            _parameterCount = constructParameterPanels(selectionIn.getDriverInfo().getConfigItems());
        }
        _chainForward = true;
        processSelection(selectionIn);
        _finalDisplayIndex = _parameterCount - 1;
        _ready = true;
    }


    @Override
    public void show() {

        super.show();

        if (null == _activePanel) {

            if (_ready) {

                if (0 > _finalDisplayIndex) {

                    _returnDialog = getPriorDialog();
                    execute(null, null);
                    destroy();

                } else {

                    activateWizard();
                }

            } else {

                _waiting = true;
            }
        }
    }

    @Override
    public void destroy() {

        _configItemList = null;
        _parameterCount = 0;
        _driverKey = null;

        super.destroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    @Override
    protected void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        try {

            buildPropertyList();
            if (_chainForward) {

                (new ConnectionTreeDialog(new AdHocDataSource(_targetResource, createBasicSourceDef(buildConnection()),
                                                                new CapcoInfo(), new SecurityTagsInfo()),
                                            _returnDialog, getTitle(), getHelp(), isInstalledTable(_driverKey))).show();

            } else if (null != _finishHandler) {

                if (null == _dataSource) {

                    _finishHandler.onDataChange(new DataChangeEvent(createBasicSourceDef(buildConnection())));

                } else {

                    _finishHandler.onDataChange(new DataChangeEvent(updateSourceDef(buildConnection())));
                }
                destroy();
            }

        } catch (CentrifugeException myException) {

            Display.error("DataSourceWizard", 4, myException);
        }
    }

    //
    //
    //
    protected void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        if (null != _finishHandler) {

            _finishHandler.onDataChange(new DataChangeEvent(null));
        }
    }

    @Override
    protected void displayNewPanel(int indexIn, ClickEvent eventIn) {

        try {

            if (_parameterCount > indexIn) {

                //
                // Display the next parameter input panel
                //
                ConfigItem myParameter = _configItemList.get(indexIn);

                displayPanel(buildParameterPanel(myParameter), buildIntermediateInfo(myParameter, indexIn));
            }

        } catch (Exception myException) {

            Display.error("DataSourceWizard", 6, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private AbstractWizardPanel buildParameterPanel(ConfigItem parameterIn) throws CentrifugeException {

        AbstractWizardPanel myPanel = null;

        if (null != parameterIn) {

            AbstractInputWidget myInputWidget = null;
            JdbcDriverParameterType myType = JdbcDriverParameterType.getValue(parameterIn.getType());
            JdbcDriverParameterKey myKey = JdbcDriverParameterKey.getValue(parameterIn.getKey());
            String myLabel = (JdbcDriverParameterKey.UNSUPPORTED != myKey) ? myKey.getLabel() : parameterIn.getKey();
            List<KeyValueItem> myDefaultValues = parameterIn.getDefaultValues();
            List<KeyValueItem> myValidationRules = parameterIn.getValidationOperations();
            ParameterValidator myValidator = ((null != myValidationRules) && (0 < myValidationRules.size()))
                    ? new ParameterValidator(myValidationRules) : null;
            String myDefault = ((null != myDefaultValues) && (0 < myDefaultValues.size()))
                    ? myDefaultValues.get(0).getValue() : null;
            WizardPanelType myPanelType = WizardPanelType.NONE;
            String myPrompt = parameterIn.getLabel();
            String myTitle = parameterIn.getTitle();
            boolean myRequiredFlag = parameterIn.getRequired();

            if ((null == myPrompt) || (0 == myPrompt.length())) {

                myPrompt = _txtConnectorParameterPrompt[myKey.getOrdinal()];
            }

            switch (myType) {

                case STRING :

                    myInputWidget = createTextInputWidget(myKey, identifyType(myValidationRules), myPrompt, myDefault, myValidator, myRequiredFlag);
                    myPanelType = WizardPanelType.SINGLE_ENTRY;
                    break;

                case LEGACY_CONNECTION_STRING :

                    _connectionString = null;
                    myInputWidget = new StringInputWidget(myPrompt, myDefault, myValidator, myRequiredFlag);
                    myPanelType = WizardPanelType.SINGLE_ENTRY;
                    break;

                case PASSWORD :

                    myInputWidget = new PasswordInputWidget(myPrompt, myValidator, myRequiredFlag);
                    myPanelType = WizardPanelType.SINGLE_ENTRY;
                    break;

                case XML_NAMESPACE :

                    myInputWidget = new StringInputWidget(myPrompt, myValidator, true);
                    myPanelType = WizardPanelType.MULTIPLE_ENTRY;
                    break;

                case BOOLEAN :

                    myInputWidget = new BooleanInputWidget(myPrompt, myDefault, myRequiredFlag);
                    myPanelType = WizardPanelType.SINGLE_ENTRY;
                    break;

                case SELECT_LIST :

                    if (parameterIn.getIsMultiline()) {

                        //myInputWidget = new ListInputWidget(myPrompt, null, parameterIn.getAllowCustomValue());
                        //myPanelType = WizardPanelType.MULTIPLE_ENTRY;

                    } else {

                        List<ListItem> myListValues = ((SelectListConfigItem)parameterIn).getListItemValues();

                        if ((null == myTitle) || (0 == myTitle.length())) {
                            myTitle = _txtConnectorParameterTitle[myKey.getOrdinal()];
                        }

                        myPanel = createSelectionPanel(myTitle, myListValues, myDefault, parameterIn.getAllowCustomValue(), myRequiredFlag);
                    }
                    break;

                case FILE :

                    if ((null == myTitle) || (0 == myTitle.length())) {
                        myTitle = _txtConnectorParameterTitle[myKey.getOrdinal()];
                    }

                    myPanel = createFileSelectionPanel(parameterIn.getLabel(), myRequiredFlag, myValidationRules);
                    break;

                case QUERY_COMMANDS : {

                    String[] myDefaultArray = getQueryDefaults(myDefaultValues);

                    _preQueryString = null;
                    _postQueryString = null;
                    myInputWidget = new StackedInputWidget(new StackedInputWidget.WidgetLabelPair[] {
                            new StackedInputWidget.WidgetLabelPair(new SqlInputWidget(_txtPreQueryPrompt, myDefaultArray[0], myRequiredFlag), myRequiredFlag),
                            new StackedInputWidget.WidgetLabelPair(new SqlInputWidget(_txtPostQueryPrompt, myDefaultArray[1], myRequiredFlag), myRequiredFlag)
                    }, _delimeter, false);
                }

                if ((null == myTitle) || (0 == myTitle.length())) {
                    myTitle = _txtQueryPairSubtitle;
                }

                myPanelType = WizardPanelType.SINGLE_ENTRY;

                break;

                case PARAMETERS_TABLE :

                    myInputWidget = new StackedInputWidget(new StackedInputWidget.WidgetLabelPair[] {
                            new StackedInputWidget.WidgetLabelPair(new TextInputWidget(_txtParameterNamePrompt, true), true),
                            new StackedInputWidget.WidgetLabelPair(new TextInputWidget(_txtParameterValuePrompt, true), true)
                    }, EQUAL, false); //$NON-NLS-1$

                    if ((null == myTitle) || (0 == myTitle.length())) {
                        myTitle = _txtParameterListSubtitle;
                    }

                    myPanel = new MultipleEntryWizardPanel(this, myLabel, myInputWidget, myTitle, null, formatDefaultPairs(extractDefaultPairs(myDefaultValues), EQUAL), myRequiredFlag); //$NON-NLS-1$
                    break;

                case XML_COLUMNS_TABLE :
                {
                    List<CsiDataType> myRejectedTypes = new ArrayList<CsiDataType>();

                    myRejectedTypes.add(CsiDataType.Boolean);

                    myInputWidget = new StackedInputWidget(new StackedInputWidget.WidgetLabelPair[] {
                            new StackedInputWidget.WidgetLabelPair(new TextInputWidget(_txtColumnNamePrompt, true), true),
                            new StackedInputWidget.WidgetLabelPair(new TextInputWidget(_txtColumnPathPrompt, true), true),
                            new StackedInputWidget.WidgetLabelPair(new TypeSelectionWidget(_txtColumnTypePrompt, myRejectedTypes), true)
                    }, _delimeter, true);

                    if ((null == myTitle) || (0 == myTitle.length())) {
                        myTitle = _txtXmlColumnsSubtitle;
                    }

                    myPanel = new MultipleEntryWizardPanel(this, myKey.getLabel(), myInputWidget, myTitle, null, formatDefaultPairs(extractDefaultPairs(myDefaultValues, myKey), _delimeter), myRequiredFlag);
                    break;
                }
                default :

                    break;
            }

            if (null == myPanel) {

                switch (myPanelType) {

                    case MULTIPLE_PAIR :

                        break;

                    case MULTIPLE_ENTRY :

                        if ((null == myTitle) || (0 == myTitle.length())) {
                            myTitle = _txtConnectorParameterTitle[myKey.getOrdinal()];
                        }

                        myPanel = new MultipleEntryWizardPanel(this, myLabel, myInputWidget, myTitle, null, extractDefaultList(myDefaultValues), myRequiredFlag);
                        break;

                    case SINGLE_ENTRY :

                        if ((null == myTitle) || (0 == myTitle.length())) {
                            myTitle = _txtConnectorParameterTitle[myKey.getOrdinal()];
                        }

                        myPanel = new SingleEntryWizardPanel(this, myLabel, myInputWidget, myTitle, null, myDefault, myRequiredFlag);
                        break;

                    case RESOURCE_SELECTOR :

                        break;

                    case NONE :

                        throw new CentrifugeException(i18n.dataviewFromScratchWizardTypeErrorMessage()
                                                        + Format.value(parameterIn.getType()));
                }
            }
            if (null != myPanel) {

            }

        }

        return myPanel;
    }

    private AbstractInputWidget createTextInputWidget(JdbcDriverParameterKey keyIn, CsiDataType typeIn, String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

        AbstractInputWidget myInputWidget = null;

        if (CsiDataType.Integer == typeIn) {

            myInputWidget = new IntegerInputWidget(promptIn, defaultIn, validatorIn, requiredIn);

        } else if (CsiDataType.Number == typeIn) {

            myInputWidget = new ValueInputWidget(promptIn, defaultIn, validatorIn, requiredIn);

        } else {

            switch (keyIn) {

                case USERNAME :

                    myInputWidget = new StringInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case SCHEMA_XPATH :
                case SCHEMA_CHARSET :
                case SCHEMA_NAMESPACE_PREFIX :

                    myInputWidget = new StringInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case HOSTNAME :

                    myInputWidget = new StringInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case INSTANCENAME :
                case DATABASENAME :
                case SCHEMA_TABLENAME :
                case SCHEMA_DATE_FORMAT :
                case QUERY_TABLE_NAME :

                    myInputWidget = new TextInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case PRE_QUERY :
                case POST_QUERY :

                    myInputWidget = new SqlInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case SCHEMA_ROWDELIM :
                case SCHEMA_CELLDELIM :
                case ESCAPED_TEXT :

                    myInputWidget = new EscapedTextInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case PORT :

                    myInputWidget = new IntegerInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;

                case PASSWORD :

                    myInputWidget = new PasswordInputWidget(promptIn, validatorIn, requiredIn);
                    break;

                case SCHEMA_USEEXISTING :

                    myInputWidget = new BooleanInputWidget(promptIn, defaultIn);
                    break;

                case SCHEMA_HASHEADERS :

                    myInputWidget = new BooleanInputWidget(promptIn, defaultIn);
                    break;

                default :

                    myInputWidget = new TextInputWidget(promptIn, defaultIn, validatorIn, requiredIn);
                    break;
            }
        }

        return myInputWidget;
    }

    private ResourceSelectorPanel<SelectorBasics> createSelectionPanel(String promptIn, List<ListItem> listValuesIn, String defaultIn, boolean allowCustomValueIn, boolean isRequiredIn) {

        ResourceSelectorPanel.SelectorMode myMode = allowCustomValueIn ? ResourceSelectorPanel.SelectorMode.SIMPLE_COMBO : ResourceSelectorPanel.SelectorMode.SIMPLE_READ_ONLY;

        ResourceSelectorPanel<SelectorBasics> myPanel = new ResourceSelectorPanel<SelectorBasics>(this, myMode, isRequiredIn);
        myPanel.resetDisplay(null, promptIn, defaultIn, createSimpleList(listValuesIn));

        return myPanel;
    }

    private ResourceSelectorPanel<SelectorBasics> createFileSelectionPanel(String promptIn, boolean isRequiredIn, List<KeyValueItem> validationRulesIn) {

        ResourceSelectorPanel<SelectorBasics> myPanel = new ResourceSelectorPanel<SelectorBasics>(this, ResourceSelectorPanel.SelectorMode.SIMPLE_LOCAL_BROWSE, isRequiredIn);
        List<String> myList = buildExtensionList(validationRulesIn);

        myPanel.addRefreshRequiredEventHandler(handleRefreshRequest);
        myPanel.setFileTypes(myList);

        retrieveFileList(myPanel.handleTripleListRequestResponse, myList);
        myPanel.initializeDisplay(null, promptIn);

        return myPanel;
    }

    private List<SelectorBasics> createSimpleList(List<ListItem> listIn) {

        List<SelectorBasics> myList = new ArrayList<SelectorBasics>();

        for (ListItem myItem : listIn) {

            myList.add(new SelectorBasics(myItem.getValue(), myItem.getLabel(), myItem.getValue()));
        }

        return myList;
    }

    private List<KeyValueItem> extractDefaultPairs(List<KeyValueItem> listIn) {

        List<KeyValueItem> myList = new ArrayList<KeyValueItem>();

        for (KeyValueItem myItem : listIn) {

            String[] myValues = myItem.getValue().split(EQUAL); //$NON-NLS-1$

            if (1 < myValues.length) {

                myList.add(new KeyValueItem(myValues[0], myValues[1]));

            } else {

                myList.add(new KeyValueItem("", myValues[1])); //$NON-NLS-1$
            }
        }

        return myList;
    }

    private List<KeyValueItem> extractDefaultPairs(List<KeyValueItem> listIn, JdbcDriverParameterKey keyIn) {

        List<KeyValueItem> myList = new ArrayList<KeyValueItem>();
        String myPrefix = keyIn.getKey();
        int myBase = myPrefix.length() + 1;

        for (KeyValueItem myItem : listIn) {

            String myKey = myItem.getKey();

            if (myKey.toLowerCase().startsWith(myPrefix)) {

                myList.add(new KeyValueItem(myKey.substring(myBase), myItem.getValue()));

            } else {

                myList.add(new KeyValueItem(myKey, myItem.getValue()));
            }
        }

        return myList;
    }

    private List<String> formatDefaultPairs(List<KeyValueItem> listIn, String delimeterIn) {

        List<String> myList = new ArrayList<String>();

        for (KeyValueItem myItem : listIn) {

            myList.add(myItem.getKey() + delimeterIn + myItem.getValue());
        }

        return myList;
    }

    private List<String> extractDefaultList(List<KeyValueItem> listIn) {

        List<String> myList = new ArrayList<String>();

        for (KeyValueItem myItem : listIn) {

            myList.add(myItem.getValue());
        }

        return myList;
    }

    private boolean extractPropertyData(AbstractWizardPanel panelIn, ConfigItem parameterIn, List<Property> propertiesIn) throws CentrifugeException {

        boolean myTableIdentified = false;
        String myResults = null;
        List<String> myResultList = null;
        JdbcDriverParameterType myType = JdbcDriverParameterType.getValue(parameterIn.getType());
        JdbcDriverParameterKey myKey = JdbcDriverParameterKey.getValue(parameterIn.getKey());
        String myLabel = (JdbcDriverParameterKey.UNSUPPORTED != myKey) ? myKey.getLabel() : parameterIn.getKey();

        if (panelIn instanceof MultipleEntryWizardPanel) {

            myResultList = ((MultipleEntryWizardPanel)panelIn).getList();

        } else  {

            myResults = panelIn.getText();
        }

        if (null != myResults) {

            switch (myType) {

                case STRING :
                case PASSWORD :
                case BOOLEAN :

                    propertiesIn.add(new Property(myLabel, myResults));
                    if (JdbcDriverParameterKey.SCHEMA_TABLENAME.equals(myKey)
                            && (null != myResults) && (0 < myResults.length())) {

                        myTableIdentified = true;
                    }
                    break;

                case LEGACY_CONNECTION_STRING :

                    _connectionString = myResults;
                    break;

                case FILE :

                    propertiesIn.add(new Property(JdbcDriverParameterKey.FILEPATH.getLabel(),
                            Format.normalizePath(((ResourceSelectorPanel) panelIn).getSelectionKey())));
                    propertiesIn.add(new Property(JdbcDriverParameterKey.REMOTEFILEPATH.getLabel(),
                            myResults));
                    break;

                case SELECT_LIST :

                    propertiesIn.add(new Property(myLabel, myResults));
                    break;

                case QUERY_COMMANDS :

                    // Should be SINGLE_ENTRY -- PARSE

                    String[] myPair = myResults.split(_splitToken);

                    if ((0 < myPair.length) && (0 < myPair[0].length())) {

                        _preQueryString = myPair[0];
                    }

                    if ((1 < myPair.length) && (0 < myPair[1].length())) {

                        _postQueryString = myPair[1];
                    }

                    break;

                default :

                    throw new CentrifugeException(i18n.dataviewFromScratchWizardParameterErrorMessage()
                                                    + Format.value(parameterIn.getLabel()));
            }

            if (JdbcDriverParameterKey.DATABASENAME.equals(myKey)
                    || JdbcDriverParameterType.FILE.equals(myType)) {

                _baseName = myResults;
            }

        } else if (null != myResultList) {

            switch (myType) {

                case SELECT_LIST :

                    for (String myResult : myResultList) {

                        propertiesIn.add(new Property(myLabel, myResult));
                    }
                    break;

                case PARAMETERS_TABLE :

                    // Should be MULTIPLE_ENTRY -- PARSE

                    for (int i = 0; myResultList.size() > i; i++) {

                        propertiesIn.add(new Property(JdbcDriverParameterKey.PARAMS_PREFIX.getLabel() + PERIOD + Integer.toString(i), myResultList.get(i))); //$NON-NLS-1$
                    }
                    break;

                case XML_NAMESPACE : {

                    // Should be MULTIPLE_ENTRY

                    for (int i = 0; myResultList.size() > i; i++) {

                        propertiesIn.add(new Property(JdbcDriverParameterKey.SCHEMA_NAMESPACE_PREFIX.getLabel() + PERIOD + Integer.toString(i), myResultList.get(i))); //$NON-NLS-1$
                    }
                    break;
                }

                case XML_COLUMNS_TABLE :

                    // Should be MULTIPLE_ENTRY -- PARSE

                    for (int i = 0; myResultList.size() > i; i++) {

                        String myResult = myResultList.get(i);
                        String[] myTriplet = myResult.split(_splitToken);

                        if ((2 < myTriplet.length) && (0 < myTriplet[0].length()) && (0 < myTriplet[1].length())) {

                            propertiesIn.add(new Property(JdbcDriverParameterKey.SCHEMA_COLUMNS.getLabel() + PERIOD + myTriplet[0], //$NON-NLS-1$
                                    Integer.toString(i) + PIPE + myTriplet[2] + PIPE + myTriplet[1])); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    break;

                default :

                    throw new CentrifugeException(i18n.dataviewFromScratchWizardParameterErrorMessage()
                                                    + Format.value(parameterIn.getLabel()));
            }

        } else if (parameterIn.getRequired()) {

            throw new CentrifugeException(i18n.dataviewFromScratchWizardMissingParameterErrorMessage()
                                            + Format.value(parameterIn.getLabel()));
        }
        return myTableIdentified;
    }

    private String[] getQueryDefaults(List<KeyValueItem> DefaultListIn) {

        String[] myDefaults = new String[] {null, null};

        for (int i = DefaultListIn.size(); 0 < i; i--) {

            KeyValueItem myPair = DefaultListIn.get(i - 1);
            String myKey = myPair.getKey();

            if (null != myKey) {

                if (myKey.equalsIgnoreCase(JdbcDriverParameterKey.PRE_QUERY.getKey())) {

                    myDefaults[0] = myPair.getValue();

                } else if (myKey.equalsIgnoreCase(JdbcDriverParameterKey.POST_QUERY.getKey())) {

                    myDefaults[1] = myPair.getValue();
                }
            }
        }

        return myDefaults;
    }

    private int constructParameterPanels(List<ConfigItem> configItemListIn) {

        _configItemList = configItemListIn;

        return _configItemList.size();
    }

    private CsiDataType identifyType(List<KeyValueItem> validationRulesIn) {

        CsiDataType myType = CsiDataType.String;

        if ((null != validationRulesIn) && (0 < validationRulesIn.size())) {

            for (KeyValueItem myPair : validationRulesIn) {

                if (TRUE.equalsIgnoreCase(myPair.getValue())) { //$NON-NLS-1$

                    if (JdbcDriverParameterValidationType.ISNUMBER.getLabel().equalsIgnoreCase(myPair.getKey())) {

                        myType = CsiDataType.Integer;

                    } else if (JdbcDriverParameterValidationType.ISVALUE.getLabel().equalsIgnoreCase(myPair.getKey())) {

                        myType = CsiDataType.Number;
                    }
                }
            }
        }
        return myType;
    }

    private List<String> buildExtensionList(List<KeyValueItem> validationRulesIn) {

        List<String> myList = new ArrayList<String>();

        if ((null != validationRulesIn) && (0 < validationRulesIn.size())) {

            for (KeyValueItem myPair : validationRulesIn) {

                if (myPair.getKey().equalsIgnoreCase(FILE_TYPE)) { //$NON-NLS-1$

                    String myExtension = myPair.getValue();
                    int myIndex = myExtension.lastIndexOf('.');

                    if (-1 == myIndex) {

                        myList.add(myExtension);

                    } else if ((myExtension.length() - 1) > myIndex) {

                        myList.add(myExtension.substring(myIndex + 1));
                    }
                }
            }
        }
        return myList;
    }

    private String buildIntermediateInfo(ConfigItem parameterIn, int indexIn) {

        JdbcDriverParameterType myType = JdbcDriverParameterType.getValue(parameterIn.getType());
        JdbcDriverParameterKey myKey = JdbcDriverParameterKey.getValue(parameterIn.getKey());
        String myHelp = parameterIn.getHelpText();

        String myPart_1 = parameterIn.getRequired() ? _txtRequiredParameter : _txtOptionalParameter;
        String myPart_2 = ((null != myHelp) && (0 < myHelp.length())) ? myHelp + "\n" : ""; //$NON-NLS-1$ //$NON-NLS-2$
        String myPart_3 = ""; //$NON-NLS-1$
        String myPart_4 = _txtSingleParameter;

        switch (myType) {

            case STRING :

                if (JdbcDriverParameterKey.USERNAME == myKey) {

                    myPart_3 = _constants.connectorParameterInstructions_USERNAME();

                } else if (JdbcDriverParameterKey.PASSWORD == myKey) {

                    myPart_3 = _constants.connectorParameterInstructions_PASSWORD();

                } else {

                    myPart_3 = _constants.connectorParameterInstructions_STRING();
                }
                break;

            case LEGACY_CONNECTION_STRING :

                myPart_3 = _constants.connectorParameterInstructions_LEGACY_CONNECTION_STRING();
                break;

            case PASSWORD :

                myPart_3 = _constants.connectorParameterInstructions_PASSWORD();
                break;

            case BOOLEAN :

                myPart_3 = _constants.connectorParameterInstructions_BOOLEAN();
                break;

            case FILE :

                myPart_3 = _constants.connectorParameterInstructions_FILE();
                break;

            case SELECT_LIST :

                myPart_3 = _constants.connectorParameterInstructions_SELECT_LIST();
                break;

            case QUERY_COMMANDS :

                myPart_3 = _constants.connectorParameterInstructions_QUERY_COMMANDS();
                break;

            case PARAMETERS_TABLE :

                myPart_3 = _constants.connectorParameterInstructions_PARAMETERS_TABLE();
                break;

            case XML_NAMESPACE :

                myPart_3 = _constants.connectorParameterInstructions_XML_NAMESPACE();
                break;

            case XML_COLUMNS_TABLE :

                myPart_3 = _constants.connectorParameterInstructions_XML_COLUMNS_TABLE();
                break;

            default :

                myPart_3 = _constants.connectorParameterInstructions_DEFAULT();
                break;
        }

        if ((JdbcDriverParameterType.XML_NAMESPACE == myType)
                || (JdbcDriverParameterType.PARAMETERS_TABLE == myType)
                || (JdbcDriverParameterType.XML_COLUMNS_TABLE == myType)) {

            myPart_4 = _txtMultipleParameter;
        }

        return _constants.dataviewFromScratchWizard_ConnectionParameterInfo(myPart_1, myPart_2, myPart_3, myPart_4);
    }

    private boolean buildPropertyList() throws CentrifugeException {

        boolean myTableIdentified = false;
        List<Property> myProperties = new ArrayList<Property>();

        _connectionProperties = null;
        _baseName = "?"; //$NON-NLS-1$

        //
        // Gather data from the previous panels
        //
        for (int i = 0; _parameterCount > i; i++) {

            if (extractPropertyData(getPanel(i), _configItemList.get(i), myProperties)) {

                myTableIdentified = true;
            }
        }

        _connectionProperties = myProperties;

        return myTableIdentified || isInstalledTable(_driverKey);
    }

    private ConnectionDef buildConnection() throws CentrifugeException {

        ConnectionDef myConnection = new ConnectionDef();

        GenericProperties myGenericProperties = new GenericProperties();
        myGenericProperties.setProperties(_connectionProperties);
        Map<String, String> myPropertiesMap = myGenericProperties.getPropertiesMap();
        CsiMap<String, String> myCsiMap = new CsiMap<String, String>();
        myCsiMap.putAll(myPropertiesMap);

        myConnection.setType(_driverKey);
        myConnection.setProperties(myGenericProperties);
        myConnection.setClientProperties(myCsiMap);
        myConnection.setConnectString(_connectionString);
        myConnection.setPreSql(_preQueryString);
        myConnection.setPostSql(_postQueryString);

        return myConnection;
    }

    private DataSourceDef updateSourceDef(ConnectionDef connectionIn) throws CentrifugeException {

        _dataSource.setName(createSourceName(connectionIn));
        _dataSource.setConnection(connectionIn);
        return _dataSource;
    }

    private DataSourceDef createBasicSourceDef(ConnectionDef connectionIn) throws CentrifugeException {

        DataSourceDef mySourceDef = new DataSourceDef();
        String myName = createSourceName(connectionIn);
        CsiMap<String, String> myProperties = new CsiMap<String, String>();

        myProperties.put("color", "13593813"); //$NON-NLS-1$ //$NON-NLS-2$
        mySourceDef.setOrdinal(0);
        mySourceDef.setClientProperties(myProperties);
        mySourceDef.setName(createSourceName(connectionIn));
        mySourceDef.setLocalId(UUID.randomUUID().toString().toLowerCase());
        mySourceDef.setConnection(connectionIn);
        mySourceDef.setInPlace(_inPlace);
        mySourceDef.setSingleTable(_singleTable);
        mySourceDef.setSimpleLoader(_simpleLoader);
        return mySourceDef;
    }

    private String createSourceName(ConnectionDef connectionIn) {

        StringBuilder myBuffer = new StringBuilder();
        String myOwner = null;
        String mySource = null;
        String myConnection = connectionIn.getType();

        if (null != _baseName) {

            String[] mySection = _baseName.split("/"); //$NON-NLS-1$
            int mySize = mySection.length;

            if ((4 == mySize) && mySection[0].equals(USERFILES) && mySection[2].equals(DATAFILES)) { //$NON-NLS-1$ //$NON-NLS-2$

                myOwner = mySection[1];
                mySource = mySection[3];

            } else {

                mySection = _baseName.split("\\\\"); //$NON-NLS-1$
                mySize = mySection.length;

                if ((4 == mySize) && mySection[0].equals(USERFILES) && mySection[2].equals(DATAFILES)) { //$NON-NLS-1$ //$NON-NLS-2$

                    myOwner = mySection[1];
                    mySource = mySection[3];

                } else {

                    mySource = _baseName;
                }
            }

            myBuffer.append(mySource);
            if (null != myOwner) {

                myBuffer.append(" ("); //$NON-NLS-1$
                myBuffer.append(myOwner);
                myBuffer.append(')');
            }
            if (null != myConnection) {

                myBuffer.append(" ["); //$NON-NLS-1$
                myBuffer.append(myConnection);
                myBuffer.append(']');
            }
        }
        return myBuffer.toString();
    }

    //
    // Request the configuration items for a specific connection type
    //

    private void extractDataSourceInformation(DataSourceDef dataSourceIn) {

        _dataSource = dataSourceIn;
        _driverKey = _dataSource.getConnection().getType();
        showWatchBox(i18n.dataviewFromScratchWizardWatchboxMessage()); //$NON-NLS-1$

        VortexFuture<Response<String, DriverConfigInfo>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            myVortexFuture.addEventHandler(handleConfigItemListResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).getConnectionUIConfigByKey(_driverKey);

        } catch (Exception myException) {

            Display.error("DataSourceWizard", 8, myException);
        }
    }

    //
    // Request the list uploaded files from the server to prevent naming conflicts
    //
    private void retrieveFileList(VortexEventHandler<List<List<SelectorBasics>>> handlerIn, List<String> listIn) {

        VortexFuture<List<List<SelectorBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handlerIn);
            myVortexFuture.execute(UserFileActionsServiceProtocol.class).getFileOverWriteControlLists(DATAFILES, listIn); //$NON-NLS-1$

        } catch (Exception myException) {

            Display.error("DataSourceWizard", 9, myException);
        }
    }

    private boolean isInstalledTable(String keyIn) {

        return "installedtabledriver".equals(keyIn);
    }

    private void processSelection(DriverBasics selectionIn) {

        _driverKey = selectionIn.getKey();
        _inPlace = selectionIn.isInPlace();
        _singleTable = selectionIn.isSingleTable();
        _simpleLoader = selectionIn.isSimpleLoader();
    }

    private void activateWizard() {

        displayNewPanel(0, null);
    }
}
