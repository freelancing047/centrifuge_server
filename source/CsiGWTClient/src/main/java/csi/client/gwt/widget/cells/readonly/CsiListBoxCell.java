package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;

import csi.client.gwt.widget.cells.ModifiableTriggerFieldDefaultAppearance;
import csi.client.gwt.widget.list_boxes.ExtendedProcessing;

/**
 * Created by centrifuge on 1/18/2016.
 */
public class CsiListBoxCell<T> extends ComboBoxCell<T> {

    protected ExtendedProcessing<T> _parent = null;
    protected boolean _inputEnabled = false;
    protected int _charCode = 0;
    protected int _keyCode = 0;

    /**
     * Creates a combo box cell that renders all items with the given label provider.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider) {

        this(store, labelProvider, GWT.<TriggerFieldAppearance> create(TriggerFieldAppearance.class));
    }

    /**
     * Creates a combo box cell that renders the input value with the label provider.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box
     * @param view the list view
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, ListView<T, ?> view) {

        this(store, labelProvider, view, GWT.<TriggerFieldAppearance> create(TriggerFieldAppearance.class));
    }

    /**
     * Creates a combo box cell that renders the input value with the label provider.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box
     * @param view the list view
     * @param appearance the appearance
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, ListView<T, ?> view,
                          TriggerFieldAppearance appearance) {

        super(store, labelProvider, view, appearance);
    }

    /**
     * Creates a combo box cell that renders the input value with the label provider and the drop down values with the
     * renderer.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box
     * @param renderer draws the current model as html in the drop down
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer) {
        this(store, labelProvider, renderer, GWT.<TriggerFieldAppearance> create(TriggerFieldAppearance.class));
    }

    /**
     * Creates a combo box cell that renders the input value with the label provider and the drop down values with the
     * renderer.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box
     * @param renderer draws the current model as html in the drop down
     * @param appearance the appearance
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer,
                          TriggerFieldAppearance appearance) {

        super(store, labelProvider, renderer, appearance);
    }

    /**
     * Creates a combo box cell that renders both the input value and drop down values with the given label provider.
     *
     * @param store the store containing the data that can be selected
     * @param labelProvider converts the current model type into a string value to display in the text box and the drop
     *          down values
     * @param appearance the appearance
     */
    public CsiListBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, TriggerFieldAppearance appearance) {

        super(store, labelProvider, appearance);

    }
    
    public void setParent(ExtendedProcessing parentIn) {

        _parent = parentIn;
    }

    public void collapseList() {

        collapse(lastContext, lastParent);
    }

    @Override
    protected void onViewClick(final XElement parentIn, NativeEvent eventIn, boolean focusIn, boolean takeSelectedIn) {

        if (null != _parent)
        {
            ListView<T, ?> myListView = getListView();
            Element myElement = myListView.findElement((Element) eventIn.getEventTarget().cast());
            int myIndex = myListView.indexOf(myElement);

            if (_parent.isSelectable(myIndex)) {

                super.onViewClick(parentIn, eventIn, focusIn, takeSelectedIn);

                if (0 <= myIndex) {

                    _parent.forwardSelectionEvent(myIndex);
                }
            }

        } else {

            super.onViewClick(parentIn, eventIn, focusIn, takeSelectedIn);
        }
    }

    public void processSelection(T selectionIn) {

        super.onSelect(selectionIn);
    }
}
