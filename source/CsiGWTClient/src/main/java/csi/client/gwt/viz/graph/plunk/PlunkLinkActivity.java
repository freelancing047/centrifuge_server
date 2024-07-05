package csi.client.gwt.viz.graph.plunk;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.surface.AbstractGraphSurfaceActivity;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.MouseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.PlunkedLink;

/**
 * Handles state when drawing a plunked link on the graph surface.
 *
 * @author Centrifuge Systems, Inc.
 */
public class PlunkLinkActivity extends AbstractGraphSurfaceActivity {

    private double centerOfNodeX;
    private double centerOfNodeY;

    public PlunkLinkActivity(GraphSurface graphSurface, double centerOfNodeX, double centerOfNodeY) {
        super(graphSurface);
        this.centerOfNodeX = centerOfNodeX;
        this.centerOfNodeY = centerOfNodeY;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        graphSurface.getMouseHandler().setDragging(true);
    }

    @Override
    public void drag(int deltaX, int deltaY) {
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        graphSurface.getView().drawLineWithCircleIndicatingStart(centerOfNodeX, centerOfNodeY, mouseHandler.getMouseX(), mouseHandler.getMouseY());
    }

    @Override
    public void stopDrag(int endX, int endY) {
        LinkDef linkDef = findLinkDef(CentrifugeConstantsLocator.get().link());
        PlunkLinkDTO plunkLinkDTO = PlunkLinkDTO.create(graphSurface.getVizUuid(), centerOfNodeX, centerOfNodeY, endX, endY, linkDef);
        VortexFuture<PlunkedLink> addLinkFuture = graphSurface.getModel().plunkLink(plunkLinkDTO);
        addLinkFuture.addEventHandler(new AbstractVortexEventHandler<PlunkedLink>() {
            @Override
            public void onSuccess(PlunkedLink result) {
                if (result != null) {
                    graphSurface.getGraph().getModel().getRelGraphViewDef().getPlunkedLinks().add(result);
                    graphSurface.getGraph().getLegend().load();
                }

            }
        });
        graphSurface.refresh(addLinkFuture);
    }

    private LinkDef findLinkDef(String link) {
        for (LinkDef linkDef : graphSurface.getGraph().getModel().getRelGraphViewDef().getLinkDefs()) {
            String linkDefType = getTypeFromLink(linkDef);
            if((linkDefType != null) && linkDefType.equalsIgnoreCase(link)) {
                return linkDef;
            }
        }
        return new LinkDef();
    }

    private String getTypeFromLink(LinkDef linkDef) {
        AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if((attributeDef != null) && (attributeDef.getFieldDef() != null) && attributeDef.getFieldDef().isAnonymous()){
            return attributeDef.getFieldDef().getStaticText();
        }
        return "";
    }

    @Override
    public void updateCursor() {
        graphSurface.getView().setCursor(Style.Cursor.POINTER);
    }
}
