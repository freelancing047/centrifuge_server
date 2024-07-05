package csi.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlUtil {
   private static final Logger LOG = LogManager.getLogger(SqlUtil.class);

    /**
     * Quote a field name, if it is not already quoted, for use in SQL statement.
     *
     * @param fieldName
     * @return quoted fieldName
     */
   public static String quote(final String fieldName) {
      String result = "\"\"";

      if ((fieldName != null) && (fieldName.length() > 0)) {
         char[] chars = fieldName.trim().toCharArray();
         char ch = chars[0];

         if ((ch == '"') || (ch == '\'')) {
            result = fieldName;
         } else {
            char[] newChars = new char[chars.length + 2];
            newChars[0] = '"';
            newChars[newChars.length - 1] = '"';

            System.arraycopy(chars, 0, newChars, 1, chars.length);

            result = new String(newChars);
         }
      }
      return result;
   }

    public static String singleQuoteWithEscape(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    public static String quoteString(String value, char quote) {
        if ((value == null) || (value.length() == 0)) {
           LOG.info("Quoting an empty or invalid string.");
            return "";
        }

        StringBuilder builder = new StringBuilder(value.length() + 2);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == quote) {
                builder.append(quote);
            }

            builder.append(c);
        }

        if (builder.charAt(0) != quote) {
            builder.insert(0, quote);
            builder.append(quote);
        }

        return builder.toString();
    }

    public static Connection quietCloseConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public static ResultSet quietCloseResulSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public static Statement quietCloseStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public static List<ResultSet> quietCloseResulSetList(List<ResultSet> results) {
       if ((results != null) && !results.isEmpty()) {
          for (ResultSet myResultSet : results) {
             quietCloseResulSet(myResultSet);
          }
       }
       return new ArrayList<ResultSet>();
    }

    public static void quietRollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                // ignore
            }
        }
    }

    public static boolean hasMoreRows(ResultSet rs) throws SQLException {
        boolean flag = rs.next();
        if (!flag) {
            // eval excel driver may lie so we have to ask twice;
            try {
                flag = rs.next();
            } catch (Throwable t) {
                // eat this exception
                // and return false
                return false;
            }
        }

        return flag;
    }

    public static List<String> getListFromResults(ResultSet set) throws SQLException {
        List<String> values = new LinkedList<String>();
        while (set.next()) {
            String value = set.getString(1);
            values.add(value);
        }

        return values;
    }

    public static boolean commitAndClose(Connection conn) {

        try {
            if (conn.isClosed()) {
                return true;
            }

            if (!conn.getAutoCommit()) {
                conn.commit();
            }

            conn.close();
        } catch (SQLException sqle) {
            if (LOG.isTraceEnabled()) {
                try {
                    String location = conn.getMetaData().getURL();
                    LOG.trace("Failed cleaning up JDBC connection : " + location, sqle);
                } catch (SQLException e) {
                    // really up the creek aren't we?
                }
            }

            return false;
        }

        return true;

    }

    public static String getColumnName(ResultSetMetaData rsMeta, int idx) throws SQLException {
        String colname = rsMeta.getColumnName(idx);
        String labelname = rsMeta.getColumnLabel(idx);

        if (labelname != null) {
            return labelname;
        }
        return colname;

    }
}
