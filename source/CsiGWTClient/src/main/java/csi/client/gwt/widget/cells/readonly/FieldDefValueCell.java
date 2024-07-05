package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;

import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.util.ValuePair;

public class FieldDefValueCell extends AbstractCell<ValuePair<Boolean, FieldDef>> {

	interface FieldTemplate extends SafeHtmlTemplates {

        @Template("<span title=\"{3}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{0}\"/>&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{1}\"/>&nbsp;&nbsp;<span style=\"color:{2}\">{3}</span></span>")
        SafeHtml display(SafeUri fieldUri, SafeUri dataUri, String color, String name);

    }

    interface EmptyTemplate extends SafeHtmlTemplates {

        @Template("<span style=\"color:{0}; text-align:center\">{1}</span>")
        SafeHtml display(String color, String name);

    }

    private static final FieldTemplate fieldTemplate = GWT.create(FieldTemplate.class);
    private static final EmptyTemplate emptyTemplate = GWT.create(EmptyTemplate.class);

    private String _defaultText = "";
    private String _defaultColor = "";

    public FieldDefValueCell() {
        super();
    }

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context contextIn, ValuePair<Boolean, FieldDef> itemIn, SafeHtmlBuilder builderIn) {
		String myColor = _defaultColor;
        String myText = _defaultText;

    	if (itemIn.getValue1()) {
            builderIn.append(emptyTemplate.display(myColor, "Selection"));
    	} else {
            if (itemIn.getValue2() != null) {
	            FieldType myFieldType = itemIn.getValue2().getFieldType();
	            CsiDataType myDataType = itemIn.getValue2().getValueType();
	            if ((null != myFieldType) && (null != myDataType)) {
	                SafeUri myFieldUri = FieldDefUtils.getFieldTypeImage(myFieldType).getSafeUri();
	                SafeUri myDataUri = FieldDefUtils.getDataTypeImage(myDataType).getSafeUri();

	                myColor = myFieldType.getColor();
	                myText = itemIn.getValue2().getFieldName();

	                builderIn.append(fieldTemplate.display(myFieldUri, myDataUri, myColor, myText));
	            } else {
	                builderIn.append(emptyTemplate.display(myColor, myText));
	            }
            } else {
                builderIn.append(emptyTemplate.display(myColor, myText));
            }
    	}
	}

}
