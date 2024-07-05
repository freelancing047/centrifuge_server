package csi.client.gwt.viz.graph.surface.multitypes;

import java.util.List;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.base.HtmlWidget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;

public class MultiTypeView implements IsWidget{
    
    private final int MAX_SHOWN = 3;
    
    private FluidContainer fluidContainer;
    public void create(List<LegendItemProxy> items){
        fluidContainer = new FluidContainer();
        Style style = fluidContainer.getElement().getStyle();
        style.setBackgroundColor("#FFFFFF");
        style.setBorderStyle(BorderStyle.SOLID);
        style.setBorderWidth(1, Unit.PX);
        style.setProperty("borderRadius", "5px");
        int count = 0;
        for(LegendItemProxy item: items){
            if(count >= MAX_SHOWN){
                break;
            }
            HtmlWidget widget = new HtmlWidget("div","");
            String imageUrl = item.getImageUrl();
            if(imageUrl != null){
                Image image = new Image();
                image.setUrl(imageUrl);
                widget.add(image);
                //fluidContainer.add(image);
            }
            widget.add(new HtmlWidget("span", item.getType()));
            fluidContainer.add(widget);
            count++;
        }
        
        int moreItems = items.size() - MAX_SHOWN;
        
        if(moreItems > 0){
            addMoreBanner(moreItems);
        }
        
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand(){

            @Override
            public boolean execute() {
                if(fluidContainer != null){
                    fluidContainer.removeFromParent();
                    
                    return true;
                } else {

                    return false;
                }
            }
            
        }, 3000);
    }

    private void addMoreBanner(int more) {

        HtmlWidget widget = new HtmlWidget("div", "+" + more + " more");
        fluidContainer.add(widget);
    }

    public void setPosition(double x, double y) {
        fluidContainer.getElement().getStyle().setDisplay(Display.INLINE);  
        fluidContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
//        fluidContainer.getElement().getStyle().setMarginTop(y, Unit.PX);
//        fluidContainer.getElement().getStyle().setMarginLeft(x, Unit.PX);
    }


    @Override
    public Widget asWidget() {
        return fluidContainer;
    }

}
