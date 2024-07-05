package csi.client.gwt.i18n;

import csi.server.common.enumerations.*;
import csi.server.common.model.FieldType;
import csi.server.common.model.operator.OpJoinType;
import csi.server.common.model.visualization.chart.DefinitionType;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.server.common.model.visualization.map.LineStyle;
import csi.server.util.sql.api.AggregateFunction;

import java.util.TreeMap;

/**
 * Created by centrifuge on 5/15/2015.
 */
public class EnumMaps {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public static void buildAllMaps() {

        buildCsiDataTypeMap();
        buildFieldTypeMap();
        buildParameterTypeMap();
        buildResourceTypeMaps();
        buildJoinTypeMap();
        buildFileTypeMap();
        buildDelimiterMap();
        buildQuoteCharacterMap();
        buildEscapeCharacterMap();
        buildNullIndicatorMap();
        buildSortModeMap();
        buildSystemParameterMap();
        buildRelationalOperatorMap();
        buildControlTypeMap();
        buildUserSortModeMap();
        buildChartDisplayTypeMap();
        buildDefinitionTypeMap();
        buildGraphStatisticsTypeMap();
        buildTimelineMetricsType();
        buildLineStyleType();
        buildMapMetrics();
        buildAggregateFunction();
    }

    public static void buildCsiDataTypeMap() {

        String[] myLabelMap = new String[CsiDataType.values().length];
        TreeMap<String, CsiDataType> myValueMap = new TreeMap<String, CsiDataType>();

        myLabelMap[CsiDataType.String.ordinal()] = _constants.csiDataType_String();
        myLabelMap[CsiDataType.Boolean.ordinal()] = _constants.csiDataType_Boolean();
        myLabelMap[CsiDataType.Integer.ordinal()] = _constants.csiDataType_Integer();
        myLabelMap[CsiDataType.Number.ordinal()] = _constants.csiDataType_Number();
        myLabelMap[CsiDataType.DateTime.ordinal()] = _constants.csiDataType_DateTime();
        myLabelMap[CsiDataType.Date.ordinal()] = _constants.csiDataType_Date();
        myLabelMap[CsiDataType.Time.ordinal()] = _constants.csiDataType_Time();
        myLabelMap[CsiDataType.Unsupported.ordinal()] = _constants.csiDataType_Unsupported();

        myValueMap.put(_constants.csiDataType_String().toLowerCase(), CsiDataType.String);
        myValueMap.put(_constants.csiDataType_Boolean().toLowerCase(), CsiDataType.Boolean);
        myValueMap.put(_constants.csiDataType_Integer().toLowerCase(), CsiDataType.Integer);
        myValueMap.put(_constants.csiDataType_Number().toLowerCase(), CsiDataType.Number);
        myValueMap.put(_constants.csiDataType_DateTime().toLowerCase(), CsiDataType.DateTime);
        myValueMap.put(_constants.csiDataType_Date().toLowerCase(), CsiDataType.Date);
        myValueMap.put(_constants.csiDataType_Time().toLowerCase(), CsiDataType.Time);
//        myValueMap.put(_constants.csiDataType_Unsupported().toLowerCase(), CsiDataType.Unsupported);
        // Do not include Unsupported since this is not an available data type

        CsiDataType.setI18nLabels(myLabelMap);
        CsiDataType.setI18nValues(myValueMap);
    }

    public static void buildFieldTypeMap() {

        String[] myLabelMap = new String[FieldType.values().length];
        TreeMap<String, FieldType> myValueMap = new TreeMap<String, FieldType>();

        myLabelMap[FieldType.COLUMN_REF.ordinal()] = _constants.fieldType_COLUMN_REF();
        myLabelMap[FieldType.SCRIPTED.ordinal()] = _constants.fieldType_SCRIPTED();
        myLabelMap[FieldType.STATIC.ordinal()] = _constants.fieldType_STATIC();
        myLabelMap[FieldType.LINKUP_REF.ordinal()] = _constants.fieldType_LINKUP_REF();
        myLabelMap[FieldType.DERIVED.ordinal()] = _constants.fieldType_DERIVED();
        myLabelMap[FieldType.UNMAPPED.ordinal()] = _constants.fieldType_UNMAPPED();

        myValueMap.put(_constants.fieldType_COLUMN_REF(), FieldType.COLUMN_REF);
        myValueMap.put(_constants.fieldType_SCRIPTED(), FieldType.SCRIPTED);
        myValueMap.put(_constants.fieldType_STATIC(), FieldType.STATIC);
        myValueMap.put(_constants.fieldType_LINKUP_REF(), FieldType.LINKUP_REF);
        myValueMap.put(_constants.fieldType_DERIVED(), FieldType.DERIVED);
        myValueMap.put(_constants.fieldType_UNMAPPED(), FieldType.UNMAPPED);

        FieldType.setI18nLabels(myLabelMap);
        FieldType.setI18nValues(myValueMap);
    }

    public static void buildParameterTypeMap() {

        String[] myLabelMap = new String[ParameterType.values().length];
        TreeMap<String, ParameterType> myValueMap = new TreeMap<String, ParameterType>();

        myLabelMap[ParameterType.TABLE.ordinal()] = _constants.parameterType_TABLE();
        myLabelMap[ParameterType.COLUMN.ordinal()] = _constants.parameterType_COLUMN();
        myLabelMap[ParameterType.DATA.ordinal()] = _constants.parameterType_DATA();

        myValueMap.put(_constants.parameterType_TABLE(), ParameterType.TABLE);
        myValueMap.put(_constants.parameterType_COLUMN(), ParameterType.COLUMN);
        myValueMap.put(_constants.parameterType_DATA(), ParameterType.DATA);

        ParameterType.setI18nLabels(myLabelMap);
        ParameterType.setI18nValues(myValueMap);
    }

    public static void buildResourceTypeMaps() {

        String[] myLabelMap1 = new String[AclResourceType.values().length];
        String[] myLabelMap2 = new String[AclResourceType.values().length];

        myLabelMap1[AclResourceType.DATAVIEW.ordinal()] = _constants.aclResourceType_DATAVIEW();
        myLabelMap2[AclResourceType.DATAVIEW.ordinal()] = _constants.aclResourceType_DATAVIEWs();
        myLabelMap1[AclResourceType.TEMPLATE.ordinal()] = _constants.aclResourceType_TEMPLATE();
        myLabelMap2[AclResourceType.TEMPLATE.ordinal()] = _constants.aclResourceType_TEMPLATEs();
        myLabelMap1[AclResourceType.DATA_MODEL.ordinal()] = _constants.aclResourceType_DATA_MODEL();
        myLabelMap2[AclResourceType.DATA_MODEL.ordinal()] = _constants.aclResourceType_DATA_MODELs();
        myLabelMap1[AclResourceType.VISUALIZATION.ordinal()] = _constants.aclResourceType_VISUALIZATION();
        myLabelMap2[AclResourceType.VISUALIZATION.ordinal()] = _constants.aclResourceType_VISUALIZATIONs();
        myLabelMap1[AclResourceType.DATA_SOURCE.ordinal()] = _constants.aclResourceType_DATA_SOURCE();
        myLabelMap2[AclResourceType.DATA_SOURCE.ordinal()] = _constants.aclResourceType_DATA_SOURCEs();
        myLabelMap1[AclResourceType.CONNECTION.ordinal()] = _constants.aclResourceType_CONNECTION();
        myLabelMap2[AclResourceType.CONNECTION.ordinal()] = _constants.aclResourceType_CONNECTIONs();
        myLabelMap1[AclResourceType.QUERY.ordinal()] = _constants.aclResourceType_QUERY();
        myLabelMap2[AclResourceType.QUERY.ordinal()] = _constants.aclResourceType_QUERYs();
        myLabelMap1[AclResourceType.DATA_TABLE.ordinal()] = _constants.aclResourceType_DATA_TABLE();
        myLabelMap2[AclResourceType.DATA_TABLE.ordinal()] = _constants.aclResourceType_DATA_TABLEs();
        myLabelMap1[AclResourceType.LIVE_ASSET.ordinal()] = _constants.aclResourceType_LIVE_ASSET();
        myLabelMap2[AclResourceType.LIVE_ASSET.ordinal()] = _constants.aclResourceType_LIVE_ASSETs();
        myLabelMap1[AclResourceType.SNAP_SHOT.ordinal()] = _constants.aclResourceType_SNAP_SHOT();
        myLabelMap2[AclResourceType.SNAP_SHOT.ordinal()] = _constants.aclResourceType_SNAP_SHOTs();
        myLabelMap1[AclResourceType.SAMPLE.ordinal()] = _constants.aclResourceType_SAMPLE();
        myLabelMap2[AclResourceType.SAMPLE.ordinal()] = _constants.aclResourceType_SAMPLEs();
        myLabelMap1[AclResourceType.THEME.ordinal()] = _constants.aclResourceType_THEME();
        myLabelMap2[AclResourceType.THEME.ordinal()] = _constants.aclResourceType_THEMEs();
        myLabelMap1[AclResourceType.ICON.ordinal()] = _constants.aclResourceType_ICON();
        myLabelMap2[AclResourceType.ICON.ordinal()] = _constants.aclResourceType_ICONs();
        myLabelMap1[AclResourceType.DISCARDED.ordinal()] = _constants.aclResourceType_DISCARDED();
        myLabelMap2[AclResourceType.DISCARDED.ordinal()] = _constants.aclResourceType_DISCARDEDs();
        myLabelMap1[AclResourceType.UNKNOWN.ordinal()] = _constants.aclResourceType_UNKNOWN();
        myLabelMap2[AclResourceType.UNKNOWN.ordinal()] = _constants.aclResourceType_UNKNOWNs();
        myLabelMap1[AclResourceType.GRAPH_THEME.ordinal()] = _constants.aclResourceType_GRAPH_THEME();
        myLabelMap2[AclResourceType.GRAPH_THEME.ordinal()] = _constants.aclResourceType_GRAPH_THEMEs();
        myLabelMap1[AclResourceType.MAP_THEME.ordinal()] = _constants.aclResourceType_MAP_THEME();
        myLabelMap2[AclResourceType.MAP_THEME.ordinal()] = _constants.aclResourceType_MAP_THEMEs();
        myLabelMap1[AclResourceType.BROKEN.ordinal()] = _constants.aclResourceType_BROKEN();
        myLabelMap2[AclResourceType.BROKEN.ordinal()] = _constants.aclResourceType_BROKENs();
        myLabelMap1[AclResourceType.MAP_BASEMAP.ordinal()] = _constants.aclResourceType_BASEMAP();
        myLabelMap2[AclResourceType.MAP_BASEMAP.ordinal()] = _constants.aclResourceType_BASEMAPs();

        AclResourceType.setI18nLabels(myLabelMap1, myLabelMap2);
    }

    public static void buildJoinTypeMap() {

        String[] myLabelMap = new String[OpJoinType.values().length];
        TreeMap<String, OpJoinType> myValueMap = new TreeMap<String, OpJoinType>();

        myLabelMap[OpJoinType.EQUI_JOIN.ordinal()] = _constants.opJoinType_EQUI_JOIN();
        myLabelMap[OpJoinType.LEFT_OUTER.ordinal()] = _constants.opJoinType_LEFT_OUTER();
        myLabelMap[OpJoinType.RIGHT_OUTER.ordinal()] = _constants.opJoinType_RIGHT_OUTER();

        myValueMap.put(_constants.opJoinType_EQUI_JOIN(), OpJoinType.EQUI_JOIN);
        myValueMap.put(_constants.opJoinType_LEFT_OUTER(), OpJoinType.LEFT_OUTER);
        myValueMap.put(_constants.opJoinType_RIGHT_OUTER(), OpJoinType.RIGHT_OUTER);

        OpJoinType.setI18nLabels(myLabelMap);
        OpJoinType.setI18nValues(myValueMap);
    }

    public static void buildFileTypeMap() {

        String[] myArray1 = new String[InstallationType.values().length];
        String[] myArray2 = new String[InstallationType.values().length];

        myArray1[InstallationType.NEW_EXCEL.ordinal()] = _constants.installationType_Label_NewExcel();
        myArray1[InstallationType.OLD_EXCEL.ordinal()] = _constants.installationType_Label_OldExcel();
        myArray1[InstallationType.CSV.ordinal()] = _constants.installationType_Label_CSV();
        myArray1[InstallationType.TEXT.ordinal()] = _constants.installationType_Label_Text();
        myArray1[InstallationType.DUMP.ordinal()] = _constants.installationType_Label_Dump();
        myArray1[InstallationType.XML.ordinal()] = _constants.installationType_Label_XML();
        myArray1[InstallationType.JSON.ordinal()] = _constants.installationType_Label_JSON();
        myArray1[InstallationType.WRAPPER.ordinal()] = _constants.installationType_Label_Wrapper();
        myArray1[InstallationType.DATAVIEW.ordinal()] = _constants.installationType_Label_DataView();
        myArray1[InstallationType.ADHOC.ordinal()] = _constants.installationType_Label_AdHoc();

        myArray2[InstallationType.NEW_EXCEL.ordinal()] = _constants.installationType_Description_NewExcel();
        myArray2[InstallationType.OLD_EXCEL.ordinal()] = _constants.installationType_Description_OldExcel();
        myArray2[InstallationType.CSV.ordinal()] = _constants.installationType_Description_CSV();
        myArray2[InstallationType.TEXT.ordinal()] = _constants.installationType_Description_Text();
        myArray2[InstallationType.DUMP.ordinal()] = _constants.installationType_Description_Dump();
        myArray2[InstallationType.XML.ordinal()] = _constants.installationType_Description_XML();
        myArray2[InstallationType.JSON.ordinal()] = _constants.installationType_Description_JSON();
        myArray2[InstallationType.WRAPPER.ordinal()] = _constants.installationType_Description_Wrapper();
        myArray2[InstallationType.DATAVIEW.ordinal()] = _constants.installationType_Description_DataView();
        myArray2[InstallationType.ADHOC.ordinal()] = _constants.installationType_Description_AdHoc();

        InstallationType.setI18nLabels(myArray1);
        InstallationType.setI18nDescriptions(myArray2);
    }

    public static void buildDelimiterMap() {

        String[] myArray = new String[CsiColumnDelimiter.values().length];

        myArray[CsiColumnDelimiter.COMMA.ordinal()] = _constants.csiColumnDelimiter_Comma();
        myArray[CsiColumnDelimiter.TAB.ordinal()] = _constants.csiColumnDelimiter_Tab();
        myArray[CsiColumnDelimiter.BAR.ordinal()] = _constants.csiColumnDelimiter_Bar();
        myArray[CsiColumnDelimiter.COLON.ordinal()] = _constants.csiColumnDelimiter_Colon();
        myArray[CsiColumnDelimiter.SEMI.ordinal()] = _constants.csiColumnDelimiter_SemiColon();

        CsiColumnDelimiter.setI18nLabels(myArray);
    }

    public static void buildControlTypeMap() {
        String[] myArray = new String[AclControlType.values().length];

        myArray[AclControlType.READ.ordinal()] = _constants.aclControlType_READ();
        myArray[AclControlType.EDIT.ordinal()] = _constants.aclControlType_EDIT();
        myArray[AclControlType.DELETE.ordinal()] = _constants.aclControlType_DELETE();
        myArray[AclControlType.CLASSIFY.ordinal()] = _constants.aclControlType_CLASSIFY();
        myArray[AclControlType.DECLASSIFY.ordinal()] = _constants.aclControlType_DECLASSIFY();
        myArray[AclControlType.FIND.ordinal()] = _constants.aclControlType_FIND();
        myArray[AclControlType.ACCESS.ordinal()] = _constants.aclControlType_ACCESS();
        myArray[AclControlType.NEED.ordinal()] = _constants.aclControlType_NEED();
        myArray[AclControlType.SHARE.ordinal()] = _constants.aclControlType_SHARE();
        myArray[AclControlType.TRANSFER.ordinal()] = _constants.aclControlType_TRANSFER();
        myArray[AclControlType.EXPORT.ordinal()] = _constants.aclControlType_EXPORT();
        myArray[AclControlType.SOURCE_EDIT.ordinal()] = _constants.aclControlType_SOURCE_EDIT();
        myArray[AclControlType.UNSUPPORTED.ordinal()] = _constants.aclControlType_UNSUPPORTED();
        myArray[AclControlType.EMBEDDED.ordinal()] = _constants.aclControlType_EMBEDDED();

        AclControlType.setI18nLabels(myArray);
    }

    public static void buildGraphStatisticsTypeMap() {
        String[] myArray = new String[GraphStatisticsType.values().length];

        myArray[GraphStatisticsType.TOTAL.ordinal()] = _constants.graphStatisticsType_TOTAL();
        myArray[GraphStatisticsType.VISIBLE.ordinal()] = _constants.graphStatisticsType_VISIBLE();

        GraphStatisticsType.setI18nLabels(myArray);
    }

    public static void buildChartDisplayTypeMap() {
        String[] myArray = new String[DisplayFirst.values().length];

        myArray[DisplayFirst.CHART.ordinal()] = _constants.chartSettingsView_generalTab_displayOnLoad_CHART();
        myArray[DisplayFirst.TABLE.ordinal()] = _constants.chartSettingsView_generalTab_displayOnLoad_TABLE();

        DisplayFirst.setI18nLabels(myArray);
    }

    public static void buildDefinitionTypeMap() {
        String[] myArray = new String[DefinitionType.values().length];

        myArray[DefinitionType.FIELD_NAME.ordinal()] = _constants.chartSettingsView_measuresTab_FIELDNAME();
        myArray[DefinitionType.CATEGORY.ordinal()] = _constants.chartSettingsView_sortTab_CATEGORY();

        DefinitionType.setI18nLabels(myArray);
    }

    public static void buildAggregateFunction() {
        String[] myArray = new String[AggregateFunction.values().length];

        myArray[AggregateFunction.COUNT.ordinal()] = _constants.aggregateFunction_count();
        myArray[AggregateFunction.STD_DEV.ordinal()] = _constants.aggregateFunction_stdDev();
        myArray[AggregateFunction.VARIANCE.ordinal()] = _constants.aggregateFunction_variance();
        myArray[AggregateFunction.MINIMUM.ordinal()] = _constants.aggregateFunction_minimum();
        myArray[AggregateFunction.MAXIMUM.ordinal()] = _constants.aggregateFunction_maximum();
        myArray[AggregateFunction.SUM.ordinal()] = _constants.aggregateFunction_sum();
        myArray[AggregateFunction.AVERAGE.ordinal()] = _constants.aggregateFunction_average();
        myArray[AggregateFunction.COUNT_DISTINCT.ordinal()] = _constants.aggregateFunction_countDistinct();
        myArray[AggregateFunction.UNITY.ordinal()] = _constants.aggregateFunction_unity();
        myArray[AggregateFunction.ARRAY_AGG.ordinal()] = _constants.aggregateFunction_arrayAgg();
        myArray[AggregateFunction.MEDIAN.ordinal()] = _constants.aggregateFunction_median();

        AggregateFunction.setI18nLabels(myArray);
    }

    public static void buildTimelineMetricsType() {
        String[] myArray = new String[TimelineMetricsType.values().length];

        myArray[TimelineMetricsType.EVENTS.ordinal()] = _constants.timeline_showMetrics_EVENTS();
        myArray[TimelineMetricsType.GROUPS.ordinal()] = _constants.timeline_showMetrics_GROUPS();

        TimelineMetricsType.setI18nLabels(myArray);
    }

    public static void buildLineStyleType() {
        String[] myArray = new String[csi.server.common.model.visualization.map.LineStyle.values().length];

        myArray[LineStyle.DASH.ordinal()] = _constants.mapSettingsView_placeTab_association_lineStyle_DASH();
        myArray[LineStyle.DOT.ordinal()] = _constants.mapSettingsView_placeTab_association_lineStyle_DOT();
        myArray[LineStyle.SOLID.ordinal()] = _constants.mapSettingsView_placeTab_association_lineStyle_SOLID();
        myArray[LineStyle.NONE.ordinal()] = _constants.mapSettingsView_placeTab_association_lineStyle_NONE();

        csi.server.common.model.visualization.map.LineStyle.setI18nLabels(myArray);
    }

    public static void buildMapMetrics() {
        String [] myArray = new String[MapMetricsType.values().length];

        myArray[MapMetricsType.PLACE.ordinal()] = _constants.map_metrics_PLACE();
        myArray[MapMetricsType.PLACES.ordinal()] = _constants.map_metrics_PLACES();

        MapMetricsType.setI18nLabels(myArray);
    }

    public static void buildQuoteCharacterMap() {

        String[] myArray = new String[CsiColumnQuote.values().length];

        myArray[CsiColumnQuote.DOUBLE_QUOTE.ordinal()] = _constants.csiColumnQuote_DoubleQuote();
        myArray[CsiColumnQuote.SINGLE_QUOTE.ordinal()] = _constants.csiColumnQuote_SingleQuote();
        myArray[CsiColumnQuote.BACK_QUOTE.ordinal()] = _constants.csiColumnQuote_BackQuote();

        CsiColumnQuote.setI18nLabels(myArray);
    }

    public static void buildEscapeCharacterMap() {

        String[] myArray = new String[CsiEscapeCharacter.values().length];

        myArray[CsiEscapeCharacter.DOUBLE_QUOTE.ordinal()] = _constants.csiEscapeCharacter_DoubleQuote();
        myArray[CsiEscapeCharacter.SINGLE_QUOTE.ordinal()] = _constants.csiEscapeCharacter_SingleQuote();
        myArray[CsiEscapeCharacter.BACK_QUOTE.ordinal()] = _constants.csiEscapeCharacter_BackQuote();
        myArray[CsiEscapeCharacter.BACK_SLASH.ordinal()] = _constants.csiEscapeCharacter_BackSlash();

        CsiEscapeCharacter.setI18nLabels(myArray);
    }

    public static void buildNullIndicatorMap() {

        String[] myArray = new String[CsiEscapeCharacter.values().length];

        myArray[CsiNullIndicator.EMPTY_STRING.ordinal()] = _constants.csiNullIndicator_EmptyString();
        myArray[CsiNullIndicator.SLASH_N.ordinal()] = _constants.csiNullIndicator_BackSlashN();
        myArray[CsiNullIndicator.UPPER_NULL.ordinal()] = _constants.csiNullIndicator_UpperNull();
        myArray[CsiNullIndicator.LOWER_NULL.ordinal()] = _constants.csiNullIndicator_LowerNull();
        myArray[CsiNullIndicator.MIXED_NULL.ordinal()] = _constants.csiNullIndicator_MixedNull();

        CsiNullIndicator.setI18nLabels(myArray);
    }

    public static void buildSortModeMap() {

        String[] myArray = new String[ResourceSortMode.values().length];

        myArray[ResourceSortMode.CREATE_DATE_ASC.ordinal()] = _constants.creationDateAscending();
        myArray[ResourceSortMode.CREATE_DATE_DESC.ordinal()] = _constants.creationDateDescending();
        myArray[ResourceSortMode.ACCESS_DATE_ASC.ordinal()] = _constants.accessDateAscending();
        myArray[ResourceSortMode.ACCESS_DATE_DESC.ordinal()] = _constants.accessDateDescending();
        myArray[ResourceSortMode.OWNER_ALPHA_ASC.ordinal()] = _constants.ownerUsernameAscending();
        myArray[ResourceSortMode.OWNER_ALPHA_DESC.ordinal()] = _constants.ownerUsernameDescending();
        myArray[ResourceSortMode.NAME_ALPHA_ASC.ordinal()] = _constants.resourceNameAscending();
        myArray[ResourceSortMode.NAME_ALPHA_DESC.ordinal()] = _constants.resourceNameDescending();

        ResourceSortMode.setI18nLabels(myArray);
    }

    public static void buildUserSortModeMap() {
        String[] myArray = new String[UserSortMode.values().length];

        myArray[UserSortMode.USERNAME_ASC.ordinal()] = _constants.userSortMode_username_ascending();
        myArray[UserSortMode.USERNAME_DESC.ordinal()] = _constants.userSortMode_username_descending();
        myArray[UserSortMode.FIRST_NAME_ASC.ordinal()] = _constants.userSortMode_firstname_ascending();
        myArray[UserSortMode.FIRST_NAME_DESC.ordinal()] = _constants.userSortMode_firstname_descending();
        myArray[UserSortMode.LAST_NAME_ASC.ordinal()] = _constants.userSortMode_lastname_ascending();
        myArray[UserSortMode.LAST_NAME_DESC.ordinal()] = _constants.userSortMode_lastname_descending();
        myArray[UserSortMode.EMAIL_ASC.ordinal()] = _constants.userSortMode_email_ascending();
        myArray[UserSortMode.EMAIL_DESC.ordinal()] = _constants.userSortMode_email_descending();

        UserSortMode.setI18nLabels(myArray);
    }

    public static void buildSystemParameterMap() {

        String[] myArray = new String[SystemParameter.values().length];

        myArray[SystemParameter.USER.ordinal()] = _constants.systemParameters_USER();
        myArray[SystemParameter.CLIENT.ordinal()] = _constants.systemParameters_CLIENT();
        myArray[SystemParameter.REMOTE_USER.ordinal()] = _constants.systemParameters_REMOTE_USER();
        myArray[SystemParameter.URL.ordinal()] = _constants.systemParameters_URL();
        myArray[SystemParameter.DN.ordinal()] = _constants.systemParameters_DN();

        SystemParameter.setI18nLabels(myArray);
    }

    public static void buildRelationalOperatorMap() {

        String[] myArray = new String[RelationalOperator.values().length];

        myArray[RelationalOperator.LT.ordinal()] = _constants.relationalOperators_lessThan();
        myArray[RelationalOperator.LE.ordinal()] = _constants.relationalOperators_lessThanOrEqual();
        myArray[RelationalOperator.GT.ordinal()] = _constants.relationalOperators_greaterThan();
        myArray[RelationalOperator.GE.ordinal()] = _constants.relationalOperators_greaterThanOrEqual();
        myArray[RelationalOperator.EQUAL.ordinal()] = _constants.relationalOperators_equal();
        myArray[RelationalOperator.NOT_EQUAL.ordinal()] = _constants.relationalOperators_notEqual();
        myArray[RelationalOperator.IN.ordinal()] = _constants.relationalOperators_inList();
        myArray[RelationalOperator.IS_NULL.ordinal()] = _constants.relationalOperators_isNull();
        myArray[RelationalOperator.LIKE.ordinal()] = _constants.relationalOperators_wildCardMatch();
        myArray[RelationalOperator.MATCHES.ordinal()] = _constants.relationalOperators_exactMatch();
        myArray[RelationalOperator.MATCHES_CASELESS.ordinal()] = _constants.relationalOperators_caselessMatch();
        myArray[RelationalOperator.INCLUDED.ordinal()] = _constants.relationalOperators_included();
        myArray[RelationalOperator.EXCLUDED.ordinal()] = _constants.relationalOperators_notIncluded();

        RelationalOperator.setI18nLabels(myArray);
    }
}
