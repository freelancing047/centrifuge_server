package csi.client.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SortResource extends ClientBundle {

    SortResource IMPL = (SortResource) GWT.create(SortResource.class);

    @Source("images/sortAscending.png")
    ImageResource sortAscending();

    @Source("images/sortDescending.png")
    ImageResource sortDescending();

    @ClientBundle.Source("images/search.png")
    ImageResource searchIcon();

    @ClientBundle.Source("images/noSearch.png")
    ImageResource cancelSearchIcon();

    @ClientBundle.Source("images/checked.png")
    ImageResource checkedIcon();

    @ClientBundle.Source("images/unchecked.png")
    ImageResource uncheckedIcon();

    @ClientBundle.Source("images/smallUpButton.png")
    ImageResource upButtonIcon();

    @ClientBundle.Source("images/smallDownButton.png")
    ImageResource downButtonIcon();
}
