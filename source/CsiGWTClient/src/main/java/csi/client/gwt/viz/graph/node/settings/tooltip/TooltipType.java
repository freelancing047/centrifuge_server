package csi.client.gwt.viz.graph.node.settings.tooltip;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public enum TooltipType {
    FIXED {

        @Override
        public String toString() {
            return CentrifugeConstantsLocator.get().fixed();
        }
    },
    DYNAMIC {

        @Override
        public String toString() {
            return CentrifugeConstantsLocator.get().dynamic();
        }
    },
    COMPUTED {

        @Override
        public String toString() {
            return CentrifugeConstantsLocator.get().computed();
        }
    },
    GRAPH_ATTRIBUTE {

        public String toString() {
            return CentrifugeConstantsLocator.get().graphAttribute();
        }
    }

}
