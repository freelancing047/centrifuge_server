package csi.server.connector.jdbc.jstels.schema;

import java.util.Comparator;

public class Column {

    public static final Comparator<? super Column> ORDER_BY_ORDINAL = new Comparator<Column>() {

        @Override
        public int compare(Column o1, Column o2) {
            return o1.ordinal - o2.ordinal;
        }

    };
    public String name;
    public String type;
    public String path; // xpath to field
    public int ordinal;
}
