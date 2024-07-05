package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationDef;
import csi.shared.core.util.HasLabel;

public enum AclResourceType implements Serializable, HasLabel {
    DATAVIEW("Entire DataView", "Entire DataViews", "DataView", "DataViewList", "DV", DataView.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            //  0
    TEMPLATE("DataView Template", "DataView Templates", "Template", "TemplateList", "TP", DataViewDef.class,
            new AclControlType[]{AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),             //  1
    VISUALIZATION("Visualization", "Visualizations", "", "", "VZ", VisualizationDef.class, null),                  //  2
    DATA_MODEL("Data Model", "Data Models", "Model", "", "DM", DataModelDef.class, null),                               //  3
    DATA_SOURCE("Data Source", "Data Sources", "Source", "", "DS", DataSourceDef.class, null),                           //  4
    CONNECTION("Connection", "Connections", "Connection", "", "CN", ConnectionDef.class, null),                              //  5
    DATA_TABLE("Installed Table", "Installed Tables", "InstalledTable", "InstalledTableList", "IT", InstalledTable.class,
            new AclControlType[] {AclControlType.READ, AclControlType.DELETE}),                                 //  6
    QUERY("Custom Query", "Custom Queries", "Query", "", "QY", QueryDef.class, null),                                   //  7
    LIVE_ASSET("Read-Only DataView", "Read-Only DataViews", "", "", "LA", null, null),                             //  8
    SNAP_SHOT("Display Image", "Display Images", "", "", "SS", null, null),                                        //  9
    SAMPLE("Sample Template", "Sample Templates", "Sample", "SampleList", "SL", DataViewDef.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 10
    THEME("Theme", "Themes", "Theme", "ThemeList", "TM", Theme.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 11
    ICON("Icon", "Icons", "Icon", "IconList", "IC", Icon.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 12
    DISCARDED("Discarded Resource", "Discarded Resources", "Discarded", "", ".", null, null),                                // 13
    UNKNOWN("Unrecognized Resource", "Unrecognized Resources", "Unrecognized", "", ".", null, null),                            // 14

    GRAPH_THEME("Graph Theme", "Graph Themes", "GraphTheme", "GraphThemeList", "TM", GraphTheme.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 15
    MAP_THEME("Map Theme", "Map Themes", "MapTheme", "MapThemeList", "TM", MapTheme.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 16
    BROKEN("Broken Resource", "Broken Resources", "Broken", "", ".", null, null),                                         // 17
    MAP_BASEMAP("Basemap", "Basemaps", "Basemap", "BasemapList", "BM", Basemap.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE}),            // 18
    ADMIN_TOOL("User Admin Tool", "User Admin Tools", "AdminTool", "AdminToolList", "AT", DataViewDef.class,
            new AclControlType[] {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE});            // 19


    private static String[] _i18nLabels = null;
    private static String[] _i18nPlurals = null;
    private static Map<String, AclResourceType> _descriptorMap = null;

    private String _label;
    private String _plural;
    private String _descriptor;
    private String _listTag;
    private String _fileTag;
    private Class<? extends Resource> _objectClass;
    private AclControlType[] _controlArray;

    public static void setI18nLabels(String[] i18nLabelsIn,
                                     String[] i18nPluralsIn) {

        _i18nLabels = i18nLabelsIn;
        _i18nPlurals = i18nPluralsIn;
    }

    private AclResourceType(String labelIn, String pluralIn, String descriptorIn, String listTagIn, String fileTagIn,
                            Class<? extends Resource> _objectClassIn, AclControlType[] controlArrayIn)  {

        _label = labelIn;
        _plural = pluralIn;
        _descriptor = descriptorIn;
        _listTag = listTagIn;
        _fileTag = fileTagIn;
        _objectClass = _objectClassIn;
        _controlArray = controlArrayIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public String getPlural() {

        String myPlural = (null != _i18nPlurals) ? _i18nPlurals[ordinal()] : _plural;
        return (null != myPlural) ? myPlural : _plural;
    }

    public String getDescriptor() {
        return (null != _descriptor) ? _descriptor : "";
    }

    public String getListTag() {
        return (null != _listTag) ? _listTag : "";
    }

    public static AclResourceType getTypeFromDescriptor(String descriptorIn) {
        return getDescriptionMap().get(descriptorIn);
    }

   public String getFileTag() {
      return (_fileTag == null) ? "" : _fileTag;
   }

   public boolean canBeFiltered() {
      return (DATAVIEW == this) || (TEMPLATE == this) || (DATA_TABLE == this);
   }

   public boolean canBeImported() {
      return (DATAVIEW == this) || (TEMPLATE == this);
   }

    public Class<? extends Resource> getObjectClass() {

        return _objectClass;
    }

    public AclControlType[] getControlArray() {

        return _controlArray;
    }

    private static Map<String, AclResourceType> getDescriptionMap() {

        if (null == _descriptorMap) {

            _descriptorMap = new TreeMap<String, AclResourceType>();

            for (AclResourceType myType : AclResourceType.values()) {

                _descriptorMap.put(myType.getDescriptor(), myType);
            }
        }
        return _descriptorMap;
    }
}
