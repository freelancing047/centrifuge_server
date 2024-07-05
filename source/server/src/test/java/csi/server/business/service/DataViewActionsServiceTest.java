package csi.server.business.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import csi.integration.hibernate.setup.InitializeCentrifuge;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.dao.CsiPersistenceManager;

public class DataViewActionsServiceTest {

    private DataViewActionsService svc;

    @Before
    public void setUp() throws Exception {
        InitializeCentrifuge.initialize();
        CsiPersistenceManager.begin();
        svc = new DataViewActionsService();
    }

    @After
    public void tearDown() throws Exception {
        svc = null;
        CsiPersistenceManager.close();
    }

    @Test
    public void testDataviewNameExists() throws Exception {
        String name1 = "Untitled 1";
        String name2 = "Untitled 2";
        String name3 = "Untitled 3";
        DataView dv1 = createTestDataView(name1, "dataview1");
        DataView dv2 = createTestDataView(name2, "dataview2");

        ModelHelper.save(dv1);
        CsiPersistenceManager.flush();
        ModelHelper.save(dv2);
        CsiPersistenceManager.flush();

        assertTrue(svc.dataviewNameExists(name1));
        assertTrue(svc.dataviewNameExists(name2));
        assertFalse(svc.dataviewNameExists(name3));
    }

    private DataView createTestDataView(String name, String remarks) throws Exception {
        DataViewDef dvdef = new DataViewDef(ReleaseInfo.version);
        dvdef.setName(name);
        dvdef.setRemarks(remarks);
        dvdef.setTemplate(true);

        DataViewHelper dvHelper = new DataViewHelper();
        DataView dv = dvHelper.createDataView(name, dvdef);

        return dv;
    }
}
