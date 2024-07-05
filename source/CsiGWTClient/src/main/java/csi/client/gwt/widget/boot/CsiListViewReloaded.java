package csi.client.gwt.widget.boot;

import com.emitrom.lienzo.client.core.util.Console;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.util.BrowserEvents;
import csi.client.gwt.widget.cells.readonly.DisplayListCell;

public class CsiListViewReloaded extends CsiListView {

    private static ResourceSelectorPanel resourcePanel;

    private static class DisplayListCellReloaded extends DisplayListCell {

        DisplayListCellReloaded() {
            super("click");
        }

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            int width = 100;
            boolean hideTrigger = false;

            if (width == -1) {
                width = 150;
            }

            SafeStyles inputStyles = null;
            String wrapStyles = "";
            String myDynamicStyle = null;

            if (width != -1) {
                wrapStyles += "width:" + width + "px;";

                // 6px margin, 2px border
                width -= 8;

                if (!hideTrigger) {
                    width -= 12;
                }
                if (null != myDynamicStyle) {
                    myDynamicStyle += "width:" + width + "px;";
                } else {
                    myDynamicStyle = "width:" + width + "px;";
                }
            }

            if (0 < myDynamicStyle.length()) {
                inputStyles = SafeStylesUtils.fromTrustedString(myDynamicStyle);
            }

            sb.appendHtmlConstant("<div style='" + wrapStyles + "'>");

            if (!hideTrigger) {
                sb.appendHtmlConstant("<table cellpadding=0 cellspacing=0><tr><td style= 'min-width: 430px'>");
                DisplayListCellReloaded.super.render(context, value, sb);
                sb.appendHtmlConstant("</td>");
                sb.appendHtmlConstant("<td><div class='" + "btn reload-install-table" + "' />Reload</td>");
                sb.appendHtmlConstant("</table>");
            } else {
                DisplayListCellReloaded.super.render(context, value, sb);
            }

            sb.appendHtmlConstant("</div>");
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, Object value, NativeEvent event, ValueUpdater valueUpdater) {
            Console.log("Inside of my onBrowserEvent");
            if("click".equals(event.getType())) {
                EventTarget eventTarget = event.getEventTarget();
                if(Element.as(event.getEventTarget()).hasClassName("btn")) {
                    resourcePanel.getReloadClickHandler().onClick(null);
                }
            }
        }
    }

    public CsiListViewReloaded(ResourceSelectorPanel panel) {
        super(new ListStore<>(new ExtendedInfoKeyProvider<>()), new ExtendedInfoValueProvider(), new DisplayListCellReloaded());
        resourcePanel = panel;
    }
}
