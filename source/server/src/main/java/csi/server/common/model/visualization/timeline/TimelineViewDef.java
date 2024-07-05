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
package csi.server.common.model.visualization.timeline;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.TimelineEventSelection;

/**
 * Definition of a Timeline
 *
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="timelineviewdef")
public class TimelineViewDef extends VisualizationDef{

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval=true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private TimelineSettings timelineSettings;

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval=true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private TimelineCachedState state;
        
	@Transient
    @XStreamOmitField
    private TimelineEventSelection selection = null;
	

    public TimelineViewDef() {
        super();
        setType(VisualizationType.CHRONOS); 
    }

	@Override
	public TimelineEventSelection getSelection() {
	    if(selection == null){
	        selection = new TimelineEventSelection(getUuid());
	    }
		return selection;
	}

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
	    
	    TimelineViewDef myCopy = new TimelineViewDef();
	    
        super.copyComponents(myCopy, fieldMapIn, filterMapIn);
        myCopy.setTimelineSettings(getTimelineSettings().copy(fieldMapIn));
        myCopy.setState(getState().copy());
        
		return myCopy;
	}

	public TimelineSettings getTimelineSettings() {
		return timelineSettings;
	}

	public void setTimelineSettings(TimelineSettings timelineSettings) {
		this.timelineSettings = timelineSettings;
	}

    public TimelineCachedState getState() {
        if(state == null) {
            state = new TimelineCachedState();
        }
        return state;
    }

    public void setState(TimelineCachedState state) {
        this.state = state;
    }


   
}
