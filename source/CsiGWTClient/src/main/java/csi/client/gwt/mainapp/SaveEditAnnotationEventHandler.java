package csi.client.gwt.mainapp;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class SaveEditAnnotationEventHandler extends BaseCsiEventHandler {
    public abstract void onSaveEdit(AnnotationCard annotationCard);
}
