package csi.server.business.service.widget.processor.api;

import csi.server.business.service.widget.common.data.ClientRequest;
import csi.server.business.service.widget.common.data.ServerResponse;

/**
 * Data object processor api
 */
public interface Processor {

    /**
     * Processes message based on the client request type message (JSON unmarshalled object and action)
     *
     * @param request message to be processed received from client
     * @param extractedData extracted data
     * @return Server response
     */
    ServerResponse processMessage(ClientRequest request, Object extractedData);

}
