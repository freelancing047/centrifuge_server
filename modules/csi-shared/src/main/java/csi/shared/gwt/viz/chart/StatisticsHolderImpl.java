package csi.shared.gwt.viz.chart;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class StatisticsHolderImpl implements StatisticsHolder {
	private List<String> categoryNames;
	private List<List<Number>> seriesDataList;
	private Map<Integer, Map<String, Integer>> decendingIndeces;
	private Map<Integer, Map<String, Integer>> ascendingIndeces;

	private class CategoryMeasureDuple {
		private String key;
		private double value;
		public CategoryMeasureDuple(String key, double value) {
			super();
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public double getValue() {
			return value;
		}
	}
	
	private class DecendingComparator implements Comparator<CategoryMeasureDuple> {
		@Override
		public int compare(CategoryMeasureDuple o1, CategoryMeasureDuple o2) {
			int retVal = 1;

			if (o1.getValue() > o2.getValue()) {
				retVal = -1;
			} else if (o1.getValue() < o2.getValue()) {
				retVal = 1;
			} else if (o1.getKey().compareTo(o2.getKey()) > 0) {
				retVal = -1;
			} else if (o1.getKey().compareTo(o2.getKey()) < 0) {
				retVal = 1;
			} else {
				retVal = 0;
			}

			return retVal;
		}
	}

	private class AscendingComparator implements Comparator<CategoryMeasureDuple> {
		@Override
		public int compare(CategoryMeasureDuple o1, CategoryMeasureDuple o2) {
			int retVal = 1;

			if (o1.getValue() > o2.getValue()) {
				retVal = 1;
			} else if (o1.getValue() < o2.getValue()) {
				retVal = -1;
			} else if (o1.getKey().compareTo(o2.getKey()) > 0) {
				retVal = 1;
			} else if (o1.getKey().compareTo(o2.getKey()) < 0) {
				retVal = -1;
			} else {
				retVal = 0;
			}

			return retVal;
		}
	}

	@Override
	public void clearStatistics() {
		decendingIndeces = null;
		ascendingIndeces = null;
	}

	@Override
	public void setCategoryNames(List<String> categoryNames) {
		this.categoryNames = categoryNames;
	}
	
	@Override
	public void setData(List<List<Number>> seriesDataList) {
		this.seriesDataList = seriesDataList;
	}
	
	@Override
	public boolean isTop(int columnIndex, int threshold, String key) {
		if (decendingIndeces == null) {
			decendingIndeces = new HashMap<Integer, Map<String, Integer>>();
		}

		int i = columnIndex;
		if (decendingIndeces.get(i) == null) {
			//this should prevent the IndexOutOfBounds, but i'm not sure if its a good fix.
			if(i >= 0 && i < seriesDataList.size() ) {
				List<Number> seriesData = seriesDataList.get(i);
				TreeSet<CategoryMeasureDuple> tree = new TreeSet<CategoryMeasureDuple>(new DecendingComparator());
				int j = 0;
				while (j < categoryNames.size()) {
					String category = categoryNames.get(j);
					double doubleValue = seriesData.get(j).doubleValue();
					CategoryMeasureDuple catetoryMeasureDuple = new CategoryMeasureDuple(category, doubleValue);
					tree.add(catetoryMeasureDuple);
					j++;
				}

				Map<String, Integer> newIndex = new HashMap<String, Integer>();
				Iterator<CategoryMeasureDuple> iterator = tree.iterator();
				int k = 0;
				while (iterator.hasNext()) {
					CategoryMeasureDuple categoryMeasureDuple = iterator.next();
					newIndex.put(categoryMeasureDuple.getKey(), k);
					k++;
				}
				decendingIndeces.put(i, newIndex);
			}
		}

		Map<String, Integer> index = decendingIndeces.get(i);
		int ranking = index.get(key);

		return ranking < threshold;
	}


	@Override
	public boolean isTopPercent(int columnIndex, int threshold, String key) {
		int numCategories = categoryNames.size();
		int newThreshold = (int)Math.ceil(numCategories*threshold/(double)100);
		return isTop(columnIndex, newThreshold, key);
	}

	@Override
	public boolean isBottom(int columnIndex, int threshold, String key) {
		if (ascendingIndeces == null) {
			ascendingIndeces = new HashMap<Integer, Map<String, Integer>>();
		}

		int i = columnIndex;
		if (ascendingIndeces.get(i) == null) {
			List<Number> seriesData = seriesDataList.get(i);
			TreeSet<CategoryMeasureDuple> tree = new TreeSet<CategoryMeasureDuple>(new AscendingComparator());
			int j = 0;
			while (j < categoryNames.size()) {
				String category = categoryNames.get(j);
				double doubleValue = seriesData.get(j).doubleValue();
				CategoryMeasureDuple catetoryMeasureDuple = new CategoryMeasureDuple(category, doubleValue);
				tree.add(catetoryMeasureDuple);
				j++;
			}

			Map<String, Integer> newIndex = new HashMap<String, Integer>();
			Iterator<CategoryMeasureDuple> iterator = tree.iterator();
			int k = 0;
			while (iterator.hasNext()) {
				CategoryMeasureDuple categoryMeasureDuple = iterator.next();
				newIndex.put(categoryMeasureDuple.getKey(), k);
				k++;
			}
			ascendingIndeces.put(i, newIndex);
		}

		Map<String, Integer> index = ascendingIndeces.get(i);
		int ranking = index.get(key);

		return ranking < threshold;
	}

	@Override
	public boolean isBottomPercent(int columnIndex, int threshold, String key) {
		int numCategories = categoryNames.size();
		int newThreshold = (int)Math.ceil(numCategories*threshold/(double)100);
		return isBottom(columnIndex, newThreshold, key);
	}
}
