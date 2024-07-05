package csi.client.gwt.widget.ui.form;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;

/**
 * A combo box that creates and manages a {@link ListStore} of {@code <T>}
 * instances. Values are added to the list store using {@link #add} and removed
 * from the list store using {@link #remove(Object)}.
 * <p/>
 * If the selection list is already in a list store for some other purpose, you
 * may find it easier to use {@link ComboBox} directly.
 * 
 * @param <T> the combo box type
 */
public class CsiSimpleComboBox<T> extends SimpleComboBox<T> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">{name}</span>")
        SafeHtml display(String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);
    
  /**
   * Creates an empty combo box in preparation for values to be added to the
   * selection list using {@link #add}.
   * 
   * @param labelProvider the label provider that implements the interface to
   *          the data model associated with this combo box and is responsible
   *          for returning the value displayed to the user
   */
  @UiConstructor
  public CsiSimpleComboBox(final LabelProvider<? super T> labelProviderIn) {
    super(new ComboBoxCell<T>(new ListStore<T>(new ModelKeyProvider<T>() {
      @Override
      public String getKey(T item) {
        return item.toString();
      }
    }),
    
    labelProviderIn,
    
    new AbstractSafeHtmlRenderer<T>() {
        @Override
        public SafeHtml render(T itemIn) {
            return comboBoxTemplates.display(labelProviderIn.getLabel(itemIn));
        }
    }));
  }
}
