/*
 * Copyright Centrifuge Systems, Inc. 2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package com.csi.sample.factory;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * The SampleConnection class implements the java.sql.Connection interface as a demonstration of creating
 * your own JDBC Connection class that wraps a 3rd parties JDBC Connection class.  In this sample implementation
 * we simply delegate the call to the wrapped Derby JDBC Connection object.
 * <br><br>
 * It does illustrate how the implementer might wish to intercept the various JDBC calls to perform specific 
 * processing before and/or after the processing by the underlying 3rd party JDBC provider.  For instance, the
 * createStatement() methods actually return Sample wrapper classes to the Centrifuge Server for its JDBC 
 * processing.  Because we use the delegate pattern for these wrapper classes the work is actually performed
 * by the Derby JDBC driver.
 */
public class SampleConnection implements Connection {
	
	/** The wrapped Derby JDBC connection. */
	private Connection conn = null;
	
	/** The reference to the SampleConnectionFactory instance. */
	private SampleConnectionFactory connFactory = null;
	

	/**
	 * Instantiates a new Sample JDBC connection.
	 *
	 * @param conn the conn
	 */
	public SampleConnection(SampleConnectionFactory connFactory, Connection conn) {
		this.conn = conn;
		this.connFactory = connFactory;
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return conn.isWrapperFor(arg0);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return conn.unwrap(arg0);
	}

	/**
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}

	/**
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException {
		conn.close();
	}

	/**
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException {
		conn.commit();
	}

	/**
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return conn.createArrayOf(arg0, arg1);
	}

	/**
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return conn.createBlob();
	}

	/**
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return conn.createClob();
	}

	/**
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return conn.createNClob();
	}

	/**
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		return conn.createSQLXML();
	}

	/**
	 * We wrap the Derby Statement object instance in our SampleStatement object.
	 * 
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException {
		return new SampleStatement(connFactory, conn.createStatement());
	}

	/**
	 * We wrap the Derby Statement object instance in our SampleStatement object.
	 * 
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return new SampleStatement(connFactory, conn.createStatement(arg0, arg1));
	}

	/**
	 * We wrap the Derby Statement object instance in our SampleStatement object.
	 * 
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
		return new SampleStatement(connFactory, conn.createStatement(arg0, arg1, arg2));
	}

	/**
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return conn.createStruct(arg0, arg1);
	}

    public void setSchema(String schema) throws SQLException {

    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    /**
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}

	/**
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}

	/**
	 * @see java.sql.Connection#getClientInfo()
	 */
	@Override
	public Properties getClientInfo() throws SQLException {
		return conn.getClientInfo();
	}

	/**
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String arg0) throws SQLException {
		return conn.getClientInfo(arg0);
	}

	/**
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		return conn.getHoldability();
	}

	/**
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}

	/**
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}

	/**
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return conn.getTypeMap();
	}

	/**
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}

	/**
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}

	/**
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	/**
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int arg0) throws SQLException {
		return conn.isValid(arg0);
	}

	/**
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL(String arg0) throws SQLException {
		return conn.nativeSQL(arg0);
	}

	/**
	 * We wrap the Derby CallableStatement object instance in our SampleCallableStatement object.
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall(String arg0) throws SQLException {
		return new SampleCallableStatement(connFactory, conn.prepareCall(arg0));
	}

	/**
	 * We wrap the Derby CallableStatement object instance in our SampleCallableStatement object.
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
		return new SampleCallableStatement(connFactory, conn.prepareCall(arg0, arg1, arg2));
	}

	/**
	 * We wrap the Derby CallableStatement object instance in our SampleCallableStatement object.
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return new SampleCallableStatement(connFactory, conn.prepareCall(arg0, arg1, arg2, arg3));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0, arg1));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0, arg1));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0, arg1));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0, arg1, arg2));
	}

	/**
	 * We wrap the Derby PreparedStatement object instance in our SamplePreparedStatement object.
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return new SamplePreparedStatement(connFactory, conn.prepareStatement(arg0, arg1, arg2, arg3));
	}

	/**
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		conn.releaseSavepoint(arg0);
	}

	/**
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException {
		conn.rollback();
	}

	/**
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint arg0) throws SQLException {
		conn.rollback(arg0);
	}

	/**
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit(boolean arg0) throws SQLException {
		conn.setAutoCommit(arg0);
	}

	/**
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
	public void setCatalog(String arg0) throws SQLException {
		conn.setCatalog(arg0);
	}

	/**
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		conn.setClientInfo(arg0);
	}

	/**
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		conn.setClientInfo(arg0, arg1);
	}

	/**
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
	public void setHoldability(int arg0) throws SQLException {
		conn.setHoldability(arg0);
	}

	/**
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean arg0) throws SQLException {
		conn.setReadOnly(arg0);
	}

	/**
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		return conn.setSavepoint();
	}

	/**
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String arg0) throws SQLException {
		return conn.setSavepoint(arg0);
	}

	/**
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
	public void setTransactionIsolation(int arg0) throws SQLException {
		conn.setTransactionIsolation(arg0);
	}

	/**
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		conn.setTypeMap(arg0);
	}

}
