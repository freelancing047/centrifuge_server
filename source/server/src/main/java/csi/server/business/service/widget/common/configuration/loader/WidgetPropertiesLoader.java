package csi.server.business.service.widget.common.configuration.loader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

import csi.server.business.service.widget.common.configuration.exception.ConfigurationException;

/**
 * Properties configuration for widget.loader file
 */
public class WidgetPropertiesLoader {

    /**
     * Loaded loader
     */
    private Properties loadedProperties;

    /**
     * Loads property file from current classpath and populates
     * loader object with key/values pairs
     *
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          property file could not be loaded from classpath
     */
   private void initializeProperties() throws ConfigurationException {
      loadedProperties = new Properties();

      try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(getFileName());
           BufferedInputStream bufferedInputStream = new BufferedInputStream(stream)) {
         loadedProperties.load(bufferedInputStream);
      } catch (Throwable e) {
         e.printStackTrace();
         throw new ConfigurationException("Unable to initialize loader file");
      }
   }

    /**
     * Returns property value as Integer type object
     *
     * @param property property name
     * @return property value as integer
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          property does not exist in loader file
     */
    public Integer getIntegerValue(String property) throws ConfigurationException {

        Integer value = null;
        if (loadedProperties == null) {
            initializeProperties();
        }

        try {
            value = Integer.valueOf(loadedProperties.getProperty(property));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Unable to convert value to Integer for property " + property);
        }

        if (value == null) {
            throw new ConfigurationException("Unable to find value for property: " + property);
        }

        return value;

    }

    /**
     * Returns property value as String type object
     *
     * @param property property name
     * @return property value
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          property does not exist in loader file
     */
    public String getStringValue(String property) throws ConfigurationException {

        String value = null;
        if (loadedProperties == null) {
            initializeProperties();
        }

        value = String.valueOf(loadedProperties.getProperty(property));

        if (value == null) {
            throw new ConfigurationException("Unable to find value for property: " + property);
        }

        return value;

    }

    /**
     * Reads object at property name
     *
     * @param property property name
     * @return object containing property value
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          property does not exist in loader file
     */
    public Object getValue(String property) throws ConfigurationException {

        Object value = null;
        if (loadedProperties == null) {
            initializeProperties();
        }

        value = loadedProperties.getProperty(property);

        if (value == null) {
            throw new ConfigurationException("Unable to find value for property: " + property);
        }

        return value;
    }

    /**
     * Returns property value as Boolean type object
     *
     * @param property property name
     * @return property value as boolean
     * @throws csi.server.business.service.widget.common.configuration.exception.ConfigurationException
     *          property does not exist in loader file
     */
    public Boolean getBooleanValue(String property) throws ConfigurationException {

        Boolean value;
        if (loadedProperties == null) {
            initializeProperties();
        }

        value = Boolean.valueOf(loadedProperties.getProperty(property));

        if (value == null) {
            throw new ConfigurationException("Unable to find value for property: " + property);
        }

        return value;

    }

    /**
     * Return file name for the property file to load.
     *
     * @return file name for current property bean
     */
    protected String getFileName() {
        return PropertiesConstants.WIDGETS_PROPERTIES;
    }
}
