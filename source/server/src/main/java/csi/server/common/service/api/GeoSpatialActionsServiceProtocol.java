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

import csi.server.common.dto.CreateKmlRequest;
import csi.shared.gwt.vortex.VortexService;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface GeoSpatialActionsServiceProtocol extends VortexService {

    String createKML(CreateKmlRequest request);

    void saveKML(CreateKmlRequest request);
}
