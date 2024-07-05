package csi.server.common.model.visualization.selection;

import java.util.HashSet;
import java.util.Set;

import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;

public class DetailMapSelection extends AbstractMapSelection {
    private Set<Geometry> nodes = new HashSet<Geometry>();
    private Set<LinkGeometry> links = new HashSet<LinkGeometry>();

    public DetailMapSelection() {
        super();
    }

    public DetailMapSelection(String mapViewDefUuid) {
        this();
        setMapViewDefUuid(mapViewDefUuid);
    }

    public Set<Geometry> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Geometry> nodes) {
        this.nodes = nodes;
    }

    public void addNode(Geometry geometry) {
        nodes.add(geometry);
    }

    public void removeNode(Geometry geometry) {
        nodes.remove(geometry);
    }

    public boolean containsGeometry(Geometry geometry) {
        return nodes.contains(geometry);
    }

    public Set<LinkGeometry> getLinks() {
        return links;
    }

    public void setLinks(Set<LinkGeometry> links) {
        this.links = links;
    }

    public boolean containsLink(LinkGeometry link) {
        return links.contains(link);
    }

    public void addLink(LinkGeometry link) {
        links.add(link);
    }

    public void addLinks(Set<LinkGeometry> links) {
        this.links.addAll(links);
    }

    public void removeLink(LinkGeometry link) {
        links.remove(link);
    }

    public void removeLinks(Set<LinkGeometry> linksToRemove) {
        links.removeAll(linksToRemove);
    }

    @Override
    public boolean isCleared() {
        return getNodes().isEmpty() && getLinks().isEmpty();
    }

    @Override
    public void clearSelection() {
        getNodes().clear();
        getLinks().clear();
    }

    @Override
    public void setFromSelection(Selection selection) {
        if ((selection == null) || (selection instanceof NullSelection)) {
            clearSelection();
        } else {
            AbstractMapSelection mapSelection = (AbstractMapSelection) selection;
            Set<Geometry> nodes = mapSelection.getNodes();
            if (nodes != null) {
               getNodes().addAll(nodes);
            }
            Set<LinkGeometry> links = mapSelection.getLinks();
            if (links != null) {
               getLinks().addAll(links);
            }
        }
    }

    @Override
    public Selection copy() {
        AbstractMapSelection mapSelection = new DetailMapSelection(getMapViewDefUuid());
        mapSelection.setFromSelection(this);
        return mapSelection;
    }
}
