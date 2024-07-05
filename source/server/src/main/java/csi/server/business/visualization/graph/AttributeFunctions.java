package csi.server.business.visualization.graph;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.attribute.AttributeAggregateType;

public class AttributeFunctions {
    private static EnumMap<AttributeAggregateType, Class> functions;

    static {
        functions = new EnumMap<AttributeAggregateType, Class>(AttributeAggregateType.class);
        functions.put(AttributeAggregateType.MIN, Minimum.class);
        functions.put(AttributeAggregateType.MAX, Maximum.class);
        functions.put(AttributeAggregateType.SUM, Sum.class);
        functions.put(AttributeAggregateType.ABS_SUM, AbsoluteSum.class);
        functions.put(AttributeAggregateType.AVG, Average.class);
        functions.put(AttributeAggregateType.ABS_AVG, AbsoluteAverage.class);
        functions.put(AttributeAggregateType.COUNT, Count.class);
        functions.put(AttributeAggregateType.COUNT_DISTINCT, CountDistinct.class);
    }

    public static Function<Property, Double> getAggregateFunction(AttributeAggregateType key) throws InstantiationException, IllegalAccessException {
        Class<Function<Property, Double>> clazz = functions.get(key);
        return clazz.newInstance();
    }

    static abstract class AbstractStandard implements Function<Property, Double> {

        double results;
        double defaultValue;

        public AbstractStandard() {
            results = Double.MIN_VALUE;
            defaultValue = Double.NaN;
        }

        @Override
        public Double apply(Property prop) {
            if (prop == null) {
                return defaultValue;
            }

            if (!prop.hasValues()) {
                return defaultValue;
            }

            List<Object> values = prop.getValues();
            int index = 0;
            int size = values.size();
            while( (index < size) && (values.get(index) == null) ) {
                index++;
            }

            if ((index >= size) || !(values.get(index) instanceof Number)) {
                return defaultValue;
            }

            for (Object o : values) {
                if ((o != null) && (o instanceof  Number)) {
                    processValue((Number) o);
                }
            }

            return results;
        }

        abstract void processValue(Number n);

    }

    static class Count implements Function<Property, Double> {

        public Count() {
        }

        @Override
        public Double apply(Property property) {

            int count = 0;
            if ((property != null) && property.hasValues()) {
                List<Object> values = property.getValues();
                List<Object> _values = new ArrayList<Object>(values);
                while(_values.contains(null)){
                    _values.remove(null);
                }
                count = _values.size();
            }

            return (double) count;
        }
    }

    static class CountDistinct implements Function<Property, Double> {

        protected Set<Object> unique = new HashSet<Object>();

        @Override
        public Double apply(Property property) {
            unique.clear();
            int count = 0;

            if ((property != null) && property.hasValues()) {
                List<Object> values = property.getValues();
                List<Object> _values = new ArrayList<Object>(values);
                while(_values.contains(null)){
                    _values.remove(null);
                }
                unique.addAll(_values);

                count = unique.size();
            }

            return (double) count;
        }

    }

    static class Minimum extends AbstractStandard {

        public Minimum() {
            super();
            this.results = Double.MAX_VALUE;
        }

        @Override
        public void processValue(Number n) {
            if (results > n.doubleValue()) {
                results = n.doubleValue();
            }
        }
    }

    static class Maximum extends AbstractStandard {

        public Maximum() {
            super();
            this.results = -Double.MAX_VALUE;
        }

        @Override
        protected void processValue(Number n) {
            if (results < n.doubleValue()) {
                results = n.doubleValue();
            }

        }
    }

    static class Sum extends AbstractStandard {

        double sum = 0.0;

        @Override
        protected void processValue(Number n) {
            sum += n.doubleValue();
            results = sum;
        }

    }

    static class AbsoluteSum extends Sum {

        @Override
        protected void processValue(Number n) {
            Double d = Double.valueOf(Math.abs(n.doubleValue()));
            super.processValue(d);
        }

    }

    static class Average extends Sum {

        int count = 0;

        @Override
        protected void processValue(Number n) {
            super.processValue(n);
            count++;
            results = sum / count;
        }

    }

    static class AbsoluteAverage extends AbsoluteSum {

        int count = 0;

        @Override
        protected void processValue(Number n) {
            super.processValue(n);

            count++;
            results = sum / count;
        }

    }

}
