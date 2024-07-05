package csi.client.gwt.viz.timeline.view.drawing.layer;

import com.google.common.base.Optional;
import com.google.gwt.event.dom.client.MouseEvent;

import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class ComplexLayer extends Layer {

    private boolean started = true;
    private boolean eventPassingEnabled = true;
    
    public void render() {
        if(started){
            super.render();
        }
    }
    
    public void stop(){
        started = false;
    }
    
    public void start(){
        started = true;
    }
    
    public Optional<Renderable> retrieveRenderableAtEvent(MouseEvent event) {
        this.eventPassingEnabled = true;
        Optional<Renderable> hitTest = this.hitTest(event.getX(), event.getY());
        this.eventPassingEnabled = false;
        return hitTest;
    }

    public boolean isEventPassingEnabled() {
        return eventPassingEnabled;
    }

    public void setEventPassingEnabled(boolean b) {
        this.eventPassingEnabled = b;
    }
}
