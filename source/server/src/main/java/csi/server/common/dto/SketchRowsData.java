package csi.server.common.dto;


import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;


public class SketchRowsData implements IsSerializable {

    private ArrayList<ArrayList<String[]>> row;

    public ArrayList<ArrayList<String[]>> getRow() {
        if (row == null) {
            row = new ArrayList<ArrayList<String[]>>();
        }
        return row;
    }
}
