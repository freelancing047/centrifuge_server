package csi.server.common.dto.graph.path;



import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class FindPathResponse implements IsSerializable {

    public List<PathMeta> foundPaths = new ArrayList<PathMeta>();

    public List<PathMeta> getFoundPaths() {
        return foundPaths;
    }

    public void setFoundPaths(List<PathMeta> foundPaths) {
        this.foundPaths = foundPaths;
    }

}
