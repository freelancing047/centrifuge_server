package csi.server.common.model.listener;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.CleanUpThread;

/**
 * Created by centrifuge on 4/13/2017.
 */
public class DirectionDefListener {
    @PostPersist
    public void onPostPersist(DirectionDef parentIn) {

        parentIn.setFieldDefId(generateId(parentIn.getFieldDef()));
    }

    @PostLoad
    public void onPostLoad(DirectionDef parentIn) {

        parentIn.setFieldDefId(generateId(parentIn.getFieldDef()));
    }

    @PostUpdate
    public void onPostUpdate(DirectionDef parentIn) {

        FieldDef myFieldDef = parentIn.getFieldDef();
        String myFieldDefId = parentIn.getFieldDefId();

        if (null != myFieldDefId) {

            if ((null == myFieldDef) || !myFieldDef.getUuid().equals(myFieldDefId)) {

                removeIfNecessary(myFieldDefId);
            }
        }
        parentIn.setFieldDefId(generateId(myFieldDef));
    }

    @PostRemove
    public void onPostRemove(DirectionDef parentIn) {

        removeIfNecessary(parentIn.getFieldDef());
    }

    private String generateId(FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            return fieldDefIn.getUuid();

        } else {

            return null;
        }
    }

    private void removeIfNecessary(String fieldDefIdIn) {

        removeIfNecessary(CsiPersistenceManager.findObject(FieldDef.class, fieldDefIdIn));
    }

    private void removeIfNecessary(FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            if (fieldDefIn.notListedFieldDef()) {

                CleanUpThread.scheduleDelete(fieldDefIn);
            }
        }
    }
}
