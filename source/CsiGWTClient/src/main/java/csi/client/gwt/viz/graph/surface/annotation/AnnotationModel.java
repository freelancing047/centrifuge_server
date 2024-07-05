package csi.client.gwt.viz.graph.surface.annotation;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.AnnotationDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.Annotation;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class AnnotationModel {


    public VortexFuture<Annotation> addAnnotation(String vizUuid, String htmlString, final String itemKey, final GraphSurface graphSurface) {
        VortexFuture<Annotation> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Annotation>() {
            @Override
            public void onSuccess(Annotation result) {
                graphSurface.getToolTipManager().removeToolTip(itemKey);
                Graph graph = graphSurface.getGraph();

                //Persist annotation to client model.
                if (graph instanceof GraphImpl) {
                    RelGraphViewDef def = ((GraphImpl) graph).getVisualizationDef();
                    for (Annotation annotation : def.getAnnotations()) {
                        if (annotation.getUuid().equals(result.getUuid())) {
                            def.getAnnotations().remove(annotation);
                        }
                    }
                    def.getAnnotations().add(result);
                }
            }
        });
        AnnotationDTO annotationDTO = AnnotationDTO.create(vizUuid, htmlString, itemKey);
        try {
            future.execute(GraphActionServiceProtocol.class).addAnnotation(annotationDTO);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return future;
    }

    public void removeAnnotation(String vizUuid, final String itemKey, final GraphSurface graphSurface) {
        VortexFuture<Annotation> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Annotation>() {
            @Override
            public void onSuccess(Annotation result) {
                graphSurface.getToolTipManager().removeToolTip(itemKey);
            }
        });
        AnnotationDTO annotationDTO = AnnotationDTO.create(vizUuid, null, itemKey);
        try {
            future.execute(GraphActionServiceProtocol.class).removeAnnotation(annotationDTO);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
    }
}
