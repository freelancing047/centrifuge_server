package csi.client.gwt.csiwizard.panels;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by centrifuge on 8/18/2017.
 */
public class WizardPanelInterfacePanel extends SimpleLayoutPanel {

    @Override
    public void onResize() {

        final Widget myWidget = getWidget();

        if (myWidget instanceof AbstractWizardPanel) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    myWidget.setPixelSize(getOffsetWidth(), getOffsetHeight());
                }
            });
        }
    }
}
