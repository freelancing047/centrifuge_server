package csi.client.gwt.icon;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.icon.ui.IconContainer;

public class IconSelectionEvent extends BaseCsiEvent<IconSelectionHandler> {
    public static final GwtEvent.Type<IconSelectionHandler> type = new GwtEvent.Type<IconSelectionHandler>();
    
    private IconContainer iconContainer;
    public IconSelectionEvent(IconContainer iconContainer) {
        this.iconContainer = iconContainer;
    }

    @Override
    public GwtEvent.Type<IconSelectionHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(IconSelectionHandler handler) {
        handler.onSelect(this);
    }
    
    public String getIconUrl(){
        return iconContainer.getImagePreview().getUrl();
    }
    
    public String getIconUuid(){
        return iconContainer.getIconUuid();
    }

}
