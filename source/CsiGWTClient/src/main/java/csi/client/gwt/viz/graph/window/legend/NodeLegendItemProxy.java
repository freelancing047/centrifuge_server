package csi.client.gwt.viz.graph.window.legend;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.GraphConstants;

public class NodeLegendItemProxy extends Composite implements LegendItemProxy {

    private static final int DEFAULT_IMAGE_SIZE = 30;
    private static final int DEFAULT_COLOR = 0;
    private static final ShapeType DEFAULT_SHAPE = ShapeType.NONE;
    
    
    protected Image image;
    protected int color = DEFAULT_COLOR;
    protected ShapeType shape;
    protected String iconId;
    protected Button label;
    // only the size back from server??
    protected int size = DEFAULT_IMAGE_SIZE;

    protected GraphNodeLegendItem item;
    protected Graph graph;
    protected final String typeName;

    public NodeLegendItemProxy(Graph graph, GraphNodeLegendItem item) {
        this.graph = checkNotNull(graph);
        this.item = checkNotNull(item, "I cannot make something from nothing.");//NON-NLS asserts are not enabled in production.
        if (item.typeName != null) {
            color = (int) item.color;
            iconId = item.iconId;
            shape = item.shape;
            typeName = item.typeName;
        }
        else{
            color = (int) item.color;
            iconId = item.iconId;
            shape = item.shape;
            typeName = item.typeName;
        }
        Row row = new Row();
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        getImage();

        image.getElement().getStyle().setWidth(17, Unit.PX);
        row.add(image.asWidget());
        image.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                image.setHeight("auto");//NON-NLS
                image.setWidth("17px");
            }
        });
        // TODO: flex port delete, previously implemented as
        // nodeItem.label = entry.typeName + " (" + entry.count + " of " + entry.totalCount + ") ";
        label = new Button();
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);

        HTMLPanel textDiv = new HTMLPanel("");
        textDiv.getElement().setClassName("textDiv");
        textDiv.getElement().setInnerHTML(typeName);
        label.add(textDiv);
        row.add(label);

        initWidget(row);

    }

    /**
     * This method maps the strings that come back from the service the the enum needed for the next call
     * 
     * @param shapeString
     * @return
     */
    // FIXME: this is doing it wrong... Why is it a String to start with?
    private static ShapeType getShape(String shapeString) {
        switch (shapeString) {
            case "Circle"://NON-NLS
                return ShapeType.CIRCLE;
            case "Triangle"://NON-NLS
                return ShapeType.TRIANGLE;
            case "Square"://NON-NLS
                return ShapeType.SQUARE;
            case "Diamond"://NON-NLS
                return ShapeType.DIAMOND;
            case "Star"://NON-NLS
                return ShapeType.STAR;
            case "Pentagon/House"://NON-NLS
                return ShapeType.HOUSE;
            case "Pentagon"://NON-NLS
                return ShapeType.PENTAGON;
            case "Octagon"://NON-NLS
                return ShapeType.OCTAGON;
            case "Rectangle"://NON-NLS
                return ShapeType.RECTANGLE;
            case "Hexagon"://NON-NLS
                return ShapeType.HEXAGON;
            case "None"://NON-NLS
                return ShapeType.NONE;
            default:
                return DEFAULT_SHAPE;
        }
    }

    public Image getImage() {
        return getImage(size);
    }

    public Image getImage(final int imageSize) {
        final Image tempImage = new Image();
        this.image = tempImage;
        if((shape == ShapeType.NONE) && (iconId == null)) {
            image.setUrl("img/LegendItem_TEXT.png");//NON-NLS
        } else {
            getImageMaybeStyle(imageSize);
        }
        return image;
    }
    
    
    public void getImageMaybeStyle(int imageSize) {
        NodeStyle style = graph.findNodeStyle(typeName);
        if(style != null && style.getIconScale() != null){
            GraphImpl.getRenderedIcon(iconId, shape, color, imageSize, style.getIconScale(), image);
        } else {
            GraphImpl.getRenderedIcon(iconId, shape, color, imageSize, 1.0, image);
        }
    }
    
    @Override
    public String getKey() {
        return ObjectAttributes.NODES_OBJECT_TYPE + item.key;
    }
    
    @Override
    public String getType() {
        return item.key;
    }

    @Override
    public String getImageUrl() {
        if(image != null){
            return image.getUrl();
        }
        return null;
    }
}
