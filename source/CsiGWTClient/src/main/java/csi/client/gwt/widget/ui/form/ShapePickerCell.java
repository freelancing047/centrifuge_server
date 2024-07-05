package csi.client.gwt.widget.ui.form;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.util.ShapeDefUtils;
import csi.server.common.graphics.shapes.ShapeType;

public class ShapePickerCell extends SelectionHandlingCell<String> {
	private Menu shapeMenu = new Menu();
	private ValueUpdater<String> valueUpdater;

	interface CellTemplate extends XTemplates {

		@XTemplate("<span title=\"{name}\"><img width=\"16\" height=\"15\" src=\"{shapeUri}\"/>&nbsp;&nbsp;<span style=\"color:{color}\">{name}</span></span>")
		SafeHtml template(SafeUri shapeUri, String color, String name);

    }
	
	private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);
	
	public ShapePickerCell() {
        super(BrowserEvents.CLICK);
        shapeMenu.setWidth(100);
        MenuItem menuItem = createMenuItem(ShapeType.CIRCLE.toString());
        shapeMenu.add(menuItem);
        menuItem = createMenuItem(ShapeType.SQUARE.toString());
        shapeMenu.add(menuItem);
        menuItem = createMenuItem(ShapeType.DIAMOND.toString());
        shapeMenu.add(menuItem);
        shapeMenu.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				String shape = event.getSelectedItem().getItemId();
				valueUpdater.update(shape);
				shapeMenu.hide();
			}
        	
        });
    }

	private MenuItem createMenuItem(String text) {
		MenuItem menuItem = new MenuItem();
		menuItem.setText(text);
        ImageResource imageResource = ShapeDefUtils.getShapeImage(text);
        menuItem.setIcon(imageResource);
        menuItem.setItemId(text);
        return menuItem;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
		SafeUri shapeUri = ShapeDefUtils.getShapeImage(value).getSafeUri();
		sb.append(cellTemplate.template(shapeUri, "#000000", value));
	}

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        if (BrowserEvents.CLICK.equals(event.getType())) {
            this.valueUpdater = valueUpdater;
//            shapeMenu.setColor(colorWithoutHash(value));
            shapeMenu.show(parent, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT, true));
        } else {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
    }
}
