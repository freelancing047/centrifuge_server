package csi.client.gwt.viz.graph.settings.fielddef;

import java.util.List;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;

public class FieldDefGrid extends Composite {
    // this will need to be populated from dataview
    private List<FieldProxy> FIELDPROXIES;
    private Column<FieldProxy, FieldProxy> fieldDefColumn;
    private CellTable<FieldProxy> table;

    public FieldDefGrid(DataModelDef dataModelDef) {
        // logger.log(Level.FINEST, "Starting making FieldGrid");
        FIELDPROXIES = Lists.newArrayList();
        List<FieldDef> fieldDefs = FieldDefUtils.getAllSortedFields(dataModelDef, SortOrder.ALPHABETIC);
        for (FieldDef fieldDef : fieldDefs) {
            String fieldName = fieldDef.getFieldName();

            if (!Strings.isNullOrEmpty(fieldName) && !(null == fieldDef.getValueType())) {
                FIELDPROXIES.add(new FieldProxy(fieldDef, fieldName));
            }
        }

        table = new CellTable<>();
        Cell<FieldProxy> fieldCell = new FieldDefCell();
        fieldDefColumn = new Column<FieldProxy, FieldProxy>(fieldCell) {

            @Override
            public FieldProxy getValue(FieldProxy object) {
                return object;
            }
        };
        table.addColumn(fieldDefColumn);

        // Push the data into the widget.
        table.setRowData(0, FIELDPROXIES);
        table.setBordered(false);
        table.setCondensed(true);
        table.setStriped(true);
        initWidget(table);
    }

    public void filter(String text) {
        List<FieldProxy> filtered = Lists.newArrayList(FIELDPROXIES);
        for (FieldProxy fieldProxy : FIELDPROXIES) {
            if (!fieldProxy.name.toLowerCase().contains(text.toLowerCase())) {
                filtered.remove(fieldProxy);
            }
        }
        table.setRowCount(filtered.size(), true);
        table.setRowData(filtered);
    }

    public void setColumnFieldUpdater(FieldUpdater<FieldProxy, FieldProxy> fieldUpdater) {
        fieldDefColumn.setFieldUpdater(fieldUpdater);

    }

}
