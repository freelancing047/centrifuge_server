package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.server.business.visualization.legend.PlaceLegendItem;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.map.PlaceStyle;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlaceLegendItemProxy extends Composite implements LegendItemProxy {
    private static final int DEFAULT_COLOR = 0;
    private static final int DEFAULT_IMAGE_SIZE = 17;
    protected Button label;
    private PlaceLegendItem item;
    private MapPresenter mapPresenter;
    private boolean isTypenameUnique;
    private Image image;
    private String id;
    private ShapeType shape;
    private int color = DEFAULT_COLOR;
    private double iconRatio = 1.0;
    private int placeId;
    private String placeName;
    private String typeName;

    PlaceLegendItemProxy(PlaceLegendItem item, MapPresenter mapPresenter, boolean isTypenameUnique) {
        this.item = checkNotNull(item, "I cannot make something from nothing.");
        this.mapPresenter = mapPresenter;
        this.isTypenameUnique = isTypenameUnique;
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
        handleImageLoad();
    }

    private void createImage() {
        gatherImageAttributes();
        getImage();
    }

    private void gatherImageAttributes() {
        id = item.iconURI;
        shape = ShapeType.getShape(item.shape);
        color = item.color;
    }

    private void getImage() {
        image = new Image();
        image.getElement().getStyle().setWidth(17, Unit.PX);
        if ((shape == ShapeType.NONE) && (id == null) && !mapPresenter.isUseSummary())
            image.setUrl("img/LegendItem_TEXT.png"); // NON-NLS
        else
            getImageMaybeStyle();
    }

    private void getImageMaybeStyle() {
        PlaceStyle style = mapPresenter.findPlaceStyle(typeName);
        if (style != null && style.getIconScale() != null)
            iconRatio = style.getIconScale();
        int size = DEFAULT_IMAGE_SIZE;
        int strokeSize = 1;
        int summaryStrokeSize = 2;
        if (mapPresenter.isUseSummary())
            mapPresenter.getImage(id, shape, color, size, iconRatio, summaryStrokeSize, image);
        else
            mapPresenter.getImage(id, shape, color, size, iconRatio, strokeSize, image);
    }

    private void handleImageLoad() {
        image.addLoadHandler(event -> {
            image.setHeight("auto");// NON-NLS
            image.setWidth("17px");
        });
    }

    private void addLabelTo(Row row) {
        createLabel();
        row.add(label);
    }

    protected void createLabel() {
        gatherLabelAttributes();
        label = new Button();
        label.addStyleName("legend-item-label");// NON-NLS
        label.setType(ButtonType.LINK);

        if (placeName == null) {
            label.setTitle(typeName);
            HTMLPanel textDiv = new HTMLPanel("");
            textDiv.getElement().setClassName("textDiv");
            textDiv.getElement().setInnerHTML(typeName);
            label.add(textDiv);
        } else {
            if (typeName == null || typeName.isEmpty() || placeName.equals(typeName)) {
                label.setTitle(placeName);
                HTMLPanel textDiv = new HTMLPanel("");
                textDiv.getElement().setClassName("textDiv");
                textDiv.getElement().setInnerHTML(placeName);
                label.add(textDiv);
            } else {
                label.setTitle(typeName + " (" + placeName + ")");
                if (isTypenameUnique) {
                    HTMLPanel textDiv = new HTMLPanel("");
                    textDiv.getElement().setClassName("textDiv");
                    textDiv.getElement().setInnerHTML(typeName);
                    label.add(textDiv);
                }
                else {
                    HTMLPanel textDiv = new HTMLPanel("");
                    textDiv.getElement().setClassName("textDiv");
                    textDiv.getElement().setInnerHTML(typeName + " (" + placeName + ")");
                    label.add(textDiv);
                }
            }
        }
    }

    void gatherLabelAttributes() {
        placeId = item.placeId;
        placeName = item.placeName;
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

    int getPlaceId() {
        return placeId;
    }

    @Override
    public String getImageUrl() {
        if (image != null)
            return image.getUrl();
        return null;
    }

}
