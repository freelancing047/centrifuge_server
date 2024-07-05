package csi.client.gwt.viz.timeline.view.drawing;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;

import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class TrackBreadCrumb extends BaseRenderable {
    

    private static final String ARROW = "< ";

    protected static final CssColor DEFAULT_LABEL_COLOR = CssColor.make(120,120,120);

    protected static final String DEFAULT_FONT = "12pt serif";
    protected static final String HIGHLIGHT_FONT = "bolder 12pt serif";
    private static final double LABEL_START_X = 5;
    private String label;
    private double labelWidth;
    private Layer layer;
    private boolean highlight = false;

    private int labelStartY = 0;

    @Override
    public void render(Context2d context2d) {
        
        context2d.save();

        if(highlight){
            context2d.setFont(HIGHLIGHT_FONT);
        } else {
            context2d.setFont(DEFAULT_FONT);
        }
        String SEPARATOR = ARROW;
        TextMetrics textMetrics = context2d.measureText(SEPARATOR +label);
        setLabelWidth(textMetrics.getWidth());
        int y = layer.getHeight();
        labelStartY = y - TrackRenderable.FULL_SUMMARY_HEIGHT * 2 - 2;
        context2d.setFillStyle(DEFAULT_LABEL_COLOR);
        context2d.fillText(SEPARATOR + label, LABEL_START_X, labelStartY);
        
        context2d.restore();
    }



    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }



    @Override
    public boolean hitTest(double x, double y) {

        
        return x <= LABEL_START_X + getLabelWidth() && x >= LABEL_START_X &&
                y >= labelStartY - TrackRenderable.FULL_SUMMARY_HEIGHT/2 && y <= labelStartY;
    }



    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }



    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
    }



    public double getLabelWidth() {
        return labelWidth;
    }



    public void setLabelWidth(double labelWidth) {
        this.labelWidth = labelWidth;
    }



    public boolean isHighlight() {
        return highlight;
    }



    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

}
