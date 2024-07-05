package csi.server.common.dto.graph;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class GraphInfo implements IsSerializable {

    public int id;

    public int nodeCount;
    public int edgeCount;
    public int visualizedNodeCount;

    public List<TypeInfo> nodeTypes = new ArrayList<TypeInfo>();
    public List<TypeInfo> edgeTypes = new ArrayList<TypeInfo>();

    public String imageLocation;

    public int renderThreshold;

}
