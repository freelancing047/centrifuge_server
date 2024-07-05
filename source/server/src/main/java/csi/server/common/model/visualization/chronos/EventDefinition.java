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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventDefinition extends ModelObject implements Serializable {


    public EventDefinition() {
        super();
    }

    private String name;

    @OneToOne
    private BundledFieldReference start;

    @OneToOne
    private BundledFieldReference end;

    @OneToOne
    private BundledFieldReference duration;

    @Enumerated(value = EnumType.STRING)
    private DurationType durationType;

    @Enumerated(value = EnumType.STRING)
    private EventEndpointShape shape;

    @Enumerated(value = EnumType.STRING)
    private EventBarStyle barStyle;

    @OneToMany
    private List<TooltipDefinition> tooltip = new ArrayList<TooltipDefinition>();

    @OneToOne
    private BundledFieldReference eventType;

    @OneToMany
    private List<BundledFieldReference> bundleDefinition = new ArrayList<BundledFieldReference>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventEndpointShape getShape() {
        return shape;
    }

    public void setShape(EventEndpointShape shape) {
        this.shape = shape;
    }

    public EventBarStyle getBarStyle() {
        return barStyle;
    }

    public void setBarStyle(EventBarStyle barStyle) {
        this.barStyle = barStyle;
    }

    public List<TooltipDefinition> getTooltip() {
        return tooltip;
    }

    public void setTooltip(List<TooltipDefinition> tooltip) {
        this.tooltip = tooltip;
    }

    public BundledFieldReference getStart() {
        return start;
    }

    public void setStart(BundledFieldReference start) {
        this.start = start;
    }

    public BundledFieldReference getEnd() {
        return end;
    }

    public void setEnd(BundledFieldReference end) {
        this.end = end;
    }

    public BundledFieldReference getDuration() {
        return duration;
    }

    public void setDuration(BundledFieldReference duration) {
        this.duration = duration;
    }

    public BundledFieldReference getEventType() {
        return eventType;
    }

    public void setEventType(BundledFieldReference eventType) {
        this.eventType = eventType;
    }

    public List<BundledFieldReference> getBundleDefinition() {
        return bundleDefinition;
    }

    public void setBundleDefinition(List<BundledFieldReference> bundleDefinition) {
        this.bundleDefinition = bundleDefinition;
    }

    public DurationType getDurationType() {
        return durationType;
    }

    public void setDurationType(DurationType durationType) {
        this.durationType = durationType;
    }

    public List<BundledFieldReference> getFields() {
        List<BundledFieldReference> list = new ArrayList<BundledFieldReference>();
        if (start != null) {
            list.add(start);
        }
        if (end != null) {
            list.add(end);
        }
        if (duration != null) {
            list.add(duration);
        }
        if (eventType != null) {
            list.add(eventType);
        }
        list.addAll(bundleDefinition);
        for (TooltipDefinition td : tooltip) {
            list.add(td.getField());
        }
        return list;
    }

}
