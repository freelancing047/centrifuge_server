package csi.server.common.model.listener;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.CleanUpThread;

/**
 * Created by centrifuge on 4/13/2017.
 */
public class AttributeDefListener {
    @PostPersist
    public void onPostPersist(AttributeDef parentIn) {

        parentIn.setFieldDefId(generateId(parentIn.getFieldDef()));
        parentIn.setTooltipLinkFieldDefId(generateId(parentIn.getTooltipLinkFeildDef()));
    }

    @PostLoad
    public void onPostLoad(AttributeDef parentIn) {

        parentIn.setFieldDefId(generateId(parentIn.getFieldDef()));
        parentIn.setTooltipLinkFieldDefId(generateId(parentIn.getTooltipLinkFeildDef()));
    }

    @PostUpdate
    public void onPostUpdate(AttributeDef parentIn) {

        FieldDef myFieldDef = parentIn.getFieldDef();
        FieldDef myTooltipFieldDef = parentIn.getTooltipLinkFeildDef();
        String myFieldDefId = parentIn.getFieldDefId();
        String myTooltipFieldDefId = parentIn.getTooltipLinkFieldDefId();

        if (null != myFieldDefId) {

            if ((null == myFieldDef) || !myFieldDef.getUuid().equals(myFieldDefId)) {

                removeIfNecessary(myFieldDefId);
            }
        }
        if (null != myTooltipFieldDefId) {

            if ((null == myTooltipFieldDef) || !myTooltipFieldDef.getUuid().equals(myFieldDefId)) {

                removeIfNecessary(myTooltipFieldDefId);
            }
        }
        parentIn.setFieldDefId(generateId(myFieldDef));
        parentIn.setTooltipLinkFieldDefId(generateId(myTooltipFieldDef));
    }

    @PostRemove
    public void onPostRemove(AttributeDef parentIn) {

        removeIfNecessary(parentIn.getFieldDef());
        removeIfNecessary(parentIn.getTooltipLinkFeildDef());
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
