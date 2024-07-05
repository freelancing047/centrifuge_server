package csi.client.gwt.widget.ui.uploader.wizards.components;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.mapper.TypedDataMapper;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 2/19/2019.
 */
public class InstallerColumnMapWidget extends AbstractInputWidget {

    public InstallerColumnMapWidget() {

        add(new TypedDataMapper());
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void resetValue() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean atReset() {
        return false;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public int getRequiredHeight() {
        return 0;
    }

    @Override
    protected void layoutDisplay() {

    }
}
