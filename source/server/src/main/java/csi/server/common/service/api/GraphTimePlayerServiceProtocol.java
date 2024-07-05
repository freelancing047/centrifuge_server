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

import java.util.Date;
import java.util.List;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.TimePlayerUnit;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface GraphTimePlayerServiceProtocol extends VortexService {

    public GraphPlayerSettings getTimePlayerSettings(String vizUuid) throws CentrifugeException;

    public List<String> seekPosition(String vizId, Integer newRelativePosition) throws CentrifugeException;
    public List<String> seek(String vizId, Long newRelativePosition) throws CentrifugeException;

    public List<String> stepPlayer(String vizUuid) throws CentrifugeException;

    public void stopPlayer(String vizUuid) throws CentrifugeException;

    List<String> activatePlayer(String dvUuid, String vizUuid, GraphPlayerSettings settings) throws CentrifugeException;

    public CsiMap<String, String> getEndPoints(String davaviewUuid, String graphUuid, String startField, String endField,
            int duration, TimePlayerUnit unit, Date start, Date end);


}
