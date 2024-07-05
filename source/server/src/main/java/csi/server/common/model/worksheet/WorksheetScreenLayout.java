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
package csi.server.common.model.worksheet;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

/**
 * Stores the screen layout state of visualizations contained in the worksheet.

 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class WorksheetScreenLayout extends ModelObject implements Serializable {

    public WorksheetScreenLayout() {
        super();
    }

    public WorksheetScreenLayout(WorksheetDef worksheetDef) {
        super();
        this.worksheetDef = worksheetDef;
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "worksheetScreenLayout")
    private WorksheetDef worksheetDef;

    @OneToOne(cascade = CascadeType.ALL)
    private VisualizationWorksheetLayout layout = new VisualizationWorksheetLayout();

    private String activatedVisualizationUuid;

    public VisualizationWorksheetLayout getLayout() {
        return layout;
    }

    public void setLayout(VisualizationWorksheetLayout layout) {
        this.layout = layout;
    }

    public String getActivatedVisualizationUuid() {
        return activatedVisualizationUuid;
    }

    public void setActivatedVisualizationUuid(String activatedVisualizationUuid) {
        this.activatedVisualizationUuid = activatedVisualizationUuid;
    }

    public WorksheetDef getWorksheetDef() {
        return worksheetDef;
    }

    public void setWorksheetDef(WorksheetDef worksheetDef) {
        this.worksheetDef = worksheetDef;
    }

    public void clear() {
        getLayout().clear();
    }

    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> WorksheetScreenLayout clone(WorksheetDef worksheetIn, Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        WorksheetScreenLayout myClone = new WorksheetScreenLayout();
        
        super.cloneComponents(myClone);

        if (null != getLayout()) {
            myClone.setLayout(getLayout().clone(visualizationMapIn, fieldMapIn, filterMapIn));
        }
        myClone.setWorksheetDef(worksheetIn);
        if ((null != getActivatedVisualizationUuid()) && (null != visualizationMapIn.get(getActivatedVisualizationUuid()))) {
            myClone.setActivatedVisualizationUuid(visualizationMapIn.get(getActivatedVisualizationUuid()).getUuid());
        }
        
        return myClone;
    }
}
