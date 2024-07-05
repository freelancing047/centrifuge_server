package csi.server.ws.support;

import java.util.Properties;

import prefuse.data.Node;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;

public class ImageResolver
    implements JsonGraphConstants
{
    private Properties imageAlias;

    public ImageResolver( Properties aliases ) {
        this.imageAlias = aliases;
    }
    
    public void resolve( Node node ) {
        NodeStore details = GraphManager.getNodeDetails(node);
        resolve( details );
    }

    public void resolve(NodeStore details) {

        Object statusColor = Utility.getPropertyValue(details, STATUS_COLOR);
        Object iconType = Utility.getPropertyValue(details, ICON_TYPE);
        
        StringBuilder builder = new StringBuilder();
        builder.append(iconType);
        if (statusColor != null) {
            builder.append("-");
            builder.append(statusColor);
        }

        String alias = builder.toString();
        if( !imageAlias.containsKey(alias)) {
            alias = iconType.toString();
        }
        
        String iconPath = imageAlias.getProperty(alias);
        
        details.setIcon( iconPath );


        
    }

}
