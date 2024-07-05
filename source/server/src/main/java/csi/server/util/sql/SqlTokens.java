package csi.server.util.sql;

/**
 * Created by centrifuge on 8/21/2018.
 */
public class SqlTokens {
   public static final String NULL_STRING = "$$NULL$$";

   public static final String TIME_STAMP_ESCAPE = "{ts '_'}";
   public static final String DATE_ESCAPE = "{d '_'}";
   public static final String TIME_ESCAPE = "{t '_'}";

   public static final String AND_TOKEN = " AND ";
   public static final String AS_PARAMETER_ID_COMMA = " AS param_id, ";
   public static final String AS_TOKEN = " AS ";
   public static final String CLOSE_PAREN_SPACE = ") ";
   public static final String CLOSE_PAREN_TERMINATE = ");";
   public static final String CLOSE_WRAPPER = ") w";
   public static final String COMMA_SPACE = ", ";
   public static final String EMPTY_STRING = "";
   public static final String EQUAL_TOKEN = " = ";
   public static final String FROM_PAREN = " FROM (";
   public static final String FROM_TOKEN = " FROM ";
   public static final String INNER_JOIN_TOKEN = " INNER JOIN ";
   public static final String LEFT_JOIN_TOKEN = " LEFT OUTER JOIN ";
   public static final String NOT_PAREN = " NOT (";
   public static final String NULL_INDICATOR = "NULL";
   public static final String ON_TOKEN = " ON ";
   public static final String OPEN_WRAPPER = "SELECT * FROM (";
   public static final String OR_TOKEN = " OR ";
   public static final String PARAMETER_ID = "param_id";
   public static final String RIGHT_JOIN_TOKEN = " RIGHT OUTER JOIN ";
   public static final String SELECT_ALL_FROM = "SELECT * FROM ";
   public static final String SELECT_DISTINCT_FROM = "SELECT DISTINCT * FROM ";
   public static final String SELECT_DISTINCT_TOKEN = "SELECT DISTINCT ";
   public static final String SELECT_TOKEN = "SELECT ";
   public static final String SQL_WILD_CARD = "%";
   public static final String UNION_ALL_TOKEN = " UNION ALL ";
   public static final String UNION_TOKEN = " UNION ";
   public static final String WHERE_TOKEN = " WHERE ";

   public static final char DOT = '.';
   public static final char OPEN_PAREN = '(';
   public static final char CLOSE_PAREN = ')';
   public static final char STAR = '*';

   public static int SEMI_COLON = ';';
}
