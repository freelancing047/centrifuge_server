package csi.server.connector.jdbc.jstels.schema;

import java.util.ArrayList;
import java.util.List;

public class Table {

    public String name;
    public String file;
    public String path; // root xpath
    public String dateFormat;
    public String namespaces;
    public List<Column> columns = new ArrayList<Column>();
}
