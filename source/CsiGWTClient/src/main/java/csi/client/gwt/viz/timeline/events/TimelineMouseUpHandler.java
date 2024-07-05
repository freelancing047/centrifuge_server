//package csi.client.gwt.viz.timeline.events;
//
//import com.google.common.base.Optional;
//import com.google.gwt.event.dom.client.MouseUpEvent;
//import com.google.gwt.event.dom.client.MouseUpHandler;
//import csi.client.gwt.viz.timeline.drawing.TimelineTrackRenderable;
//import csi.client.gwt.viz.timeline.drawing.ZoomRectangle;
//import csi.client.gwt.viz.timeline.drawing.layer.ComplexLayer;
//import csi.client.gwt.widget.drawing.Renderable;
//
///**
// * Created by Ivan on 8/29/2017.
// */
//public class TimelineMouseUpHandler implements MouseUpHandler {
//    private ComplexLayer eventLayer;
//    private ZoomRectangle zoomRectangle;
//    private ComplexLayer zoomLayer()
//
//    public TimelineMouseUpHandler(ComplexLayer eventLayer, ZoomRectangle zoomRectangle) {
//        this.eventLayer = eventLayer;
//        this.zoomRectangle = zoomRectangle;
//    }
//
//    @Override
//    public void onMouseUp(MouseUpEvent event) {a
//        eventLayer.start();
//
//        if(zoomRectangle != null){
//            zoomLayer.remove(zoomRectangle);
//        }
//
//        if (!mouseIsDown){ // this means we had clicked on a date label.
//            return;
//        }
//        mouseIsDown=false;
//        //eventLayer.stop();
//        int a=(int) Math.min(event.getX(), firstMouse.getX());
//        int b=(int) Math.max(event.getX(), firstMouse.getX());
//
//        if (b-a<16) // a click rather than a drag;
//        {
//            Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
//            if(optional.isPresent()){
//                TimelineTrackRenderable track = (TimelineTrackRenderable) optional.get();
//                track.toggleCollapse();
//                view.getEventBus().fireEvent(new TrackChangeEvent(track.getTrackModel()));
//            }
//            return;
//        }
//        else
//        {
//            if(event.isControlKeyDown() && !event.isShiftKeyDown()){
//                RangeSelectionEvent rangeSelectionEvent = new RangeSelectionEvent(a, b, true);
//                view.getEventBus().fireEvent(rangeSelectionEvent);
//                //					repaint();
//            } else if(event.isControlKeyDown() && event.isShiftKeyDown()){
//                RangeSelectionEvent rangeSelectionEvent = new RangeSelectionEvent(a, b, false);
//                view.getEventBus().fireEvent(rangeSelectionEvent);
//            }else {
//
//                TimeScaleChangeEvent timeScaleChangeEvent = new TimeScaleChangeEvent(a, b);
//                view.getEventBus().fireEvent(timeScaleChangeEvent);
//            }
//            //moveTime(new Interval(start,end));
//
//        }
//        eventLayer.start();
//
//    }
//    }
//}
