package csi.shared.gwt.viz.chart;

import java.util.List;

public interface StatisticsHolder {
	void clearStatistics();
	void setCategoryNames(List<String> categoryNames);
	void setData(List<List<Number>> seriesDataList);
	boolean isTop(int columnIndex, int threshold, String key);
	boolean isTopPercent(int columnIndex, int threshold, String key);
	boolean isBottom(int columnIndex, int threshold, String key);
	boolean isBottomPercent(int columnIndex, int threshold, String key);
}
