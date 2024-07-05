package test;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.iapi.jdbc.DRDAServerStarter;

public class NetworkListenerInitializer
        implements ServletContextListener
{
    /*
     * This creates a Derby network listener for receiving external JDBC connections.
     * 
     * To enable this a listener entry needs to be added into the web.xml deployment descriptor
     * for this application.
     */

    private NetworkServerControl control;

    private DRDAServerStarter server;

    public void contextInitialized( ServletContextEvent event )
    {
        try {
            System.out.println( "\nStarting network control localhost@1527" );
            control = new NetworkServerControl( InetAddress.getByName("localhost"), 1527 );
            // control.start( new PrintWriter( System.out ) );
            server = new DRDAServerStarter();
            server.setStartInfo( InetAddress.getByName("localhost"), 1527, new PrintWriter( System.out ) );
            server.boot( true, null );

        } catch( UnknownHostException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        } catch( Exception e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }

        boolean started = false;

        while( !started ) {
            try {
                Thread.sleep( 500 );
                control.ping();
                started = true;
                System.out.println( "\n\tDerby Server started and running" );
            } catch( InterruptedException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch( Exception e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            Class.forName( "org.apache.derby.jdbc.ClientDriver" ).newInstance();
            Connection connection = DriverManager.getConnection( "jdbc:derby://localhost:1527/AdminDatabase" );
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables( null, null, "DataViews", null );
            while( tables.next() ) {
                System.out.println( tables.getString( 0 ) );
            }
        } catch( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( InstantiationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( ClassNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void contextDestroyed( ServletContextEvent event )
    {
        // try {
        // control.shutdown();
        // } catch( Exception e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        server.stop();

    }

}
