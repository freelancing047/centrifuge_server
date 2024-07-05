package csi.client.gwt.edit_sources.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.column.ColumnDef;


public class FieldColumnMappingColumnCell extends AbstractCell<ColumnDef> {

    interface Template extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static Template template = GWT.create(Template.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, ColumnDef value, SafeHtmlBuilder sb) {
        sb.append(template.html(FieldDefUtils.getColumnDataTypeImage(value).getSafeUri(),
                value.getColumnName()));
    }
}
