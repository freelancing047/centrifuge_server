package csi.client.gwt.viz.shared;

import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.form.DualListField;

/**
 * Combines two list view fields and allows selections to be moved between
 * fields either using buttons or by dragging and dropping selections
 *
 * @param <M> the model type
 * @param <T> the type displayed in the list view
 */
public class CsiDualListField<M, T> extends DualListField<M, T> {

    private static final String SPACER_CSS = "csi-dual-list-spacer";

    /**
     * Creates a dual list field that allows selections to be moved between two
     * list views using buttons or by dragging and dropping selections
     *
     * @param keyProvider   the key provider to use to track items
     * @param valueProvider the interface to {@code <M>}
     * @param cell          displays the data in the list views (e.g. {@link TextCell})
     */
    public CsiDualListField(ModelKeyProvider<? super M> keyProvider,
                            ValueProvider<? super M, T> valueProvider, Cell<T> cell) {
        this(new ListStore<M>(keyProvider), new ListStore<M>(keyProvider),
                valueProvider, cell);
    }

    /**
     * Creates a dual list field that allows selections to be moved between two
     * list views using buttons or by dragging and dropping selections.
     *
     * @param fromStore     the store containing the base set of items
     * @param toStore       the store containing the items selected by the user
     * @param valueProvider the interface to {@code <M>}
     * @param cell          displays the data in the list view (e.g. {@link TextCell})
     */
    @UiConstructor
    public CsiDualListField(ListStore<M> fromStore, ListStore<M> toStore,
                            ValueProvider<? super M, T> valueProvider, Cell<T> cell) {
        this(
                fromStore,
                toStore,
                valueProvider,
                cell,
                GWT.<DualListFieldAppearance>create(DualListFieldAppearance.class));
    }

    /**
     * Creates a dual list field that allows selections to be moved between two
     * list views using buttons or by dragging and dropping selections.
     *
     * @param fromStore     the store containing the base set of items
     * @param toStore       the store containing the items selected by the user
     * @param valueProvider the interface to {@code <M>}
     * @param cell          displays the data in the list view (e.g. {@link TextCell})
     * @param appearance    the appearance instance to use when rendering this widget
     */
    public CsiDualListField(ListStore<M> fromStore, ListStore<M> toStore,
                            ValueProvider<? super M, T> valueProvider, Cell<T> cell,
                            DualListFieldAppearance appearance) {

        super(fromStore, toStore, valueProvider, cell, appearance);

        HorizontalPanel panel = (HorizontalPanel) (super.getWidget());

        VerticalPanel buttonPanel = (VerticalPanel) panel.getWidget(1);

        IconButton up = (IconButton) buttonPanel.getWidget(0);
        IconButton allRight = (IconButton) buttonPanel.getWidget(1);
        IconButton right = (IconButton) buttonPanel.getWidget(2);
        IconButton left = (IconButton) buttonPanel.getWidget(3);
        IconButton allLeft = (IconButton) buttonPanel.getWidget(4);
        IconButton down = (IconButton) buttonPanel.getWidget(5);

        up.removeFromParent();
        allRight.removeFromParent();
        right.removeFromParent();
        left.removeFromParent();
        allLeft.removeFromParent();
        down.removeFromParent();

        buttonPanel.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        HTMLPanel spacerPanel;
        buttonPanel.add(right);
        buttonPanel.add(left);

        spacerPanel = new HTMLPanel("");
        spacerPanel.addStyleName(SPACER_CSS);
        buttonPanel.add(spacerPanel);
        buttonPanel.add(allRight);
        buttonPanel.add(allLeft);

        spacerPanel = new HTMLPanel("");
        spacerPanel.addStyleName(SPACER_CSS);
        buttonPanel.add(spacerPanel);

        /// this adds the space between buttons.. and now i just gotta find the listeners for this.

        buttonPanel.add(up);
        buttonPanel.add(down);
        buttonPanel.addStyleName("csi-dual-list-buttons");

        down.getElement().getStyle().setZIndex(XDOM.getTopZIndex());


    }


    @Override
    protected void onUp() {
        super.onUp();
        scrollAfterMove();
    }

    @Override
    protected void onDown(){
        super.onDown();
        scrollAfterMove();
    }


    /**
     *  Gets the selected element id, and scrolls the view to that index.
     */
    protected void scrollAfterMove(){
        ListView listView = super.getToView();
        ListStore<M> store = super.getToStore();
        List selectedItems = listView.getSelectionModel().getSelectedItems();

        if(selectedItems.size() > 0){
            int index = store.indexOf((M) selectedItems.get(0));
            listView.getElement(index).scrollIntoView();
        }
    }



}
