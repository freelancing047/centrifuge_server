package csi.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import sun.misc.Launcher;

public class ConnectionTester {
	
	private static String ORA_PKI_PROVIDER = "oracle.security.pki.OraclePKIProvider";

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			showUsage();
			return;
		}

		File serverdir = new File(args[0]);
		File propFile = new File(args[1]);

		File commonLib = new File(serverdir, "lib");

		List<URL> jars = new ArrayList<URL>();
		addJars(jars, commonLib);

		URLClassLoader loader = new URLClassLoader(jars.toArray(new URL[0]));
		Thread.currentThread().setContextClassLoader(loader);

		Properties testProps = new Properties();
		if (propFile.exists()) {
			FileInputStream ins = new FileInputStream(propFile);
			try {
				testProps.load(ins);
			} finally {
				if (ins != null) {
					try {
						ins.close();
					} catch (Exception ignore) {
					}
				}
			}
		} else {
			System.out.println("Property file not found: "
					+ propFile.getCanonicalPath());
			return;
		}

		String clzname = testProps.getProperty("driver.class");
		if (clzname == null || clzname.trim().isEmpty()) {
			System.out.println("ERROR: missing driver class name");
		}
		Class driverClz = loader.loadClass(clzname.trim());
		Driver driver = (Driver) driverClz.newInstance();
		
		String connUrl = testProps.getProperty("connection.url");

		Properties connProps = new Properties();
		for (String key : testProps.stringPropertyNames()) {
			if (key != null && key.startsWith("connection.prop.")) {
				String conkey = key.substring("connection.prop.".length());
				connProps.put(conkey, testProps.get(key));
			}
		}
		
		Boolean autoCommit = null;
		String autoc = testProps.getProperty("connection.autoCommit");
		if (autoc != null) {
			 autoCommit = Boolean.parseBoolean(testProps.getProperty("connection.autoCommit"));
		}

		Boolean readResult = Boolean.parseBoolean(testProps.getProperty("test.readResult"));
		
		String preSql = testProps.getProperty("test.preSql");
		String sql = testProps.getProperty("test.sql");
		String postSql = testProps.getProperty("test.postSql");

		Boolean loadOraPki = Boolean.parseBoolean(testProps.getProperty("misc.initOraPki"));
		if (loadOraPki) {
			// init the oracle pki provider if it's available
			Class clz = null;
			try {
				clz = Class.forName(ORA_PKI_PROVIDER);
			} catch (ClassNotFoundException e) {
				// ignore
			}

			if (clz != null) {
				try {
					clz.newInstance();
				} catch (Throwable t) {
					throw new Exception(
							"Failed to initialize Oracle PKI provider", t);
				}
			}
		}
		
		int numThreads = Integer.parseInt(testProps.getProperty("test.numThreads"));
		int iterations = Integer.parseInt(testProps.getProperty("test.iterations"));
		int sleepTime = Integer.parseInt(testProps.getProperty("test.sleepTime"));
		Boolean enableLog = Boolean.parseBoolean(testProps.getProperty("test.logging"));
		
		Object waitObj = new Object();
		
		for(int i=0; i < numThreads; i++) {
			Thread t = new TestRunner(driver, connUrl, connProps, waitObj, autoCommit, preSql, sql, postSql, iterations, sleepTime, readResult, enableLog);
			t.setContextClassLoader(loader);
			t.start();
		}
		
		System.out.println("Starting " + numThreads + " threads.");
		// give the threads a chance to initiallize and get to the wait point
		Thread.sleep(200);
		synchronized(waitObj) {
			waitObj.notifyAll();
		}
	}



	private static void addResource(List<URL> jars, File path)
			throws MalformedURLException {
		jars.add(path.toURL());
	}

	private static void addJars(List<URL> jars, File path)
			throws MalformedURLException {
		if (path.isFile()) {
			jars.add(path.toURL());
		} else {
			if (!path.exists()) {
				return;
			}

			FileFilter jarFilter = new FileFilter() {

				public boolean accept(File pathname) {
					return (pathname.getName().endsWith(".jar"));
				}
			};

			File[] listing = path.listFiles(jarFilter);
			for (File child : listing) {
				jars.add(child.toURL());
			}
		}
	}

	private static void showUsage() {
		StringBuffer buf = new StringBuffer();
		buf.append("\nSYNTAX: testconn server-dir property-file");
		buf.append("\n\n  server-home       - Home directory of Centrifuge Server");
		buf.append("\n\n  property-file     - Settings property file");
		System.out.println(buf.toString());
	}
}
