package csi.server.business.service.visualization.theme;

import java.util.List;

import csi.server.business.service.theme.ThemeActionsService;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.dao.CsiPersistenceManager;

public class ThemeManager {
    
    public static GraphTheme getGraphTheme(String uuid) {
        if(uuid == null || uuid.isEmpty()){
            return null;
        }
        
        if(ThemeActionsService.checkAuthorization(uuid)){
            GraphTheme graphTheme = CsiPersistenceManager.findObject(GraphTheme.class, uuid);
            CsiPersistenceManager.detachEntity(graphTheme);
            return graphTheme;
        }
        
        return null;
    }

    public static LinkStyle getLinkStyle(String typeName, GraphTheme theme) {
        
        List<LinkStyle> linkStyles = theme.getLinkStyles();
        for(LinkStyle style: linkStyles){
            if(style.getFieldNames().contains(typeName)){
               return style; 
            }
        }
        
        return null;
    }

}
