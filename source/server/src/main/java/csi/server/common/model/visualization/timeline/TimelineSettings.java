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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimelineSettings extends ModelObject implements Serializable {


    @ManyToOne
    private FieldDef groupByField;
    

    @ManyToOne
    private FieldDef colorByField;
    
    @ManyToOne
    private FieldDef dotSize;

    private Boolean showSummary = true;
    
    private Boolean groupNameSpace = true;
    
    private Boolean sortAscending = true;
    
	@OneToMany(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<TimelineEventDefinition> events;
    
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<TimelineField> fieldList;
    
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<TimelineLegendDefinition> legendItems;

	public TimelineSettings() {
		super();
	}
	

	public List<TimelineField> getFieldList() {
		return fieldList;
	}


	public void setFieldList(List<TimelineField> fieldList) {
		this.fieldList = fieldList;
	}


    public List<TimelineEventDefinition> getEvents() {
        return events;
    }


    public void setEvents(List<TimelineEventDefinition> events) {
        this.events = events;
    }


    public FieldDef getGroupByField() {
        return groupByField;
    }


    public void setGroupByField(FieldDef groupByField) {
        this.groupByField = groupByField;
    }


    public FieldDef getColorByField() {
        return colorByField;
    }


    public void setColorByField(FieldDef colorByField) {
        this.colorByField = colorByField;
    }


    public List<TimelineLegendDefinition> getLegendItems() {
        return legendItems;
    }


    public void setLegendItems(List<TimelineLegendDefinition> legendItems) {
        this.legendItems = legendItems;
    }

    public <T extends ModelObject> TimelineSettings copy(Map<String, T> fieldMapIn){
        TimelineSettings myCopy = new TimelineSettings();
        myCopy.setFieldList(copyFieldList());
        myCopy.setEvents(copyEvents());
        myCopy.setColorByField(getColorByField());
        myCopy.setLegendItems(copyLegendItems());
        myCopy.setGroupByField(getGroupByField());
        myCopy.setDotSize(getDotSize());
        myCopy.setGroupNameSpace(getGroupNameSpace());
        myCopy.setShowSummary(getShowSummary());
        myCopy.setSortAscending(getSortAscending());
                
        return myCopy;
    }

    private <T extends ModelObject> List<TimelineField> copyFieldList(){
        
        List<TimelineField> myCopy = new ArrayList<TimelineField>();
        for(TimelineField field: getFieldList()){
            myCopy.add(field.copy());
        }
        
        return myCopy;
    }

    private <T extends ModelObject> List<TimelineEventDefinition> copyEvents(){
    
        List<TimelineEventDefinition> myCopy = new ArrayList<TimelineEventDefinition>();
        for(TimelineEventDefinition field: getEvents()){
            myCopy.add(field.copy());
        }
        
        return myCopy;
    }
  
    private <T extends ModelObject> List<TimelineLegendDefinition> copyLegendItems(){
        
        List<TimelineLegendDefinition> myCopy = new ArrayList<TimelineLegendDefinition>();
        for(TimelineLegendDefinition field: getLegendItems()){
            field.copy();
        }
        
        return myCopy;
    }


    public FieldDef getDotSize() {
        return dotSize;
    }


    public void setDotSize(FieldDef dotSize) {
        this.dotSize = dotSize;
    }


    public Boolean getShowSummary() {
        return showSummary;
    }


    public void setShowSummary(Boolean showSummary) {
        this.showSummary = showSummary;
    }


    public Boolean getGroupNameSpace() {
        return groupNameSpace;
    }


    public void setGroupNameSpace(Boolean groupNameSpace) {
        this.groupNameSpace = groupNameSpace;
    }


    public Boolean getSortAscending() {
        return sortAscending;
    }


    public void setSortAscending(Boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
}
