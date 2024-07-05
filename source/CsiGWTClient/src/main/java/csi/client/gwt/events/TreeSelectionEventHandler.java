package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class TreeSelectionEventHandler<R, S, T> extends BaseCsiEventHandler {

    public abstract void onTreeSelection(TreeSelectionEvent<R, S, T> event);
}
