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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import csi.shared.core.util.TypedClone;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class ChronosEvent implements Serializable, TypedClone<ChronosEvent> {

    private long start;
    private long end;
    private long durationInMillis;
    private String eventType;
    private List<String> bundleValues = new ArrayList<String>();
    private List<TooltipEntry> tooltipEntries = new ArrayList<TooltipEntry>();
    private EventDefinition eventDefinition;
    private List<ChronosEvent> bundledEvents = new ArrayList<ChronosEvent>();

    public void compute(Date start, Date end, long duration, DurationType durationType) {
        if (start != null) {
            this.start = start.getTime();
        }
        if (end != null) {
            this.end = end.getTime();
        }
        if ((start != null) && (end != null)) {
            durationInMillis = end.getTime() - start.getTime();
        } else if (start != null) {
            durationInMillis = durationType.toMillis(duration);
        } else {
            durationInMillis = durationType.toMillis(duration);
        }
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String value) {
        this.eventType = value;
    }

    public void addBundleField(String value) {
        bundleValues.add(value);
    }

    public void addTooltip(boolean hyperLink, String title, String value) {
        tooltipEntries.add(new TooltipEntry(hyperLink, title, value));
    }

    public List<TooltipEntry> getTooltipEntries() {
        return tooltipEntries;
    }

    public void setEventDefinition(EventDefinition eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    public boolean isBundled() {
        return !bundleValues.isEmpty();
    }

    public String getBundleKey() {
        return bundleValues.stream().collect(Collectors.joining(":"));
    }

    public void bundle(ChronosEvent event) {
        // The first time around, we add a copy of ourself so that this event's original start/end times are not lost.
        if (bundledEvents.isEmpty()) {
            bundledEvents.add(this.getClone());
        }
        bundledEvents.add(event);
        // Adjust start/end of this event if required.
        if (event.start < this.start) {
            this.start = event.start;
        }
        if (event.end > this.end) {
            this.end = event.end;
        }
        durationInMillis = end - start;
    }

    @Override
    public ChronosEvent getClone() {
        ChronosEvent clone = new ChronosEvent();
        clone.start = this.start;
        clone.end = this.end;
        clone.durationInMillis = this.durationInMillis;
        clone.eventType = this.eventType;
        clone.bundleValues = new ArrayList<String>(this.bundleValues);
        clone.tooltipEntries = new ArrayList<TooltipEntry>(this.tooltipEntries);
        clone.eventDefinition = this.eventDefinition;
        return clone;
    }
}
