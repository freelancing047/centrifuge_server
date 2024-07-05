package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.server.business.visualization.legend.AssociationLegendItem;
import csi.server.common.graphics.shapes.ShapeType;

public class AssociationLegendItemProxy extends Composite implements LegendItemProxy {
	private static final ShapeType SHAPE_TYPE = ShapeType.LINE;
    private static final int DEFAULT_COLOR = 0;
    private static final int DEFAULT_IMAGE_SIZE = 17;
	
	private AssociationLegendItem item;
	private String typeName;
	private Image image;
    private int color = DEFAULT_COLOR;
    private Button label;
    private int size = DEFAULT_IMAGE_SIZE;

	public AssociationLegendItemProxy(AssociationLegendItem item) {
		this.item = item;
		
		color = (int) item.color;
		
		Row row = new Row();
		row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
		typeName = item.typeName;
        getImage();
        row.add(image.asWidget());
        image.asWidget().addStyleName("legend-item-image");
        label = new Button();
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);
        label.setText(item.typeName);
        row.add(label);
        initWidget(row);
	}
	
	
	public Image getImage() {
		image = new Image();
		GraphImpl.getRenderedIcon("", SHAPE_TYPE, color, size, 1.0, image);
		image.setHeight("auto");// NON-NLS
		image.setWidth("18px");// NON-NLS
		return image;
	}

	@Override
	public String getKey() {
		return item.key;
	}
	
	@Override
    public String getType() {
        return typeName;
    }


    @Override
    public String getImageUrl() {
        if(image != null){
            return image.getUrl();
        }
        return null;
    }
}
