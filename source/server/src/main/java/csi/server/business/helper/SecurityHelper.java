package csi.server.business.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.AclTagRepository;
import csi.security.SecurityMask;
import csi.security.monitors.CapcoRollup;
import csi.security.queries.AclRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.exception.FailOverSecurityException;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.interfaces.DataWrapper;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheTokens;
import csi.server.util.sql.SqlTokens;

/**
 * Created by centrifuge on 10/4/2018.
 */
public class SecurityHelper {
   private static final Logger LOG = LogManager.getLogger(SecurityHelper.class);

    private static String _capcoDefault = null;
    private static String _capcoFailOver = null;
    private static String _tagFailOver = null;
    private static boolean _failOverInitialized = false;
    private static boolean _isInitialized = false;
    private static boolean _doCapcoBanners = false;
    private static boolean _doTagBanners = false;
    private static boolean _enforceCapco = false;
    private static boolean _enforceTags = false;

    public static boolean initialized() {

        if (!_isInitialized) {
            SecurityPolicyConfig mySecurityConfig = Configuration.getInstance().getSecurityPolicyConfig();

            _capcoDefault = mySecurityConfig.getDefaultPortion();
            _doCapcoBanners = mySecurityConfig.getEnableCapcoLabelProcessing();
            _doTagBanners = mySecurityConfig.getEnableTagLabelProcessing();
            _enforceCapco = mySecurityConfig.getEnforceCapcoRestrictions();
            _enforceTags = mySecurityConfig.getEnforceDataSecurityTags();

            _isInitialized = true;
        }
        return true;
    }

   public static String getCapcoDefault() {
      initialized();
      return  _capcoDefault;
   }

   public static boolean doCapcoBanners() {
      initialized();
      return _doCapcoBanners;
   }

   public static boolean doTagBanners() {
      initialized();
      return _doTagBanners;
   }

   public static boolean enforceCapco() {
      initialized();
      return _enforceCapco;
   }

   public static boolean enforceTags() {
      initialized();
      return _enforceTags;
   }

   public static boolean doCapco() {
      return doCapcoBanners() || enforceCapco();
   }

   public static boolean doTags() {
      return doTagBanners() || enforceTags();
   }

   public static boolean doSecurity() {
      return doCapco() || doTags();
   }

    public static void discardLinkupSecurity(DataView dataViewIn) {

        SecurityAccess mySecurityAccess = dataViewIn.getSecurityAccess();
        CapcoInfo myCapco = (null != mySecurityAccess) ? mySecurityAccess.getCapcoInfo() : null;
        SecurityTagsInfo myTags = (null != mySecurityAccess) ? mySecurityAccess.getSecurityTagsInfo() : null;

        if (null != myCapco) {

            myCapco.removeAllLinkupInfo();
        }
        if (null != myTags) {

            myTags.removeAllLinkupInfo();
        }
    }

    public static void resetSecurityInfo(Resource resourceIn) throws CsiSecurityException {

        CapcoInfo myCapcoInfo = resetSecurityData(resourceIn);
        Set<String> mySourceAccessList = resourceIn.getDataSourceKeySet();
        SecurityTagsInfo myTagInfo = resetSecurityTags(resourceIn);
        establishResourceSecurity(resourceIn, mySourceAccessList, myTagInfo,
                (null != myCapcoInfo) ? new CapcoRollup(myCapcoInfo) : null);
    }

    public static void cloneSecurity(Resource targetResource, String sourceUuidIn) throws CsiSecurityException {

        if ((null != targetResource) && (null != sourceUuidIn)) {

            if (targetResource instanceof DataView) {

                AclRequest.migrateSecurity(targetResource, sourceUuidIn, targetResource.getUuid());

            } else {

                resetSecurityInfo(targetResource);
            }
        }
    }

   public static void migrateSecurityInfo(DataView targetIn, InstalledTable sourceIn) throws CsiSecurityException {
      DataViewDef myTarget = (targetIn == null) ? null : targetIn.getMeta();
      DataSetOp myTargetTree = (myTarget == null) ? null : myTarget.getDataTree();
      SqlTableDef myColumnAccess = (myTargetTree == null) ? null : myTargetTree.getTableDef();

      if ((myTarget != null) && (sourceIn != null)) {
         CapcoInfo mySourceCapco = sourceIn.getCapcoInfo();
         SecurityTagsInfo mySourceTags = sourceIn.getSecurityTagsInfo();

         if (mySourceCapco != null) {
            CapcoInfo myTargetCapco = mySourceCapco.clone();

            if (myColumnAccess != null) {
               List<String> mySourceKeys = mySourceCapco.getSecurityFields();

               if (mySourceKeys != null) {
                  Set<String> myTargetKeys = new TreeSet<String>();

                  convertSecurityFieldList(myColumnAccess, mySourceKeys, myTargetKeys);
                  myTargetCapco.setSecurityFields(myTargetKeys);
               }
            }
            myTarget.setCapcoInfo(myTargetCapco);
         }
         if (mySourceTags != null) {
            SecurityTagsInfo myTargetTags = mySourceTags.clone();

            if (myColumnAccess != null) {
               List<String> mySourceKeys = mySourceTags.getColumnList();

               if (mySourceKeys != null) {
                  Set<String> myTargetKeys = new TreeSet<String>();

                  convertSecurityFieldList(myColumnAccess, mySourceKeys, myTargetKeys);
                  myTargetTags.setColumns(myTargetKeys);
               }
            }
            myTarget.setSecurityTagsInfo(myTargetTags);
         }
         cloneSecurity(targetIn, sourceIn.getUuid());
      }
   }

    public static void convertSecurityFieldList(FieldListAccess accessIn,
                                                Set<String> sourceKeysIn, Collection<String> targetKeysIn) {

        if (sourceKeysIn != null) {

            for (String myKey : sourceKeysIn) {

                FieldDef myField = accessIn.getFieldDefByAnyKey(myKey);

                if (null != myField) {

                    targetKeysIn.add(myField.getColumnLocalId());
                }
            }
        }
    }

    public static void convertSecurityFieldList(SqlTableDef accessIn,
                                                List<String> sourceKeysIn, Collection<String> targetKeysIn) {

        if (sourceKeysIn != null) {

            for (String myKey : sourceKeysIn) {

                ColumnDef myColumn = accessIn.getColumnByReferenceId(myKey);

                if (null != myColumn) {

                    targetKeysIn.add(myColumn.getColumnLocalId());
                }
            }
        }
    }

    // TODO: Fixup Security for SPIN-OFF

   public static boolean applyInstalledTableSecurity(InstalledTable tableIn) throws CentrifugeException {
      boolean result = false;

      if ((tableIn.getSecurityTagsInfo() != null) || (tableIn.getCapcoInfo() != null)) {
//            CapcoInfo myCapco = tableIn.getCapcoInfo();
//            SecurityTagsInfo myTagInfo = tableIn.getSecurityTagsInfo();
         CsiPersistenceManager.commit();
         CsiPersistenceManager.begin();

         try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
            try {
               updateSecurityData(myConnection, tableIn, tableIn.getActiveTableName());
               result = true;
            } catch (Exception myException) {
               myConnection.rollback();

               throw new CentrifugeException("Failed applying security to table created from upload:\n"
                                             + CacheUtil.replaceNames(Format.value(myException),
                                             CacheUtil.getTableNamePair(tableIn),
                                             CacheUtil.getColumnNameMap(tableIn.getColumns())));
               }
            } catch (Exception e) {
               throw new CentrifugeException("Failed applying security to table created from upload:\n"
                                             + CacheUtil.replaceNames(Format.value(e),
                                             CacheUtil.getTableNamePair(tableIn),
                                             CacheUtil.getColumnNameMap(tableIn.getColumns())));
            }
        }
        return result;
    }

    public static void recordLinkupSecurity(SecurityAccess targetIn, SecurityAccess sourceIn,
                                             List<String> capcoColumnNamesIn, List<String> tagColumnNamesIn) {

        if ((null != targetIn) && (null != sourceIn)) {

            CapcoInfo myTargetCapco = (null != targetIn) ? targetIn.getCapcoInfo() : null;
            SecurityTagsInfo myTargetTags = (null != targetIn) ? targetIn.getSecurityTagsInfo() : null;
            CapcoInfo mySourceCapco = (null != sourceIn) ? sourceIn.getCapcoInfo() : null;
            SecurityTagsInfo mySourceTags = (null != sourceIn) ? sourceIn.getSecurityTagsInfo() : null;

            if (null != myTargetCapco) {

                if (null != mySourceCapco) {

                    myTargetCapco.addLinkupInfo(mySourceCapco.genForLinkup(capcoColumnNamesIn));

                } else {

                    myTargetCapco.addLinkupInfo(new CapcoInfo());
                }
            }
            if (null != myTargetTags) {

                if (null != mySourceTags) {

                    myTargetTags.addLinkupInfo(mySourceTags.genForLinkup(tagColumnNamesIn));

                } else {

                    myTargetTags.addLinkupInfo(new SecurityTagsInfo());
                }
            }
        }
    }

    public static Boolean updateSecurity(Resource resourceIn) {
        try {

            if (resourceIn instanceof DataView) {

                return updateSecurityData(CsiPersistenceManager.getCacheConnection(), (DataView)resourceIn,
                        CacheUtil.getCacheTableName(resourceIn.getUuid()));

            } else if (resourceIn instanceof InstalledTable) {

                return applyInstalledTableSecurity((InstalledTable)resourceIn);

            } else if (resourceIn instanceof DataViewDef) {

                return true;
            }

        } catch (Exception myException) {

            try {

                invokeFallBackSecurity(resourceIn);
            } catch (Exception IGNORE) {}
            LOG.error("Caught exception applying security to resource.\nApplying fall-back security.\n",
                    myException);
        }
        return false;
    }

    private static CapcoRollup updateCapcoData(Connection connectionIn, CapcoInfo sourceInfoIn, CapcoInfo targetInfoIn,
                                               String tableNameIn, List<String> capcoColumnsIn,
                                               boolean clearMarkingTableIn, Integer originIn) {

        CapcoRollup myCapcoProcessing = null;

        if (Configuration.getInstance().getSecurityPolicyConfig().getEnforceAccessRestrictions().booleanValue()
                || Configuration.getInstance().getSecurityPolicyConfig().getEnableCapcoLabelProcessing().booleanValue()) {

            if (null != sourceInfoIn) {

                boolean myDataFlag = ((CapcoSource.DATA_ONLY == sourceInfoIn.getMode())
                        || (CapcoSource.USER_AND_DATA == sourceInfoIn.getMode()));
                String myPortion = ((CapcoSource.USER_ONLY == sourceInfoIn.getMode())
                                    || (CapcoSource.USER_AND_DATA == sourceInfoIn.getMode()))
                                        ? sourceInfoIn.getUserPortion() : null;

                locateMarkingTable(connectionIn, tableNameIn, clearMarkingTableIn);

                boolean myForceSciFlag = Configuration.getInstance().getSecurityPolicyConfig().getForceUnrecognizedSciProcessing().booleanValue();
                myCapcoProcessing = new CapcoRollup(sourceInfoIn, myForceSciFlag);
                String oldRollup = (null != targetInfoIn) ? targetInfoIn.getPortion() : null;

                if (null != tableNameIn) {

                    if (myDataFlag && (null != capcoColumnsIn) && !capcoColumnsIn.isEmpty()) {

                        if (1 == capcoColumnsIn.size()) {

                            String myColumnName = capcoColumnsIn.get(0);

                            String myCapcoQuery = " SELECT DISTINCT "
                                    + CacheUtil.quote(myColumnName)
                                    + " FROM " + CacheUtil.quote(tableNameIn);

                            if (null != originIn) {

                                myCapcoQuery = myCapcoQuery + " WHERE \"" + CacheTokens.CSI_ORIGIN + "\" = " + originIn.toString();
                            }
                            myCapcoProcessing.captureCapcoColumn(connectionIn, myCapcoQuery, myPortion);

                        } else {

                            StringBuilder myBuffer = new StringBuilder();
                            boolean first = true;

                            myBuffer.append(" SELECT DISTINCT ");

                            for (String myColumnName : capcoColumnsIn) {
                               if (first) {
                                  first = false;
                               } else {
                                  myBuffer.append(", ");
                               }
                               myBuffer.append(CacheUtil.quote(myColumnName));
                            }
                            myBuffer.append(" FROM ");
                            myBuffer.append(CacheUtil.quote(tableNameIn));
                            if (null != originIn) {

                                myBuffer.append(" WHERE \"");
                                myBuffer.append(CacheTokens.CSI_ORIGIN);
                                myBuffer.append("\" = ");
                                myBuffer.append(originIn.toString());
                            }
                            myCapcoProcessing.captureMultipleCapcoColumns(connectionIn, myBuffer.toString(), myPortion);
                        }

                    } else {

                        myCapcoProcessing.captureCapcoColumn(connectionIn, null, myPortion);
                    }
                }
                myCapcoProcessing.createRollup(oldRollup);
                myCapcoProcessing.finalizeRollup((null != oldRollup) && (0 < oldRollup.length()));
                if (null != targetInfoIn) {

                    targetInfoIn.setPortion(sourceInfoIn.getPortion());
                    targetInfoIn.setAbbreviation(sourceInfoIn.getAbbreviation());
                    targetInfoIn.setBanner(sourceInfoIn.getBanner());
                }
            }
        }
        return myCapcoProcessing;
    }

    private static String locateMarkingTable(Connection connectionIn, String tableNameIn, boolean clearTableIn) {

        return locateSecurityTable(connectionIn, CacheUtil.extractMarkingTableName(tableNameIn), clearTableIn);
    }

    private static String locateTagTable(Connection connectionIn, String tableNameIn, boolean clearTableIn) {

        return locateSecurityTable(connectionIn, CacheUtil.extractTagTableName(tableNameIn), clearTableIn);
    }

    private static String locateSecurityTable(Connection connectionIn, String tableNameIn, boolean clearTableIn) {

        if (null != tableNameIn) {

            try {

                if (DataCacheHelper.actualTableExists(connectionIn, tableNameIn)) {

                    if (clearTableIn) {

                        DataCacheHelper.truncateTable(connectionIn, tableNameIn);
                        connectionIn.commit();
                    }

                } else {

                    createSecurityTable(tableNameIn);
                    connectionIn.commit();
                }
                return tableNameIn;

            } catch (Exception myException) {

                SqlUtil.quietRollback(connectionIn);
                LOG.error("Caught exception while attempting to access table " + Format.value(tableNameIn));
            }
        }
        return null;
    }

    private static void updateSecurityTags(Connection connectionIn, SecurityTagsInfo tagInfoIn,
                                           String tableNameIn, List<String> tagColumnsIn, Integer originIn)
            throws CsiSecurityException {

        if ((null != tagInfoIn) && Configuration.getInstance().getSecurityPolicyConfig().getEnforceDataSecurityTags().booleanValue()) {

            CapcoSource myMode = tagInfoIn.getMode();

            if ((null != tableNameIn) || (CapcoSource.USER_ONLY == myMode) || (CapcoSource.USE_DEFAULT == myMode)) {

                if (tagInfoIn.getOrTags()) {

                    updateDistributionTags(connectionIn, tagInfoIn, tableNameIn, tagColumnsIn, originIn);

                } else {

                    updateRestrictiveTags(connectionIn, tagInfoIn, tableNameIn, tagColumnsIn, originIn);
                }
            }
        }
    }

    private static void updateRestrictiveTags(Connection connectionIn, SecurityTagsInfo tagInfoIn,
                                              String tableNameIn, List<String> tagColumnsIn, Integer originIn)
            throws CsiSecurityException {

        if ((null != tableNameIn) && (null != tagInfoIn)) {

            if (tagInfoIn.doDataScan()) {

                if ((null != tagColumnsIn) && !tagColumnsIn.isEmpty()) {

                    ResultSet myResults = null;
                    StringBuilder myBuffer = new StringBuilder();
                    boolean first = true;

                    for (String myColumn : tagColumnsIn) {

                        if (null != myColumn) {
                           if (first) {
                              first = false;
                           } else {
                              myBuffer.append(" UNION ");
                           }
                            myBuffer.append(" SELECT DISTINCT ");
                            myBuffer.append(CacheUtil.quote(myColumn));
                            myBuffer.append(" FROM ");
                            myBuffer.append(CacheUtil.quote(tableNameIn));
                            if (null != originIn) {

                                myBuffer.append(" WHERE \"" + CacheTokens.CSI_ORIGIN + "\" = ");
                                myBuffer.append(originIn.toString());
                            }
                        }
                    }
                    if (0 < myBuffer.length()) {

                        try {

                            myResults = QueryHelper.executeSingleQuery(connectionIn, myBuffer.toString(), null);

                            if (null != myResults) {

                                TreeSet<String> myResultSet = new TreeSet<String>();

                                while (SqlUtil.hasMoreRows(myResults)) {

                                    String myString = myResults.getString(1);

                                    if (null != myString) {

                                        myString = myString.trim().toLowerCase();

                                        if (0 < myString.length()) {

                                            myResultSet.add(myString);
                                        }
                                    }
                                }
                                tagInfoIn.initializeTags(myResultSet, false);

                            } else {

                                tagInfoIn.setDefaultTags();
                            }

                        } catch (Exception myException) {

                            throw new CsiSecurityException("Caught exception while extracting Restrictive Tags:\n" + Format.value(myException));

                        } finally {

                            SqlUtil.quietCloseResulSet(myResults);
                        }
                    }
                }
            }
        }
    }

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    // TODO: SEE HOW THIS FITS WITH CASTING

    private static void updateDistributionTags(Connection connectionIn, SecurityTagsInfo tagInfoIn,
                                               String tableNameIn, List<String> tagColumnsIn, Integer originIn)
            throws CsiSecurityException {

        if ((null != tableNameIn) && (null != tagInfoIn)) {

            StringBuilder myBuffer = new StringBuilder();

            tagInfoIn.setIdentifiedTags(null);
            tagInfoIn.setRestrictionBits(null);

            if ((null != tagColumnsIn) && !tagColumnsIn.isEmpty()) {
               boolean first = true;

                for (String myColumn : tagColumnsIn) {

                    if (null != myColumn) {
                       if (first) {
                          first = false;
                       } else {
                          myBuffer.append(" UNION ");
                       }

                        myBuffer.append(" SELECT DISTINCT ");
                        myBuffer.append(CacheUtil.quote(myColumn));
                        myBuffer.append(" FROM ");
                        myBuffer.append(CacheUtil.quote(tableNameIn));
                        if (null != originIn) {

                            myBuffer.append(" WHERE \"" + CacheTokens.CSI_ORIGIN + "\" = ");
                            myBuffer.append(originIn.toString());
                        }
                    }
                }
                if (0 < myBuffer.length()) {

                    try (ResultSet myResults = QueryHelper.executeSingleQuery(connectionIn, myBuffer.toString(), null)) {

                        if (null != myResults) {

                            TreeSet<String> myResultSet = new TreeSet<String>();

                            while (SqlUtil.hasMoreRows(myResults)) {

                                String myString = myResults.getString(1);

                                if (null != myString) {

                                    myString = myString.trim().toLowerCase();

                                    if (0 < myString.length()) {

                                        myResultSet.add(myString);
                                    }
                                }
                            }
                            tagInfoIn.initializeTags(myResultSet, true);
                        }

                    } catch(Exception myException) {
                       throw new CsiSecurityException("Caught exception while extracting Distribution Tags:\n" + Format.value(myException));
                    }
                }
            }
        }
    }

    private static void augmentResourceSecurity(Resource targetIn, Set<String> sourceAccessListIn,
                                                SecurityTagsInfo tagInfoIn, CapcoRollup capcoProcessingIn)
            throws CsiSecurityException {

        SecurityMask mySecurityMode = SecurityMask.getTotalSecurityMask();
        Resource mySecurityObject = (targetIn instanceof DataView) ? ((DataView)targetIn).getMeta() : targetIn;
        Set<String> myCapcoAccessList = null;
        List<Set<String>> myDistributionList = null;

        if ((capcoProcessingIn != null) && mySecurityMode.hasCapcoRestrictions()) {
           myCapcoAccessList = capcoProcessingIn.getPositiveAclList();
        }
        if ((tagInfoIn != null) && mySecurityMode.hasGenericRestrictions()) {
           if (mySecurityObject.getSecurityTagsInfo() == null) {
              mySecurityObject.setSecurityTagsInfo(new SecurityTagsInfo());
           }
           myDistributionList = mySecurityObject.getSecurityTagsInfo().augmentTags(tagInfoIn.getRestrictionTree());
        }
        if (mySecurityMode.hasSecurity()) {

            AclTagRepository.augmentResourceSecurity(targetIn, targetIn.getUuid(), myCapcoAccessList,
                    myDistributionList, sourceAccessListIn);
        }
    }
    /*
        private static void updateSecurityData(Connection connectionIn, Resource resourceIn)
                throws CentrifugeException, CsiSecurityException {

            InstalledTable myTable = (InstalledTable)resourceIn;

            try {

                if (resourceIn instanceof InstalledTable) {

                    String myTableName = myTable.getActiveTableName();
                    SecurityTagsInfo myTagInfo = resourceIn.getSecurityTagsInfo();
                    CapcoInfo mySourceCapco = resourceIn.getCapcoInfo();
                    List<String> mySecurityFields = getInstalledSecurityNames((InstalledTable)resourceIn);
                    List<String> myCapcoFields = (null != mySourceCapco) ? mySourceCapco.getSecurityFields() : null;
                    Set<String> mySourceAccessList = resourceIn.getDataSourceKeySet();
                    CapcoRollup myRollUp = updateCapcoData(connectionIn, myPortionMap, mySourceCapco,
                                                            myTableName, CacheUtil.toDbUuid(myCapcoFields));

                    updateSecurityTags(connectionIn, myTagInfo, myTableName, mySecurityFields);
                    establishResourceSecurity(resourceIn, mySourceAccessList, myTagInfo, myRollUp);

                } else {

                    resetSecurityInfo(resourceIn);
                }

            } catch (Exception myException) {

                LOG.error("Caught exception applying security to installed table, "
                                + Format.value(myTable.getTablePath()) + "\nApplying fail-over security.\n",
                        myException);

                invokeFallBackSecurity(connectionIn, resourceIn);
                throw new FailOverSecurityException();
            }
        }
    */
    public static void applyCapturedTableSecurity(InstalledTable tableIn) throws CentrifugeException {

        Connection myConnection = CsiPersistenceManager.getCacheConnection();

        finalizeSpinoffSecurityData(myConnection, tableIn, tableIn.getActiveTableName());
    }

    public static void finalizeSpinoffSecurityData(Connection connectionIn, DataWrapper resourceIn, String tableNameIn)
            throws CentrifugeException {

        SecurityAccess mySecurityAccess = resourceIn.getSecurityAccess();
        Set<String> mySourceAccessList = resourceIn.getDataSourceKeySet();  // TODO: create a full set from linkups
        CapcoInfo myCapco = (null != mySecurityAccess) ? mySecurityAccess.getCapcoInfo() : null;
        SecurityTagsInfo myTags = (null != mySecurityAccess) ? mySecurityAccess.getSecurityTagsInfo() : null;
        CapcoRollup myRollUp = null;

        resourceIn.lockDataKeys();
        if (null != myCapco) {

            int myIndex = 0;

            myCapco.reset(true);
            myRollUp = updateCapcoData(connectionIn, myCapco, null, tableNameIn,
                                        resourceIn.getCapcoColumnNames(), true, myIndex++);

            if (null != myCapco.getNext()) {

                for (CapcoInfo myNewCapco = myCapco.getNext(); null != myNewCapco; myNewCapco = myNewCapco.getNext()) {

                    myRollUp = updateCapcoData(connectionIn, myNewCapco, myCapco, tableNameIn,
                                                CacheUtil.toDbUuid(myNewCapco.getSecurityFields()), false, myIndex++);
                }
                myCapco.lockResults();
            }
        }
        if (null != myTags) {

            SecurityTagsInfo myBackup = myTags.clone();
            try {

                int myIndex = 0;

                myTags.reset();
                updateSecurityTags(connectionIn, myTags, tableNameIn, resourceIn.getTagColumnNames(), myIndex++);

                if (null != myTags.getNext()) {

                    for (SecurityTagsInfo myNewTags = myTags.getNext();
                         null != myNewTags; myNewTags = myNewTags.getNext()) {

                        updateSecurityTags(connectionIn, myNewTags, tableNameIn,
                                CacheUtil.toDbUuid(myNewTags.getColumnList()), myIndex++);
                    }
                    myTags.lockResults();
                }

            } catch (Exception myException) {

                mySecurityAccess.setSecurityTagsInfo(myBackup);
            }
        }
        establishResourceSecurity(resourceIn.getResource(), mySourceAccessList, myTags, myRollUp);
    }

    public static Boolean updateSecurityData(Connection connectionIn, DataWrapper sourceIn, String tableNameIn)
            throws CentrifugeException {

        if (null != sourceIn) {

            return updateSecurityData(connectionIn, sourceIn, tableNameIn, null,
                    sourceIn.getCapcoColumnNames(), sourceIn.getTagColumnNames());
        }
        return false;
    }

    public static Boolean updateSecurityData(Connection connectionIn, DataWrapper sourceIn, String tableNameIn,
                                              DataWrapper targetIn, List<String> capcoColumnsIn, List<String> tagColumnsIn)
            throws CentrifugeException {

        try {

            boolean myClearFlag = (null == targetIn);
            Resource mySecureResource = sourceIn.getResource();
            SecurityAccess mySourceSecurity = sourceIn.getSecurityAccess();
            SecurityTagsInfo mySourceTagInfo = (null != mySourceSecurity) ? mySourceSecurity.getSecurityTagsInfo() : null;
            CapcoInfo mySourceCapco = resetSecurityData(mySecureResource);
            DataDefinition mySourceMeta = sourceIn.getDataDefinition();
            Set<String> mySourceAccessList = (null != mySourceMeta) ? mySourceMeta.getDataSourceKeySet() : null;
            SecurityAccess myTargetSecurity = (null != targetIn) ? targetIn.getSecurityAccess() : null;
            CapcoInfo myTargetCapco = (null != myTargetSecurity) ? myTargetSecurity.getCapcoInfo() : null;
            CapcoRollup myRollUp = updateCapcoData(connectionIn, mySourceCapco, myTargetCapco,
                                                    tableNameIn, capcoColumnsIn, myClearFlag, null);

            updateSecurityTags(connectionIn, mySourceTagInfo, tableNameIn, tagColumnsIn, null);

            if (null != targetIn) {

                augmentResourceSecurity(targetIn.getResource(), mySourceAccessList, mySourceTagInfo, myRollUp);

            } else {

                establishResourceSecurity(sourceIn.getResource(), mySourceAccessList, mySourceTagInfo, myRollUp);
            }
            return true;

        } catch (Exception myException) {

            LOG.error("Caught exception applying security to resource, "
                            + Format.value(sourceIn.getName()) + "\nApplying fail-over security.\n",
                    myException);

            invokeFallBackSecurity(connectionIn, sourceIn.getResource());
            throw new FailOverSecurityException();
        }
    }

    private static List<String> getInstalledSecurityNames(InstalledTable tableIn) {

        List<String> myTagColumns = new ArrayList<String>();
        SecurityTagsInfo myTagInfo = tableIn.getSecurityTagsInfo();
        List<String> myLocalIds = (null != myTagInfo) ? myTagInfo.getColumnList() : null;

        if ((null != myLocalIds) && !myLocalIds.isEmpty()) {

            for (String myLocalId : myLocalIds) {

                myTagColumns.add(tableIn.getColumnNameByLocalId(myLocalId));
            }
        }
        return myTagColumns;
    }

    public static void invokeFallBackSecurity(Resource resourceIn) throws CsiSecurityException, CentrifugeException {

        invokeFallBackSecurity(CsiPersistenceManager.getCacheConnection(), resourceIn);
    }

   public static void invokeFallBackSecurity(Connection connectionIn, Resource resourceIn) throws CsiSecurityException {
      Resource mySecurityBase = (resourceIn instanceof DataView) ? ((DataView)resourceIn).getMeta() : resourceIn;
      CapcoInfo myCapcoRequest = mySecurityBase.getCapcoInfo();
      SecurityTagsInfo myTagRequest = mySecurityBase.getSecurityTagsInfo();
      CapcoInfo myCapcoInfo = CapcoInfo.genFallBack(getCapcoFailOver());
      SecurityTagsInfo myTagInfo = SecurityTagsInfo.genFallBack(getSecurityTagsFailOver());
      String myMarkingTable = CacheUtil.getMarkingTableName(resourceIn);

      mySecurityBase.setCapcoInfo(myCapcoInfo);
      mySecurityBase.setSecurityTagsInfo(myTagInfo);

      myCapcoInfo = resetSecurityData(resourceIn);
      CapcoRollup myRollUp = updateCapcoData(connectionIn, myCapcoInfo, null, myMarkingTable, null, true, null);

      updateSecurityTags(connectionIn, myTagInfo, null, null, null);
      establishResourceSecurity(resourceIn, null, myTagInfo, myRollUp);

      if (myCapcoRequest == null) {
         if (myCapcoInfo != null) {
            myCapcoInfo.setMode(CapcoSource.USE_DEFAULT);
            myCapcoInfo.setUserPortion(null);
         }
      } else {
         myCapcoRequest.reset();

         if (myCapcoInfo != null) {
            myCapcoRequest.setBanner(myCapcoInfo.getBanner());
            myCapcoRequest.setAbbreviation(myCapcoInfo.getAbbreviation());
            myCapcoRequest.setPortion(myCapcoInfo.getPortion());
         }
         mySecurityBase.setCapcoInfo(myCapcoRequest);
      }
      if (myTagRequest == null) {
         myTagInfo.setMode(CapcoSource.USE_DEFAULT);
         myTagInfo.setBaseTagString(null);
      } else {
         myTagRequest.reset();
         myTagRequest.setIdentifiedTagString(myTagInfo.getIdentifiedTagString());
         myTagRequest.setRestrictionBitString(myTagInfo.getRestrictionBitString());
         mySecurityBase.setSecurityTagsInfo(myTagRequest);
      }
   }

    private static CapcoInfo resetSecurityData(Resource resourceIn) {

        Resource myResource = ((null != resourceIn) && (resourceIn instanceof DataView))
                ? ((DataView) resourceIn).getMeta()
                : resourceIn;

        if (null != myResource) {

            CapcoInfo myCapcoInfo = myResource.getCapcoInfo();
            SecurityTagsInfo myTagInfo = myResource.getSecurityTagsInfo();

            if (null != myCapcoInfo) {

                myCapcoInfo.setDataPortion(null);
            }

            if (null != myTagInfo) {

                myTagInfo.setDefaultTags();
            }
            return myCapcoInfo;
        }
        return null;
    }

    private static SecurityTagsInfo resetSecurityTags(Resource resourceIn) {

        SecurityTagsInfo myTagInfo = null;

        if (null != resourceIn) {

            if (resourceIn instanceof DataView) {

                DataViewDef myMeta = ((DataView)resourceIn).getMeta();
                myTagInfo = (null != myMeta) ? myMeta.getSecurityTagsInfo() : null;

            } else {

                myTagInfo = resourceIn.getSecurityTagsInfo();
            }
        }
        if (null != myTagInfo) {

            myTagInfo.setDefaultTags();
        }
        return myTagInfo;
    }

    // TODO: Review establishResourceSecurity

    private static void establishResourceSecurity(Resource resourceIn, Set<String> sourceAccessListIn,
                                                  SecurityTagsInfo tagInfoIn, CapcoRollup capcoProcessingIn)
            throws CsiSecurityException {

        SecurityMask mySecurityMode = SecurityMask.getTotalSecurityMask();
        Set<String> myCapcoAccessList = null;
        List<Set<String>> myDistributionList = null;

        if ((capcoProcessingIn != null) && mySecurityMode.hasCapcoRestrictions()) {
           myCapcoAccessList = capcoProcessingIn.getPositiveAclList();
        }
        if ((tagInfoIn != null) && mySecurityMode.hasGenericRestrictions()) {
           myDistributionList = tagInfoIn.getRestrictionTree();
        }
        if (mySecurityMode.hasSecurity()) {
            AclTagRepository.establishResourceSecurity(resourceIn, resourceIn.getUuid(), myCapcoAccessList,
                    myDistributionList, sourceAccessListIn);
        }
    }

    private static String getCapcoFailOver() {

        initializeSecurityFailOver();
        return _capcoFailOver;
    }

    private static String getSecurityTagsFailOver() {

        initializeSecurityFailOver();
        return _tagFailOver;
    }

    private static void initializeSecurityFailOver() {

        if (!_failOverInitialized) {

            _capcoFailOver = Configuration.getInstance().getSecurityPolicyConfig().getFailOverPortion();
            _tagFailOver = Configuration.getInstance().getSecurityPolicyConfig().getFailOverTags();

            if (((null == _capcoFailOver) || (0 == _capcoFailOver.length()))
                    && ((null == _tagFailOver) || (0 == _tagFailOver.length()))) {

                _capcoFailOver = Configuration.getInstance().getSecurityPolicyConfig().getDefaultPortion();
                _tagFailOver = Configuration.getInstance().getSecurityPolicyConfig().getDefaultTags();
            }
            _failOverInitialized = true;
        }
    }

   private static void convertToRowLevelSecurity(Connection connectionIn, Resource resourceIn) {
//    boolean myCapcoChange = convertToRowLevelCapco(connectionIn, resourceIn);
//    boolean myTagsChange = convertToRowLevelTags(connectionIn, resourceIn);
//
//    if (myCapcoChange || myTagsChange) {
//       CsiPersistenceManager.merge(resourceIn);
//       CsiPersistenceManager.commit();
//       CsiPersistenceManager.begin();
//    }
   }

    private static boolean convertToRowLevelCapco(Connection connectionIn, DataView dataViewIn) {

        if (null != dataViewIn) {

            String[] myDataTables = dataViewIn.getTableList();
            DataViewDef myMeta = dataViewIn.getMeta();
            CapcoInfo myCapco = myMeta.getCapcoInfo();

            if ((null != myCapco) && (null != myDataTables) && (0 < myDataTables.length)) {

                String myMarkingTable = locateMarkingTable(connectionIn, myDataTables[0], true);
                Map<String, Integer> myMap = new HashMap<String, Integer>();

                for (int i = 0; myDataTables.length > i; i++) {

                    if (null == myCapco) {

                        break;
                    }
                    String myTableName = myDataTables[i];

                    // Generate CAPCO ID field values within data table
                    // while rolling up CAPCO portions into final security banner
                    /*String myCapcoResult =*/ processCapcoPortions(connectionIn, myTableName,
                                                                myMarkingTable, myMap, myCapco);


                    // TODO: add to total rollup

                    // TODO: add to total rollup

                    // TODO: add to total rollup

                    // TODO: add to total rollup

                    // TODO: add to total rollup

                    // TODO: add to total rollup



                    myCapco = myCapco.getNext();
                }
                dataViewIn.setRowLevelCapco(true);
            }
            return dataViewIn.getRowLevelCapco();
        }
        return false;
    }

    private static boolean convertToRowLevelTags(Connection connectionIn, DataView dataViewIn) {

        if (null != dataViewIn) {

//            String[] myDataTables = dataViewIn.getTableList();
//            DataViewDef myMeta = dataViewIn.getMeta();
//            SecurityTagsInfo myTags = myMeta.getSecurityTagsInfo();

            return dataViewIn.getRowLevelCapco();
        }
        return false;
    }

    private static boolean convertToRowLevelCapco(Connection connectionIn, InstalledTable installedTableIn) {

        if (null != installedTableIn) {

//            String myDataTable = installedTableIn.getActiveTableName();
//            String myMarkingTable = locateMarkingTable(connectionIn, myDataTable, true);
//            CapcoInfo myCapco = installedTableIn.getCapcoInfo();







            installedTableIn.setRowLevelCapco(true);
            return installedTableIn.getRowLevelCapco();
        }
        return false;
    }

    private static boolean convertToRowLevelTags(Connection connectionIn, InstalledTable installedTableIn) {

        if (null != installedTableIn) {

//            String myDataTable = installedTableIn.getActiveTableName();
//            String myMarkingTable = locateMarkingTable(connectionIn, myDataTable, true);
//            SecurityTagsInfo myTags = installedTableIn.getSecurityTagsInfo();







            installedTableIn.setRowLevelTags(true);
            return installedTableIn.getRowLevelTags();
        }
        return false;
    }

   private static String processCapcoPortions(Connection connection, String dataTable, String markingTable,
                                              Map<String,Integer> mapIn, CapcoInfo capco) {
      String result = null;
      String userCapcoPortion = null;

      try {
         switch (capco.getMode()) {
            case USE_DEFAULT:
               result = genCapcoResult(connection, dataTable, markingTable, mapIn, getCapcoDefault());
               break;
            case USER_ONLY:
               result = genCapcoResult(connection, dataTable, markingTable, mapIn, capco.getUserPortion());
               break;
            case USER_AND_DATA:
               userCapcoPortion = capco.getUserPortion();
               //no break intentional
            case DATA_ONLY:
               List<String> fields = capco.getSecurityFields();

               if (fields.isEmpty()) {
                  result = genCapcoResult(connection, dataTable, markingTable, mapIn, userCapcoPortion);
               } else {
                  String sql =
                     new StringBuilder("SELECT DISTINCT ")
                              .append(fields.stream().map(SqlUtil::quote).collect(Collectors.joining(SqlTokens.COMMA_SPACE)))
                              .append(" FROM ")
                              .append(SqlUtil.quote(dataTable)).toString();

                  try (Statement statement = connection.createStatement();
                       ResultSet results = statement.executeQuery(sql)) {
                     result =
                        ((results != null) && results.next())
                           ? genCapcoResult(connection, dataTable, markingTable, mapIn, results, fields, userCapcoPortion)
                           : genCapcoResult(connection, dataTable, markingTable, mapIn, userCapcoPortion);
                  }
               }
               break;
         }
      } catch (Exception myException) {
         SqlUtil.quietRollback(connection);
         LOG.error("Caught exception processing CAPCO rollup:", myException);
      }
      return result;
   }

    private static String genCapcoResult(Connection connectionIn, String dataTableIn, String markingTableIn,
                                         Map<String, Integer> mapIn, ResultSet resultSetIn,
                                         List<String> fieldListIn, String commonIn) {

        try {

            int myCounter = mapIn.size();
            Map<String, ValuePair<Integer, String>> myMap = new HashMap<String, ValuePair<Integer, String>>();
            int myFieldCount = fieldListIn.size();

            while (resultSetIn.next()) {

                if (1 < myFieldCount) {

                    Map<String, Integer> myLocalMap = new HashMap<String, Integer>(myFieldCount);
                    String[] myPortionArray = new String[myFieldCount];
                    for (int i = 0; myFieldCount > i; i++) {

                        myPortionArray[i] = resultSetIn.getString(i + 1);
                        myLocalMap.put(myPortionArray[i], null);
                    }
                    CapcoRollup myLocalRollup = CapcoRollup.rollupPortions(myLocalMap);
                    String myLocalPortion = myLocalRollup.getCapcoInfo().getPortion();
                    Integer myId = mapIn.get(myLocalPortion);

                    if (null == myId) {

                        myMap.put(myLocalPortion, new ValuePair<Integer, String>(myCounter, myLocalPortion));
                        mapIn.put(myLocalPortion, myCounter++);
                        // Update marking table
                        updateMarkingTable(connectionIn, markingTableIn, myId, myLocalPortion);
                    }
                    // Update data table
                    updateDataTable(connectionIn, dataTableIn, CacheTokens.CSI_CAPCO_ID, myId, fieldListIn, myPortionArray);

                } else {

                    String myLocalPortion = resultSetIn.getString(1);
                    Integer myId = mapIn.get(myLocalPortion);

                    if (null == myId) {

                        myId = myCounter++;
                        myMap.put(myLocalPortion, new ValuePair<Integer, String>(myCounter, myLocalPortion));
                        mapIn.put(myLocalPortion, myId);
                        // Update marking table
                        updateMarkingTable(connectionIn, markingTableIn, myId, myLocalPortion);
                    }
                    // Update data table
                    updateDataTable(connectionIn, dataTableIn, CacheTokens.CSI_CAPCO_ID, myId,
                                    fieldListIn, new String[]{myLocalPortion});
                }
            }
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            // TODO: Add possible user portion to rollup
            return CapcoRollup.rollupPortions(myMap).getCapcoInfo().getPortion();

        } catch (Exception myException) {

            LOG.error("Caught exception initializing row level security:", myException);
        }
        return null;
    }

    private static String genCapcoResult(Connection connectionIn, String dataTableIn,
                                         String markingTableIn, Map<String, Integer> mapIn, String commonIn) {

        try {
            Integer myKey = mapIn.get(commonIn);

            if (null == myKey) {

                myKey = mapIn.size();
                mapIn.put(commonIn, myKey);
                updateMarkingTable(connectionIn, markingTableIn, myKey, commonIn);
            }
            updateDataTable(connectionIn, dataTableIn, CacheTokens.CSI_CAPCO_ID, myKey);

            return commonIn;

        } catch (Exception myException) {

            LOG.error("Caught exception initializing row level security:", myException);
        }
        return null;
    }

   private static void updateMarkingTable(Connection connection, String table, Integer key, String marking)
         throws Exception {
      String sql = new StringBuilder("INSERT INTO (key, tag) VALUES ('")
                             .append(key.toString())
                             .append("', '")
                             .append(marking)
                             .append("')")
                             .append(SqlUtil.quote(table)).toString();

      try (Statement statement = connection.createStatement()) {
         statement.executeUpdate(sql);
      }
   }

    private static void updateDataTable(Connection connectionIn, String tableIn, String targetIn, Integer keyIn)
            throws Exception {

        updateDataTable(connectionIn, tableIn, targetIn, keyIn, null, null);
    }

    private static void updateDataTable(Connection connectionIn, String tableIn, String targetIn,
                                        Integer keyIn, List<String> columnListIn, String[] markingArrayIn)
            throws Exception {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append("UPDATE ");
        myBuffer.append(SqlUtil.quote(tableIn));
        myBuffer.append(" SET ");
        myBuffer.append(SqlUtil.quote(targetIn));
        myBuffer.append(" = ");
        CacheUtil.singleQuote(myBuffer, keyIn.toString());
        if ((null != columnListIn) && !columnListIn.isEmpty()) {

            myBuffer.append(" WHERE ");
            int howMany = columnListIn.size();
            boolean first = true;

            for (int i = 0; i < howMany; i++) {
                String myColumn = columnListIn.get(i);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(" AND ");
                }
                myBuffer.append(SqlUtil.quote(myColumn));

                if ((null != markingArrayIn) && (markingArrayIn.length > i) && (null != markingArrayIn[i])) {

                    myBuffer.append(" = ");
                    CacheUtil.singleQuote(myBuffer, markingArrayIn[i]);

                } else {

                    myBuffer.append(" IS NULL");
                }
            }
        }
        try (Statement statement = connectionIn.createStatement()) {
           statement.executeUpdate(myBuffer.toString());
        }
    }

    private static boolean convertToRowLevelCapco(Connection connectionIn, Resource resourceIn) {

        return false;
    }

    private static boolean convertToRowLevelTags(Connection connectionIn, Resource resourceIn) {

        return false;
    }

    private static void createSecurityTable(String tableNameIn) {

//        String myCommand = "CREATE TABLE " + tableNameIn + " ('key' SMALLINT, 'tag' VARCHAR(256))";
    }
}
