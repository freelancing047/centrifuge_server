package csi.client.gwt.widget.gxt.grid;


public interface DataStoreColumnAccess {

    public void setColumnData(int columnIn, Object dataIn);

    public Object getColumnData(int columnIn);

    public boolean isSelected();
}
