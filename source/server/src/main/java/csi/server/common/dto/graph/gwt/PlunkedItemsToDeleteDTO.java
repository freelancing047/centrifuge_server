package csi.server.common.dto.graph.gwt;

import java.io.Serializable;
import java.util.ArrayList;

import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.PlunkedNode;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkedItemsToDeleteDTO implements Serializable{

    private ArrayList<PlunkedNode> nodesToDelete = new ArrayList<PlunkedNode>();
    private ArrayList<PlunkedLink> linksToDelete = new ArrayList<PlunkedLink>();

    public ArrayList<PlunkedNode> getNodesToDelete() {
        return nodesToDelete;
    }

    public void setNodesToDelete(ArrayList<PlunkedNode> nodesToDelete) {
        this.nodesToDelete = nodesToDelete;
    }

    public ArrayList<PlunkedLink> getLinksToDelete() {
        return linksToDelete;
    }

    public void setLinksToDelete(ArrayList<PlunkedLink> linksToDelete) {
        this.linksToDelete = linksToDelete;
    }
}
