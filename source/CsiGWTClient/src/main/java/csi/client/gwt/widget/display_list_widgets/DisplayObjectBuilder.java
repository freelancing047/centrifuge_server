package csi.client.gwt.widget.display_list_widgets;


/**
 * Created by centrifuge on 3/8/2015.
 */
public interface DisplayObjectBuilder<S, T> {

    public <R extends S> R createObject(T itemIn);
    public <R extends S> R createObject();
}
