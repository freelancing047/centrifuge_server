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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChronosSettings extends ModelObject implements Serializable {

    public ChronosSettings() {
        super();
    }

    @OneToMany
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<EventDefinition> events;

    @OneToMany
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<FieldDef> grouping = new ArrayList<FieldDef>();

    public List<FieldDef> getGrouping() {
        return grouping;
    }

    public void setGrouping(List<FieldDef> grouping) {
        this.grouping = grouping;
    }

    public List<EventDefinition> getEvents() {
        return events;
    }

    public void setEvents(List<EventDefinition> events) {
        this.events = events;
    }

}
