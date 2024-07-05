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
package csi.server.business.service.chronos;

import java.util.HashMap;
import java.util.Map;

import csi.server.common.model.visualization.chronos.ChronosEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChronosData {

    private Map<String, ChronosEvent> eventsByBundleValue = new HashMap<String, ChronosEvent>();
    
    // TODO: When adding event, accumulate unique set of event types.
    // Also bundle events together if bundling is specified.
    public void addEvent(ChronosEvent event) {
        handleBundling(event);

    }

    private void handleBundling(ChronosEvent event) {
        if (event.isBundled()) {
            String key = event.getBundleKey();
            ChronosEvent bundled = eventsByBundleValue.get(key);
            if (bundled != null) {
                bundled.bundle(event);
            } else {
                eventsByBundleValue.put(key, event);
            }
        }
    }

}
