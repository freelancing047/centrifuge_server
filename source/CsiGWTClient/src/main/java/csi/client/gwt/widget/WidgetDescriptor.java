package csi.client.gwt.widget;

import com.google.gwt.user.client.ui.Widget;


public class WidgetDescriptor {

    private Widget _widget;
    private int _height;
    private int _width;
    
    public WidgetDescriptor(Widget widgetIn, int heightIn, int widthIn) {
        
        _widget = widgetIn;
        _height = heightIn;
        _width = widthIn;
    }
    
    public Widget getWidget() {
        
        return _widget;
    }
    
    public int getHeight() {
        
        return _height;
    }
    
    public int getWidth() {
        
        return _width;
    }
}
