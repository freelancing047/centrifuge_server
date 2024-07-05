package csi.shared.gwt.viz.graph;

public enum LinkDirection {
    NONE{
        @Override
        public String toString() {
            return "NONE";
        }
    },
    FORWARD{
        @Override
        public String toString() {
            return "FORWARD";
        }
    },
    REVERSE{
        @Override
        public String toString() {
            return "REVERSE";
        }
    }, 
    BOTH{
        @Override
        public String toString() {
            return "BOTH";
        }
    },
    DYNAMIC{
        @Override
        public String toString() {
            return "DYNAMIC";
        }
    },;
    
    public static LinkDirection revert(LinkDirection direction) {
        switch (direction) {
            case FORWARD:
                return REVERSE;
            case REVERSE:
                return FORWARD;
            default:
                return direction;
        }
    }
    
}
