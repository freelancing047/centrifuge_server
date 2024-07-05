package csi.server.common.model.visualization.selection;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import csi.config.Configuration;
import csi.server.business.visualization.map.RangeChecker;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;

public class TrackmapSelection extends AbstractMapSelection {
    private Set<LinkGeometry> links = new HashSet<LinkGeometry>();

    public TrackmapSelection() {
        super();
    }

    public TrackmapSelection(String mapViewDefUuid) {
        this();
        setMapViewDefUuid(mapViewDefUuid);
    }

    @Override
   public Set<Geometry> getNodes() {
        return null;
    }

    @Override
   public void setNodes(Set<Geometry> nodes) {
    }

    @Override
   public void addNode(Geometry geometry) {
    }

    @Override
   public void removeNode(Geometry geometry) {
    }

    @Override
   public boolean containsGeometry(Geometry geometry) {
        if (geometry.getSummaryLevel() == Configuration.getInstance().getMapConfig().getDetailLevel()) {
            return hasEqualGeometry(geometry);
        } else {
            RangeChecker rangeChecker = new RangeChecker(geometry);
            return containsGeometry(rangeChecker);
        }
    }

    private boolean hasEqualGeometry(Geometry geometry) {
        return links.stream().anyMatch(link -> link.getNode1Geometry().equals(geometry) || link.getNode2Geometry().equals(geometry));
    }

    public Set<LinkGeometry> getNumEqualGeometry(Geometry geometry) {
        return links.stream().filter(link -> link.getNode1Geometry().equals(geometry) || link.getNode2Geometry().equals(geometry)).collect(Collectors.toCollection(Sets::newTreeSet));
    }

    private boolean containsGeometry(RangeChecker rangeChecker) {
        return links.stream().anyMatch(link -> rangeChecker.isInRange(link.getNode1Geometry()) || rangeChecker.isInRange(link.getNode2Geometry()));
    }

    @Override
   public Set<LinkGeometry> getLinks() {
        return links;
    }

    @Override
   public void setLinks(Set<LinkGeometry> links) {
        this.links = links;
    }

    @Override
   public boolean containsLink(LinkGeometry link) {
        return links.contains(link);
    }

    @Override
   public void addLink(LinkGeometry link) {
        links.add(link);
    }

    @Override
   public void addLinks(Set<LinkGeometry> linksToAdd) {
        links.addAll(linksToAdd);
    }

    @Override
   public void removeLink(LinkGeometry link) {
        links.remove(link);
    }

    @Override
   public void removeLinks(Set<LinkGeometry> linksToRemove) {
        links.removeAll(linksToRemove);
    }

    @Override
   public boolean isCleared() {
        return links.isEmpty();
    }

    @Override
   public void clearSelection() {
        links.clear();
    }

    @Override
   public void setFromSelection(Selection selection) {
        if ((selection == null) || (selection instanceof NullSelection)) {
            clearSelection();
        } else {
            AbstractMapSelection mapSelection = (AbstractMapSelection) selection;
            getLinks().addAll(mapSelection.getLinks());
        }
    }

    @Override
   public Selection copy() {
        AbstractMapSelection mapSelection = new TrackmapSelection(getMapViewDefUuid());
        mapSelection.setFromSelection(this);
        return mapSelection;
    }
}
