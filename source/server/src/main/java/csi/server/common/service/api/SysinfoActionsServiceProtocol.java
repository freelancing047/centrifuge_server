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

import java.io.IOException;

import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.LicenseInfoData;
import csi.server.common.dto.SystemInfoData;
import csi.server.common.exception.CentrifugeException;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface SysinfoActionsServiceProtocol extends VortexService {

    public SystemInfoData systemInfo() throws IOException, CentrifugeException;


    public LicenseInfoData licenseInfo() throws IOException, CentrifugeException;


    public String taskStatus() throws IOException, CentrifugeException;


    public ClientStartupInfo startupInfo();
}
