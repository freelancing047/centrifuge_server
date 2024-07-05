package csi.client.gwt.viz.shared.chrome.portlet;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;

import csi.client.gwt.viz.VizMessage;
import csi.client.gwt.viz.VizMessageArea;

class MessageArea implements VizMessageArea {

    /**
     * 
     */
    private Optional<VizPortlet> vizPortlet;

    /**
     * @param vizPortletImpl
     */
    MessageArea(VizPortlet vizPortlet) {
        checkNotNull(vizPortlet);
        this.vizPortlet = Optional.of(vizPortlet);
    }

    @Override
    public void show(VizMessage message) {

    }
}