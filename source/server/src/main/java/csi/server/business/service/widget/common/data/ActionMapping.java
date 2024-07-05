package csi.server.business.service.widget.common.data;

import java.util.List;

/**
 * Action mapping data class.
 * Contains class locations for data extractor and data processor and all
 * actions supported
 */
public class ActionMapping {

    /** Data processor class */
    private String processorClass;

    /** Data extractor class */
    private String dataExtractorClass;

    /** List of known supported actions */
    private List<String> action;

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(String processorClass) {
        this.processorClass = processorClass;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public String getDataExtractorClass() {
        return dataExtractorClass;
    }

    public void setDataExtractorClass(String dataExtractorClass) {
        this.dataExtractorClass = dataExtractorClass;
    }

}
