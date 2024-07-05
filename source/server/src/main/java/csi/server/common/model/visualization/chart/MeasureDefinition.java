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
package csi.server.common.model.visualization.chart;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.AbstractMeasureDefinition;
import csi.shared.core.visualization.chart.MeasureChartType;

/**
 * Information on measures.
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MeasureDefinition extends AbstractMeasureDefinition implements Serializable {

    public MeasureDefinition() {
        super();
    }

    private String color; // Hex color code including #

    @Enumerated(value = EnumType.STRING)
    private MeasureChartType measureChartType = MeasureChartType.COLUMN;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public MeasureChartType getMeasureChartType() {
        // TODO: Remove this.
        if (measureChartType == null) {
            return MeasureChartType.DEFAULT;
        }
        return measureChartType;
    }

    public void setMeasureChartType(MeasureChartType measureChartType) {
        this.measureChartType = measureChartType;
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
              (obj instanceof MeasureDefinition) &&
              Objects.equal(getUuid(), ((MeasureDefinition) obj).getUuid()));
   }

    @Override
    public <T extends ModelObject> MeasureDefinition clone(Map<String, T> fieldMapIn) {

        MeasureDefinition myClone = new MeasureDefinition();

        super.cloneComponents(myClone, fieldMapIn);

        myClone.setMeasureChartType(getMeasureChartType());
        myClone.setColor(getColor());

        return myClone;
    }

    public <T extends ModelObject> MeasureDefinition copy(Map<String, T> fieldMapIn) {

    	if(fieldMapIn.containsKey(this.getUuid())){
    		return (MeasureDefinition) fieldMapIn.get(this.getUuid());
    	}
        MeasureDefinition myCopy = new MeasureDefinition();

        super.copyComponents(myCopy, fieldMapIn);

        myCopy.setMeasureChartType(getMeasureChartType());
        myCopy.setColor(getColor());
        fieldMapIn.put(this.getUuid(), (T) myCopy);

        return myCopy;
    }
}
