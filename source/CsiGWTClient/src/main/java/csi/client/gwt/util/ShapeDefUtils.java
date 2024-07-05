package csi.client.gwt.util;

import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.ShapeDefResource;

public class ShapeDefUtils {
    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	public static ImageResource getShapeImage(String shapeName) {
        switch (shapeName) {
            case "Circle":
                return ShapeDefResource.IMPL.shapeCircle();
            case "Square":
                return ShapeDefResource.IMPL.shapeSquare();
            case "Diamond":
                return ShapeDefResource.IMPL.shapeDiamond();
            default:
                throw new RuntimeException(i18n.shapeDefImageFieldException() + shapeName); //$NON-NLS-1$
        }
    }
}
