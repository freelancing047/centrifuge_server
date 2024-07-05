package csi.client.gwt.viz.graph.link.settings;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.server.common.model.attribute.AttributeDef;
import csi.shared.core.color.ClientColorHelper.Color;

public class LinkColor {

    private Color color = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultLinkColor();
    private Boolean colorEnabled=false;

    public LinkColor(LinkProxy linkProxy) {
        AttributeDef def = linkProxy.getColorAttributeDef();
        String text = GraphSettings.getStaticTextFromAttributeDef(def);
        if(text != null){
            try {
                color = new Color(Integer.parseInt(text));
            } catch (NumberFormatException e) {
                // can't use the String we found
            }
        }
        
        colorEnabled = linkProxy.isColorOverride();
    }

    public Color getColor() {
        return color;
    }

    public void persist(LinkProxy linkProxy) {
        linkProxy.setColor(color);
        linkProxy.setColorOverride(colorEnabled);

    }

    public boolean isColorEnabled(){
        return this.colorEnabled;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }

    public void setColorEnabled(Boolean value) {
        this.colorEnabled = value;
    }
}