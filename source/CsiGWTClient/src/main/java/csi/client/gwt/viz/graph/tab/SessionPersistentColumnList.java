package csi.client.gwt.viz.graph.tab;

import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import csi.server.common.dto.graph.gwt.NodeListDTO;

import java.util.List;

public class SessionPersistentColumnList {
    private ColumnModel<?> columnModel;
    List<? extends ColumnConfig<?, ?>> columnList;

    protected void saveColumnModel(ColumnModel<?> existing){
        this.columnModel = existing;
    }

    protected ColumnModel<?> getExistingColumnModel(){
        return this.columnModel;
    }

    protected ColumnConfig<?, ?> getTypeColumn(String columnHeader){
        ColumnConfig<?, ?> oldTypeColumn = null;
        //guard if we don't have a model
        if(getExistingColumnModel() == null){
            return  oldTypeColumn;
        }

        List<? extends ColumnConfig<?, ?>> columnList1 = getExistingColumnModel().getColumns();
        for (ColumnConfig<?, ?> columnConfig : columnList1) {
            if(columnConfig.getHeader().toString().contains(columnHeader)){
                oldTypeColumn = columnConfig;
            }
        }
        return oldTypeColumn;
    }
}
