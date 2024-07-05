package csi.shared.gwt.viz.table;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TableSearchRequest implements  IsSerializable, Serializable {

    public String searchText;
    public int offset;
    public String dataViewUuid;
    public String visualizationUuid;
    public int limit;
   
    
}
