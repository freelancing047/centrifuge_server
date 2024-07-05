package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


public abstract class EscapeKeyEventHandler extends BaseCsiEventHandler {

    public abstract void onEscapeKeyRecognized(EscapeKeyEvent event);
}
