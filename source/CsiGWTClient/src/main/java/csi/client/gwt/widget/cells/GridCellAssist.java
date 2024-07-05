package csi.client.gwt.widget.cells;


public interface GridCellAssist<T> {

    public String getStyle(Integer rowIn, Integer columnIn);
    
    public void forceRedraw(Integer rowIn);
    
    public void forceRedraw(Integer rowIn, Integer columnIn);
    
    public void forceUpdate(T valueIn, Integer rowIn, Integer columnIn);
    
    public void reportTextChange(String valueIn, Integer rowIn, Integer columnIn);
}
