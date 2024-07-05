package csi.server.business.visualization.viewer.lens;

import com.google.common.collect.Lists;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.LinkObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.LinkLensImage;
import prefuse.data.Edge;

import java.util.ArrayList;
import java.util.List;

public class LinkLens implements Lens {
    @Override
    public LensImage focus(LensDef lensDef, Objective objective) {
        LinkLensImage image = new LinkLensImage();
        if (objective instanceof LinkObjective) {
            LinkObjective linkObjective = (LinkObjective) objective;

            GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());

            Edge link = graphContext.getGraphData().getEdge(linkObjective.getFindItemDTO().getID());
            LinkStore details = GraphManager.getEdgeDetails(link);
            image.setCount(details.getCountForward() + "");
            image.setLabel(details.getLabel());
            ArrayList<String> types = Lists.newArrayList(details.getTypes().keySet());
            image.setTypes(types);

        }
        return image;
    }

    @Override
    public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
        if (objective instanceof LinkObjective) {
            LinkObjective linkObjective = (LinkObjective) objective;

            GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());

            Edge link = graphContext.getGraphData().getEdge(linkObjective.getFindItemDTO().getID());
            LinkStore details = GraphManager.getEdgeDetails(link);

            LinkLensImage.LinkLensTokens linkLensTokens = LinkLensImage.LinkLensTokens.valueOf(token);
            switch (linkLensTokens) {

                case TYPE: {
                    ArrayList<List<?>> out = Lists.newArrayList();
                    ArrayList<?> keys = Lists.newArrayList(details.getTypes().keySet());
                    ArrayList<?> values = Lists.newArrayList(details.getTypes().values());
                    out.add(keys);
                    out.add(values);
                    return out;
                }
                case LABEL: {
                    ArrayList<List<?>> out = Lists.newArrayList();
                    ArrayList<?> keys = Lists.newArrayList(details.getLabels());
                    out.add(keys);
                    return out;
                }
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public ViewerGridConfig getGridConfig() {
        return null;
    }

}
