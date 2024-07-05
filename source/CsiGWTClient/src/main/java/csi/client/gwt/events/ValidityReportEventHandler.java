package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ValidityReportEventHandler extends BaseCsiEventHandler {

    public abstract void onValidityReport(ValidityReportEvent event);
}
