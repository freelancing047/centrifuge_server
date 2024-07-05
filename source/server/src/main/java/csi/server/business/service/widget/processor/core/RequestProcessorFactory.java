package csi.server.business.service.widget.processor.core;

import java.util.HashMap;
import java.util.Map;

import csi.server.business.service.widget.common.configuration.exception.ConfigurationException;
import csi.server.business.service.widget.common.configuration.loader.JsonActionMappingsLoader;
import csi.server.business.service.widget.common.data.ActionMapping;
import csi.server.business.service.widget.common.data.ActionMappings;
import csi.server.business.service.widget.processor.api.Processor;
import csi.server.business.service.widget.processor.exception.ProcessorException;

/**
 * Request Message actionmappings factory.
 */
public class RequestProcessorFactory {

    /**
     * Action actionmappings cache (class name, actionmappings)
     */
    private static Map<String, Processor> actionProcessors = new HashMap<String, Processor>();

    /**
     * Actions cache (action , class name)
     */
    private static Map<String, String> actionClassMapping = new HashMap<String, String>();

    /**
     * Processing Mappings Data object
     */
    private static ActionMappings actionsMapping;

    /**
     * Returns actionmappings for specified action.
     *
     * @param action action name
     * @return processor for mapped action
     * @throws csi.server.business.service.widget.processor.exception.ProcessorException error while processing
     */
    public static Processor getProcessorForAction(String action) throws ProcessorException {

        if (actionsMapping == null) {
            initializeProcessorMapping();
        }

        Processor actionProcessor = null;
        String classMapping = actionClassMapping.get(action);
        if (classMapping != null) {
            actionProcessor = actionProcessors.get(classMapping);
        }

        boolean found = false;

        if (actionProcessor == null) {
            for (ActionMapping processor : actionsMapping.getActionMappings()) {
                for (String searchAction : processor.getAction()) {
                    if (action.equals(searchAction)) {
                        if (actionProcessors.containsKey(processor.getProcessorClass())) {
                            actionProcessor = actionProcessors.get(processor.getProcessorClass());
                            actionClassMapping.put(action, processor.getProcessorClass());
                        } else {
                            actionProcessor = (Processor) initializeClass(processor.getProcessorClass());
                            actionProcessors.put(processor.getProcessorClass(), actionProcessor);
                            actionClassMapping.put(action, processor.getProcessorClass());
                        }
                        found = true;
                        break;
                    }
                }

                if (found) {
                    break;
                }
            }

        }

        assert (actionProcessor != null) : "Action " + action + " could not be processed";
        return actionProcessor;

    }

    /**
     * Initialized actionmappings object for provided classname
     *
     * @param className classname for the actionmappings to be initialized
     * @return ProcessorObject
     * @throws csi.server.business.service.widget.processor.exception.ProcessorException
     *          unable to initialize actionmappings object
     */
    private static Object initializeClass(String className) throws ProcessorException {
        assert (className != null) : "Null provided class name";
        Object classInstance = null;
        try {
            Class klass = Thread.currentThread().getContextClassLoader().loadClass(className);
            classInstance = klass.newInstance();
        } catch (Exception e) {
            throw new ProcessorException("Unable to instantiate class for class name: " + className + ". Reason: " + e.getMessage());
        }

        assert (classInstance != null) : "Processor class instance is null";
        return classInstance;

    }

    /**
     * Initializes actionmappings data mappings object
     *
     * @throws csi.server.business.service.widget.processor.exception.ProcessorException
     *          unable to load actionmappings data file
     */
    private static void initializeProcessorMapping() throws ProcessorException {

        try {
            JsonActionMappingsLoader loader = new JsonActionMappingsLoader();
            actionsMapping = loader.getMappingData();
        } catch (ConfigurationException e) {
            throw new ProcessorException("Unable to load actionmappings data file. Reason: " + e.getMessage());
        }
    }

}
