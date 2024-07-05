package csi.server.common.dto.graph;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class GraphRequest implements IsSerializable {

    public boolean allNodes;
    public boolean allLinks;

    public boolean useSearchResults;

    public List<Integer> nodes = new ArrayList<Integer>();

    public List<Integer> links = new ArrayList<Integer>();
}
