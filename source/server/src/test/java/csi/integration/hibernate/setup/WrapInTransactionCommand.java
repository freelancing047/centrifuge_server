package csi.integration.hibernate.setup;

import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class WrapInTransactionCommand {

    public void execute(){
        CsiPersistenceManager.begin();
        try {
            withinTransaction();
            CsiPersistenceManager.commit();
        } catch (Exception e) {
            e.printStackTrace();
            CsiPersistenceManager.rollback();
            throw new RuntimeException(e);
        }
        finally {
            CsiPersistenceManager.close();
        }
    }

    protected abstract void withinTransaction() throws Exception;
}
