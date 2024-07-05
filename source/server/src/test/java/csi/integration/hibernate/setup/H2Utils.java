package csi.integration.hibernate.setup;

import javax.persistence.Query;

import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 * Run this to clear your H2 database.
 * The H2 database contains data for integration tests that the cache db does in production.
 */
public class H2Utils {

    public static void clearDatabase(){
        Query query =  CsiPersistenceManager.getMetaEntityManager().createNativeQuery("DROP ALL OBJECTS");
        CsiPersistenceManager.begin();
        query.executeUpdate();
        CsiPersistenceManager.close();

    }

    public static void main(String [] args){
        H2Utils.clearDatabase();
    }
}
