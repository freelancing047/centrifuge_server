package csi.server.common.dto;


import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;


public class RowDataSet implements IsSerializable {

    private HashMap<String, String> row;

    public HashMap<String, String> getRow() {
        if (row == null) {
            row = new HashMap<String, String>();
        }
        return row;
    }
}
