/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.common.model.visualization.map;

import com.google.common.base.Objects;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapBundleDefinition extends ModelObject implements Serializable {
    @ManyToOne
    private FieldDef fieldDef;
    private int listPosition;
    private String shapeString;
    private String colorString;
    private Boolean showLabel;
    private Boolean allowNulls;

    public MapBundleDefinition() {
        super();
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public String getShapeString() {
        return shapeString;
    }

    public void setShapeString(String shapeString) {
        this.shapeString = shapeString;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public String getColor() {
        return "#" + colorString;
    }

    public void setColor(String color) {
        colorString = color.substring(1);
    }

    public Boolean getShowLabel() {
        return showLabel;
    }

    public void setShowLabel(Boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isAllowNulls() {
        return allowNulls;
    }

    public void setAllowNulls(boolean suppressNulls) {
        this.allowNulls = suppressNulls;
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof MapBundleDefinition)) {
            return false;
        } else {
            MapBundleDefinition typed = (MapBundleDefinition) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }

    @Override
    public <T extends ModelObject> MapBundleDefinition clone(Map<String, T> fieldMapIn) {

        MapBundleDefinition myClone = new MapBundleDefinition();

        cloneComponents(myClone, fieldMapIn);

        myClone.setListPosition(listPosition);

        myClone.setShapeString(shapeString);

        myClone.setColorString(colorString);

        myClone.setShowLabel(showLabel);

        myClone.setAllowNulls(allowNulls);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void cloneComponents(MapBundleDefinition cloneIn, Map<String, T> fieldMapIn) {
        if (null != cloneIn) {
            super.cloneComponents(cloneIn);

            cloneIn.setFieldDef((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getFieldDef(), fieldMapIn));
        }
    }

    public <T extends ModelObject> MapBundleDefinition copy(Map<String, T> fieldMapIn) {
        if (fieldMapIn.containsKey(this.getUuid())) {
            return (MapBundleDefinition) fieldMapIn.get(this.getUuid());
        }

        MapBundleDefinition myCopy = new MapBundleDefinition();

        copyComponents(myCopy);

        myCopy.setListPosition(listPosition);

        myCopy.setShapeString(shapeString);

        myCopy.setColorString(colorString);

        myCopy.setShowLabel(showLabel);

        myCopy.setAllowNulls(allowNulls);

        fieldMapIn.put(this.getUuid(), (T) myCopy);

        return myCopy;
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void copyComponents(MapBundleDefinition copyIn) {
        if (null != copyIn) {
            super.copyComponents(copyIn);

            copyIn.setFieldDef(getFieldDef());
        }
    }
}
