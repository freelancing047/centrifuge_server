package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.server.business.visualization.legend.TrackLegendItem;
import csi.server.common.graphics.shapes.ShapeType;

import static com.google.common.base.Preconditions.checkNotNull;

public class TrackLegendItemProxy extends Composite implements LegendItemProxy {
    private static final ShapeType SHAPE_TYPE = ShapeType.LINE;
    private static final int DEFAULT_COLOR = 0;
    private static final int DEFAULT_IMAGE_SIZE = 17;

    private TrackLegendItem item;

    private Image image;

    private int color = DEFAULT_COLOR;
    private int size = DEFAULT_IMAGE_SIZE;

    private Button label;
    private int trackId;
    private String typeName;

    TrackLegendItemProxy(TrackLegendItem item) {
        this.item = checkNotNull(item, "I cannot make something from nothing.");
        Row row = createRow();
        addImageTo(row);
        addLabelTo(row);
        initWidget(row);
    }

    private Row createRow() {
        Row row = new Row();
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        return row;
    }

    private void addImageTo(Row row) {
        createImage();
        row.add(image.asWidget());
    }

    private void createImage() {
        gatherImageAttributes();
        getImage();
    }

    private void gatherImageAttributes() {
        color = item.color;
    }

    public void getImage() {
        image = new Image();
        GraphImpl.getRenderedIcon("", SHAPE_TYPE, color, size, 1.0, image);
        image.setHeight("auto");// NON-NLS
        image.getElement().getStyle().setWidth(17, Unit.PX);
        image.asWidget().addStyleName("legend-item-image");
    }

    private void addLabelTo(Row row) {
        createLabel();
        row.add(label);
    }

    private void createLabel() {
        gatherLabelAttributes();
        label = new Button();
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);
        label.setTitle(typeName);
        HTMLPanel textDiv = new HTMLPanel("");
        textDiv.getElement().setClassName("textDiv");
        textDiv.getElement().setInnerHTML(typeName);
        label.add(textDiv);
    }

    private void gatherLabelAttributes() {
        trackId = item.trackId;
        typeName = item.typeName;
    }

    @Override
    public String getKey() {
        return item.key;
    }

    @Override
    public String getType() {
        return typeName;
    }

    int getTrackId() {
        return trackId;
    }

    @Override
    public String getImageUrl() {
        if (image != null) {
            return image.getUrl();
        }
        return null;
    }
}
