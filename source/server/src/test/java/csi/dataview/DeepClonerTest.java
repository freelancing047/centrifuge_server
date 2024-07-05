package csi.dataview;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import csi.server.business.helper.DeepCloner;
import csi.server.business.helper.DeepCloner.CloneType;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataViewDef;

public class DeepClonerTest {

    @Test
    public void testCloneNewId() {
        DataViewDef def = new DataViewDef(ReleaseInfo.version);
        def.setModelDef(new DataModelDef());

        FieldDef f = new FieldDef(FieldType.COLUMN_REF);
        f.setFieldName("testField");
        f.setOrdinal(0);

        def.getModelDef().getFieldDefs().add(f);

        DataViewDef clone = DeepCloner.clone(def, CloneType.NEW_ID);

        assertTrue("def uuid should not match", !def.getUuid().equals(clone.getUuid()));
        assertTrue("model uuid should not match", !def.getModelDef().getUuid().equals(clone.getModelDef().getUuid()));
        assertTrue("field uuid should not match", !def.getModelDef().getFieldDefs().get(0).getUuid().equals(clone.getModelDef().getFieldDefs().get(0).getUuid()));
    }

    @Test
    public void testCloneExact() {
        DataViewDef def = new DataViewDef(ReleaseInfo.version);
        def.setModelDef(new DataModelDef());

        FieldDef f = new FieldDef(FieldType.COLUMN_REF);
        f.setFieldName("testField");
        f.setOrdinal(0);

        def.getModelDef().getFieldDefs().add(f);

        DataViewDef clone = DeepCloner.clone(def, CloneType.EXACT);

        assertTrue("def uuid does not match", def.getUuid().equals(clone.getUuid()));
        assertTrue("model uuid does not match", def.getModelDef().getUuid().equals(clone.getModelDef().getUuid()));
        assertTrue("field uuid does not match", def.getModelDef().getFieldDefs().get(0).getUuid().equals(clone.getModelDef().getFieldDefs().get(0).getUuid()));
    }

}
