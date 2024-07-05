package csi.client.gwt.viz.graph.settings;

import java.util.ArrayList;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.link.settings.LinkColor;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.widget.drawing.Edge;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.gwt.viz.graph.LinkDirection;

public class DrawLink extends Edge {

    private LinkProxy linkProxy;
    private final String themeOverride;

    public DrawLink(NodeProxy node1, NodeProxy node2, LinkProxy linkProxy, String themeOverride) {
        super(node1, node2);
        this.linkProxy = linkProxy;
        this.themeOverride = themeOverride;
    }

    public LinkProxy getLinkProxy() {
        return linkProxy;
    }

    @Override
    public String getColor() {
        if (themeOverride != null) {
            try {
                return ClientColorHelper.get().make(Integer.parseInt(themeOverride)).toString();
            }catch (NumberFormatException e){
                //ignore
            }
        }

        return (new LinkColor(linkProxy)).getColor().toString();
    }

    @Override
    public void render(Context2d context2d) {
        context2d.save();
            context2d.beginPath();
             context2d.setStrokeStyle("#EEE");
            context2d.setLineWidth(7);

             context2d.beginPath();
             moveToStartLineToEnd(context2d);
             context2d.stroke();
            context2d.setStrokeStyle(getColor());
             context2d.setLineWidth(4);
            moveToStartLineToEnd(context2d);
            context2d.stroke();
        context2d.restore();
        renderLabel(context2d);
        renderArrow(context2d);
    }

    private void renderArrow(Context2d context2d) {
        DirectionDef directionDef = linkProxy.getDirectionDef();
        if (directionDef == null) {
            return;
        }
        FieldDef fieldDef = directionDef.getFieldDef();
        if (fieldDef == null) {
            return;
        }
        context2d.save();
        double startNodeX = layer.getCanvas().getCoordinateSpaceWidth() * startNode.getX();
        double endNodeX = endNode.getX() * layer.getCanvas().getCoordinateSpaceWidth();
        double startNodeY = layer.getCanvas().getCoordinateSpaceHeight() * startNode.getY();
        double endNodeY = endNode.getY() * layer.getCanvas().getCoordinateSpaceHeight();
        double len = Math.sqrt(Math.pow(startNodeX - endNodeX, 2) + Math.pow(startNodeY - endNodeY, 2));
        // Rotate the context to point along the path
        context2d.translate(endNodeX, endNodeY);
        context2d.rotate(Math.atan2(startNodeY - endNodeY, startNodeX - endNodeX));
        context2d.setFillStyle("rgb(0,0,0)");//NON-NLS

        if (FieldType.STATIC.equals(fieldDef.getFieldType())) {
            if (LinkDirection.FORWARD.toString().equals(fieldDef.getStaticText())) {
                renderArrow1(context2d);
            } else if (LinkDirection.REVERSE.toString().equals(fieldDef.getStaticText())) {
                renderArrow2(context2d, len);
            }

        } else {
            ArrayList<String> forwardValues = directionDef.getForwardValues();
            if (forwardValues != null && !forwardValues.isEmpty()) {
                renderArrow1(context2d);
            }
            ArrayList<String> reverseValues = directionDef.getReverseValues();
            if (reverseValues != null && !reverseValues.isEmpty()) {
                renderArrow2(context2d, len);
            }
        }
        context2d.restore();
    }

    private void renderLabel(Context2d context2d) {
        String text = linkProxy.getName();
        if (text == null) {
            return;
        }
        if (text.isEmpty()) {
            return;
        }
        context2d.save();

        double textX = layer.getCanvas().getCoordinateSpaceWidth() * (startNode.getX() + endNode.getX()) / 2D;
        double textY = layer.getCanvas().getCoordinateSpaceHeight() * (startNode.getY() + endNode.getY()) / 2D + 5;
        context2d.setFont("normal 400 13px Arial");//NON-NLS
        TextMetrics textMetrics = context2d.measureText(text);

        //renderBox
        context2d.setFillStyle("rgba(255,255,255,.9)");//NON-NLS
        context2d.setLineCap(Context2d.LineCap.ROUND);
        context2d.fillRect(textX - (textMetrics.getWidth() + 10) / 2D, textY - 15, textMetrics.getWidth() + 10, 20);
        context2d.setStrokeStyle("rgb(100,100,100)");//NON-NLS
        context2d.strokeRect(textX - (textMetrics.getWidth() + 10) / 2D, textY - 15, textMetrics.getWidth() + 10, 20);

        //renderText
        context2d.setFillStyle("rgb(0,0,0)");//NON-NLS
        context2d.setTextAlign(Context2d.TextAlign.CENTER);
        context2d.fillText(text, textX, textY);

        context2d.restore();
    }

    private void renderArrow2(Context2d context2d, double len) {
        context2d.beginPath();
        context2d.moveTo(len - 25, 0);
        context2d.lineTo(len - 45, -10);
        context2d.lineTo(len - 45, 10);
        context2d.closePath();
        context2d.fill();
    }

    private void renderArrow1(Context2d context2d) {
        context2d.beginPath();
        context2d.moveTo(25, 0);
        context2d.lineTo(45, -10);
        context2d.lineTo(45, 10);
        context2d.closePath();
        context2d.fill();
    }
}
