package csi.client.gwt.theme.editor;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;

import csi.server.common.model.themes.VisualItemStyle;

/**
 * Created by Ivan on 4/5/2017.
 */
public class FilterTextbox<T extends VisualItemStyle> extends TextBox {
    ListStore<T> attachedStore;

    @UiConstructor
    public FilterTextbox(){
        super();
        setupHandlers();
    }


    /**
     *
     * @param store
     */
    public FilterTextbox(ListStore<T> store){
        super();
        this.attachedStore = store;
        setupHandlers();
    }


    private void setupHandlers(){
        addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                clearFilter();
                addFilter(getText().trim());
            }
        });
    }


    public void clearFilter(){
        this.attachedStore.removeFilters();
    }


    public void setAttachedStore(ListStore<T> store){
        this.attachedStore = store;
    }

    private void addFilter(String filterText){
        final String filter = filterText;
        attachedStore.addFilter(new Store.StoreFilter<T>() {
            @Override
            public boolean select(Store<T> store, T parent, T item) {
                return containsIgnoreCase(item.getName(), filter);
            }
        });
        attachedStore.setEnableFilters(true);
    }


    //http://stackoverflow.com/a/25379180
    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

}
