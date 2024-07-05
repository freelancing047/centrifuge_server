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
package csi.server.common.model.visualization;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractAttributeDefinition extends ModelObject implements Serializable {

    public AbstractAttributeDefinition() {
        super();
    }

    /**
     * @return Display name that is composed with aggregate/bundle function.
     */
    public abstract String getComposedName();

    /**
     * @return Type taking into consideration the bundle/aggregate function.
     */
    public abstract CsiDataType getDerivedType();

    /**
     * @return Field reference
     */
    public abstract FieldDef getFieldDef();

    /**
     * @return Description of this definition.
     */
    public abstract String getDefinitionName();

    public abstract int getListPosition();
}
