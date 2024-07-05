package csi.integration.hibernate.setup;

import java.util.Arrays;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.server.business.service.AuthorizationStub;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.config.DriverList;
import csi.server.connector.config.JdbcDriver;

/**
 * @author Centrifuge Systems, Inc.
 * Initializes Centrifuge with the H2 database as the cache DB (instead of postgres).
 */
public class InitializeCentrifuge {

    public static void initialize(){
        try {
            createTestConfiguration();
            ConnectionFactoryManager.getInstance().init();
            CsiSecurityManager.setAuthorization(new AuthorizationStub("centrifuge", JAASRole.ADMIN_ROLE_NAME));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void createTestConfiguration() {
        Configuration configuration = new Configuration();

        JdbcDriver driver = new JdbcDriver();
        driver.setName("H2");
        driver.setKey("CacheData");
        driver.setFactory(H2JdbcConnectionFactory.class.getName());

        DriverList driverList = new DriverList();
        driverList.setDrivers(Arrays.asList(driver));

        DBConfig dbConfig = new DBConfig();
        dbConfig.setDrivers(driverList);

        configuration.setDbConfig(dbConfig);
        Configuration.setInstance(configuration);

    }
}
