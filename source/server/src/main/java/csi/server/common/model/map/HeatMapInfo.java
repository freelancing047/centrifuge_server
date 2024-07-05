package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class HeatMapInfo implements IsSerializable {
    private Double blurValue;
    private Double maxValue;
    private Double minValue;
	public Double getBlurValue() {
		return blurValue;
	}
	public void setBlurValue(Double blurValue) {
		this.blurValue = blurValue;
	}
	public Double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	public Double getMinValue() {
		return minValue;
	}
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
}
