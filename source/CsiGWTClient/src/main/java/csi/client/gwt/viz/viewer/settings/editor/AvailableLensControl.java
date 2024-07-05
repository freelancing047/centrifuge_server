package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

public class AvailableLensControl extends Composite {

    private final FluidRow fr;
    private ViewerSettingsEditor vse;
    private LensDefSettings lensDefSettings;

    public AvailableLensControl(LensDefSettings lensDefSettings) {
        this.lensDefSettings = lensDefSettings;
        fr = new FluidRow();
        fr.add(new InlineLabel(lensDefSettings.getName()));
        initWidget(fr);
        fr.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                vse.addLens(AvailableLensControl.this.lensDefSettings);
            }
        }, MouseUpEvent.getType());
    }


    public void setVse(ViewerSettingsEditor vse) {
        this.vse = vse;
    }
}
