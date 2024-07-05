package csi.client.gwt.widget.misc;

/**
 * An implementation of WidgetCallback with a no op callback option 
 * @author bmurray
 *
 */
public class NoOpWidgetCallback implements WidgetCallback{

    public static final NoOpWidgetCallback INSTANCE = new NoOpWidgetCallback();
    
    @Override
    public void action() {
        //Do Nothing
    }

}
