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
package csi.server.gwt.vortex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.user.client.rpc.SerializationException;

import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.vortex.VortexRequest;

/**
 * Handles begin/commit sequence for persistence calls.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class PersistenceManagerFilter implements VortexServerFilter {
   private static final Logger LOG = LogManager.getLogger(PersistenceManagerFilter.class);
    
    @Override
    public <R> R filter(VortexInvocationContext invocationContext, VortexRequest request, VortexFilterChain chain)
            throws SerializationException {
        try {
            CsiPersistenceManager.begin();

            R result = chain.<R>filter(invocationContext, request);

            CsiPersistenceManager.flush();
            
            if (CsiPersistenceManager.isRollbackOnly()) {
                CsiPersistenceManager.rollback();
            } else {
                CsiPersistenceManager.commit();
            }
            
            return result;
        } catch (RuntimeException e) {
            CsiPersistenceManager.rollback();
            throw e;
        } catch (SerializationException e) {
            CsiPersistenceManager.rollback();
            throw e;
        } finally {
            try {
                CsiPersistenceManager.close();
            } catch (Throwable e) {
               LOG.error(e.getMessage(), e);
            }
        }
    }

}
