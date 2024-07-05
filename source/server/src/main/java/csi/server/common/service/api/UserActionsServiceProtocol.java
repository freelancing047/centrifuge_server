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

import csi.server.common.dto.PasswordChangeInfo;
import csi.server.common.dto.UserFields;
import csi.server.common.exception.CentrifugeException;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface UserActionsServiceProtocol extends VortexService {

    public List<UserFields> listUsers() throws CentrifugeException;


    public String changePassword(PasswordChangeInfo passwordChangeInfo) throws CentrifugeException;


    public void add(UserFields fields) throws CentrifugeException;


    public void update(UserFields fields) throws CentrifugeException;


    public void delete(UserFields fields) throws CentrifugeException;
    
    /**
     * @return Name of current logged in user.
     */
    public String getLoggedInUserName();
}
