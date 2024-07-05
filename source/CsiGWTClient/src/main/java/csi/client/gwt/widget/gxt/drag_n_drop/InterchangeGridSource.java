package csi.client.gwt.widget.gxt.drag_n_drop;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.GridDragSource;

/**
 * Created by centrifuge on 8/2/2016.
 */
public class InterchangeGridSource<T> extends GridDragSource<T> {

    String _id;
    String _default = null;
    DragLabelProvider _labelProvider = null;

    public InterchangeGridSource(IntegratedGrid<T> gridIn, String idIn) {

        super(gridIn);
        _id = idIn;
    }

    public InterchangeGridSource(IntegratedGrid<T> gridIn, String idIn, DragLabelProvider labelProviderIn) {

        super(gridIn);
        _id = idIn;
        _labelProvider = labelProviderIn;
    }

    public InterchangeGridSource(IntegratedGrid<T> gridIn, String idIn, String defaultIn) {

        super(gridIn);
        _id = idIn;
        _default = defaultIn;
    }

    public InterchangeGridSource(IntegratedGrid<T> gridIn, String idIn, SafeHtml defaultIn) {

        super(gridIn);
        _id = idIn;
        _default = defaultIn.asString();
    }

    public void setId(String idIn) {

        _id = idIn;
    }

    public void setDefault(String defaultIn) {

        _default = defaultIn;
    }

    public void setDefault(SafeHtml defaultIn) {

        _default = defaultIn.asString();
    }

    public void setLabelProvider(DragLabelProvider labelProviderIn) {

        _labelProvider = labelProviderIn;
    }

    public String getId() {

        return _id;
    }

    @Override
    protected void onDragDrop(DndDropEvent event) {

    }

    @Override
    protected void onDragStart(DndDragStartEvent eventIn) {

        String myLabel = (null != _labelProvider) ? _labelProvider.getLabel() : _default;

        super.onDragStart(eventIn);

        if (null != myLabel) {

            eventIn.getStatusProxy().update(SafeHtmlUtils.fromString(myLabel));
        }
    }
}

