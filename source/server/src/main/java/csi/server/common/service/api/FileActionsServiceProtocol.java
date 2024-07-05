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

import java.util.List;

import csi.server.common.dto.FileValidation;
import csi.server.common.exception.CentrifugeException;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface FileActionsServiceProtocol extends VortexService {

    public FileValidation validateName(String filename) throws CentrifugeException;
    
    /**
     * Can throw SecurityException or FileNotFoundException
     * @param resourceName Path to resource relative to /resource folder.
     * @return Contents of the resource.
     */
    public String getApplicationResource(String resourceName);
    
    
    /**
     * Can throw SecurityException.
     * @param resourceName Path to resource relative to /resource folder.
     * @return Contents of the resource.
     */
    public List<String> getApplicationResourceDirectories(String resourceName);
    
    /**
     * @param resourceName Path to resource relative to /resource folder.
     * @return
     */
    public List<String> getApplicationResourceFiles(String resourceName);

    public Long getMaximumClientBufferSize();
}
