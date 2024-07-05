package csi.server.common.model.visualization.selection;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import csi.config.Configuration;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;

public class SummaryMapSelection extends AbstractMapSelection {
    private final MapSelectionGrid mapSelectionGrid = new MapSelectionGrid();
    private Set<LinkGeometry> links = new HashSet<LinkGeometry>();

    public SummaryMapSelection() {
        super();
    }

    public SummaryMapSelection(String mapViewDefUuid) {
        this();
        setMapViewDefUuid(mapViewDefUuid);
    }

    @Override
   public Set<Geometry> getNodes() {
        return new TreeSet<Geometry>(mapSelectionGrid.getNodes());
    }

    @Override
    public void setNodes(Set<Geometry> nodes) {
    }

    public void removeSmaller(Geometry geometry) {
        mapSelectionGrid.removeSmaller(geometry);
    }

    @Override
   public void addNode(Geometry geometry) {
        mapSelectionGrid.addGeometry(geometry);
    }

    @Override
   public void removeNode(Geometry geometry) {
        mapSelectionGrid.removeGeometry(geometry);
    }

    @Override
   public boolean containsGeometry(Geometry geometry) {
        if (geometry.getSummaryLevel() == Configuration.getInstance().getMapConfig().getDetailLevel()) {
         return mapSelectionGrid.hasEqual(geometry);
      }
        return mapSelectionGrid.hasSmaller(geometry);
    }

    public MapSelectionGrid getMapSelectionGrid() {
        return mapSelectionGrid;
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
   public void addLinks(Set<LinkGeometry> links) {
        this.links.addAll(links);
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
        return mapSelectionGrid.isEmpty() && getLinks().isEmpty();
    }

    @Override
    public void clearSelection() {
        mapSelectionGrid.clear();
        getLinks().clear();
    }

    @Override
    public void setFromSelection(Selection selection) {
        if (selection instanceof NullSelection) {
         clearSelection();
      } else {
            AbstractMapSelection mapSelection = (AbstractMapSelection) selection;
            mapSelectionGrid.addAll(mapSelection.getNodes());
            getLinks().addAll(mapSelection.getLinks());
        }
    }

    @Override
    public Selection copy() {
        AbstractMapSelection mapSelection = new SummaryMapSelection(getMapViewDefUuid());
        mapSelection.setFromSelection(this);
        return mapSelection;
    }
}
