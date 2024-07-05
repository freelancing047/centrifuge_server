package csi.client.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ShapeDefResource extends ClientBundle {

	ShapeDefResource IMPL = (ShapeDefResource) GWT.create(ShapeDefResource.class);

    @Source("images/shapes/circle.png")
    ImageResource shapeCircle();
    
    @Source("images/shapes/square.png")
    ImageResource shapeSquare();
    
    @Source("images/shapes/diamond.png")
    ImageResource shapeDiamond();
}
