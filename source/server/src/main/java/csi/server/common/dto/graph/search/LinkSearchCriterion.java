package csi.server.common.dto.graph.search;



import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.visualization.graph.LinkDef;


public class LinkSearchCriterion implements IsSerializable {

    private LinkDef linkDef;

    private List<AttributeCriterion> attributeCriteria;

    private NodeSearchCriterion node1;
    private NodeSearchCriterion node2;

    public LinkDef getLinkDef() {
        return linkDef;
    }

    public void setLinkDef(LinkDef linkDef) {
        this.linkDef = linkDef;
    }

    public List<AttributeCriterion> getAttributeCriteria() {
        if (attributeCriteria == null) {
            attributeCriteria = new ArrayList<AttributeCriterion>();
        }
        return attributeCriteria;
    }

    public void setAttributeCriteria(List<AttributeCriterion> attributeCriteria) {
        this.attributeCriteria = attributeCriteria;
    }

    public NodeSearchCriterion getNode1() {
        return node1;
    }

    public void setNode1(NodeSearchCriterion node1) {
        this.node1 = node1;
    }

    public NodeSearchCriterion getNode2() {
        return node2;
    }

    public void setNode2(NodeSearchCriterion node2) {
        this.node2 = node2;
    }

}
