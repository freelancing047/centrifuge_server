package csi.server.business.cachedb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import csi.server.dao.CsiConnection;
import csi.server.util.CsiUtil;
import csi.server.util.SqlUtil;

public class PGCacheDataConsumer extends Thread implements CacheDataConsumer {
   private static final Logger LOG = LogManager.getLogger(PGCacheDataConsumer.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

   private Connection _connection;
   private String _sqlCommand = null;
   private String _tableName;
   private String _columnNames;
   private InputStream _reader;
   private long _rowCount;
   private Exception _exception;
   private boolean _executeCommand;

   private boolean usePipeDelimiter;

   public PGCacheDataConsumer(InputStream readerIn, Connection connectionIn, String tableNameIn, String columnNamesIn) {
      this(readerIn, connectionIn, tableNameIn, columnNamesIn, true);
   }

   public PGCacheDataConsumer(InputStream readerIn, Connection connectionIn, String tableNameIn, String columnNamesIn,
         boolean executeCommandIn) {
//        _reader = new BufferedInputStream(readerIn, 65536);
      _reader = readerIn;
      _connection = connectionIn;
      _tableName = tableNameIn;
      _columnNames = columnNamesIn;
      _executeCommand = executeCommandIn;
   }

   public PGCacheDataConsumer(InputStream readerIn, Connection connectionIn, String sqlCommandIn) {
      this(readerIn, connectionIn, sqlCommandIn, true);
   }

   public PGCacheDataConsumer(InputStream readerIn, Connection connectionIn, String sqlCommandIn,
                              boolean executeCommandIn) {
      _reader = new BufferedInputStream(readerIn, 65536);
      _connection = connectionIn;
      _sqlCommand = sqlCommandIn;
      _executeCommand = executeCommandIn;
   }

   public void usePipeDelimiter(boolean flag) {
      usePipeDelimiter = flag;
   }

   public long getRowCount() {
      return _rowCount;
   }

   public Exception getException() {
      return _exception;
   }

   public void run() {
      try {
         if (_executeCommand) {
            CopyManager copy = getCopyManager(_connection);

            if (_sqlCommand == null) {
               String template = "COPY %s (%s) FROM STDIN WITH CSV";

               if (usePipeDelimiter) {
                  template = "COPY %s (%s) FROM STDIN WITH DELIMITER AS '|' CSV";
               }
               _sqlCommand = String.format(template, SqlUtil.quote(_tableName), _columnNames);
            }
            LOG.debug("EXECUTE:" + _sqlCommand);
            _rowCount = copy.copyIn(_sqlCommand, _reader);
         } else {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start();

            try (InputStreamReader inputStreamReader = new InputStreamReader(_reader);
                 BufferedReader myReader = new BufferedReader(inputStreamReader)) {
               String line;
               _rowCount = 0;

               while ((line = myReader.readLine()) != null) {
                  _rowCount++;

                  if (_doDebug) {
                     LOG.debug(Long.toString(_rowCount) + ": " + line);
                  }
               }
            }
            stopWatch.stop();
            LOG.debug("Imported " + _rowCount + " rows into cache in " + stopWatch.getTime() + " ms");
         }
      } catch (Exception exception) {
         _exception = exception;
      } finally {
         CsiUtil.quietClose(_reader);
      }
   }

   private CopyManager getCopyManager(Connection connectionIn) throws SQLException {

      Connection myConnection =
         (connectionIn instanceof CsiConnection)
            ? ((CsiConnection) connectionIn).getConnection()
            : connectionIn;
      PGConnection myPgConnection =
         (myConnection instanceof PGConnection)
            ? (PGConnection) myConnection
            : (PGConnection) ((DelegatingConnection) connectionIn).getInnermostDelegate();

      return myPgConnection.getCopyAPI();
   }
}
