package csi.client.gwt.viz.graph.window.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.service.api.ThemeActionsServiceProtocol;

public class LinkLegendItemProxy extends Composite implements LegendItemProxy {


    private static final int DEFAULT_IMAGE_SIZE = 20;
    private static final int DEFAULT_COLOR = 0;
    private static final ShapeType SHAPE_TYPE = ShapeType.LINE;
    

    private Image image;
    private int color = DEFAULT_COLOR;
    private Button label;
    private int size = DEFAULT_IMAGE_SIZE;

    private final GraphLegend legend;
    private final GraphLinkLegendItem item;

    public LinkLegendItemProxy(GraphLegend legend, GraphLinkLegendItem item) {
        this.legend = legend;
        this.item = item;

        //TODO: need null gaurd?
        color = (int) item.color;

        Row row = new Row();
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        getImage();
        row.add(image.asWidget());
        image.asWidget().addStyleName("legend-item-image");
        // TODO: flex port delete, previously implemented as
        // nodeItem.label = entry.typeName + " (" + entry.count + " of " + entry.totalCount + ") ";
        label = new Button();
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);
        HTMLPanel textDiv = new HTMLPanel("");
        textDiv.getElement().setClassName("textDiv");
        textDiv.getElement().setInnerHTML(item.typeName);
        label.add(textDiv);
        row.add(label);
        initWidget(row);
    }

    public Image getImage() {
        image = new Image();
        String themeUuid = legend.getGraph().getModel().getTheme();
        
        VortexFuture<GraphTheme> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {
            @Override
            public void onSuccess(GraphTheme result) {
                GraphImpl.getRenderedIcon("", SHAPE_TYPE, color, size, 1.0, image);
                image.setHeight("auto");//NON-NLS
                image.setWidth("18px");//NON-NLS
            }
        });

        future.execute(ThemeActionsServiceProtocol.class).findGraphTheme(themeUuid);
        return image;
    }

    public String getKey() {
        return ObjectAttributes.EDGES_OBJECT_TYPE + item.key;
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
