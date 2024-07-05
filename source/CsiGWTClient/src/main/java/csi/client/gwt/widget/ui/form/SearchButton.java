package csi.client.gwt.widget.ui.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.SortResource;
import csi.client.gwt.util.BooleanResponse;
import csi.server.common.model.SortOrder;

/**
 * Created by centrifuge on 11/8/2017.
 */
public class SearchButton extends FlowPanel {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"text-align: left;display:inline;\" title=\"{1}\"><img src=\"{0}\"></img></div>")
        SafeHtml templateOrder(SafeUri uri, String flyOverIn);
    }

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);
    private static final String _txtFlyOver = "Click for advanced search";

    private boolean _filtered = false;
    private BooleanResponse _handler = null;

    public SearchButton(BooleanResponse handlerIn) {
        super();
        _handler = handlerIn;
        this.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setFiltered(!isFiltered());
                _handler.onClick(isFiltered());
            }
        }, ClickEvent.getType());
        setFiltered(false);
    }

    public void setFiltered(boolean filteredIn) {

        _filtered = filteredIn;
        SafeUri uri = _filtered ? SortResource.IMPL.cancelSearchIcon().getSafeUri() : SortResource.IMPL.searchIcon().getSafeUri();
        getElement().setInnerSafeHtml(cellTemplate.templateOrder(uri, _txtFlyOver));
    }

    public boolean isFiltered() {

        return _filtered;
    }
}
