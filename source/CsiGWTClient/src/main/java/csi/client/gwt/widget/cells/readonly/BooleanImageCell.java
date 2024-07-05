package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.resources.SortResource;
import csi.client.gwt.util.BooleanResponse;
import csi.server.common.model.SortOrder;

/**
 * Created by centrifuge on 11/8/2017.
 */
public class BooleanImageCell extends AbstractCell<Boolean> {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"horizontal-align:center;display:inline;\"><img src=\"{0}\"></img></div>")
        SafeHtml display(SafeUri uri);
    }
    private static final CellTemplate checkboxTemplate = GWT.create(CellTemplate.class);

    private ChoiceMadeEventHandler _handler = null;
    private SafeUri _imageTrue = SortResource.IMPL.checkedIcon().getSafeUri();
    private SafeUri _imageFalse = SortResource.IMPL.uncheckedIcon().getSafeUri();
    private Boolean _value = false;

    public BooleanImageCell(ChoiceMadeEventHandler handlerIn) {

        super(BrowserEvents.CLICK);
        _handler = handlerIn;
    }

    public BooleanImageCell(ChoiceMadeEventHandler handlerIn, SafeUri imageTrueIn, SafeUri imageFalseIn) {

        super(BrowserEvents.CLICK);
        _handler = handlerIn;
        _imageTrue = imageTrueIn;
        _imageFalse = imageFalseIn;
    }

    @Override
    public void onBrowserEvent(Context contextIn, Element parentIn, Boolean valueIn,
                               NativeEvent eventIn, ValueUpdater<Boolean> valueUpdaterIn) {

//        if (eventIn.equals(BrowserEvents.CLICK)) {

            int myRow = contextIn.getIndex();

            _value = !valueIn;
            if (null != _handler) {

                _handler.onChoiceMade(new ChoiceMadeEvent(myRow, _value));
            }
/*
        } else {

            super.onBrowserEvent(contextIn, parentIn, valueIn, eventIn, valueUpdaterIn);
        }
*/
    }

    @Override
    public void render(Context contextIn, Boolean valueIn, SafeHtmlBuilder htmlBuilderIn) {

        SafeUri mySafeUri = valueIn ? _imageTrue : _imageFalse;

        _value = valueIn;
        htmlBuilderIn.append(checkboxTemplate.display(mySafeUri));
    }
}
