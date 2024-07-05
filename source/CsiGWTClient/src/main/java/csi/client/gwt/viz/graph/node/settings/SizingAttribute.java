package csi.client.gwt.viz.graph.node.settings;

import java.io.Serializable;

import org.bouncycastle.util.Strings;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.core.util.HasLabel;

public enum SizingAttribute implements HasLabel, Serializable{
    
//    <g:item value="{i18n.get.degree}"
//            text="{i18n.get.numberOfNeighbors}"/>
//    <g:item value="Betweenness" text="{i18n.get.betweenness}"/>
//    <g:item value="Closeness" text="{i18n.get.closeness}"/>
//    <g:item value="Eigenvector" text="{i18n.get.eigenvector}"/>
//    <g:item value="Count" text="{i18n.get.occurrence}"/>;

    DEGREE,
    BETWEENNESS,
    CLOSENESS,
    EIGENVECTOR,
    COUNT
    ;

    public String getName() {
        if(this == DEGREE){
            return CentrifugeConstantsLocator.get().degree();
        } else {
            return super.name();
        }
    }
    
    @Override
    public String getLabel() {
        if(this == DEGREE){
            return CentrifugeConstantsLocator.get().numberOfNeighbors();
        } else if(this == BETWEENNESS){
            return CentrifugeConstantsLocator.get().betweenness();
        } else if(this == CLOSENESS){
            return CentrifugeConstantsLocator.get().closeness();
        } else if(this == EIGENVECTOR){
            return CentrifugeConstantsLocator.get().eigenvector();
        } else if(this == COUNT){
            return CentrifugeConstantsLocator.get().occurrence();
        };
        
        return this.name();
    }
    
    
    
    public static SizingAttribute valueOfIgnoreCase(String textValue) {
        textValue = textValue.toUpperCase();
        return valueOf(textValue);
    }

}
