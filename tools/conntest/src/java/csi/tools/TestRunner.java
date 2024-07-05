package csi.tools;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class TestRunner extends Thread {

	private Properties props;
	private String url;
	private Object waitObj;
	private String preSql;
	private String sql;
	private String postSql;
	private int sleepTime;
	private int iterations;
	private Boolean autoCommit;
	private Boolean readResult;
	private Driver driver;
	private Boolean logEnabled;

	public TestRunner(Driver driver, String connUrl, Properties connProps,
			Object waitObj, Boolean autoCommit, String preSql, String sql,
			String postSql, int iterations, int sleepTime, Boolean readResult, Boolean logEnabled) {
		this.driver = driver;
		this.url = connUrl;
		this.props = connProps;
		this.waitObj = waitObj;
		this.preSql = preSql;
		this.sql = sql;
		this.postSql = postSql;
		this.iterations = iterations;
		this.sleepTime = sleepTime;
		this.autoCommit = autoCommit;
		this.readResult = readResult;
		this.logEnabled = logEnabled;
	}

	public void run() {
		synchronized (waitObj) {
			try {
				waitObj.wait();
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted");
				return;
			}
		}
		log("Starting");

		for (int i = 0; i < iterations; i++) {
			Connection conn = null;
			try {
				log("connecting");
				conn = driver.connect(url, props);

				if (autoCommit != null) {
					conn.setAutoCommit(autoCommit);
				}

				if (preSql != null && !preSql.trim().isEmpty()) {
					log("executing pre sql");
					Statement preStmt = conn.createStatement();
					preStmt.execute(preSql);
				}

				if (sql != null && !sql.trim().isEmpty()) {
					log("executing sql");
					Statement sqlStmt = conn.createStatement();
					ResultSet rs = sqlStmt.executeQuery(sql);
					if (readResult) {
						log("reading query result");
						while (rs.next()) {
							rs.getObject(1);
						}
					}
				}

				if (postSql != null && !postSql.trim().isEmpty()) {
					log("executing post sql");
					Statement postStmt = conn.createStatement();
					postStmt.execute(postSql);
				}

				if (sleepTime > 0) {
					try {
						log("sleeping");
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		log("complete");

	}
	
	private void log(String msg) {
		if (logEnabled) {
			System.out.println(this.toString() + " : " + msg);
		}
	}
}