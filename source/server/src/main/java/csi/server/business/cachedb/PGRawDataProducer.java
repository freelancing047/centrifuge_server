package csi.server.business.cachedb;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.CsiUtil;
import csi.server.util.SqlUtil;

public class PGRawDataProducer extends Thread implements CacheDataProducer {
   private static final Logger LOG = LogManager.getLogger(PGRawDataProducer.class);

   private static boolean _doDebug = LOG.isDebugEnabled();
   private static int _maxInt = 0x7fffffff;

    private OutputStream _out;
    private ResultSet _results;
    private Exception _exception;
    private String _charset;
    private long _rowCount;
    private ConnectionFactory _factory;
    private int _rowLimit;
    private volatile boolean _isCanceled = false;
    private long _progress = 0L;
    private long _blockCount = 0L;
    private String _taskId = null;
    private String _source = null;

    public PGRawDataProducer(PipedOutputStream writerIn, ResultSet resultsIn, ConnectionFactory factoryIn,
                             String charsetIn, Integer rowLimitIn, String sourceIn) {
        _out = new BufferedOutputStream(writerIn, 65536);
        _results = resultsIn;
        _factory = factoryIn;
        _charset = charsetIn;
        _rowLimit = (null != rowLimitIn) ? rowLimitIn : _maxInt;
        _taskId = TaskHelper.getCurrentContext().getTaskId();
        _source = sourceIn;
    }

    public synchronized void handleCancel(boolean forceCancelIn) {

        if (forceCancelIn) {

            _isCanceled = true;

        } else if (_isCanceled) {

            throw new TaskCancelledException();
        }
    }

    public long getRowCount() {
        return _rowCount;
    }

    public Exception getException() {
        return _exception;
    }

    public void run() {
        Map<String, List<String>> warnings = new HashMap<String, List<String>>();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        // insert the data
        try {
            ResultSetMetaData rsMeta = _results.getMetaData();
            int myColumnCount = rsMeta.getColumnCount();
            boolean[] myDoProcess = new boolean[myColumnCount + 1];
            long myRowCount = 0;
            byte[] myDelimeter = ",".getBytes(_charset);
            byte[] myTerminator = "\n".getBytes(_charset);
            byte[][] myPrefixes = new byte[myColumnCount + 1][];

            for (int i = 1; i <= myColumnCount; i++) {

                int myTypeCode = rsMeta.getColumnType(i);
                String myTypeName = rsMeta.getColumnTypeName(i);
                CsiDataType myValueType = CacheUtil.resolveCsiType(myTypeName, myTypeCode, _factory);

                myDoProcess[i] = (CsiDataType.Unsupported != myValueType);
                if (1 < i) {

                    myPrefixes[i] = myDelimeter;
                }
            }
            myPrefixes[1] = new byte[0];

            while (SqlUtil.hasMoreRows(_results)) {

                if (_rowLimit <= _rowCount) {

                    break;
                }
                for (int i = 1; i <= myColumnCount; i++) {

                    _out.write(myPrefixes[i]);

                    if (myDoProcess[i]) {

                        try {

                            String myValue = CacheUtil.encodeCsv(_results.getObject(i));

                            if (null != myValue) {

                                _out.write(myValue.getBytes(_charset));
                            }

                        } catch (SQLException e) {

                            String myColumnname = SqlUtil.getColumnName(rsMeta, i);
                            CacheUtil.trackErrors(warnings, myRowCount + 1, myColumnname, e);
                        }
                    }
                }
                _out.write(myTerminator);
                recordProgress(++myRowCount);
            }
            _out.flush();
            stopWatch.stop();
            LOG.debug("Retrieved " + myRowCount + " rows in " + stopWatch.getTime() + " ms");

            if (!warnings.isEmpty()) {
                LOG.warn(CacheUtil.buildLoadWarningMsg(warnings));
            }
            _rowCount = myRowCount;
        } catch (Exception myException) {
            _exception = myException;
        } finally {
            SqlUtil.quietCloseResulSet(_results);
            CsiUtil.quietClose(_out);
        }
    }

    protected void recordProgress(long rowCountIn) {

        long myProgress = rowCountIn / 1000L;
        long myBlockCount = rowCountIn / 200L;

        if (myBlockCount > _blockCount) {

            _blockCount = myBlockCount;
            handleCancel(false);
        }
        if (null != _taskId) {

            if (myProgress > _progress) {

                _progress = myProgress;

                if (_doDebug) {

                    LOG.debug("* * * * * TaskHelper.reportProgress(" + Format.value(_taskId) + ", " + Format.value(_progress) + ");");
                }
                TaskHelper.reportProgress(_source, (int) _progress);
            }
        }
    }
}
