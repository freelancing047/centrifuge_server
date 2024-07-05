package csi.client.gwt.viz.graph.plunk.edit.widgets;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Row;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import csi.client.gwt.viz.graph.node.settings.appearance.NodeShape;
import csi.server.common.graphics.shapes.ShapeType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ShapePicker extends Composite implements TakesValue<ShapeType>{

    private Row mainPanel = new Row();
    private HTMLPanel shapePanel = new HTMLPanel("");
    private SimplePanel simplePanel = new SimplePanel();
    private ShapeType shapeType;
    private CheckBox checkBox = new CheckBox();
    private boolean shapeEnabled = false;

    public ShapePicker(){
        buildUI();
        initWidget(mainPanel);

        styleUi();
        initShapePanel(ShapeType.values());
        enableShapePicker();
    }

    private void styleUi() {
        simplePanel.getElement().getStyle().setPaddingTop(5, Unit.PX);
        simplePanel.getElement().getStyle().setPaddingRight(5, Unit.PX);
    }

    @Override
    public void setValue(ShapeType value) {
        if(value == null){
            checkBox.setValue(false, true);
        } else {
            if(value != ShapeType.NONE){
                shapeEnabled = true;
                checkBox.setValue(true);
                enableShapePicker();
            }

            setShape(value);
        }
    }

    @Override
    public ShapeType getValue() {
        if(shapeEnabled)
            return shapeType;
        return null;
    }

    private void buildUI() {
        Column pickerColumn = new Column(4);

        Column checkBoxColumn = new Column(1);
        checkBoxColumn.add(checkBox);
        mainPanel.add(checkBoxColumn);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(simplePanel);
        hPanel.add(shapePanel);
        pickerColumn.add(hPanel);
        
        mainPanel.add(pickerColumn);
    }

    private void clearShapeDisplay() {
        simplePanel.setWidget(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));//NON-NLS
        shapeType = null;
        checkBox.setValue(false, true);
    }

    private void initShapePanel(ShapeType[] shapes) {
        shapePanel.addStyleName("node-edit-shape-panel");//NON-NLS
        clearShapeDisplay();

        for (ShapeType shape : shapes) {
            addShapeToPanel(shape);
        }

        
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                    shapeEnabled = event.getValue();
                    enableShapePicker();
                    if(!shapeEnabled)
                        clearShapeDisplay();
            }
        });
    }

    private void enableShapePicker() {
        if (shapeEnabled) {
            shapePanel.removeStyleName("overlay");//NON-NLS
            shapePanel.getElement().getStyle().setProperty("opacity", "1");//NON-NLS
        } else {
            shapePanel.addStyleName("overlay");//NON-NLS
            shapePanel.getElement().getStyle().setProperty("opacity", ".5");//NON-NLS
        }
    }

    private void addShapeToPanel(ShapeType shape) {
        ImageResource imageResource = NodeShape.imageFromShapeType(shape);
        if(imageResource == null)
            return;

        Image image = createShapeImage(shape, imageResource);
        shapePanel.add(image);
    }

    private Image createShapeImage(final ShapeType shape, ImageResource imageResource) {
        Image image = new Image(imageResource);
        image.setVisibleRect(-4, 0, 16, 16);
        image.getElement().getStyle().setMarginBottom(2, Style.Unit.PX);
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(shapeEnabled) {
                    setShape(shape);
                }
            }
        });
        return image;
    }

    private void setShape(ShapeType shape) {
        this.shapeType = shape;

        if(shape == null){
            return;
        }
        ImageResource imageResource = NodeShape.imageFromShapeType(shape);
        if(imageResource == null)
            return;

        Image image = new Image(imageResource);
        simplePanel.setWidget(image);
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearShapeDisplay();
            }
        });
    }

}
