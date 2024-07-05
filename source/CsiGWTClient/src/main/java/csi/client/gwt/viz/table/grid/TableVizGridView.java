package csi.client.gwt.viz.table.grid;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.GridView;

public class TableVizGridView<M> extends GridView<M> {
    
    private SortInfoBean lastSort = null;

    @Override
    protected void doSort(int colIndex, SortDir sortDir) {
        //super.doSort(colIndex, sortDir);
        ColumnConfig<M, ?> column = cm.getColumn(colIndex);

        ValueProvider<? super M, ?> vp = column.getValueProvider();

        SortInfoBean bean = new SortInfoBean(vp, sortDir);

        if(lastSort != null && lastSort.getSortField().equals(vp.getPath())){
           bean.setSortDir(SortDir.toggle(lastSort.getSortDir()));
        } else if (sortDir == null) {
            bean.setSortDir(SortDir.ASC);
        }  else {
            bean.setSortDir(sortDir);
        }

        ((TableVizGrid)grid).sort(bean);
        lastSort = bean;
    }

    protected void updateHeaderSortState() {        
        if(lastSort != null){

            ColumnConfig<M, ?> config = cm.findColumnConfig(lastSort.getSortField());
            if(config != null){
                int index = cm.indexOf(config);
                if (index != -1) {
                  updateSortIcon(index, lastSort.getSortDir());
                }
            }
        }
    }

}
