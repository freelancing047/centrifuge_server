package csi.server.common.dto.graph;


import csi.server.common.dto.CsiMap;


public class GraphSelectionOperation extends GraphRequest {

    public String selectionId;

    public String operation;

    public CsiMap<String, String> parameters;

}
