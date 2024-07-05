package csi.integration.log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: cristina.nuna
 * Date: 4/13/11
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class SQLUtil {

    // method from centrifuge SQLUtil
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

    public static long getNumberOfRows(String driver, String url, String user, String password) {
        long initialSize = 0;
        Connection connection = null;
        try {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            }
            connection = DriverManager.getConnection(url, user, password);
            if (connection != null) {
                PreparedStatement stmt = connection.prepareStatement("Select count(*) from logs");
                ResultSet r = stmt.executeQuery();
                while (SQLUtil.hasMoreRows(r)) {
                    initialSize = r.getBigDecimal(1).longValue();
                }
            }
        } catch (SQLException e) {
            // table could not even exist
            return initialSize;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return initialSize;
    }

}
