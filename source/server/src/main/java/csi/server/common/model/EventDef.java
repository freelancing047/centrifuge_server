package csi.server.common.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.visualization.graph.NodeDef;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventDef extends ModelObject {

    @ManyToOne(cascade = { CascadeType.ALL })
    protected NodeDef eventNode;

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected List<NodeDef> participantDefs;

    public EventDef() {
    	super();
    }

    public NodeDef getEventNode() {
        return eventNode;
    }

    public void setEventNode(NodeDef eventNode) {
        this.eventNode = eventNode;
    }

    public List<NodeDef> getParticipantDefs() {
        if (participantDefs == null) {
            participantDefs = new ArrayList<NodeDef>();
        }
        return this.participantDefs;
    }

    public void setParticipantDefs(List<NodeDef> nodeDefs) {
        this.participantDefs = nodeDefs;
    }

    // convenience method....
    public String getName() {
        String name = null;
        if (eventNode != null) {
            name = eventNode.getName();
        }
        return name;
    }

    public void setName(String name) {
        if (eventNode != null) {
            eventNode.setName(name);
        }
    }

}
