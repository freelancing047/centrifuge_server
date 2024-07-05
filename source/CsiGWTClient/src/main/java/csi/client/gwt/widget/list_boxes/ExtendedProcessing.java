package csi.client.gwt.widget.list_boxes;

/**
 * Created by centrifuge on 1/20/2016.
 */
public interface ExtendedProcessing<T> {

    public T getPopUpSelection();
    public void forwardSelectionEvent(int indexIn);
    public void forwardSelectionEvent(T selectionIn);
    public boolean isSelectable(int indexIn);
}
