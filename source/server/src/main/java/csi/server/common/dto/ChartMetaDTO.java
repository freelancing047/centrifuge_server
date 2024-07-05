package csi.server.common.dto;



import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ChartMetaDTO implements IsSerializable {

    public int rowCount;
    public ArrayList<ChartDimensionDTO> dimension;

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public ArrayList<ChartDimensionDTO> getDimension() {
        return dimension;
    }

    public void setDimension(ArrayList<ChartDimensionDTO> dimension) {
        this.dimension = dimension;
    }

}
