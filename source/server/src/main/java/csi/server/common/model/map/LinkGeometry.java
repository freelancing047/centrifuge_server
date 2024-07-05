package csi.server.common.model.map;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class LinkGeometry implements Comparable<LinkGeometry>, IsSerializable, Serializable {
	private int linkType;
	private TrackidTracknameDuple tracktype;
	private Geometry node1Geometry;
	private Geometry node2Geometry;

	public LinkGeometry() {
	}

	public LinkGeometry(int linkType, Geometry node1Geometry, Geometry node2Geometry) {
		this.linkType = linkType;
		this.node1Geometry = node1Geometry;
		this.node2Geometry = node2Geometry;
	}

	public LinkGeometry(TrackidTracknameDuple tracktype, Geometry node1Geometry, Geometry node2Geometry) {
		this.tracktype = tracktype;
		this.node1Geometry = node1Geometry;
		this.node2Geometry = node2Geometry;
	}

	public int getLinkType() {
		return linkType;
	}

	public void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	public TrackidTracknameDuple getTracktype() {
		return tracktype;
	}

	public void setTracktype(TrackidTracknameDuple tracktype) {
		this.tracktype = tracktype;
	}

	public Geometry getNode1Geometry() {
		return node1Geometry;
	}

	public void setNode1Geometry(Geometry node1Geometry) {
		this.node1Geometry = node1Geometry;
	}

	public Geometry getNode2Geometry() {
		return node2Geometry;
	}

	public void setNode2Geometry(Geometry node2Geometry) {
		this.node2Geometry = node2Geometry;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(node1Geometry.getX(), node1Geometry.getY(), node2Geometry.getX(), node2Geometry.getY());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinkGeometry other = (LinkGeometry) obj;
		if (linkType != other.linkType)
			return false;
		if (node1Geometry == null) {
			if (other.node1Geometry != null)
				return false;
		} else if (!node1Geometry.equals(other.node1Geometry))
			return false;
		if (node2Geometry == null) {
			if (other.node2Geometry != null)
				return false;
		} else if (!node2Geometry.equals(other.node2Geometry))
			return false;
		return true;
	}

	@Override
	public int compareTo(LinkGeometry other) {
		if (other == null)
			throw new NullPointerException();
		if (this.equals(other)) {
			return 0;
		} else {
			if (linkType > other.linkType) {
				return 1;
			} else if (linkType < other.linkType) {
				return -1;
			} else {
				int compareResult = node1Geometry.compareTo(other.node1Geometry);
				if (compareResult > 0) {
					return 1;
				} else if (compareResult < 0) {
					return -1;
				} else {
					return node2Geometry.compareTo(other.node2Geometry);
				}
			}
		}
	}
}
