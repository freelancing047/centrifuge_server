package csi.client.gwt.resources;

import com.github.gwtbootstrap.client.ui.config.Configurator;
import com.github.gwtbootstrap.client.ui.resources.Resources;
import com.google.gwt.core.client.GWT;

public class BootstrapConfigurator implements Configurator {

    public Resources getResources() {
        return GWT.create(ApplicationResources.class);
    }

    @Override
    public boolean hasResponsiveDesign() {
        return false;
    }
}
