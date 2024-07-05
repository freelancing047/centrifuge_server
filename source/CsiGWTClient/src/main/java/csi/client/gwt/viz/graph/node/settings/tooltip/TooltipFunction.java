package csi.client.gwt.viz.graph.node.settings.tooltip;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.attribute.AttributeAggregateType;

public enum TooltipFunction {
    SUM(AttributeAggregateType.SUM) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;
        }
    }, //
    COUNT(AttributeAggregateType.COUNT) {
        @Override
        public boolean isSupported(CsiDataType type) {
            return true;
        }
    }, //
    COUNT_DISTINCT(AttributeAggregateType.COUNT_DISTINCT) {
        @Override
        public boolean isSupported(CsiDataType type) {
            return true;
        }
    }, //
    MIN(AttributeAggregateType.MIN) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;

        }
    }, //
    MAX(AttributeAggregateType.MAX) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;

        }
    }, //
    AVG(AttributeAggregateType.AVG) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;

        }
    }, //
    ABS_AVG(AttributeAggregateType.ABS_AVG) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;

        }
    }, //
    ABS_SUM(AttributeAggregateType.ABS_SUM) {
        @Override
        public boolean isSupported(CsiDataType type) {
            switch (type) {
                case String:
                    break;
                case Boolean:
                    break;
                case Integer:
                    return true;
                case Number:
                    return true;
                case DateTime:
                    break;
                case Date:
                    break;
                case Time:
                    break;
                case Unsupported:
                    break;
            }
            return false;

        }
    }, //
    ;

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private AttributeAggregateType aggregateType;

    TooltipFunction(AttributeAggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }
    
    public String getDisplayString() {
        return getDisplayName(this);
    }

    public static String getDisplayName(TooltipFunction function) {
        //Not using a field because it cannot be set in advance.
        switch (function) {
            case SUM:
                return i18n.sum();
            case ABS_AVG:
                return i18n.absoluteAverage();
            case ABS_SUM:
                return i18n.absoluteSum();
            case AVG:
                return i18n.average();
            case COUNT:
                return i18n.count();
            case COUNT_DISTINCT:
                return i18n.countDistinct();
            case MAX:
                return i18n.maximum();
            case MIN:
                return i18n.minimum();
            default:
                //TODO: log this case...
                return "";
        }
    }
    public abstract boolean isSupported(CsiDataType type);

    public AttributeAggregateType getAggregateType() {
        return aggregateType;
    }

    public static TooltipFunction getByAggregateFunction(AttributeAggregateType aggregateFunction) {
        //TODO: might be slightly faster to compute a static reverse map;
        for (TooltipFunction func : values()) {
            if(func.getAggregateType().equals(aggregateFunction)) {
                return func;
            }
        }
        return null;
    }
}
