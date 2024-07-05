package csi.shared.core.visualization.matrix;

import java.io.Serializable;
public class MatrixSelectionRequest implements Serializable{

    public enum Axis implements Serializable{
        X("X-axis"),
        Y("Y-axis");

        private String label;
        private Axis(String label) {
            this.label = label;
        }


    }

    private String dvUuid;
    private String vizUuid;

    private Axis axis;

    private int index;
    private String categoryText;

    private int bucketSize;


    public MatrixSelectionRequest() {}

    public MatrixSelectionRequest(String vizUuid, String dvUuid, Axis axis, int catStart, int bucketSize) {
        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;
        this.axis = axis;
    }


    public String getCategoryText() {
        return categoryText;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

    public String getDvUuid() {
        return dvUuid;
    }

    public void setDvUuid(String dvUuid) {
        this.dvUuid = dvUuid;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

    public Axis getAxis() {
        return axis;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
