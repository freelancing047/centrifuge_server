package csi.client.gwt.widget.display_list_widgets;

import com.google.gwt.resources.client.ImageResource;

/**
 * Created by centrifuge on 3/20/2015.
 */
public interface ComponentTreeHelper<T> {

    public String getLabel(T itemIn);
    public ImageResource getIcon(T itemIn);
}
