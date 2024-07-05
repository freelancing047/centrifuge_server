package csi.server.common.model.visualization.graph;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "graphcachedstate")
public class GraphCachedState extends ModelObject {

    @Basic(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Type(type = "jsonb")
    @Column(columnDefinition = "json")
    private ArrayList<String> legendOrder;

    @Basic(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Type(type = "jsonb")
    @Column(columnDefinition = "json")
    private HashMap<String, ArrayList> nodeLegendDecorations;

    @Basic(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Type(type = "jsonb")
    @Column(columnDefinition = "json")
    private HashMap<String, ArrayList> linkLegendDecorations;

    public ArrayList<String> getLegendOrder() {
        return legendOrder;
    }

    public void setLegendOrder(ArrayList<String> legendOrder) {
        this.legendOrder = legendOrder;
    }

    public HashMap<String, ArrayList> getNodeLegendDecorations() {
        return nodeLegendDecorations;
    }

    public void setNodeLegendDecorations(HashMap<String, ArrayList> nodeLegendDecorationsIn) {
        nodeLegendDecorations = nodeLegendDecorationsIn;
    }

    public HashMap<String, ArrayList> getLinkLegendDecorations() {
        return linkLegendDecorations;
    }

    public void setLinkLegendDecorations(HashMap<String, ArrayList> linkLegendDecorationsIn) {
        linkLegendDecorations = linkLegendDecorationsIn;
    }
}
