package csi.client.gwt.viz.graph.window.legend;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;

public class BundleNodeLegendItemProxy extends NodeLegendItemProxy {

    public BundleNodeLegendItemProxy(Graph graph, GraphNodeLegendItem item) {
        super(graph, item);
    }

    @Override
    public void getImageMaybeStyle(int imageSize) {
        if(iconId != null || shape != null) {

            GraphTheme theme = graph.getTheme();
            NodeStyle style = null;
            if(theme != null){
                style = theme.getBundleStyle();
            }
            Double iconScale = 1.08;
            
            if(style != null && style.getIconScale() != null){
                //iconScale = style.getIconScale();
                
            }
            if(style != null && style.getIconId() != null){
                GraphImpl.getRenderedIcon(style.getIconId(), shape, color, imageSize, iconScale, image);
                return;
            } 

            GraphImpl.getRenderedIcon(iconId, shape, color, imageSize, iconScale, image);
            return;

        }

        GraphImpl.getBundleIcon(20, 1.08, image);
    }


    @Override
    public String getKey(){
        return item.key;
    }
}
