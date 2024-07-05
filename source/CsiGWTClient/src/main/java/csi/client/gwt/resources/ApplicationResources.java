package csi.client.gwt.resources;

import com.github.gwtbootstrap.client.ui.resources.Resources;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.resources.client.TextResource;

public interface ApplicationResources extends Resources {

    @Source("css/CentrifugeStyles.css")
    ApplicationStyles style();

    @Source("ico_3.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource barChartIcon();

    // these files are empty since we're using LESS
    @Source("css/bootstrap.min.css")
    TextResource boostrapCss();

    @Source("css/bootstrap-responsive.min.css")
    TextResource bootstrapResponsiveCss();

    @Source("js/bootstrap.fix.js")
    // Refer to http://stackoverflow.com/questions/13649459/twitter-bootstrap-multiple-modal-error
    TextResource bootstrapModalFix();

    // Highchart javascript dependencies
    @Source("js/highcharts-4.1.5.js")
    TextResource highchartsCore();

    @Source("js/highcharts-4.1.5more.js")
    TextResource highchartsMore();

    @Source("js/highcharts-exporting-2.3.5.js")
    TextResource highchartsExport();

    // Used by tag-it
    @Source("js/jquery-ui.min-1.10.3.js")
    TextResource jqueryUi();
    
    @Source("js/tag-it.min.js")
    TextResource tagIt();
    
    @Source("ico_8.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource areaChartIcon();

    @Source("ico_5.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource lineChartIcon();

    @Source("ico_4.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource pieChartIcon();

    @Source("ico_7.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource bubbleChartIcon();

    @Source("edit.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource editIcon();

    @Source("ico_delete.jpg")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource deleteIcon();

    @Source("ico_move.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource moveIcon();

    @Source("loading.gif")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource loadingSpinner();

    @Source("ipixel.gif")
    ImageResource invisiblePixel();

    @Source("gray_mask.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource grayMask();

    @Source("slider_handle.png")
    ImageResource sliderHandle();

    @Source("ico_rel_graph.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource graphSelectionIcon();

    @Source("Glasses.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource readAccess();

    @Source("Pencil.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource writeAccess();

    @Source("RecycleBin.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource deleteAccess();

    @Source("OpenLock.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource access();

    @Source("Briefcase.png")
    @ImageOptions(repeatStyle = RepeatStyle.None)
    ImageResource ownership();

    @Source("logo.png")
    ImageResource logo();

    @Source("new_logo.png")
    ImageResource new_logo();
}
