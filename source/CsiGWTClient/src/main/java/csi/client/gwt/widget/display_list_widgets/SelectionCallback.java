package csi.client.gwt.widget.display_list_widgets;

/**
 * Created by centrifuge on 3/9/2015.
 */
public interface SelectionCallback<T> {

    public void segmentSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn);
    public void valueSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn);
    public void emptyValueSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn);
}
