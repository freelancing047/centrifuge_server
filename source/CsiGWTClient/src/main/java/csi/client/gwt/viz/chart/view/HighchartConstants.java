package csi.client.gwt.viz.chart.view;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public enum HighchartConstants {
	NEGATIVE {
		public String toString() {
			return CentrifugeConstantsLocator.get().highchartConstants_negative();
		}
	},
	GRID_LINE_INTERPOLATION {
		public String toString() {
			return CentrifugeConstantsLocator.get().highchartConstants_gridLineInterpolation();
		}
	},
	SLICED_OFFSET {
		public String toString() {
			return CentrifugeConstantsLocator.get().highchartConstants_slicedOffset();
		}
	},
	POINT_RANGE {
		public String toString() {
			return CentrifugeConstantsLocator.get().highchartConstants_pointRange();
		}
	}
}
