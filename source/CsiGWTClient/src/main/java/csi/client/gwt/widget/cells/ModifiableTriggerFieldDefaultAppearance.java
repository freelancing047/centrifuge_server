package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.FieldCell.FieldAppearanceOptions;
import com.sencha.gxt.theme.base.client.field.TriggerFieldDefaultAppearance;


public class ModifiableTriggerFieldDefaultAppearance extends TriggerFieldDefaultAppearance {
    
    GridCellAssist _styleProvider = null;

    public ModifiableTriggerFieldDefaultAppearance(GridCellAssist styleProviderIn) {
        _styleProvider = styleProviderIn;
    }

    public ModifiableTriggerFieldDefaultAppearance(GridCellDataAssist styleProviderIn) {
        _styleProvider = (GridCellAssist)styleProviderIn;
    }

    public void render(Context contextIn, SafeHtmlBuilder sb, String value, FieldAppearanceOptions options) {
      int width = options.getWidth();
      boolean hideTrigger = options.isHideTrigger();

      if (width == -1) {
        width = 150;
      }

      SafeStyles inputStyles = null;
      String wrapStyles = "";
      String myDynamicStyle = (null != _styleProvider) ? _styleProvider.getStyle(contextIn.getIndex(), contextIn.getColumn()) : null;

      if (width != -1) {
        wrapStyles += "width:" + width + "px;";

        // 6px margin, 2px border
        width -= 8;

        if (!hideTrigger) {
          width -= getResources().triggerArrow().getWidth();
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

      sb.appendHtmlConstant("<div style='" + wrapStyles + "' class='" + getStyle().wrap() + "'>");

      if (!hideTrigger) {
        sb.appendHtmlConstant("<table cellpadding=0 cellspacing=0><tr><td>");
        renderInput(sb, value, inputStyles, options);
        sb.appendHtmlConstant("</td>");
        sb.appendHtmlConstant("<td><div class='" + getStyle().trigger() + "' /></td>");
        sb.appendHtmlConstant("</table>");
      } else {
        renderInput(sb, value, inputStyles, options);
      }

      sb.appendHtmlConstant("</div>");
    }

    /*
    public void render(Context contextIn, SafeHtmlBuilder sb, String value, FieldAppearanceOptions options) {
      int width = options.getWidth();
      boolean hideTrigger = options.isHideTrigger();

      if (width == -1) {
        width = 150;
      }

      SafeStyles inputStyles = null;
      String wrapStyles = "";
      String inputStyleString = "";

      if (width != -1) {
        wrapStyles += "width:" + width + "px;";

        // 6px margin, 2px border
        width -= 8;

        if (!hideTrigger) {
          width -= getResources().triggerArrow().getWidth();
        }
        inputStyleString = "width:" + width + "px;";
      }
      
      if (null != _styleProvider) {
          String myStyle = _styleProvider.getStyle(contextIn.getIndex(), contextIn.getColumn());
          if ((null != myStyle) && (0 < myStyle.length())) {
              inputStyleString += myStyle;
          }
      }
      
      if (0 < inputStyleString.length()) {
          SafeStylesUtils.fromTrustedString(inputStyleString);
      }

      sb.appendHtmlConstant("<div style='" + wrapStyles + "' class='" + getStyle().wrap() + "'>");

      if (!hideTrigger) {
        sb.appendHtmlConstant("<table cellpadding=0 cellspacing=0><tr><td>");
        renderInput(sb, value, inputStyles, options);
        sb.appendHtmlConstant("</td>");
        sb.appendHtmlConstant("<td><div class='" + getStyle().trigger() + "' /></td>");
        sb.appendHtmlConstant("</table>");
      } else {
        renderInput(sb, value, inputStyles, options);
      }

      sb.appendHtmlConstant("</div>");
    }
    */
}
