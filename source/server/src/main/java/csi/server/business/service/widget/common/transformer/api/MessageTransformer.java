package csi.server.business.service.widget.common.transformer.api;

import java.util.Map;

import csi.server.business.service.widget.common.transformer.exception.TransformerException;

/**
 * <p>Message transformer API.</p>
 */
public interface MessageTransformer {

    /**
     * Unmarshalls message to a POJO object based on the provided data type.
     *
     * @param message    Message to unmarshallMessage
     * @param dataObjectType object type
     * @return POJO populated with values received from message
     * @throws csi.server.business.service.widget.common.transformer.exception.TransformerException
     *          error in unmarshallMessage or class loading
     */
    Object unmarshallMessage(String message, String dataObjectType) throws TransformerException;

    /**
     * Marshalls object to message
     *
     * @param objectToMarshall POJO to be marshalled
     * @return message
     * @throws TransformerException marshalling error
     */
    String marshallMessage(Object objectToMarshall) throws TransformerException;

    /**
    * Unmarshalls message to map containing key - value pairs
    *
    * @param message message to unmarshallMessage
    * @return map type collection
    * @throws csi.server.business.service.widget.common.transformer.exception.TransformerException error while unmarshalling message
    */
    Map<String, String> unmarshallToStringCollection(String message) throws TransformerException;

}
