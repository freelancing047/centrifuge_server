package csi.client.gwt.widget.display_list_widgets;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 3/21/2015.
 */
public interface ObjectBuilder<T extends Widget & HasClickHandlers & CanBeSelected, S, R> {

    R buildObject(DataViewDef metaDataIn, DisplayListItem<T, S> displayListItemIn);
}
