package csi.client.gwt.csi_resource;

import csi.client.gwt.widget.IsSelectable;

/**
 * Created by centrifuge on 4/22/2019.
 */
public interface OptionControl<T> extends IsSelectable {

    public void setConflicts(Boolean conflictsIn);
    public Boolean getConflicts();
    public void setSelected(Boolean selectedIn);
    public T getOption();
    public String getType();
    public String getOwner();
    public String getName();
    public void setName(String nameIn);
    public String getRemarks();
    public void setRemarks(String remarksIn);
}
