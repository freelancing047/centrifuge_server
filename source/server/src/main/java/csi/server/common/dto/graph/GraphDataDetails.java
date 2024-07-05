package csi.server.common.dto.graph;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;


public class GraphDataDetails implements IsSerializable {

    public List<CsiMap<String, String>> nodes = new ArrayList<CsiMap<String, String>>();
    public List<CsiMap<String, String>> links = new ArrayList<CsiMap<String, String>>();
}
