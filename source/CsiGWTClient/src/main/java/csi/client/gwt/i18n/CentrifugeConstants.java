package csi.client.gwt.i18n;

import com.google.gwt.i18n.client.Messages;

import java.util.Map;

public interface CentrifugeConstants extends Messages {
    @SuppressWarnings("GwtInconsistentI18nInterface")
    public void initialize(Map<String, String> properties);

    public String Help_AboutDialog_Title();

    public String Help_AboutDialog_Info(String versionIn);

    public String Help_Analytics_Dialog_Title(String versionIn);

    public String Help_Analytics_Dialog_Info();

    @Meaning("See through")
    @Description("This string appears in the heading for the Transparency Settings for the graph visualization")
    public String transparency();

    public String editNode();

    public String rename();

    public String delete();

    public String cancel();

    public String close();

    public String save();

    public String add();

    @Meaning("The 'white-space' in a vizualization")
    public String background();

    @Meaning("The 'option set' used to style nodes and edges in the graph visualization")
    public String theme();

    public String testQuery();

    public String inputParameters();

    public String title();

    public String graphSettings();

    public String renderThreshold();

    public String find();

    public String tooltipDetails();

    public String tooltips();

    public String name();

    @Meaning("Kind")
    @Description("Some concepts have child concepts. Assigning a type to each child concepts allows them to be distinguished.")
    public String type();

    public String source();

    public String operator();

    @Meaning("Assigned attribute.")
    @Description("The value is an generally a numeric amount, but can also be a string or object.")
    public String value();

    public String sum();

    public String count();

    public String countDistinct();

    public String continuousColorPickerTitle();

    public String continuousColorPickerInstructions();

    public String continuousColorPickerRangeHeader();

    public String discreteColorPickerTitle();

    public String discreteColorPickerCategoryLabel();

    public String discreteColorPickerSizelabel();

    public String discreteColorType_DIVERGING();

    public String discreteColorType_QUALITATIVE();

    public String discreteColorType_SEQUENTIAL();

    public String minimum();

    public String maximum();

    public String average();

    public String absoluteAverage();

    public String absoluteSum();

    public String numberOfNeighbors();

    public String neighbors();

    public String editLink();

    public String dialog_ExceptionTitle();

    public String dialog_ErrorTitle();

    public String dialog_UnknownError();

    public String dialog_WarningTitle();

    public String dialog_ProblemTitle();

    public String dialog_DecisionTitle();

    public String dialog_InfoTitle();

    public String dialog_WatchBoxTitle();

    public String dialog_LogonTitle();

    public String dialog_LogonButton();

    public String dialog_LogonPrompt();

    public String dialog_UsernamePrompt();

    public String dialog_PasswordPrompt();

    public String dialog_RetryButton();

    public String dialog_UndoButton();

    public String dialog_YesButton();

    public String dialog_NoButton();

    public String dialog_TrueButton();

    public String dialog_FalseButton();

    public String dialog_SaveButton();

    public String dialog_OkayButton();

    public String dialog_ContinueButton();

    public String dialog_ExecuteButton();

    public String dialog_AdvancedButton();

    public String dialog_DeleteButton();

    public String dialog_ExitButton();

    public String dialog_UpdateButton();

    public String dialog_AddButton();

    public String dialog_OverwriteButton();

    public String dialog_SkipButton();

    public String dialog_CancelButton();

    public String dialog_SelectButton();

    public String dialog_ApplyButton();

    public String dialog_CreateButton();

    public String dialog_NewFieldButton();

    public String dialog_OpenButton();

    public String dialog_NextButton();

    public String dialog_PreviousButton();

    public String dialog_HideTraceButton();

    public String dialog_ShowTraceButton();

    public String dialog_ExportButton();

    public String dialog_ImportButton();

    public String dialog_FinishButton();

    public String dialog_LaunchButton();

    public String dialog_RefreshButton();

    public String dialog_TestButton();

    public String dialog_FieldsButton();

    public String dialog_ParametersButton();

    public String dialog_MapButton();

    public String dialog_PropertiesButton();

    public String dialog_InformationButton();

    public String dialog_UnmapButton();

    public String dialog_ShowFullMessageButton();

    public String dialog_HideFullMessageButton();

    public String dialog_ConnectionTestLabel();

    public String dialog_EditButton();

    public String dialog_ReloadButton();

    public String dialog_InstallButton();

    public String dialog_RenameButton();

    public String dialog_ReplaceButton();

    public String dialog_CopyButton();

    public String dialog_ActionNotSupported();

    public String helpFolder();

    public String helpWindowTitle();

    public String linkupNewFieldDialog_HelpTarget();

    public String advancedLinkupParameterDialog_HelpTarget();

    public String linkupDefinitionPanel_HelpTarget();

    public String linkupSelectionDialog_HelpTarget();

    public String fromScratchWizard_HelpTarget(String resourceTypeIn);

    public String dataviewFromTemplateWizard_HelpTarget();

    public String dataviewSaveAsDialog_HelpTarget();

    public String templateSaveAsDialog_HelpTarget();

    public String dataviewSaveAsTemplateDialog_HelpTarget();

    public String exportDialog_HelpTarget();

    public String newFromTemplateDialog_HelpTarget_1();

    public String newFromTemplateDialog_HelpTarget_2();

    public String newFromTemplateDialog_HelpTarget_3();

    public String openDataviewDialog_HelpTarget();

    public String parameterEditor_HelpTarget();

    public String resourceFilterListDialog_HelpTarget();

    public String dataSourceEditor_ParameterListHelpTarget();

    public String dataSourceEditor_radioButton_smallDisplay();

    public String dataSourceEditor_radioButton_largeDisplay();

    public String dataSourceEditor_checkbox_hideConnectors();

    public String customQueryDialog_CreateHelpTarget();

    public String customQueryDialog_EditHelpTarget();

    public String administrationDialogs_UserPopupHelpTarget();

    public String administrationDialogs_SharingPopupHelpTarget();

    public String administrationDialogs_ReaperPopupHelpTarget();

    public String administrationDialogs_SecurityPopupHelpTarget();

    public String derivedFieldDialog_HelpTarget();

    public String sqlFunctionConstantDialog_HelpTarget();

    public String installFileWizard_HelpTarget();

    public String resourceSharingDialog_txtHelpTarget();

    public String resourceSecurityDialog_txtHelpTarget();

    public String administrationDialogs_UnselectedGroups();

    public String administrationDialogs_SelectedGroups();

    public String administrationDialogs_UnselectedClearances();

    public String administrationDialogs_SelectedClearances();

    public String administrationDialogs_GroupGroupsPrompt();

    public String administrationDialogs_EncapsulateClearancesPrompt();

    public String administrationDialogs_UserGroupsPrompt();

    public String administrationDialogs_ViewGroups();

    public String administrationDialogs_EditGroups();

    public String administrationDialogs_EditClearances();

    public String administrationDialogs_ConfirmPassword(String usernameIn);

    public String administrationDialogs_SharingColumn_1();

    public String administrationDialogs_SharingColumn_2();

    public String administrationDialogs_SharingColumn_3();

    public String administrationDialogs_SharingColumn_4();

    public String administrationDialogs_SecurityColumn_1();

    public String administrationDialogs_SecurityColumn_2();

    public String administrationDialogs_SecurityColumn_3();

    public String administrationDialogs_SecurityColumn_4();

    public String administrationDialogs_SecurityColumn_5();

    public String administrationDialogs_SecurityColumn_6();

    public String administrationDialogs_UserColumn_01();

    public String administrationDialogs_UserColumn_02();

    public String administrationDialogs_UserColumn_03();

    public String administrationDialogs_UserColumn_04();

    public String administrationDialogs_UserColumn_05();

    public String administrationDialogs_UserColumn_06();

    public String administrationDialogs_UserColumn_07();

    public String administrationDialogs_UserColumn_08();

    public String administrationDialogs_UserColumn_09();

    public String administrationDialogs_UserColumn_10();

    public String administrationDialogs_UserColumn_11();

    public String administrationDialogs_UserColumn_12();

    public String administrationDialogs_UserColumn_13();

    public String administrationDialogs_ReportsColumn_1();

    public String administrationDialogs_ReportsColumn_2();

    public String administrationDialogs_ReportsColumn_3();

    public String administrationDialogs_ReportsColumn_4();

    public String administrationDialogs_ReportsColumn_5();

    public String administrationDialogs_CannotDeleteUser1(final String userIn);

    public String administrationDialogs_CannotDeleteUser2(final String userIn);

    public String administrationDialogs_CannotDeactivateUser1(final String userIn);

    public String administrationDialogs_CannotDeactivateUser2(final String userIn);

    public String administrationDialogs_CannotRemoveUserFromGroup1(final String userIn, final String groupIn);

    public String administrationDialogs_CannotRemoveUserFromGroup2(final String userIn, final String groupIn);

    public String administrationDialogs_CannotAddUserToGroup1(final String userIn, final String groupIn);

    public String administrationDialogs_CannotAddUserToGroup2(final String userIn, final String groupIn);

    public String administrationDialogs_CannotDeleteGroup1(final String groupIn);

    public String administrationDialogs_CannotDeleteGroup2(final String groupIn);

    public String administrationDialogs_CannotAddGroupToGroup1(final String groupIn);

    public String administrationDialogs_CannotAddGroupToGroup2(final String groupIn);

    public String parameterLinkupHelpTarget();

    public String parameterRefreshHelpTarget();

    public String parameterTestHelpTarget();

    public String parameters_UnsupportedType();

    public String parameters_UnnamedParameter();

    public String parameters_DefaultDialogDescription(String nameIn);

    public String fileColision_InfoString(String fileNameIn);

    public String fileColisionChoice_InfoString(String overwriteButtonIn, String skipButtonIn, String cancelButtonIn);

    public String uploadWidget_DialogTitle();

    public String uploadWidget_Choice_DialogTitle();

    public String parameterRefreshTitle();

    public String parameterLinkupTitle();

    public String parameterTestTitle();

    public String parameterMissingList();

    public String parameterInfo_ControlTitle();

    public String parameterInfo_ControlInstructions(int totalCountIn, int optionalParametersIn, int requiredParametersIn);

    public String parameterInfo_ControlInfo();

    public String parameterInfo_DisplayAll();

    public String parameterInfo_DisplayUsed();

    public String parameterInfo_DisplayRequired();

    public String parameterInfo_TotalCount(int totalCountIn);

    public String parameterInfo_FilterCount(int requiredCountIn);

    public String parameterInfo_String();

    public String parameterInfo_Boolean();

    public String parameterInfo_Integer();

    public String parameterInfo_Number();

    public String parameterInfo_DateTime();

    public String parameterInfo_Date();

    public String parameterInfo_Time();

    public String parameterInfo_MultiValue();

    public String parameterFormat_mustBeValue();

    public String parameterFormat_mustBeNumber();

    public String parameterFormat_MinCharacters(String valueIn);

    public String parameterFormat_MaxCharacters(String valueIn);

    public String parameterFormat_CharacterLimits(String minIn, String maxIn);

    public String parameterFormat_MinSelection(String valueIn);

    public String parameterFormat_MaxSelection(String valueIn);

    public String parameterFormat_SelectionLimits(String minIn, String maxIn);

    public String parameterFormat_MinValue(String valueIn);

    public String parameterFormat_MaxValue(String valueIn);

    public String parameterFormat_valueLimits(String minIn, String maxIn);

    public String parameterFormat_regularExpression();

    public String parameterFormat_ConfigurationProblem_Dialog(String typeIn);

    public String parameterFormat_ConfigurationProblem(String typeIn);

    public String connectorParameterPrompt_Hostname();

    public String connectorParameterPrompt_Port();

    public String connectorParameterPrompt_Username();

    public String connectorParameterPrompt_Password();

    public String connectorParameterPrompt_InstanceName();

    public String connectorParameterPrompt_DatabaseName();

    public String connectorParameterPrompt_ParameterName();

    public String connectorParameterPrompt_ParameterValue();

    public String connectorParameterPrompt_UseExisting();

    public String connectorParameterPrompt_HasHeaders();

    public String connectorParameterPrompt_RowDelimiter();

    public String connectorParameterPrompt_CellDelimiter();

    public String connectorParameterPrompt_TableName();

    public String connectorParameterPrompt_XPath();

    public String connectorParameterPrompt_ColumnName();

    public String connectorParameterPrompt_ColumnPath();

    public String connectorParameterPrompt_ColumnType();

    public String connectorParameterPrompt_Charset();

    public String connectorParameterPrompt_DateFormat();

    public String connectorParameterPrompt_Namespace();

    public String connectorParameterPrompt_FileToken();

    public String connectorParameterPrompt_LocalFilePath();

    public String connectorParameterPrompt_RemoteFilePath();

    public String connectorParameterPrompt_FilePath();

    public String connectorParameterPrompt_QueryTableName();

    public String connectorParameterPrompt_PreQueryCommand();

    public String connectorParameterPrompt_PostQueryCommand();

    public String connectorParameterPrompt_ConnectionString();

    public String connectorParameterPrompt_EnterParameter();

    public String connectorParameterTitle_Credentials();

    public String connectorParameterTitle_DbInfo();

    public String connectorParameterTitle_FileFormat();

    public String connectorParameterTitle_XmlSchema();

    public String connectorParameterTitle_FileIdentification();

    public String connectorParameterTitle_ParameterEntry();

    public String connectorParameterInstructions_USERNAME();

    public String connectorParameterInstructions_PASSWORD();

    public String connectorParameterInstructions_STRING();

    public String connectorParameterInstructions_LEGACY_CONNECTION_STRING();

    public String connectorParameterInstructions_BOOLEAN();

    public String connectorParameterInstructions_FILE();

    public String connectorParameterInstructions_SELECT_LIST();

    public String connectorParameterInstructions_QUERY_COMMANDS();

    public String connectorParameterInstructions_PARAMETERS_TABLE();

    public String connectorParameterInstructions_XML_NAMESPACE();

    public String connectorParameterInstructions_XML_COLUMNS_TABLE();

    public String connectorParameterInstructions_DEFAULT();

    public String multipleSelector_DefaultListLabel();

    public String serverRequest_SuccessDialogTitle();

    public String serverRequest_FailureDialogTitle();

    public String serverRequest_ErrorDialogTitle();

    public String serverRequest_CreationSuccessMessage();

    public String serverRequest_WaitingForTableList();

    public String serverRequest_WaitingForColumnList();

    public String resourceType_Dataview();

    public String resourceType_Template();

    public String resourceType_DataModel();

    public String resourceType_Visualization();

    public String resourceType_DataSource();

    public String resourceType_Connection();

    public String resourceType_Query();

    public String resourceType_Unknown();

    public String displayDataViewList();

    public String displayTemplateList();

    public String displayThemeList();

    public String error_RetrieveDataviewList_Title();

    public String error_RetrieveTemplateList_Title();

    public String error_RetrieveTemplateData_Title();

    public String error_RetrieveLinkupData_Title();

    public String deleteItems_Title();

    public String deleteItems_Description(String continueButtonIn, String cancelButtonIn);

    public String resourceSelector_DialogTitle(String resourceTypeIn);

    public String resourceSelector_ListLabel();

    public String resourceSelector_NameLabel();

    public String resourceSelector_RemarksLabel();

    public String resourceSelector_OverwriteDialogTitle(String resourceTypeIn);

    public String resourceSelector_OverwriteInfoString(String resourceTypeIn, String continueButtonIn, String cancelButtonIn);

    public String linkupGridMapper_TemplateFieldHeader();

    public String linkupGridMapper_TemplateFieldDataHeader();

    public String linkupGridMapper_SelectedDataViewFieldHeader();

    public String linkupGridMapper_DataViewFieldHeader();

    public String linkupGridMapper_ErrorTitle();

    public String linkupGridMapper_EmptyParameterError();

    public String linkupGridMapper_BadFieldError();

    public String linkupFieldMapDisplay_MappedToHeader();

    public String linkupGridMapper_IncludeCheckBoxHeader();

    public String linkupParameterMapper_TemplateParameterHeader();

    public String linkupParameterMapper_FieldHeader();

    public String linkupConditionalFieldMapper_RequiredTemplateFieldLabel();

    public String linkupConditionalFieldMapper_RequiredMappedFieldLabel();

    public String linkupConditionalFieldMapper_ButtonColumnHeader();

    public String advancedLinkupParameterDialog_DialogTitle();

    public String advancedLinkupParameterDialog_DisabledCheckBox();

    public String advancedLinkupParameterDialog_RequiredParameterSetIdentification();

    public String advancedLinkupParameterDialog_DelayedInputOption();

    public String advancedLinkupParameterDialog_AvailableVisualizationsLabel();

    public String advancedLinkupParameterDialog_SetAddButton();

    public String advancedLinkupParameterDialog_SetDeleteButton();

    public String advancedLinkupParameterDialog_SetNameLabel();

    public String advancedLinkupParameterDialog_SetDescriptionLabel();

    public String advancedLinkupParameterDialog_UseAllRadioButton();

    public String advancedLinkupParameterDialog_UseNodeRadioButton();

    public String advancedLinkupParameterDialog_UseLinkRadioButton();

    public String advancedLinkupParameterDialog_FillParameterGridLabel();

    public String advancedLinkupParameterDialog_IgnoreParameterGridLabel();

    public String advancedLinkupParameterDialog_FieldGridLabel();

    public String advancedLinkupParameterDialog_NotRelGraphLabel();

    public String advancedLinkupParameterDialog_NoNodesLabel();

    public String advancedLinkupParameterDialog_NoEdgesLabel();

    public String advancedLinkupParameterDialog_SelectVisualization();

    public String advancedLinkupParameterDialog_AddParameterSet();

    public String advancedLinkupParameterDialog_EnterParameterSetName();

    public String advancedLinkupParameterDialog_SelectNode();

    public String advancedLinkupParameterDialog_SelectEdge();

    public String advancedLinkupParameterDialog_NoParametersLabel();

    public String advancedLinkupParameterDialog_MissingReferencedViz(String countIn);

    public String linkupNewFieldDialog_DialogTitle();

    public String linkupNewFieldDialog_InstructionOne();

    public String linkupNewFieldDialog_InstructionTwo();

    public String linkupDefinitionPanel_DialogTitle();

    public String linkupDefinitionPanel_ServerRequestSuccessTitle();

    public String linkupDefinitionPanel_ServerRequestSuccessMessage();

    public String linkupDefinitionPanel_ServerDeleteFailureTitle();

    public String linkupDefinitionPanel_ServerUpdateFailureTitle();

    public String linkupDefinitionPanel_MappingMissing();

    public String linkupDefinitionPanel_MappingConflict();

    public String linkupDefinitionPanel_DelayedInputOption();

    public String linkupDefinitionPanel_MappingInstructions();

    public String linkupDefinitionPanel_LinkupNameLabel();

    public String linkupDefinitionPanel_AvailableTemplatesLabel();

    public String linkupDefinitionPanel_CreateLinkupLabel();

    public String linkupDefinitionPanel_EditLinkupLabel();

    public String linkupDefinitionPanel_NeedSelection();

    public String linkupDefinitionPanel_FillParameterGridLabel();

    public String linkupDefinitionPanel_IgnoreParameterGridLabel();

    public String linkupDefinitionPanel_FieldGridLabel();

    public String linkupDefinitionPanel_NoParametersLabel();

    public String linkupDefinitionPanel_SelectTemplate();

    public String linkupDefinitionPanel_EnterName();

    public String linkupDefinitionPanel_SelectLinkup();

    public String linkupDefinitionPanel_ReadOnly();

    public String linkupDefinitionPanel_ReturnAllRows();

    public String linkupDefinitionPanel_ReturnUniqueRows();

    public String linkupDefinitionPanel_DataModeLabel();

    public String linkupSelectionDialog_DialogTitle();

    public String linkupSelectionDialog_FailureDialogTitle();

    public String linkupSelectionDialog_ErrorDialogTitle();

    public String linkupSelectionDialog_FillParameterGridLabel();

    public String linkupSelectionDialog_IgnoreParameterGridLabel();

    public String linkupSelectionDialog_FieldGridLabel();

    public String linkupSelectionDialog_LinkupNameLabel();

    public String linkupSelectionDialog_TemplateNameLabel();

    public String linkupSelectionDialog_NewDataviewNameLabel();

    public String linkupSelectionDialog_DisabledCheckBox();

    public String linkupSelectionDialog_UseDefaultRadioButton();

    public String linkupSelectionDialog_UseNodeEdgeRadioButton();

    public String linkupSelectionDialog_MergeRadioButton();

    public String linkupSelectionDialog_SpinOffRadioButton();

    public String linkupSelectionDialog_SpinUpRadioButton();

    public String linkupSelectionDialog_MergeRadioButtonExplanation();

    public String linkupSelectionDialog_SpinOffRadioButtonExplanation();

    public String linkupSelectionDialog_SpinUpRadioButtonExplanation();

    public String linkupSelectionDialog_IgnoreNullsCheckBox();

    public String linkupSelectionDialog_DataSelectionRequired();

    public String linkupSelectionDialog_NoLinkupInfo();

    public String linkupSelectionDialog_SuccessResponseTitle();

    public String linkupSelectionDialog_SuccessResponseMessage(long rowCountIn);

    public String linkupSelectionDialog_NoDataviewNameTitle();

    public String linkupSelectionDialog_NoDataviewNameMessage(final String continueButtonIn, final String cancelButtonIn);

    public String linkupSelectionDialog_NoChangesSaved();

    public String linkupSelectionDialog_NoParametersSet();

    public String linkupSelectionDialog_NoExtendersLabel();

    public String linkupSelectionDialog_NoMappingLabel();

    public String linkupSelectionDialog_NoParametersLabel();

    public String linkupSelectionDialog_NoMappingsLabel();

    public String linkupSelectionDialog_EverythingDisabled();

    public String fromScratchWizard_DialogTitle(String resourceTypeIn);

    public String newSourceWizard_DialogTitle();

    public String dataviewFromScratchWizard_InfoString();

    public String fromScratchWizard_ModeSelectionPanel(final String buttonIn);

    public String dataviewFromScratchWizard_ConnectorPanel(final String buttonIn);

    public String dataviewFromScratchWizard_ConnectionTreePanel(final String buttonIn);

    public String dataviewFromScratchWizard_ConnectionParameterInstallPrompt(String buttonIn);

    public String dataviewFromScratchWizard_ConnectionParameterInstallButton();

    public String dataviewFromScratchWizard_ConnectionParameterInfo(String part1In, String part2In, String part3In, String part4In);

    public String dataviewFromScratchWizard_RequiredConnectionParameter();

    public String dataviewFromScratchWizard_OptionalConnectionParameter();

    public String dataviewFromScratchWizard_SingleParameter(String nextButtonIn);

    public String dataviewFromScratchWizard_MultipleParameter(String addButtonIn, String nextButtonIn);

    public String dataviewFromScratchWizard_QueryPairSubtitle();

    public String dataviewFromScratchWizard_ParameterListSubtitle();

    public String dataviewFromScratchWizard_XmlColumnsSubtitle();

    public String dataviewFromScratchWizard_IntroTitle();

    public String dataviewFromScratchWizard_IntroInfo();

    public String dataviewFromScratchWizard_UseWizard();

    public String dataviewFromScratchWizard_UseDSE();

    public String dataviewFromTemplateWizard_DialogTitle();

    public String dataviewFromTemplateWizard_InfoString();

    public String dataviewFromTemplateWizard_TemplatePanel(final String buttonIn);

    public String dataviewFromTemplateWizard_ParameterPanel(final String parameterIn, final String ordinalIn, final String countIn, final String descriptionIn, final String buttonIn);

    public String dataviewFromTemplateWizard_DataviewPanel(final String buttonIn);

    public String dataviewLaunching_WatchBoxInfo(String nameIn);

    public String creatingResource_WatchBoxInfo(String resourceIn, String nameIn);

    public String createdResource_SuccessBoxInfo(String resourceIn, String nameIn);

    public String successfulMessageTitle();

    public String exportDialog_Choice_InfoString();

    public String exportDialog_Title();

    public String exportDialog_Dataview_InfoString(final String labelIn);

    public String exportDialog_Template_InfoString(final String labelIn);

    public String exportDialog_Theme_InfoString(final String labelIn);

    public String exportDialog_Instructions();

    public String downloadHelper_CallbackTitle();

    public String downloadHelper_CallbackPrompt();

    public String newFromTemplateDialog_InfoString_1(final String nextButtonIn);

    public String newFromTemplateDialog_InfoString_2();

    public String newFromTemplateDialog_InfoString_3();

    public String uncaughtErrorDialog_Title();

    public String uncaughtErrorDialog_GeneralMessage(final int errorHasCode);

    public String incompatibleRemoteServiceException_Title();

    public String incompatibleRemoteServiceException_Message();

    public String openDataviewDialog_InfoString(final String openButtonIn);

    public String openDataviewDialog_Choice_DialogTitle();

    public String openDataviewDialog_Choice_InfoString(final String currentButtonIn, final String newButtonIn);

    public String openDataviewDialog_Choice_NewButtonString();

    public String openDataviewDialog_Choice_CurrentButtonString();

    public String customQueryDialog_QueryName();

    public String customQueryDialog_SQLQuery();

    public String customQueryDialog_QueryInputParameters();

    public String customQueryDialog_CreateTitle();

    public String customQueryDialog_EditTitle();

    public String customQueryDialog_GridCol_Name();

    public String customQueryDialog_GridCol_Prompt();

    public String customQueryDialog_GridCol_Type();

    public String customQueryDialog_GridCol_Paste_Tooltip();

    public String customQueryDialog_TestQuery_Mask();

    public String customQueryDialog_TestQuery_Failed_Title();

    public String customQueryDialog_TestQuery_Failed_Message(final String error);

    public String customQueryDialog_TestQuery_Success_Title();

    public String customQueryDialog_TestQuery_Success_Message();

    public String customQueryDialog_TestQuery_Empty_Title();

    public String customQueryDialog_TestQuery_Empty_Message();

    public String sharingDialogs_SharingColumn_1();

    public String sharingDialogs_SharingColumn_2();

    public String sharingDialogs_SharingColumn_3();

    public String sharingDialogs_SharingColumn_4();

    public String sharingDialogs_SharingColumn_5();

    public String sharingDialogs_SharingColumn_6();

    public String sharingDialogs_SharingColumn_7();

    public String sharingDialogs_SharingColumn_8();

    public String sharingDialogs_SharingColumn_9();

    public String sharingDialogs_SharingColumn_10();

    public String administrationDialogs_UserPopupTitle();

    public String administrationDialogs_SharingPopupTitle();

    public String administrationDialogs_ReaperPopupTitle();

    public String administrationDialogs_SecurityPopupTitle();

    public String fieldList_CreateNew();

    public String fieldList_EditExisting();

    public String fieldList_DeleteError_Title();

    public String fieldList_DeleteError_Text();

    public String fieldList_Title();

    public String fieldList_SaveChanges_Button();

    public String fieldList_FieldTypeText_Column();

    public String fieldList_FieldTypeText_Static();

    public String fieldList_FieldTypeText_Derived();

    public String fieldList_FieldTypeText_Scripted();

    public String fieldList_FieldTypeText_Unknown();

    public String fieldList_GridTitle_Name();

    public String fieldList_GridTitle_DataType();

    public String fieldList_GridTitle_FieldType();

    public String fieldList_GridTitle_UsedVisualizations();

    public String fieldList_GridTitle_SampleValue();

    public String fieldList_HousingTitle_EditField();

    public String fieldList_DeleteErrorPopup_Visualizations();

    public String fieldList_DeleteErrorPopup_Fields();

    public String fieldList_DeleteErrorPopup_Filters();

    public String fieldList_DeleteErrorPopup_Linkups();

    public String fieldList_DeleteErrorPopup_Title();

    public String fieldList_DeleteErrorPopup_Description();

    public String fieldList_FieldEditor_Value();

    public String fieldList_FieldEditor_SelectFunction();

    public String fieldList_FieldEditor_Name();

    public String fieldList_FieldEditor_EditDerivedFieldPrompt();

    public String fieldList_FieldEditor_PreCacheButton();

    public String fieldList_FieldEditor_ReCalculateButton();

    public String fieldList_FieldEditor_DerivedInfo();

    public String fieldList_FieldEditor_DerivedInfoWithPrecache(final String preCacheButton, final String reCalculateButton);

    public String fieldList_FieldEditor_ExpressionPrompt();

    public String fieldList_fieldEditor_inUseViz_None();

    public String fieldList_fieldEditor_inUseViz_containingVizAnd();

    public String fieldList_fieldEditor_inUseViz_otherVisualizations();

    public String fieldList_fieldEditor_inUseViz_dialogTitle();

    public String fieldList_CalculationDuration_Now();

    public String fieldList_CalculationDuration_DurationField();

    public String fieldList_CalculationDuration_DurationNow();

    public String fieldList_Field();

    public String fieldList_CalculationDuration_To();

    public String fieldList_CalculationDuration_TimeUnits();

    public String fieldList_CalculateValue_Operator();

    public String fieldList_Concatenate_AppendText();

    public String fieldList_Concatenate_AppendSpecial();

    public String fieldList_Concatenate_With();

    public String fieldList_Javascript_Toggle();

    public String fieldList_Javascript_Test();

    public String fieldList_Javascript_Success();

    public String fieldList_Substring_Begin();

    public String fieldList_Substring_End();

    public String derivedFieldDialog_Title();

    public String bundle();

    public String dataSourceEditor_StateReady(String buttonIn, String resourceTypeIn);

    public String dataSourceEditor_StateNoAppendMappings();

    public String dataSourceEditor_StateUnusedSource();

    public String dataSourceEditor_StateNoJoinMappings();

    public String dataSourceEditor_StateUnsupportedFields();

    public String dataSourceEditor_StateNoComponents();

    public String dataSourceEditor_StateDanglingComponents();

    public String dataSourceEditor_StateBrokenTree();

    public String dataSourceEditor_StateEmptyTable();

    public String dataSourceEditor_StateUnsupportedTypes();

    public String dataSourceEditor_StateNoMappedFields(String mapperButtonIn);

    public String dataSourceEditor_NoAppendMapping();

    public String dataSourceEditor_NoJoinMapping();

    public String dataSourceEditor_NoSelectedFields();

    public String dataSourceEditor_DeleteObjectHint();

    public String dataSourceEditor_ReplaceObjectHint();

    public String dataSourceEditor_ParametersButtonHint();

    public String dataSourceEditor_PreviewButtonHint();

    public String dataSourceEditor_EditFieldsButtonHint();

    public String dataSourceEditor_MapButtonHint();

    public String dataSourceEditor_AddButtonHint();

    public String dataSourceEditor_NewButtonHint();

    public String dataSourceEditor_EditButtonHint();

    public String dataSourceEditor_DeleteButtonHint();

    public String dataSourceEditor_FieldUnmapButtonHint();

    public String dataSourceEditor_FieldMapButtonHint();

    public String dataSourceEditor_EditParameterListTitle();

    public String dataSourceEditor_FinalizeQueryUpdate_Title();

    public String dataSourceEditor_FinalizeReplace_Title(String tableOrQueryIn);

    public String dataSourceEditor_FinalizeReplace_Message();

    public String parameterEditor_CreateTitle();

    public String parameterEditor_UpdateTitle();

    public String parameterEditor_EditDefaultsButton();

    public String parameterEditor_NameLabel();

    public String parameterEditor_DescriptionLabel();

    public String parameterEditor_PromptLabel();

    public String parameterEditor_AlwaysFillCheckBox();

    public String parameterEditor_NeverPromptCheckBox();

    public String parameterEditor_AlwaysPromptCheckBox();

    public String parameterEditor_AcceptMultipleCheckBox();

    public String parameterEditor_TypeLabel();

    public String parameterEditor_DefaultsLabel();

    public String parameterDefaults_CreateTitle();

    public String parameterDefaults_HelpTarget();

    public String bundleSpecification();

    public String remove();

    public String group_x_by(String name);

    public String plunking_Edit_Node_Title();

    public String plunking_Edit_Link_Title();

    public String plunking_Edit_Node_Label();

    public String plunking_Edit_Node_Type();

    public String plunking_Edit_Node_Size();

    public String plunking_Edit_Node_Transparency();

    public String plunking_Edit_Node_Color();

    public String plunking_Edit_Node_Shape();

    public String plunking_Edit_Node_Icon();

    public String plunking_Edit_Link_Direction();

    public String plunking_Color_Inherited_Info();

    public String plunking_DeleteAll_Dialog_Title();

    public String plunking_DeleteAll_Dialog_Message();

    public String plunking_Size_Help_Info();

    public String plunking_Transparency_Help_Info();

    public String plunking_Size_Error_Text();

    public String plunking_Transparency_Error_Text();

    public String plunking_Width_Help_Info();

    public String importDialog_Browse();

    public String importDialog_InfoDisplay();

    public String importDialog_CurrentTab();

    public String importDialog_NewTab();

    public String importDialog_UnableDisplayDialog();

    public String importDialog_ImportUnsuccessful();

    public String importDialog_DataViewSuccess(String dataViewName);

    public String importDialog_TemplateSuccess(String templateName);

    public String importDialog_ImportResource();

    public String importDialog_ResourceName();

    public String validator_RequiredValue();

    public String validator_RequiredName();

    public String validator_DuplicateName();

    public String validator_RequiredType();

    public String validator_MustBeInteger();

    public String validator_MustBeNumber();

    public String validator_MustBePositiveInteger();

    public String validator_Integer_Between1And5();

    public String advanced();

    public String eigenvector();

    public String closeness();


    public String betweenness();

    public String occurrence();

    public String function();

    public String metric();

    public String fixed();

    public String size();

    public String field();

    public String label();

    public String transparencyShort();

    public String search();

    public String visualizationLoadingError_TitleSuffix(final String visualizationNameIn);

    public String visualizationLoadingError_suggestion();

    public String refreshDataViewDialog_Title();

    public String refreshDataViewDialog_DisplayMessage(final String refreshButton, final String fieldListButton,
                                                       final String sourcesButton, final String closeButton);

    public String refreshDataViewDialog_SourcesButton();

    public String refreshDataViewDialog_FieldListButton();

    public String sqlTokenLabel_BooleanAnd();

    public String sqlTokenLabel_BooleanOR();

    public String sqlTokenLabel_BooleanNOT();

    public String sqlTokenLabel_IsTrue();

    public String sqlTokenLabel_IsNotTrue();

    public String sqlTokenLabel_IsFalse();

    public String sqlTokenLabel_IsNotFalse();

    public String sqlTokenLabel_IsUnknown();

    public String sqlTokenLabel_IsNotUnknown();

    public String sqlTokenLabel_Equal();

    public String sqlTokenLabel_NotEqual();

    public String sqlTokenLabel_IsDistinct();

    public String sqlTokenLabel_IsNotDistinct();

    public String sqlTokenLabel_IsNull();

    public String sqlTokenLabel_IsNotNull();

    public String sqlTokenLabel_LessThan();

    public String sqlTokenLabel_GreaterThan();

    public String sqlTokenLabel_LessThanOrEqual();

    public String sqlTokenLabel_GreaterThanOrEqual();

    public String sqlTokenLabel_Between();

    public String sqlTokenLabel_NotBetween();

    public String sqlTokenLabel_BetweenSymetric();

    public String sqlTokenLabel_NotBetweenSymetric();

    public String sqlTokenLabel_RegularExpression();

    public String sqlTokenLabel_CaselessRegularExpression();

    public String sqlTokenLabel_BitwiseAnd();

    public String sqlTokenLabel_BitwiseOR();

    public String sqlTokenLabel_BitwiseXOR();

    public String sqlTokenLabel_BitwiseNOT();

    public String sqlTokenLabel_ShiftLeft();

    public String sqlTokenLabel_ShiftRight();

    public String sqlTokenLabel_IntegerAddition();

    public String sqlTokenLabel_IntegerSubtraction();

    public String sqlTokenLabel_IntegerMultiplication();

    public String sqlTokenLabel_IntegerDivision();

    public String sqlTokenLabel_ModuloDivision();

    public String sqlTokenLabel_IntegerFactorial();

    public String sqlTokenLabel_IntegerExponentiation();

    public String sqlTokenLabel_DecimalAddition();

    public String sqlTokenLabel_DecimalSubtraction();

    public String sqlTokenLabel_DecimalMultiplication();

    public String sqlTokenLabel_DecimalDivision();

    public String sqlTokenLabel_DecimalExponentiation();

    public String sqlTokenLabel_CastInteger();

    public String sqlTokenLabel_CastDecimal();

    public String sqlTokenLabel_CastDate();

    public String sqlTokenLabel_CastTime();

    public String sqlTokenLabel_CastDateTime();

    public String sqlTokenLabel_CastBoolean();

    public String sqlTokenLabel_TruncateToInteger();

    public String sqlTokenLabel_TruncateDecimal();

    public String sqlTokenLabel_RoundToInteger();

    public String sqlTokenLabel_RoundDecimal();

    public String sqlTokenLabel_AbsoluteInteger();

    public String sqlTokenLabel_AbsoluteDecimal();

    public String sqlTokenLabel_SquareRoot();

    public String sqlTokenLabel_CubeRoot();

    public String sqlTokenLabel_NaturalLog();

    public String sqlTokenLabel_LogBase10();

    public String sqlTokenLabel_Sine();

    public String sqlTokenLabel_Cosine();

    public String sqlTokenLabel_Tangent();

    public String sqlTokenLabel_Cotangent();

    public String sqlTokenLabel_InverseSine();

    public String sqlTokenLabel_InverseCosine();

    public String sqlTokenLabel_InverseTangent();

    public String sqlTokenLabel_InverseCotangent();

    public String sqlTokenLabel_Concatenation();

    public String sqlTokenLabel_ToString();

    public String sqlTokenLabel_CharacterLength();

    public String sqlTokenLabel_ByteCount();

    public String sqlTokenLabel_BitCount();

    public String sqlTokenLabel_Capitalize();

    public String sqlTokenLabel_UpperCase();

    public String sqlTokenLabel_LowerCase();

    public String sqlTokenLabel_LeftTrim1();

    public String sqlTokenLabel_LeftTrim2();

    public String sqlTokenLabel_RightTrim1();

    public String sqlTokenLabel_RightTrim2();

    public String sqlTokenLabel_LeftPad1();

    public String sqlTokenLabel_LeftPad2();

    public String sqlTokenLabel_RightPad1();

    public String sqlTokenLabel_RightPad2();

    public String sqlTokenLabel_Substring1();

    public String sqlTokenLabel_Substring2();

    public String sqlTokenLabel_LocateSubstring();

    public String sqlTokenLabel_CurrentTimestamp();

    public String sqlTokenLabel_CurrentDate();

    public String sqlTokenLabel_CurrentTime();

    public String sqlTokenLabel_DateDateExtract();

    public String sqlTokenLabel_DateTimeExtract();

    public String sqlTokenLabel_DateEpochExtract();

    public String sqlTokenLabel_DateMillenniumExtract();

    public String sqlTokenLabel_DateCenturyExtract();

    public String sqlTokenLabel_DateDecadeExtract();

    public String sqlTokenLabel_DateYearExtract();

    public String sqlTokenLabel_DateQuarterExtract();

    public String sqlTokenLabel_DateMonthExtract();

    public String sqlTokenLabel_DateWeekExtract();

    public String sqlTokenLabel_DateYearDayExtract();

    public String sqlTokenLabel_DateMonthDayExtract();

    public String sqlTokenLabel_DateWeekDayExtract();

    public String sqlTokenLabel_DateHourExtract();

    public String sqlTokenLabel_DateMinuteExtract();

    public String sqlTokenLabel_DateSecondExtract();

    public String sqlTokenLabel_DateMillisecondsExtract();

    public String sqlTokenLabel_DateMicrosecondsExtract();

    public String sqlTokenLabel_DateTimezoneExtract();

    public String sqlTokenLabel_DayPartDifference();

    public String sqlTokenLabel_HourPartDifference();

    public String sqlTokenLabel_MinutePartDifference();

    public String sqlTokenLabel_SecondPartDifference();

    public String sqlTokenLabel_MillisecondPartDifference();

    public String sqlTokenLabel_DateMillenniumTruncate();

    public String sqlTokenLabel_DateCenturyTruncate();

    public String sqlTokenLabel_DateDecadeTruncate();

    public String sqlTokenLabel_DateYearTruncate();

    public String sqlTokenLabel_DateQuarterTruncate();

    public String sqlTokenLabel_DateMonthTruncate();

    public String sqlTokenLabel_DateWeekTruncate();

    public String sqlTokenLabel_DateMonthDayTruncate();

    public String sqlTokenLabel_DateHourTruncate();

    public String sqlTokenLabel_DateMinuteTruncate();

    public String sqlTokenLabel_DateSecondTruncate();

    public String sqlTokenLabel_DateMillisecondsTruncate();

    public String sqlTokenLabel_DateMicrosecondsTruncate();

    public String sqlTokenLabel_StringField();

    public String sqlTokenLabel_BooleanField();

    public String sqlTokenLabel_IntegerField();

    public String sqlTokenLabel_NumberField();

    public String sqlTokenLabel_DateTimeField();

    public String sqlTokenLabel_DateField();

    public String sqlTokenLabel_TimeField();

    public String sqlTokenLabel_StringParameter();

    public String sqlTokenLabel_BooleanParameter();

    public String sqlTokenLabel_IntegerParameter();

    public String sqlTokenLabel_NumberParameter();

    public String sqlTokenLabel_DateTimeParameter();

    public String sqlTokenLabel_DateParameter();

    public String sqlTokenLabel_TimeParameter();

    public String sqlTokenLabel_StringValue();

    public String sqlTokenLabel_BooleanValue();

    public String sqlTokenLabel_IntegerValue();

    public String sqlTokenLabel_NumberValue();

    public String sqlTokenLabel_DateTimeValue();

    public String sqlTokenLabel_DateValue();

    public String sqlTokenLabel_TimeValue();

    public String sqlTokenLabel_Conditional();

    public String sqlTokenLabel_ConditionalComponent1();

    public String sqlTokenLabel_ConditionalComponent2();

    public String sqlTokenLabel_ConditionalDefault();

    public String sqlTokenLabel_FormatValue();

    public String sqlTokenLabel_EpochToDateTime();

    public String sqlTokenLabel_ScanDateTime();

    public String sqlTokenLabel_ScanDate();

    public String sqlTokenLabel_ScanInteger();

    public String sqlTokenLabel_ScanDecimal();

    public String sqlTokenLabel_Extract1stValue();

    public String sqlTokenLabel_Extract2ndValue();

    public String sqlTokenLabel_ExtractNthValue();

    public String sqlTokenLabel_ExtractLastValue();

    public String sqlTokenLabel_NumToken();

    public String sqlTokenLabel_GetSingleToken();

    public String sqlTokenLabel_GetLastToken();

    public String sqlTokenLabel_ExtractMatchingString();

    public String sqlTokenLabel_IfNotNull();

    public String sqlTokenLabel_NullValue();


    public String sqlTokenDescription_BooleanAnd();

    public String sqlTokenDescription_BooleanOR();

    public String sqlTokenDescription_BooleanNOT();

    public String sqlTokenDescription_IsTrue();

    public String sqlTokenDescription_IsNotTrue();

    public String sqlTokenDescription_IsFalse();

    public String sqlTokenDescription_IsNotFalse();

    public String sqlTokenDescription_IsUnknown();

    public String sqlTokenDescription_IsNotUnknown();

    public String sqlTokenDescription_Equal();

    public String sqlTokenDescription_NotEqual();

    public String sqlTokenDescription_IsDistinct();

    public String sqlTokenDescription_IsNotDistinct();

    public String sqlTokenDescription_IsNull();

    public String sqlTokenDescription_IsNotNull();

    public String sqlTokenDescription_LessThan();

    public String sqlTokenDescription_GreaterThan();

    public String sqlTokenDescription_LessThanOrEqual();

    public String sqlTokenDescription_GreaterThanOrEqual();

    public String sqlTokenDescription_Between();

    public String sqlTokenDescription_NotBetween();

    public String sqlTokenDescription_BetweenSymetric();

    public String sqlTokenDescription_NotBetweenSymetric();

    public String sqlTokenDescription_RegularExpression();

    public String sqlTokenDescription_CaselessRegularExpression();

    public String sqlTokenDescription_BitwiseAnd();

    public String sqlTokenDescription_BitwiseOR();

    public String sqlTokenDescription_BitwiseXOR();

    public String sqlTokenDescription_BitwiseNOT();

    public String sqlTokenDescription_ShiftLeft();

    public String sqlTokenDescription_ShiftRight();

    public String sqlTokenDescription_IntegerAddition();

    public String sqlTokenDescription_IntegerSubtraction();

    public String sqlTokenDescription_IntegerMultiplication();

    public String sqlTokenDescription_IntegerDivision();

    public String sqlTokenDescription_ModuloDivision();

    public String sqlTokenDescription_IntegerFactorial();

    public String sqlTokenDescription_IntegerExponentiation();

    public String sqlTokenDescription_DecimalAddition();

    public String sqlTokenDescription_DecimalSubtraction();

    public String sqlTokenDescription_DecimalMultiplication();

    public String sqlTokenDescription_DecimalDivision();

    public String sqlTokenDescription_DecimalExponentiation();

    public String sqlTokenDescription_CastInteger();

    public String sqlTokenDescription_CastDecimal();

    public String sqlTokenDescription_CastDate();

    public String sqlTokenDescription_CastTime();

    public String sqlTokenDescription_CastDateTime();

    public String sqlTokenDescription_CastBoolean();

    public String sqlTokenDescription_TruncateToInteger();

    public String sqlTokenDescription_TruncateDecimal();

    public String sqlTokenDescription_RoundToInteger();

    public String sqlTokenDescription_RoundDecimal();

    public String sqlTokenDescription_AbsoluteInteger();

    public String sqlTokenDescription_AbsoluteDecimal();

    public String sqlTokenDescription_SquareRoot();

    public String sqlTokenDescription_CubeRoot();

    public String sqlTokenDescription_NaturalLog();

    public String sqlTokenDescription_LogBase10();

    public String sqlTokenDescription_Sine();

    public String sqlTokenDescription_Cosine();

    public String sqlTokenDescription_Tangent();

    public String sqlTokenDescription_Cotangent();

    public String sqlTokenDescription_InverseSine();

    public String sqlTokenDescription_InverseCosine();

    public String sqlTokenDescription_InverseTangent();

    public String sqlTokenDescription_InverseCotangent();

    public String sqlTokenDescription_Concatenation();

    public String sqlTokenDescription_ToString();

    public String sqlTokenDescription_CharacterLength();

    public String sqlTokenDescription_ByteCount();

    public String sqlTokenDescription_BitCount();

    public String sqlTokenDescription_Capitalize();

    public String sqlTokenDescription_UpperCase();

    public String sqlTokenDescription_LowerCase();

    public String sqlTokenDescription_LeftTrim1();

    public String sqlTokenDescription_LeftTrim2();

    public String sqlTokenDescription_RightTrim1();

    public String sqlTokenDescription_RightTrim2();

    public String sqlTokenDescription_LeftPad1();

    public String sqlTokenDescription_LeftPad2();

    public String sqlTokenDescription_RightPad1();

    public String sqlTokenDescription_RightPad2();

    public String sqlTokenDescription_Substring1();

    public String sqlTokenDescription_Substring2();

    public String sqlTokenDescription_LocateSubstring();

    public String sqlTokenDescription_CurrentTimestamp();

    public String sqlTokenDescription_CurrentDate();

    public String sqlTokenDescription_CurrentTime();

    public String sqlTokenDescription_DateDateExtract();

    public String sqlTokenDescription_DateTimeExtract();

    public String sqlTokenDescription_DateEpochExtract();

    public String sqlTokenDescription_DateMillenniumExtract();

    public String sqlTokenDescription_DateCenturyExtract();

    public String sqlTokenDescription_DateDecadeExtract();

    public String sqlTokenDescription_DateYearExtract();

    public String sqlTokenDescription_DateQuarterExtract();

    public String sqlTokenDescription_DateMonthExtract();

    public String sqlTokenDescription_DateWeekExtract();

    public String sqlTokenDescription_DateYearDayExtract();

    public String sqlTokenDescription_DateMonthDayExtract();

    public String sqlTokenDescription_DateWeekDayExtract();

    public String sqlTokenDescription_DateHourExtract();

    public String sqlTokenDescription_DateMinuteExtract();

    public String sqlTokenDescription_DateSecondExtract();

    public String sqlTokenDescription_DateMillisecondsExtract();

    public String sqlTokenDescription_DateMicrosecondsExtract();

    public String sqlTokenDescription_DateTimezoneExtract();

    public String sqlTokenDescription_DayPartDifference();

    public String sqlTokenDescription_HourPartDifference();

    public String sqlTokenDescription_MinutePartDifference();

    public String sqlTokenDescription_SecondPartDifference();

    public String sqlTokenDescription_MillisecondPartDifference();

    public String sqlTokenDescription_DateMillenniumTruncate();

    public String sqlTokenDescription_DateCenturyTruncate();

    public String sqlTokenDescription_DateDecadeTruncate();

    public String sqlTokenDescription_DateYearTruncate();

    public String sqlTokenDescription_DateQuarterTruncate();

    public String sqlTokenDescription_DateMonthTruncate();

    public String sqlTokenDescription_DateWeekTruncate();

    public String sqlTokenDescription_DateMonthDayTruncate();

    public String sqlTokenDescription_DateHourTruncate();

    public String sqlTokenDescription_DateMinuteTruncate();

    public String sqlTokenDescription_DateSecondTruncate();

    public String sqlTokenDescription_DateMillisecondsTruncate();

    public String sqlTokenDescription_DateMicrosecondsTruncate();

    public String sqlTokenDescription_StringField();

    public String sqlTokenDescription_BooleanField();

    public String sqlTokenDescription_IntegerField();

    public String sqlTokenDescription_NumberField();

    public String sqlTokenDescription_DateTimeField();

    public String sqlTokenDescription_DateField();

    public String sqlTokenDescription_TimeField();

    public String sqlTokenDescription_StringParameter();

    public String sqlTokenDescription_BooleanParameter();

    public String sqlTokenDescription_IntegerParameter();

    public String sqlTokenDescription_NumberParameter();

    public String sqlTokenDescription_DateTimeParameter();

    public String sqlTokenDescription_DateParameter();

    public String sqlTokenDescription_TimeParameter();

    public String sqlTokenDescription_StringValue();

    public String sqlTokenDescription_BooleanValue();

    public String sqlTokenDescription_IntegerValue();

    public String sqlTokenDescription_NumberValue();

    public String sqlTokenDescription_DateTimeValue();

    public String sqlTokenDescription_DateValue();

    public String sqlTokenDescription_TimeValue();

    public String sqlTokenDescription_Conditional();

    public String sqlTokenDescription_ConditionalComponent1();

    public String sqlTokenDescription_ConditionalComponent2();

    public String sqlTokenDescription_ConditionalDefault();

    public String sqlTokenDescription_FormatValue();

    public String sqlTokenDescription_EpochToDateTime();

    public String sqlTokenDescription_ScanDateTime();

    public String sqlTokenDescription_ScanDate();

    public String sqlTokenDescription_ScanInteger();

    public String sqlTokenDescription_ScanDecimal();

    public String sqlTokenDescription_Extract1stValue();

    public String sqlTokenDescription_Extract2ndValue();

    public String sqlTokenDescription_ExtractNthValue();

    public String sqlTokenDescription_ExtractLastValue();

    public String sqlTokenDescription_NumToken();

    public String sqlTokenDescription_GetSingleToken();

    public String sqlTokenDescription_GetLastToken();

    public String sqlTokenDescription_ExtractMatchingString();

    public String sqlTokenDescription_TimestampPlusTime();

    public String sqlTokenDescription_TimestampMinusTimeStamp();

    public String sqlTokenDescription_TimestampMinusDate();

    public String sqlTokenDescription_TimestampMinusTime();

    public String sqlTokenDescription_DatePlusDays();

    public String sqlTokenDescription_DatePlusTime();

    public String sqlTokenDescription_DateMinusDate();

    public String sqlTokenDescription_DateMinusDays();

    public String sqlTokenDescription_DateMinusTime();

    public String sqlTokenDescription_TimePlusTime();

    public String sqlTokenDescription_TimeMinusTime();

    public String sqlTokenDescription_TimeMultiplied();

    public String sqlTokenDescription_TimeDivided();

    public String sqlTokenDescription_TimeAsTimeOfDay();

    public String sqlTokenDescription_TimestampAsDate();

    public String sqlTokenDescription_AsNumberOfDays();

    public String sqlTokenDescription_AsNumberOfHours();

    public String sqlTokenDescription_AsNumberOfMinutes();

    public String sqlTokenDescription_AsNumberOfSeconds();

    public String sqlTokenDescription_AsNumberOfMilliseconds();

    public String sqlTokenDescription_AsNumberOfMicroseconds();

    public String sqlTokenDescription_IfNotNull();

    public String sqlTokenDescription_NullValue();

    public String sqlTokenHelp_BooleanAnd();

    public String sqlTokenHelp_BooleanOR();

    public String sqlTokenHelp_BooleanNOT();

    public String sqlTokenHelp_IsTrue();

    public String sqlTokenHelp_IsNotTrue();

    public String sqlTokenHelp_IsFalse();

    public String sqlTokenHelp_IsNotFalse();

    public String sqlTokenHelp_IsUnknown();

    public String sqlTokenHelp_IsNotUnknown();

    public String sqlTokenHelp_Equal();

    public String sqlTokenHelp_NotEqual();

    public String sqlTokenHelp_IsDistinct();

    public String sqlTokenHelp_IsNotDistinct();

    public String sqlTokenHelp_IsNull();

    public String sqlTokenHelp_IsNotNull();

    public String sqlTokenHelp_LessThan();

    public String sqlTokenHelp_GreaterThan();

    public String sqlTokenHelp_LessThanOrEqual();

    public String sqlTokenHelp_GreaterThanOrEqual();

    public String sqlTokenHelp_Between();

    public String sqlTokenHelp_NotBetween();

    public String sqlTokenHelp_BetweenSymetric();

    public String sqlTokenHelp_NotBetweenSymetric();

    public String sqlTokenHelp_RegularExpression();

    public String sqlTokenHelp_CaselessRegularExpression();

    public String sqlTokenHelp_BitwiseAnd();

    public String sqlTokenHelp_BitwiseOR();

    public String sqlTokenHelp_BitwiseXOR();

    public String sqlTokenHelp_BitwiseNOT();

    public String sqlTokenHelp_ShiftLeft();

    public String sqlTokenHelp_ShiftRight();

    public String sqlTokenHelp_IntegerAddition();

    public String sqlTokenHelp_IntegerSubtraction();

    public String sqlTokenHelp_IntegerMultiplication();

    public String sqlTokenHelp_IntegerDivision();

    public String sqlTokenHelp_ModuloDivision();

    public String sqlTokenHelp_IntegerFactorial();

    public String sqlTokenHelp_IntegerExponentiation();

    public String sqlTokenHelp_DecimalAddition();

    public String sqlTokenHelp_DecimalSubtraction();

    public String sqlTokenHelp_DecimalMultiplication();

    public String sqlTokenHelp_DecimalDivision();

    public String sqlTokenHelp_DecimalExponentiation();

    public String sqlTokenHelp_CastInteger();

    public String sqlTokenHelp_CastDecimal();

    public String sqlTokenHelp_CastDate();

    public String sqlTokenHelp_CastTime();

    public String sqlTokenHelp_CastDateTime();

    public String sqlTokenHelp_CastBoolean();

    public String sqlTokenHelp_TruncateToInteger();

    public String sqlTokenHelp_TruncateDecimal();

    public String sqlTokenHelp_RoundToInteger();

    public String sqlTokenHelp_RoundDecimal();

    public String sqlTokenHelp_AbsoluteInteger();

    public String sqlTokenHelp_AbsoluteDecimal();

    public String sqlTokenHelp_SquareRoot();

    public String sqlTokenHelp_CubeRoot();

    public String sqlTokenHelp_NaturalLog();

    public String sqlTokenHelp_LogBase10();

    public String sqlTokenHelp_Sine();

    public String sqlTokenHelp_Cosine();

    public String sqlTokenHelp_Tangent();

    public String sqlTokenHelp_Cotangent();

    public String sqlTokenHelp_InverseSine();

    public String sqlTokenHelp_InverseCosine();

    public String sqlTokenHelp_InverseTangent();

    public String sqlTokenHelp_InverseCotangent();

    public String sqlTokenHelp_Concatenation();

    public String sqlTokenHelp_ToString();

    public String sqlTokenHelp_CharacterLength();

    public String sqlTokenHelp_ByteCount();

    public String sqlTokenHelp_BitCount();

    public String sqlTokenHelp_Capitalize();

    public String sqlTokenHelp_UpperCase();

    public String sqlTokenHelp_LowerCase();

    public String sqlTokenHelp_LeftTrim1();

    public String sqlTokenHelp_LeftTrim2();

    public String sqlTokenHelp_RightTrim1();

    public String sqlTokenHelp_RightTrim2();

    public String sqlTokenHelp_LeftPad1();

    public String sqlTokenHelp_LeftPad2();

    public String sqlTokenHelp_RightPad1();

    public String sqlTokenHelp_RightPad2();

    public String sqlTokenHelp_Substring1();

    public String sqlTokenHelp_Substring2();

    public String sqlTokenHelp_LocateSubstring();

    public String sqlTokenHelp_CurrentTimestamp();

    public String sqlTokenHelp_CurrentDate();

    public String sqlTokenHelp_CurrentTime();

    public String sqlTokenHelp_DateDateExtract();

    public String sqlTokenHelp_DateTimeExtract();

    public String sqlTokenHelp_DateEpochExtract();

    public String sqlTokenHelp_DateMillenniumExtract();

    public String sqlTokenHelp_DateCenturyExtract();

    public String sqlTokenHelp_DateDecadeExtract();

    public String sqlTokenHelp_DateYearExtract();

    public String sqlTokenHelp_DateQuarterExtract();

    public String sqlTokenHelp_DateMonthExtract();

    public String sqlTokenHelp_DateWeekExtract();

    public String sqlTokenHelp_DateYearDayExtract();

    public String sqlTokenHelp_DateMonthDayExtract();

    public String sqlTokenHelp_DateWeekDayExtract();

    public String sqlTokenHelp_DateHourExtract();

    public String sqlTokenHelp_DateMinuteExtract();

    public String sqlTokenHelp_DateSecondExtract();

    public String sqlTokenHelp_DateMillisecondsExtract();

    public String sqlTokenHelp_DateMicrosecondsExtract();

    public String sqlTokenHelp_DateTimezoneExtract();

    public String sqlTokenHelp_DayPartDifference();

    public String sqlTokenHelp_HourPartDifference();

    public String sqlTokenHelp_MinutePartDifference();

    public String sqlTokenHelp_SecondPartDifference();

    public String sqlTokenHelp_MillisecondPartDifference();

    public String sqlTokenHelp_DateMillenniumTruncate();

    public String sqlTokenHelp_DateCenturyTruncate();

    public String sqlTokenHelp_DateDecadeTruncate();

    public String sqlTokenHelp_DateYearTruncate();

    public String sqlTokenHelp_DateQuarterTruncate();

    public String sqlTokenHelp_DateMonthTruncate();

    public String sqlTokenHelp_DateWeekTruncate();

    public String sqlTokenHelp_DateMonthDayTruncate();

    public String sqlTokenHelp_DateHourTruncate();

    public String sqlTokenHelp_DateMinuteTruncate();

    public String sqlTokenHelp_DateSecondTruncate();

    public String sqlTokenHelp_DateMillisecondsTruncate();

    public String sqlTokenHelp_DateMicrosecondsTruncate();

    public String sqlTokenHelp_StringField();

    public String sqlTokenHelp_BooleanField();

    public String sqlTokenHelp_IntegerField();

    public String sqlTokenHelp_NumberField();

    public String sqlTokenHelp_DateTimeField();

    public String sqlTokenHelp_DateField();

    public String sqlTokenHelp_TimeField();

    public String sqlTokenHelp_StringParameter();

    public String sqlTokenHelp_BooleanParameter();

    public String sqlTokenHelp_IntegerParameter();

    public String sqlTokenHelp_NumberParameter();

    public String sqlTokenHelp_DateTimeParameter();

    public String sqlTokenHelp_DateParameter();

    public String sqlTokenHelp_TimeParameter();

    public String sqlTokenHelp_StringValue();

    public String sqlTokenHelp_BooleanValue();

    public String sqlTokenHelp_IntegerValue();

    public String sqlTokenHelp_NumberValue();

    public String sqlTokenHelp_DateTimeValue();

    public String sqlTokenHelp_DateValue();

    public String sqlTokenHelp_TimeValue();

    public String sqlTokenHelp_Conditional();

    public String sqlTokenHelp_ConditionalComponent1();

    public String sqlTokenHelp_ConditionalComponent2();

    public String sqlTokenHelp_ConditionalDefault();

    public String sqlTokenHelp_FormatValue();

    public String sqlTokenHelp_EpochToDateTime();

    public String sqlTokenHelp_ScanDateTime();

    public String sqlTokenHelp_ScanDate();

    public String sqlTokenHelp_ScanInteger();

    public String sqlTokenHelp_ScanDecimal();

    public String sqlTokenHelp_Extract1stValue();

    public String sqlTokenHelp_Extract2ndValue();

    public String sqlTokenHelp_ExtractNthValue();

    public String sqlTokenHelp_ExtractLastValue();

    public String sqlTokenHelp_NumToken();

    public String sqlTokenHelp_GetSingleToken();

    public String sqlTokenHelp_GetLastToken();

    public String sqlTokenHelp_ExtractMatchingString();

    public String sqlTokenHelp_TimestampPlusTime();

    public String sqlTokenHelp_TimestampMinusTimeStamp();

    public String sqlTokenHelp_TimestampMinusDate();

    public String sqlTokenHelp_TimestampMinusTime();

    public String sqlTokenHelp_DatePlusDays();

    public String sqlTokenHelp_DatePlusTime();

    public String sqlTokenHelp_DateMinusDate();

    public String sqlTokenHelp_DateMinusDays();

    public String sqlTokenHelp_DateMinusTime();

    public String sqlTokenHelp_TimePlusTime();

    public String sqlTokenHelp_TimeMinusTime();

    public String sqlTokenHelp_TimeMultiplied();

    public String sqlTokenHelp_TimeDivided();

    public String sqlTokenHelp_TimeAsTimeOfDay();

    public String sqlTokenHelp_TimestampAsDate();

    public String sqlTokenHelp_AsNumberOfDays();

    public String sqlTokenHelp_AsNumberOfHours();

    public String sqlTokenHelp_AsNumberOfMinutes();

    public String sqlTokenHelp_AsNumberOfSeconds();

    public String sqlTokenHelp_AsNumberOfMilliseconds();

    public String sqlTokenHelp_AsNumberOfMicroseconds();

    public String sqlTokenHelp_IfNotNull();

    public String sqlTokenHelp_NullValue();

    public String sqlTokenTreeLabel_Conditional();

    public String sqlTokenTreeLabel_ConditionalComponent();

    public String sqlTokenTreeLabel_ConditionalDefault();

    public String sqlTokenTreeLabel_MissingValue();

    public String sqlTokenTreeLabel_OptionalValueValue();

    public String sqlFunction_ConstantDescription();

    public String sqlFunctionConstantDialog_Title();

    public String parameterPrompt_String();

    public String parameterPrompt_Boolean();

    public String parameterPrompt_Integer();

    public String parameterPrompt_Number();

    public String parameterPrompt_DateTime();

    public String parameterPrompt_Date();

    public String parameterPrompt_Time();

    public String derivedFieldDialog_group_1_String();

    public String derivedFieldDialog_group_1_Boolean();

    public String derivedFieldDialog_group_1_Integer();

    public String derivedFieldDialog_group_1_Number();

    public String derivedFieldDialog_group_1_DateTime();

    public String derivedFieldDialog_group_1_Date();

    public String derivedFieldDialog_group_1_Time();

    public String derivedFieldDialog_group_2_String();

    public String derivedFieldDialog_group_2_Boolean();

    public String derivedFieldDialog_group_2_Integer();

    public String derivedFieldDialog_group_2_Number();

    public String derivedFieldDialog_group_2_DateTime();

    public String derivedFieldDialog_group_2_Date();

    public String derivedFieldDialog_group_2_Time();

    public String derivedFieldDialog_group_3_String();

    public String derivedFieldDialog_group_3_Boolean();

    public String derivedFieldDialog_group_3_Integer();

    public String derivedFieldDialog_group_3_Number();

    public String derivedFieldDialog_group_3_DateTime();

    public String derivedFieldDialog_group_3_Date();

    public String derivedFieldDialog_group_3_Time();

    public String derivedFieldDialog_group_4_String();

    public String derivedFieldDialog_group_4_Boolean();

    public String derivedFieldDialog_group_4_Integer();

    public String derivedFieldDialog_group_4_Number();

    public String derivedFieldDialog_group_4_DateTime();

    public String derivedFieldDialog_group_4_Date();

    public String derivedFieldDialog_group_4_Time();

    public String derivedFieldDialog_UserInputHeader();

    public String derivedFieldDialog_DataViewParameterHeader();

    public String derivedFieldDialog_DataFieldHeader();

    public String derivedFieldDialog_ExpressionPrompt();

    public String derivedFieldDialog_Instructions(final String missingParameterPrompt,
                                                  final String optionalParameterPrompt, final String dataFieldHeader,
                                                  final String parameterHeader, final String userInfoPrompt);

    public String defaultGraphModeTooltip();

    public String fitToSize();

    public String pan();

    public String select();

    public String zoomIn();

    public String zoom();

    public String linkTabColumn_source();

    public String linkTabColumn_target();

    public String linkTabColumn_type();

    public String linkTabColumn_selected();

    public String linkTabColumn_size();

    public String linkTabColumn_hidden();

    public String linkTabColumn_label();

    public String linkTabColumn_opacity();

    public String linkTabFilterLabel_unplunked();

    public String linkTabFilterLabel_selected();

    public String linkTabFilterLabel_unselected();

    public String linkTabFilterLabel_hidden();

    public String linkTabFilterLabel_visible();

    public String linkTabFilterLabel_plunked();

    public String linkTabFilterLabel_bundled();

    public String linkTabFilterLabel_unbundled();

    public String linkTabFilterLabel_commented();

    public String linkTabFilterLabel_uncommented();

    public String linkTabColumn_comment();

    public String pageSize();

    public String linkTab_searchPrompt();

    public String linkTabHeading();

    public String clearSearch();

    public String hide();

    public String show();

    public String savingChanges();

    public String removeFromSelection();

    public String linksTab_action_exportNodesList();

    public String addToSelection();

    public String clearAllFilters();

    public String filters();

    public String actions();

    public String progressBar_loading();

    public String graphLayout_circular();

    public String graphLayout_centrifuge();

    public String graph_tooManyTypes();

    public String zoomOut();

    public String bundleDialog_create();

    public String bundleDialog_requireSelection();

    public String bundleDialog_nameCannotBeBlank();

    public String bundleDialog_bundleNameMustBeUnique();

    public String bundleDialog_bySecificationTooltip();

    public String bundleDialog_bySecification();

    public String bundleDialog_together();

    public String bundleDialog_title();

    public String bundleDialog_bundleNamePlaceholder();

    public String bundleDialog_whatToBundle();

    public String bundleDialog_howToBundle();

    public String bundleFunctionDialogFunctionText();

    public String bundleFunctionDialogFunctionTitle();

    public String selection();

    public String bundleDialog_entireGraph();

    public String exportRequestor_exportAsPNG_ErrorTitle();

    public String exportRequestor_exportAsPNG_ErrorMessage();

    public String exportInProgress();

    public String selectionRadioWidget_all();

    public String selectionRadioWidget_currentSelection();

    public String selectionRadioWidget_instruction();

    public String resolutionWidget_desiredHeight();

    public String resolutionWidget_desiredWidth();

    public String resolutionWidget_instruction();

    public String exportTypeDropdownWidget_fileType();

    public String exportTypeDropdownWidget_instruction();

    public String exportingTextWidgetBuilder_error();

    public String exportingTextWidgetBuilder_asA();

    public String exportingTextWidgetBuilder_visualizationSize();

    public String exportingTextWidgetBuilder_withSize();

    public String exportingTextWidgetBuilder_withThe();

    public String exportingTextWidgetBuilder_selectionOf();

    public String exportingTextWidgetBuilder_all();

    public String chrome_vizPanel_broadcast_tooltip();

    public String timelineLimitWarningTitle();

    public String timelineNoGroupValue();

    public String timelineLimitWarningMessage(int total, int max);

    public String timelineTypeLimitWarningMessage();

    public String timelineColorLimitWarningMessage();

    public String timelineSettingsGeneralTab();

    public String timelineSettingsTooltipsTab();

    public String timelineSettingsEventsTab();

    public String timelineSettingsAdvancedTab();

    public String timelineGeneralTabNameField();

    public String timelineSettingsDisplayHeader();

    public String timelineSettingsAvailableHeader();

    public String timelineSettingsSelectedHeader();

    public String timelineSettingsJavaWarning();

    public String timelineSettingsStart();

    public String timelineSettingsEnd();

    public String timelineSettingsLabel();

    public String timelineSettingsSizeBy();

    public String timelineSettingsGroupBy();

    public String timelineSettingsColorBy();

    public String timelinePresenterLoading();

    public String timelinePresenterImageError();

    public String timelinePresenterDataError();

    public String timelinePresenterLoadError();

    public String timelinePresenterConfigError();

    public String timelineTitle();

    public String timelineValidationMinimum();

    public String timelineValidationTime();

    public String timelineSettingsTitle();

    public String timelineSettings_eventsTab_eventDefinitions();

    public String timelineSettings_eventsTab_newDefinition();

    public String timelineSettings_eventsTab_start();

    public String timelineSettings_eventsTab_end();

    public String timelineSettings_eventsTab_label();

    public String timelineSettings_newDefinition_eventDefinition();

    public String timelineSettings_advancedTab_groups();

    public String timelineSettings_advancedTab_showSummary();

    public String timelineSettings_advancedTab_reserveSpaceForGroupName();

    public String timelineSettings_advancedTab_sortAscending();

    public String timelineSettings_advancedTab_sortDescending();

    public String timeline_showMetrics_timelineMetrics();

    public String timeline_showMetrics_shortTitle();

    public String timeline_showMetrics_EVENTS();

    public String timeline_showMetrics_GROUPS();

    public String timeline_colorLegend_noValue();

    public String timeline_trackRenderable_noValue();

    public String timeline_hide();

    public String tableSettingsSortTabName();

    public String tableSettingsGeneralTabName();

    public String tableSettingsColumnTabName();

    public String tableSettingsTitle();

    public String tableSortTabFieldLabel();

    public String tableGeneralTabNameLabel();

    public String tableGeneralTabResultsLabel();

    public String tableDeleteSortFieldButtonLabel();

    public String tableAddSortFieldButtonLabel();

    public String tableSpinoffError();

    public String tableSpinoffErrorMessage();

    public String sortTabFieldNameTitle();

    public String sortTabSortTitle();

    public String tableTitle();

    public String tableMinimumValidation();

    public String timelineValidationEvents();

    public String tablePresenterErrorDialog();

    public String filteredDateTimeBoxLengthMinError(String string, String string2);

    public String filteredDateTimeBoxLengthMaxError(String string, String string2);

    public String filteredDateTimeBoxDuplicateError(String componentname);

    public String fieldDefOrderException();

    public String fieldDefImageFieldException();

    public String fieldDefImageDataException();

    public String dialogUnknownException();

    public String exceptionDialogNoMessage();

    public String exceptionDialogCausedBy();

    public String exceptionDialogUnknown();

    public String modalSizeException();

    public String multipleEntryWizardAddButton();

    public String singleEntryWizardException();

    public String singleEntryWizardDefaultButton();

    public String singleEntryWizardCurrentButton();

    public String dateTimeInputRequireDateException();

    public String escapedTextInputException();

    public String stringInputcontrolException();

    public String stringInputblankException();

    public String textInputControlCharacterException();

    public String textInputDisableValidityMessage();

    public String drawingPanelStartDrawingMessage();

    public String drawingPanelFinishedCreatingMessage();

    public String drawingPanelFinishedRenderingMessage();

    public String drawingPanelStartRenderingMessage();

    public String multiPageCheckboxSelectionModelBadMsIe();

    public String singleColorPickerTitle();

    public String singleColorPickerSelectedLabel();

    public String fileUploadProgressDialogTitle();

    public String fileUploadProgressDialogUpdateMessage(int nextItemIn, int totalFiles);

    public String bundleFunctionDialogInvalidPopUpTitle();

    public String bundleFunctionDialogInvalidPopUpMessage();

    public String bundleFunctionDialogUnknownTypeException();

    public String abstractColorPickerSelectLabel();

    public String colorPickerSingleHeader();

    public String colorPickerDiscreteHeader();

    public String colorPickerContinuousHeader();

    public String nativeScrollerException();

    public String surfaceLocationException();

    public String fileUploadProgressDialogServerProcessMessage();

    public String fileUploadProgressDialogServerDelimiter();

    public String fileUploadProgressDialogUploadingLabel();

    public String fileUploadProgressDialogUploadingDelimiter();

    public String fileUploadProgressDialogCancelLabel();

    public String fileUploadProgressDialogCancelWarningMessage();

    public String uploadWidgetQueueError();

    public String uploadWidgetQueuingError();

    public String uploadWidgetUploadError();

    public String refreshErrorDialogTitle();

    public String uploadWidgetFailedUploading();

    public String uploadWidgetCanceled();

    public String uploadWidgetCompleted();

    public String uploadWidgetNotUploadedCorrectly();

    public String uploadWidgetErrorMessage();

    public String slidingAccessPanelPinTooltip();

    public String slidingAccessPanelUnpinTooltip();

    public String linkSetting_typePlaceholder();

    public String linkSettings_sizeHelp();

    public String linkSettings_appearance();

    public String linkSettings_transparencyHelp();

    public String color();

    public String LinkSettings_direction();

    public String linkDirection_undirected();

    public String dynamic();

    public String linkDirection_reverse();

    public String linkDirection_forward();

    public String linkDirection_to();

    public String linkDirection_invalidSelectionTitle();

    public String linkDirection_invalidSelectionMessage();

    public String options();

    public String showAsHyperlink();

    public String hyperLinkAltTextPrompt();

    public String hideEmptyValues();

    public String spinoffHandler_ErrorTitle();

    public String spinoffHandler_ErrorMessage();

    public String spawnTableHandler_ErrorTitle();

    public String spawnTableHandler_ErrorMessage();

    public String createSelectionFilterHandler_ErrorTitle();

    public String createSelectionFilterHandler_ErrorMessage();

    public String matrixSortTab_sortByAxis();

    public String matrixSortTab_sortByMeasure();

    public String matrixSortTab_fieldLabel();

    public String matrixSortTab_sortLabel();

    public String matrixSortTab_axis();

    public String matrixSortTab_field();

    public String matrixSortTab_sort();

    public String matrixContextMenu_selectCell();

    public String matrixContextMenu_deselectCell();

    public String matrixContextMenu_selectRow();

    public String matrixContextMenu_selectColumn();

    public String matrixContextMenu_selectIntersection();

    public String colorTab_colorModel();

    public String colorTab_select();

    public String matrixAxisTab_flipXY();

    public String matrixCategoriesTab_add();

    public String matrixCategoriesTab_delete();

    public String matrixCategoriesTab_select();

    public String matrixCategoriesTab_field();

    public String matrixCategoriesTab_bundle();

    public String matrixCategoriesTab_axis();

    public String matrixCategoriesTab_displayAs();

    public String matrixCategoriesTab_nulls();

    public String matrixCategoriesTab_errorTitle();

    public String matrixCategoriesTab_errorMessge();

    public String matrixGeneralTab_name();

    public String matrixGeneralTab_type();

    public String matrixGeneralTab_labels();

    public String matrixGeneralTab_bubbleDisplay();

    public String matrixGeneralTab_heatMapDisplay();

    public String matrixMeasuresTab_measureTypeCount();

    public String matrixMeasuresTab_measureTypeField();

    public String matrixMeasuresTab_fieldControl();

    public String matrixMeasuresTab_functionControl();

    public String matrixMeasuresTab_scaleMinControl();

    public String matrixMeasuresTab_scaleMaxControl();

    public String matrixMeasuresTab_displayAsControl();

    public String matrixSettingsPersenter_validationFeedbackX();

    public String matrixSettingsPersenter_validationFeedbackY();

    public String matrixSettingsPersenter_validationFeedbackMeasureField();

    public String matrixSettingsPersenter_validationFeedbackMeasureFunction();

    public String matrixSettingsPresenter_filterFeedback();

    public String spinoffDialog_dataviewNameLabel();

    public String spinoffDialog_createSpinoff();

    public String spinoffDialog_openSpinoffNow();

    public String spinoffDialog_spinoffButton();

    public String spinoffDialog_dataviewMustHaveName();

    public String spinoffDialog_dataviewNameExists();

    public String spinoffDialog_creatingSpinoff();

    public String spinoffDialog_openSpinoffTitle();

    public String spinoffDialog_openSpinoffMessage();

    public String createSelectionFilterDialog_filterNameLabel();

    public String createSelectionFilterDialog_createFilter();

    public String createSelectionFilterDialog_createFilterButton();

    public String createSelectionFilterDialog_filterMustHaveName();

    public String createSelectionFilterDialog_filterNameExists();

    public String createSelectionFilterDialog_creatingFilter();

    public String createSelectionFilterDialog_filterCreated();

    public String broadcastAlert_broadcastReceived();

    public String absVizPresenter_filterTooltipText();

    public String absVizPresenter_filterClauseTooltipText();

    public String absVizPresenter_broadcastTooltipText();

    public String absVizPresenter_sqlErrorText();

    public String absVizPresenter_vizNotLoaded();

    public String absVizPresenter_loadViz();

    public String selectNeighbors();

    public String selectNeighbor_actionButton();

    public String selectNeighbor_numberOfNeighbors();

    public String appearanceTab_heading();

    public String degree();

    public String nodeSettings_transparencyHelpInfo();

    public String nodeAppearance_identity();

    public String nodeAppearance_icon();

    public String nodeAppearance_shape();

    public String nodeAppearance_color();

    public String nodeAppearance_preview();

    public String bundleTab_reorder();

    public String tooltip();

    public String neither();

    public String graphAttribute();

    public String computed();

    public String worksheetCopyDialogLabel();

    public String worksheetMoveDialogLabel();

    public String worksheetNameDialogLabel();

    public String visualizationIconManagerTimelineAlt();

    public String visualizationIconManagerChartAlt();

    public String visualizationIconManagerMapAlt();

    public String visualizationIconManagerMatrixAlt();

    public String visualizationIconManagerGraphAlt();

    public String visualizationIconManagerTableAlt();

    public String visualizationBarIconDefaultText();

    public String visualizationBarIconBetaText();

    public String visualizationBarIconContainerCascadeTooltip();

    public String visualizationBarIconContainerEqualTooltip();

    public String worksheetTabPanelAddTooltip();

    public String worksheetTabPanelRenameItem();

    public String worksheetTabPanelDeleteItem();

    public String worksheetTabPanelColorItem();

    public String worksheetTabPanelColorTextBox();

    public String worksheetTabColorDialog_noColor();

    public String worksheetCopyDialogActionButton();

    public String worksheetCopyDialogTitle();

    public String worksheetMoveDialogActionButton();

    public String worksheetMoveDialogTitle();

    public String worksheetNameDialogcreateTitle();

    public String worksheetNameDialogrenameTitle();

    public String worksheetNameDialogCreateAction();

    public String worksheetNameDialogDefaultWorksheetName();

    public String worksheetNameDialogRenameAction();

    public String columnFilterDialogDeleteMessageConfirmation(String paramName);

    public String columnDefFilterCollectionDialogFilterMessage(String columnName);

    public String previewItemException(String string);

    public String refreshDirectedDialogRefreshTitle();

    public String refreshDirectedDialogRefreshMessage();

    public String refreshDirectedDialogCloseButton();

    public String refreshDirectedDialogRefreshButton();

    public String refreshDirectedDialogEditButton();

    public String previewDialogtitle();

    public String previewDialogCloseButton();

    public String previewDialogMaskMessage();

    public String previewDialogErrorMessage();

    public String dataSourceClientUtilAccessDriverName();

    public String dataSourceClientUtilExcelDriverName();

    public String dataSourceClientUtilGenericDriverName();

    public String dataSourceClientUtilLinkedDriverName();

    public String dataSourceClientUtilMySqlDriverName();

    public String dataSourceClientUtilOracleDriverName();

    public String dataSourceClientUtilPostgresSqlDriverName();

    public String dataSourceClientUtilReservedDriverName();

    public String dataSourceClientUtilSqlDriverName();

    public String dataSourceClientUtilTableDriverName();

    public String dataSourceClientUtilTextDriverName();

    public String dataSourceClientUtilViewDriverName();

    public String dataSourceClientUtilWebServiceDriverName();

    public String dataSourceClientUtilXmlDriverName();

    public String dataSourceClientUtilCustomDriverName();

    public String dataSourceClientUtilInstalledTableDriverName();

    public String connectionPointExceptionMessage();

    public String wienzoFactoryExceptionMessage();

    public String wienzoTabletooltipText();

    public String configurationPresenterUpdateDialogTitle();

    public String configurationPresenterDeleteConfirmation(String SourceObjectIn);

    public String configurationPresenterDataInUseMessage();

    public String configurationPresenterDeleteDialog();

    public String columnDefFilterCollectionDialogFilter();

    public String columnDefFilterCollectionDialogSourceLabel();

    public String columnDefFilterCollectionDialogTypeLabel();

    public String columnDefFilterCollectionDialogAddButton();

    public String columnDefFilterCollectionDialogDeleteButton();

    public String columnDefFilterCollectionDialogFilterTitle();

    public String columnDefFilterCollectionDialogOperatorLabel();

    public String columnDefFilterCollectionDialogOperandLabel();

    public String columnDefFilterCollectionDialogValueLabel();

    public String columnDefFilterCollectionDialogEditLabel();

    public String columnDefFilterCollectionDialogDeleteLabel();

    public String columnDefFilterCollectionDialogDeleteMessage();

    public String columnDefFilterCollectionDialogDeleteConfirmationMessage();

    public String columnFilterDialogexcludeLabel();

    public String columnFilterDialogoperatorLabel();

    public String columnFilterDialogoperandLabel();

    public String columnFilterDialogaddDateLabel();

    public String columnFilterDialogEditTitle();

    public String columnFilterDialogCreateTitle();

    public String columnFilterDialogCreateButton();

    public String columnFilterDialogSaveButton();

    public String columnFilterDialogNewButton();

    public String columnFilterDialogEditButton();

    public String columnFilterDialogDeleteButton();

    public String columnFilterDialogDeleteDialogTitle();

    public String columnFilterDialogParameterLabel();

    public String columnFilterDialogColumnLabel();

    public String columnFilterDialogStaticLabel();

    public String columnFilterExcludeCellNotLabel();

    public String inputParameterListDialogAddButton();

    public String inputParameterListDialogDeleteButton();

    public String inputParameterListDialogEditColumn();

    public String inputParameterListDialogNameColumn();

    public String inputParameterListDialogDeleteTitle();

    public String inputParameterListDialogDeleteMessage();

    public String inputParameterListDialogDeleteConfirmation();

    public String dataSourceEditorViewCapcoButton();

    public String dataSourceEditorViewSecurityButton();

    public String dataSourceEditorViewDistributionButton();

    public String dataSourceEditorViewParametersButton();

    public String dataSourceEditorViewPreviewButton();

    public String dataSourceEditorViewSaveButton();

    public String dataSourceEditorViewMapButton();

    public String dataSourceEditorViewCancelButton();

    public String vectorValueDefinitionDialog_picklistPick();

    public String dontKnowHowToHandle();

    public String clear();

    public String manageFilterDialog_title();

    public String manageFilterDialog_new();

    public String manageFilterDialog_noFilterSelected();

    public String manageFilterDialog_copyEdit();

    public String manageFilterDialog_copyAndEdit();

    public String manageFilterDialog_clearButtonTitle();

    public String manageFilterDialog_newButtonTitle();

    public String manageFilterDialog_deleteButtonTitle();

    public String manageFilterDialog_copyEditButtonTitle();

    public String manageFilterDialog_editButtonTitle();

    public String manageFilterDialog_deleteFilterTitle();

    public String manageFilterDialog_deleteFilterMessage();

    public String edit();

    public String filterSelectionDialog_manage();

    public String filterSelectionDialog_noFilterSelected();

    public String filterSelectionDialog_WarningTitle();

    public String filterSelectionDialog_WarningMessage();

    public String filterDisplayWidget_referencedBy();

    public String filterDisplayWidget_referencedByListTitle();

    public String filterDisplayWidget_negatedDes();

    public String filterDisplayWidget_operatorDes();

    public String filterDisplayWidget_valueDefDes();

    public String filterDisplayWidget_vizs();

    public String filterDisplayWidget_containingVizAnd();

    public String filterDisplayWidget_otherVisualizations();

    public String filterDisplayWidget_none();

    public String dualListBoxDialogHeader();

    public String dualListBoxDialogAvailableLabel();

    public String dualListBoxDialogSelectedLabel();

    public String kmlExportDialogcreateButton();

    public String kmlExportDialogcloseButton();

    public String kmlExportDialognewFilterDropBox();

    public String kmlExportDialogNameHeader();

    public String kmlExportDialogdeleteTooltip();

    public String kmlExportDialogeditCellTooltip();

    public String ShowKmlExportErrorTitle();

    public String ShowKmlExportErrorMessage();


    public String filterSettingsHandler_ErrorTitle();

    public String filterSettingsHandler_ErrorMessage();

    public String createEditFilterDialog_copyOf();

    public String createEditFilterDialog_haveNameTitle();

    public String createEditFilterDialog_haveNameMsg();

    public String createEditFilterDialog_haveOpTitle();

    public String createEditFilterDialog_haveOpMsg();

    public String createEditFilterDialog_haveValueTitle();

    public String createEditFilterDialog_haveValueMsg();

    public String createEditFilterDialog_createFilterTitle();

    public String createEditFilterDialog_editFilterTitle();

    public String createEditFilterDialog_addButtonTitle();

    public String createEditFilterDialog_deleteButtonTitle();

    public String filter_ref();

    public String filter_field();

    public String filter_bundle();

    public String filter_not();

    public String filter_operator();

    public String filter_value();

    public String calculateDurationWidgetErrorTitle();

    public String calculateDurationWidgetErrorMessage();

    public String calculateValueWidgetErrorTitle();

    public String calculateValueWidgetErrorMessage();

    public String concatenateFieldsWidgetErrorTitle();

    public String concatenateFieldsWidgetErrorMessage();

    public String javascriptWidgetErrorTitle();

    public String javascriptWidgetErrorMessage();

    public String substringWidgetProcessErrorTitle();

    public String substringWidgetProcessErrorMessage();

    public String substringWidgetFirstCharacterErrorMessage();

    public String substringWidgetLastCharacterErrorMessage();

    public String substringWidgetIllegalIndexMessage();

    public String fieldListDialogInstruction(String editButton, String deleteButton);

    public String fieldListDialogButtonText();

    public String linkupSelectionDialogExecutingLinkupMessage(String param, String param2, String param3);

    public String dataViewPresenterDefaultWorksheetName();

    public String dataViewPresenterMetaDescription();

    public String dataViewPresenterWorksheetSingleDeleteConfirmation(String name);

    public String dataViewPresenterDeleteConfirmation();

    public String dataViewPresenterWorksheetManyDeleteMessage(String name, int vizCount);

    public String dataViewPresenterDeleteWorksheetTItle();

    public String addLinkupFieldDialogErrorTitle();

    public String addLinkupFieldDialogErrorMessage();

    public String dataviewFromScratchWizardTypeErrorMessage();

    public String dataviewFromScratchWizardParameterErrorMessage();

    public String dataviewFromScratchWizardMissingParameterErrorMessage();

    public String dataviewFromScratchWizardWatchboxMessage();

    public String linkupGridMapperFieldInstructions();

    public String linkupParameterMapperInstructions();

    public String linkupSelectionDialogExecuteLinkupTitle();

    public String linkupSelectionDialogLinkupErrorMessage();

    public String linkupSelectionDialogMaskMessage();

    public String linkupSelectionDialogTabTitle();

    public String linkupSelectionDialogNewDataviewLabel();

    public String linkupSelectionDialogExecuteButton();

    public String linkupSelectionDialogModifyButton();

    public String connectionTreeItemTypeException(String typeIn);

    public String connectionTreeItemNameException();

    public String connectionTreeItemBadConnectionLog();

    public String connectionTreePanelErrorMessage();

    public String connectionTreePanelAddButtonText();

    public String connectionTreePanelNewSourceText();

    public String connectionTreePanelEditSourceText();

    public String connectionTreePanelRemoveText();

    public String connectionTreePanelNewQuery();

    public String connectionTreePanelNewTable();

    public String connectionTreePanelEmptyMessage();

    public String connectionTreePanelErrorTitle();

    public String connectionTreePanelInstructionText();

    public String dataviewCreationWizardDefaultName();

    public String dataviewCreationWizardErrorTitle();

    public String dataviewCreationWizardErrorMessage();

    public String parameterPanelSupportParameterError();

    public String parameterValidatorError();

    public String parameterWizardTitle();

    public String abstractDataViewPresenterRefreshMessage();

    public String abstractDataViewPresenterAddMessage();

    public String abstractDataViewPresenterUpdateMessage();

    public String abstractDataViewPresenterDeleteMessage();

    public String abstractDataViewPresenterWaitingMessage();

    public String abstractDataViewPresenter_CancelError();

    public String abstractDataViewPresenter_CancelSuccess();

    public String abstractDataViewPresenter_ProcessedRows(long countIn);

    public String abstractDataViewPresenter_ProcessedRowsMessage(String messageIn, long countIn);

    public String abstractDataViewPresenter_MoreDataTitle();

    public String abstractDataViewPresenter_MoreDataMessage(long countIn);

    public String abstractDataViewPresenterWaitComment();

    public String dataViewInNewTabErrorTitle();

    public String dataViewInNewTabErrorMessage();

    public String authorizationObjectDefaultName();

    public String credentialBuilderMessage();

    public String stringValidationFeedbackVisualizationsUniqueMessage();

    public String stringValidationFeedbackVisualizationEmptyNameValidation();

    public String securityInfoPopup_ClearanceLabel();

    public String securityInfoPopup_RemarksLabel();

    public String securityInfoPopup_CAPCOSectionLabel();

    public String securityInfoPopup_PortionTextLabel();

    public String securityInfoPopup_BannerTextLabel();

    public String securityInfoPopup_AclCheckBoxText();

    public String tableDetailEditor_Columns();

    public String tableDetailEditor_RejectFilter_Title();

    public String tableDetailEditor_RejectFilter_Message(String replaceMethodIn);

    public String tableDetailEditor_RejectFilter_UseReset();

    public String tableDetailEditor_RejectFilter_ReplaceQuery();

    public String tableDetailEditor_Edit_Query();

    public String tableDetailEditor_Reset_Query();

    public String tableDetailEditor_Select_All();

    public String tableDetailEditor_Deselect_All();

    public String tableDetailEditor_Source_Name();

    public String tableDetailEditor_Source_Type();

    public String tableDetailEditor_Filename();

    public String tableDetailEditor_Host_Port();

    public String tableDetailEditor_Schema();

    public String tableDetailEditor_Type();

    public String tableDetailEditor_NotApplicableAbreviation();

    public String snapshotPublishDialog_name();

    public String snapshotPublishDialog_desc();

    public String snapshotPublishDialog_tags();

    public String snapshotPublishDialog_publish();

    public String deleteMenuHanlder_title();

    public String deleteMenuHandler_message();

    public String broadcastMenuManager_title();

    public String broadcastMenuManager_message();

    public String menuKeyConstants_actions();

    public String menuKeyConstants_edit();

    public String menuKeyConstants_configure();

    public String menuKeyConstants_tools();

    public String menuKeyConstants_layout();

    public String menuKeyConstants_linkup();

    public String menuKeyConstants_broadcast();

    public String menuKeyConstants_linkup_defname();

    public String menuKeyConstants_broadcast_inclusion();

    public String menuKeyConstants_broadcast_exclusion();

    public String menuKeyConstants_broadcast_replace();

    public String menuKeyConstants_broadcast_addto();

    public String menuKeyConstants_broadcast_remove();

    public String menuKeyConstants_clear_broadcast();

    public String menuKeyConstants_clear_selection();

    public String menuKeyConstants_clear_all_selection();

    public String menuKeyConstants_clear_all_broadcasts();

    public String menuKeyConstants_clear_Everything();

    public String menuKeyConstants_listen_for_broadcast();

    public String menuKeyConstants_load();

    public String menuKeyConstants_save();

    public String menuKeyConstants_spinoff();

    public String menuKeyConstants_spawn();

    public String menuKeyConstants_print();

    public String menuKeyConstants_publish();

    public String menuKeyConstants_export();

    public String menuKeyConstants_download_image();

    public String menuKeyConstants_copy();

    public String menuKeyConstants_move();

    public String menuKeyConstants_delete();

    public String menuKeyConstants_select_all();

    public String menuKeyConstants_select_neighbors();

    public String menuKeyConstants_invert_selection();

    public String menuKeyConstants_deselect_all();

    public String menuKeyConstants_select_ddd();

    public String menuKeyConstants_hide_selection();

    public String menuKeyConstants_unhide_selection();

    public String menuKeyConstants_unhide_all();

    public String menuKeyConstants_remove_selected_nodes();

    public String menuKeyConstants_clear_merge_highlights();

    public String menuKeyConstants_delete_plunked();

    public String menuKeyConstants_settings();

    public String menuKeyConstants_filters();

    public String menuKeyConstants_load_on_startup();

    public String menuKeyConstants_toggle_tooltip_anchors_hover();

    public String menuKeyConstants_toggle_tooltip_anchors_always();

    public String menuKeyConstants_nodes_list();

    public String menuKeyConstants_links_list();

    public String menuKeyConstants_time_player();

    public String menuKeyConstants_graph_search();

    public String menuKeyConstants_bundle();

    public String menuKeyConstants_unbundle();

    public String menuKeyConstants_reveal_neighbors();

    public String menuKeyConstants_find_paths();

    public String menuKeyConstants_compute_sna_metrics();

    public String menuKeyConstants_appearance_editor();

    public String menuKeyConstants_hide_legend();

    public String menuKeyConstants_show_legend();

    public String menuKeyConstants_reset_legend();

    public String menuKeyConstants_hide_annotation();

    public String menuKeyConstants_show_annotation();

    public String menuKeyConstants_centrifuge();

    public String menuKeyConstants_circular();

    public String menuKeyConstants_force_directed();

    public String menuKeyConstants_linear_hierarchy();

    public String menuKeyConstants_radial();

    public String menuKeyConstants_scramble_and_place();

    public String menuKeyConstants_grid();

    public String menuKeyConstants_hide_multitype_decorator();

    public String menuKeyConstants_show_multitype_decorator();

    public String menuKeyConstants_showChartMetrics();

    public String menuKeyConstants_showMatrixMetrics();

    public String menuKeyConstants_showMapMetrics();

    public String menuKeyConstants_showTimelineMetrics();

    public String drillHeader_promote();

    public String drillError_message(long actual);

    public String dimensionDrillTab_heading1(String selection, String category);

    public String dimensionDrillTab_heading2(String category);

    public String rangeCalculator_categoriesGreaterThanWidth();

    public String chartMeasuresTab_measureTypeCount();

    public String chartMeasuresTab_measureTypeField();

    public String chartMeasuresTab_dragCol();

    public String chartMeasuresTab_nameCol();

    public String chartMeasuresTab_measureType();

    public String chartMeasuresTab_colorCol();

    public String chartMeasuresTab_chartType();

    public String chartMeasuresTab_displayAsCol();

    public String chartMeasuresTab_alignAxes();

    public String chartCategoriesTab_select();

    public String chartCategoriesTab_dragCol();

    public String chartCategoriesTab_nameCol();

    public String chartCategoriesTab_bundle();

    public String chartCategoriesTab_chartType();

    public String chartCategoriesTab_displayAsCol();

    public String chartCategoriesTab_allowNulls();

    public String measure();

    public String countStar();

    public String labelDefinitionCell_useFieldName();

    public String labelDefinitionCell_useStaticValue();

    public String labelDefinitionCell_ok();

    public String chartSortTab_dragCol();

    public String chartSortTab_select();

    public String chartSortTab_nameCol();

    public String chartSortTab_sortOrder();

    public String chartGeneralTab_displayOnLoad();

    public String chartGeneralTab_chartDisplay();

    public String chartGeneralTab_tableDisplay();

    public String chartSettingsPresenter_name();

    public String chartSettingsPresenter_tabCategoriesFeedback();

    public String chartSettingsPresenter_measuresFeedback();

    public String chartSettingsPresenter_incorrectAggregateFunction();

    public String chartSettingsPresenter_incorrectChartType();

    public String searchCategoryDialog_category();

    public String searchCategoryDialog_title();

    public String searchCategoryDialog_actionButton();

    public String highchartConstants_negative();

    public String highchartConstants_gridLineInterpolation();

    public String highchartConstants_pointRange();

    public String highchartConstants_slicedOffset();

    public String highchartBuilder_chartTypeNull();

    public String highchartBuilder_seriesCountNegative();

    public String highchartBuilder_polygon();

    public String highchartBuilder_dontKnowAbout();

    public String chartView_search();

    public String chartView_tooManyCategoriesTitle();

    public String chartView_tooManyCategoriesMessage();

    public String chartView_chart();

    public String chartView_table();

    public String visualizationFactory_dontKnowHow();

    public String visualizationFactory_unknowVizType();

    public String resourceTab_share();

    public String resourceTab_classify();

    public String resourceTab_grant();

    public String resourceTab_group();

    public String resourceTab_user();

    public String resourceTab_only();

    public String resourceTab_view();

    public String resourceTab_edit();

    public String resourceTab_owner();

    public String resourceTab_privileges();

    public String resourceTab_returnItemsMatch();

    public String resourceTab_returnAll();

    public String resourceTab_ownedBy();

    public String resourceTab_sharedWith();

    public String resourceTab_no_groups_select();

    public String resourceTab_no_users_selected();

    public String resourceTab_all_groups();

    public String resourceTab_all_users();

    public String resourceTab_itemsCantBeDeleted();

    public String resourceTab_retrieveShareInfo();

    public String resourceTab_retrieveSecurityInfo();

    public String resourceTab_updateSecurityInfo();

    public String resourceTab_deleteResources();

    public String resourceTab_updateShareInfo();

    public String resourceTab_matchingPattern();

    public String resourceSharingView_manageResources();

    public String resourceSharingView_exit();

    public String resourceSharingView_adminGroup();

    public String resourceSharingView_securityGroup();

    public String resourceSharingView_everyoneGroup();

    public String resourceSharingView_adminUser();

    public String resourceSharingView_securityUser();

    public String resourceSharingTab_selectForAccess();

    public String resourceSharingDialog_SingleTitle(String resourceType_singular_In);

    public String resourceSharingDialog_MultipleTitle(String resourceType_plural_In);

    public String resourceSharingDialog_waitingMessage();

    public String resourceSharingDialog_Instructions(String unselectedUsers, String selectedUsers, String unselectedGroups, String selectedGroups);

    public String resourceSecurityDialog_txtTitle();

    public String getStartedView_header();

    public String getStartedView_openRecentHeader();

    public String getStartedView_openRecentText();

    public String getStartedView_createNewHeader();

    public String getStartedView_open();

    public String getStartedView_createNewText();

    public String getStartedView_createNewFromTemplateHeader();

    public String getStartedView_createNewFromTemplateText();

    public String getStartedView_exploreSamplesHeader();

    public String getStartedView_exploreSamplesText();

    public String getStartedView_shareResourceHeader();

    public String getStartedView_shareResourceText();

    public String getStartedView_sysAdminHeader();

    public String getStartedView_sysAdminText();

    public String getStartedView_recentDataviews();

    public String getStartedView_launchMappterDialog();

    public String applicationToolbar_SaveSuccess_DialogTitle();

    public String applicationToolbar_SaveSuccess_InfoString(String nameIn);

    public String applicationToolbar_DeleteDataview_DialogTitle();

    public String applicationToolbar_DeleteDataview_InfoString();

    public String applicationToolbar_ClosingDataview_DialogTitle();

    public String applicationToolbar_ClosingDataview_InfoString();

    public String applicationToolbar_discard();

    public String applicationToolbar_newDataview();

    public String applicationToolbar_newDataviewFromTemplate();

    public String applicationToolbar_openDataview();

    public String applicationToolbar_importResources();

    public String applicationToolbar_exportResources();

    public String applicationToolbar_manageResources();

    public String applicationToolbar_manageResourceFilters();

    public String applicationToolbar_systemAdministration();

    public String applicationToolbar_configureReaper();

    public String applicationToolbar_analysisView();

    public String applicationToolbar_saveDataview();

    public String applicationToolbar_saveDataviewAs();

    public String applicationToolbar_saveAsTemplate();

    public String applicationToolbar_exportDataview();

    public String applicationToolbar_createKML();

    public String applicationToolbar_editFields();

    public String applicationToolbar_editDataSources();

    public String applicationToolbar_shareDataView();

    public String applicationToolbar_refreshDataSources();

    public String applicationToolbar_closeDataview();

    public String applicationToolbar_deleteDataview();

    public String applicationToolbar_manageLinkups();

    public String applicationToolbar_about();

    public String applicationToolbar_help();

    public String applicationToolbar_changePassword();

    public String applicationToolbar_logout();

    public String applicationToolbar_addDataSource();

    public String applicationToolbar_editDataSource();

    public String applicationToolbar_removeDataSource();

    public String applicationToolbar_mapFields();

    public String applicationToolbar_editParameters();

    public String applicationToolbar_editTemplate();

    public String applicationToolbar_newInstalledTable();

    public String applicationToolbar_dataviewDropdown();

    public String applicationToolbar_configurationDropdown();

    public String applicationToolbar_renameDataview();

    public String applicationToolbar_editThemes();

    public String applicationToolbar_installFunction();

    public String applicationToolbar_displayParameters();

    public String applicationToolbar_managementDropDown_caption();

    public String applicationToolbar_helpDropDown_caption();

    public String editTemplateDialog_HelpTarget();

    public String editTemplateDialog_InfoString(final String openButtonIn);

    public String editTemplateDialog_Choice_DialogTitle();

    public String editTemplateDialog_Choice_InfoString(final String currentButtonIn, final String newButtonIn);

    public String editTemplateDialog_Choice_NewButtonString();

    public String editTemplateDialog_Choice_CurrentButtonString();

    public String recentDataviewsGrid_description();

    public String recentDataviewsGrid_lastOpened();

    public String mainPresenter_CloseDataview_DialogTitle();

    public String mainPresenter_CloseDataview_InfoString(final String continueButtonIn, final String cancelButtonIn);

    public String mainPresenter_loadingDataview();

    public String mainPresenter_createDataviewFromTemplateError();

    public String mainPresenter_validatingRequest();

    public String mainPresenter_noDataviewFound();

    public String mainPresenter_locatingDataview();

    public String mainPresenter_loading();

    public String selectDataviewDialog_name();

    public String selectDataviewDialog_title();

    public String resourceSaveAsDialog_WatchBoxInfo(String resourceTypeIn, String nameIn);

    public String resourceSaveAsDialog_DialogInfo(String resourceTypeIn);

    public String resourceSaveAsDialog_InfoString(String resourceTypeIn);

    public String resourceSaveAsDialog_InstalledTableInfoString();

    public String restrictedResourceSaveAsDialog_InfoString(String resourceTypeIn);

    public String resourceSaveAsDialog_FailureTitle(String resourceTypeIn);

    public String resourceSaveAsDialog_SuccessTitle(String resourceTypeIn);

    public String resourceSaveAsDialog_SuccessMessage(String resourceTypeIn, String nameIn);

    public String extractTableDialog_CreateTable();

    public String extractTableDialog_UpdateTable();

    public String extractTableDialog_Title();

    public String extractTableDialog_BasicInstructions(String createPromptIn, String updatePromptIn, String buttonIn);

    public String extractTableDialog_FieldSelectionInstructions(String buttonIn);

    public String extractTableDialog_FieldMappingInstructions(String buttonIn);

    public String extractTableDialog_CreateInstructions(String buttonIn);

    public String extractTableDialog_UpdateInstructions(String buttonIn);

    public String extractTableDialog_NewOrUpdatePrompt();

    public String extractTableDialog_MappingPrompt();

    public String extractTableDialog_FieldHeader();

    public String post_title();

    public String administrationDialogs_MainTitle();

    public String administrationDialogs_MainHelpTarget();

    public String administrationDialogs_individualUsers();

    public String administrationDialogs_sharingGroups();

    public String administrationDialogs_reports();

    public String administrationDialogs_securityGroups();

    public String administrationDialogs_CapcoComponents();

    public String passwordPopup_passwordMatch();

    public String passwordPopup_passwordDontMatch();

    public String passwordPopup_passwordLengthReq();

    public String passwordPopup_newPassword();

    public String passwordPopup_repeatPassword();

    public String concurrencyTooltip();

    public String plusplus();

    public String threepluses();

    public String fourpluses();

    public String sixpluses();

    public String eightpluses();

    public String toFrom();

    public String implied();

    public String namePrompt();

    public String groupTab_returnGroupsMatch();

    public String groupTab_returnAllGroups();

    public String groupTab_newGroup();

    public String groupTab_returnRolesMatch();

    public String groupTab_returnAllRoles();

    public String groupTab_newRole();

    public String capcoTab_returnTokensMatch();

    public String capcoTab_returnAllTokens();

    public String capcoTab_newToken();

    public String reportsTab_returnTokensMatch();

    public String reportsTab_returnAllTokens();

    public String reportsTab_newToken();

    public String sharingInfoPopup_groupName();

    public String sharingInfoPopup_remarks();

    public String reaperInfoPopup_ResourceTypePrompt();

    public String reaperInfoPopup_remarks();

    public String reaperInfoPopup_Created();

    public String reaperInfoPopup_LastAccessed();

    public String reaperInfoPopup_OnOrBefore();

    public String reaperInfoPopup_DaysAgo();

    public String reaperInfoPopup_UserSelectionPrompt();

    public String securityAdmin_SelectSharing();

    public String securityAdmin_SelectSecurity();

    public String securityAdmin_AllSharing();

    public String securityAdmin_AllSecurity();

    public String securityAdmin_NoInfoFound();

    public String userTab_deactivate();

    public String userTab_activate();

    public String userTab_returnUsersMatch();

    public String userTab_returnAllUsers();

    public String userTab_and();

    public String userTab_newUser();

    public String membership();

    public String membersOf();

    public String providing();

    public String access();

    public String getResults();

    public String userInfoPopup_username();

    public String userInfoPopup_password();

    public String userInfoPopup_firstName();

    public String userInfoPopup_lastName();

    public String userInfoPopup_emailAddress();

    public String userInfoPopup_remarks();

    public String userInfoPopup_expirationDate();

    public String userInfoPopup_perpetual();

    public String userInfoPopup_disabled();

    public String userInfoPopup_suspended();

    public String systemParameters_USER();

    public String systemParameters_CLIENT();

    public String systemParameters_REMOTE_USER();

    public String systemParameters_URL();

    public String systemParameters_DN();

    public String serverMessage_LicenseLimitation();

    public String serverMessage_AdminRequired();

    public String serverMessage_LicenseCountExceeded();

    public String serverMessage_LicenseUserDisabled();

    public String serverMessage_CorruptUserData();

    public String serverMessage_UserNotAuthorized();

    public String serverMessage_CsoRequired();

    public String serverMessage_GroupCreateFailed();

    public String serverMessage_CorruptGroup();

    public String serverMessage_CaughtException();

    public String serverMessage_AdminOrCsoRequired();

    public String serverMessage_CredentialsException();

    public String serverMessage_LinkupDataViewException();

    public String serverMessage_LinkupDataSourceEmpty();

    public String serverMessage_InvalidState();

    public String serverMessage_SaveCanceled();

    public String serverMessage_TemplateAccessError();

    public String serverMessage_DataViewCreateException();

    public String serverMessage_DataViewSaveError();

    public String serverMessage_DataViewLocateError();

    public String serverMessage_DataViewEditError();

    public String serverMessage_DataViewValidationError();

    public String serverMessage_DataViewDeleteException();

    public String serverMessage_DataViewLoadException();

    public String serverMessage_DataViewCreateError();

    public String serverMessage_DataViewUuidError();

    public String serverMessage_DataViewDataSourceEmpty();

    public String serverMessage_TemplateNotFound();

    public String serverMessage_DriverLocateError();

    public String serverMessage_ConnectorConfigError();

    public String serverMessage_MissingDriverKey();

    public String serverMessage_TemplateAccessException();

    public String serverMessage_DataViewSaveException();

    public String serverMessage_TemplateXferException();

    public String serverMessage_DataViewPersistException();

    public String serverMessage_FileUploadError();

    public String serverMessage_BadArguments();

    public String serverMessage_TemplateSaveError();

    public String serverMessage_TemplateLocateError();

    public String serverMessage_TemplateEditError();

    public String serverMessage_TemplateValidationError();

    public String serverMessage_TemplateDeleteException();

    public String serverMessage_TemplateLoadException();

    public String serverMessage_TemplateUuidError();

    public String serverMessage_ResourceNotImportable();

    public String serverMessage_ResourceNotFound();

    public String serverMessage_MissingDataKey();

    public String serverMessage_MissingDialogKey();

    public String serverMessage_ResourceNotSupported();

    public String serverMessage_TemplateCreateError();

    public String serverMessage_FailedServerRequest();

    public String serverMessage_UnknownError();

    public String serverMessage_InstalledTableExists();

    public String serverMessage_ResourceExportError();

    public String graphLegend_Header();

    public String graphLegend_collapseTooltip();

    public String graphLegend_expandTooltip();

    public String graphLegend_closeTooltip();

    public String graphLegend_placeHolder();

    public String transparencyWindow_NodeSliderLabel();

    public String transparencyWindow_LinkSliderLabel();

    public String transparencyWindow_LabelSliderLabel();

    public String annotationDialog_Heading();

    public String graphAnnotation_collapseTooltip();

    public String graphAnnotation_expandTooltip();

    public String graphAnnotationDialog_Heading();

    public String graphAnnotationDialog_closeButton();

    public String graphAnnotationDialog_EditButton();

    public String graphSettings_nodeTooltip_nodeName();

    public String graphSettings_nodeTooltip_fromLabelPostfix();

    public String graphSettings_nodeTooltip_fromIdPostfix();

    public String statisticsTab_Heading();

    public String statisticsTab_notYetLoaded();

    public String statisticsTab_lastUpdated();

    public String statisticsTab_loadButton();

    public String statisticsTab_header_name();

    public String graphStatisticsType_TOTAL();

    public String graphStatisticsType_VISIBLE();

    public String timePlayerTab_heading();

    public String timePlayerTab_speed_slow();

    public String timePlayerTab_speed_fast();

    public String timePlayerTab_speed_moderate();

    public String timePlayerTab_speed();

    public String timePlayerTab_hide();

    public String timePlayerTab_frameSize();

    public String nonVisisbleItems();

    public String timeplayerTab_stepSize();

    public String timeplayerTab_stepMode_relativeTime();

    public String timeplayerTab_stepMode_absoluteTime();

    public String timeplayerTab_stepMode_percentage();

    public String timeplayerTab_stepMode();

    public String timeplayerTab_timeWindow();

    public String timePlayerTab_timeControls();

    public String timePlayerTab_timeControls_reset();

    public String timePlayerTab_timeControls_stop();

    public String timePlayerTab_timeControls_step();

    public String timePlayerTab_timeControls_play();

    public String timePlayerTab_tooltip();

    public String timePlayerTab_timeRange();

    public String timePlayerTab_startTime();

    public String userAddNode_duplicateNodeFound();

    public String userAddNode_duplicateNodeMessage(String name, String type);

    public String link();

    public String userAddNOde_DialogTitle();

    public String userAddNode_nodeName();

    public String userAddNode_nodeType();

    public String userAddNode_noIconOption();

    public String patternTab_heading();


    public String patternTab_combinationLimitMessage();

    public String patternTab_permutationLimitMessage();

    public String patternTab_noSearchService();

    public String patternTab_searchSuccessMessage();

    public String patternTab_betaTag();

    public String typeCriteria_valueLabel();

    public String labelCriteria_mustEqualLabel();

    public String directionCriteria_valueLabel();

    public String directionCriteria_directionLabel();

    public String directionCriteria_directionOption_undirected();

    public String directionCriteria_directionOption_reverse();

    public String directionCriteria_directionOption_forward();

    public String directionCriteria_directionOption_bidirectional();

    public String patternResult_loadingMask();

    public String patternResult_labelColumnHeading();

    public String patternTab_autoHighlight();

    public String patternTab_clearHighlight();

    public String patternTab_actions();

    public String patternTab_action_highlight();

    public String patternTab_action_showOnly();

    public String patternTab_action_addToSelection();

    public String patternTab_action_select();

    public String patternTab_links();

    public String patternTab_nodes();

    public String patternSettings_newPattern();

    public String pattern_criteria_nameInput();

    public String pattern_criteria_namePlaceholder();

    public String pattern_criteria_criteriaListHeading();

    public String pattern_criteria_defaultCriterionName();

    public String patttern_criteria_showLabelInResults();

    public String pattern_defaultTypeCriteriaName();

    public String patternSettings_heading();

    public String patternSettings_requireDistinctNodes();

    public String patternSettings_requireDistinctLinks();

    public String pattern_newCriterion_type_numberOfNeighbors_equalTo();

    public String pattern_newCriterion_type_numberOfNeighbors_greaterThan();

    public String pattern_newCriterion_type_numberOfNeighbors_lessThan();

    public String pattern_newCriterion_type_numberOfNeighbors_neighborType();

    public String pattern_newCriterion_type_numberOfNeighbors_includeHiddenNeighbors();

    public String pattern_newCriterion_type_occurrence_equalTo();

    public String pattern_newCriterion_type_occurrence_greaterThan();

    public String pattern_newCriterion_type_occurrence_lessThan();

    public String patternCriterionType_TYPE();

    public String patternCriterionType_LABEL();

    public String patternCriterionType_NUMBER_OF_NEIGHBORS();

    public String patternCriterionType_OCCURRENCE();

    public String patternCriterionType_DIRECTION();

    public String patternCriterionType_FIELD_VALUE();

    public String pathTab_noneFound_Header();

    public String pathTab_noneFound_Body();

    public String pathSettings_currentlySelectedNodes();

    public String pathSettings_limitPathsReturned();

    public String pathSettings_pathsMatchingNodes();

    public String pathSettings_maximumPathLength();

    public String pathSettings_includeDirection();

    public String pathSettings_minimumPathLength();

    public String findingPathsRequiresASelection_errorHeader();

    public String findingPathsRequiresASelection_errorBody();

    public String pathResults_waypoints();

    public String pathResults_targets();

    public String pathResults_source();

    public String pathResults_length();

    public String pathResults_pathName();

    public String pathTab_heading();

    public String pathTab_search();

    public String pathTab_autoHighlight();

    public String pathTab_action_highlight();

    public String pathTab_action_select();

    public String pathTab_action_addToSelection();

    public String pathTab_action_showOnly();

    public String pathTab_action_targetNodes();

    public String pathTab_action_targetLinks();

    public String pathTab_actionButton();

    public String pathTab_clearHighlights();

    public String dragSelect_message();

    public String dragZoom_message();

    public String nodesTab_heading();

    public String nodesTab_searchPlaceholder();

    public String nodesTab_clearSearch();

    public String nodesTab_filters();

    public String nodesTab_filter_selected();

    public String nodesTab_filter_uncommented();

    public String nodesTab_filter_commented();

    public String nodesTab_filter_isBundle();

    public String nodesTab_filter_data();

    public String nodesTab_filter_user();

    public String nodesTab_filter_hidden();

    public String nodesTab_filter_visible();

    public String nodesTab_filter_unbundled();

    public String nodesTab_filter_bundled();

    public String nodesTab_filter_unselected();

    public String nodesTab_filter_clearAll();

    public String nodesTab_actions();

    public String nodesTab_action_zoomTo();

    public String nodesTab_action_show();

    public String nodesTab_action_hide();

    public String nodesTab_action_bundle();

    public String nodesTab_action_unbundle();

    public String nodesTab_action_select();

    public String nodesTab_action_addToSelection();

    public String nodesTab_action_removeFromSelection();

    public String nodesTab_action_exportNodesList();

    public String annotation_title();

    public String graphContextMenu_moreRevealNeighborsByTypePrompt();

    public String graphContextMenu_select();

    public String graphContextMenu_showDetails();

    public String graphContextMenu_deselect();

    public String graphContextMenu_bundle();

    public String graphContextMenu_unbundle();

    public String graphContextMenu_hideSelection();

    public String graphContextMenu_showOnly();

    public String graphContextMenu_revealNeighbors();

    public String graphContextMenu_selectNeighbors();

    public String graphContextMenu_selectAll();

    public String graphContextMenu_deselectAll();

    public String graphContextMenu_addNewNode();

    public String graphContextMenu_addNewLink();

    public String graphContextMenu_edit();

    public String graphContextMenu_delete();

    public String graphContextMenu_comment();

    public String tooltipLabel_contains();

    public String tooltipLabel_computed();

    public String tooltipLabel_directionMap();

    public String tooltipLabel_subgraphNodeID();

    public String tooltipLabel_countInDisplayEdges();

    public String tooltipLabel_componentID();

    public String tooltipLabel_visualItemType();

    public String tooltipLabel_visualized();

    public String tooltipLabel_tooltips();

    public String tooltipLabel_neighborTypeCounts();

    public String tooltipLabel_label();

    public String tooltipLabel__bundle();

    public String tooltipLabel_size();

    public String tooltipLabel_hideLabels();

    public String tooltipLabel_anchored();

    public String tooltipLabel_itemType();

    public String tooltipLabel_itemKey();

    public String tooltipLabel_itemID();

    public String tooltipLabel_selected();

    public String tooltipLabel_clickY();

    public String tooltipLabel_clickX();

    public String tooltipLabel_dislpayY();

    public String tooltipLabel_displayX();

    public String tooltipLabel_y();

    public String tooltipLabel_x();

    public String tooltipLabel_ID();

    public String tooltipLabel_Labels();

    public String tooltipLabel_direction();

    public String tooltipValue_typeBundle();

    public String tooltipLabel_type();

    public String tooltipLabel_comments();

    public String tooltip_editComment();

    public String tooltip_removeComment();

    public String tooltipLabel_undirected();

    public String graph_defaultName();

    public String graph_defaultLinkType();

    public String pieChartSettings_title();

    public String pieChartSettings_labels();

    public String pieChartSettings_legends();

    public String pieChartSettings_enable();

    public String pieChartSettings_showValue();

    public String pieChartSettings_showPercentage();

    public String pieChartSettings_percentageThreshold();

    public String capcoDialog_DialogTitle();

    public String capcoDialog_Instructions(String applyButtonIn, String usageIn, String prompt1In, String prompt2In, String prompt3In, String prompt14n);

    public String capcoDialog_ClassificationSource();

    public String capcoDialog_ClassifyByUser();

    public String capcoDialog_ClassifyByDataUser();

    public String capcoDialog_ClassifyByData();

    public String capcoDialog_UseDefault();

    public String capcoDialog_SecurityFieldsPrompt();

    public String capcoDialog_UserSecurityPrompt();

    public String securityTagsDialog_DialogTitle();

    public String securityTagsDialog_Instructions(String applyButtonIn, String prompt1In, String prompt2In, String prompt3In, String prompt14n);

    public String securityTagsDialog_ClassificationSource();

    public String securityTagsDialog_ClassifyByUser();

    public String securityTagsDialog_ClassifyByDataUser();

    public String securityTagsDialog_ClassifyByData();

    public String securityTagsDialog_NoTags();

    public String securityTagsDialog_SecurityFieldsPrompt();

    public String securityTagsDialog_UserSecurityPrompt();

    public String securityTagsDialog_DelimiterPrompt();

    public String securityTagsDialog_MultiTagMode();

    public String distributionTagsDialog_DialogTitle();

    public String distributionTagsDialog_Instructions(String applyButtonIn, String prompt1In, String prompt2In, String prompt3In, String prompt14n);

    public String distributionTagsDialog_ClassificationSource();

    public String distributionTagsDialog_ClassifyByUser();

    public String distributionTagsDialog_ClassifyByDataUser();

    public String distributionTagsDialog_ClassifyByData();

    public String distributionTagsDialog_NoTags();

    public String distributionTagsDialog_SecurityFieldsPrompt();

    public String distributionTagsDialog_UserSecurityPrompt();

    public String distributionTagsDialog_DelimiterPrompt();


    public String csiDataType_String();

    public String csiDataType_Boolean();

    public String csiDataType_Integer();

    public String csiDataType_Number();

    public String csiDataType_DateTime();

    public String csiDataType_Date();

    public String csiDataType_Time();

    public String csiDataType_Unsupported();

    public String fieldType_COLUMN_REF();

    public String fieldType_SCRIPTED();

    public String fieldType_STATIC();

    public String fieldType_LINKUP_REF();

    public String fieldType_DERIVED();

    public String fieldType_UNMAPPED();

    public String parameterType_TABLE();

    public String parameterType_COLUMN();

    public String parameterType_DATA();

    public String resourceSelector_BrowseLocally();

    public String templateSaveAsDialog_SavingTemplate();

    public String linkupDefinitionPanel_PromptToVerify();

    public String linkupDefinitionPanel_MonitorProgress();

    public String linkupDefinitionPanel_OkToChange();

    public String dataSourceEditor_TableInstructionText();

    public String dataSourceEditor_Table();

    public String dataSourceEditor_Query();

    public String dataSourceEditor_Column();

    public String dataSourceEditor_LeftColumn();

    public String dataSourceEditor_RightColumn();

    public String dataSourceEditor_ToColumn();

    public String dataSourceEditor_FromColumn();

    public String dataSourceEditor_Field();

    public String dataSourceEditor_Map();

    public String dataSourceEditor_Unmap();

    public String dataSourceEditor_Selected();

    public String dataSourceEditor_Name();

    public String dataSourceEditor_DataTable();

    public String dataSourceEditor_DataColumn();

    public String dataSourceEditor_From();

    public String dataSourceEditor_To();

    public String dataSourceEditor_Filter();

    public String dataSourceEditor_CustomQuery();

    public String dataSourceEditor_Add();

    public String dataSourceEditor_Delete();

    public String dataSourceEditor_Type();

    public String dataSourceEditor_Remove();

    public String dataSourceEditor_NoSelection();

    public String dataSourceEditor_JoinType();

    public String dataSourceEditor_AppendOnlyUnique();

    public String dataSourceEditor_NeedTwoColumns();

    public String dataSourceEditor_MappingExists();

    public String dataSourceEditor_SelectDeselect();

    public String dataSourceEditor_DeleteObjectDialogTitle();

    public String dataSourceEditor_DeleteObjectDialogPrompt();

    public String dataSourceEditor_EditSourceTitle();

    public String dataSourceEditor_RemoveSourceMessage1();

    public String dataSourceEditor_RemoveSourceMessage2(String sourceIn);

    public String dataSourceEditor_EditSourceMessage();

    public String dataSourceEditor_RemoveSourceTitle();

    public String editKmlMappingHeader();

    public String editKmlNameLabel();

    public String editKmlLocationLabel();

    public String editKmlAddressRadioLabel();

    public String editKmlLatLongLabel();

    public String editKmlLatLabel();

    public String editKmlLongLabel();

    public String editKmlAddressDropdownLabel();

    public String useMultipleLabel();

    public String editKmlLabelLabel();

    public String editKmlDetailsLabel();

    public String editKmlStartLabel();

    public String editKmlEndLabel();

    public String editKmlDurationLabel();

    public String editKmlFixedRadioLabel();

    public String editKmlFieldRadioLabel();

    public String editKmlFieldIconLabel();

    public String nodesTabTrueValue();

    public String nodesTabBooleanLabel();

    public String nodesTabFalseValue();

    public String nodesTabDataLabel();

    public String nodesTabUserLabel();

    public String nodesTabBundleErrorTitle();

    public String nodesTabBundleErrorMessage();

    public String nodesTabAnchoredColumn();

    public String nodesTabBetweennessColumn();

    public String nodesTabBundledColumn();

    public String nodesTabBudleNameColumn();

    public String nodesTabClosenessColumn();

    public String nodesTabComponentColumn();

    public String nodesTabDegreeColumn();

    public String nodesTabEigenvectorColumn();

    public String nodesTabHiddenColumn();

    public String nodesTabHideLabelsColumn();

    public String nodesTabLabelColumn();

    public String nodesTabSelectedColumn();

    public String nodesTabTypeColumn();

    public String nodesTabVisibleNeighborsColumn();

    public String nodesTabVisualizedColumn();

    public String nodesTabCommentColumn();

    public String nodesTabIsBundleColumn();

    public String nodesTabPageSizeLabel();

    public String nodesTabYesValue();

    public String nodesTabNoValue();

    public String nodesTabSize();

    public String nodesTabOpacity();

    public String scalarValueDefinitionDialogTitle();

    public String bundleFunctionNone();

    public String bundleFunctionLeft();

    public String bundleFunctionRight();

    public String bundleFunctionSubstring();

    public String bundleFunctionLength();

    public String bundleFunctionTrim();

    public String bundleFunctionRegexReplace();

    public String bundleFunctionSplit();

    public String bundleFunctionCountToken();

    public String bundleFunctionSingleToken();

    public String bundleFunctionDate();

    public String bundleFunctionQuarter();

    public String bundleFunctionYearAndMonth();

    public String bundleFunctionYear();

    public String bundleFunctionMonth();

    public String bundleFunctionDayOfMonth();

    public String bundleFunctionHour();

    public String bundleFunctionMinute();

    public String bundleFunctionSecond();

    public String bundleFunctionMillisecond();

    public String bundleFunctionMicrosecond();

    public String bundleFunctionDayOfWeek();

    public String bundleFunctionDayOfYear();

    public String bundleFunctionWeek();

    public String bundleFunctionDayType();

    public String bundleFunctionCeiling();

    public String bundleFunctionFloor();

    public String bundleFunctionRound();

    public String bundleFunctionAbsolute();

    public String bundleFunctionMod();

    public String bundleFunctionSign();

    public String pickListTitle();

    public String pickListValueColumn();

    public String pickListFrequencyColumn();

    public String kmlExportDialogTitle();

    public String kmlExportDialogNewLinkText();

    public String kmlExportDialogFilterLabel();

    public String kmlExportDialogIncludeLabel();

    public String tableSettingsColumnsTabTitle();

    public String tableSettingsColumnsTabAvailableLabel();

    public String tableSettingsColumnsTabSelectedLabel();

    public String tableSettingsColumnsTabAlphaButton();

    public String tableSettingsColumnsTabNaturalButton();

    public String vizSettingsDontLoadAtStartup();

    public String vizSettingsDontLoadAfterSave();

    public String vizSettingsSaveButton();

    public String vizSettingsCancelButton();

    public String chartTypeArea();

    public String chartTypeSpline();

    public String chartTypeBar();

    public String chartTypeColumn();

    public String chartTypeDonut();

    public String chartTypeLine();

    public String chartTypePie();

    public String chartTypePolar();

    public String chartTypeSpider();

    public String chartMeasuresTypeArea();

    public String chartMeasuresTypeAreaSpline();

    public String chartMeasuresTypeColumn();

    public String chartMeasuresTypeDefault();

    public String chartMeasuresTypeDonut();

    public String chartMeasuresTypeLIne();

    public String chartMeasuresTypePie();

    public String axisLabelsX();

    public String axisLabelsY();

    public String matrixMeasuresTabCount();

    public String matrixMeasuresTabStdDev();

    public String matrixMeasuresTabVariance();

    public String matrixMeasuresTabMin();

    public String matrixMeasuresTabMax();

    public String matrixMeasuresTabSum();

    public String matrixMeasuresTabAverage();

    public String matrixMeasuresTabCountDistinct();

    public String matrixMeasuresTabUnity();

    public String matrixMeasuresTabMedian();

    public String windowBaseMinimizeTooltip();

    public String windowBaseResotreTooltip();

    public String windowBaseMaximizeTooltip();

    public String visualizationWindowRestoreTooltip();

    public String visualizationWindowFullscreenTooltip();

    public String timePlayerTabCurrentTime();

    public String timePlayerTabEndTime();

    public String timePlayerTabEventDefinition();

    public String timePlayerTabStartTime();

    public String timePlayerTabNone();

    public String timePlayerTabConstant();

    public String timePlayerTabField();

    public String timePlayerTabEndField();

    public String timePlayerTabDuration();

    public String mapSettingsPresenter_name();

    public String chartSettingsView_title();

    public String chartSettingsView_generalTab();

    public String chartSettingsView_generalTab_displayOnLoad_CHART();

    public String chartSettingsView_generalTab_displayOnLoad_TABLE();

    public String chartSettingsView_measuresTab_FIELDNAME();

    public String chartSettingsView_sortTab_CATEGORY();

    public String chartSettingsView_categoriesTab();

    public String chartSettingsView_measuresTab();

    public String chartSettingsView_sortTab();

    public String chartSettingsView_filterTab();

    public String chartSettingsView_advancedTab();

    public String mapSettingsView_title();

    public String mapSettingsView_generalTab();

    public String mapSettingsView_layersTab();

    public String mapSettingsView_placesTab();

    public String mapSettingsView_associationsTab();

    public String mapSettingsView_tracksTab();

    public String mapSettingsView_placeSize();

    public String mapSettingsView_placeSize_help();

    public String mapSettingsView_useDynamicTypeStatistics();

    public String mapSettingsView_includeNullType();

    public String mapSettingsView_associationStyle();

    public String mapSettingsView_associationWidth();

    public String mapSettingsView_associationDirection();

    public String mapSettingsView_placeTab_association_lineStyle_NONE();

    public String mapSettingsView_placeTab_association_lineStyle_DASH();

    public String mapSettingsView_placeTab_association_lineStyle_DOT();

    public String mapSettingsView_placeTab_association_lineStyle_SOLID();

    public String mapSettingsView_sizeFieldNullFeedbackText();

    public String mapSettingsView_sizeColumnNullFeedbackText();

    public String mapSettingsView_sizeFunctionNullFeedbackText();

    public String mapSettingsView_sizeFieldValueNumberNotRightFeedbackText();

    public String mapSettingsView_typeColumnNullFeedbackText();

    public String mapSettingsView_identityColumnNullFeedbackText();

    public String mapSettingsView_iconColumnNullFeedbackText();

    public String mapSettingsView_association_showButton();

    public String mapLegend_combinedPlaces();

    public String tooltip_Type();

    public String multipleValues();

    public String tooltipLabelMemberTypes();

    public String relationalOperators_lessThan();

    public String relationalOperators_lessThanOrEqual();

    public String relationalOperators_greaterThan();

    public String relationalOperators_greaterThanOrEqual();

    public String relationalOperators_equal();

    public String relationalOperators_notEqual();

    public String relationalOperators_inList();

    public String relationalOperators_isNull();

    public String relationalOperators_wildCardMatch();

    public String relationalOperators_exactMatch();

    public String relationalOperators_caselessMatch();

    public String relationalOperators_included();

    public String relationalOperators_notIncluded();

    public String aclResourceType_DATAVIEW();

    public String aclResourceType_DATAVIEWs();

    public String aclResourceType_TEMPLATE();

    public String aclResourceType_TEMPLATEs();

    public String aclResourceType_DATA_MODEL();

    public String aclResourceType_DATA_MODELs();

    public String aclResourceType_VISUALIZATION();

    public String aclResourceType_VISUALIZATIONs();

    public String aclResourceType_DATA_SOURCE();

    public String aclResourceType_DATA_SOURCEs();

    public String aclResourceType_CONNECTION();

    public String aclResourceType_CONNECTIONs();

    public String aclResourceType_QUERY();

    public String aclResourceType_QUERYs();

    public String aclResourceType_DATA_TABLE();

    public String aclResourceType_DATA_TABLEs();

    public String aclResourceType_LIVE_ASSET();

    public String aclResourceType_LIVE_ASSETs();

    public String aclResourceType_SNAP_SHOT();

    public String aclResourceType_SNAP_SHOTs();

    public String aclResourceType_SAMPLE();

    public String aclResourceType_SAMPLEs();

    public String aclResourceType_ICON();

    public String aclResourceType_ICONs();

    public String aclResourceType_THEME();

    public String aclResourceType_THEMEs();

    public String aclResourceType_DISCARDED();

    public String aclResourceType_DISCARDEDs();

    public String aclResourceType_UNKNOWN();

    public String aclResourceType_UNKNOWNs();

    public String aclResourceType_GRAPH_THEME();

    public String aclResourceType_GRAPH_THEMEs();

    public String aclResourceType_MAP_THEME();

    public String aclResourceType_MAP_THEMEs();

    public String aclResourceType_BROKEN();

    public String aclResourceType_BROKENs();

    public String aclResourceType_BASEMAP();

    public String aclResourceType_BASEMAPs();

    public String aclControlType_CREATE();

    public String aclControlType_READ();

    public String aclControlType_EDIT();

    public String aclControlType_DELETE();

    public String aclControlType_CLASSIFY();

    public String aclControlType_DECLASSIFY();

    public String aclControlType_FIND();

    public String aclControlType_ACCESS();

    public String aclControlType_NEED();

    public String aclControlType_SHARE();

    public String aclControlType_TRANSFER();

    public String aclControlType_EXPORT();

    public String aclControlType_SOURCE_EDIT();

    public String aclControlType_UNSUPPORTED();

    public String aclControlType_EMBEDDED();

    public String delimiter();

    public String divisor();

    public String flags();

    public String index();

    public String length();

    public String regex();

    public String regexReplace();

    public String replacement();

    public String start();

    public String trim();

    public String decimalPlace();

    public String labelDefinitionCellFieldName();

    public String matrixTooltipMinLabel();

    public String matrixTooltipMax();

    public String matrixTooltipAvg();

    public String matrixTooltipSum();

    public String matrixTooltipCount();

    public String matrixSettingsGeneralHeading();

    public String matrixSettingsAxisHeading();

    public String matrixSettingsMeasureHeading();

    public String matrixSettingsColorsHeading();

    public String matrixSettingsSortHeading();

    public String matrixSettingsTitle();

    public String matrixSettingsValidatingMinScaleSize();

    public String matrixSettingsValidatingMaxScaleSize();

    public String matrixDefaultName();

    public String matrixTypeBubble();

    public String matrixTypeCoOccurrence();

    public String matrixTypeCoOccurrenceDir();

    public String matrixTypeHeatMap();

    public String matrixMeasure();

    public String matrixCategory();

    public String chartSettingsPresenterNumberError();

    public String chartSettingsPresenter_filterFeedback();

    public String i18nErrorTitle();

    public String i18nErrorMessage();

    public String durationUnitDays();

    public String durationUnitHours();

    public String durationUnitMilliseconds();

    public String durationUnitMinutes();

    public String durationUnitMonths();

    public String durationUnitSeconds();

    public String durationUnitWeeks();

    public String durationUnitYears();

    public String concatenateFieldsNoValue();

    public String concatenateFieldsSpace();

    public String concatenateFieldsTab();

    public String tooltipTab_name();

    public String tooltipTab_value();

    public String saveDataViewAs_UnknownError();

    public String lostConnectionErrorDialogTitle();

    public String lostConnectionErrorDialogGeneralMessage();

    public String nodesTabSelectedLabel();

    public String nodesTabUnselectedLabel();

    public String nodesTabBundledLabel();

    public String nodesTabUnBundledLabel();

    public String nodesTabVisibleLabel();

    public String nodesTabHiddenLabel();

    public String nodesTabIsBundleLabel();

    public String nodesTabCommentedLabel();

    public String nodesTabUncommentedLabel();

    public String stdDev();

    public String variance();

    public String min();

    public String max();

    public String unity();

    public String median();

    public String loginTitle();

    public String loginButtonText();

    public String passwordRecoveryUrl();

    public String logoutTitle();

    public String logoutMessageText();

    public String loginFailedText();

    public String security_NotAuthorized();

    public String dataSourceEditor_DiscardDialogTitle();

    public String dataSourceEditor_DiscardDialogMessage();

    public String passwordPopup_Title();

    public String opJoinType_EQUI_JOIN();

    public String opJoinType_LEFT_OUTER();

    public String opJoinType_RIGHT_OUTER();

    public String unionType_Unique();

    public String unionType_All();

    public String unionType_Unique_InfoString(String tableOneIn, String TableTwoIn);

    public String unionType_All_InfoString(String tableOneIn, String TableTwoIn);

    public String sqlTokenLabel_TimestampPlusTime();

    public String sqlTokenLabel_TimestampMinusTimeStamp();

    public String sqlTokenLabel_TimestampMinusDate();

    public String sqlTokenLabel_TimestampMinusTime();

    public String sqlTokenLabel_DatePlusDays();

    public String sqlTokenLabel_DatePlusTime();

    public String sqlTokenLabel_DateMinusDate();

    public String sqlTokenLabel_DateMinusDays();

    public String sqlTokenLabel_DateMinusTime();

    public String sqlTokenLabel_TimePlusTime();

    public String sqlTokenLabel_TimeMinusTime();

    public String sqlTokenLabel_TimeMultiplied();

    public String sqlTokenLabel_TimeDivided();

    public String sqlTokenLabel_TimeAsTimeOfDay();

    public String sqlTokenLabel_TimestampAsDate();

    public String sqlTokenLabel_AsNumberOfDays();

    public String sqlTokenLabel_AsNumberOfHours();

    public String sqlTokenLabel_AsNumberOfMinutes();

    public String sqlTokenLabel_AsNumberOfSeconds();

    public String sqlTokenLabel_AsNumberOfMilliseconds();

    public String sqlTokenLabel_AsNumberOfMicroseconds();

    public String numberValidationFeedbackValueNotDoubleMessage();

    public String mapSettingsId();

    public String mapSettingsName();

    public String mapSettingsLat();

    public String mapSettingsLong();

    public String mapSettingsShape();

    public String mapSettingsColor();

    public String mapSettingsColorModel();

    public String mapSettingsWeight();

    public String mapSettingsDisplayIn();

    public String mapSettingsIconSize();

    public String mapSettingsDisplayHeatmap();

    public String mapSettingsDisplayBundles();

    public String mapSettingsDisplayBreadcrumb();

    public String mapSettingsDisplayPoints();

    public String mapSettingsIdentity();

    public String mapSettingsSequence();

    public String mapSettingsView_renderTab();

    public String mapSettingsView_tootipTab();

    public String mapSettingsView_heatmapTab();

    public String mapSettingsView_bundleTab();

    public String mapSettingsView_bundleTab_select();

    public String mapSettingsView_bundleTab_dragCol();

    public String mapSettingsView_bundleTab_fieldCol();

    public String mapSettingsView_bundleTab_shapeCol();

    public String mapSettingsView_bundleTab_colorCol();

    public String mapSettingsView_bundleTab_showLabelCol();

    public String mapSettingsView_bundleTab_allowNullsCol();

    public String mapSettingsView_bundleTab_fromCol();

    public String mapSettingsView_bundleTab_toCol();

    public String mapSettingsView_bundleTab_borderCol();

    public String mapSettingsView_layersTab_layersLabel();

    public String mapSettingsView_layersTab_nameCol();

    public String mapSettingsView_layersTab_visibleCol();

    public String mapSettingsView_layersTab_opacityCol();

    public String mapSettingsView_layersTab_editCellTooltip();

    public String mapSettingsView_layersTab_deleteTooltip();

    public String mapSettingsView_placesTab_placeDefinition();

    public String mapSettingsView_placesTab_newDefinition();

    public String mapSettingsView_placesTab_MapInUseTitle();

    public String mapSettingsView_placesTab_MapInUseMsg();

    public String mapSettingsView_placesTab_PlaceName();

    public String mapSettingsView_placesTab_Latitude();

    public String mapSettingsView_placesTab_Longitude();

    public String mapSettingsView_placesTab_editCellTooltip();

    public String mapSettingsView_placesTab_deleteTooltip();

    public String mapSettingsView_associationsTab_associationDefinition();

    public String mapSettingsView_associationsTab_newDefinition();

    public String mapSettingsView_associationsTab_AssociationName();

    public String mapSettingsView_associationsTab_AssociationSource();

    public String mapSettingsView_associationsTab_AssociationDestination();

    public String mapSettingsView_associationsTab_editCellTooltip();

    public String mapSettingsView_associationsTab_deleteTooltip();

    public String mapSettingsView_tracksTab_trackDefinition();

    public String mapSettingsView_tracksTab_newDefinition();

    public String mapSettingsView_tracksTab_TrackName();

    public String mapSettingsView_tracksTab_editCellTooltip();

    public String mapSettingsView_tracksTab_deleteTooltip();

    public String mapSettingsView_tracksTab_Place();

    public String mapSettingsView_editPlace();

    public String mapSettingsView_editAssociation();

    public String mapSettingsView_editTrack();

    public String mapSettingsView_placeNameNullFeedbackText();

    public String mapSettingsView_placeNameAlreadyExistingFeedbackText();

    public String mapSettingsView_latFieldNullFeedbackText();

    public String mapSettingsView_longFieldNullFeedbackText();

    public String mapSettingsView_latFieldValueNotNumberFeedbackText();

    public String mapSettingsView_longFieldValueNotNumberFeedback();

    public String mapSettingsView_ErrorsIntro();

    public String mapSettingsView_ErrorsTitle();

    public String mapSettingsView_associationNameNullFeedbackText();

    public String mapSettingsView_associationNameAlreadyExistingFeedbackText();

    public String mapSettingsView_trackNameNullFeedbackText();

    public String mapSettingsView_trackNameAlreadyExistingFeedbackText();

    public String mapSettingsView_sourceFieldNullFeedbackText();

    public String mapSettingsView_destinationFieldNullFeedbackText();

    public String mapSettingsView_sourceFieldExistenceValidatorFeedbackText();

    public String mapSettingsView_destinationFieldExistenceValidatorFeedbackText();

    public String mapSettingsView_sourceAndDestinationSameValidatorFeedbackText();

    public String mapSettingsView_sameAssociationInExistenceValidatorFeedbackText();

    public String mapSettingsView_sameTrackInExistenceValidatorFeedbackText();

    public String mapSettingsView_placeFieldNullFeedbackText();

    public String mapSettingsView_placeFieldExistenceValidatorFeedbackText();

    public String menuKeyConstantsZoomOut();

    public String menuKeyConstantsZoomIn();

    public String manageResources_DataViewTab_installDialogTitle();

    public String manageResources_DataViewTab_installDialogContent(String arg1, String arg2);

    public String manageResources_DataViewTab_createDialogTitle();

    public String manageResources_DataViewTab_createDialogContent(String arg1, String arg2);

    public String manageResources_DataViewTab_fromTemplateButton();

    public String manageResources_DataViewTab_fromScratchButton();

    public String manageResources_DataViewTab_newDataViewButton();

    public String manageResources_DataViewTab_newTemplateButton();

    public String manageResources_DataViewTab_newDatTableButton();

    public String manageResources_DataTableTab_foreignTableButton();

    public String manageResources_DataTableTab_uploadFileButton();

    public String manageResources_DataTableTab_sourceEditButton();

    public String manageResources_LayerTab_newLayerButton();

    public String fileInputWidget_Button();

    public String fileInputWidget_FirstRow();

    public String installationType_Label_NewExcel();

    public String installationType_Label_OldExcel();

    public String installationType_Label_CSV();

    public String installationType_Label_XML();

    public String installationType_Label_Text();

    public String installationType_Label_Dump();

    public String installationType_Label_JSON();

    public String installationType_Label_Wrapper();

    public String installationType_Label_Linkup();

    public String installationType_Label_DataView();

    public String installationType_Label_AdHoc();

    public String installationType_Description_NewExcel();

    public String installationType_Description_OldExcel();

    public String installationType_Description_CSV();

    public String installationType_Description_XML();

    public String installationType_Description_Text();

    public String installationType_Description_Dump();

    public String installationType_Description_JSON();

    public String installationType_Description_Wrapper();

    public String installationType_Description_Linkup();

    public String installationType_Description_DataView();

    public String installationType_Description_AdHoc();

    public String updateTableWizard_Title();

    public String updateTableWizard_Success(String tableNameIn);

    public String installFileWizard_Title();

    public String installFileWizard_ParsingError_Title();

    public String installFileWizard_ParsingError_Message(String fileTypeIn);

    public String installFileWizard_OptionError_Message(String fileTypeIn);

    public String installFileWizard_GridCreationError_Message();

    public String installFileWizard_MapCreationError_Message();

    public String installFileWizard_Uploading_Message();

    public String installFileWizard_Success_Message(String tableIn);

    public String installFileWizard_CellDataFormatError(String reasonIn);

    public String installFileWizard_CellDataErrorReason1();

    public String installFileWizard_CellDataErrorReason2();

    public String installFileWizard_CellDataErrorReason3();

    public String installFileWizard_CellDataErrorReason4();

    public String installFileWizard_CellDataErrorReason5(String valueIn);

    public String menuKeyConstantsSearch();

    public String timelineNoResultsMessage();

    public String installFileWizard_SelectFileType_Prompt();

    public String installFileWizard_SelectFileType_Directions();

    public String installFileWizard_SelectFileType_Directions_csv();

    public String installFileWizard_SelectFileType_Directions_txt();

    public String installFileWizard_SelectFile_Prompt();

    public String installFileWizard_SelectFile_Directions(String buttonIn);

    public String installFileWizard_FileFormat_Prompt();

    public String installFileWizard_CsvFileFormat_Directions();

    public String installFileWizard_ExcelFileFormat_Directions();

    public String installFileWizard_ConfigureTable_Directions(String renameButtonIn, String applyButtonIn);

    public String installFileWizard_SelectFile_Encoding();

    public String installFileWizard_SelectFile_ColDelimiter();

    public String installFileWizard_SelectFile_RowDelimiter();

    public String installFileWizard_SelectFile_QuoteCharacter();

    public String installFileWizard_SelectFile_EscapeCharacter();

    public String installFileWizard_SelectFile_NullIndicator();

    public String installFileWizard_GridColumn_1();

    public String installFileWizard_GridColumn_2();

    public String installFileWizard_GridColumn_3();

    public String installFileWizard_GridColumn_4();

    public String installFileWizard_GridColumn_5();

    public String installFileWizard_UploaderTitle();

    public String installFileWizard_InstallingProgress();

    public String installFileWizard_CancelPrompt();

    public String installFileWizard_CancelResponse();

    public String installFileWizard_RenameColumn_Prompt(int idIn, String valueIn, String nameIn);

    public String installFileWizard_RenameColumn_Instructions();

    public String installFileWizard_ChoseOperation();

    public String installFileWizard_TypeEquals();

    public String installFileWizard_ScanningFile();

    public String installFileWizard_InstallingTable();

    public String installFileWizard_NewTableOption();

    public String csiColumnDelimiter_Comma();

    public String csiColumnDelimiter_Tab();

    public String csiColumnDelimiter_Bar();

    public String csiColumnDelimiter_Colon();

    public String csiColumnDelimiter_SemiColon();

    public String csiColumnQuote_DoubleQuote();

    public String csiColumnQuote_SingleQuote();

    public String csiColumnQuote_BackQuote();

    public String csiEscapeCharacter_DoubleQuote();

    public String csiEscapeCharacter_SingleQuote();

    public String csiEscapeCharacter_BackQuote();

    public String csiEscapeCharacter_BackSlash();

    public String csiNullIndicator_EmptyString();

    public String csiNullIndicator_BackSlashN();

    public String csiNullIndicator_UpperNull();

    public String csiNullIndicator_LowerNull();

    public String csiNullIndicator_MixedNull();

    public String csiUserValue_Prompt();

    public String csiUserValue_Display(String promptIn, String valueIn);

    public String useAsPrompt();

    public String useAsColumnNames();

    public String useAsDataValues();

    public String gridWidget_Naming_Error();

    public String gridWidget_Column_Naming_Error();

    public String gridWidget_InstallTable();

    public String gridWidget_HideColumns();

    public String gridWidget_Rename();

    public String gridWidget_Include();

    public String gridWidget_Exclude();

    public String mapViewHome();

    public String mapViewZoomIn();

    public String mapViewZoomOut();

    public String mapViewSelect();

    public String mapViewSearch();

    public String mapViewConfigureHeatmap();

    public String mapViewBack();

    public String mapViewUnpinMap();

    public String mapViewPinMap();

    public String mapViewSelectionModeRectangle();

    public String mapViewSelectionModeCircle();

    public String mapViewSelectionModePolygon();

    public String mapViewSelectionModePan();

    public String mapViewDeselect();

    public String mapViewBlurRadius();

    public String mapViewMaxValue();

    public String mapViewMinValue();

    public String mapViewNoData();

    public String mapViewCannotReachTileService();

    public String mapViewShowpoints();

    public String mapSettingsLabel();

    public String mapSettingsValidatingMinIconSize();

    public String mapSettingsValidatingMaxIconSize();

    public String mapSettingsValidatingMinIconSizeTooSmall();

    public String mapSettingsValidatingMaxIconSizeTooLargeString();

    public String mapSettingsValidatingMinMaxIconSizeValue();

    public String mapSettingsValidatingPlacesNotEmpty();

    public String mapPointLimit();

    public String mapPlaceTypeLimit();

    public String mapTrackTypeLimit();

    public String mapTooManyPoints(int limit, String number);

    public String graphSettingsView_advancedButton();

    public String menuKeyConstants_apply_force();

    public String defaultLayout();

    public String numberOfLayoutIterations();

    public String incrementalLayoutIterations();

    public String hierarchyLayoutLabel();

    public String hierarchyLayoutLeftToRight();

    public String hierarchyLayoutTopToBottom();

    public String hierarchyLayoutRightToLeft();

    public String hierarchyLayoutBottomToTop();

    public String menuKeyConstantsCollapse();

    public String menuKeyConstantsExpand();

    public String menuKeyConstantsCreateSelectionFilter();

    public String dialog_CloseButton();

    public String shapeDefImageFieldException();

    public String linkupGridMenuTopMenuItem();

    public String linkupGridMenuByNameItem();

    public String linkupGridMenuByTypeItem();

    public String linkupGridMenuByExactMatchItem();

    public String linkupGridMenuByPositionItem();

    public String linkupGridMenuByRelativePositionItem();

    public String fileParsingWidget_ColumnNamesRowNumberLabel();

    public String fileParsingWidget_DataStartRowNumberLabel();

    public String fileParsingWidget_TooManyDropDowns();

    public String fileParsingWidget_FileTypeNotSupported(String fileTypeIn);

    public String csiClientInflaterStream_StreamEndsEarly();

    public String csiClientInflaterStream_BufferSizeIllegal(String sizeIn);

    public String csiClientInflaterStream_InflatorNotIdentified();

    public String csiClientInflaterStream_StreamNotIdentified();

    public String csiClientInflaterStream_StreamClosed();

    public String csiClientInflaterStream_MissingOutputBuffer();

    public String csiClientInflaterStream_NoCallback();

    public String csiClientInflaterStream_InflaterInputStreamClosed();

    public String csiClientZipScanner_CantReadInput();

    public String csiClientZipScanner_BadZipEntry(String entryIn);

    public String csiClientZipScanner_StreamNotSupported();

    public String csiClientZipScanner_BadInflatedDataSize();

    public String csiDirectFileAccess_BadSkip();

    public String csiDirectFileAccess_BadSeek();

    public String sandBoxFileStream_CacheExceedsMax(String sizeIn);

    public String sandBoxFileStream_LoadFailure();

    public String fileInstallWizard_ExcludedTableNames();

    public String fileInstallWizard_TableNamingInstructions(String prefixIn);

    public String fileInstallWizard_UnrecognizedType();

    public String fileInstallWizard_UnsupportedType(String typeIn, String fileIn);

    public String fileInstallWizard_UnrecognizedType2(String fileIn);

    public String fileInstallWizard_RetrievingNames();

    public String newExcelInstallWizard_EmptyExcelFile();

    public String newExcelInstallWizard_WorksheetPrompt();

    public String inCommon();

    public String newlyAdded();

    public String multitype();

    public String gridColumnTitle_RowNumber();

    public String gridColumnTitle_FieldDefault(String columnNumberIn);

    public String customQueryInstructions(String buttonIn, String parameterFormatIn, String parameterIn);

    public String administrationDialogs_AskRealm();

    public String userInfoPopup_NewInstructions();

    public String userInfoPopup_UpdateInstructions();

    public String userInfoPopup_Instructions(String initialInstructions, String finalInstructions);

    public String userInfoPopup_CsoInstructions();

    public String userInfoPopup_GroupInstructions();

    public String userInfoPopup_DualInstructions();

    public String sharingInfoPopup_NewInstructions();

    public String sharingInfoPopup_UpdateInstructions();

    public String sharingInfoPopup_Instructions(String initialInstructions);

    public String securityInfoPopup_NewInstructions();

    public String securityInfoPopup_UpdateInstructions();

    public String securityInfoPopup_Instructions(String initialInstructions, String capcoInstructions);

    public String capcoInfoPopup_Instructions();

    public String chartTableTab_Total();

    public String menuKeyConstantsSortAscending();

    public String menuDynamicToggleSortBy(String sortBy);

    public String menuKeyConstantsSortByMeasure();

    public String menuKeyConstantsSortByCategory();

    public String menuKeyConstantsSortByX();

    public String menuKeyConstantsSortByY();

    public String menuKeyConstantsSortDescending();

    public String menuKeyConstantsGroups();

    public String returnEmptyString();

    public String created();

    public String modified();

    public String accessed();

    public String onOrAfter(String actionIn);

    public String before(String actionIn);

    public String creationDateAscending();

    public String creationDateDescending();

    public String accessDateAscending();

    public String accessDateDescending();

    public String ownerUsernameAscending();

    public String ownerUsernameDescending();

    public String resourceNameAscending();

    public String resourceNameDescending();

    public String namePatternMatch();

    public String createResourceFilter();

    public String sortBy();

    public String then();

    public String thenBy();

    public String resourceFilter_Create_DialogTitle();

    public String resourceFilter_Copy_DialogTitle();

    public String resourceFilter_Edit_DialogTitle();

    public String resourceFilterList_DialogTitle();

    public String resourceFilterList_GridColumn_1();

    public String resourceFilterList_GridColumn_2();

    public String resourceFilterList_GridColumn_3();

    public String resourceFilterList_GridColumn_4();

    public String description();

    public String matchPattern();

    public String doMatchPattern();

    public String doNotMatchPattern();

    public String doMatchUsername();

    public String doNotMatchUsername();

    public String doMatchFirstName();

    public String doNotMatchFirstName();

    public String doMatchLastName();

    public String doNotMatchLastName();

    public String doMatchEmail();

    public String doNotMatchEmail();

    public String createdOnOrAfter();

    public String createdBefore();

    public String accessedOnOrAfter();

    public String accessedBefore();

    public String ownedBy();

    public String canRead();

    public String canEdit();

    public String canDelete();

    public String notOwnedBy();

    public String canNotRead();

    public String canNotEdit();

    public String canNotDelete();

    public String displayList();

    public String toModifyList();

    public String selectViewChart();

    public String selectViewMap();

    public String selectViewGraph();

    public String selectViewTable();

    public String selectViewTimeline();

    public String selectViewBarChart();

    public String selectViewSketch();

    public String selectViewMatrix();

    public String chartSelectDialog_title();

    public String chartSelectDialog_addCriterion();

    public String chartSelectDialog_actionButton();

    public String chartSelectDialog_replaceSelection();

    public String chartSelectDialog_lt();

    public String chartSelectDialog_le();

    public String chartSelectDialog_eq();

    public String chartSelectDialog_ge();

    public String chartSelectDialog_gt();

    public String chartSelectDialog_ne();

    public String chartSelectDialog_btw();

    public String chartSelectDialog_top();

    public String chartSelectDialog_topPercent();

    public String chartSelectDialog_bottom();

    public String chartSelectDialog_bottomPercent();

    public String chartSelectDialog_selectMeasure();

    public String chartSelectDialog_selectOperator();

    public String chartSelectDialog_enterMinValue();

    public String chartSelectDialog_enterMaxValue();

    public String chartSelectDialog_enterThresholdValue();

    public String mapper_QuickClick_checkBox();

    public String mapper_MappingInstructions();

    public String mapper_UnmappingInstructions();

    public String mapperMenu_AutoMap_ByExactNameItem();

    public String mapperMenu_AutoMap_ByCaselessNameItem();

    public String mapperMenu_AutoMap_ByTypeItem();

    public String mapperMenu_AutoMap_ByExactMatchItem();

    public String mapperMenu_AutoMap_ByNearMatchItem();

    public String mapperMenu_AutoMap_ByPositionItem();

    public String mapperMenu_AutoMap_ByRelativePositionItem();

    public String mapperMenu_Sort_ByAlphaAscending();

    public String mapperMenu_Sort_ByAlphaDescending();

    public String mapperMenu_Sort_ByOrdinalAscending();

    public String mapperMenu_FieldMap_genNewFieldsItem();

    public String mapper_GridHeader_Field_SortFilter();

    public String mapper_GridHeader_Column_SortFilter();

    public String mapper_GridHeader_Map();

    public String mapper_GridHeader_TableQuery();

    public String mapper_GridHeader_Field();

    public String mapper_GridHeader_Column();

    public String mapper_DSE_MapperTitle();

    public String mapper_FieldGroup_InUse();

    public String mapper_FieldGroup_NotInUse();

    public String mapper_CombineAtSource();

    public String mapper_LimitRowCount();

    public String mapper_DragLeftSourceDefault();

    public String mapper_DragRightSourceDefault();

    public String mapper_DragResultDefault();

    public String locateTemplate_Failure();

    public String noResourceFilter();

    public String adHocResourceFilter();

    public String noResourceFilterRemarks();

    public String adHocResourceFilterRemarks();

    public String filter_name();

    public String selection_filter_name();

    public String templateValidation_Unsuccessful();

    public String templateValidation_TemplateNotFound(String templateNameIn, String templateOwnerIn);

    public String templateValidation_FieldNotFound();

    public String templateValidation_ParameterNotFound();

    public String templateValidation_ParameterDataNotFound();

    public String linkupFieldMapperDialogTitle();

    public String linkupParameterMapperDialogTitle();

    public String linkupDialog_TemplateFieldFailure();

    public String linkupDialog_TemplateParameterFailure();

    public String unexpectedError();

    public String timelineGroupingLegendAllButton();

    public String timelineGroupingLegendNoneButton();

    public String timelineGroupingLegendTitle();

    public String chartNoResultsMessage();

    public String chartExceedChartCategoryLimitMessage(int limit, int number);

    public String chartExceedTableCategoryLimitMessage(int limit, int number);

    public String chartRequestProcessing();

    public String tooltipMoreDetails();

    public String createEditFilterDialog_haveUniqueNameTitle();

    public String createEditFilterDialog_haveUniqueNameBody();

    public String resourceFilter_FilterDialog_Title();

    public String resourceFilter_FilterDialog_NameLabel();

    public String resourceFilter_FilterDialog_AdHocLabel();

    public String resourceFilter_FilterDialog_MatchPattern();

    public String resourceFilter_FilterDialog_CreateOnOrAfter();

    public String resourceFilter_FilterDialog_CreateBefore();

    public String resourceFilter_FilterDialog_AccessOnOrAfter();

    public String resourceFilter_FilterDialog_AccessBefore();
    public String resourceFilter_FilterDialog_SearchNames();
    public String resourceFilter_FilterDialog_SearchRemarks();
    public String resourceFilter_FilterDialog_TodayLabel();
    public String resourceFilter_FilterDialog_TomorrowLabel();
    public String resourceFilter_FilterDialog_TimeUnit();
    public String resourceFilter_FilterDialog_DateValuesAre();
    public String resourceFilter_FilterDialog_Absolute();
    public String resourceFilter_FilterDialog_Relative();
    public String resourceFilter_FilterDialog_PairedListWidgetYes();
    public String resourceFilter_FilterDialog_PairedListWidgetNo();


    public String warningDialog_DeleteResourceTitle(String resourceType);

    public String warningDialog_DeleteResourceMessage(int countIn, String resourceType);

    public String importWizard_Title();

    public String importWizard_HelpTarget();

    public String importWizard_SelectFile_Directions(String selectButtonIn, String nextButtonIn);

    public String importWizard_SelectTheme_Directions(String selectButtonIn, String nextButtonIn);

    public String importWizard_NameResource_Directions(String resourceTypeIn, String buttonTextIn);

    public String importWizard_Success(String resourceTypeIn, String resourceNameIn);

    public String linkup_NoMappingsCanNotMerge_Warning();

    public String dateTimePicker_DateTimePrompt();

    public String dateTimePicker_DatePrompt();

    public String dateTimePicker_TimePrompt();

    public String comparatorError_Title(String dataTypeIn);

    public String comparatorError_Message();

    public String map_place_name();

    public String map_association_name();

    public String map_track_name();
    public String map_metrics_title();
    public String map_metrics_title_short();
    public String map_metrics_PLACE();
    public String map_metrics_PLACES();


    public String directedPresenterLoading();

    public String menuKeyConstantsGroupsReset();

    public String openResourceResponse_Title(String resourceTypeIn);

    public String editResourceResponse_Title(String resourceTypeIn);

    public String renameResourceResponse_Title(String resourceTypeIn);

    public String launchResourceResponse_Title(String resourceTypeIn);

    public String renameResourceResponse_Success(String resourceTypeIn, String nameIn);

    public String renameResourceResponse_Failure(String resourceTypeIn, String nameIn);

    public String renameResource_WatchMessage(String resourceTypeIn, String nameIn);

    public String deleteResource_DisplayOutOfDate();

    public String deleteResource_ResourceDeleted(String resourceTypeIn, String nameIn);

    public String securityBlock_ExportDenied();

    public String securityBlock_SourceEditDenied();

    public String pin();

    public String changeName();

    public String restoreName();

    public String Help_Analytics_Dialog_Email();

    public String Help_Analytics_Dialog_Website_Label();

    public String Help_Analytics_Dialog_Website_Value();

    public String Help_Analytics_Dialog_Phone();

    public String Help_Analytics_Dialog_Getting_Started_Text();

    public String Help_Analytics_Dialog_Tutorial_Text();

    public String sharing_WatchBoxInfo();

    public String sharing_Success();

    public String userValue();

    public String user();

    public String banner();

    public String userAccess();

    public String bannerAndUserAccess();

    public String patternTab_otherError();

    public String sharingWatchBoxText();

    public String saveAsTitle(String resourceTypeIn);

    public String namingTitle(String resourceTypeIn);

    public String renamingTitle(String resourceTypeIn);

    public String currentDataView();

    public String selectedDataView();

    public String selectedTemplate();

    public String conflictListLabel(String pluralResourceType);

    public String saveAsInstructions(String pluralResourceType, String resourceTypeIn, String buttonIn);

    public String namingInstructions(String pluralResourceType, String resourceTypeIn, String buttonIn);

    public String renamingInstructions(String pluralResourceType, String resourceTypeIn, String buttonIn);

    public String broadcastErrorMessage();

    public String broadcastErrorTitle();

    public String mapRequestProcessing();

    public String doo();

    public String doNot();

    public String leftUserHeaderDefault();

    public String rightUserHeaderDefault();

    public String leftGroupHeaderDefault();

    public String rightGroupHeaderDefault();

    public String leftUserGroupHeaderDefault();

    public String rightUserGroupHeaderDefault();

    public String simpleOnOrAfter();

    public String simpleBefore();

    public String pleaseWait();

    public String retrievingConflicts();

    public String testingCustomQuery();

    public String verifyingCustomQuery();

    public String creatingCustomQuery();

    public String blockingCustomQuery();

    public String readOnlyError(String resourceIn);

    public String returnCount(int countIn);

    public String applicationToolbar_manageIcons();

    public String orLabel();

    public String andLabel();

    public String newLabel();

    public String resourceFilterListDialog_Title();

    public String resourceFilterListDialog_ConfirmDeleteTitle();

    public String resourceFilterListDialog_ConfirmDeletePrompt();

    public String resourceFilterListDialog_NameColumnTitle();

    public String resourceFilterListDialog_DescriptionColumnTitle();

    public String resourceFilterListDialog_DefaultColumnTitle();

    public String resourceFilterListDialog_RenameResourceFilter();

    public String resourceFilterListDialog_ResourceFilterSingular();

    public String resourceFilterListDialog_ResourceFilterPlural();

    public String resourceFilterListDialog_AddToPrompt();

    public String dragDeselect_message();

    public String applicationToolbar_importTheme();


    public String themeEditor_graph_title();

    public String themeEditor_map_title();
    public String themeEditor_graph_shape_none();
    public String themeEditor_graph_shape_circle();
    public String themeEditor_graph_shape_triangle();
    public String themeEditor_graph_shape_square();
    public String themeEditor_graph_shape_diamond();
    public String themeEditor_graph_shape_star();
    public String themeEditor_graph_shape_pentagonhouse();
    public String themeEditor_graph_shape_pentagon();
    public String themeEditor_graph_shape_octagon();
    public String themeEditor_graph_shape_hexagon();
    public String themeEditor_map_add_place_tooltip();

    public String themeEditor_map_add_association_tooltip();
    public String themeEditor_map_add_association_general_fieldname();
    public String themeEditor_map_add_association_appearance_width();
    public String themeEditor_map_place_styles_label();

    public String themeEditor_map_association_label();
    public String themeEditor_map_association_tab_general();
    public String themeEditor_map_association_tab_appearance();
    public String themeEditor_map_place_style_general_fieldname();
    public String themeEditor_map_place_style_general();
    public String themeEditor_map_place_style_appearance();
    public String themeEditor_map_place_style_appearance_icon_checkbox();
    public String themeEditor_map_place_style_appearance_icon_browse();
    public String themeEditor_map_place_style_appearance_previous();
    public String themeEditor_map_place_style_appearance_current();
    public String themeEditor_map_place_style_appearance_scale();
    public String themeEditor_default_shape_label();

    public String themeEditor_graph_bundle_style_label();

    public String themeEditor_graph_add_node_style_tooltip();

    public String themeEditor_graph_node_style_label();

    public String themeEditor_graph_add_link_style_tooltip();

    public String themeEditor_graph_add_link_style_label();

    public String themeEditor_graph_node_filter_placeholder();

    public String themeEditor_graph_link_filter_placeholder();
    public String themeEditor_graph_link_style_appearance_width();
    public String themeEditor_graph_link_style_general_fieldname();
    public String themeEditor_graph_link_style_general();
    public String themeEditor_graph_link_style_appearance();
    public String themeEditor_title();

    public String themeEditor_themeExportTooltip();

    public String iconPicker_browseButton();

    public String nodeStyleDialog_title();

    public String nodeStyle_errorUpload();

    public String nodeStyle_iconCheckbox();

    public String nodeStyle_stylePrevious();

    public String nodeStyle_styleCurrent();

    public String nodeStyle_generalTab();

    public String nodeStyle_appearanceTab();

    public String nodeStyle_scaleLabel();

    public String nodeStyle_iconScale();

    public String nodeStyle_filedNames();

    public String themeStyle_ConflictTitle();

    public String themeStyle_ConflictMessage(String styleName, String styleId, String actionButton, String CancelButton);

    public String themeMapButton();

    public String themeGraphButton();

    public String themeExportTitle();

    public String themeExportWarning(String item);

    public String themeExportPrompt(String typeIn, String nameIN, String uuidIn);

    public String themeDeleteTitle();

    public String themeDeleteWarning();

    public String themeCreateTitle();

    public String iconPanelCloseButton();

    public String iconPanelDoneButton();


    public String iconPanelTitle();

    public String iconPanelNameTitle();

    public String iconPanelAvailableTitle();

    public String iconPanelDeleteButton();

    public String iconPanelCurrentTitle();
    public String iconPanelTagFilter();
    public String iconPanelAllTagsTag();
    public String iconPanelIconFilter();
    public String iconPanelClearButton();
    public String iconPanelAddButton();
    public String iconPanelAllTags();
    public String iconPanelWildcard();

    public String export_no_selection_error();

    public String export_error_title();

    public String export_default_error_message();

    public String Sharing_renameToAvoidConclict(String resourceTypeIn);

    public String Sharing_renameTo(String oldNameIn, String newNameIn);

    public String RetrievingTemplates();

    public String menuKeyConstantsBroadcastDirect();

    public String nullCharacterEncountered();

    public String wizard_PreviousButtonHint();

    public String wizard_NextButtonHint();

    public String wizard_CancelButtonHint();

    public String wizard_FinalizeButtonHint(String actionIn, String resourceIn);

    public String editSourcesFor();

    public String creationWizard();

    public String gridAction_Augment();

    public String gridAction_Replace();

    public String gridAction_ResetFilters();

    public String having();

    public String includeOnly();

    public String activePerpetual();

    public String activeNotPerpetual();

    public String disabled();

    public String suspended();

    public String chartView_toggleBreadCrumb();

    public String chartView_fitToScreen();

    public String chartView_showChart();

    public String chartView_showTable();

    public String chartView_drillUp();

    public String menuKeyConstants_show_last_linkup();

    public String userFilter_UserListFilterDialog_Title();

    public String userFilterDialog_Instructions();

    public String userFilterDialog_usernameCheckbox1();
    public String userFilterDialog_firstnameCheckbox1();
    public String userFilterDialog_lastnameCheckbox1();
    public String userFilterDialog_emailCheckbox1();
    public String userFilterDialog_usernameCheckbox2();
    public String userFilterDialog_firstnameCheckbox2();
    public String userFilterDialog_lastnameCheckbox2();
    public String userFilterDialog_emailCheckbox2();

    public String visualization_show_metrics_menu();

    public String visualization_metrics_toggle_all();

    public String visualization_metrics_toggle_view();

    public String visualization_metrics_toggle_all_tooltip();

    public String visualization_metrics_toggle_separator();

    public String matrix_metrics_title();

    public String matrix_metrics_title_short();

    public String matrix_metrics_min();

    public String matrix_metrics_max();

    public String matrix_metrics_totalCells();

    public String matrix_metrics_x_count();

    public String matrix_metrics_y_count();

    public String matrix_view_controlButtons_fit();

    public String matrix_view_controlButtons_zoom_in();

    public String matrix_view_controlButtons_zoom_out();

    public String matrix_view_controlButtons_search();

    public String chart_metrics_title();

    public String chart_metrics_title_short();

    public String chart_metrics_min();

    public String chart_metrics_max();

    public String chart_metrics_total();

    public String mapLayerButton();

    public String mapLayerEditor_editMapLayer();

    public String mapLayerEditor_mapLayerNameLabel();

    public String mapLayerEditor_mapLayerMustHaveName();

    public String mapLayerEditor_mapLayerNameExists();

    public String mapLayerEditor_mapLayerUrlLabel();

    public String mapLayerEditor_mapLayerTypeLabel();

    public String mapLayerEditor_mapLayerLayerNameLabel();

    public String mapPlaceAppearance_identity();

    public String null_label();

    public String empty_string_label();

    public String broadcastAbortedMessage();

    public String broadcastAborted();

    public String broadcastExecutionErrorMessage();

    public String broadcastAlert_broadcastIncoming();

    public String broadcastAlert_broadcastCleared();

    public String tableSaveAsDialog_InfoString();

    public String unexpectedNullFromServer();

    public String installedTableUpdate_SuccessMessage(String tableNameIn);

    public String notSupportedTitle();

    public String installedTableUpdate_NotSupportedMessage();

    public String installedTableUpdate_IncorrectMethodTitle();

    public String installedTableUpdate_UseCapture(String dataViewNameIn, String menuItemIn, String updateSelectionIn,
                                                  String nextButtonIn, String tableNameIn, String finishButtonIn);

    public String tooManyAssociations();

    public String tooManyAssociationsTitle();

    public String tooManyAssociationsMessage(int threshold);

    public String fieldList_DependencyError(String fieldIn, String dependencyIn);

    public String linkinFields();

    public String menuKeyConstants_copy_cells();

    public String menuKeyConstants_quick_unbundle();

    public String dialogConflict();

    public String dialogConflictReopen();

    public String dialogConflictRefresh();

    public String addLeft();

    public String moveLeft();

    public String addRight();

    public String moveRight();

    public String export_DisplayList();

    public String export_DataViews();

    public String export_Templates();

    public String export_Themes();

    public String export_Include();

    public String export_Support();

    public String export_MultiSelection();

    public String export_Raw_XML_out();

    public String export_AllMaps();

    public String export_RequiredMaps();

    public String export_AllIcons();

    public String export_RequiredIcons();

    public String export_AllThemes();

    public String export_RequiredThemes();

    public String export_SelectedThemes();

    public String restrictedCredentialsTitle();

    public String restrictedCredentialsInfo();

    public String fileSelector_compressNulls();

    public String userSortMode_username_ascending();
    public String userSortMode_username_descending();
    public String userSortMode_firstname_ascending();
    public String userSortMode_firstname_descending();
    public String userSortMode_lastname_ascending();
    public String userSortMode_lastname_descending();
    public String userSortMode_email_ascending();
    public String userSortMode_email_descending();

    public String vennDiagram_chooseSet();
    public String vennDiagram_button_filter();
    public String vennDiagram_button_select();
    public String vennDiagram_button_cancel();
    public String vennDiagram_circle_new();
    public String vennDiagram_circle_inCommon();
    public String vennDiagram_circle_existing();
    public String vennDiagram_circle_everythingElse();

    public String table_export_titlePrefix();
    public String table_export_button();
    public String table_export_fileName();
    public String table_export_type();
    public String table_export_size();
    public String table_export_imageSize();
    public String table_export_size_allData();
    public String table_export_size_selectionOnly();

    public String criterionPanel_checkbox_showInResults();
    public String criterionPanel_label_Name();
    public String criterionPanel_label_Type();

    public String aggregateFunction_count();
    public String aggregateFunction_stdDev();
    public String aggregateFunction_variance();
    public String aggregateFunction_minimum();
    public String aggregateFunction_maximum();
    public String aggregateFunction_sum();
    public String aggregateFunction_average();
    public String aggregateFunction_countDistinct();
    public String aggregateFunction_unity();
    public String aggregateFunction_arrayAgg();
    public String aggregateFunction_median();

    String transparencyWindow_MinimumNodeSize();

    String menuKeyConstants_show_overview();
    String menuKeyConstants_hide_overview();

    public String hiddenItemIndicator();

    String applicationToolbar_userDropDown_caption();

    String iconPanelTagsTitle();

    String loginLicenseTime();

    String loginLicenseConcurrent();

    String messagesMenuItem_edit();
    String messagesMenuItem_delete();
    String messagesPanel_addButton();
    String messagesPanel_editSaveButton();
    String messagesPanel_editCancelButton();
    String messagesPanel_emptyAnnotationNotification();
    String messagesPanel_editedMessage();
    String messagesPanel_informationTooltip();
}
