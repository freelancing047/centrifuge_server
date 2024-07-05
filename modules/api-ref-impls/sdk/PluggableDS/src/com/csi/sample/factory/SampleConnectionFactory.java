/*
 * Copyright Centrifuge Systems, Inc. 2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package com.csi.sample.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.csi.jdbc.factory.CsiConnectionFactoryException;
import com.csi.jdbc.factory.PluggableConnectionFactory;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

/**
 * This is a sample implementation of the PluggableConnectionFactory using an in memory relational database,
 * Derby, to provide the JDBC interface to the Centrifuge Server.   Using the Derby RDBMS as an in memory
 * database this sample expects a CSV data file that contains as its first row the column names to be used.  
 * It assumes all columns are of type 'string' for simplicity.  
 * <br><br>
 * The input parameters are obtained from the user via the Data Source editor that dynamically creates the
 * user input UI forms using the UI configuration parameters stored in the Centrifuge Servers configuration file,
 * centrifuge.xml.  The only parameter that is used by this sample implementation is the name of the CSV file.  
 * The filename is used to create a Derby database schema and a table within the schema for storing the data
 * contained in the CSV file.
 * <br><br>
 * This implementation is loaded by the Centrifuge Server during initialization using the PluggableProxyConnectionFactory
 * class.  The only touch point between the Centrifuge Server and a PluggableConnectionFactory implementation is
 * the getConnection(properties) method.  In this sample implementation the first time the getConnection() method
 * is called is detected and the Derby database schema and table are created, the data loaded into the table.  A 
 * Derby JDBC connection to the in memory database is then wrapped in the SampleConnection object, which implements
 * the JDBC Connection interface, and returned to the Centrifuge Server for processing.  All subsequent calls to the
 * getConnection() method by the Centrifuge Server a JDBC Connection object will be returned.
 * <br><br>
 * This sample implementation also demonstrates how an implementer can detect that new data is available to refresh
 * the in memory Derby database table.  It includes an inner class, Watcher, that runs in a thread that will 
 * periodically check the timestamp of the CSV file to determine if there has been a change.  It will then call the
 * refresh() method of the SampleConnectionFactory class to purge the old data from the in memory Derby database and
 * refresh it with the data contained in the new file.  On all subsequent calls to getConnection() a JDBC Connection 
 * will be created that references the refreshed data.
 * 
 */
public class SampleConnectionFactory extends PluggableConnectionFactory {

	/** The Constant FILENAME. */
	private static final String FILENAME = "filenamefile.filetoken";
	
    /** The protocol. */
    private static final String DBURLPROTOCOL = "jdbc:derby:memory:samples/database/";
    
    /** The Constant DEFAULTDBTYPE. */
    private static final String DEFAULTDBTYPE = "varchar";
    
    /** The Constant DATABASESUFFIX. */
    private static final String DATABASESUFFIX = "DB";
    
    /** The Constant DBURLSUFFIX. */
    private static final String DBURLSUFFIX = ";create=true";
    
    /** The Constant DBTABLEEXISTSQL. */
    private static final String DBTABLEEXISTSQL = "select * from SYS.SYSTABLES where TABLENAME = ";
	
    /** The driver. */
    private Driver driver = null;
    
	/** The watcher. */
	private Watcher watcher = null;
	
	/** The display properties. */
	private boolean displayProperties = true;
	
	/**
	 * Instantiates a new SampleConnectionFactory object.
	 */
	public SampleConnectionFactory() {
		try {
			watcher = new Watcher();
			watcher.setDaemon(true);
			watcher.start();
		}
		catch(Throwable e) {
			getLogger().error(Messages.getString("PDSRefImpl.0002"));
			getLogger().error(e);
		}
	}
	
	/**
	 * Called by the Centrifuge Server to obtain a JDBC Connection.  It returns an instance of a
	 * SampleConnection object that wraps Derby's database connection.  
	 *
	 * @param props the props
	 * @return the connection
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 * @see com.csi.jdbc.factory.PluggableConnectionFactory#getConnection(java.util.Properties)
	 */
	@Override
	public Connection getConnection(Properties props) throws CsiConnectionFactoryException {
		
		if (displayProperties) {
			logProperties(props);
			displayProperties = false;
		}
		
		String url = checkSchemaInit(props);
		return new SampleConnection(this, getDerbyConnection(url)); 
	}
	
	/**
	 * Check schema init, returns a Derby dburl for creating a JDBC connection. Its other function
	 * is to verify that a Derby schema for this request has been created, the associated table defined,
	 * and the data parsed and loaded into the Derby database.
	 *
	 * @param props the props
	 * @return the string
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String checkSchemaInit(Properties props) throws CsiConnectionFactoryException {
		String url = createUrl(props);
		Connection conn = getDerbyConnection(url);
		
		createDerbySchema(conn, props);
		
		try {
			conn.close();
		} catch (Throwable e) {
			getLogger().warn(Messages.getString("PDSRefImpl.0004"));
			getLogger().warn(e);
		}
		
		return url;
	}
	
	/**
	 * Constructs the Derby dburl for creating JDBC connections.
	 *
	 * @param props the props
	 * @return the string
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String createUrl(Properties props) throws CsiConnectionFactoryException {
		return(DBURLPROTOCOL + getDatabaseName(props) + DBURLSUFFIX);
	}
	
	/**
	 * Gets the tablename from the properties.
	 *
	 * @param props the props
	 * @return the tablename
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String getTablename(Properties props) throws CsiConnectionFactoryException {
		String fname = props.getProperty(FILENAME);
		int idx = fname.lastIndexOf(".");
		if (idx > -1) {
			fname = fname.substring(0, idx);
		}
		StringTokenizer parser = new StringTokenizer(fname, "/\\");
		String tname = null;
		while (parser.hasMoreTokens()) {
			tname = parser.nextToken();
		}
		if (tname == null) {
			getLogger().error(Messages.getString("PDSRefImpl.0011"));
			throw new CsiConnectionFactoryException(Messages.getString("PDSRefImpl.0011"));
		}
		tname = convertToSQLSyntax(tname);
		return tname;
	}
	
	/**
	 * Gets the database name from the properties.
	 *
	 * @param props the props
	 * @return the database name
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String getDatabaseName(Properties props) throws CsiConnectionFactoryException {
		String dbname = getTablename(props);
		if (dbname == null) {
			throw new CsiConnectionFactoryException(Messages.getString("PDSRefImpl.0011"));
		}
		return(getDatabaseName(dbname));
	}
	
	/**
	 * Gets the database name based on the table name.
	 *
	 * @param tablename the tablename
	 * @return the database name
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String getDatabaseName(String tablename) throws CsiConnectionFactoryException {
		if (tablename == null) {
			throw new CsiConnectionFactoryException(Messages.getString("PDSRefImpl.0011"));
		}
		return tablename + DATABASESUFFIX;
	}
	
	/**
	 * Will detect if the internal schema has been created yet, if not it will create the
	 * schema using the first data row as column names and the insert the remain rows in the CSV file
	 * into the newly created schema.  If the schema already exists, this method simply returns.
	 *
	 * @param conn the conn
	 * @param props the props
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private void createDerbySchema(Connection conn, Properties props) throws CsiConnectionFactoryException {
		Statement stmt = null;
		BufferedReader reader = null;
		String tableName = null;
		try {
			tableName = getTablename(props);
			if (!tableExists(conn, tableName)) {
				reader = getFileReader(props);
				String headers = reader.readLine();
				List<String> collist = parseData(headers);
				String createtablesql = createTableDef(collist, tableName);
				stmt = conn.createStatement();
				int rc = stmt.executeUpdate(createtablesql);
				if (rc != 0) {
					getLogger().error(Messages.getString("PDSRefImpl.0005"));
					throw new CsiConnectionFactoryException(Messages.getString("PDSRefImpl.0005"));
				}
				loadData(conn, reader, tableName);
				String filepath = props.getProperty(FILENAME);
				watcher.addFile(tableName, filepath, new File(filepath).lastModified(), props);
			}
		} catch (Throwable e) {
			String msg = Messages.getString("PDSRefImpl.0006");
			getLogger().error(MessageFormat.format(msg, props.getProperty(FILENAME)));
			getLogger().error(e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch(SQLException se) {}
		}
	}
	
	/**
	 * Tests for the existence of the table to detect if it must be created and data loaded.
	 *
	 * @param conn the conn
	 * @param tablename the tablename
	 * @return true, if successful
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private boolean tableExists(Connection conn, String tablename) throws CsiConnectionFactoryException {
		boolean rc = false;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(DBTABLEEXISTSQL + "'" + tablename.toUpperCase() + "'");
			if (rs.next()) {
				rc = true;
			}
		} catch(Throwable t) {
			rc = false;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
			}
		}
		return rc;
	}
	
	/**
	 * Gets the file reader for reading the CSV file.  The filename is contained the properties 
	 * object, which is set by the user using the Data Source Editor UI.
	 *
	 * @param props the props
	 * @return the file reader
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private BufferedReader getFileReader(Properties props) throws CsiConnectionFactoryException {
		try {
			File csvfile = new File(props.getProperty(FILENAME));
			FileReader input = new FileReader(csvfile);
			BufferedReader reader = new BufferedReader(input);
			return(reader);
		} catch(Throwable t) {
			String msg = Messages.getString("PDSRefImpl.0018");
			getLogger().error(MessageFormat.format(msg, props.getProperty(FILENAME)));
			getLogger().error(t);
			throw new CsiConnectionFactoryException(t);
		}
		
	}
	
	/**
	 * Parse the CSV data.
	 *
	 * @param input the input
	 * @return the list
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private List<String> parseData(String input) throws CsiConnectionFactoryException {
		StringTokenizer tokens = new StringTokenizer(input, ",");
		List<String> list = new ArrayList<String>();
		
		while (tokens.hasMoreTokens()) {
			list.add(tokens.nextToken());
		}
		return list;
	}
	
	/**
	 * Based on the first row of the data file, construct the proper SQL DDL to create the
	 * table for loading the data into.
	 *
	 * @param columns the columns
	 * @param tableName the table name
	 * @return the string
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private String createTableDef(List<String> columns, String tableName) throws CsiConnectionFactoryException {
		DbSpec spec = new DbSpec();
		DbSchema schema = spec.addDefaultSchema();
		
		DbTable table = schema.addTable(tableName);
		
		for (String col : columns) {
			col = convertToSQLSyntax(col);
			DbColumn column = table.addColumn(col, DEFAULTDBTYPE, 50);
		}
		
		return new CreateTableQuery(table, true).validate().toString();
	}
	
	/**
	 * Derby has come problems with special characters, this method cleans up the 
	 * Derby SQL syntax errors that might appear in the column names and/or data.
	 *
	 * @param str the str
	 * @return the string
	 */
	private String convertToSQLSyntax(String str) {
		return str.replaceAll("[^A-Za-z0-9]", "").trim().replace(' ', '_');
	}
	/**
	 * Read the CSV data file and insert the data into the Derby table we have created.
	 *
	 * @param conn the conn
	 * @param reader the reader
	 * @param tableName the table name
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private void loadData(Connection conn, BufferedReader reader, String tableName) throws CsiConnectionFactoryException {
		try {
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			String input = reader.readLine();
			while (input != null) {
				List<String> data = parseData(input);
				StringBuilder str = new StringBuilder();
				String sqlstr = Messages.getString("PDSRefImpl.0010");
				
				str.append(MessageFormat.format(sqlstr, tableName));
				boolean firstime = true;
				for (String value : data) {
					if (firstime) {
						str.append("'");
						firstime = false;
					}
					else {
						str.append(",'");
					}
					str.append(value + "'");
				}
				str.append(")");
				int rc = stmt.executeUpdate(str.toString());
				input = reader.readLine();
			}
			conn.commit();
			conn.setAutoCommit(true);
			stmt.close();
		}
		catch (IOException ioe) {
			String msg = Messages.getString("PDSRefImpl.0007");
			getLogger().error(MessageFormat.format(msg, tableName));
			getLogger().error(ioe);
			throw new CsiConnectionFactoryException(ioe);
		}
		catch (Throwable t) {
			String msg = Messages.getString("PDSRefImpl.0008");
			getLogger().error(MessageFormat.format(msg, tableName));
			getLogger().error(t);
			throw new CsiConnectionFactoryException(t);
		}
	}
	
	/**
	 * Gets a derby connection to be return to the Centrifuge Server.
	 *
	 * @param url the url
	 * @return the derby connection
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	private Connection getDerbyConnection(String url) throws CsiConnectionFactoryException {
		try {
			if (driver == null) {
				driver = DriverManager.getDriver(url);
				
			}
			return driver.connect(url, new Properties());
		}
		catch (Throwable e) {
			getLogger().error(Messages.getString("PDSRefImpl.0009"));
			getLogger().error(e);
			throw new CsiConnectionFactoryException(e);
		}
	}
	
	/**
	 * Display the contents of the properties object to the log for debug purposes.
	 *
	 * @param props the props
	 */
	private void logProperties(Properties props) {
		Enumeration<String> keys = (Enumeration<String>)props.propertyNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = props.getProperty(key);
			String msg = Messages.getString("PDSRefImpl.0013");
			getLogger().info(MessageFormat.format(msg, key, value));
		}
	}
	
	/**
	 * This method is called by the Watcher thread whenever a new file is detected.  It
	 * is assumed that the file is in identical format, just new data.  The data file will be read
	 * and the new data will replace the data in the Derby database.
	 *
	 * @param props the props
	 * @throws CsiConnectionFactoryException the csi connection factory exception
	 */
	void refresh(Properties props) throws CsiConnectionFactoryException {
		Statement stmt = null;
		Connection conn = null;
		String tablename = null;
		try {
			tablename = getTablename(props);
			conn = getDerbyConnection(createUrl(props));
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			String deleteData = MessageFormat.format(Messages.getString("PDSRefImpl.0019"), tablename.toUpperCase());
			stmt.executeUpdate(deleteData);
			BufferedReader reader = getFileReader(props);
			String buf = reader.readLine();  // skip over column headers
			loadData(conn, reader, tablename);
			conn.commit();
		} catch (Throwable t) {
			getLogger().error(MessageFormat.format(Messages.getString("PDSRefImpl.0014"), tablename));
			getLogger().error(t);
			throw new CsiConnectionFactoryException(t);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				
			}
		}
	}
	
	/**
	 * The Watcher class is an inner class that is launched in a thread.  It will poll the CSV file 
	 * to detect if its timestamp changes, indicating that a new file has been saved to the filesystem.  
	 * It then will call the refresh() method to replace the old data in the Derby database instance with 
	 * the new data from the file.
	 */
	class Watcher extends Thread {
		
		/** The filelist. */
		Map<String, Node> filelist = new HashMap<String, Node>();
		
		/** The wait a minute. */
		long waitAminute = 60 * 1000;

		/**
		 * Instantiates a new watcher.
		 */
		public Watcher() {
			
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run () {
			do {
				File file = null;
				try {
					Map<String, Node> wrklist = new HashMap<String, Node>();
					synchronized(filelist) {
						wrklist.putAll(filelist);
					}
					
					for (String filename : wrklist.keySet()) {
						Node node = wrklist.get(filename);
						file = new File(node.getFilePath());
						if (file.lastModified() != node.getLastModified()) {
							refresh(node.getProperties());
							node.setLastModified(file.lastModified());
						}
						file = null;
					}
					
					wrklist.clear();
					synchronized(this) {
						this.wait(waitAminute);
					}
				} catch (CsiConnectionFactoryException e) {
					String msg = Messages.getString("PDSRefImpl.0014");
					getLogger().error(MessageFormat.format(msg, file.getName()));
					getLogger().error(e);
					getLogger().info(Messages.getString("PDSRefImpl.0016"));
				} catch (Throwable t) {
					getLogger().error(Messages.getString("PDSRefImpl.0015"));
					getLogger().error(t);
					getLogger().info(Messages.getString("PDSRefImpl.0016"));
				} 
				
			} while (true);
		}		 
		
		/**
		 * Adds a file to the watch list.
		 *
		 * @param filename the filename
		 * @param filepath the filepath
		 * @param lastModified the last modified
		 * @param props the props
		 */
		public void addFile(String filename, String filepath, long lastModified, Properties props) {
			synchronized(filelist) {
				filelist.put(filename, new Node(filepath, lastModified, props));
			}
		}
	}
	
	/**
	 * Node is an inner class that encapsulates data about a file being watched by the Watcher thread.
	 */
	class Node {
		
		/** The filepath. */
		String filepath;
		
		/** The last modified. */
		long lastModified;
		
		/** The props. */
		Properties props;
		
		/**
		 * Instantiates a new node.
		 *
		 * @param filepath the filepath
		 * @param lastModified the last modified
		 * @param props the props
		 */
		Node(String filepath, long lastModified, Properties props) {
			this.filepath = filepath;
			this.lastModified = lastModified;
			this.props = props;
		}
		
		/**
		 * Gets the file path.
		 *
		 * @return the file path
		 */
		String getFilePath() {
			return filepath;
		}
		
		/**
		 * Gets the last modified.
		 *
		 * @return the last modified
		 */
		long getLastModified() {
			return lastModified;
		}
		
		/**
		 * Sets the last modified.
		 *
		 * @param lastModified the new last modified
		 */
		void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}
		
		/**
		 * Gets the properties.
		 *
		 * @return the properties
		 */
		Properties getProperties() {
			return props;
		}
	}
}
