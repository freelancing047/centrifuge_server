package csi.server.business.cachedb;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOut;

import csi.server.dao.CsiConnection;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CsiUtil;
import csi.server.util.SqlUtil;

/**
 * Created by centrifuge on 5/23/2018.
 */
public class PGCacheDataDump extends Thread implements CacheDataProducer {
    private static final Logger LOG = LogManager.getLogger(PGCacheDataDump.class);

    private boolean _doDebug = LOG.isDebugEnabled();

    private Connection _connection;
    private String _sqlCommand = null;
    private String _tableName;
    private String _columnNames;
    private OutputStream _writer;
    private long _rowCount;
    private Exception _exception;
    private boolean _executeCommand;
    private boolean _isCanceled = false;

    public PGCacheDataDump(OutputStream writerIn, Connection connectionIn,
                           String tableNameIn, String columnNamesIn) {
        this(writerIn, connectionIn, tableNameIn, columnNamesIn, true);
    }

    public PGCacheDataDump(OutputStream writerIn, Connection connectionIn, String tableNameIn,
                           String columnNamesIn, boolean executeCommandIn) {
        _writer = writerIn;
        _connection = connectionIn;
        _tableName = tableNameIn;
        _columnNames = columnNamesIn;
        _executeCommand = executeCommandIn;
    }

    public PGCacheDataDump(OutputStream writerIn, Connection connectionIn, String sqlCommandIn) {

        this(writerIn, connectionIn, sqlCommandIn, true);
    }

    public PGCacheDataDump(OutputStream writerIn, Connection connectionIn,
                               String sqlCommandIn, boolean executeCommandIn) {
        _writer = writerIn;
        _connection = connectionIn;
        _sqlCommand = sqlCommandIn;
        _executeCommand = executeCommandIn;
    }

    public long getRowCount() {

        return _rowCount;
    }

    public Exception getException() {

        return _exception;
    }

    public synchronized void handleCancel(boolean forceCancelIn) {

        if (forceCancelIn) {

            _isCanceled = true;

        }
        if (_isCanceled) {

            throw new TaskCancelledException();
        }
    }

    public void run() {
        try {
            StopWatch stopWatch = new StopWatch();

            stopWatch.start();

            CopyManager myManager = getCopyManager(_connection);
            String mySql = _sqlCommand;

            if (null == mySql) {

                String myTemplate = "COPY %s (%s) TO STDOUT WITH CSV";
                mySql = String.format(myTemplate, SqlUtil.quote(_tableName), _columnNames);
            }
            if (_doDebug) {
               LOG.debug("EXECUTE:" + mySql);
            }
            _rowCount = dump(myManager, mySql, _writer);
            stopWatch.stop();
            if (_doDebug) {
               LOG.debug("Exported " + _rowCount + " rows into cache in " + stopWatch.getTime() + " ms");
            }

        } catch (Exception myException) {

            _exception = myException;

        } finally {

            CsiUtil.quietClose(_writer);
        }
    }

    private CopyManager getCopyManager(Connection connectionIn) throws SQLException {

        Connection myConnection = (connectionIn instanceof CsiConnection)
                ? ((CsiConnection) connectionIn).getConnection()
                : connectionIn;
        PGConnection myPgConnection = (myConnection instanceof PGConnection)
                ? (PGConnection)myConnection
                : (PGConnection)((org.apache.tomcat.dbcp.dbcp2.DelegatingConnection)connectionIn).getInnermostDelegate();

        return myPgConnection.getCopyAPI();
    }

    private long dump(CopyManager managerIn, final String sqlIn, OutputStream writerIn) throws SQLException, IOException {

        byte[] myBuffer;

        CopyOut myControl = managerIn.copyOut(sqlIn);
        try {

            while ((myBuffer = myControl.readFromCopy()) != null) {

                writerIn.write(myBuffer);
            }

        } catch (Exception myException) {
            // if not handled this way the close call will hang, at least in 8.2
            throw myException;

        } finally {

            if (myControl.isActive()) {

                myControl.cancelCopy();
            }
        }
        return myControl.getHandledRowCount();
    }
}
