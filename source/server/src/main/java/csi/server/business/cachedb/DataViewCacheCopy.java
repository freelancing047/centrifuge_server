package csi.server.business.cachedb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.helper.QueryHelper;
import csi.server.business.helper.SharedDataSourceHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

/**
 * Created by centrifuge on 5/22/2015.
 */
public class DataViewCacheCopy {
   private static final Logger LOG = LogManager.getLogger(DataViewCacheCopy.class);

    private DataView _dataView = null;
    private String _oldTableBase = null;
    private String _newTableBase = null;
    private String _oldViewName = null;
    private String _newViewName = null;
    private String[] _views = null;
    private String[] _tables = null;
    private String[] _linkups = null;
    private String[] _installedTables = null;

    public DataViewCacheCopy(DataView sourceDataViewIn, DataView targetDataViewIn) {

        _dataView = targetDataViewIn;

        _oldTableBase = CacheUtil.toDbUuid(sourceDataViewIn.getUuid());
        _newTableBase = CacheUtil.toDbUuid(targetDataViewIn.getUuid());
        _oldViewName = CacheUtil.getCacheTableName(sourceDataViewIn.getUuid());
        _newViewName = CacheUtil.getCacheTableName(targetDataViewIn.getUuid());

        _views = targetDataViewIn.clearViews();
        _tables = targetDataViewIn.clearTables();
        _linkups = targetDataViewIn.clearLinkups();
        _installedTables = targetDataViewIn.clearInstalledTables();
    }

    public void executeCopy() throws Exception {
       try (Connection myConnection = CsiPersistenceManager.getCacheConnection()) {
          try {
             for (int i = 0; i < _installedTables.length; i += 2) {
//                String myLock = _installedTables[i];
                String myQuery = _installedTables[i + 1].replace(_oldTableBase, _newTableBase);

                _dataView.addInstalledTable(SharedDataSourceHelper.incrementLock(_installedTables[i]));
                _dataView.addInstalledTable(myQuery);
             }
             try (Statement myStatement = myConnection.createStatement()) {
                for (int i = 0 ; i < _tables.length; i++) {
                   String mySource = _tables[i];
                   String myTarget = mySource.replace(_oldTableBase, _newTableBase);
                   String myCreateRequest = "CREATE TABLE " + Format.value(myTarget)
                                             + " AS SELECT * FROM " + Format.value(mySource);
                   String myAlterRequest = "ALTER TABLE " + Format.value(myTarget) + " ADD CONSTRAINT "
                                            + myTarget + "_pkey" + " PRIMARY KEY (internal_id)";

                   myStatement.addBatch(myCreateRequest);
                   myStatement.addBatch(myAlterRequest);
                   _dataView.addTable(myTarget);
                }
                for (int i = 0 ; i < _linkups.length; i++) {
                   String mySource = _linkups[i];
                   String myTarget = mySource.replace(_oldTableBase, _newTableBase);
                   String myCreateRequest = "CREATE TABLE " + Format.value(myTarget)
                                              + " AS SELECT * FROM " + Format.value(mySource);
                   String myAlterRequest = "ALTER TABLE " + Format.value(myTarget) + " ADD CONSTRAINT "
                                              + myTarget + "_pkey" + " PRIMARY KEY (internal_id)";

                   myStatement.addBatch(myCreateRequest);
                   myStatement.addBatch(myAlterRequest);
                   _dataView.addLinkup(myTarget);
                }
                myStatement.executeBatch();
                myConnection.commit();
             }
             // Create a top level view of the cache including dynamic fields
             for (String viewCommand: createViewCommands(myConnection)) {
                QueryHelper.executeSQL(myConnection, viewCommand, null);
                myConnection.commit();      //TODO: ?? inside loop?  Not after?
             }
          } catch(Exception exception) {
             LOG.error("Caught exception copying cache for DataView " + Format.value(_dataView.getName()), exception);
             SqlUtil.quietRollback(myConnection);
             throw exception;
          }
       } catch(Exception exception) {
          LOG.error("Caught exception copying cache for DataView " + Format.value(_dataView.getName()), exception);
          throw exception;
       }
    }

   private List<String> createViewCommands(Connection connectionIn) throws CentrifugeException, SQLException {
      List<String> viewCommands = new ArrayList<String>();
      //Creates top level view and any other subviews that needed to be created as well, relies on insertion order being correct
      String myViewRequest = "SELECT viewname, definition from pg_catalog.pg_views where viewowner = 'csiserver' and (viewname = '"
                + _oldViewName + "' OR viewname LIKE '"
                        + _oldViewName + "\\__') ORDER BY viewname DESC";

      try (ResultSet myResults = QueryHelper.executeSingleQuery(connectionIn, myViewRequest, null)) {
         while (myResults.next()) {
            StringBuilder myBuffer = new StringBuilder();
            String myOldName = myResults.getString(1);
            String myOldViewCommand = myResults.getString(2);

            if ((myOldViewCommand != null) && (myOldViewCommand.length() > 0)) {
                String myNewViewCommand = myOldViewCommand.replace(_oldTableBase, _newTableBase);
                String myNewName = myOldName.replace(_oldTableBase, _newTableBase);

                myBuffer.append("CREATE OR REPLACE VIEW \"");
                myBuffer.append(myNewName);
                myBuffer.append("\" AS ");
                myBuffer.append(myNewViewCommand);

                _dataView.addView(_newViewName);

                if (myBuffer.length() > 0) {
                    viewCommands.add(myBuffer.toString());
                }
            }
        }
      }
      return viewCommands;
   }
}
