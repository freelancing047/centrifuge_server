package csi.security.queries;

import csi.security.queries.Constants.QueryMode;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 3/29/2018.
 */

public enum AclResource {
   DATAVIEW("DataView d", "dataview d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND "),
   DELETED_DATAVIEW("DataView d", "dataview d", Constants.WHERE_MODIFIER_PAIR, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.DISCARDED.ordinal()) + ") AND (d.priorType = "
               + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.DISCARDED.ordinal()) + ") AND (d.priortype = "
               + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND "),
   TEMPLATE("DataViewDef d", "dataviewdef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND "),
   DELETED_TEMPLATE("DataViewDef d", "dataviewdef d", Constants.WHERE_MODIFIER_PAIR, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.DISCARDED.ordinal()) + ") AND (d.priorType = "
               + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.DISCARDED.ordinal()) + ") AND (d.priortype = "
               + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND "),
   SAMPLE("DataViewDef d", "dataviewdef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.SAMPLE.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.SAMPLE.ordinal()) + ") AND "),
   THEME("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType IN( " + Integer.toString(AclResourceType.THEME.ordinal()) + "," + ""
               + Integer.toString(AclResourceType.GRAPH_THEME.ordinal()) + "," + ""
               + Integer.toString(AclResourceType.MAP_THEME.ordinal()) + ")) AND ",
         "(d.resourcetype IN( " + Integer.toString(AclResourceType.THEME.ordinal()) + "," + ""
               + Integer.toString(AclResourceType.GRAPH_THEME.ordinal()) + "," + ""
               + Integer.toString(AclResourceType.MAP_THEME.ordinal()) + ")) AND "),
   VISUALIZATION("VisualizationDef d", "visualizationdef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         null, null),
   DATA_MODEL("DataModelDef d", "datamodeldef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN, null, null),
   DATA_SOURCE("DataSourceDef d", "datasourcedef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN, null, null),
   CONNECTION("ConnectionDef d", "connectiondef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN, null, null),
   QUERY("QueryDef d", "querydef d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN, null, null),
   TABLE("InstalledTable d", "installedtable d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.useCount = 1) AND ", "(d.usecount = 1) AND "),
   SPECIFIC_TABLE("InstalledTable d", "installedtable d", Constants.WHERE_MODIFIER_PAIR, Constants.WHERE_MODIFIER_PAIR,
         "(d.useCount = 1) AND ", "(d.usecount = 1) AND "),
   EVERY_TABLE("InstalledTable d", "installedtable d", null, Constants.RESTRICTIVE_JOIN,
         "(d.useCount = 1) AND ", "(d.usecount = 1) AND "),
   ACL("ACL a", "acl a", Constants.WHERE_MODIFIER_PAIR, Constants.WHERE_MODIFIER_PAIR, null, null),
   DATAVIEW_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.DATAVIEW.ordinal()) + ") AND "),
   TEMPLATE_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.TEMPLATE.ordinal()) + ") AND "),
   SAMPLE_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.SAMPLE.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.SAMPLE.ordinal()) + ") AND "),
   THEME_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.THEME.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.THEME.ordinal()) + ") AND "),
   TABLE_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.DATA_TABLE.ordinal()) + ") AND (d.useCount = 1) AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.DATA_TABLE.ordinal()) + ") AND (d.usecount = 1) AND "),
   UNKNOWN_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.UNKNOWN.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.UNKNOWN.ordinal()) + ") AND "),
   ICON("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.ICON.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.ICON.ordinal()) + ") AND "),
   GRAPH_THEME("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.GRAPH_THEME.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.GRAPH_THEME.ordinal()) + ") AND "),
   MAP_THEME("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.MAP_THEME.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.MAP_THEME.ordinal()) + ") AND "),
   MAP_BASEMAP("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.MAP_BASEMAP.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.MAP_BASEMAP.ordinal()) + ") AND "),
   ADMIN_TOOL_RESOURCE("ModelResource d", "modelresource d", Constants.RESTRICTIVE_JOIN, Constants.RESTRICTIVE_JOIN,
         "(d.resourceType = " + Integer.toString(AclResourceType.ADMIN_TOOL.ordinal()) + ") AND ",
         "(d.resourcetype = " + Integer.toString(AclResourceType.ADMIN_TOOL.ordinal()) + ") AND ");

   private String[] name;
   private String[] joinString;
   private String[] alternateString;
   private String[] modifier;

   private AclResource(String javaNameIn, String sqlNameIn, String[] joinStringIn, String[] alternateJoinIn,
                       String javaIn, String sqlIn) {
      name = new String[] { javaNameIn, sqlNameIn };
      joinString = joinStringIn;
      alternateString = alternateJoinIn;
      modifier = new String[] { javaIn, sqlIn };
   }

   public String getName(QueryMode modeIn) {
      return name[modeIn.ordinal()];
   }

   public String getJavaName() {
      return name[0];
   }

   public String getSqlName() {
      return name[1];
   }

   public boolean hasJoin(Constants.QueryMode modeIn) {
      return ((joinString != null) && (joinString[modeIn.ordinal()] != null));
   }

   public String getJoinString(QueryMode modeIn) {
      return (joinString == null) ? null : joinString[modeIn.ordinal()];
   }

   public String getJavaJoinString() {
      return (joinString == null) ? null : joinString[0];
   }

   public String getSqlJoinString() {
      return (joinString == null) ? null : joinString[1];
   }

   public boolean hasAlternateJoin(Constants.QueryMode modeIn) {
      return ((alternateString != null) && (alternateString[modeIn.ordinal()] != null));
   }

   public String getAlternateJoin(QueryMode modeIn) {
      return (alternateString == null) ? null : alternateString[modeIn.ordinal()];
   }

   public String getJavaAlternateJoin() {
      return (alternateString == null) ? null : alternateString[0];
   }

   public String getSqlAlternateJoin() {
      return (alternateString == null) ? null : alternateString[1];
   }

   public boolean hasModifier(Constants.QueryMode modeIn) {
      return ((modifier != null) && (modifier[modeIn.ordinal()] != null));
   }

   public String getModifier(Constants.QueryMode modeIn) {
      return (modifier == null) ? null : modifier[modeIn.ordinal()];
   }

   public String getJavaModifier() {
      return (modifier == null) ? null : modifier[0];
   }

   public String getSqlModifier() {
      return (modifier == null) ? null : modifier[1];
   }
}
