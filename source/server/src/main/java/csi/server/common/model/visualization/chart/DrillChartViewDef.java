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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;


/**
 * Definition of a Chart
 * 
 * @author Centrifuge Systems, Inc.
 */

//TODO: rename this to ChartViewDef after getting rid of the legacy ChartViewDef.
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DrillChartViewDef extends VisualizationDef implements Serializable {

    @OneToOne(cascade = CascadeType.ALL)
    private ChartSettings chartSettings;

    @OneToOne(cascade = CascadeType.ALL)
    private DrillCategory drillSelection = new DrillCategory();

    @Transient
    private ChartSelectionState selection = new ChartSelectionState();


    
    public DrillChartViewDef() {
        super();
        // TODO: Rename after getting rid of old chart type.
        setType(VisualizationType.DRILL_CHART);
    }

    public DrillCategory getDrillSelection() {
        return drillSelection;
    }

    public void setDrillSelection(DrillCategory drillSelection) {
        this.drillSelection = drillSelection;
    }

    public ChartSettings getChartSettings() {
        return chartSettings;
    }

    public void setChartSettings(ChartSettings chartSettings) {
        this.chartSettings = chartSettings;
    }
    
    @Override
    public ChartSelectionState getSelection() {
        return selection;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> DrillChartViewDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        DrillChartViewDef myClone = new DrillChartViewDef();
        
        super.cloneComponents(myClone, fieldMapIn, filterMapIn);

        if (null != getChartSettings()) {
            myClone.setChartSettings(getChartSettings().clone(fieldMapIn));
        }
        
        return myClone;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {

		DrillChartViewDef myCopy = new DrillChartViewDef();
        
        super.copyComponents(myCopy, fieldMapIn, filterMapIn);

        if (null != getChartSettings()) {
        	myCopy.setChartSettings(getChartSettings().copy(fieldMapIn));
        }
        
        return myCopy;
	}
}
