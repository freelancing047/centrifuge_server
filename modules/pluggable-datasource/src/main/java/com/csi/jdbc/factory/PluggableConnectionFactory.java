/*
 * Copyright Centrifuge Systems, Inc.  2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package com.csi.jdbc.factory;

import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * All PluggableConnectionFactory implementations must extend this class.  The PluggableProxyConnectionFactory
 * class is used to load all implementations and provides the interface mechanisms between the Centrifuge server
 * and the PluggableConnectionFactory implementation.  
 * <br><br>
 * There is single method that the proxy connection factory uses to communicate with the pluggable connection
 * factory, getConnection().  All runtime parameters are marshalled from the Datasource Editor UI and the 
 * centrifuge.xml configuration file and stored in a Properties object as name/value pairs, then passed to the
 * pluggable implementation.  This method returns a JDBC compliant Connection object instance that the Centrifuge 
 * Server uses to obtain data and metadata from the JDBC data source.  
 * <br><br>
 *
 * It has been determined that each Pluggable connector need to monitor the status of each task that uses one of its
 * connections. Therefore, the PluggableProxyConnectionFactory now registers each connection and the initial status
 * of its associated task with the PluggableConnectionFactory. When the PluggableProxyConnectionFactory detects that
 * the task has changed its status, it updates the status cached in the PluggableConnectionFactory. When the task
 * completes successfully, the connection is unregistered. This allows the connectors to monitor the status of the 
 * task for which it is performing work. If the task is canceled or terminated in error, the connector can stop 
 * whatever work it was performing. See the PluggableProxyConnectionFactory for more information.
 *  
 *  
 * 
 */
public abstract class PluggableConnectionFactory  {
    protected static final Logger LOG = LogManager.getLogger(PluggableConnectionFactory.class);

    protected static ConcurrentMap<Connection, PluggableConnectionFactoryTaskContext> taskContextByConnection = 
    			new ConcurrentHashMap<Connection, PluggableConnectionFactoryTaskContext>();
    
    /**
     * Get a JDBC connection from underlying PluggableConnectionFactory implementation.
     *
     * @param props the props
     * @return a DB connection.
     * @throws CsiConnectionFactoryException the csi connection factory exception
     */
    abstract public Connection getConnection(Properties props) throws CsiConnectionFactoryException;
    
    /**
     * Helper method to get a reference to the Centrifuge Server logger.
     *
     * @return the logger
     */
    protected Logger getLogger() {
    	return(LOG);
    }

    /**
     * Register a Connection and the initial status of its associated task
     * @param connection - The Connection object
     * @param status - A String representing the status
     */
    public void registerTaskContext(Connection connection, String status) {
    	PluggableConnectionFactoryTaskContext taskContext = new PluggableConnectionFactoryTaskContext(PluggableConnectionFactoryTaskStatus.valueOf(status));
    	taskContextByConnection.put(connection, taskContext);

	}

    /**
     * Unregister a Connection from the cache
     * @param connection - The Connection object
     */
    public void unregisterTaskContext(Connection connection) {
    	taskContextByConnection.remove(connection);
    }

    /**
     * Update the status of task associated with the specified connection
     * @param connection - The Connection object
     * @param status - A String representing the status
     */
    public void updateTaskStatus(Connection connection, String status) {
    	PluggableConnectionFactoryTaskContext taskContext = taskContextByConnection.get(connection);
    	if (taskContext != null) {
        	PluggableConnectionFactoryTaskStatus taskStatus = PluggableConnectionFactoryTaskStatus.valueOf(status);
        	taskContext.setStatus(taskStatus);
    	}
    }

    /**
     * Get the status of the task associated with the specified connection
     * @param connection - The Connection object
     * @return a PluggableConnectionFactoryTaskStatus object representing the status
     */
    public PluggableConnectionFactoryTaskStatus getTaskStatus(Connection connection) {
    	PluggableConnectionFactoryTaskContext taskContext = taskContextByConnection.get(connection);
    	if (taskContext != null) {
        	return taskContext.getStatus();
    	}
    	return PluggableConnectionFactoryTaskStatus.TASK_STATUS_NEW;
    }

	/**
	 * This class represents a task that has allocated a connection through a Pluggable connection factory.
	 * Currently, the only part of the task that is represented here is its status. The status corresponds to 
	 * the TaskStatus object of the TaskContext.
	 * 
	 * @author Pat Hayes
	 *
	 */
    public class PluggableConnectionFactoryTaskContext {
    	
    	private PluggableConnectionFactoryTaskStatus status;
    	
    	public PluggableConnectionFactoryTaskContext() {
    	}
    	
    	public PluggableConnectionFactoryTaskContext(PluggableConnectionFactoryTaskStatus status ) {
    		this.status = status;
    	}
    	
    	public PluggableConnectionFactoryTaskStatus getStatus() {
    		return status;
    	}
    	
    	public void setStatus(PluggableConnectionFactoryTaskStatus status) {
    		this.status = status;
    	}
    }

    public enum PluggableConnectionFactoryTaskStatus {
        TASK_STATUS_NEW, TASK_STATUS_RUNNING, TASK_STATUS_UPDATE, TASK_STATUS_COMPLETE, TASK_STATUS_ERROR, TASK_STATUS_CANCELED;
    }
}
