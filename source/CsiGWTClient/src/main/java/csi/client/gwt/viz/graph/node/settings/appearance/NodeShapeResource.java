package csi.client.gwt.viz.graph.node.settings.appearance;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface NodeShapeResource extends ClientBundle {

    NodeShapeResource IMPL = (NodeShapeResource) GWT.create(NodeShapeResource.class);

    ImageResource circle();

    ImageResource diamond();

    ImageResource hexagon();

    ImageResource house();

    ImageResource octagon();

    ImageResource pentagon();

    ImageResource square();

    ImageResource star();

    ImageResource triangle();

    ImageResource none();
}
