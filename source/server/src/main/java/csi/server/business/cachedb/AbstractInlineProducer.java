package csi.server.business.cachedb;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.util.Format;
import csi.server.common.util.uploader.CsiInputStream;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;

/**
 * Created by centrifuge on 9/24/2015.
 */
public abstract class AbstractInlineProducer<T extends CsiInputStream> extends Thread implements CacheDataProducer {
   private static final Logger LOG = LogManager.getLogger(AbstractInlineProducer.class);

    private static final int _byteBufferSize = 16 * 1024;
    private static int _maxInt = 0x7fffffff;
    private static boolean _doDebug = LOG.isDebugEnabled();

    private int _progress = 0;
    private String _taskId = null;

    protected long _rowCount = 0L;
    protected T _receivingStream = null;
    protected PipedInputStream _generatedStream = null;
    protected PipedOutputStream _intermediateStream = null;
    protected Exception _exception = null;
    protected volatile boolean _isCanceled = false;
    protected int _rowLimit;
    protected long _byteCount = 0L;
    protected long _blockCount = 0L;

    public AbstractInlineProducer(String taskIdIn, T streamIn, Integer rowLimitIn) {

        try {

            _taskId = taskIdIn;
            _receivingStream = streamIn;
            _generatedStream = new PipedInputStream();
            _intermediateStream = new PipedOutputStream(_generatedStream);
            _rowLimit = (null != rowLimitIn) ? rowLimitIn : _maxInt;

        } catch(Exception myException) {

            _exception = myException;
        }
    }

    public InputStream getStreamHandle() {

        return _generatedStream;
    }

    public Exception getException() {

        return _exception;
    }

    public synchronized void handleCancel(boolean forceCancelIn) {

        if (forceCancelIn) {

            _isCanceled = true;

        } else if (_isCanceled) {

            throw new TaskCancelledException();
        }
    }

    public void run() {

        if (null == _exception) {

            try {

                Exception myException = execute();

                if (null == _exception) {

                    if (null == myException) {

                        _intermediateStream.flush();

                    } else {

                        _exception = myException;
                    }
                }

            } catch(Exception myException) {

                if (null == _exception) {

                    _exception = myException;
                }
            }
        }
        try {

            _intermediateStream.close();

        } catch(Exception myException) {

            if (null == _exception) {

                _exception = myException;
            }
        }
    }

    public long getRowCount() {

        return _rowCount;
    }

    protected Exception execute() {

        int myCount = 0;
        byte[] myBuffer = new byte[_byteBufferSize];

        while (0 <= myCount) {

            myCount = read(myBuffer, 0, _byteBufferSize);

            if (0 <= myCount) {

                write(myBuffer, 0, myCount);
            }
        }
        return null;
    }

    protected void write(byte[] bufferIn, int offsetIn, int lengthIn) {

        try {

            _byteCount += lengthIn;
            _intermediateStream.write(bufferIn, offsetIn, lengthIn);
            _intermediateStream.flush();
            recordProgress();

        } catch(Exception myException) {

            _exception = myException;
        }
    }

    protected int read(byte[] bufferIn, int offsetIn, int lengthIn) {

        int myResult = -1;

        try {

            myResult = _receivingStream.read(bufferIn, offsetIn, lengthIn);

        } catch(Exception myException) {

            _exception = myException;
        }
        return myResult;
    }

    protected void recordProgress() {

        int myProgress = _receivingStream.getProgress();
        long myBlockCount = _byteCount / _byteBufferSize;

        if (myBlockCount > _blockCount) {

            _blockCount = myBlockCount;
            handleCancel(false);
        }
        if (myProgress > _progress) {

            _progress = myProgress;

            if (_doDebug) {

               LOG.debug("* * * * * TaskHelper.reportProgress(" + Format.value(_taskId) + ", " + Format.value(_progress) + ");");
            }
            TaskHelper.reportProgress(_taskId, _progress);
        }
    }
}
