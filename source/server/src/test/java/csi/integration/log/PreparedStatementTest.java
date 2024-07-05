package csi.integration.log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

/**
 * Tests different inserts with prepared statement vs statement.
 */
public class PreparedStatementTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "adminadmin";
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    private Connection connection = null;

    class PreparedStatementProducer extends Thread {

        public static final int NR_OF_LOGS = 10;

        public void run() {
            System.out.println("producer " + this.getName() + " has started.");
            try {
                long start = System.currentTimeMillis();
                for (int i = 0; i < NR_OF_LOGS; i++) {
                    testConnectionWithPreparedStatement();
                }
                System.out.println("**** Average time for insert in db " + ((double) (System.currentTimeMillis() - start)) / (NR_OF_LOGS * NR_OF_LOGS * 500) + " ms");
                start = System.currentTimeMillis();
                for (int i = 0; i < NR_OF_LOGS; i++) {
                    testConnectionWithStatement();
                }
                System.out.println("**** Average time for insert in db " + ((double) (System.currentTimeMillis() - start)) / (NR_OF_LOGS * NR_OF_LOGS * 500) + " ms");
            } catch (SQLException e) {
                e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
                }
            }
            System.out.println("producer " + this.getName() + " has finished job!");
        }

        public void testConnectionWithPreparedStatement() throws SQLException {
            long start = System.currentTimeMillis();
            connection = getConnection();
            PreparedStatement stmt = connection
                    .prepareStatement("Insert into logs (priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message)"
                            + " values (?,?,?,?,?,?,?,?,?,?)");
            for (int k = 0; k < NR_OF_LOGS; k++) {
                for (int i = 0; i < 500; i++) {
                    stmt.setString(1, "INFO");
                    stmt.setString(2, this.getName());
                    stmt.setString(3, "location info");
                    stmt.setString(4, "");
                    stmt.setString(5, null);
                    stmt.setString(6, "action");
                    stmt.setObject(7, null);
                    stmt.setString(8, "user name");
                    stmt.setString(9, "session");
                    stmt.setString(10, "message x");
                    stmt.addBatch();
                }
                stmt.executeBatch();
                connection.commit();
            }
            stmt.close();
            System.out.println("**** Time for insert prepared statement in db " + (System.currentTimeMillis() - start));
        }

        public void testConnectionWithStatement() throws SQLException {
            long start = System.currentTimeMillis();
            connection = getConnection();
            Statement stmt = connection.createStatement();
            for (int k = 0; k < NR_OF_LOGS; k++) {
                for (int i = 0; i < 500; i++) {
                    String sql = "Insert into logs (priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message)"
                            + " values ('INFO','" + this.getName() + "', 'location info', 'app id', 'ip server', 'action', 'client ip', 'user name', 'session id', 'message')";
                    stmt.execute(sql);
                }
                connection.commit();
            }
            stmt.close();
            connection.close();
            System.out.println("**** Time for insert statement in db " + (System.currentTimeMillis() - start));
        }
    }

    protected Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Error while loading driver!");
        }
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            connection.setAutoCommit(false);
        }
        return connection;
    }

    @Test
    public void testStatementVsPreparedStatement() {
        PreparedStatementProducer producer = new PreparedStatementProducer();
        producer.start();
        try {
            producer.join();
        } catch (InterruptedException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

}
