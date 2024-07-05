package csi.security.queries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.security.SecurityMask;
import csi.security.queries.Constants.MatchingMode;
import csi.security.queries.Constants.QueryMode;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.interfaces.SortingEnum;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 3/29/2018.
 */
public class ControlledQueries {
   protected static final Logger LOG = LogManager.getLogger(ControlledQueries.class);

   protected static boolean _doDebug = LOG.isDebugEnabled();
   protected static Set<String> _capcoAccessList = null;
   protected static Set<String> _capcoRelToList = null;

   static Map<AclResourceType, AclResource> resourceTypeMap;

   static {
      resourceTypeMap = new HashMap<AclResourceType, AclResource>();
      resourceTypeMap.put(AclResourceType.DATAVIEW, AclResource.DATAVIEW_RESOURCE);
      resourceTypeMap.put(AclResourceType.TEMPLATE, AclResource.TEMPLATE_RESOURCE);
      resourceTypeMap.put(AclResourceType.SAMPLE, AclResource.SAMPLE_RESOURCE);
      resourceTypeMap.put(AclResourceType.VISUALIZATION, AclResource.VISUALIZATION);
      resourceTypeMap.put(AclResourceType.DATA_MODEL, AclResource.DATA_MODEL);
      resourceTypeMap.put(AclResourceType.DATA_SOURCE, AclResource.DATA_SOURCE);
      resourceTypeMap.put(AclResourceType.CONNECTION, AclResource.CONNECTION);
      resourceTypeMap.put(AclResourceType.DATA_TABLE, AclResource.TABLE);
      resourceTypeMap.put(AclResourceType.QUERY, AclResource.QUERY);
      resourceTypeMap.put(AclResourceType.THEME, AclResource.THEME);
      resourceTypeMap.put(AclResourceType.ICON, AclResource.ICON);
      resourceTypeMap.put(AclResourceType.GRAPH_THEME, AclResource.GRAPH_THEME);
      resourceTypeMap.put(AclResourceType.MAP_THEME, AclResource.MAP_THEME);
      resourceTypeMap.put(AclResourceType.MAP_BASEMAP, AclResource.MAP_BASEMAP);
   }

    protected static QueryBlock loadQueryBlock(QueryBlock queryBlockIn, AclFunction functionIn, String filterIn,
                                               AclScope[] scopeIn, AclResource tableIn, String[] patternIn,
                                               SecurityMask enforceSecurityIn, MatchingMode matchingModeIn,
                                               QueryMode modeIn) {

        queryBlockIn.setApplyScope(false);
        queryBlockIn.setRequiresPermissions(false);
        queryBlockIn.setUseOther(false);

        if ((null != scopeIn) && (0 < scopeIn.length)) {

            for (int i = 0; scopeIn.length > i; i++) {

                AclScope myScope = scopeIn[i];

                if (null != myScope) {

                    if (myScope.hasText()) {

                        queryBlockIn.setApplyScope(true);
                    }
                    if (myScope.requiresPermissions()) {

                        queryBlockIn.setRequiresPermissions(true);
                    }
                    if (myScope.requiresOther()) {

                        queryBlockIn.setUseOther(true);
                    }

                } else {

                    break;
                }
            }
        }
        queryBlockIn.setMatchingMode(matchingModeIn);
        queryBlockIn.setRequiresJoin(functionIn.requiresJoin());
        queryBlockIn.setEnforceSecurity(enforceSecurityIn);
        queryBlockIn.setNoConditions((!tableIn.hasJoin(modeIn)) && (!tableIn.hasModifier(modeIn)));

        return queryBlockIn;
    }

    protected static String createQueryString(QueryBlock queryBlockIn, AclFunction functionIn, String filterIn,
                                            AclScope[] scopeIn, AclResource tableIn, AclCondition conditionIn,
                                            Date[] temporalParmsIn, SortingEnum<?>[] orderByIn, QueryMode modeIn) {

        StringBuilder myBuffer = new StringBuilder();
        SecurityMask _enforceSecurity = queryBlockIn.enforceSecurity();
        boolean myAndSet = false;
        MatchingMode myMatchingMode = queryBlockIn.getMatchingMode();
        //
        // Identify what we are doing against what table
        //
        myBuffer.append(functionIn.getText(modeIn));
        myBuffer.append(" FROM ");
        myBuffer.append(tableIn.getJavaName());

        //
        // Require ACL for any but unrestricted "ANY" scope
        //
        if (queryBlockIn.applyScope() || queryBlockIn.requiresJoin() || _enforceSecurity.hasSecurity()) {

            if (_enforceSecurity.hasSecurity()) {

                if (tableIn.hasAlternateJoin(modeIn)) {

                    myBuffer.append(tableIn.getAlternateJoin(modeIn));
                }

            } else if (tableIn.hasJoin(modeIn)) {

                myBuffer.append(tableIn.getJoinString(modeIn));
            }

            if (queryBlockIn.applyScope()) {

                for (int i = 0; scopeIn.length > i; i++) {

                    AclScope myScope = scopeIn[i];

                    if (null != myScope) {

                        if (myScope.hasText()) {

                            myBuffer.append(myScope.getText(modeIn));
                        }
                    }
                }
            }
            if (null != myMatchingMode) {

                myBuffer.append(myMatchingMode.getText());
            }
            if (_enforceSecurity.hasSecurity()) {

                if (_enforceSecurity.hasGenericRestrictions()) {

                    myBuffer.append(AclSecurity.GENERIC_MODIFIER.getText(modeIn));
                    myBuffer.append(AclSecurity.DISTRIBUTION_MODIFIER.getText(modeIn));
                }
                if (_enforceSecurity.hasCapcoRestrictions()) {

                    myBuffer.append(getCapcoModifier(modeIn));
                }
                if ((AclResource.DATAVIEW == tableIn) || (AclResource.DATAVIEW_RESOURCE == tableIn)
                        || (AclResource.TEMPLATE == tableIn) || (AclResource.TEMPLATE_RESOURCE == tableIn)
                        || (AclResource.SAMPLE == tableIn) || (AclResource.SAMPLE_RESOURCE == tableIn)) {

                    if (_enforceSecurity.hasConfiguredExportRestrictions()) {

                        myBuffer.append(AclSecurity.EXPORT_MODIFIER.getText(modeIn));

                    } else if (_enforceSecurity.hasConfiguredSourceEditRestrictions()) {

                        myBuffer.append(AclSecurity.SOURCE_EDIT_MODIFIER.getText(modeIn));

                    } else if (_enforceSecurity.hasConfiguredAccessRestrictions()) {

                        myBuffer.append(AclSecurity.CONFIGURED_MODIFIER.getText(modeIn));
                    }
                }
            }
            myAndSet = true;

        } else if (!queryBlockIn.noConditions()) {

            myBuffer.append(Constants.WHERE_MODIFIER);
        }

        if (null != temporalParmsIn) {

            String[] myTimeStampTest = Constants.TIMESTAMP_TEST[modeIn.ordinal()];
            int myLimit = Math.min(temporalParmsIn.length, myTimeStampTest.length);

            for (int i =0; myLimit > i; i++) {

                if (null != temporalParmsIn[i]) {

                    myBuffer.append(myTimeStampTest[i]);
                    myBuffer.append(Constants.AND_MODIFIER);
                    myAndSet = true;
                }
            }
        }

        if (null != filterIn) {

            myBuffer.append(filterIn);
            myBuffer.append(Constants.AND_MODIFIER);
            myAndSet = true;
        }

        if (tableIn.hasModifier(modeIn)) {

            myBuffer.append(tableIn.getModifier(modeIn));
            myAndSet = true;
        }

        if ((null != conditionIn) && conditionIn.hasText()) {

            myBuffer.append(conditionIn.getText(modeIn));

        } else if (myAndSet) {

            myBuffer.setLength(myBuffer.length() - 5);
        }

        if ((null != orderByIn) && (0 < orderByIn.length)) {

            String myPrefix = " ORDER BY ";

            for (int i = 0; orderByIn.length > i; i++) {

                SortingEnum mySort = orderByIn[i];

                if (null != mySort) {

                    myBuffer.append(myPrefix);
                    myBuffer.append(mySort.getColumn());
                    myBuffer.append(' ');
                    myBuffer.append(mySort.getDirection());
                    myPrefix = ", ";
                }
            }
        }
        return myBuffer.toString();
    }

    protected static AclResource identifyTable(Class<?> objectTypeIn) {

        AclResource myTable = null;

        if (DataView.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.DATAVIEW;

        } else if (DataViewDef.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.TEMPLATE;

        } else if (DataSourceDef.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.DATA_SOURCE;

        } else if (ConnectionDef.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.CONNECTION;

        } else if (QueryDef.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.QUERY;

        } else if (InstalledTable.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.TABLE;

        } else if (Icon.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.ICON;

        } else if (MapTheme.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.MAP_THEME;

        } else if (GraphTheme.class.isAssignableFrom(objectTypeIn)) {

            myTable = AclResource.GRAPH_THEME;
        }

        return myTable;
    }

   protected static AclControlType[] expandPermissions(AclResource tableIn, AclControlType[] permissionsIn) {
      AclControlType[] results = permissionsIn;

      if ((permissionsIn != null) && ((permissionsIn.length) > 0)) {
         Set<AclControlType> mySet = new TreeSet<AclControlType>();

         for (AclControlType myPermission : permissionsIn) {
            mySet.add(myPermission);

            if (AclControlType.READ == myPermission) {
               mySet.add(AclControlType.EDIT);
            }
            if ((AclControlType.EDIT == myPermission) &&
                ((AclResource.DATAVIEW == tableIn) || (AclResource.DATAVIEW_RESOURCE == tableIn))) {
               mySet.add(AclControlType.READ);
            }
         }
         results = mySet.toArray(new AclControlType[0]);
      }
      return results;
   }

   protected static String getCapcoModifier(QueryMode modeIn) {
      return passesDefaultCapcoAccess()
                ? AclSecurity.CAPCO_MODIFIER_PASS_DEFAULT.getText(modeIn)
                : AclSecurity.CAPCO_MODIFIER_FAIL_DEFAULT.getText(modeIn);
   }

    protected static boolean passesDefaultCapcoAccess() {

        boolean mySuccess = true;
        Set<String> myUserRoles = CsiSecurityManager.getUserRoles();
        Set<String> myRequiredRoles = CsiSecurityManager.getDefaultCapcoAccessList();

        if (null != myRequiredRoles) {

            for (String myRole : myRequiredRoles) {

                if (!myUserRoles.contains(myRole)) {

                    mySuccess = false;
                    break;
                }
            }
        }
        return mySuccess;
    }

    protected static String getResourceDisplayName(Resource resourceIn) {

        return getDisplayName(getResourceName(resourceIn), (null != resourceIn) ? resourceIn.getUuid() : null);
    }

    protected static String getResourceDisplayName(Resource resourceIn, String uuidIn) {

        return getDisplayName(getResourceName(resourceIn), uuidIn);
    }

    protected static String getResourceName(Resource resourceIn) {

        String myName = resourceIn.getName();

        return (null != myName) ? myName.trim() : null;
    }

    protected static String getDisplayName(String nameIn, String uuidIn) {

        String myTrimmedName = (null != nameIn) ? nameIn.trim() : null;
        String myDisplayName =  ((null != myTrimmedName) && (0 < myTrimmedName.length()))
                ? Format.value(myTrimmedName) + ", id = " + Format.value(uuidIn) + ","
                : ", id = " + Format.value(uuidIn) + ",";

        return myDisplayName;
    }
}

class QueryBlock {
    private SecurityMask _enforceSecurity;
    private boolean _applyScope = false;
    private boolean _requiresJoin = false;
    private boolean _requiresPermissions = false;
    private boolean _isProxy = false;
    private boolean _useOther = false;
    private MatchingMode _matchingMode = null;
    private boolean _noConditions = false;

    public void setApplyScope(boolean doSetIn) {

        if (doSetIn) {

            _applyScope = true;
        }
    }

    public boolean applyScope() {

        return _applyScope;
    }

    public void setRequiresJoin(boolean doSetIn) {

        if (doSetIn) {

            _requiresJoin = true;
        }
    }

    public boolean requiresJoin() {

        return _requiresJoin;
    }

    public void setRequiresPermissions(boolean doSetIn) {

        if (doSetIn) {

            _requiresPermissions = true;
        }
    }

    public boolean requiresPermissions() {

        return _requiresPermissions;
    }

    public void setIsProxy(boolean doSetIn) {

        if (doSetIn) {

            _isProxy = true;
        }
    }

    public boolean isProxy() {

        return _isProxy;
    }

    public void setUseOther(boolean doSetIn) {

        if (doSetIn) {

            _useOther = true;
        }
    }

    public boolean useOther() {

        return _useOther;
    }

    public void setMatchingMode(MatchingMode matchingModeIn) {

        _matchingMode = matchingModeIn;
    }

    public MatchingMode getMatchingMode() {

        return _matchingMode;
    }

    public void setEnforceSecurity(SecurityMask enforceSecurityIn) {

        _enforceSecurity = enforceSecurityIn;
    }

    public SecurityMask enforceSecurity() {

        return _enforceSecurity;
    }

    public void setNoConditions(boolean doSetIn) {

        if (doSetIn) {

            _noConditions = true;
        }
    }

    public boolean noConditions() {

        return _noConditions;
    }
}
