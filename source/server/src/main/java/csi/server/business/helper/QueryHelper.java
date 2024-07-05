package csi.server.business.helper;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;
import csi.server.connector.AbstractConnectionFactory;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.rest.RestServiceConnection;
import csi.server.connector.webservice.WebServiceConnection;
import csi.server.connector.webservice.salesforce.SalesForceConnection;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskCancelledException;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.ws.filemanager.FileProcessor;

public class QueryHelper {
   private static final Logger LOG = LogManager.getLogger(QueryHelper.class);

   public static final int SQL_MAX_LENGTH = 64 * 1024;
   public static final int SQL_STORAGE_MAX_LENGTH = 2147483647;
   public static final Pattern SQL_BREAK_PATTERN = Pattern.compile("-- sql_break --");
   public static final Pattern XML_COMMENT_PATTERN = Pattern.compile("<!--.*-->");

   // this should be moved to oracle specific factory
   public static final int ORACLE_TYPES_CURSOR = -10;

   private static final String V1_QUERY_PARAM_NAME_REGEX = "(?i)\\?PARAM\\s*\\(.*?,.*?,\\s*[\"']?(.*?)[\"']?\\s*,.*?\\)";
   private static final Pattern V1_QUERY_PARAM_PATTERN = Pattern.compile(V1_QUERY_PARAM_NAME_REGEX);

   public static final Pattern PARAM_NAME_PATTERN_FORMAT = Pattern.compile("(?i)[\"']?\\{:(.*?)\\}[\"']?");

   public static final Pattern IN_CLAUSE_PATTERN = Pattern.compile("(?i)\\bIN\\W*\\(\\W*(\\{:(.*?)\\})\\W*\\)");

   public static final Pattern CALL_STATEMENT_PATTERN = Pattern.compile("(?i)^\\s*call\\s.*");

   private static final String QUERY_LOG_FORMAT = "Executing Query: User=%s; Query=%s";

   private static long executeCount(final Connection conn, final String sql) {
      long rowCount = 0L;

      try (PreparedStatement statement = conn.prepareStatement(sql);
           ResultSet results = statement.executeQuery()) {
         if (results.next()) {
            rowCount = results.getLong(1);
         }
      } catch (Exception myException) {
         LOG.error("Error counting rows: " + Format.value(myException));
      }
      return rowCount;
   }

   public static long countRows(Connection conn, String databaseName, String schema, String tableName) {
      return executeCount(conn,
                          new StringBuilder("SELECT COUNT(*) FROM ").append(Format.value(databaseName)).append(".")
                                                                    .append(Format.value(schema)).append(".")
                                                                    .append(Format.value(tableName)).toString());
   }

   public static long countRows(Connection conn, String tableName) {
      return executeCount(conn,
                          new StringBuilder("SELECT COUNT(*) FROM ").append(Format.value(tableName)).toString());
   }

   public static void setBaseIdValue(Connection conn, String databaseName, String tableName, long value) {
      String sql = new StringBuilder("ALTER SEQUENCE ").append(Format.value(CacheUtil.getGeneratorNameForCacheTable(tableName)))
                             .append(" START WITH ").append(value)
                             .append(" RESTART").toString();

      try (Statement statement = conn.createStatement()) {
         statement.execute(sql);
      } catch (Exception exception) {
      }
   }

   public static void executeSQL(Connection conn, String sql, List<QueryParameterDef> params)
         throws CentrifugeException {
      if (StringUtils.isNotBlank(sql)) {
         String[] queries = SQL_BREAK_PATTERN.split(sql);

         for (String query : queries) {
            if (StringUtils.isNotBlank(query)) {
               try (Statement statement = conn.createStatement()) {
                  executeCommand(statement, expandParams(query, params));
               } catch (SQLException e) {
                  throw new CentrifugeException("Failed to execute sql query: " + query, e);
               }
            }
         }
      }
   }

   public static ResultSet executeStatement(Statement statement, String query, String logMessage) throws SQLException {
      logQueryInfo(query, null, logMessage);

      try {
         return statement.executeQuery(query);
      } catch (TaskCancelledException myException) {
         try { statement.cancel(); } catch (Exception IGNORE) { }
         throw myException;
      }
   }

   public static ResultSet executeStatement(Statement statement, String query) throws SQLException {
      return executeStatement(statement, query, null);
   }

   public static boolean executeCommand(Statement statement, String command, String logMessage) throws SQLException {
      logQueryInfo(command, null, logMessage);

      try {
         return statement.execute(command);
      } catch (TaskCancelledException myException) {
         try { statement.cancel(); } catch (Exception IGNORE) { }
         throw myException;
      }
   }

   public static boolean executeCommand(Statement statementIn, String commandIn) throws SQLException {
      return executeCommand(statementIn, commandIn, null);
   }

   public static ResultSet executeSingleQuery(Connection conn, String query, List<QueryParameterDef> params,
                                              int fetchSize, int maxRows) throws CentrifugeException {
      ResultSet results = null;
      // hack till ui gets parameter editor
      String parameterizedQuery = migrateQuery(query);
      //List<QueryParameterDef> preprocessingParams = getPreprocessingParams(parameterizedQuery, params, true);
      String sql = expandParams(parameterizedQuery, params);

      logQueryInfo(sql, params);

      if (conn instanceof WebServiceConnection) {
         results = executeWebService(conn, params, sql);
      } else if (conn instanceof RestServiceConnection) {
         results = executeRestWebService(conn, params, sql);
      } else if (conn instanceof SalesForceConnection) {
         try {
            results = ((SalesForceConnection) conn).execute("??", params, sql);
         } catch (Throwable e) {
            throw new CentrifugeException(e);
         }
      } else {
         if (CALL_STATEMENT_PATTERN.matcher(sql).matches()) {
            results = executeStoreProc(conn, sql, params);
         } else {
            results = executeSqlQuery(conn, sql, params, fetchSize, maxRows);
         }
      }
      return results;
   }

   public static ResultSet executeSingleQuery(Connection conn, String sql, List<QueryParameterDef> params, int fetchSize)
         throws CentrifugeException {
      return executeSingleQuery(conn, sql, params, fetchSize, -1);
   }

   public static ResultSet executeSingleQuery(Connection conn, String sql, List<QueryParameterDef> params)
         throws CentrifugeException {
      return executeSingleQuery(conn, sql, null, params);
   }

   public static ResultSet executeSingleQuerySized(Connection conn, String sql, int maxRows)
         throws CentrifugeException {
      DBConfig dbConfig = Configuration.getInstance().getDbConfig();

      return executeSingleQuery(conn, sql, null, dbConfig.getRecordFetchSize(), maxRows);
   }

   public static ResultSet executeSingleQuery(Connection conn, String sql, Integer rowLimit, List<QueryParameterDef> params)
         throws CentrifugeException {
      DBConfig dbConfig = Configuration.getInstance().getDbConfig();

      return executeSingleQuery(conn, sql, params, dbConfig.getRecordFetchSize(),
                                (rowLimit == null) ? -1 : rowLimit.intValue());
   }

   public static ResultSet executeSingleQuerySized(Connection conn, String sql, List<QueryParameterDef> params, int maxRows)
         throws CentrifugeException {
      DBConfig dbConfig = Configuration.getInstance().getDbConfig();

      return executeSingleQuery(conn, sql, params, dbConfig.getRecordFetchSize(), maxRows);
   }

    public static String preProcessSql(ConnectionDef dsDef, QueryDef qdef) throws CentrifugeException {
        String sql = qdef.getQueryText();
        boolean validSql = (sql != null) && !sql.trim().isEmpty();

        // TODO: fix me. The ui should be using query.tableName for all
        // conntypes
        // instead of different key for different conntypes.
        // Basically this whole thing is a kludge and needs to be refactor
        // when we have more time.
        if (JdbcDriverType.CUSTOM.equals(dsDef.getType()) && !validSql) {
            throw new CentrifugeException("Missing sql query");
        } else if (!validSql) {
            ConnectionFactory factory = ConnectionFactoryManager.getInstance().getFactoryForType(dsDef.getType());

            Map<String, String> propMap = qdef.getProperties().getPropertiesMap();
            String tablename = propMap.get(AbstractConnectionFactory.CSI_QUERY_TABLE_NAME);
            if (tablename == null) {
                tablename = propMap.get(AbstractConnectionFactory.CSI_QUERY_WORKSHEET);
                if (tablename == null) {
                    String ftok = dsDef.getProperties().getPropertiesMap().get(AbstractConnectionFactory.CSI_FILETOKEN);
                    if (ftok != null) {
                        File fileFromToken = FileProcessor.getFileFromToken(ftok);
                        tablename = factory.getTableNameQualifier() + fileFromToken.getName()
                                + factory.getTableNameQualifier();
                    }
                }
            }

            if (tablename == null) {
                throw new CentrifugeException("Missing table name, worksheet name or query string");
            }
            sql = "select * from " + tablename;
        }
        return sql;
    }

    public static void logQueryInfo(String sql, List<QueryParameterDef> params) {
        logQueryInfo(sql, params, null);
    }

    private static void logQueryInfo(String sql, List<QueryParameterDef> params, String logMessage) {
        Authorization auth = CsiSecurityManager.getAuthorization();
        String uname = null;
        if (auth != null) {
            uname = auth.getName();
        }

        String expandedSql = expandParams(sql, params);

        String logmsg = String.format(QUERY_LOG_FORMAT, uname, expandedSql);
        LOG.debug(logmsg);
        if (LOG.isTraceEnabled()) {
            if (logMessage != null) {
                LOG.trace("Trace Message: " + logMessage);
            }
            LOG.trace("Previous query partial stack trace:");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            LOG.trace(stackTraceElementToString(stackTrace[7]));
            LOG.trace(stackTraceElementToString(stackTrace[6]));
            LOG.trace(stackTraceElementToString(stackTrace[5]));
            LOG.trace(stackTraceElementToString(stackTrace[4]));
        }
    }

    private static String stackTraceElementToString(StackTraceElement ste) {
        return "- " + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber();
    }

   public static String expandParams(String sql, List<QueryParameterDef> params) {
      String expanded = sql;

      if (params != null) {
         for (QueryParameterDef param : params) {
            String value = (param.getValue() == null) ? (param.getDefaultValue() == null) ? "null" : param.getDefaultValue() : param.getValue();
            expanded = expanded.replace("{:" + param.getName() + "}", value);
         }
      }
      return expanded;
   }

   public static ResultSet executeStoreProc(Connection conn, String queryText, List<QueryParameterDef> params)
         throws CentrifugeException {
        TaskHelper.reportTaskID();
        String prepareSql = makePrepareStr(queryText);
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall(prepareSql);
            applyParameters(stmt, queryText, params);

            TaskHelper.checkForCancel();
            boolean hasResult = stmt.execute();

            return (hasResult) ? stmt.getResultSet() : null;

        } catch (SQLException e) {
            throw new CentrifugeException("Failed to execute stored procedure: " + queryText, e);

        } catch (TaskCancelledException tce) {
            try {
               if (stmt != null) {
                  stmt.cancel();
               }
                conn.rollback();
            } catch (Exception IGNORE) {}
            throw tce;
        }
    }

    public static int executeSqlUpdate(Connection conn, String queryText, List<QueryParameterDef> params)
            throws CentrifugeException {
        TaskHelper.reportTaskID();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        String prepareSql = makePrepareStr(queryText);

        PreparedStatement stmt = null;
        try {
            DBConfig dbConfig = Configuration.getInstance().getDbConfig();
            stmt = conn.prepareStatement(prepareSql);
            try {
                stmt.setFetchSize(dbConfig.getRecordFetchSize());
            } catch (SQLException e) {
                LOG.warn("SetFetchSize() method not supported.");
            }
            applyParameters(stmt, queryText, params);
            TaskHelper.checkForCancel();
            try {

                return stmt.executeUpdate();

            } catch (SQLException ignoreFirstFailure) {

                conn.rollback();
                return stmt.executeUpdate();
            }

        } catch (TaskCancelledException myException) {

            try {
               if (stmt != null) {
                  stmt.cancel();
               }
                conn.rollback();
            } catch (Exception IGNORE) {}
            throw myException;

        } catch (SQLException e) {
            throw new CentrifugeException("Failed to execute sql update: " + queryText, e);
        } finally {
           stopWatch.stop();
           LOG.info("Time to execute update: " + stopWatch.getTime());

           if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String migrateQuery(String v1Query) {
        String parameterizedQuery = v1Query;
        Matcher m = V1_QUERY_PARAM_PATTERN.matcher(v1Query);
        int pos = 0;
        while (m.find()) {
            String replacement = m.group(1);
            if ((replacement == null) || replacement.trim().isEmpty()) {
                replacement = "PARAM_" + pos;
            }
            parameterizedQuery = parameterizedQuery.replace(m.group(), "{:" + replacement + "}");
            pos++;
        }
        return parameterizedQuery;
    }

    public static String makePrepareStr(String queryText) {
        Matcher matcher = PARAM_NAME_PATTERN_FORMAT.matcher(queryText);
        return matcher.replaceAll("\\?");
    }

    public static ResultSet executeSqlQuery(Connection conn, String queryText, List<QueryParameterDef> params,
                                            int fetchSize, int maxRows) throws CentrifugeException {
        TaskHelper.reportTaskID();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        PreparedStatement stmt = null;

        try {
            int myFetchSize = (0 <= maxRows) ? Math.min(fetchSize, maxRows) : fetchSize;
            String prepareSql = makePrepareStr(queryText);
            stmt = conn.prepareStatement(prepareSql);
            // if connection is read only, applying params can mess things up.
            if ((params != null) && !params.isEmpty()) {
                applyParameters(stmt, queryText, params);
            }
            // hack to force mysql driver to stream data instead of loading all rows into memory
            // since it doesn't respect setFetchSize()
            try {
                if (conn.getMetaData().getDatabaseProductName().equalsIgnoreCase("MySQL")) {
                    stmt.setFetchSize(Integer.MIN_VALUE);
                } else {
                    stmt.setFetchSize(myFetchSize);
                }
            } catch (SQLException e) {
                LOG.warn("SetFetchSize() method not supported.");
            }

            if (maxRows != -1) {
                try {
                    stmt.setMaxRows(maxRows);
                } catch (SQLException e) {
                    LOG.warn("SetMaxRows(i) method not supported.");
                }
            }

            TaskHelper.checkForCancel();
            try {

                return stmt.executeQuery();

            } catch (SQLException ignoreFirstFailure) {

                conn.rollback();
                return stmt.executeQuery();
            }

        } catch (SQLException e) {
            throw new CentrifugeException("Failed to execute sql query: " + queryText, e);

        } catch (TaskCancelledException myException) {

            try {
               if (stmt != null) {
                  stmt.cancel();
               }
                conn.rollback();
            } catch (Exception IGNORE) {}
            throw myException;

        } catch (java.util.MissingResourceException e){

            LOG.error(e.toString());
            return null;
        } finally {
           stopWatch.stop();
           LOG.debug("Time to execute query: " + stopWatch.getTime());
        }
    }

    public static ResultSet executeSqlQuery(Connection conn, String queryText, List<QueryParameterDef> params,
                                            int fetchSize) throws CentrifugeException {
        return executeSqlQuery(conn, queryText, params, fetchSize, -1);
    }

   public static ResultSet executeWebService(Connection conn, List<QueryParameterDef> params, String sql)
         throws CentrifugeException {
      ResultSet result = null;
      String usingSQL = XML_COMMENT_PATTERN.matcher(sql).replaceAll("");
      int anchor = usingSQL.indexOf('?');
      String operationName = usingSQL.substring(0, anchor);
      String recordQuery = usingSQL.substring(anchor + 1);

      try {
         result = ((WebServiceConnection) conn).execute(operationName, params, recordQuery);
      } catch (Throwable t) {
         throw new CentrifugeException("Failed to execute web service", t);
      }
      return result;
   }

   public static ResultSet executeRestWebService(Connection conn, List<QueryParameterDef> params, String sql)
         throws CentrifugeException {
      ResultSet result = null;
      int pcnt = 0;

      if (params != null) {
         pcnt = params.size();
      }
      LOG.debug("QueryHelper: executeRestWebService - sql={}" + sql + " params={}" + pcnt);
      String operationName = "fetch";
      String recordQuery = sql;

      try {
         result = ((RestServiceConnection) conn).execute(operationName, params, recordQuery);
      } catch (Throwable t) {
         throw new CentrifugeException("Failed to execute REST web service", t);
      }
      return result;
   }

   public static void applyParameters(PreparedStatement stmt, String queryText, List<QueryParameterDef> params)
         throws SQLException {
      if ((params != null) && !params.isEmpty()) {
         Matcher matcher = PARAM_NAME_PATTERN_FORMAT.matcher(queryText);

        // make sure we're not dealing with a query string
        // that has already been substituted with values. this ensures
        // that we're not subject to bad JDBC drivers not implementing
        // parameter meta calls...
        if (matcher.find()) {
           // re-init our pattern for searches then...really crappy isn't it.
           matcher = PARAM_NAME_PATTERN_FORMAT.matcher(queryText);

           // TODO: extract getting the params by name map out to util method
           Map<String, QueryParameterDef> nameMap = new HashMap<String, QueryParameterDef>();

           for (QueryParameterDef qp : params) {
              if (qp.getName() != null) {
                 nameMap.put(qp.getName().toLowerCase(), qp);
              }
           }
           ParameterMetaData parameterMD = stmt.getParameterMetaData();
           int i = 0;

           while (matcher.find()) {
              i++;
              String name = matcher.group(1);

              if (name != null) {
                 QueryParameterDef parm = nameMap.get(name.toLowerCase());
                 String value = parm.getValue();
                 int paramType = parameterMD.getParameterType(i);
                 String paramTypeName = parameterMD.getParameterTypeName(i);
                 CsiDataType paramCsiType = CacheUtil.resolveCsiType(paramTypeName, paramType, null);
                 Object coercedValue = CsiTypeUtil.coerceType(value, paramCsiType, null);

                 if (CsiDataType.String == paramCsiType) {
                    stmt.setString(i, value);
                 } else {
                    if (coercedValue != null) {
                       stmt.setObject(i, coercedValue, paramType);
                    } else {
                        // bets are off. Postgres JDBC driver doesn't quite do the right thing.
                        // but in the hopes of the driver getting fixed, we'll pass the
                        // string value and the desired type.
                       stmt.setObject(i, value, paramType);
                    }
                 }
              }
           }
        }
      }
   }

    public static boolean hasV1Params(String sql) {
        return (sql.toUpperCase().contains("?PARAM"));
    }

    public static boolean hasV2Params(String sql) {
        Matcher matcher = PARAM_NAME_PATTERN_FORMAT.matcher(sql);
        return matcher.find();
    }

    public static List<String> listParameterNames(String queryText) {
        List<String> paramNames = new ArrayList<String>();

        if ((queryText == null) || queryText.isEmpty()) {
            return paramNames;
        }

        // track lowercase version of names
        // since param names are case insensitive
        List<String> lowerNames = new ArrayList<String>();

        Matcher matcher = QueryHelper.PARAM_NAME_PATTERN_FORMAT.matcher(queryText);

        while (matcher.find()) {
            String name = matcher.group(1);
            String lowerName = name.toLowerCase();
            if (!lowerNames.contains(lowerName)) {
                lowerNames.add(lowerName);
                paramNames.add(name);
            }
        }

        return paramNames;
    }

    public static String genLinkupQuery(String queryIn) {

        String myQuery = queryIn.trim();
        StringBuilder myBuffer = new StringBuilder();
        boolean myTerminated = (';' == myQuery.charAt(myQuery.length() - 1));
        boolean mySingleQuote = false;
        boolean myDoubleQuote = false;
        boolean myWhiteSpace = false;
        boolean myPossibleFlag = false;
        boolean myFoundFlag = false;
        int myIndex = myQuery.lastIndexOf(')') + 1;
        int myLastIndex = 0;
        int mySecondLastIndex = 0;

        while (myQuery.length() > myIndex) {

            char myCharacter = myQuery.charAt(myIndex++);

            if (mySingleQuote) {

                if ('\'' == myCharacter) {

                    mySecondLastIndex = 0;
                    myLastIndex = myIndex;
                    mySingleQuote = false;
                    myWhiteSpace = false;
                    myBuffer.setLength(0);
                }

            } else if (myDoubleQuote) {

                if ('"' == myCharacter) {

                    mySecondLastIndex = 0;
                    myLastIndex = myIndex;
                    myDoubleQuote = false;
                    myWhiteSpace = false;
                    myBuffer.setLength(0);
                }

            }else {

                if (',' == myCharacter) {

                    if (myPossibleFlag) {

                        myFoundFlag = myBuffer.toString().equalsIgnoreCase("BY");
                    }
                    if (!myFoundFlag) {

                        mySecondLastIndex = 0;
                    }
                    myLastIndex = myIndex;
                    myWhiteSpace = false;
                    myPossibleFlag = false;
                    myBuffer.setLength(0);

                } else if ('\'' == myCharacter) {

                    if (myPossibleFlag) {

                        myFoundFlag = myBuffer.toString().equalsIgnoreCase("BY");
                    }
                    if (!myFoundFlag) {

                        mySecondLastIndex = 0;
                    }
                    myLastIndex = myIndex;
                    myWhiteSpace = false;
                    myPossibleFlag = false;
                    mySingleQuote = true;
                    myBuffer.setLength(0);

                } else if ('"' == myCharacter) {

                    if (myPossibleFlag) {

                        myFoundFlag = myBuffer.toString().equalsIgnoreCase("BY");
                    }
                    if (!myFoundFlag) {

                        mySecondLastIndex = 0;
                    }
                    myLastIndex = myIndex;
                    myWhiteSpace = false;
                    myPossibleFlag = false;
                    myDoubleQuote = true;
                    myBuffer.setLength(0);

                } else if (' ' >= myCharacter) {

                    if (!myWhiteSpace) {

                        if (myPossibleFlag) {

                            myFoundFlag = myBuffer.toString().equalsIgnoreCase("BY");
                        }
                        if (!myFoundFlag) {

                            String myTestValue = myBuffer.toString();

                            myPossibleFlag = myTestValue.equalsIgnoreCase("ORDER") || myTestValue.equalsIgnoreCase("SORT");

                            mySecondLastIndex = myPossibleFlag ? myLastIndex : 0;
                            myLastIndex = myIndex - 1;
                        }
                        myBuffer.setLength(0);
                    }
                    myWhiteSpace = true;

                } else {

                    myBuffer.append(myCharacter);
                    myWhiteSpace = false;
                }
            }
            if (myFoundFlag) {

                break;
            }
        }
        if (!myFoundFlag) {

            myFoundFlag = (myPossibleFlag && myBuffer.toString().equalsIgnoreCase("BY"));
        }

        if (myFoundFlag) {

            if (myTerminated) {

                myQuery = myQuery.substring(0, mySecondLastIndex) + ";";

            } else {

                myQuery = myQuery.substring(0, mySecondLastIndex);
            }
        }
        return myQuery;
    }

}
