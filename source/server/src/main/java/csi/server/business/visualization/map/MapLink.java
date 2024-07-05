package csi.server.business.visualization.map;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;

import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.TrackidTracknameDuple;

public class MapLink {
	private Long linkId;
	private Set<Integer> internalstateids = Sets.newTreeSet();
	private Integer typeId;
	private TrackidTracknameDuple tracktype;
	private Set<Integer> rowIds = new TreeSet<Integer>();
	private Boolean selected = false;
	private MapNode sourceNode;
	private MapNode destinationNode;
	private double hits = 0;
	private LinkGeometry linkGeometry = null;

	public MapLink(Long linkId) {
		this.linkId = linkId;
	}

	public Long getLinkId() {
		return linkId;
	}

	public void addInternalstateid(int internalstateid) {
		internalstateids.add(internalstateid);
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public TrackidTracknameDuple getTracktype() {
		return tracktype;
	}

	public void setTracktype(TrackidTracknameDuple tracktype) {
		this.tracktype = tracktype;
	}

	public Set<Integer> getRowIds() {
		return rowIds;
	}

	public Boolean isSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public MapNode getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(MapNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	public MapNode getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(MapNode destinationNode) {
		this.destinationNode = destinationNode;
	}

	public double getHits() {
		return hits;
	}

	public void setHits(double hits) {
		this.hits = hits;
	}

	public void incrementHits() {
		hits++;
	}

	public LinkGeometry getLinkGeometry() {
		if (linkGeometry == null) {
			if (tracktype != null)
				linkGeometry = new LinkGeometry(tracktype, sourceNode.getGeometry(), destinationNode.getGeometry());
			else
				linkGeometry = new LinkGeometry(typeId, sourceNode.getGeometry(), destinationNode.getGeometry());
		}
		return linkGeometry;
	}

	public void incorporate(MapLink other) {
		rowIds.addAll(other.rowIds);
		hits += other.hits;
	}
}
