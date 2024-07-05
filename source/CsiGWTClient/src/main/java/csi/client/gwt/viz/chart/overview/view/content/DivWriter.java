package csi.client.gwt.viz.chart.overview.view.content;

import csi.client.gwt.viz.chart.overview.view.OverviewView;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DivWriter {

    private DivWriter(){}

    public static String writeDiv(String color, int width, boolean borderLeft, boolean borderRight){
        StringBuilder builder = new StringBuilder("<div style=\"");
        builder.append("float:left;");
        builder.append("border-top:1px solid black;");
        builder.append("border-bottom:1px solid black;");

        if(borderLeft){
            builder.append("border-left:1px solid black;");
            width --;
        }
        if(borderRight){
            builder.append("border-right:1px solid black;");
            width --;
        }

        builder.append("margin-top:").append(((OverviewView.OVERVIEW_HEIGHT - ColoredOverview.OVERVIEW_CONTENT_HEIGHT) / 2) - 1).append("px;");
        builder.append("height:").append(ColoredOverview.OVERVIEW_CONTENT_HEIGHT).append("px;");

        builder.append("width:").append(width).append("px;");
        if(color != null){
            builder.append("background-color:").append(color).append(";");
        }

        builder.append("\"></div>");
        return builder.toString();
    }

}

