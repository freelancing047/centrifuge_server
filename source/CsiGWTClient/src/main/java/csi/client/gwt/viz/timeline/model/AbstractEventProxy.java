package csi.client.gwt.viz.timeline.model;


public abstract class AbstractEventProxy implements EventProxy, Comparable{
    @Override
    public int compareTo(Object o) {
        return compareTo((EventProxy)o);
    }

    public int compareTo(EventProxy eventRenderable){

        if (this==eventRenderable)
            return 0;
        if (eventRenderable==null || eventRenderable.getStartTime() == null)
            return 1;
        if (this.getStartTime() == null){
            return -1;
        }
        long dt= getStartTime() - eventRenderable.getStartTime();
        if (dt==0)
            return 0;
        if (dt>0)
            return 1;
        return -1;
    }

}
