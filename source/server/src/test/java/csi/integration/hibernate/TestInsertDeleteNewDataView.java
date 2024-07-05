package csi.integration.hibernate;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.integration.hibernate.setup.InsertSimpleDataView;
import csi.integration.hibernate.setup.WrapInTransactionCommand;
import csi.server.common.model.dataview.DataView;
import csi.server.dao.CsiPersistenceManager;

/**
 * Inserts a new data view and deletes it.
 * Test ensures delete removes the dataview from the database.
 * @author Centrifuge Systems, Inc.
 */
public class TestInsertDeleteNewDataView {

    @BeforeClass
    public static void setup(){
        InitializeCentrifuge.initialize();
    }

    @Test
    public void testDataViewExistsAndCanBeDeleted(){
        new WrapInTransactionCommand(){
            @Override
            protected void withinTransaction() throws Exception{
                String dvUuid = InsertSimpleDataView.saveNewDataView("newDataView");
                DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
                assertNotNull(dv);

                CsiPersistenceManager.deleteObject(DataView.class, dvUuid);
                CsiPersistenceManager.flush();

                dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
                assertNull(dv);
            }
        }.execute();

    }

}
