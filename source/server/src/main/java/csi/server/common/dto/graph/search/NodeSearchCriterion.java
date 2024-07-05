package csi.server.common.dto.graph.search;



import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.visualization.graph.NodeDef;


public class NodeSearchCriterion implements IsSerializable {

    private NodeDef nodeDef;

    private List<AttributeCriterion> attributeCriteria;

    public List<AttributeCriterion> getAttributeCriteria() {
        if (attributeCriteria == null) {
            attributeCriteria = Collections.emptyList();
        }
        return attributeCriteria;
    }

    public void setAttributeCriteria(List<AttributeCriterion> attributeCriteria) {
        this.attributeCriteria = attributeCriteria;
    }

    public NodeDef getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(NodeDef nodeDef) {
        this.nodeDef = nodeDef;
    }
}
