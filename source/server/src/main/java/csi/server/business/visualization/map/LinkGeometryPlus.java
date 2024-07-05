package csi.server.business.visualization.map;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;

public class LinkGeometryPlus extends LinkGeometry implements Comparable<LinkGeometry>, IsSerializable, Serializable {
	private IntegerTreeNode rowIdTree;

	public LinkGeometryPlus() {
		super();
	}

	public LinkGeometryPlus(int linkType, Geometry node1Geometry, Geometry node2Geometry) {
		super(linkType, node1Geometry, node2Geometry);
	}

	public Set<Integer> getRowIds() {
		if (rowIdTree == null)
			return new HashSet<Integer>();
		return rowIdTree.getIntegerSet();
	}

	public void setRowIds(Set<Integer> rowIds) {
		for (Integer rowId : rowIds)
			addRowId(rowId);
	}

	public void addRowId(Integer rowId) {
		if (rowIdTree == null) {
			rowIdTree = new IntegerTreeNode();
			rowIdTree.setValue(rowId);
		} else
			rowIdTree.push(rowId);
	}
}
