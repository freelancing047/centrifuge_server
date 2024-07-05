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
package csi.server.util.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class AdaptiveXStream extends XStream {
   @Override
   protected MapperWrapper wrapMapper(MapperWrapper next) {
      return new MapperWrapper(next) {
         @SuppressWarnings("rawtypes")
         @Override
         public boolean shouldSerializeMember(Class defined, String fieldName) {
            return (defined != Object.class) && super.shouldSerializeMember(defined, fieldName);
         }
      };
   }
}
