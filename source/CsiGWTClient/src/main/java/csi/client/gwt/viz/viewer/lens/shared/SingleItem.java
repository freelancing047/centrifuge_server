package csi.client.gwt.viz.viewer.lens.shared;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.viewer.lens.shared.Pinnable;

public class SingleItem extends Composite implements Pinnable {

    private FluidContainer container;

    public SingleItem(String string){
        initWidget(build());
        container.add(new InlineLabel(string));
    }

    private Widget build() {
        container = new FluidContainer();
        return container;
    }
}
