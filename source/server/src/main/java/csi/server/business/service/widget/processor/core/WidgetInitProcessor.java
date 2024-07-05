package csi.server.business.service.widget.processor.core;

import java.util.Map;

import csi.server.business.service.widget.common.constants.WidgetConstants;
import csi.server.business.service.widget.common.data.ClientRequest;
import csi.server.business.service.widget.common.data.ServerResponse;
import csi.server.business.service.widget.processor.api.Processor;

/**
 * Processor used for loading widgets .
 */
class WidgetInitProcessor implements Processor {

    /**
     * Processes message based on the client request type message (JSON unmarshalled object and action)
     *
     * @param request message to be processed received from client
     * @return Server response
     */
    public ServerResponse processMessage(ClientRequest request, Object extractedData) {
        ServerResponse response = new ServerResponse();
        Map<String, String> unmarshalledData = (Map<String, String>) request.getContent();
        if (WidgetConstants.LOAD_ACTION.equals(request.getAction())) {
            String widget = WidgetConstants.CENTRIFUGE_WIDGETS_PAGES + unmarshalledData.get(WidgetConstants.WIDGET) + WidgetConstants.JSP_EXTENSION;
            String position = unmarshalledData.get(WidgetConstants.POSITION);

            response.setJavaScript(WidgetConstants.LOAD_WIDGET_JAVASCRIPT_METHOD + "('" + position + "','" + widget + "');");
        }

        return response;
    }

}
