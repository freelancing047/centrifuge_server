package csi.server.common.dto.graph;


import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;


public class GraphOperation implements IsSerializable {

    public String type;

    public String id;

    public String operation;

    public CsiMap<String, String> parameters;

}
