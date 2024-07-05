package csi.server.business.visualization.graph.util;

public class BundleMetrics {
	public double numNodes;
	public double size;
	public boolean bySize;
	public boolean byTransparency;
	public BundleMetrics() {
		numNodes = 0;
		size = 0;
		bySize = false;
		byTransparency = false;
	}
	public int computeSize() {
		if (numNodes > 0) {
			return (int)((size/numNodes)+.5);
		}
		else {
			return 1;
		}
	}
}
