/**
 * Copyright 2013 Centrifuge Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package csi.server.gwt.vortex;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import csi.server.util.reflect.ClassUtils;
import csi.shared.gwt.vortex.VortexService;

/**
 * Dynamically discovers VortexService interfaces and sets up reference to their implementing beans.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortexServiceRegistry implements ApplicationListener<ContextRefreshedEvent>, BeanFactoryAware {
   private static final Logger LOG = LogManager.getLogger(VortexServiceRegistry.class);

    private Map<String, VortexInvocationContext> invocationContextsByMethodSignature = new HashMap<String, VortexInvocationContext>();

    public Map<String, VortexInvocationContext> getInvocationContextsByMethodSignature() {
        return invocationContextsByMethodSignature;
    }

    private ListableBeanFactory beanFactory;

    public ListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    private List<VortexServerFilter> filters = new ArrayList<VortexServerFilter>();

    public List<VortexServerFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<VortexServerFilter> filters) {
        this.filters = filters;
    }

   @Override
   public void setBeanFactory(BeanFactory factory) throws BeansException {
      if (!(factory instanceof ListableBeanFactory)) {
         throw new RuntimeException("Expected to find instance of ListableBeanFactory.");
      }
      this.beanFactory = ((ListableBeanFactory) factory);
   }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Collection<VortexService> beans = getBeanFactory().getBeansOfType(VortexService.class).values();

        // Go through all the known beans and for each interface that the bean implements that also extends from
        // RpcEnabled, capture the bean that will implement the interface.

        for (VortexService vortexServiceBean : beans) {
           LOG.trace("Wiring bean " + vortexServiceBean.getClass().getName() + " for vortex RPC.");

            Class<?> clz = vortexServiceBean.getClass();

            // Get all interfaces implemented by this class.
            Set<Class<? extends VortexService>> implementedInterfaces = ClassUtils.getAllInterfacesOfType(clz,
                    VortexService.class);

            for (Class<? extends VortexService> interfaceOfInterest : implementedInterfaces) {
                for (Method method : interfaceOfInterest.getMethods()) {
                    VortexInvocationContext context = new VortexInvocationContext();
                    context.setMethod(method);
                    context.setServiceImplementation(vortexServiceBean);
                    context.setServiceInterface(interfaceOfInterest);
                    invocationContextsByMethodSignature
                            .put(ClassUtils.getTypedMethodSignature(interfaceOfInterest, vortexServiceBean, method),
                                    context);
                }
            }
        } // end iteration over beans
    }
}
