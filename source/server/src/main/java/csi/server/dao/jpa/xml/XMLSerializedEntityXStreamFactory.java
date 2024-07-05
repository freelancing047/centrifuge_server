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
package csi.server.dao.jpa.xml;

import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import csi.server.util.xml.AdaptiveXStream;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class XMLSerializedEntityXStreamFactory implements InitializingBean {

    private XStream xstream;
    private static XMLSerializedEntityXStreamFactory instance;

    @Override
    public void afterPropertiesSet() throws Exception {
        xstream = new AdaptiveXStream();

        // Get all classes that have XStream annotation on them and add them.
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(XStreamAlias.class));
        Set<BeanDefinition> beans = provider.findCandidateComponents("csi.server.common");
        for (BeanDefinition bd : beans) {
            xstream.processAnnotations(ClassUtils.forName(bd.getBeanClassName(), null));
        }

        instance = this;
    }

    public static XStream get() {
        return instance.xstream;
    }
}
