package csi.client.gwt.widget.ui.color;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public enum DiscreteColorAttribute {
    DIVERGING,
    QUALITATIVE,
    SEQUENTIAL
    ;

    public static String getInternationalizedType(DiscreteColorType type) {
        if(type == DiscreteColorType.DIVERGING){
            return CentrifugeConstantsLocator.get().discreteColorType_DIVERGING();
        } else if(type == DiscreteColorType.QUALITATIVE){
            return CentrifugeConstantsLocator.get().discreteColorType_QUALITATIVE();
        } else if(type == DiscreteColorType.SEQUENTIAL){
            return CentrifugeConstantsLocator.get().discreteColorType_SEQUENTIAL();
        }
        return CentrifugeConstantsLocator.get().discreteColorType_DIVERGING();
    }

}
