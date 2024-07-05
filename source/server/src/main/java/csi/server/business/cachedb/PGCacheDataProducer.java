package csi.server.business.cachedb;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.util.CsiUtil;
import csi.server.util.DateUtil;
import csi.server.util.DerbyColumnTypeMapper;
import csi.server.util.SqlUtil;

// NB: There is currently a hack in DataCacheHelper::createSupportingQuery to trim the
// myValues of a column and the myValue when building a query for the supporting rows.
// This is required to ensure that are properly performing comparisons for the query.
// For example 'CORPORATE   ' = 'CORPORATE' fails.  If any changes are made to
// trim string myValues or provide varying length myValues, please visit the method
// named above and update accordingly!
public class PGCacheDataProducer extends Thread implements CacheDataProducer {
    private static final Logger LOG = LogManager.getLogger(PGCacheDataProducer.class);
    private static int _maxInt = 0x7fffffff;

    private static boolean _doDebug = LOG.isDebugEnabled();

    private OutputStream _out;
    private ResultSet _results;
    private Exception _exception;
    private long _rowCount;
    private List<ValuePair<FieldDef, Integer>> _orderedFields;
    private int _columnLimit;
    private int _rowLimit;
    private volatile boolean _isCanceled = false;
    private long _progress = 0L;
    private long _blockCount = 0L;
    private String _taskId = null;

    public PGCacheDataProducer(PipedOutputStream writerIn, ResultSet resultsIn,
                               List<ValuePair<FieldDef, Integer>> orderedFieldsIn, Integer rowLimitIn) {
//        _out = new BufferedOutputStream(writerIn, 65536);
        _out = writerIn;
        _results = resultsIn;
        _orderedFields = orderedFieldsIn;
        _columnLimit = _orderedFields.size();
        _rowLimit = (null != rowLimitIn) ? rowLimitIn : _maxInt;
        _taskId = TaskHelper.getCurrentContext().getTaskId();
    }

    public synchronized void handleCancel(boolean forceCancelIn) {

        if (forceCancelIn) {

            _isCanceled = true;

        }
        if (_isCanceled) {

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
        Map<String, List<String>> myWarnings = new HashMap<String, List<String>>();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        // insert the data
        long myRowCount = 0;
//        StringBuilder myBuffer = new StringBuilder();


        if (_doDebug) {
         LOG.debug("Preformat data for copy command . . .");
      }
        try {
            byte[] myDelimiter = ",".getBytes();
            byte[] myTerminator = "\n".getBytes();
            byte[] myEmptyBytes = new byte[0];
            boolean[] myDoProcess = new boolean[_orderedFields.size()];
            int[] mySourceIndex = new int[_orderedFields.size()];
            CsiDataType[] myCoerceType = new CsiDataType[_orderedFields.size()];
            Object[] myValues = new Object[_orderedFields.size()];

            ResultSetMetaData myMetaData = _results.getMetaData();

            for (int myColumnId = 0; _columnLimit > myColumnId; myColumnId++) {

                ValuePair<FieldDef, Integer> myPair = _orderedFields.get(myColumnId);
                FieldDef myField = myPair.getValue1();

                myDoProcess[myColumnId] = false;
                myValues[myColumnId] = null;

                if ((null != myField) && ((FieldType.COLUMN_REF == myField.getFieldType())
                                            || (FieldType.LINKUP_REF == myField.getFieldType()))) {

                    // get location within data row and data type for target column
                    Integer myIndex = myPair.getValue2();
                    CsiDataType myTargetDataType = myField.tryStorageType();

                    // Get data type for data in the stream
                    int myTypeCode = myMetaData.getColumnType(myIndex);
                    CsiDataType mySourceDataType = DerbyColumnTypeMapper.jdbcCodeToCentrifugeType(myTypeCode);

                    // Establish any necessary coercion
                    myCoerceType[myColumnId] = ((null == myTargetDataType) || (myTargetDataType == mySourceDataType))
                            ? null : myTargetDataType;

                    myDoProcess[myColumnId] = true;
                    mySourceIndex[myColumnId] = myIndex;

                    if (null != myCoerceType[myColumnId]) {

                        LOG.debug(Format.value(myField.getFieldName())
                                + "  srcType:" + Format.value(mySourceDataType.getLabel())
                                + "  coerceTo:" + Format.value(myCoerceType[myColumnId].getLabel()));
                    }
                }
            }
            while (SqlUtil.hasMoreRows(_results)) {

                byte[] myPrefix = myEmptyBytes;

                if (_rowLimit <= myRowCount) {

                    break;
                }
                TaskHelper.checkForCancel();

                for (int myColumnId = 0; _columnLimit > myColumnId; myColumnId++) {
                    TaskHelper.checkForCancel();

                    Object myValue = null;

                    if (myDoProcess[myColumnId]) {

                        Integer myIndex = mySourceIndex[myColumnId];

                        try {
                            myValue = _results.getObject(myIndex);
                            if ((myValue != null) && (myValue instanceof Calendar)) {
                                myValue = ((Calendar) myValue).getTime();
                            }
                        } catch (SQLException e) {
                            // track error and treat myValue as null
                            String myColumnName
                                    = CacheUtil.toQuotedDbUuid(_orderedFields.get(myColumnId).getValue1().getUuid());
                            CacheUtil.trackErrors(myWarnings, myRowCount + 1, myColumnName, e);
                        }

                        if ((myValue != null) && (null != myCoerceType[myColumnId])) {
                           try {
                              if (myValue instanceof java.sql.Time) {
                                 myValue = CsiTypeUtil.coerceString((java.sql.Time) myValue, DateTimeFormatter.ISO_LOCAL_TIME);
                              } else if (myValue instanceof java.sql.Date) {
                                 myValue = CsiTypeUtil.coerceString((java.sql.Date) myValue, DateTimeFormatter.ISO_LOCAL_DATE);
                              } else if (myValue instanceof Date) {
                                 myValue = CsiTypeUtil.coerceString((Date) myValue, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
                              } else {
                                 myValue = CsiTypeUtil.coerceType(myValue, myCoerceType[myColumnId], null);
                              }
//                                    myValue = CsiTypeUtil.coerceType(myValue, CsiDataType.valueOf(myCoerceType[myColumnId]), myFormat);
                            } catch (Throwable t) {
                                myValue = null;
                                CacheUtil.trackErrors(myWarnings, myRowCount + 1,
                                                        _orderedFields.get(myColumnId).getValue1().getFieldName(), t);
                            }
                        }
                        _out.write(myPrefix);
                        myPrefix = myDelimiter;
                        if (myValue != null) {
                            String myEncodedValue = CacheUtil.encodeCsv(myValue);
//                            myBuffer.append(myEncodedValue);
                            _out.write(myEncodedValue.getBytes("UTF8"));
                        }
//                        myBuffer.append(",");
                    }
                }
                _out.write(myTerminator);
//                myBuffer.setLength(myBuffer.length() - 1);
//                myBuffer.append("\n");
//                LOG.info(myBuffer.toString());
//                myBuffer.setLength(0);

                recordProgress(++myRowCount);
                //LOG.info(Long.toString(myRowCount++) + ':' + myBuffer.toString());
                //myBuffer.setLength(0);
                if (_doDebug) {
                  LOG.debug("    " + Long.toString(myRowCount) + " rows written to stream.");
               }
            }

            _out.flush();
            stopWatch.stop();
            LOG.info("Retrieved " + myRowCount + " rows from data source in " + stopWatch.getTime() + " ms");

            if (!myWarnings.isEmpty()) {
                LOG.warn(CacheUtil.buildLoadWarningMsg(myWarnings));
            }
            _rowCount = myRowCount;
        } catch (Exception myException) {
            _exception = myException;
//            if (0 < myBuffer.length()) {

//                LOG.info(myBuffer.toString());
//            }
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
                TaskHelper.reportProgress("Resulting Cache Table", (int)_progress);
            }
        }
    }
}
