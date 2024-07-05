/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.service.api;

import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface BroadcastServiceProtocol extends VortexService {

    public Selection broadcastFilter(BroadcastRequest request);

    public Selection broadcastSelection(BroadcastRequest request);

    public Selection clearBroadcast(BroadcastRequest request);
    
    public void clearSelection(BroadcastRequest request);

    void invalidateDataviewBroadcast(String uuid);

    public boolean isBroadcast(String uuid);

    void pinBroadcast(BroadcastRequest request);

    void unpinBroadcast(BroadcastRequest request);
}
