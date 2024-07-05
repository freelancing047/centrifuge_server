package csi.client.gwt.viz.timeline.view.drawing;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class TimelineSliderRenderable extends BaseRenderable {

	private static final CssColor MID_COLOR = CssColor.make(91, 160, 228);
    private static final CssColor MID_DRAG_COLOR = CssColor.make(91, 160, 228);
    private static final CssColor THUMB_COLOR = CssColor.make(251,89,77);
    private static final CssColor THUMB_DRAG_COLOR = CssColor.make(237,40,40);
    private static final double OPACITY = .55;
    private static final double DRAG_OPACITY = .75;
	private static final int THUMB_HEIGHT = 50;
	static final int THUMB_WIDTH = 10;
	private TimelineSlider timelineSlider;	
	private TimeScale timeScale;
	private int x0,x1;
    private boolean leftDragging = false;
    private boolean rightDragging = false;
    private Layer layer;
    private boolean panning;
    private long lastStart;
    private long lastEnd;
    private boolean hoverLeft = false;
    private boolean hoverRight = false;
    private boolean hoverCenter = false;
	
	@Override
	public void render(Context2d context2d) {
	    context2d.save();
		if(timelineSlider.getStart() == null){
			
			timelineSlider.setStart(timeScale.getStart());
			
		}
		
		if(timelineSlider.getEnd() == null){
			
			timelineSlider.setEnd(timeScale.getEnd());
			
		}

		x0 = (int) timeScale.toNum(timelineSlider.getStart());
		x1 = (int) timeScale.toNum(timelineSlider.getEnd());
		
		int start = x0;
		int end = x1;
		
		if(start <= timeScale.getLow()){
		    start = (int) (timeScale.getLow());
		}
		
		if(end >= timeScale.getHigh()){
		    end = (int) (timeScale.getHigh());
		}
		
		context2d.setGlobalAlpha(OPACITY);
		
		//Render middle rectangle
		if(isPanning() || hoverCenter){
            context2d.setGlobalAlpha(DRAG_OPACITY);
			context2d.setFillStyle(MID_DRAG_COLOR);
		} else {
			context2d.setFillStyle(MID_COLOR);
			
		}
		context2d.fillRect(start, 0, end-start, THUMB_HEIGHT);
		
		if(isLeftDragging() || hoverLeft){
	        context2d.setGlobalAlpha(1);
			context2d.setFillStyle(THUMB_DRAG_COLOR);
		} else {
	        context2d.setGlobalAlpha(1);
			context2d.setFillStyle(THUMB_COLOR);
		}
		
		//render left thumb
		context2d.fillRect(start - THUMB_WIDTH, 0, THUMB_WIDTH, THUMB_HEIGHT);
		
		if(isRightDragging() || hoverRight){
	        context2d.setGlobalAlpha(1);
            context2d.setFillStyle(THUMB_DRAG_COLOR);
        } else {
            context2d.setGlobalAlpha(1);
            context2d.setFillStyle(THUMB_COLOR);
        }
		//render right thumb
		context2d.fillRect(end, 0, THUMB_WIDTH, THUMB_HEIGHT);

		context2d.setGlobalAlpha(1);
		context2d.restore();
	}

	@Override
	public boolean hitTest(double x, double y) {
	    // as implemented only affects filled region not stroke.
        if ((x < x0 - THUMB_WIDTH) || x > x1 + THUMB_WIDTH) {
            // to the left, above, to the right, or bellow
            // the later conditions must check equality.
            return false;
        }
        if(y >layer.getHeight() || y<0){
            return false;}
        if(x>layer.getWidth() || x<0){
            return false;}
        return true;
	}

	@Override
	public void bind(Layer layer) {
		this.setLayer(layer);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public TimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(TimeScale timeScale) {
		this.timeScale = timeScale;
	}

	public TimelineSlider getTimelineSlider() {
		return timelineSlider;
	}

	public void setTimelineSlider(TimelineSlider timelineSlider) {
		this.timelineSlider = timelineSlider;
	}


	public void moveLeftThumb(long startTime) {
	    if(timelineSlider != null)
		timelineSlider.setStart(startTime);
		
	}

	public void moveRightThumb(long endTime) {

        if(timelineSlider != null)
		timelineSlider.setEnd(endTime);
		
	}
	
	public int getStartX(){
        return x0;
    }
	
	public int getEndX(){
        return x1;
    }

	public int getThumbWidth(){
	    return THUMB_WIDTH;
	}

	public boolean isDragging(){
	    return this.leftDragging || this.rightDragging;
	}
	
    public void setLeftDragging(boolean b) {
        this.leftDragging = b;
        rememberState();
    }

    private void rememberState() {
        lastStart = timelineSlider.getStart().longValue();
        lastEnd = timelineSlider.getEnd().longValue();
    }
    
    public void hoverLeft() {
        
    }
    
    public void cancelDrag() {
        timelineSlider.setStart(lastStart);
        timelineSlider.setEnd(lastEnd);

        setDragging(false);
        setPanning(false);
    }

    public boolean isRightDragging() {
        return rightDragging;
    }

    public void setRightDragging(boolean rightDragging) {
        this.rightDragging = rightDragging;
        rememberState();
    }

    public boolean isLeftDragging() {
        return leftDragging;
    }

    public void setDragging(boolean b) {
        setRightDragging(b);
        setLeftDragging(b);
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
    
    public boolean isPanning(){
        return panning;
    }

    public void setPanning(boolean b) {
        this.panning = b;
        rememberState();
    }

    public void updateTimeScale(int start, int end) {
        timeScale.setNumberRange(start, end);
    }

    public boolean isHoverLeft() {
        return hoverLeft;
    }

    public void setHoverLeft(boolean hoverLeft) {
        this.hoverLeft = hoverLeft;
    }

    public boolean isHoverRight() {
        return hoverRight;
    }

    public void setHoverRight(boolean hoverRight) {
        this.hoverRight = hoverRight;
    }

    public boolean isHoverCenter() {
        return hoverCenter;
    }

    public void setHoverCenter(boolean hoverCenter) {
        this.hoverCenter = hoverCenter;
    }

    public void setHover(boolean b) {
        hoverLeft=b;
        hoverRight=b;
        hoverCenter=b;
    }
}
