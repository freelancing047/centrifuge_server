package csi.server.business.service.widget.common.transformer.core;

import csi.server.business.service.widget.common.constants.WidgetConstants;
import csi.server.business.service.widget.common.transformer.api.MessageTransformer;

/**
 * Message transformer factory
 */
public class MessageTransformerFactory {

    /**
     * Returns transformer implementation for marshall/unmarshalling
     *
     * @param type transformer type
     * @return transformer implementation
     */
    public static MessageTransformer getTransformer(String type) {

        assert type != null : "Message transformer cannot be created on null type";

        MessageTransformer transformer = null;

        if (WidgetConstants.JACKSON_IMPLEMENTATION.equals(type)) {
            transformer = new JSONMessageTransformer();
        }

        assert transformer != null : "Unable to find message transformer";
        return transformer;
    }

}
