package csi.client.gwt.widget.drawing;

import com.google.gwt.canvas.dom.client.Context2d;

import csi.client.gwt.util.HasXY;

public class Edge extends BaseRenderable {

    protected int width = 6;
    protected HasXY startNode;
    protected HasXY endNode;
    protected Layer layer;
    private String color = "#EEE";

    public Edge(HasXY node1, HasXY node2) {
        startNode = node1;
        endNode = node2;
    }

    @Override
    public void render(Context2d context2d) {
        context2d.beginPath();
        context2d.setStrokeStyle(getColor());
        context2d.setLineWidth(width);

        // context2d.beginPath();
        // moveToStartLineToEnd(context2d);
        // context2d.stroke();
        // context2d.setStrokeStyle("#444");
        // context2d.setLineWidth(4);
        moveToStartLineToEnd(context2d);
        context2d.stroke();
    }

    protected void moveToStartLineToEnd(Context2d context2d) {
        int coordinateSpaceWidth = layer.getCanvas().getCoordinateSpaceWidth();
        int coordinateSpaceHeight = layer.getCanvas().getCoordinateSpaceHeight();
        context2d.moveTo((startNode.getX() * coordinateSpaceWidth), (startNode.getY() * coordinateSpaceHeight));
        context2d.lineTo((endNode.getX() * coordinateSpaceWidth), (endNode.getY() * coordinateSpaceHeight));
    }

    @Override
    public boolean hitTest(double x, double y) {
        Context2d context2d = layer.getDrawingPanel().getCanvas().getContext2d();
        if (context2d == null) {
            return false;
        }

        int coordinateSpaceWidth = layer.getCanvas().getCoordinateSpaceWidth();
        int coordinateSpaceHeight = layer.getCanvas().getCoordinateSpaceHeight();
        double x1 = startNode.getX() * coordinateSpaceWidth;
        double x2 = endNode.getX() * coordinateSpaceWidth;
        double y1 = startNode.getY() * coordinateSpaceHeight;
        double y2 = endNode.getY() * coordinateSpaceHeight;

        if(!isWithinBound(x, x1, x2) || !isWithinBound(y, y1, y2))
        	return false;

        double epsilon = (width / 2) + 1;
        double m = 0;
        if ((x1 - x2) != 0) {
            m = (y1 - y2) / (x1 - x2);
        }
        double b = y1 - m * x1;
        // y= mx + b;
        double yhat = m * x + b;
        if (Math.abs(y - yhat) < epsilon) {
            return true;
        }
        double xhat = x1;
        if (m != 0) {
            xhat = (y - b) / m;
        }
        if (Math.abs(x - xhat) < epsilon) {
            return true;
        }
        return false;
    }

    private boolean isWithinBound(double testValue, double value1, double value2) {
        if (Math.abs(value1 - value2) < width) {
        	double centerValue = (value1 + value2) / 2;
        	double minValue = centerValue - width / 2;
        	double maxValue = centerValue + width / 2;
	        if (!(minValue < testValue && maxValue > testValue)) {
	            return false;
	        }
        } else {
	        if (!(Math.min(value1, value2) < testValue && Math.max(value1, value2) > testValue)) {
	            return false;
	        }
        }
        return true;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
