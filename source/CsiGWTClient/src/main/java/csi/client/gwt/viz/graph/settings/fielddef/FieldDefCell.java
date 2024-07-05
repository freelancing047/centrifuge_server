package csi.client.gwt.viz.graph.settings.fielddef;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.ImageResourceRenderer;

import csi.client.gwt.util.FieldDefUtils;

public class FieldDefCell extends AbstractCell<FieldProxy> {

    interface Templates extends SafeHtmlTemplates {

        @Template("<div class=\"graph-field-row\">{0}</div>")
        SafeHtml cell(SafeHtml value);
    }

    private static Templates templates = GWT.create(Templates.class);
    private static ImageResourceRenderer imageRenderer;
    private Cell<String> buttonCell;

    public FieldDefCell() {

        super(CLICK);
        if (imageRenderer == null) {
            imageRenderer = new ImageResourceRenderer();
        }
        if (buttonCell == null) {
            buttonCell = new ButtonCell(IconType.ARROW_RIGHT, ButtonType.INFO, ButtonSize.MINI) {

                @Override
                public void onBrowserEvent(Context context, Element parent,
                        String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
                }
            };
        }
    }

    @Override
    public void render(final Context context, final FieldProxy value, SafeHtmlBuilder sb) {
        if (value == null) {
            return;
        }
        SafeHtml inside = new SafeHtml() {

            @Override
            public String asString() {
                String safe = "";
                safe = imageRenderer.render(FieldDefUtils.getFieldTypeImage(value.getFieldDef().getFieldType()))
                        .asString();
                safe += imageRenderer.render(
                        FieldDefUtils.getDataTypeImage(value.getFieldDef().getValueType()))
                        .asString();
                safe += value.name;
                SafeHtmlBuilder sb2 = new SafeHtmlBuilder();
                buttonCell.render(context, "", sb2);
                safe += sb2.toSafeHtml().asString();
                return safe;
            }
        };
        SafeHtml rendered = templates.cell(inside);
        sb.append(rendered);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, FieldProxy value, NativeEvent event,
            ValueUpdater<FieldProxy> valueUpdater) {
        if (CLICK.equals(event.getType())) {
            valueUpdater.update(value);
/*            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            if (Element.as(eventTarget).getNodeName().equals("I")//NON-NLS
                    || Element.as(eventTarget).getNodeName().equals("BUTTON")) {//NON-NLS
                valueUpdater.update(value);
            }*/
        }
    }
}
