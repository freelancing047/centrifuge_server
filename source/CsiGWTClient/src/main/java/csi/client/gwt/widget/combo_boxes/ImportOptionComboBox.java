package csi.client.gwt.widget.combo_boxes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.interfaces.TrippleDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Created by centrifuge on 4/23/2019.
 */
public class ImportOptionComboBox<S extends TrippleDisplay> extends ComboBox<S> {

    private HandlerRegistration handlerRegistration;

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span qtip=\"{description}\" qtitle=\"{title}\">{name}</span></span>")
        SafeHtml display(String name, String title, String description);
    }

    static interface OptionProperty extends PropertyAccess<ConflictResolution> {

        ModelKeyProvider<TrippleDisplay> ordinal();
    }

    private static OptionProperty optionProperty = GWT.create(OptionProperty.class);
    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    private ClickEvent _clickEvent = null;
    private ClickHandler _clickHandler = null;

    public ImportOptionComboBox() {
        super(
                new ListStore<>(optionProperty.ordinal()),
                item -> item.getLabel(),
                new AbstractSafeHtmlRenderer<S>() {

                    public SafeHtml render(S item) {
                        return comboBoxTemplates.display(item.getLabel(), item.getTitle(), item.getDescription());
                    }
                }
        );

        addStyleName("string-combo-style");
        initialize();
    }
/*
    public void addClickHandler(ClickHandler handlerIn) {

        _clickHandler = handlerIn;
        addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent eventIn) {

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _clickEvent = eventIn;
                        getListView().addHideHandler(new HideEvent.HideHandler() {
                            @Override
                            public void onHide(HideEvent hideEvent) {

                                if ((null != _clickEvent) && (null != _clickHandler)) {

                                    _clickHandler.onClick(_clickEvent);
                                    _clickEvent = null;
                                }
                            }
                        });
                    }
                });
            }
        }, ClickEvent.getType());
    }
*/
    private void initialize() {
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }
}
