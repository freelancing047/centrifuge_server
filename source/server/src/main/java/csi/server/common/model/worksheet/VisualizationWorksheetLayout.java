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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class VisualizationWorksheetLayout extends ModelObject implements Serializable {

    @Transient
    private boolean dirty;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<VisualizationLayoutState> layouts;

    public VisualizationWorksheetLayout() {
        super();
    }

    public void removeVisualizationLayoutState(String vizUuid) {
        VisualizationLayoutState state = findStateByUuid(vizUuid);
        if (state != null) {
            state.setVisualizationDef(null);
            getLayouts().remove(state);
        }
    }

    private VisualizationLayoutState findStateByUuid(String vizUuid) {
        for (VisualizationLayoutState vls : getLayouts()) {
            if (vls.getVisualizationDef().getUuid().equals(vizUuid)) {
                return vls;
            }
        }

        return null;
    }

    public VisualizationLayoutState getLayoutState(VisualizationDef visualizationDef) {
        VisualizationLayoutState state = findStateByUuid(visualizationDef.getUuid());
        if (state == null) {
            state = new VisualizationLayoutState();
            state.setVisualizationDef(visualizationDef);
            getLayouts().add(state);
            dirty = true;
        }
        return state;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clear() {
        dirty = false;
    }

    public List<VisualizationLayoutState> getLayouts() {
        if(layouts == null) {
            layouts = new ArrayList<VisualizationLayoutState>();
        }
        return layouts;
    }

    public void setLayouts(List<VisualizationLayoutState> layouts) {
        this.layouts = layouts;
    }
    
    @Override
    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> VisualizationWorksheetLayout clone(Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        VisualizationWorksheetLayout myClone = new VisualizationWorksheetLayout();
        
        super.cloneComponents(myClone);

        myClone.setLayouts(cloneVisualizationLayoutStates(visualizationMapIn, fieldMapIn, filterMapIn));
        
        return myClone;
    }
    
    private <T extends ModelObject, S extends ModelObject, R extends ModelObject> List<VisualizationLayoutState> cloneVisualizationLayoutStates(Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        if (null != getLayouts()) {
            
            List<VisualizationLayoutState>  myList = new ArrayList<VisualizationLayoutState>();
            
            for (VisualizationLayoutState myItem : getLayouts()) {
                
                myList.add(myItem.clone(visualizationMapIn, fieldMapIn, filterMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
}
