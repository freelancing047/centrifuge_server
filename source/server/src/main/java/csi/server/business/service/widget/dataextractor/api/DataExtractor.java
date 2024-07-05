package csi.server.business.service.widget.dataextractor.api;

import javax.servlet.http.HttpServletRequest;

import csi.server.business.service.widget.common.data.ClientRequest;
import csi.server.business.service.widget.dataextractor.exception.DataExtractorException;

/**
 * Data extractor api
 */
public interface DataExtractor {

    /**
     * Extract required information in order to be processed
     *
     * @param request       servlet request used to read data persisted on Http Session
     * @param clientRequest client request object
     * @return data extraction result
     * @throws DataExtractorException error on data extraction
     */
    Object extract(HttpServletRequest request, ClientRequest clientRequest) throws DataExtractorException;
}
