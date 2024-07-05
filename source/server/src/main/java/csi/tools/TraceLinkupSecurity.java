package csi.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import csi.server.business.helper.QueryHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 6/4/2018.
 */
public class TraceLinkupSecurity implements CustomTaskChange {
    private static final Logger LOG = LogManager.getLogger(TraceLinkupSecurity.class);

    private Connection _metaConnection = null;
    private Map<String, Map<String, String>> _crossMap = null;
    private Map<String, Map<String, String>> _fieldMap = null;

   @Override
   public void execute(Database database) throws CustomChangeException {
      try {
         _metaConnection = CsiPersistenceManager.getMetaConnection();

         if (_metaConnection != null) {
            retrieveLinkupInfo();
         }
      } catch (Exception myException) {
         LOG.fatal("Caught fatal exception while migrating data!\n" + myException.getMessage());
      } finally {
         try {
            if (_metaConnection != null) {
               _metaConnection.close();
            }
         } catch (Exception IGNORE) {
         }
      }
   }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    private void retrieveLinkupInfo() {

        ResultSet myBaseResults = null;
        ResultSet myCapcoInfo = null;
        ResultSet myTagInfo = null;
        ResultSet myLinkupInfo = null;
        ResultSet myTemplateInfo = null;

        try {

//            String myQueryString = "SELECT capco, tags, linkups FROM linkupsecurity";
            String myQueryString = "SELECT m.capcoinfo_uuid, m.securitytagsinfo_uuid, d.linkups"
                                    + " FROM dataview d, dataviewdef t, modelresource m"
                                    + " WHERE (d.meta_uuid = t.uuid) AND (t.uuid = m.uuid)"
                                    + " AND (d.linkups IS NOT NULL)"
                                    + " AND ((m.capcoinfo_uuid IS NOT NULL)"
                                    + " OR (m.securitytagsinfo_uuid IS NOT NULL))";
            myBaseResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);

            if (null != myBaseResults) {

                _fieldMap = new TreeMap<String, Map<String, String>>();

                while (myBaseResults.next()) {

                    try {

                        String myCapcoId = myBaseResults.getString(1);
                        String myTagId = myBaseResults.getString(2);
                        String myLinkupString = myBaseResults.getString(3);
                        boolean myCapcoFlag = (null != myCapcoId);
                        boolean myTagFlag = (null != myTagId);

                        if ((myCapcoFlag || myTagFlag) && (null != myLinkupString) && (0 < myLinkupString.length())) {

                            String[] myLikupIdArray = myLinkupString.replace("_", "-").split("\\|");

                            _crossMap = new TreeMap<String, Map<String, String>>();
                            for (int i = 0; myLikupIdArray.length > i; i++) {

                                try {

                                    String myLinkupId = myLikupIdArray[i].substring(7, 43);
                                    myLinkupInfo = retrieveLinkupInfo(myLinkupId);
                                    String myTemplateId = myLinkupInfo.getString(1);
                                    myTemplateInfo = (null != myLinkupInfo)
                                                        ? retrieveTemplateInfo(myTemplateId,
                                                                                myLinkupInfo.getString(2),
                                                                                myLinkupInfo.getString(3))
                                                        : null;
                                    if (myCapcoFlag) {

                                        myCapcoId = updateCapcoInfo(myCapcoId, myTemplateId, myLinkupId, myTemplateInfo);
                                    }
                                    if (myTagFlag) {

                                        myTagId = updateTagInfo(myTagId, myTemplateId, myLinkupId, myTemplateInfo);
                                    }

                                } finally {

                                    myLinkupInfo = close(myLinkupInfo);
                                    myTemplateInfo = close(myTemplateInfo);
                                }
                            }
                        }

                    } catch (Exception myException) {

                        LOG.error("Caught an exception tracing security for linkups.\n" + myException.getMessage());
                    }
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught an exception tracing security for linkups.\n" + myException.getMessage());

        } finally {

            myBaseResults = close(myBaseResults);
            myCapcoInfo = close(myCapcoInfo);
            myTagInfo = close(myTagInfo);
        }
    }

    private ResultSet retrieveLinkupInfo(String idIn) {

        ResultSet myResults = null;

        if (null != idIn) {

            try {

                String myQueryString = "SELECT templateuuid, templatename, templateowner FROM linkupmapdef WHERE uuid = '"
                                        + idIn + "'";
                myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);

                if ((null != myResults) && (!myResults.next())) {

                    myResults = close(myResults);
                }

            } catch (Exception myException) {

                myResults = null;
                LOG.error("Caught exception retrieving linkup info.\n" + myException.getMessage());
            }
        }
        return myResults;
    }

    private ResultSet retrieveTemplateInfo(String idIn, String nameIn, String ownerIn) {

        ResultSet myResults = null;

        if (null != idIn) {

            try {

                String myQueryString = "SELECT capcoinfo_uuid, securitytagsinfo_uuid FROM modelresource WHERE uuid = '"
                        + idIn + "'";
                myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);

                if ((null != myResults) && (!myResults.next())) {

                    myResults = close(myResults);
                }

            } catch (Exception myException) {

                myResults = null;
                LOG.error("Caught exception retrieving template data using template Id.\n" + myException.getMessage());
            }
        }
        if ((null == myResults) && (null != nameIn) && (null != ownerIn)) {

            try {

                String myQueryString = "SELECT capcoinfo_uuid, securitytagsinfo_uuid FROM modelresource WHERE name = '"
                        + nameIn + "' AND owner = '" + ownerIn + "'";
                myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);

                if ((null != myResults) && (!myResults.next())) {

                    myResults = close(myResults);
                }

            } catch (Exception myException) {

                myResults = null;
                LOG.error("Caught exception retrieving template data using template name and owner.\n" + myException.getMessage());
            }
        }
        return myResults;
    }

    private String updateCapcoInfo(String capcoIdIn, String templateIdIn, String linkupIdIn, ResultSet templateInfoIn)
            throws CentrifugeException, SQLException {

        String myCapcoId = null;
        ResultSet myTemplateCapco = null;

        try {

            if ((null != templateInfoIn) && (null != capcoIdIn)) {

                myTemplateCapco = retrieveCapcoInfo(templateInfoIn.getString(1));

                if (null != myTemplateCapco) {

                    Integer myMode = myTemplateCapco.getInt(4);

                    if ((null != myMode) && (3 > myMode)) {

                        if (1 == myMode) {

                            myCapcoId = generateUserOnlyCapcoInfo(myTemplateCapco);

                        } else {

                            myCapcoId = generateCapcoInfoWithData(myTemplateCapco, templateIdIn, linkupIdIn);
                        }
                    }
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception generating Security Tag info.\n" + myException.getMessage());

        } finally{

            try {

                if (null == myCapcoId) {

                    myCapcoId = generateDefaultCapcoInfo();
                }
                if (null != myCapcoId) {

                    linkCapcoInfos(capcoIdIn, myCapcoId);
                }

            } catch (Exception myException) {

                LOG.error("Caught exception linking CAPCO info.\n" + myException.getMessage());

            }
            myTemplateCapco = close(myTemplateCapco);
        }
        return myCapcoId;
    }

    private String updateTagInfo(String tagIdIn, String templateIdIn, String linkupIdIn, ResultSet templateInfoIn)
            throws CentrifugeException, SQLException {

        String myTagId = null;
        ResultSet myTemplateTags = null;

        try {

            if ((null != tagIdIn) && (null != templateInfoIn)) {

                myTemplateTags = retrieveTagInfo(templateInfoIn.getString(2));

                if (null != myTemplateTags) {

                    Integer myMode = myTemplateTags.getInt(6);

                    if ((null != myMode) && (3 > myMode)) {

                        if (1 == myMode) {

                            myTagId = generateUserOnlyTagInfo(myTemplateTags);

                        } else {

                            myTagId = generateTagInfoWithData(myTemplateTags, templateIdIn, linkupIdIn);
                        }
                    }
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception generating Security Tag info.\n" + myException.getMessage());

        } finally{

            try {

/*
                if (null == myTagId) {

                    myTagId = generateDefaultCapcoInfo();
                }
*/
                if (null != myTagId) {

                    linkTagInfos(tagIdIn, myTagId);
                }

            } catch (Exception myException) {

                LOG.error("Caught exception linking Security Tag info.\n" + myException.getMessage());

            }
            myTemplateTags = close(myTemplateTags);
        }
        return myTagId;
    }

    private ResultSet retrieveCapcoInfo(String idIn) {

        ResultSet myResults = null;

        try {

            String myQueryString = "SELECT userportion, fieldstring, uuid, mode FROM capcoinfo WHERE uuid = '"
                                    + idIn + "'";

            myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);
            if ((null != myResults) && (!myResults.next())) {

                myResults = close(myResults);
            }

        } catch (Exception myException) {

            myResults = null;
            LOG.error("Caught exception retrieving CAPCO info from template.\n" + myException.getMessage());
        }
        return myResults;
    }

    private ResultSet retrieveTagInfo(String idIn) {

        ResultSet myResults = null;

        try {

            String myQueryString = "SELECT basetagstring, delimiterstring, columnstring, ignoredtagstring,"
                                    + " uuid, mode, ortags FROM securitytagsinfo WHERE uuid = '" + idIn + "'";

            myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);
            if ((null != myResults) && (!myResults.next())) {

                myResults = close(myResults);
            }

        } catch (Exception myException) {

            myResults = null;
            LOG.error("Caught exception retrieving Security Tag info from template.\n" + myException.getMessage());
        }
        return myResults;
    }

    private String generateUserOnlyCapcoInfo(ResultSet capcoInfoIn) {

        String myId = null;

        try {

            String myUserPortion = capcoInfoIn.getString(1);

            myId = generateCapcoInfo(myUserPortion, null, 1);

        } catch (Exception myException) {

            myId = null;
            LOG.error("Caught exception generating CAPCO info with static value.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateCapcoInfoWithData(ResultSet capcoInfoIn, String templateIdIn, String linkupIdIn) {

        String myId = null;
        try {

            String myUserPortion = capcoInfoIn.getString(1);
            String myOldFieldString = capcoInfoIn.getString(2);
            int myMode = capcoInfoIn.getInt(4);
            String myNewFieldString = (null != myOldFieldString)
                                                ? convertColumnNames(myOldFieldString, templateIdIn, linkupIdIn)
                                                : null;
            myId = generateCapcoInfo(myUserPortion, myNewFieldString, myMode);

        } catch (Exception myException) {

            myId = null;
            LOG.error("Caught exception generating Security Tag info with static value.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateUserOnlyTagInfo(ResultSet tagInfoIn) {

        String myId = null;

        try {

            String myBaseTags = tagInfoIn.getString(1);

            myId = generateTagInfo(myBaseTags, null, null, null, 1, true);

        } catch (Exception myException) {

            myId = null;
            LOG.error("Caught exception generating CAPCO info referencing data.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateTagInfoWithData(ResultSet tagInfoIn, String templateIdIn, String linkupIdIn) {

        String myId = null;
        try {

            String myBaseTags = tagInfoIn.getString(1);
            String myDelimiters = tagInfoIn.getString(2);
            String myOldColumnString = tagInfoIn.getString(3);
            String myNewColumnString = (null != myOldColumnString)
                                                ? convertColumnNames(myOldColumnString, templateIdIn, linkupIdIn)
                                                : null;
            String myIgnored = tagInfoIn.getString(4);
            Integer myMode = tagInfoIn.getInt(6);
            Boolean myOrTags = tagInfoIn.getBoolean(7);
            myId = generateTagInfo(myBaseTags, myDelimiters, myNewColumnString, myIgnored, myMode, myOrTags);

        } catch (Exception myException) {

            myId = null;
            LOG.error("Caught exception generating Security Tag info referencing data.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateDefaultCapcoInfo() throws CentrifugeException, SQLException {

        return generateCapcoInfo(null, null, 3);
    }

    private String generateDefaultTagInfo() throws CentrifugeException, SQLException {

        return generateTagInfo(null, null, null, null, 3, true);
    }

    private String generateCapcoInfo(String userPortionIn, String fieldStringIn, int modeIn) {

        String myId = null;
        String myUuid = null;

        try {

            myUuid = generateModelObject();
            if (null != myUuid) {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append("INSERT INTO capcoinfo (userportion, fieldstring, uuid, mode) VALUES (");
                if (null != userPortionIn) {

                    myBuffer.append("'");
                    myBuffer.append(userPortionIn);
                    myBuffer.append("',");

                } else {

                    myBuffer.append("null,");
                }
                if (null != fieldStringIn) {

                    myBuffer.append("'");
                    myBuffer.append(fieldStringIn);
                    myBuffer.append("','");

                } else {

                    myBuffer.append("null,'");
                }
                myBuffer.append(myUuid);
                myBuffer.append("',");
                myBuffer.append(Integer.toString(modeIn));
                myBuffer.append(')');

                QueryHelper.executeSQL(_metaConnection, myBuffer.toString(), null);
                _metaConnection.commit();
                myId = myUuid;
            }

        } catch (Exception myException) {

            try {
                _metaConnection.rollback();

            } catch (Exception IGNORE) {}
            LOG.error( "Caught an exception generating Security Tag info.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateTagInfo(String baseTagsIn, String delimitersIn, String columnsIn,
                                      String ignoredIn, int modeIn, Boolean orTagsIn) {

        String myId = null;
        String myUuid = null;

        try {

            myUuid = generateModelObject();
            if (null != myUuid) {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append("INSERT INTO securitytagsinfo (basetagstring, delimiterstring,"
                        + " columnstring, ignoredtagstring, uuid, mode, ortags) VALUES (");
                if (null != baseTagsIn) {

                    myBuffer.append("'");
                    myBuffer.append(baseTagsIn);
                    myBuffer.append("',");

                } else {

                    myBuffer.append("null,");
                }
                if (null != delimitersIn) {

                    myBuffer.append("'");
                    myBuffer.append(delimitersIn);
                    myBuffer.append("',");

                } else {

                    myBuffer.append("null,");
                }
                if (null != columnsIn) {

                    myBuffer.append("'");
                    myBuffer.append(columnsIn);
                    myBuffer.append("',");

                } else {

                    myBuffer.append("null,");
                }
                if (null != ignoredIn) {

                    myBuffer.append("'");
                    myBuffer.append(ignoredIn);
                    myBuffer.append("','");

                } else {

                    myBuffer.append("null,'");
                }
                myBuffer.append(myUuid);
                myBuffer.append("',");
                myBuffer.append(Integer.toString(modeIn));
                myBuffer.append(',');
                if (null != orTagsIn) {

                    myBuffer.append(orTagsIn.booleanValue() ? "true)" : "false)");

                } else {

                    myBuffer.append("null)");
                }

                QueryHelper.executeSQL(_metaConnection, myBuffer.toString(), null);
                _metaConnection.commit();
                myId = myUuid;
            }

        } catch (Exception myException) {

            try {
                _metaConnection.rollback();

            } catch (Exception IGNORE) {}
            LOG.error( "Caught an exception generating Security Tag info.\n" + myException.getMessage());
        }
        return myId;
    }

    private String generateModelObject() throws CentrifugeException, SQLException {

        String myUuid = CsiUUID.randomUUID();
        String myCommandString = "INSERT INTO modelobject (uuid) VALUES ( '" + myUuid + "')";

        QueryHelper.executeSQL(_metaConnection, myCommandString, null);
        _metaConnection.commit();
        return myUuid;
    }

    private void removeCapcoObject(String uuidIn) {

        try {

            String myCommandString = "DELETE FROM capcoinfo WHERE uuid = '" + uuidIn + "'";

            QueryHelper.executeSQL(_metaConnection, myCommandString, null);

        } catch (Exception IGNORE) {}
    }

    private void removeTagObject(String uuidIn) {

        try {

            String myCommandString = "DELETE FROM securitytagsinfo WHERE uuid = '" + uuidIn + "'";

            QueryHelper.executeSQL(_metaConnection, myCommandString, null);

        } catch (Exception IGNORE) {}
    }

    private void removeModelObject(String uuidIn) {

        try {

            String myCommandString = "DELETE FROM modelobject WHERE uuid = '" + uuidIn + "'";

            QueryHelper.executeSQL(_metaConnection, myCommandString, null);

        } catch (Exception IGNORE) {}
    }

   private String convertColumnNames(String nameListIn, String templateIdIn, String linkupIdIn) {
      StringJoiner columnNames = null;

      if ((nameListIn != null) && (nameListIn.length() > 35)) {
         columnNames = new StringJoiner("|");
         String[] nameList = nameListIn.split("\\|");

         if ((nameList != null) && (nameList.length > 0)) {
            Map<String, String> crossMap = getCrossMap(linkupIdIn);
            Map<String, String> fieldMap = getFieldMap(templateIdIn);

            for (String name : nameList) {
               if (name != null) {
                  String key = name.replace('_', '-');
                  String sourceLocalId = fieldMap.get(key);

                  if (sourceLocalId != null) {
                     String targetLocalId = crossMap.get(sourceLocalId);

                     if (targetLocalId != null) {
                        columnNames.add(targetLocalId.replace('-', '_'));
                     }
                  }
               }
            }
         }
      }
      return (columnNames == null) ? null : columnNames.toString();
   }

    // Returns map to LocalId of DataView FieldDef by LocalId of Template FieldDef.
    private Map<String, String> getCrossMap(String linkupIdIn) {

        Map<String, String> myMap = _crossMap.get(linkupIdIn);

        if (null == myMap) {

            ResultSet myMappingList = null;

            myMap = new TreeMap<String,String>();
            try {

                String my1stQueryString = "SELECT fieldsmap_uuid FROM linkupmapdef_loosemapping WHERE linkupmapdef_uuid = '"
                        + linkupIdIn + "'";
                myMappingList = QueryHelper.executeSingleQuery(_metaConnection, my1stQueryString, null);

                if (null != myMappingList) {

                    while (myMappingList.next()) {

                        ResultSet myPair = null;

                        try {

                            String myMappingId = myMappingList.getString(1);

                            if (null != myMappingId) {

                                String my2ndQueryString = "SELECT _mappedlocalid, _mappinglocalid FROM loosemapping WHERE uuid = '"
                                        + myMappingId + "'";

                                myPair = QueryHelper.executeSingleQuery(_metaConnection, my2ndQueryString, null);
                                if ((null != myPair) && myPair.next()) {

                                    String myMappedId = myPair.getString(1);
                                    String myMappingIn = myPair.getString(2);

                                    if ((null != myMappingIn) && (null != myMappedId)) {

                                        myMap.put(myMappingIn, myMappedId);
                                    }
                                }
                            }

                        } catch (Exception myException) {

                            LOG.error("Caught exception building linkup field-to-field map.\n" + myException.getMessage());

                        } finally {

                            myPair = close(myPair);
                        }
                    }
                }

            } catch(Exception myException){

                LOG.error("Caught exception retrieving linkup field-to-field map.\n" + myException.getMessage());

            } finally {

                if (!myMap.isEmpty()) {

                    _crossMap.put(linkupIdIn, myMap);

                } else {

                    myMap = null;
                }
                myMappingList = close(myMappingList);
            }
        }
        return myMap;
    }

    // Returns map to FieldDef LocalId by either FieldDef LocalID or ColumnDef LocalId.
    private Map<String, String> getFieldMap(String templateIdIn) {

        Map<String, String> myMap = _fieldMap.get(templateIdIn);

        if (null == myMap) {

            ResultSet myFieldList = null;

            try {

                myMap = new TreeMap<String, String>();
                myFieldList = getFieldList(templateIdIn);
                if (null != myFieldList) {

                    while (myFieldList.next()) {

                        String myLocalId = myFieldList.getString(1);

                        if (null != myLocalId) {

                            String myColumnLocalId = myFieldList.getString(2);

                            myMap.put(myLocalId, myLocalId);
                            if (null != myColumnLocalId) {

                                myMap.put(myColumnLocalId, myLocalId);
                            }
                        }
                    }
                }

            } catch (Exception myException) {

                LOG.error("Caught exception building field map.\n" + myException.getMessage());

            } finally {

                if ((myMap != null) && !myMap.isEmpty()) {

                    _fieldMap.put(templateIdIn, myMap);

                } else {

                    myMap = null;
                }
                myFieldList = close(myFieldList);
            }
        }
        return  myMap;
    }

    private ResultSet getFieldList(String templateIdIn) {

        ResultSet myResults = null;

        try {
            String myModelId = getModelId(templateIdIn);

            if (null != myModelId) {

                String myQueryString = "SELECT localid, columnlocalid FROM fielddef WHERE parent_uuid = '" + myModelId + "'";

                myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);
                if ((null != myResults) && (!myResults.next())) {

                    myResults = close(myResults);
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception linking Field information.\n" + myException.getMessage());

        }
        return myResults;
    }

    private String getModelId(String templateIdIn) {

        String myModelId = null;
        ResultSet myResults = null;

        try {

            String myQueryString = "SELECT modeldef_uuid FROM dataviewdef WHERE uuid = '" + templateIdIn + "'";

            myResults = QueryHelper.executeSingleQuery(_metaConnection, myQueryString, null);
            if ((null != myResults) && (myResults.next())) {

                myModelId = myResults.getString(1);
            }

        } catch (Exception myException) {

            LOG.error("Caught exception getting Data Model Id.\n" + myException.getMessage());

        } finally {

            myResults = close(myResults);
        }
        return myModelId;
    }

    private ResultSet close(ResultSet resultSetIn) {

        if (null != resultSetIn) {

            try {

                resultSetIn.close();

            } catch (Exception IGNORE) {}
        }
        return null;
    }

    private void linkCapcoInfos(String oldCapcoIdIn, String newCapcoIdIn) {

        try {

            String myCommandString = "UPDATE capcoinfo SET next_uuid = '" + newCapcoIdIn
                    + "' WHERE uuid = '" + oldCapcoIdIn + "'";

            QueryHelper.executeSQL(_metaConnection, myCommandString, null);
            _metaConnection.commit();

        } catch (Exception myException) {

            try {
                _metaConnection.rollback();

            } catch (Exception IGNORE) {}
            LOG.error("Caught exception linking CAPCO information.\n" + myException.getMessage());
        }
    }

    private void linkTagInfos(String oldTagIdIn, String newTagIdIn) {

        try {

            String myCommandString = "UPDATE securitytagsinfo SET next_uuid = '" + newTagIdIn
                    + "' WHERE uuid = '" + oldTagIdIn + "'";

            QueryHelper.executeSQL(_metaConnection, myCommandString, null);
            _metaConnection.commit();

        } catch (Exception myException) {

            try {
                _metaConnection.rollback();

            } catch (Exception IGNORE) {}
            LOG.error("Caught exception linking Security Tag information.\n" + myException.getMessage());
        }
    }
}
