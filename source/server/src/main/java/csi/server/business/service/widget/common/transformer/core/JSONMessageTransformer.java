package csi.server.business.service.widget.common.transformer.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import csi.server.business.service.widget.common.configuration.loader.WidgetPropertiesLoader;
import csi.server.business.service.widget.common.transformer.api.MessageTransformer;
import csi.server.business.service.widget.common.transformer.exception.TransformerException;

/**
 * Utility class used for Mashall/Unmarshall of JSON Messages
 */
class JSONMessageTransformer implements MessageTransformer {

    /**
     * Jackson object mapper implementation (reusable)
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Properties loader
     */
    private WidgetPropertiesLoader widgetsProperties = new WidgetPropertiesLoader();

    /**
     * Data objects package location
     */
    private static final String CSI_WIDGET_COMMON_DATA = "csi.widget.common.data.";

    /**
     * Unmarshalls an JSON type message to a POJO object based on the provided data type.
     *
     * @param JSONMessage    Message to unmarshallMessage
     * @param dataObjectType object type
     * @return POJO populated with values received from JSON message
     * @throws csi.server.business.service.widget.common.transformer.exception.TransformerException
     *          error in unmarshallMessage or class loading
     */
    public Object unmarshallMessage(String JSONMessage, String dataObjectType) throws TransformerException {
        assert JSONMessage != null : "JSON message is null";
        assert dataObjectType != null : "Unable to unarshall object. null provided data object type";

        Object unmarshalledObject = null;
        Class klass = null;
        String classLocation = null;
        try {
            classLocation = widgetsProperties.getStringValue(dataObjectType);

            klass = Class.forName(classLocation);
            unmarshalledObject = mapper.readValue(JSONMessage, klass);

        } catch (ClassNotFoundException e) {
            throw new TransformerException("Unable to unmarshallMessage provided JSON message. Unable to find model class: " + classLocation);
        } catch (Exception e) {
            throw new TransformerException("Error while unmarshalling JSON Message for type:" + dataObjectType + ". Reason: " + e.getMessage());
        }

        assert unmarshalledObject != null : "Null unmarshalled object from JSON message";

        return unmarshalledObject;

    }

    /**
     * Marshalls object to JSON type message
     *
     * @param objectToMarshall POJO to be marshalled
     * @return JSON message
     * @throws TransformerException marshalling error
     */
    public String marshallMessage(Object objectToMarshall) throws TransformerException {
        String marshalledObject = "";

        try {
            marshalledObject = mapper.writeValueAsString(objectToMarshall);
        } catch (IOException e) {
            throw new TransformerException("Unable to marshall object. Reason: " + e.getMessage());
        }

        return marshalledObject;
    }

    /**
     * Unmarshalls JSON message to map containing key - value pairs
     *
     * @param JSONMessage message to unmarshallMessage
     * @return map type collection
     * @throws TransformerException error while unmarshalling message
     */
    public Map<String, String> unmarshallToStringCollection(String JSONMessage) throws TransformerException {

        Map<String, String> str;
        try {
            JsonNode jsonNode = mapper.readTree(JSONMessage);
            str = new HashMap<String, String>();

            JsonParser parser = jsonNode.traverse();
            String fieldname;
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                fieldname = parser.getCurrentName();
                if (!fieldname.equals(parser.getText())) {
                    str.put(fieldname, parser.getText());
                }
            }

        } catch (IOException e) {
            throw new TransformerException("Unable to parse JSONMessage to simple JsonNode. Reason: " + e.getMessage());
        }

        return str;
    }

}
