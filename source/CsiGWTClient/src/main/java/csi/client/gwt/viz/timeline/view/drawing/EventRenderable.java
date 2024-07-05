package csi.client.gwt.viz.timeline.view.drawing;

import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.viz.timeline.model.AbstractEventProxy;
import csi.client.gwt.widget.drawing.BaseRenderable;


public abstract class EventRenderable extends BaseRenderable implements HasHighlight, Comparable{

    public static final String ELLIPSIS = "\u2026";
    public abstract void setHandlers(List<HandlerRegistration> handlers);

    
    public abstract Long getStartTime();
    
    @Override
    public int compareTo(Object o) {
        return compareTo((EventRenderable)o);
    }

    protected void drawExpandedLabel(Context2d context2d, AbstractEventProxy event, String label, int tx, int ty) {
        context2d.save();
        context2d.setFont("10pt Arial");
        int width = (int) context2d.measureText(label).getWidth();
        if((width + 25) > event.getSpaceToRight()) {
            tx = tx + (event.getSpaceToRight() - width - 25);
        }
        context2d.setGlobalAlpha(1);
        drawRoundedRectangle(context2d, tx, ty - 8 - 15, 15, width + 8, 2, CssColor.make(230, 240, 245), CssColor.make(240, 240, 245));
        context2d.fillText(label, tx + 2, ty - 10);

        context2d.restore();
    }


    protected static String format(String s, int maxLength, boolean tryNoDots)
    {
        if (s==null)
            return "";
        int n=s.length();
        if (n<=maxLength)
            return s;
        if (maxLength<3)
            return ELLIPSIS;
        if (!tryNoDots)
        {
            return s.substring(0, maxLength-2)+ELLIPSIS;
        }
        // find last space before maxLength and after maxLength/2
        for (int j=maxLength-1; j>maxLength/2; j--)
        {
            if (s.charAt(j)==' ')
                return s.substring(0,j);
        }
        return s.length() + 3<=maxLength ? s + ELLIPSIS : (maxLength<6 ? "" : s.substring(0, maxLength-3)+ELLIPSIS);
    }
    
    public int compareTo(EventRenderable eventRenderable){

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
