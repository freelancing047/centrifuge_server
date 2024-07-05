package csi.client.gwt.viz.chart.overview.view.content;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.CssColor;

import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.overview.range.RangeCalculator;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.shared.gwt.viz.chart.ChartOverviewColorMapper;

/**
 * @author Centrifuge Systems, Inc.
 */
public class CanvasOverview extends DrawingPanel implements OverviewContent  {

    private int width = OverviewPresenter.DEFAULT_OVERVIEW_WIDTH;
    private List<Integer> buckets = new ArrayList<Integer>();
    
    private ComplexLayer backgroundLayer;

    private ComplexLayer colorLayer;

    private Rectangle backgroundRectangle;

    private CssColor backgroundColor = CssColor.make(75, 75, 75);
    
    public CanvasOverview(){
        backgroundLayer = new ComplexLayer();
        colorLayer = new ComplexLayer();
        addLayer(backgroundLayer);
        addLayer(colorLayer);
        buildUI();
    }

    @Override
    public void setCategoryData(List<Integer> colors) {
        
        this.buckets = colors;
        redraw();
    }

    public void redraw() {
        reset();
        resetBackground();
        
        double widgetWidth = RangeCalculator.createBinSize(width, buckets.size());
        setColors(buckets, widgetWidth);
        this.render();
        
        
    }

    @Override
    public void     resize(int width) {
        if(this.width == width){
            return;
        }
        this.width = width;
        //buildUI();
        this.setWidth(this.width + "px");
        redraw();
    }

    private void buildUI() {
        resetBackground();
//        if(buckets.isEmpty()) {
//            setText("");
//            return;
//        }
//
//        int widgetWidth = (int) Math.ceil(RangeCalculator.createBinSize(width, buckets.size()));
//        
//        setHTML(buildDivs(widgetWidth));
    }

    
    private void resetBackground() {
        if(backgroundRectangle != null){
            backgroundRectangle.removeAllHandlers();
            backgroundLayer.removeAll();
            backgroundRectangle = null;
        }
        
        //Height & width fail-safes
        int height = 20;
        int width = 10000;


        if(this.getOffsetWidth() > 100){
            width = this.getOffsetWidth();
        }

        backgroundRectangle = new Rectangle(0, 0, width, height);

        backgroundLayer.addItem(backgroundRectangle);
//        backgroundRectangle.addMouseUpHandler(mouseUpHandler);
//        backgroundRectangle.addMouseMoveHandler(mouseMoveHandler);
        //backgroundRectangle.setFillStyle(backgroundColor);
        backgroundRectangle.setStrokeStyle(backgroundColor);
        
    }
    
    public void setColors(List<Integer> colorIndexes, double widgetWidth) {
        colorLayer.clear();
        colorLayer.start();
        int ii = 0;
        ColorRenderable renderable = null;
        for(Integer colorIndex: colorIndexes){
            
            renderable = new ColorRenderable(ii*widgetWidth, widgetWidth, ChartOverviewColorMapper.getColor(colorIndex));
            renderable.bind(colorLayer);
            colorLayer.addItem(renderable);
            ii++;
        }
        
        if(renderable != null){
            renderable.addPostRenderingProcess(new PostRenderingProcess(){
    
                @Override
                public void execute() {
                    colorLayer.stop();
                }});
        }
    }
    

    public void reset() {
        colorLayer.clear();
    }


}
