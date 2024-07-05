package csi.server.common.dto.graph.path;



import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class FindAllNodesMetaRequest implements IsSerializable {

    public String vizUUID;
    public List<Integer> findPathNodes = new ArrayList<Integer>();

}
