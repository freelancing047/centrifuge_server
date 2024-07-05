package csi.server.business.service.widget.dataextractor.core;

import java.util.HashMap;
import java.util.Map;

import csi.server.business.service.widget.common.configuration.exception.ConfigurationException;
import csi.server.business.service.widget.common.configuration.loader.JsonActionMappingsLoader;
import csi.server.business.service.widget.common.data.ActionMapping;
import csi.server.business.service.widget.common.data.ActionMappings;
import csi.server.business.service.widget.dataextractor.api.DataExtractor;
import csi.server.business.service.widget.dataextractor.exception.DataExtractorException;

/**
 * Data extractor factory
 */
public class DataExtractorFactory {

    /**
     * Data extractors cache
     */
    private static Map<String, DataExtractor> dataExtractors = new HashMap<String, DataExtractor>();

    /**
     * action - class mappings
     */
    private static Map<String, String> dataExtractorClassMapping = new HashMap<String, String>();

    /**
     * mappings data object read from file
     */
    private static ActionMappings dataExtractorMappings;

    /**
     * Return data extractor implementation for specified action
     *
     * @param action action for data extractor
     * @return data extractor
     * @throws DataExtractorException error on data extractor initialization
     */
    public static DataExtractor getDataExtractorForAction(String action) throws DataExtractorException {

        assert action != null : "Cannot create date extractor for null action";

        if (dataExtractorMappings == null) {
            initializeExtractorMappings();
        }

        DataExtractor dataExtractor = null;
        String classMapping = dataExtractorClassMapping.get(action);
        if (classMapping != null) {
            dataExtractor = dataExtractors.get(classMapping);
        }

        boolean found = false;

        if (dataExtractor == null) {
            for (ActionMapping mapping : dataExtractorMappings.getActionMappings()) {
                for (String searchAction : mapping.getAction()) {
                    if (action.equals(searchAction)) {
                        if (dataExtractors.containsKey(mapping.getDataExtractorClass())) {
                            dataExtractor = dataExtractors.get(mapping.getDataExtractorClass());
                            dataExtractorClassMapping.put(action, mapping.getDataExtractorClass());
                        } else {
                            dataExtractor = (DataExtractor) getInstance(mapping.getDataExtractorClass());
                            dataExtractors.put(mapping.getDataExtractorClass(), dataExtractor);
                            dataExtractorClassMapping.put(action, mapping.getDataExtractorClass());
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

        assert (dataExtractor != null) : "Data extractor could not be found for action: " + action;

        return dataExtractor;

    }

    /**
     * Initialized action mappings object for provided classname
     *
     * @param className classname for the actionmappings to be initialized
     * @return ProcessorObject
     * @throws csi.server.business.service.widget.dataextractor.exception.DataExtractorException
     *          error on reading mappings file
     */
    private static Object getInstance(String className) throws DataExtractorException {
        assert (className != null) : "Null provided class name";
        Object classInstance = null;
        try {
            Class klass = Thread.currentThread().getContextClassLoader().loadClass(className);
            classInstance = klass.newInstance();
        } catch (Exception e) {
            throw new DataExtractorException("Unable to instantiate class for class name: " + className + ". Reason: " + e.getMessage());
        }

        assert (classInstance != null) : "Processor class instance is null";
        return classInstance;

    }

    /**
     * Initializes action mappings data mappings object
     *
     * @throws csi.server.business.service.widget.dataextractor.exception.DataExtractorException
     *          error on reading loader file
     */
    private static void initializeExtractorMappings() throws DataExtractorException {

        try {
            JsonActionMappingsLoader loader = new JsonActionMappingsLoader();
            dataExtractorMappings = loader.getMappingData();
        } catch (ConfigurationException e) {
            throw new DataExtractorException("Unable to load extractor data file. Reason: " + e.getMessage());
        }

    }
}