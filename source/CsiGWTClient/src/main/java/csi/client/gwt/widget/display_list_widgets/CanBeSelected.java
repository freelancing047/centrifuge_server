package csi.client.gwt.widget.display_list_widgets;

/**
 * Created by centrifuge on 3/2/2015.
 */
public interface CanBeSelected {

    public boolean isEnabled();
    public boolean isSelected();
    public void setEnabled(boolean enabledIn);
    public void setSelected(boolean selectedIn);
    public boolean isValid();
}
