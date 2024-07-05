package csi.startup;

import java.sql.Connection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import csi.server.common.exception.CentrifugeException;

//import liquibase.integration.spring.SpringLiquibase;

public class LiquibaseCacheInitializer extends AbstractCacheInitializer {
   void createIfDontExist(Connection conn) throws CentrifugeException {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring4.xml");
		/*SpringLiquibase springLiquibase = (SpringLiquibase) */context.getBean("liquibase");
	}
}
