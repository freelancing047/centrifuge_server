<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="csi.config.RelGraphConfig"%>
<%@ page import="csi.config.Configuration"%>

<%
 String dataViewId  = (String) request.getAttribute("dataViewId");
 String tabIndex    = (String) request.getAttribute("tabIndex");
 String queryParams = (String) request.getAttribute("params");
 
 String flashVars = "dataViewId=" + dataViewId + "&tabIndex=" + tabIndex + "&params=" + queryParams + "&exportToANX=false";
 
 RelGraphConfig rgConfig = Configuration.instance().getGraphConfig();
 flashVars += "&graphRenderThreshold=" + rgConfig.getRenderThreshold();
 flashVars += "&relgraphTooltipOpenDelayTime=" + rgConfig.getTooltipOpenDelayTime(); 
 
 String dataviewName  = (String) request.getAttribute("dataviewName");
 if (dataviewName != null)
 {
     flashVars += "&dataviewName=" + dataviewName;
 }
 
 String userID = (String) request.getAttribute("userID");
 if (userID != null)
 {
 	flashVars += "&userID=" + userID;
 }
 
 String procs = (String) request.getAttribute("procs");
 if (procs != null)
 {	
 	flashVars += "&procs=" + procs + "&procParams=" + (String) request.getAttribute("procParams");
 }
 
 if(tabIndex.equals("1") )
 {
   String layout = (String) request.getAttribute("layout");
   flashVars += ("&layout="+layout);
 }
 else if( tabIndex.equals("2") )
 {
   String chartViewIndex = (String) request.getAttribute("chartViewIndex");
   String columns = (String) request.getAttribute("columns");
   String columnCount = (String) request.getAttribute("columnCount");
   String chartCell = (String) request.getAttribute("chartCell");
   String chartFunction = (String) request.getAttribute("chartFunction");
   
   flashVars += ("&chartViewIndex=" + chartViewIndex);
   flashVars += ("&columnCount=" + columnCount);
   
   if( chartCell != null )
     flashVars += ("&chartCell=" + chartCell);
        
   if( chartFunction != null )  
     flashVars += ("&chartFunction=" + chartFunction);
     
   flashVars += columns;
 }
 else if( tabIndex.equals("4") )
 {
   String labels = (String) request.getAttribute("labels");
   flashVars += ("&labels="+labels);
 }
 
%>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Centrifuge</title>
<script src="/Centrifuge/flex/AC_OETags.js" language="javascript"></script>
<script src="/Centrifuge/resources/mapchart/mapchart.js" language="javascript"></script>
<script src="../javascript/jquery.js" language="javascript"></script>
<script src="../javascript/jquery-uuid.js" language="javascript"></script>
<script src="../javascript/jquery.timers.js" language="javascript"></script>
<script src="../javascript/comet.js" language="javascript"></script>

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

 -->
 </script>

</head>

<body onmousewheel="return getWheel();" scroll="no" >
<script type="text/javascript">
<!--
// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
var hasProductInstall = DetectFlashVer(6, 0, 65);

// Version check based upon the values defined in globals
var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);

var alternateContent = 'Centrifuge requires Flash Player version 9 or higher. '
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
            "src", "/Centrifuge/flex/main",
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
            id="main" width="100%" height="100%"
            codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
            <param name="movie" value="main.swf" />
            <param name="quality" value="high" />
            <param name="bgcolor" value="white" />
            <param name="allowScriptAccess" value="sameDomain" />
            <param name='flashVars' value='<%= flashVars %>'>
            <embed src="main.swf" quality="high" bgcolor="white"
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
