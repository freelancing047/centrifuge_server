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
package csi.server.common.model.visualization.chronos;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChronosViewDef extends VisualizationDef {

    /**
     * Purely for accessing server-side cache of the data for this visualization.
     */
    private String cacheKey;

    @OneToOne
    private ChronosSettings chronosSettings;

    public ChronosViewDef() {
        super();
        setType(VisualizationType.CHRONOS);
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public ChronosSettings getChronosSettings() {
        return chronosSettings;
    }

    public void setChronosSettings(ChronosSettings chronosSettings) {
        this.chronosSettings = chronosSettings;
    }

    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        //log.error("Attempting to clone unsupported visualization type \"ChronosViewDef\"");
        return null;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
