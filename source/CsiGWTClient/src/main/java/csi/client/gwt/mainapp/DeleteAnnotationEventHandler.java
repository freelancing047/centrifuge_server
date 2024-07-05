package csi.client.gwt.mainapp;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class DeleteAnnotationEventHandler extends BaseCsiEventHandler {
    public abstract void onDelete(AnnotationCard annotationCard);
}
