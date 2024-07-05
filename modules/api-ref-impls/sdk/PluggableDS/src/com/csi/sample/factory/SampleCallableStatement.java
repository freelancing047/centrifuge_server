/*
 * Copyright Centrifuge Systems, Inc. 2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package com.csi.sample.factory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * The Class SampleCallableStatement.
 */
public class SampleCallableStatement implements CallableStatement {

	/** The wrapped Derby JDBC CallableStatement. */
	private CallableStatement stmt = null;
	
	/** The reference to the SampleConnectionFactory instance. */
	private SampleConnectionFactory connFactory = null;
	
	/**
	 * Instantiates a new sample callable statement.
	 *
	 * @param stmt the stmt
	 */
	public SampleCallableStatement(SampleConnectionFactory connFactory, CallableStatement stmt) {
		this.stmt = stmt;
		this.connFactory = connFactory;
	}

	/**
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
	public void addBatch() throws SQLException {
		stmt.addBatch();

	}

	/**
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
	public void clearParameters() throws SQLException {
		stmt.clearParameters();

	}

	/**
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
	public boolean execute() throws SQLException {
		
		return stmt.execute();
	}

	/**
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		
		return stmt.executeQuery();
	}

	/**
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
	public int executeUpdate() throws SQLException {
		
		return stmt.executeUpdate();
	}

	/**
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		
		return stmt.getMetaData();
	}

	/**
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		
		return stmt.getParameterMetaData();
	}

	/**
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	@Override
	public void setArray(int arg0, Array arg1) throws SQLException {
		stmt.setArray(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
		stmt.setAsciiStream(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		stmt.setAsciiStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setAsciiStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
		stmt.setBigDecimal(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
		stmt.setBinaryStream(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		stmt.setBinaryStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setBinaryStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	@Override
	public void setBlob(int arg0, Blob arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setBlob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	@Override
	public void setBoolean(int arg0, boolean arg1) throws SQLException {
		stmt.setBoolean(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	@Override
	public void setByte(int arg0, byte arg1) throws SQLException {
		stmt.setByte(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	@Override
	public void setBytes(int arg0, byte[] arg1) throws SQLException {
		stmt.setBytes(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
		stmt.setCharacterStream(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
		stmt.setCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	@Override
	public void setClob(int arg0, Clob arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setClob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	@Override
	public void setDate(int arg0, Date arg1) throws SQLException {
		stmt.setDate(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
	 */
	@Override
	public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
		stmt.setDate(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	@Override
	public void setDouble(int arg0, double arg1) throws SQLException {
		stmt.setDouble(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	@Override
	public void setFloat(int arg0, float arg1) throws SQLException {
		stmt.setFloat(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	@Override
	public void setInt(int arg0, int arg1) throws SQLException {
		stmt.setInt(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	@Override
	public void setLong(int arg0, long arg1) throws SQLException {
		stmt.setLong(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
		stmt.setNCharacterStream(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setNCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setNClob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	@Override
	public void setNString(int arg0, String arg1) throws SQLException {
		stmt.setNString(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	@Override
	public void setNull(int arg0, int arg1) throws SQLException {
		stmt.setNull(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	@Override
	public void setNull(int arg0, int arg1, String arg2) throws SQLException {
		stmt.setNull(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	@Override
	public void setObject(int arg0, Object arg1) throws SQLException {
		stmt.setObject(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	@Override
	public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
		stmt.setObject(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	 */
	@Override
	public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
		stmt.setObject(arg0, arg1, arg2, arg3);

	}

	/**
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	@Override
	public void setRef(int arg0, Ref arg1) throws SQLException {
		stmt.setRef(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException {
		stmt.setRowId(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
		stmt.setSQLXML(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	@Override
	public void setShort(int arg0, short arg1) throws SQLException {
		stmt.setShort(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	@Override
	public void setString(int arg0, String arg1) throws SQLException {
		stmt.setString(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	@Override
	public void setTime(int arg0, Time arg1) throws SQLException {
		stmt.setTime(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
	 */
	@Override
	public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
		stmt.setTime(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(int arg0, Timestamp arg1) throws SQLException {
		stmt.setTimestamp(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
	 */
	@Override
	public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
		stmt.setTimestamp(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	@Override
	public void setURL(int arg0, URL arg1) throws SQLException {
		stmt.setURL(arg0, arg1);

	}

	/**
	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		stmt.setUnicodeStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
	public void addBatch(String sql) throws SQLException {
		stmt.addBatch(sql);

	}

	/**
	 * @see java.sql.Statement#cancel()
	 */
	@Override
	public void cancel() throws SQLException {
		stmt.cancel();

	}

	/**
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
	public void clearBatch() throws SQLException {
		stmt.clearBatch();

	}

	/**
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		stmt.clearWarnings();

	}

	/**
	 * @see java.sql.Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		stmt.close();

	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	@Override
	public boolean execute(String sql) throws SQLException {
		
		return stmt.execute(sql);
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		
		return stmt.execute(sql, autoGeneratedKeys);
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		
		return stmt.execute(sql, columnIndexes);
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		
		return stmt.execute(sql, columnNames);
	}

	/**
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		
		return stmt.executeBatch();
	}

	/**
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		
		return stmt.executeQuery(sql);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	@Override
	public int executeUpdate(String sql) throws SQLException {
		
		return stmt.executeUpdate(sql);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		
		return stmt.executeUpdate(sql, autoGeneratedKeys);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		
		return stmt.executeUpdate(sql, columnIndexes);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		
		return stmt.executeUpdate(sql, columnNames);
	}

	/**
	 * @see java.sql.Statement#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		
		return stmt.getConnection();
	}

	/**
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		
		return stmt.getFetchDirection();
	}

	/**
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		
		return stmt.getFetchSize();
	}

	/**
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		
		return stmt.getGeneratedKeys();
	}

	/**
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
	public int getMaxFieldSize() throws SQLException {
		
		return stmt.getMaxFieldSize();
	}

	/**
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
	public int getMaxRows() throws SQLException {
		
		return stmt.getMaxRows();
	}

	/**
	 * @see java.sql.Statement#getMoreResults()
	 */
	@Override
	public boolean getMoreResults() throws SQLException {
		
		return stmt.getMoreResults();
	}

	/**
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	@Override
	public boolean getMoreResults(int current) throws SQLException {
		
		return stmt.getMoreResults(current);
	}

	/**
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
	public int getQueryTimeout() throws SQLException {
		
		return stmt.getQueryTimeout();
	}

	/**
	 * @see java.sql.Statement#getResultSet()
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		
		return stmt.getResultSet();
	}

	/**
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	@Override
	public int getResultSetConcurrency() throws SQLException {
		
		return stmt.getResultSetConcurrency();
	}

	/**
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	@Override
	public int getResultSetHoldability() throws SQLException {
		
		return stmt.getResultSetHoldability();
	}

	/**
	 * @see java.sql.Statement#getResultSetType()
	 */
	@Override
	public int getResultSetType() throws SQLException {
		
		return stmt.getResultSetType();
	}

	/**
	 * @see java.sql.Statement#getUpdateCount()
	 */
	@Override
	public int getUpdateCount() throws SQLException {
		
		return stmt.getUpdateCount();
	}

	/**
	 * @see java.sql.Statement#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		
		return stmt.getWarnings();
	}

	/**
	 * @see java.sql.Statement#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		
		return stmt.isClosed();
	}

	/**
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		
		return stmt.isPoolable();
	}

    public void closeOnCompletion() throws SQLException {

    }

    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    /**
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	@Override
	public void setCursorName(String name) throws SQLException {
		stmt.setCursorName(name);

	}

	/**
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		stmt.setEscapeProcessing(enable);

	}

	/**
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(int direction) throws SQLException {
		stmt.setFetchDirection(direction);

	}

	/**
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(int rows) throws SQLException {
		stmt.setFetchSize(rows);

	}

	/**
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		stmt.setMaxFieldSize(max);

	}

	/**
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
	public void setMaxRows(int max) throws SQLException {
		stmt.setMaxRows(max);

	}

	/**
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		stmt.setPoolable(poolable);

	}

	/**
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		stmt.setQueryTimeout(seconds);

	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		
		return stmt. isWrapperFor(iface);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		
		return stmt.unwrap(iface);
	}

	/**
	 * @see java.sql.CallableStatement#getArray(int)
	 */
	@Override
	public Array getArray(int arg0) throws SQLException {
		
		return stmt.getArray(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getArray(java.lang.String)
	 */
	@Override
	public Array getArray(String arg0) throws SQLException {
		
		return stmt.getArray(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBigDecimal(int)
	 */
	@Override
	public BigDecimal getBigDecimal(int arg0) throws SQLException {
		
		return stmt. getBigDecimal(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal(String arg0) throws SQLException {
		
		return stmt.getBigDecimal(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBigDecimal(int, int)
	 */
	@Override
	public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
		
		return stmt.getBigDecimal(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getBlob(int)
	 */
	@Override
	public Blob getBlob(int arg0) throws SQLException {
		
		return stmt.getBlob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBlob(java.lang.String)
	 */
	@Override
	public Blob getBlob(String arg0) throws SQLException {
		
		return stmt.getBlob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBoolean(int)
	 */
	@Override
	public boolean getBoolean(int arg0) throws SQLException {
		
		return stmt.getBoolean(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String arg0) throws SQLException {
		
		return stmt.getBoolean(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getByte(int)
	 */
	@Override
	public byte getByte(int arg0) throws SQLException {
		
		return stmt.getByte(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String arg0) throws SQLException {
		
		return stmt.getByte(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBytes(int)
	 */
	@Override
	public byte[] getBytes(int arg0) throws SQLException {
		
		return stmt.getBytes(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes(String arg0) throws SQLException {
		
		return stmt.getBytes(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getCharacterStream(int)
	 */
	@Override
	public Reader getCharacterStream(int arg0) throws SQLException {
		
		return stmt.getCharacterStream(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getCharacterStream(String arg0) throws SQLException {
		
		return stmt.getCharacterStream(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getClob(int)
	 */
	@Override
	public Clob getClob(int arg0) throws SQLException {
		
		return stmt.getClob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getClob(java.lang.String)
	 */
	@Override
	public Clob getClob(String arg0) throws SQLException {
		
		return stmt.getClob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getDate(int)
	 */
	@Override
	public Date getDate(int arg0) throws SQLException {
		
		return stmt.getDate(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getDate(java.lang.String)
	 */
	@Override
	public Date getDate(String arg0) throws SQLException {
		
		return stmt.getDate(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getDate(int, java.util.Calendar)
	 */
	@Override
	public Date getDate(int arg0, Calendar arg1) throws SQLException {
		
		return stmt.getDate(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Date getDate(String arg0, Calendar arg1) throws SQLException {
		
		return stmt.getDate(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getDouble(int)
	 */
	@Override
	public double getDouble(int arg0) throws SQLException {
		
		return stmt.getDouble(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String arg0) throws SQLException {
		
		return stmt.getDouble(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getFloat(int)
	 */
	@Override
	public float getFloat(int arg0) throws SQLException {
		
		return stmt.getFloat(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String arg0) throws SQLException {
		
		return stmt.getFloat(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getInt(int)
	 */
	@Override
	public int getInt(int arg0) throws SQLException {
		
		return stmt.getInt( arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String arg0) throws SQLException {
		
		return stmt.getInt(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getLong(int)
	 */
	@Override
	public long getLong(int arg0) throws SQLException {
		
		return stmt.getLong(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String arg0) throws SQLException {
		
		return stmt.getLong(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int arg0) throws SQLException {
		
		return stmt.getNCharacterStream(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String arg0) throws SQLException {
		
		return stmt.getNCharacterStream(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNClob(int)
	 */
	@Override
	public NClob getNClob(int arg0) throws SQLException {
		
		return stmt.getNClob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String arg0) throws SQLException {
		
		return stmt.getNClob(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNString(int)
	 */
	@Override
	public String getNString(int arg0) throws SQLException {
		
		return stmt.getNString(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String arg0) throws SQLException {
		
		return stmt.getNString(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getObject(int)
	 */
	@Override
	public Object getObject(int arg0) throws SQLException {
		
		return stmt.getObject(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String arg0) throws SQLException {
		
		return stmt.getObject(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		
		return stmt.getObject(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		
		return stmt.getObject(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getRef(int)
	 */
	@Override
	public Ref getRef(int arg0) throws SQLException {
		
		return stmt.getRef(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef(String arg0) throws SQLException {
		
		return stmt.getRef(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getRowId(int)
	 */
	@Override
	public RowId getRowId(int arg0) throws SQLException {
		
		return stmt.getRowId(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String arg0) throws SQLException {
		
		return stmt.getRowId(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException {
		
		return stmt.getSQLXML(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException {
		
		return stmt.getSQLXML(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getShort(int)
	 */
	@Override
	public short getShort(int arg0) throws SQLException {
		
		return stmt.getShort(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String arg0) throws SQLException {
		
		return stmt.getShort(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getString(int)
	 */
	@Override
	public String getString(int arg0) throws SQLException {
		
		return stmt.getString(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getString(java.lang.String)
	 */
	@Override
	public String getString(String arg0) throws SQLException {
		
		return stmt.getString(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getTime(int)
	 */
	@Override
	public Time getTime(int arg0) throws SQLException {
		
		return stmt.getTime(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getTime(java.lang.String)
	 */
	@Override
	public Time getTime(String arg0) throws SQLException {
		
		return stmt.getTime(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getTime(int, java.util.Calendar)
	 */
	@Override
	public Time getTime(int arg0, Calendar arg1) throws SQLException {
		
		return stmt.getTime(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Time getTime(String arg0, Calendar arg1) throws SQLException {
		
		return stmt.getTime(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp(int arg0) throws SQLException {
		
		return stmt.getTimestamp(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp(String arg0) throws SQLException {
		
		return stmt.getTimestamp(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
		
		return stmt.getTimestamp(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
		
		return stmt.getTimestamp(arg0, arg1);
	}

	/**
	 * @see java.sql.CallableStatement#getURL(int)
	 */
	@Override
	public URL getURL(int arg0) throws SQLException {
		
		return stmt.getURL(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#getURL(java.lang.String)
	 */
	@Override
	public URL getURL(String arg0) throws SQLException {
		
		return stmt.getURL(arg0);
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int)
	 */
	@Override
	public void registerOutParameter(int arg0, int arg1) throws SQLException {
		stmt.registerOutParameter(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int)
	 */
	@Override
	public void registerOutParameter(String arg0, int arg1) throws SQLException {
		stmt.registerOutParameter(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int, int)
	 */
	@Override
	public void registerOutParameter(int arg0, int arg1, int arg2) throws SQLException {
		stmt.registerOutParameter(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int, java.lang.String)
	 */
	@Override
	public void registerOutParameter(int arg0, int arg1, String arg2) throws SQLException {
		stmt.registerOutParameter(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int, int)
	 */
	@Override
	public void registerOutParameter(String arg0, int arg1, int arg2) throws SQLException {
		stmt.registerOutParameter(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int, java.lang.String)
	 */
	@Override
	public void registerOutParameter(String arg0, int arg1, String arg2) throws SQLException {
		stmt.registerOutParameter(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(String arg0, InputStream arg1) throws SQLException {
		stmt.setAsciiStream(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void setAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		stmt.setAsciiStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void setAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setAsciiStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
		stmt.setBigDecimal(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(String arg0, InputStream arg1) throws SQLException {
		stmt.setBinaryStream(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void setBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		stmt.setBinaryStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void setBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setBinaryStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
	public void setBlob(String arg0, Blob arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void setBlob(String arg0, InputStream arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
		stmt.setBlob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setBoolean(java.lang.String, boolean)
	 */
	@Override
	public void setBoolean(String arg0, boolean arg1) throws SQLException {
		stmt.setBoolean(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setByte(java.lang.String, byte)
	 */
	@Override
	public void setByte(String arg0, byte arg1) throws SQLException {
		stmt.setByte(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setBytes(java.lang.String, byte[])
	 */
	@Override
	public void setBytes(String arg0, byte[] arg1) throws SQLException {
		stmt.setBytes(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(String arg0, Reader arg1) throws SQLException {
		stmt.setCharacterStream(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	@Override
	public void setCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
		stmt.setCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void setCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void setClob(String arg0, Clob arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void setClob(String arg0, Reader arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void setClob(String arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setClob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void setDate(String arg0, Date arg1) throws SQLException {
		stmt.setDate(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date, java.util.Calendar)
	 */
	@Override
	public void setDate(String arg0, Date arg1, Calendar arg2) throws SQLException {
		stmt.setDate(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setDouble(java.lang.String, double)
	 */
	@Override
	public void setDouble(String arg0, double arg1) throws SQLException {
		stmt.setDouble(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setFloat(java.lang.String, float)
	 */
	@Override
	public void setFloat(String arg0, float arg1) throws SQLException {
		stmt.setFloat(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setInt(java.lang.String, int)
	 */
	@Override
	public void setInt(String arg0, int arg1) throws SQLException {
		stmt.setInt(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setLong(java.lang.String, long)
	 */
	@Override
	public void setLong(String arg0, long arg1) throws SQLException {
		stmt.setLong(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setNCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(String arg0, Reader arg1) throws SQLException {
		stmt.setNCharacterStream(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void setNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setNCharacterStream(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void setNClob(String arg0, NClob arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void setNClob(String arg0, Reader arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return null;
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return null;
    }

    /**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void setNClob(String arg0, Reader arg1, long arg2) throws SQLException {
		stmt.setNClob(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setNString(String arg0, String arg1) throws SQLException {
		stmt.setNString(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int)
	 */
	@Override
	public void setNull(String arg0, int arg1) throws SQLException {
		stmt.setNull(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int, java.lang.String)
	 */
	@Override
	public void setNull(String arg0, int arg1, String arg2) throws SQLException {
		stmt.setNull(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setObject(String arg0, Object arg1) throws SQLException {
		stmt.setObject(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public void setObject(String arg0, Object arg1, int arg2) throws SQLException {
		stmt.setObject(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int, int)
	 */
	@Override
	public void setObject(String arg0, Object arg1, int arg2, int arg3) throws SQLException {
		stmt.setObject(arg0, arg1, arg2, arg3);

	}

	/**
	 * @see java.sql.CallableStatement#setRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void setRowId(String arg0, RowId arg1) throws SQLException {
		stmt.setRowId(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(String arg0, SQLXML arg1) throws SQLException {
		stmt.setSQLXML(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setShort(java.lang.String, short)
	 */
	@Override
	public void setShort(String arg0, short arg1) throws SQLException {
		stmt.setShort(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setString(String arg0, String arg1) throws SQLException {
		stmt.setString(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void setTime(String arg0, Time arg1) throws SQLException {
		stmt.setTime(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time, java.util.Calendar)
	 */
	@Override
	public void setTime(String arg0, Time arg1, Calendar arg2) throws SQLException {
		stmt.setTime(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(String arg0, Timestamp arg1) throws SQLException {
		stmt.setTimestamp(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp, java.util.Calendar)
	 */
	@Override
	public void setTimestamp(String arg0, Timestamp arg1, Calendar arg2) throws SQLException {
		stmt.setTimestamp(arg0, arg1, arg2);

	}

	/**
	 * @see java.sql.CallableStatement#setURL(java.lang.String, java.net.URL)
	 */
	@Override
	public void setURL(String arg0, URL arg1) throws SQLException {
		stmt.setURL(arg0, arg1);

	}

	/**
	 * @see java.sql.CallableStatement#wasNull()
	 */
	@Override
	public boolean wasNull() throws SQLException {
		
		return stmt.wasNull();
	}

}
