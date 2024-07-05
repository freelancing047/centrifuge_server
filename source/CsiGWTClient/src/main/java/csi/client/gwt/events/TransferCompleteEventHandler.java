package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


public abstract class TransferCompleteEventHandler extends BaseCsiEventHandler {

    public abstract void onTransferComplete(TransferCompleteEvent event);
}
