package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.server.common.dto.SelectionListData.SelectorBasics;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ResourceSelectionEventHandler<T extends SelectorBasics> extends BaseCsiEventHandler {

    public abstract void onResourceSelection(ResourceSelectionEvent<T> event);
}
