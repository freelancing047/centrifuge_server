package csi.server.common.codec.xstream.converter;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;

/**
 * Provides the converter implementation for Prefuse Tables.
 * <p>
 * This currently only serializes the table with a wire format of:
 *
 * <pre>
 *
 * &lt;tableAlias&gt;
 *  &lt;meta&gt;
 *      &lt;rowCount&gt;
 *      [
 *
 *          &lt;dimension&gt;
 *              &lt;label&gt;
 *              &lt;type&gt;
 *              &lt;count&gt;
 *              &lt;ranges&gt;
 *                  &lt;item&gt;*
 *      ]+
 *  &lt;data&gt;
 *      &lt;row&gt;
 *          &lt;col&gt;+
 *
 * </pre>
 *
 * The XML template indicates that one or more dimension(s) exist. At run-time a
 * row contains <i>n</i> col elements where each one corresponds to a dimension
 * defined in the meta element; such that the first column represents the first
 * dimension's value etc.
 *
 * @author Tildenwoods
 *
 */
public class PrefuseTableConverter extends AbstractCollectionConverter {


    public PrefuseTableConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
       return ((type != null) && type.isAssignableFrom(Table.class));
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Table data = (Table) source;
        Schema schema = data.getSchema();
        int columnCount = schema.getColumnCount();

        writer.startNode("meta");

        int rowCount = data.getRowCount();

        {
            writer.startNode("rowCount");
            writer.setValue(Integer.toString(rowCount));
            writer.endNode();
        }

        for (int i = 0; i < columnCount; i++) {
            writeDimension(writer, context, data, i);
        }

        writer.endNode();
        writer.startNode("data");

        for (int i = 0; i < rowCount; i++) {

            Tuple tuple = data.getTuple(i);
            writer.startNode("row");
            for (int col = 0; col < columnCount; col++) {
                writer.startNode("col");
                String val = tuple.getString(col);
                writer.setValue((val == null) ? "" : val);
                writer.endNode();
            }

            writer.endNode();
        }

        writer.endNode();
    }

    /**
     * Writes out the meta data for a given column in the table.
     * <p>
     * This includes the name, type, and the set of values contained in the
     * specified column.
     * <p>
     *
     * @param writer
     * @param context
     * @param table
     * @param columnIndex
     */
    @SuppressWarnings("unchecked")
    private void writeDimension(HierarchicalStreamWriter writer, MarshallingContext context, Table table, int columnIndex) {
        Schema schema = table.getSchema();
        writer.startNode("dimension");

        // NB: use of code blocks to group together node-start, value, node-end
        {
            writer.startNode("label");
            writer.setValue(schema.getColumnName(columnIndex));
            writer.endNode();
        }

        {
            writer.startNode("type");
            // TODO: could have better mapping from local, native type to
            // what we serialize out---uses the codec's wire types
            writer.setValue(mapper().serializedClass(schema.getColumnType(columnIndex)));
            writer.endNode();
        }

        // ... and the set of possible values
        {
            Column column = table.getColumn(columnIndex);
            Set<Object> values = new HashSet<Object>();
            for (int row = 0; row < column.getRowCount(); row++) {
                values.add(column.get(row));
            }

            // report total unique values.
            {
                int uniqueValues = values.size();
                writer.startNode("count");
                writer.setValue(Integer.toString(uniqueValues));
                writer.endNode();
            }

            writer.startNode("ranges");
            context.convertAnother(values);
            writer.endNode();
        }

        writer.endNode();
    }

    /*
     * Currently doesn't do anything...we aren't expecting a table to be posted
     * in.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Table data = new Table();

        if (reader.hasMoreChildren()) {
            reader.moveDown();

        }
        return data;
    }

}
