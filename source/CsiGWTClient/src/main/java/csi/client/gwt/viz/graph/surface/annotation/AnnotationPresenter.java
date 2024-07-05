package csi.client.gwt.viz.graph.surface.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.TooltipPropsDTO;

public class AnnotationPresenter {

    private String vizUuid;
    private FindItemDTO item;
    private AnnotationDialog view;
    private AnnotationModel model;
    private String currentHtml;
    private GraphSurface graphSurface;

    private final String ANNOTATION_PROPERTY = "Annotation";//NON-NLS

    public AnnotationPresenter(Graph graph, FindItemDTO item) {
        this.model = new AnnotationModel();
        this.vizUuid = graph.getUuid();
        this.item = item;
        this.graphSurface = graph.getGraphSurface();
        setHtml(item);
        view = new AnnotationDialog(this);
    }

    private void setHtml(FindItemDTO item) {

        TooltipPropsDTO props = item.getTooltips();
        CsiMap<String, List<String>> attributes = props.getAttributeNames();
        List<String> annotations = attributes.get("Comments");//NON-NLS

        this.currentHtml = "";
        if (annotations == null) {
            return;
        }
        for (String annotation : annotations) {
            this.currentHtml += annotation;
        }

    }

    public void show() {
        view.show();
    }

    public void addAnnotation(String html) {

        TooltipPropsDTO props = item.getTooltips();
        CsiMap<String, List<String>> map = props.getAttributeNames();

        Set<String> keyset = map.keySet();
        List<String> currentAnnotations;

        if (!keyset.contains(ANNOTATION_PROPERTY)) {

            currentAnnotations = new ArrayList<>();

        } else {

            currentAnnotations = map.get(ANNOTATION_PROPERTY);

        }

        if (currentAnnotations == null) {

            currentAnnotations = new ArrayList<>();
        }

        map.put(ANNOTATION_PROPERTY, currentAnnotations);

        model.addAnnotation(vizUuid, html, item.getItemKey(), graphSurface);


    }

    public String getCurrentHtml() {
        return currentHtml;
    }

    public void remove() {
        model.removeAnnotation(vizUuid, item.getItemKey(), graphSurface);
    }

}
