package csi.server.common.model.visualization.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import csi.config.Configuration;
import csi.server.common.model.map.Geometry;

public class MapSelectionGrid implements Serializable {
	private LatLongGrid latLongGrid;

	public MapSelectionGrid() {
		latLongGrid = new LatLongGrid();
	}

	public void addGeometry(Geometry geometry) {
		if (!hasEqual(geometry)) {
         latLongGrid.addGeometry(geometry);
      }
	}

	public void removeGeometry(Geometry geometry) {
		if (!latLongGrid.isEmpty() && hasEqual(geometry)) {
         latLongGrid.removeGeometry(geometry);
      }
	}

	public void clear() {
		latLongGrid.clear();
	}

	public void addAll(Collection<Geometry> nodes) {
		for (Geometry geometry : nodes) {
         latLongGrid.addGeometry(geometry);
      }
	}

	public void removeAll(Collection<Geometry> nodes) {
		for (Geometry geometry : nodes) {
         latLongGrid.removeGeometry(geometry);
      }
	}

	public void removeSmaller(Geometry geometry) {
		if (!latLongGrid.isEmpty()) {
			int summaryLevel = geometry.getSummaryLevel();
			double minx = geometry.getX();
			double maxx = minx + (1 * Math.pow(10, -summaryLevel));
			double miny = geometry.getY();
			double maxy = miny + (1 * Math.pow(10, -summaryLevel));

			latLongGrid.removeSmaller(minx, maxx, miny, maxy);
		}
	}

	public boolean containsKey(Geometry geometry) {
		return latLongGrid.containsKey(geometry);
	}

	public boolean hasEqual(Geometry geometry) {
		return latLongGrid.containsKey(geometry);
	}

	public boolean hasSmaller(Geometry geometry) {
		int summaryLevel = geometry.getSummaryLevel();
		double minx = geometry.getX();
		double maxx = minx + (1 * Math.pow(10, -summaryLevel));
		double miny = geometry.getY();
		double maxy = miny + (1 * Math.pow(10, -summaryLevel));

		return latLongGrid.hasSmaller(minx, maxx, miny, maxy);
	}

	public List<Geometry> getNodes() {
		return latLongGrid.getNodes();
	}

	public boolean isEmpty() {
		return latLongGrid.isEmpty();
	}

	class LatLongGrid implements Serializable {
		private TreeMap<Long, LongGrid> latToLongMap;

		LatLongGrid() {
			latToLongMap = new TreeMap<Long,LongGrid>();
		}

		public boolean containsKey(Geometry geometry) {
			long key = getKey(geometry);
			if (latToLongMap.containsKey(key)) {
				LongGrid longGrid = latToLongMap.get(key);
				return longGrid.containsKey(geometry);
			}
			return false;
		}

		private long getKey(Geometry geometry) {
			return getKey(geometry.getSummaryLevel(), geometry.getY());
		}

		private long getKey(int summaryLevel, double y) {
			return (long) (Math.floor(y * Math.pow(10, summaryLevel)));
		}

		private LongGrid getLongGrid(Geometry geometry) {
			long key = getKey(geometry);
			LongGrid longGrid;
			if (latToLongMap.containsKey(key)) {
				longGrid = latToLongMap.get(key);
			} else {
				longGrid = new LongGrid();
				latToLongMap.put(key, longGrid);
			}
			return longGrid;
		}

		public void addGeometry(Geometry geometry) {
			LongGrid longGrid = getLongGrid(geometry);
			longGrid.addGeometry(geometry);
		}

		public void removeGeometry(Geometry geometry) {
			long key = getKey(geometry);
			LongGrid longGrid = latToLongMap.get(key);
			if (longGrid != null) {
				longGrid.removeGeometry(geometry);
				if (longGrid.isEmpty()) {
               latToLongMap.remove(key);
            }
			}
		}

		public boolean isEmpty() {
			return latToLongMap.isEmpty();
		}

		public boolean hasSmaller(double minx, double maxx, double miny, double maxy) {
			int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
			Long fromKey = getKey(summaryLevel, miny);
			Long toKey = getKey(summaryLevel, maxy);
			SortedMap<Long, LongGrid> subMap = latToLongMap.subMap(fromKey, toKey);
			if (!subMap.isEmpty()) {
				for (LongGrid longGrid : subMap.values()) {
               if (longGrid.hasSmaller(minx, maxx)) {
                  return true;
               }
            }
			}
			return false;
		}

		public void removeSmaller(double minx, double maxx, double miny, double maxy) {
			int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
			Long fromKey = getKey(summaryLevel, miny);
			Long toKey = getKey(summaryLevel, maxy);
			SortedMap<Long, LongGrid> subMap = latToLongMap.subMap(fromKey, toKey);
			if (!subMap.isEmpty()) {
				List<Long> keySet = new ArrayList<Long>(subMap.keySet());
				for (Long key : keySet) {
					LongGrid longGrid = subMap.get(key);
					longGrid.removeSmaller(minx, maxx);
				}
				for (Long key : keySet)  {
					LongGrid longGrid = subMap.get(key);
					if (longGrid.isEmpty()) {
                  latToLongMap.remove(key);
               }
				}
			}
		}

		public List<Geometry> getNodes() {
			List<Geometry> nodes = new ArrayList<Geometry>();
			for (LongGrid longGrid : latToLongMap.values()) {
            nodes.addAll(longGrid.getNodes());
         }
			return nodes;
		}

		public void clear() {
			latToLongMap.clear();
		}
	}

	class LongGrid implements Serializable {
		private TreeMap<Long,Geometry> longToGeometryMap;

		LongGrid() {
			longToGeometryMap = new TreeMap<Long,Geometry>();
		}

		public boolean containsKey(Geometry geometry) {
		   return longToGeometryMap.containsKey(getKey(geometry));
		}

		private long getKey(Geometry geometry) {
			return getKey(geometry.getSummaryLevel(), geometry.getX());
		}

		private long getKey(int summaryLevel, double x) {
			return (long) (Math.floor(x * Math.pow(10, summaryLevel)));
		}

		public void addGeometry(Geometry geometry) {
			longToGeometryMap.put(getKey(geometry), geometry);
		}

		public void removeGeometry(Geometry geometry) {
			longToGeometryMap.remove(getKey(geometry));
		}

		public boolean isEmpty() {
			return longToGeometryMap.isEmpty();
		}

		public boolean hasSmaller(double minx, double maxx) {
			int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
			Long fromKey = getKey(summaryLevel, minx);
			Long toKey = getKey(summaryLevel, maxx);
			SortedMap<Long, Geometry> subMap = longToGeometryMap.subMap(fromKey, toKey);

			return !subMap.isEmpty();
		}

		public void removeSmaller(double minx, double maxx) {
			int summaryLevel = Configuration.getInstance().getMapConfig().getDetailLevel();
			Long fromKey = getKey(summaryLevel, minx);
			Long toKey = getKey(summaryLevel, maxx);
			SortedMap<Long, Geometry> subMap = longToGeometryMap.subMap(fromKey, toKey);

			if (!subMap.isEmpty()) {
				List<Long> keySet = new ArrayList<Long>(subMap.keySet());
				for (Long key : keySet) {
               longToGeometryMap.remove(key);
            }
			}
		}

		public List<Geometry> getNodes() {
			return new ArrayList<Geometry>(longToGeometryMap.values());
		}
	}
}
