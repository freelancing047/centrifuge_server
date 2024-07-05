package csi.client.gwt.widget.combo_boxes;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.link.settings.LinkDirectionDef;
import csi.shared.gwt.viz.graph.LinkDirection;

public class DirectionLabelProvider implements LabelProvider<LinkDirection>{

    private LinkDirectionDef directionDef;
    
    public DirectionLabelProvider(){

    }

    @Override
    public String getLabel(LinkDirection item) {
        if(directionDef == null){
            return item.name();
        }
        if(item == LinkDirection.NONE){
            return CentrifugeConstantsLocator.get().linkDirection_undirected();
        } else if(item == LinkDirection.FORWARD){
            return directionDef.getNode1Name() + " " + CentrifugeConstantsLocator.get().linkDirection_to() + " " + directionDef.getNode2Name();
        } else if(item == LinkDirection.REVERSE){
            return directionDef.getNode2Name() + " " + CentrifugeConstantsLocator.get().linkDirection_to() + " " + directionDef.getNode1Name();
        } else if(item == LinkDirection.DYNAMIC){
            return CentrifugeConstantsLocator.get().dynamic();
        } else {
            return item.toString();
        }
    }



    public LinkDirectionDef getDirectionDef() {
        return this.directionDef;
    }



    public void setDirectionDef(LinkDirectionDef directionDef) {
        this.directionDef = directionDef;
    }


}