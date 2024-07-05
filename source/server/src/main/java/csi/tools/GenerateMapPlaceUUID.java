package csi.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import csi.server.common.model.CsiUUID;
import csi.server.dao.CsiPersistenceManager;

public class GenerateMapPlaceUUID implements CustomTaskChange {
	private static final Logger LOG = LogManager.getLogger(GenerateMapPlaceUUID.class);

	@Override
	public String getConfirmationMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUp() throws SetupException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// TODO Auto-generated method stub

	}

	@Override
	public ValidationErrors validate(Database database) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
//		ApplicationContext context = LiquibaseCacheInitializer.getInstance().getApplicationContext();
//		JndiObjectFactoryBean dataSource = (JndiObjectFactoryBean) context.getBean("dataSource");
//		Properties properties = dataSource.getJndiEnvironment();

		// JDBC driver name and database URL
//		String JDBC_DRIVER = properties.getProperty("driverClassName");
//		String DB_URL = properties.getProperty("url");

		//  Database credentials
//		String USER = properties.getProperty("username");
//		String PASS = properties.getProperty("password");

		try (Connection conn = CsiPersistenceManager.getMetaConnection();
		     Statement stmt = conn.createStatement()) {
//			Class.forName(JDBC_DRIVER);
//			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   Map<String,String> mapsettingsMapplace = new HashMap<String,String>();
		   String sql = "SELECT uuid FROM mapsettings";

		   try (ResultSet rs = stmt.executeQuery(sql)) {
		      while (rs.next()) {
		         mapsettingsMapplace.put(rs.getString("uuid"), CsiUUID.randomUUID());
		      }
		   }
			for (Map.Entry<String,String> entry : mapsettingsMapplace.entrySet()) {
				sql = "INSERT INTO mapsettings_mapplace (mapsettings_uuid, mapplaces_uuid) " +
		              "VALUES ('" + entry.getKey() + "', '" + entry.getValue() + "')";

				stmt.executeUpdate(sql);
			}
			conn.commit();
		} catch (SQLException se) {
			LOG.error(se);
		} catch (Exception e) {
			LOG.error(e);
		}
	}
}
