/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.model.visualization.matrix;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.AbstractMeasureDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MatrixMeasureDefinition extends AbstractMeasureDefinition {

    private int measureScaleMin;
    private int measureScaleMax;

    public MatrixMeasureDefinition() {
        super();
    }

    public int getMeasureScaleMin() {
        // FIXME: Remove later
        if (measureScaleMin == 0) {
            measureScaleMin = 5;
        }
        return measureScaleMin;
    }

    public void setMeasureScaleMin(int measureScale) {
        this.measureScaleMin = measureScale;
    }

    public int getMeasureScaleMax() {
        // FIXME: remove later.
        if (measureScaleMax == 0) {
            measureScaleMax = 100;
        }

        return measureScaleMax;
    }

    public void setMeasureScaleMax(int measureScaleMax) {
        this.measureScaleMax = measureScaleMax;
    }

    @Override
    public String getDefinitionName() {
        return "Measure";
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof MatrixMeasureDefinition) &&
              Objects.equal(getUuid(), ((MatrixMeasureDefinition) obj).getUuid()));
   }

    @Override
    public <T extends ModelObject> MatrixMeasureDefinition clone(Map<String, T> fieldMapIn) {

        MatrixMeasureDefinition myClone = new MatrixMeasureDefinition();

        super.cloneComponents(myClone, fieldMapIn);

        myClone.setMeasureScaleMin(getMeasureScaleMin());
        myClone.setMeasureScaleMax(getMeasureScaleMax());

        return myClone;
    }


    public <T extends ModelObject> MatrixMeasureDefinition copy(Map<String, T> fieldMapIn) {

        MatrixMeasureDefinition myClone = new MatrixMeasureDefinition();

        super.copyComponents(myClone, fieldMapIn);

        myClone.setMeasureScaleMin(getMeasureScaleMin());
        myClone.setMeasureScaleMax(getMeasureScaleMax());

        return myClone;
    }
}
