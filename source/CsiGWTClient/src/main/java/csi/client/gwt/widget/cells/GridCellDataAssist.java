package csi.client.gwt.widget.cells;


public interface GridCellDataAssist {

    public boolean isComboBox(Integer rowIn, Integer columnIn);
    
    public String getLabel(Integer rowIn, Integer columnIn);
    
    public void forceUpdate(String valueIn, Integer rowIn, Integer columnIn);
}
