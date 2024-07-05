package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class UserInputEventHandler<T> extends BaseCsiEventHandler {

    public abstract void onUserInput(UserInputEvent<T> event);
}
