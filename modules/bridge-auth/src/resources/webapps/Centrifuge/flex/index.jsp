<!-- saved from url=(0014)about:internet -->
<%@ page import="java.security.*" %>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate" />
<META HTTP-EQUIV="Expires" CONTENT="-1" />
<link rel="shortcut icon" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon" />
<link rel="icon" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon" /> 
<title>Centrifuge</title>
<script src="AC_OETags.js" language="javascript"></script>
<script src="geometry.js" language="javascript"></script>

<style type="text/css">@import url('https://bridge-ic.net/header/bridge-header.css');</style>

<style>
body { margin: 0px; overflow:hidden }
</style>

<script language="javascript" src="https://bridge-ic.net/header/bridge-header.jsp"></script>

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

 -->
 </script>
 


</head>

<body onmousewheel="return getWheel();" scroll="no" 
    onload="bridge.replaceHeader('BridgeHeader', '1'), geometry.resizeContent('CentrifugeContent');" 
    onresize="geometry.resizeContent('CentrifugeContent')" >

<div id="BridgeHeader"></div>


<div id="CentrifugeContent">
<script language="JavaScript" type="text/javascript">
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
            "src", "main",
            "width", "100%",
            "height", "100%",
            "align", "middle",
            "id", "Centrifuge",
            "quality", "high",
            "bgcolor", "white",
            "name", "Centrifuge",
            "flashvars",'historyUrl=history.htm%3F&lconid=' + lc_id + '',
            "allowScriptAccess","sameDomain",
            "type", "application/x-shockwave-flash",
            "pluginspage", "http://www.adobe.com/go/getflashplayer",
            "wmode", "transparent"
            
    );
  } else {  // flash is too old or we can't detect the plugin
    document.write(alternateContent);
  }

  geometry.resizeContent('CentrifugeContent');
// -->
</script>
</div>
<noscript>

    <% 
    Principal p = (Principal) request.getUserPrincipal();
    String user = p.getName(); 
    boolean isAdmin = false;
    Boolean admin = (Boolean) request.getAttribute("isAdmin");
    if (null != admin) {
        isAdmin = admin.booleanValue();
    }
    %>

    <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
            id="main" width="100%" height="100%"
            codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
            <param name="movie" value="main.swf" />
            <param name="quality" value="high" />
            <param name="bgcolor" value="white" />
            <param name="allowScriptAccess" value="sameDomain" />
            <embed src="main.swf" quality="high" bgcolor="white"
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
