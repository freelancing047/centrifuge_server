package csi.server.business.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ValuePair;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.SqlUtil;

/**
 * Created by centrifuge on 4/4/2018.
 */
public class BackgroundQuery extends Thread {
   private static final Logger LOG = LogManager.getLogger(BackgroundQuery.class);

    private enum Mode { CALL, LOAD, RETRIEVE }

    private Connection _connection;
    private PreparedStatement _statement;
    private Exception _exception;
    private Mode _mode;
    private ResultSet _results;
    volatile boolean cancel = false;

    public BackgroundQuery(Connection connectionIn, PreparedStatement statementIn, Mode modeIn) {
       _connection = connectionIn;
       _statement = statementIn;
       _mode = modeIn;
       _results = null;
       cancel = false;
    }

    public static Exception load(Connection connectionIn, String commandIn,
                                 List<QueryParameterDef> parametersIn, Integer rowLimitIn) {

        Exception myException = null;
        String myQueryText = QueryHelper.makePrepareStr(commandIn);
        DBConfig myConfig = Configuration.getInstance().getDbConfig();
        boolean myProcessingFlag = false;
//        String myPidQuery = "SELECT pid FROM pg_stat_activity WHERE datname = 'cachedb' AND state != 'active' AND query_start >= '2018-04-10 14:08:38.496476+00';

        try (PreparedStatement myRequest = connectionIn.prepareStatement(myQueryText)) {
           try {

                myRequest.setFetchSize(myConfig.getRecordFetchSize());

            } catch (SQLException myLimitException) {

               LOG.warn("SetFetchSize() method not supported.");
            }
            QueryHelper.applyParameters(myRequest, commandIn, parametersIn);
            if (null != rowLimitIn) {

                myRequest.setMaxRows(rowLimitIn);
            }
            launchRequest(connectionIn, myRequest, Mode.LOAD);
            myProcessingFlag = true;

        } catch (TaskCancelledException myCancel) {

            myException = myCancel;

        } catch (Exception myError) {

            String myNotation = myProcessingFlag ? "Caught exception Retrieving data." : "Caught exception creating load query.";

            myException = new CentrifugeException(myNotation, myError);
        }
        return myException;
    }

    public static ValuePair<ResultSet, Exception> callProcedure(Connection connectionIn, PreparedStatement statementIn) {

        return launchRequest(connectionIn, statementIn, Mode.CALL);
    }

    public static ValuePair<ResultSet, Exception> loadTable(Connection connectionIn, PreparedStatement statementIn) {

        return launchRequest(connectionIn, statementIn, Mode.LOAD);
    }

    public static ValuePair<ResultSet, Exception> getData(Connection connectionIn, PreparedStatement statementIn) {

        return launchRequest(connectionIn, statementIn, Mode.RETRIEVE);
    }

    private static ValuePair<ResultSet, Exception> launchRequest(Connection connectionIn,
                                                                 PreparedStatement statementIn, Mode modeIn) {

        BackgroundQuery myRequest = new BackgroundQuery(connectionIn, statementIn, modeIn);
        Exception myException = null;
        boolean myFinishedFlag = false;
        int myCancelCount = 3;

        try {

            myRequest.start();
            while (!myFinishedFlag) {

                try {

                    myRequest.join();
                    myFinishedFlag = true;

                } catch (InterruptedException myInterrupt) {

                    myFinishedFlag = !myRequest.isAlive();
                }
            }
            myException = myRequest.getException();

        } catch (TaskCancelledException myCancel) {

            myException = myCancel;
            myRequest.cancel = true;
            while (0 < myCancelCount--) {

                try {

                    if (myRequest.isAlive()) {

                        statementIn.cancel();

                    } else {

                        break;
                    }

                } catch (Exception IGNORE) { }
            }
        }
        return new ValuePair<>(myRequest.getResults(), myException);
    }

    @Override
   public void run() {

        _results = null;
        _exception = null;
        cancel = false;

        if (!cancel) {

            try {

                switch (_mode) {

                    case CALL:

                        if (_statement.execute() && (!cancel)) {

                            _results = _statement.getResultSet();
                        }
                        break;

                    case LOAD:

                        _statement.executeUpdate();
                        if (!cancel) {

                            _connection.commit();
                        }
                        break;

                    case RETRIEVE:

                        _results = _statement.executeQuery();
                        break;
                }

            } catch (Exception myException) {

                _exception = myException;
            }
        }
        if (cancel) {

            _exception = new TaskCancelledException();
        }
        if (null != _exception) {

            SqlUtil.quietRollback(_connection);
        }
    }

   public ResultSet getResults() {
      return _results;
   }

    public Exception getException() {

        return _exception;
    }
}
