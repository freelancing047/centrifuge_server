<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="csi.config.Configuration"%>
<%@ page import="csi.config.RelGraphConfig"%>
<%@ page import="csi.config.UiSortConfig"%>
<%@page import="csi.config.AppLabelConfig"%>
<%@ page import = "csi.server.common.publishing.Asset"%>
<%
 String assetId  = (String) request.getAttribute(Asset.ASSET_ID);
 if (assetId == null)
 {
     assetId = request.getParameter(Asset.ASSET_ID);
 }
 String dataViewId  = (String) request.getAttribute(Asset.DATA_VIEW_ID);
 if (dataViewId == null)
 {
     dataViewId = request.getParameter(Asset.DATA_VIEW_ID);
 }
 
 String flashVars = "assetId=" + assetId + "&dataViewId=" + dataViewId + "&exportToANX=false"; 
 
 RelGraphConfig rgConfig = Configuration.instance().getGraphConfig();
 flashVars += "&graphRenderThreshold=" + rgConfig.getRenderThreshold();
 flashVars += "&relgraphTooltipOpenDelayTime=" + rgConfig.getTooltipOpenDelayTime(); 

 UiSortConfig uiSortConfig = Configuration.instance().getUiSortConfig();
 flashVars += "&sortFieldsAlphabetically="
         + uiSortConfig.isSortFieldsAlphabetically();
 
 AppLabelConfig appLabelConfig = Configuration.instance().getAppLabelConfig();
 String  headerLeftLabelTxt    = appLabelConfig.getHeaderLeftLabel();
 String  headerCenterLabelTxt  = appLabelConfig.getHeaderCenterLabel();
 String  headerRightLabelTxt   = appLabelConfig.getHeaderRightLabel();
 String  headerBackgroundColor = appLabelConfig.getHeaderBackgroundColor(); 
 Boolean includeHeaderLabels   = appLabelConfig.getIncludeHeaderLabels();
 
%>

<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Centrifuge Server</title>

<script src="../javascript/jquery.js" language="javascript"></script>
<script src="../javascript/jquery-uuid.js" language="javascript"></script>
<script src="../javascript/jquery.timers.js" language="javascript"></script>
<script src="../javascript/comet.js" language="javascript"></script>
<script src="/Centrifuge/flex/AC_OETags.js" language="javascript"></script>

<style>
body { margin: 0px; overflow:hidden }
</style>
<script type="text/javascript">
 <!--
// -----------------------------------------------------------------------------
// Globals
// Major version of Flash required
var requiredMajorVersion = 9;
// Minor version of Flash required
var requiredMinorVersion = 0;
// Minor version of Flash required
var requiredRevision = 0;
// -----------------------------------------------------------------------------

//
// JavaScript handler for mouse wheel.
// As reported in CTWO-380, the default browser action of
// when CTRL-wheel is used to shrink the image (zoom out) messes up the display.
// By returning "false" to the body's "onmouswheel" attribute, this handler
// prevents the default browser behavior.
//
function getWheel() {
    if(window.event.ctrlKey) {
        return false;
    }  else {
        return true;
    }
 }
 
 function changeTitle(title) {
     document.title = title;
 }
 
 $(document).ready(function () {
	    var includeHeaderLabels = <%=includeHeaderLabels%>;
	    if(includeHeaderLabels) {
	       $.getScript('../javascript/csi.config.js', function(){
	            csi.config.showConfigurableLabels("<%=headerLeftLabelTxt%>", "<%=headerRightLabelTxt%>", "<%=headerCenterLabelTxt%>", "<%=headerBackgroundColor%>");
	       });
	    }
	});
 
 -->
 </script>

</head>

<body onmousewheel="return getWheel();" scroll="no" >

<div id="labelsContainer">
    <span id="headerLeftLabel"></span>
    <span id="headerCenterLabel"></span>
    <span id="headerRightLabel"></span>
</div>

<script type="text/javascript">
<!--
// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
var hasProductInstall = DetectFlashVer(6, 0, 65);

// Version check based upon the values defined in globals
var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

var alternateContent = 'Centrifuge Server requires Flash Player version 9 or higher. '
    + '<a href=http://www.adobe.com/go/getflash/>Get Flash Here.</a>';

// Check to see if a player with Flash Product Install is available and the version does not meet the requirements for playback
if ( hasProductInstall && !hasRequestedVersion ) {
    // MMdoctitle is the stored document.title value used by the installation process to close the window that started the process
    // This is necessary in order to close browser windows that are still utilizing the older version of the player after installation has completed
    // DO NOT MODIFY THE FOLLOWING FOUR LINES
    // Location visited after installation is complete if installation is required
    document.write(alternateContent);

    var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
    var MMredirectURL = window.location;
    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
    var MMdoctitle = document.title;

    AC_FL_RunContent(
        "src", "playerProductInstall",
        "FlashVars", "MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
        "width", "100%",
        "height", "100%",
        "align", "middle",
        "id", "Centrifuge",
        "quality", "high",
        "bgcolor", "white",
        "name", "Centrifuge",
        "allowScriptAccess","sameDomain",
        "type", "application/x-shockwave-flash",
        "pluginspage", "http://www.adobe.com/go/getflashplayer"
    );
} else if (hasRequestedVersion) {
    // if we've detected an acceptable version
    // embed the Flash Content SWF when all tests are passed
    AC_FL_RunContent(
            "src", "/Centrifuge/flex/liveAsset",
            "flashVars", "<%= flashVars %>",
            "width", "100%",
            "height", "100%",
            "align", "middle",
            "id", "Centrifuge",
            "quality", "high",
            "bgcolor", "white",
            "name", "Centrifuge",
            "allowScriptAccess","sameDomain",
            "type", "application/x-shockwave-flash",
            "pluginspage", "http://www.adobe.com/go/getflashplayer"
    );
  } else {  // flash is too old or we can't detect the plugin
    document.write(alternateContent);
  }
// -->
</script>
<noscript>
    <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
            id="liveAsset" width="100%" height="100%"
            codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
            <param name="movie" value="liveAsset.swf" />
            <param name="quality" value="high" />
            <param name="bgcolor" value="white" />
            <param name="allowScriptAccess" value="sameDomain" />
            <param name='flashVars' value='<%= flashVars %>'>
            <embed src="liveAsset.swf" quality="high" bgcolor="white"
                flashVars='<%= flashVars %>'
                width="100%" height="100%" name="Centrifuge" align="middle"
                play="true"
                loop="false"
                quality="high"
                allowScriptAccess="sameDomain"
                type="application/x-shockwave-flash"
                pluginspage="http://www.adobe.com/go/getflashplayer">
            </embed>
    </object>
</noscript>
</body>
</html>
