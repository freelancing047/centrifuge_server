package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.csi.jdbc.factory.CsiConnectionFactoryException;
import com.csi.jdbc.factory.PluggableConnectionFactory;

import csi.server.common.exception.CentrifugeException;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskStatus;
import csi.server.task.api.TaskStatusCode;
import csi.server.ws.filemanager.FileProcessor;

/**
 * A factory for creating PluggableProxyConnection objects.
 */
public class PluggableProxyConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(PluggableProxyConnectionFactory.class);

    /** The pluggable cf. */
    private PluggableConnectionFactory pluggableCF = null;

	//Cache created connections by the TaskContext that creates them. Note, the
    //TaskContext is used instead of the TaskId to prevent to TaskContext from being
    //garbage-collected before the reaper can access it.
    private static ConcurrentMap<TaskContext, Connection> connectionsByTask = null;
    //Reaper thread to manage the cache of connections. See inner class below for
    //more information.
    private static PluggableProxyConnectionFactoryReaper reaper = null;


    public PluggableProxyConnectionFactory() {
    	connectionsByTask = new ConcurrentHashMap<TaskContext, Connection>();
    }
    /**
     * Gets the pluggable cf.
     *
     * @return the pluggable cf
     * @throws CentrifugeException the centrifuge exception
     */
    private synchronized PluggableConnectionFactory getPluggableCF() throws CentrifugeException {
    	if (pluggableCF == null) {
	    	try {
	    		Class<?> facClass =  Class.forName(getDriverClass());
	    		pluggableCF = (PluggableConnectionFactory) facClass.newInstance();
	    	}

	    	catch (Throwable e) {
	    		throw new CentrifugeException("Pluggable Connection Factory initialization failed.", e);
	    	}
	    	reaper = PluggableProxyConnectionFactoryReaper.getInstance();
	    	reaper.setFactory(pluggableCF);
	    	reaper.start();
    	}
    	return pluggableCF;
    }

    /**
     * Get a connection for the indicated URL (connection string). If the
     * attempt causes an SQLException, then check the error code and SQL state
     * to see whether the problem is missing or incorrect user/password. If this
     * is the problem, then throw GeneralSecurityException.
     *
     * @param url the url
     * @param props the props
     * @return a DB connection.
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException the sQL exception
     * @throws GeneralSecurityException the general security exception
     * @throws CentrifugeException the centrifuge exception
     */
    public Connection getConnection(String url, Properties props) throws ClassNotFoundException, SQLException, GeneralSecurityException, CentrifugeException {

        Connection connection;

        try {
            synchronized(this) {
            	PluggableConnectionFactory factory = getPluggableCF();
            	connection = factory.getConnection(decodeProperties(props));
        		//Cache the connection by the TaskContext.
            	TaskContext context = TaskHelper.getCurrentContext();
            	connectionsByTask.put(context, connection);
            	factory.registerTaskContext(connection, context.getStatus().getTaskStatus().name());
            }
        }
        catch (CsiConnectionFactoryException sqle) {
        	LOG.error(sqle);
        	throw new CentrifugeException("Pluggable Connection Factory encountered an exception: ", sqle);
        }
        catch (Throwable exception) {
        	LOG.error(exception);
        	throw new CentrifugeException("Pluggable Connection Factory exception: ", exception);
        }

        return connection;
    }

    /* This method should only be invoked if the connection type
     * is NOT "Legacy"
     */
    /* (non-Javadoc)
     * @see csi.server.connector.jdbc.JdbcConnectionFactory#getConnection(csi.server.common.model.GenericProperties)
     */
    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
        Connection connection;
        try {
        	synchronized(this) {
            	PluggableConnectionFactory factory = getPluggableCF();
                connection = factory.getConnection(decodeProperties(toNativeProperties(propMap)));
        		//Cache the connection by the TaskContext.
            	TaskContext context = TaskHelper.getCurrentContext();
            	connectionsByTask.put(context, connection);
            	factory.registerTaskContext(connection, context.getStatus().getTaskStatus().name());
        	}
        }
        catch (CsiConnectionFactoryException exception) {
        	LOG.error(exception);
        	throw new CentrifugeException("Pluggable Connection Factory encountered an exception: ", exception);
        }
        catch (Throwable ee) {
        	LOG.error(ee);
        	throw new CentrifugeException("Pluggable Connection Factory exception: ", ee);
        }
        return connection;
    }

    /**
     * Decode properties.
     *
     * @param props the props
     * @return the properties
     */
    private Properties decodeProperties(Properties props) {
    	Properties newprops = new Properties();

        Object[] set = props.stringPropertyNames().toArray();
        for (Object key : set) {
            String value = props.getProperty((String)key);
            if (!isSpecialKey((String)key, value, newprops)) {
            	newprops.put(key, value);
            }
        }

    	return newprops;
    }

    /**
     * Checks if is special key.
     *
     * @param key the key
     * @param value the value
     * @param props the props
     * @return true, if is special key
     */
    private boolean isSpecialKey(String key, String value, Properties props) {

    	boolean rc = false;

    	int idx = key.lastIndexOf(".filetoken");
    	if (idx > -1) {
    		value = value.replace(" ", "+");
     		value = FileProcessor.decodeToken(value);
    		props.put(key, value);
    		rc = true;
    	}

   	return rc;
    }

    /**
     * This class is a daemon thread that will monitor all active tasks that use
     * a connection created through this PluggableProxyConnectionFactory. The
     * PluggableConnectionFactory keeps a cache of each connection and the associated
     * task so that the connector can determine if the task has been canceled or has
     * terminated in error. This allows the connector code to interrupt its processing if the
     * associated task is canceled.
     *
     * This class was created after it was determined that canceling a task did not
     * interrupt any activity of the connector. Specifically, the DataCacheHelper creates
     * a PGRawDataProducer thread to cache data from the ResultSet. If the connector is
     * streaming data to the ResultSet, and is waiting for the DataSource, that thread
     * cannot be canceled. Since that thread is not a daemon thread, the server will not
     * shut down properly.
     *
     * @author Pat Hayes
     *
     */
    private static class PluggableProxyConnectionFactoryReaper  extends Thread {

    	private static PluggableProxyConnectionFactoryReaper instance = new PluggableProxyConnectionFactoryReaper();
    	private static PluggableConnectionFactory factory = null;

    	public PluggableProxyConnectionFactoryReaper() {
    		this.setName("PluggableProxyConnectionFactoryReaper");
    		this.setDaemon(true);
    	}

    	public static PluggableProxyConnectionFactoryReaper getInstance() {
    		return instance;
    	}

		public void setFactory(PluggableConnectionFactory factory) {
			PluggableProxyConnectionFactoryReaper.factory = factory;
		}

		/**
         * @see java.lang.Thread#run()
         */
    	public void run() {
    		while (true) {
    			for (Map.Entry<TaskContext,Connection> entry : connectionsByTask.entrySet()) {
    			  TaskContext taskContext = entry.getKey();
    				Connection conn = entry.getValue();
		        	try {
		        		//If the connection has already been closed, we don't care anymore
		        		if (conn.isClosed()) {
			        		connectionsByTask.remove(taskContext);
			            	factory.updateTaskStatus(conn, TaskStatusCode.TASK_STATUS_COMPLETE.name());
			        		continue;
	    				}

		        		TaskStatus status = taskContext.getStatus();
	    		        switch (status.getTaskStatus()) {
		    		        case TASK_STATUS_COMPLETE:
				        		connectionsByTask.remove(taskContext);
				            	factory.unregisterTaskContext(conn);
		    		        	break;
		    		        case TASK_STATUS_CANCELED:
		    		        case TASK_STATUS_ERROR:
				            	factory.updateTaskStatus(conn, status.getTaskStatus().name());
				        		connectionsByTask.remove(taskContext);
		    		        	break;
				        	default:
		    		        	break;
	    		        }
		        	} catch (SQLException se) {
		        		LOG.error("SQLException caught when attempting to close a connection: " + se.getLocalizedMessage());
		        	}
	    		}
    			try {
    				//Wait for 10 seconds. Do we want to make this configurable?
    				Thread.sleep(10000);
    			} catch (InterruptedException ie) {
    				return;
    			}
	    	}
	    }
    }

}
