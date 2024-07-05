package csi.server.business.service.widget.common.configuration.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import csi.server.business.service.widget.common.configuration.exception.ConfigurationException;
import csi.server.business.service.widget.common.constants.WidgetConstants;
import csi.server.business.service.widget.common.data.ActionMappings;
import csi.server.business.service.widget.common.transformer.api.MessageTransformer;
import csi.server.business.service.widget.common.transformer.core.MessageTransformerFactory;
import csi.server.business.service.widget.common.transformer.exception.TransformerException;

/**
 * JSON type message data file action mappings
 */
public class JsonActionMappingsLoader {

    /**
     * Mappings file name (without extension)
     */
    private static String bindingsDataFileName = null;

    /**
     * Mappings file extension (also used to define implementation. ex: json, xml)
     */
    private static String bindingsDataFileType = null;

    /**
     * Return file content as String in order to be unmarshalled into object
     *
     * @return file content
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          error while reading configuration file
     */
   protected String getDataFileAsString() throws ConfigurationException {
      StringBuilder sb = new StringBuilder();

      try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(getBindingsDataFileName() + "." + getBindingsDataFileType());
           InputStreamReader inputStreamReader = new InputStreamReader(stream);
           BufferedReader reader = new BufferedReader(inputStreamReader)) {
         String line;

         while ((line = reader.readLine()) != null) {
            sb.append(line);
         }
      } catch (IOException e) {
         throw new ConfigurationException("Unable to open stream for file: " + getBindingsDataFileName() + "." + getBindingsDataFileType());
      }
      return sb.toString();
   }

    /**
     * Returns mappings data file name
     *
     * @return name
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          unable to initialize loader bean
     */
    private static String getBindingsDataFileName() throws ConfigurationException {
        if (bindingsDataFileName == null) {
            initializeProperties();
        }
        return bindingsDataFileName;
    }

    /**
     * Returns processing data file type/extension (ex: json, xml)
     *
     * @return file type
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          unable to initialize loader bean
     */
    private static String getBindingsDataFileType() throws ConfigurationException {
        if (bindingsDataFileType == null) {
            initializeProperties();
        }
        return bindingsDataFileType;
    }

    /**
     * Loads file name and extension from the main property file
     *
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          unable to initialize loader file
     */
    private static void initializeProperties() throws ConfigurationException {
        WidgetPropertiesLoader widgetsProperties = new WidgetPropertiesLoader();
        try {
            bindingsDataFileName = widgetsProperties.getStringValue(PropertiesConstants.PROCESSOR_DATA_FILE_NAME);
            bindingsDataFileType = widgetsProperties.getStringValue(PropertiesConstants.PROCESSOR_DATA_FILE_TYPE);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Unable to read processing data file name and type from the main loader file");
        }
    }

    /**
     * Processor collection data object
     */
    private ActionMappings processorCollection;

    /**
     * Return action - class mappings object based on data read from file
     *
     * @return Data object containing action-class bindings
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException configuration exception
     */
    public ActionMappings getMappingData() throws ConfigurationException {
        if (processorCollection == null) {
            String data = getDataFileAsString();
            MessageTransformer messageTransfomer = MessageTransformerFactory.getTransformer(WidgetConstants.JACKSON_IMPLEMENTATION);
            try {
                processorCollection = (ActionMappings) messageTransfomer.unmarshallMessage(data, PropertiesConstants.PROCESSOR_COLLECTION_DATA_LOCATION);
            } catch (TransformerException e) {
                e.printStackTrace();
            }

        }

        assert (processorCollection != null) : "Unable to read actionmappings action data file";
        return processorCollection;
    }
}
