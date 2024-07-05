package csi.client.gwt.icon.ui;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

import csi.server.common.model.icons.Icon;

public class IconContainer extends AbsolutePanel {

    private static final String CSI_ICON_BORDER_STYLE = "csi-icon-border";
    private static final String CSI_ICON_HOVER_STYLE = "csi-icon-hover";
    private static final String CSI_ICON_PREVIEW_STYLE = "csi-icon-preview";
    private static final String CSI_ICON_SELECTED_STYLE = "csi-icon-select";
    private static final int ERROR_MARGIN = 3;
    final static int ICON_HEIGHT = 50;
    final static int ICON_WIDTH = 50;

    private int index;
    private String uuid;
    private Image imagePreview;
    private int top;
    private int left;
    private Icon icon;
    private String dataUrl;

    IconContainer(Icon icon, int start) {
        super();
        this.addStyleName(CSI_ICON_PREVIEW_STYLE);
        this.addStyleName(CSI_ICON_HOVER_STYLE);

        dataUrl = icon.getImage();
        imagePreview = new Image();

        uuid = icon.getUuid();

        imagePreview.addStyleName(CSI_ICON_BORDER_STYLE);
        imagePreview.addStyleName(CSI_ICON_PREVIEW_STYLE);
        this.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        this.getElement().getStyle().setPosition(Position.ABSOLUTE);
        this.setWidth(ICON_WIDTH + "px");
        this.setHeight(ICON_HEIGHT + "px");
        imagePreview.getElement().getStyle().setMargin(0, Unit.PX);
        imagePreview.getElement().getStyle().setPadding(0, Unit.PX);
        imagePreview.getElement().getStyle().setBorderWidth(0, Unit.PX);

        setIndex(start);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof IconContainer) {
            return uuid.equals(((IconContainer) object).getIconUuid());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();

    }

    public Image getImagePreview() {
        return imagePreview;
    }

    public boolean hitTest(int x, int y) {

        return x > left + ERROR_MARGIN && x < left + this.getOffsetWidth() + 3 && y > top && y < top + this.getOffsetHeight();

    }

    public void setPosition(int top, int left) {
        this.top = top;
        this.left = left;
        getElement().getStyle().setTop(top, Unit.PX);
        getElement().getStyle().setLeft(left, Unit.PX);
    }

    public int getLeft() {
        return getElement().getOffsetLeft();
    }

    public int getTop() {
        return getElement().getOffsetTop();
    }

    public String getIconUuid() {
        return this.uuid;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setSelected(boolean b) {
        if (b) {
            this.addStyleName(CSI_ICON_SELECTED_STYLE);
        } else {
            this.removeStyleName(CSI_ICON_SELECTED_STYLE);

        }
    }

    public boolean isSelected() {
        return this.getStyleName().contains(CSI_ICON_SELECTED_STYLE);
    }

    void loadImage() {
        final IconContainer widgets = this;
        imagePreview.addLoadHandler(event -> {
            widgets.clear();
            IconUtil.addImage(widgets, imagePreview);
        });
        imagePreview.setUrl(dataUrl);
        widgets.add(imagePreview);
    }

}
