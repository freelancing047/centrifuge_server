package csi.client.gwt.widget.mapper;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;

import csi.client.gwt.widget.ui.FullSizeLayoutPanel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MapperWidget extends ResizeComposite{

    private Label label = new Label("This will be the mapper.."); //$NON-NLS-1$
    private FullSizeLayoutPanel fullSizeLayoutPanel = new FullSizeLayoutPanel();

    public MapperWidget(){
        fullSizeLayoutPanel.add(label);
        initWidget(fullSizeLayoutPanel);
    }

}
