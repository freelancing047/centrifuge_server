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
package csi.server.util;

import com.thoughtworks.xstream.XStream;

import csi.server.dao.jpa.xml.XMLSerializedEntityXStreamFactory;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ObjectUtil {

    /**
     * This general-purpose cloning method uses XML conversion to achieve deep-copy cloning. 
     * @param instance Instance to clone
     * @return Cloned instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T instance) {
        XStream xs = XMLSerializedEntityXStreamFactory.get();
        return (T) xs.fromXML(xs.toXML(instance));
    }
}
