package csi.security.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.CsiSecurityManager;
import csi.security.SecurityMask;
import csi.security.queries.Constants.MatchingMode;
import csi.security.queries.Constants.QueryMode;
import csi.server.business.helper.QueryHelper;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ResourceSortMode;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 3/27/2018.
 */
public class DirectMetaDB extends ControlledQueries {
   protected static final Logger LOG = LogManager.getLogger(DirectMetaDB.class);

   protected static boolean _doDebug = LOG.isDebugEnabled();
   protected static QueryMode _mode = QueryMode.SQL;

    private static class AclBuilder {

        private String _uuid;
        private String _owner;
        private StringBuilder[] _myAclBuffer;

        public AclBuilder(ResultSet resultSetIn) throws SQLException {

            _myAclBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            _uuid = resultSetIn.getString(1);
            _owner = resultSetIn.getString(2);
            addEntry(resultSetIn);
        }

        public void addEntry(ResultSet resultSetIn) throws SQLException {

            int myType = resultSetIn.getInt(3) - 1;

            if ((0 <= myType) && (3 > myType)) {

                _myAclBuffer[myType].append(resultSetIn.getString(4));
                _myAclBuffer[myType].append(",");
            }
        }

        public String[] generateAclStrings() {

            for (int i = 0; 3 > i; i++) {

                _myAclBuffer[i].setLength(Math.max(0, (_myAclBuffer[i].length() - 1)));
            }
            return new String[] {_myAclBuffer[0].toString(), _myAclBuffer[1].toString(), _myAclBuffer[2].toString()};
        }

        public SharingDisplay generateSharingDisplay() {

            for (int i = 0; 3 > i; i++) {

                _myAclBuffer[i].setLength(Math.max(0, (_myAclBuffer[i].length() - 1)));
            }
            return new SharingDisplay(_uuid, _owner, _myAclBuffer[0].toString(),
                    _myAclBuffer[1].toString(), _myAclBuffer[2].toString());
        }
    }

    public static List<SharingDisplay> getAclInfoAvoidingSecurity(List<String> uuidListIn) {

        List<SharingDisplay> myResultList = new ArrayList<SharingDisplay>();

        if ((uuidListIn != null) && !uuidListIn.isEmpty()) {

            Map<String, AclBuilder> myAclMap = createAclMap(uuidListIn);

            for (AclBuilder myBuilder : myAclMap.values()) {

                myResultList.add(myBuilder.generateSharingDisplay());
            }
            for (String myUuid : uuidListIn) {

                if (null == myAclMap.get(myUuid)) {

                    myResultList.add(new SharingDisplay(myUuid, null, null, null, null));
                }
            }
        }
        return myResultList;
    }

    public static List<ResourceBasics> listMatchingTablesAvoidingSecurity(String patternIn)
            throws CentrifugeException, SQLException {

        return filterResourcesBasedOnString(AclResourceType.DATA_TABLE, patternIn, null);
    }

    public static List<ResourceBasics> filterResourcesBasedOnString(AclResourceType resourceTypeIn, String matchingIn,
                                                                    AclControlType[] permissionsIn)
            throws CentrifugeException, SQLException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        MetaQuery myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource, null, matchingIn, null, permissionsIn);

        return retrieveResourceBasicsList(myQuery.execute());
    }

    public static List<ResourceBasics> filterResources(AclResourceType resourceTypeIn,
                                                       ResourceFilter filterIn, AclControlType[] permissionsIn)
            throws CentrifugeException, SQLException {

        AclResource myResource = resourceTypeMap.get(resourceTypeIn);
        MetaQuery myQuery = buildFilterQuery(AclFunction.LIST_BASICS, myResource, filterIn, null, null, permissionsIn);

        return retrieveResourceBasicsList(myQuery.execute());
    }

    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:

   public static List<SharingDisplay> getSharingNamesAvoidingSecurity(AclResourceType resourceTypeIn,
                                                                      ResourceFilter filterIn, String patternIn,
                                                                      String ownerIn)
         throws CentrifugeException, SQLException {
      List<SharingDisplay> resultList = null;
      AclResource resource = resourceTypeMap.get(resourceTypeIn);
      MetaQuery query = buildFilterQuery(AclFunction.LIST_RESOURCE, resource, filterIn, patternIn, ownerIn, null);

      if (query != null) {
         try (ResultSet results = query.execute()) {
            if (results != null) {
               resultList = new ArrayList<>();

               while (results.next()) {
                  resultList.add(loadSharingResults(results, null));
               }
            }
         }
      }
      return resultList;
   }

    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:

    public static SharingDisplay getSingleSharingNameAvoidingSecurity(AclResourceType resourceTypeIn, String nameIn,
                                                                      String ownerIn)
            throws CentrifugeException, SQLException {

        AclResource resource = resourceTypeMap.get(resourceTypeIn);
        MetaQuery query = buildFilterQuery(AclFunction.LIST_RESOURCE, resource, null, nameIn, ownerIn, null);
        ResultSet results = (query == null) ? null : query.execute();
        String myKey = (results == null) ? null : results.getString(1);
        Map<String, AclBuilder> aclMap = createAclMap(Arrays.asList(myKey));
        AclBuilder aclBuilder = aclMap.get(myKey);

        return loadSharingResults(results, aclBuilder.generateAclStrings());
    }

    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:


    // TODO:

   public static List<SharingDisplay> getSharingNamesAvoidingSecurity(List<String> uuidListIn)
         throws CentrifugeException, SQLException {
      List<SharingDisplay> resultList = null;
      Map<String,AclBuilder> aclMap = createAclMap(uuidListIn);
      MetaQuery query = new MetaQuery();

      query.addQueryText(AclFunction.LIST_RESOURCE.getSql());
      query.addQueryText(" FROM modelresource d, ACL a WHERE d.uuid IN (:ids) AND d.uuid = a.uuid");
      query.addParameter("ids", uuidListIn, MetaQuery.ParameterType.STRING_LIST);

      try (ResultSet results = query.execute()) {
         if (results != null) {
            resultList = new ArrayList<SharingDisplay>();

            while (results.next()) {
               AclBuilder aclBuilder = aclMap.get(results.getString(1));

               resultList.add(loadSharingResults(results, aclBuilder.generateAclStrings()));
            }
         }
      }
      return resultList;
   }

    private static MetaQuery buildFilterQuery(AclFunction requestIn, AclResource resourceTypeIn, ResourceFilter filterIn,
                                          String matchPatternIn, String ownerIn, AclControlType[] permissionsIn)
            throws CentrifugeException, SQLException {

        List<String> myOwnerMatchList = (null != ownerIn) ? Arrays.asList(ownerIn) : null;
        List<String> myOwnerRejectList = null;
        List<String> myReadMatchList = null;
        List<String> myReadRejectList = null;
        List<String> myEditMatchList = null;
        List<String> myEditRejectList = null;
        List<String> myDeleteMatchList = null;
        List<String> myDeleteRejectList = null;
        String myMatchPattern = ((null != matchPatternIn) && (0 < matchPatternIn.length())) ? matchPatternIn : null;
        String myRejectPattern = null;
        boolean myTestName = true;
        boolean myTestRemarks = true;
        Date[] myTemporalParameters = null;
        ResourceSortMode[] mySortingRequest = null;
        MatchingMode myMatchingMode = null;

        if (null != filterIn) {

            myOwnerMatchList = filterIn.getOwnerMatchForAcl();
            myOwnerRejectList = filterIn.getOwnerRejectForAcl();
            myReadMatchList = filterIn.getAccessMatchForAcl();
            myReadRejectList = filterIn.getAccessRejectForAcl();
            myEditMatchList = filterIn.getEditMatchForAcl();
            myEditRejectList = filterIn.getAccessRejectForAcl();
            myDeleteMatchList = filterIn.getAccessMatchForAcl();
            myDeleteRejectList = filterIn.getAccessRejectForAcl();
            if (null == myMatchPattern) {

                myMatchPattern = filterIn.getMatchPattern();
                myRejectPattern = filterIn.getRejectPattern();
                myTestName = filterIn.getTestName();
                myTestRemarks = filterIn.getTestRemarks();
            }
            myTemporalParameters = filterIn.getTemporalValuesForQuery();
            mySortingRequest = filterIn.getSortingRequest();
        }
        myMatchingMode = MatchingMode.determineMode(myMatchPattern, myRejectPattern, myTestName, myTestRemarks);
        return buildFilterQuery(requestIn, resourceTypeIn, myMatchPattern, myRejectPattern,
                myOwnerMatchList, myOwnerRejectList, myReadMatchList, myReadRejectList,
                myEditMatchList, myEditRejectList, myDeleteMatchList, myDeleteRejectList,
                myTemporalParameters, permissionsIn, mySortingRequest, myMatchingMode);
    }

    private static MetaQuery buildFilterQuery(AclFunction requestIn, AclResource resourceTypeIn,
                                          String matchPatternIn, String rejectPatternIn,
                                          List<String> ownerMatchIn, List<String> ownerRejectIn,
                                          List<String> readMatchIn, List<String> readRejectIn,
                                          List<String> editMatchIn, List<String> editRejectIn,
                                          List<String> deleteMatchIn, List<String> deleteRejectIn,
                                          Date[] temporalFiltersIn, AclControlType[] permissionsIn,
                                          ResourceSortMode[] sortModeIn, MatchingMode matchingModeIn)
            throws CsiSecurityException {

        MetaQuery query = null;
        SecurityMask mySecurityMask = (null != permissionsIn)
                ? new SecurityMask(true)
                : SecurityMask.getNoSecurityMask();
        String[] myPatterns = new String[]{matchPatternIn, rejectPatternIn};
        List<AclScope> myScope = new ArrayList<AclScope>();
        String myFilter = null;
        ResourceSortMode[] mySortMode = (null != sortModeIn)
                ? sortModeIn
                : new ResourceSortMode[] {ResourceSortMode.NAME_ALPHA_ASC, ResourceSortMode.OWNER_ALPHA_ASC};

        if ((null != permissionsIn) && (0 < permissionsIn.length)) {

            myScope.add(AclScope.AUTHORIZED);
        }
        if ((null != readMatchIn) && !readMatchIn.isEmpty()) {

            myScope.add(AclScope.READER_FILTER);
        }
        if ((null != readRejectIn) && !readRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_READER_FILTER);
        }
        if ((null != editMatchIn) && !editMatchIn.isEmpty()) {

            myScope.add(AclScope.EDITER_FILTER);
        }
        if ((null != editRejectIn) && !editRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_EDITER_FILTER);
        }
        if ((null != deleteMatchIn) && !deleteMatchIn.isEmpty()) {

            myScope.add(AclScope.DELETER_FILTER);
        }
        if ((null != deleteRejectIn) && !deleteRejectIn.isEmpty()) {

            myScope.add(AclScope.NOT_DELETER_FILTER);
        }
        if (!myScope.isEmpty()) {
/*
        RESOURCE_OWNER_FILTER("(d.owner IN (:owners)) AND ", false, false, false),
        RESOURCE_NOT_OWNER_FILTER("(d.owner NOT IN (:notOwners)) AND ", false, false, false);

 */
            if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

                if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                    myScope.add(AclScope.OWNER_FILTER);
                }
                if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                    myScope.add(AclScope.NOT_OWNER_FILTER);
                }

            } else {

                myScope.add(AclScope.OWNED);
            }

        } else {

            if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

                if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                    if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                        myFilter = AclScope.RESOURCE_OWNER_FILTER.getSql() + Constants.AND_MODIFIER
                                + AclScope.RESOURCE_NOT_OWNER_FILTER.getSql();

                    } else {

                        myFilter = AclScope.RESOURCE_OWNER_FILTER.getSql();
                    }

                } else  if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                    myFilter = AclScope.RESOURCE_NOT_OWNER_FILTER.getSql();
                }

            } else {

                myFilter = AclScope.RESOURCE_OWNED.getSql();
            }
        }

        query = createSqlQuery(requestIn, myFilter, myScope.toArray(new AclScope[0]), resourceTypeIn, null, null,
                               permissionsIn, mySortMode, myPatterns, null, mySecurityMask, temporalFiltersIn, matchingModeIn);

        if (query != null) {
           if (!myScope.isEmpty()) {

              if ((null != readMatchIn) && !readMatchIn.isEmpty()) {

                 query.addParameter("readers", readMatchIn, MetaQuery.ParameterType.STRING_LIST);
              }
              if ((null != readRejectIn) && !readRejectIn.isEmpty()) {

                 query.addParameter("notReaders", readRejectIn, MetaQuery.ParameterType.STRING_LIST);
              }
              if ((null != editMatchIn) && !editMatchIn.isEmpty()) {

                 query.addParameter("editors", editMatchIn, MetaQuery.ParameterType.STRING_LIST);
              }
              if ((null != editRejectIn) && !editRejectIn.isEmpty()) {

                 query.addParameter("notEditors", editRejectIn, MetaQuery.ParameterType.STRING_LIST);
              }
              if ((null != deleteMatchIn) && !deleteMatchIn.isEmpty()) {

                 query.addParameter("deleters", deleteMatchIn, MetaQuery.ParameterType.STRING_LIST);
              }
              if ((null != deleteRejectIn) && !deleteRejectIn.isEmpty()) {

                 query.addParameter("notDeleters", deleteRejectIn, MetaQuery.ParameterType.STRING_LIST);
              }
           } else {

              if ((AclFunction.LIST_BASICS == requestIn) || CsiSecurityManager.isSpecialAccess()) {

                 if ((null != ownerMatchIn) && !ownerMatchIn.isEmpty()) {

                    query.addParameter("owners", ownerMatchIn, MetaQuery.ParameterType.STRING_LIST);
                 }
                 if ((null != ownerRejectIn) && !ownerRejectIn.isEmpty()) {

                    query.addParameter("notOwners", ownerRejectIn, MetaQuery.ParameterType.STRING_LIST);
                 }

              } else {

                 query.addParameter("roles", CsiSecurityManager.getUserName(), MetaQuery.ParameterType.STRING);
              }
           }
        }
        return query;
     }

    private static MetaQuery createSqlQuery(AclFunction functionIn, String filterIn, AclScope[] scopeIn,
                                         AclResource tableIn, AclCondition conditionIn, Object[] parametersIn,
                                         AclControlType[] permissionsIn, ResourceSortMode[] orderByIn, String[] patternIn,
                                         List<Set<String>> otherIn, SecurityMask enforceSecurityIn,
                                         Date[] temporalParmsIn, MatchingMode matchingModeIn)
            throws CsiSecurityException {

        AclControlType[] myPermissions = expandPermissions(tableIn, permissionsIn);
        QueryBlock myQueryBlock = loadQueryBlock(new QueryBlock(), functionIn, filterIn, scopeIn, tableIn, patternIn,
                (null != enforceSecurityIn) ? enforceSecurityIn : new SecurityMask(true), matchingModeIn, _mode);

        String myQueryString = createQueryString(myQueryBlock, functionIn, filterIn, scopeIn, tableIn,
                                                    conditionIn, temporalParmsIn, orderByIn, _mode);
        LOG.debug("QUERY:\n" + myQueryString);

        try {

            // LOG.info("EXECUTE QUERY: " + Display.value(myQueryString));

            return createQuery(myQueryBlock, myQueryString, parametersIn,
                                myPermissions, patternIn, otherIn, temporalParmsIn);

        } catch (Exception myException) {

            LOG.error("Caught exception:\n" + Format.value(myException)
                    + "\n while building query:\n" + Format.value(myQueryString));
        }
        return null;
    }

    protected static MetaQuery createQuery(QueryBlock queryBlockIn, String queryStringIn,
                                       Object[] parametersIn, AclControlType[] permissionsIn,
                                       String[] patternIn, List<Set<String>> otherIn, Date[] temporalParmsIn) {
        Query myQuery = null;

        if (null != queryStringIn) {

            EntityManager myManager = CsiPersistenceManager.getMetaEntityManager();

            myQuery = myManager.createQuery(queryStringIn);

            if (_doDebug) {
               LOG.debug("Query String: [ " + queryStringIn + " ]");
            }

            if (queryBlockIn.applyScope() || queryBlockIn.enforceSecurity().hasSecurity()) {

                if (0 <= queryStringIn.indexOf(":roles")) {

                    if (_doDebug) {

                        LOG.debug("parameter \"roles\": [ " + CsiSecurityManager.getUserRoles() + " ]");
                    }
                    myQuery.setParameter("roles", CsiSecurityManager.getUserRoles());
                }
            }

            if (queryBlockIn.useOther()) {
               int howMany = otherIn.size();

               for (int i = 0; i < howMany; i++) {
                    if (_doDebug) {

                        LOG.debug("parameter \"other" + Integer.toString(i + 1) + "\": [ " + otherIn.get(i) + " ]");
                    }
                    myQuery.setParameter("other" + Integer.toString(i + 1), otherIn.get(i));
                }
            }

            if (null != queryBlockIn.getMatchingMode()) {

                if (0 <= queryStringIn.indexOf(":match")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"match\": [ " + patternIn[0] + " ]");
                    }
                    myQuery.setParameter("match", patternIn[0]);
                }
                if (0 <= queryStringIn.indexOf(":reject")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"reject\": [ " + patternIn[1] + " ]");
                    }
                    myQuery.setParameter("reject", patternIn[1]);
                }
            }

            if (queryBlockIn.requiresPermissions()) {

                if (0 <= queryStringIn.indexOf(":permissions")) {


                    if (_doDebug) {

                        LOG.debug("parameter \"permissions\": [ " + permissionsIn + " ]");
                    }
                    myQuery.setParameter("permissions", Arrays.asList(permissionsIn));
                }
            }

            if ((null != parametersIn) && (0 < parametersIn.length)) {

                for (int i = 0; parametersIn.length > i; i++) {


                    if (_doDebug) {

                        LOG.debug("parameter \"parm" + Integer.toString(i + 1) + "\": [ " + parametersIn[i] + " ]");
                    }
                    myQuery.setParameter("parm" + Integer.toString(i + 1), parametersIn[i]);
                }
            }

            if (null != temporalParmsIn) {

                for (int i = 0; temporalParmsIn.length > i; i++) {

                    if (Constants.JAVA_TIMESTAMP_TEST.length <= i) {

                        break;
                    }
                    if (null != temporalParmsIn[i]) {

                        if (_doDebug) {

                            LOG.debug("parameter \"date" + Integer.toString(i + 1) + "\": [ "
                                    + temporalParmsIn[i] + " ]");
                        }
                        myQuery.setParameter("date" + Integer.toString(i + 1), temporalParmsIn[i]);
                    }
                }
            }
        }
        return null;
    }

    private static List<ResourceBasics> retrieveResourceBasicsList(ResultSet resultSetIn) throws SQLException {

        List<ResourceBasics> myResults = new ArrayList<>();
        String myUser = CsiSecurityManager.getUserName();

        if (null != resultSetIn) {

            while (resultSetIn.next()) {

                myResults.add(new ResourceBasics(resultSetIn.getString(1), resultSetIn.getString(2),
                                                    resultSetIn.getString(3), resultSetIn.getTimestamp(4),
                                                    resultSetIn.getString(5), resultSetIn.getLong(6), myUser));
            }
        }
        return myResults;
    }

    private static SharingDisplay loadSharingResults(ResultSet rowIn, String[] aclIn)
            throws CentrifugeException, SQLException {

        SharingDisplay myResult = null;

        if (null != rowIn) {

            myResult = new SharingDisplay( rowIn.getString(1),  rowIn.getString(2),  rowIn.getString(3),
                                            rowIn.getTimestamp(4), rowIn.getTimestamp(5), rowIn.getTimestamp(6),
                                            rowIn.getLong(7), rowIn.getInt(8), rowIn.getInt(9), rowIn.getString(10), aclIn);
        }
        return myResult;
    }

   private static Map<String,AclBuilder> createAclMap(List<String> uuidListIn) {
      Map<String,AclBuilder> aclMap = new HashMap<String,AclBuilder>();

      if ((uuidListIn != null) && !uuidListIn.isEmpty()) {
         try (Connection connection = CsiPersistenceManager.getMetaConnection();
              Statement statement = connection.createStatement()) {
            String sql = new StringBuilder("SELECT a.\"uuid\", a.\"owner\", e.\"accesstype\", e.\"rolename\" FROM \"acl\" a, "
                  + "\"accesscontrolentry\" e, \"acl_accesscontrolentry\" l WHERE a.\"id\" = "
                  + "l.\"acl_id\" AND e.\"id\" = l.\"entries_id\" AND a.\"uuid\" in ('")
                                   .append(uuidListIn.stream().collect(Collectors.joining("', '", "", "')")))
                                   .toString();

            try (ResultSet resultSet = QueryHelper.executeStatement(statement, sql)) {
               while (resultSet.next()) {
                  String key = resultSet.getString(1);
                  AclBuilder builder = aclMap.get(key);

                  if (builder == null) {
                     aclMap.put(key, new AclBuilder(resultSet));
                  } else {
                     builder.addEntry(resultSet);
                  }
               }
            }
         } catch (Exception exception) {
         }
      }
      return aclMap;
   }
}
