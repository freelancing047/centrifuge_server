package csi.client.gwt.viz.shared;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.timeline.model.LegendItem;

import java.util.ArrayList;
import java.util.List;

/**
 * In progress still.
 *
 * //TODO
 *  - add getLegendAsCanvas( possibly, we need to color array passed in, but we can also extract the color from the html so it might be easier that way)
 *  - reordering for the other legends will probably be mia, but maybe add another class to house that
 */
public abstract class ExportableLegend extends Composite {
    protected FluidContainer fluidContainer;

    protected ExportableLegend(){
//        initWidget(fluidContainer);
    }


    public List<String> getVisItems(){
        List<String> visItems = new ArrayList<String>();

        int containerBottom = fluidContainer.getElement().getAbsoluteBottom();
        int containerTop = fluidContainer.getAbsoluteTop();

        for(int i = 0; i < fluidContainer.getWidgetCount(); i++){
            Widget widget = fluidContainer.getWidget(i);
            int legendItemTop = widget.getElement().getAbsoluteTop() + 5;

            if(legendItemTop > containerTop  && legendItemTop < containerBottom) {
                visItems.add(widget.getElement().getInnerText().trim());
            }
        }

        return visItems;
    }


    public native void console(String msg) /*-{
        console.log(msg);
    }-*/;


    public abstract void addLegendItem(IsWidget item);

    public abstract void clear();

}
