package csi.client.gwt.viz.shared.export;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 8/11/2017.
 */
public class LegendExporter {

    static int fontSize = 16;

    public static int[] getLegendSize(java.util.Map<String, String> itemImages){
        int[] ret = new int[2];
        int maxHeight = 50;
        int maxWidth = 0;

        Canvas c = Canvas.createIfSupported();
        Context2d ctx = c.getContext2d();

        for (String key : itemImages.keySet()) {
            TextMetrics textMetrics = ctx.measureText(key);
            maxWidth = textMetrics.getWidth() > maxWidth ? (int) textMetrics.getWidth() : maxWidth;


            String imgURL = itemImages.get(key);
            Image tmp = new Image();
            if(imgURL != null) {
                tmp.setUrl(imgURL);
                maxHeight+=tmp.getHeight();
            }
        }


        ret[0] = maxWidth + 50;
        ret[1] = maxHeight;

        return ret;
    }



    public static String getLegendAsBase64(java.util.Map<String, String> itemImages, String title, int[] size) {
        int _x = 10, _y = 10;

        Canvas c = Canvas.createIfSupported();

        c.setWidth(size[0] + "px");
        c.setCoordinateSpaceWidth(size[0] * 5);

        c.setHeight(size[1] + "px");
        c.setCoordinateSpaceHeight(size[1] * 5);

        Context2d ctx = c.getContext2d();

        ctx.setFillStyle("black");
        ctx.setFont(fontSize+"px");
        ctx.fillText(title, _x, _y);
        _y += 10;
        int prevElHeight = 0;
        int prevElWidth = 0;
        for (String key : itemImages.keySet()) {
            String imgURL = itemImages.get(key);
            Image tmp = new Image();
            if(imgURL != null) {
                tmp.setUrl(imgURL);
                ctx.drawImage((ImageElement) tmp.getElement().cast(), Double.valueOf(_x), Double.valueOf(_y));
            }

            prevElHeight = tmp.getHeight() != 0 ? tmp.getHeight() : prevElWidth;
            prevElWidth = tmp.getWidth() != 0 ? tmp.getWidth() : prevElWidth;

            int widthOffset, heightOffset;

            heightOffset = prevElHeight/2 + fontSize/4 ;
            widthOffset = prevElWidth +5;

            if(!key.equals("Combined Place")) {
                ctx.fillText(key, _x + widthOffset, _y + heightOffset);
            }else{
                ctx.save();
                ctx.setStrokeStyle("rgb(0, 255, 0)");
                ctx.setLineWidth(2);
                ctx.beginPath();
                ctx.moveTo(Double.valueOf(_x), Double.valueOf(_y)+2);
                ctx.lineTo(Double.valueOf(_x), Double.valueOf(_y) + 15);
                ctx.lineTo(Double.valueOf(_x)+15, Double.valueOf(_y)+15);
                ctx.lineTo(Double.valueOf(_x)+15, Double.valueOf(_y)+2);
                ctx.closePath();
                ctx.stroke();
                ctx.restore();
//                ctx.lineTo(Double.valueOf(_x), Double.valueOf(_y));

                ctx.fillText(key, _x + widthOffset, _y + heightOffset);
            }
            if(tmp.getHeight()!= 0) {
                _y += tmp.getHeight();
            }else{
                _y+=prevElHeight;
            }
        }

        ctx.stroke();

        return c.toDataUrl("image/png");
    }


    public static Map<String, String> getVisItems(FluidContainer fluidContainer){
//        FluidContainer fluidContainer = legend.getFluidContainer();
        Map<String, String> visItems = new HashMap<String, String>();

        int containerBottom = fluidContainer.getElement().getAbsoluteBottom();
        int containerTop = fluidContainer.getAbsoluteTop();

        for(int i = 0; i < fluidContainer.getWidgetCount(); i++){
            Widget widget = fluidContainer.getWidget(i);
            int legendItemTop = widget.getElement().getAbsoluteTop() + 5;

            if(legendItemTop > containerTop  && legendItemTop < containerBottom) {
                String itemPicURL = null;
                if(widget instanceof LegendItemProxy) {
                    LegendItemProxy proxy = (LegendItemProxy) widget;
                    itemPicURL = proxy.getImageUrl();
                }

                visItems.put(widget.getElement().getInnerText().trim(), itemPicURL);
            }
        }

        return visItems;
    }

}
