package csi.shared.core.visualization.matrix;

import java.io.Serializable;

/**
 * Created by grinv on 3/21/2018.
 */
public class MatrixSearchRequest implements Serializable {
    private String dvUuid;
    private String vizUuid;
    // how many categories to return around the found values
    private int CONTEXT_WIDTH = 2;

    private String xQuery, yQuery;
    private int valueQuery;


    public MatrixSearchRequest(String dvUuid, String vizUuid) {
        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;
    }

    public MatrixSearchRequest() {
    }

    public int getCONTEXT_WIDTH() {
        return CONTEXT_WIDTH;
    }

    public void setCONTEXT_WIDTH(int CONTEXT_WIDTH) {
        this.CONTEXT_WIDTH = CONTEXT_WIDTH;
    }

    public String getxQuery() {
        return xQuery;
    }

    public void setxQuery(String xQuery) {
        this.xQuery = xQuery;
    }

    public String getyQuery() {
        return yQuery;
    }

    public void setyQuery(String yQuery) {
        this.yQuery = yQuery;
    }

    public int getValueQuery() {
        return valueQuery;
    }

    public void setValueQuery(int valueQuery) {
        this.valueQuery = valueQuery;
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
}
