package csi.client.gwt.mainapp;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEvent;

public class SaveEditAnnotationEvent extends BaseCsiEvent<SaveEditAnnotationEventHandler> {
    private AnnotationCard annotationCard;
    private Widget annotationCardWidget;
    public static final GwtEvent.Type<SaveEditAnnotationEventHandler> type = new GwtEvent.Type<SaveEditAnnotationEventHandler>();

    @Override
    public Type<SaveEditAnnotationEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(SaveEditAnnotationEventHandler handler) {
        handler.onSaveEdit(annotationCard);
    }

    public void setAnnotationCardWidget(Widget annotationCardWidget) {
        this.annotationCardWidget = annotationCardWidget;
    }

    public void setAnnotationCard(AnnotationCard annotationCard) {
        this.annotationCard = annotationCard;
    }
}
