package csi.client.gwt.widget.cells.context_menu;

/**
 * Created by centrifuge on 1/24/2018.
 */
public interface CellMenuCallback<T> {

    public void onSelection(T valueIn, int rowIn, int columnIn, int choiceIn);
}
