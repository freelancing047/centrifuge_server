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
package csi.server.common.model;

import java.io.Serializable;
import java.util.List;


/**
 * Marker interface for any entity that has a reference to fields that are stored in "FieldReference" objects within
 * its structure. 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface FieldReferencingEntity extends Serializable {

    /**
     * @return List of fields that are referenced using FieldReference objects.
     */
    public List<FieldDef> getReferencedFields();
}
