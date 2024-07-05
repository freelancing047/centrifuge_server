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
package csi.server.dao.jpa;

import javax.persistence.PostLoad;

import com.google.common.base.Strings;

import csi.server.common.model.FieldDef;
import csi.server.common.model.UUID;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FieldDefEntityListener {

    @PostLoad
    public void postProcess(FieldDef fieldDef) {
        if (Strings.isNullOrEmpty(fieldDef.getLocalId())) {
            fieldDef.setLocalId(UUID.randomUUID());
        }
    }
}
