package csi.server.common.model.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import csi.server.common.model.OrderedField;

/**
 * @author Centrifuge Systems, Inc.
 */
public class FieldSorter {

    private static final Comparator<? super OrderedField> COMPARE_BY_ORDINAL = new Comparator<OrderedField>() {
        @Override
        public int compare(OrderedField o1, OrderedField o2) {
            return (o1.getOrdinal() - o2.getOrdinal());
        }
    };

    public static List<OrderedField> getSortedFields(List<OrderedField> fields){
        List<OrderedField> sorted = new ArrayList<OrderedField>();
        sorted.addAll(fields);

        Collections.sort(sorted, COMPARE_BY_ORDINAL);
        return sorted;
    }
}
