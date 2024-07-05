package csi.client.gwt.worksheet.tab.events;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class TabColorEventHandler extends BaseCsiEventHandler {

    public abstract void onColor(Widget tabContentWidget);
}
