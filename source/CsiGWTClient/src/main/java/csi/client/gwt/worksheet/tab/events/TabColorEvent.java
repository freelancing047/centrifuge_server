package csi.client.gwt.worksheet.tab.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEvent;


public class TabColorEvent extends BaseCsiEvent<TabColorEventHandler> {
    private Widget tabContentWidget;

    public static final GwtEvent.Type<TabColorEventHandler> type = new GwtEvent.Type<TabColorEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TabColorEventHandler> getAssociatedType() {
        return type;
    }

    public void setTabContentWidget(Widget tabContentWidget) {
        this.tabContentWidget = tabContentWidget;
    }

    @Override
    protected void dispatch(TabColorEventHandler handler) { handler.onColor(tabContentWidget); }

}

