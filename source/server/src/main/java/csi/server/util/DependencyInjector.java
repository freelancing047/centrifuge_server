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
package csi.server.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DependencyInjector implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    public void bind(Object instance) {
        AutowiredAnnotationBeanPostProcessor aaProcessor = getBeanFactory().getBean(
                AutowiredAnnotationBeanPostProcessor.class);
        if (aaProcessor == null) {
            throw new RuntimeException("No autowired annotation post processor found (type = "
                    + AutowiredAnnotationBeanPostProcessor.class.getName() + ").");
        } else {
            aaProcessor.processInjection(instance);
        }
    }
}
