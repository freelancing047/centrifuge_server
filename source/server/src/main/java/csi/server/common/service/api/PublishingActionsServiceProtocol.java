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
package csi.server.common.service.api;

import csi.shared.core.publish.PublishRequest;
import csi.shared.core.publish.SnapshotImagingRequest;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface PublishingActionsServiceProtocol extends VortexService {

    /**
     * @param request Collection of ImagingRequest instances
     * @return Base64 url encoded image data for the imaging requests.
     */
    public SnapshotImagingResponse getImages(SnapshotImagingRequest request);
    
    
    /**
     * Publishes the given request.
     * @param request 
     */
    public void publish(PublishRequest request);
}
