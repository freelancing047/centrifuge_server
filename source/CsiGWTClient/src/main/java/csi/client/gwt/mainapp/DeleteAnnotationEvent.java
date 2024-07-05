package csi.client.gwt.mainapp;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEvent;
import csi.server.common.model.visualization.graph.Annotation;

public class DeleteAnnotationEvent  extends BaseCsiEvent<DeleteAnnotationEventHandler> {
    private AnnotationCard annotationCard;
    private Widget annotationCardWidget;
    public static final GwtEvent.Type<DeleteAnnotationEventHandler> type = new GwtEvent.Type<DeleteAnnotationEventHandler>();


    @Override
    public Type<DeleteAnnotationEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(DeleteAnnotationEventHandler handler) {
        handler.onDelete(annotationCard);
    }

    public void setAnnotationWidget(Widget annotationCardWidget) {
        this.annotationCardWidget = annotationCardWidget;
    }

    public void setAnnotationCard(AnnotationCard annotationCard) {
        this.annotationCard = annotationCard;
    }
}
