<%
	String dv = request.getParameter("dv");
	String viz = request.getParameter("viz");
	
	String useSession = request.getParameter("useSession");
	if (useSession != null && useSession.equalsIgnoreCase("true")) {
		String graphId = (String) request.getSession().getAttribute("sessionGraphId");
		dv = graphId;
		viz = graphId;
	}
	
%>

<!doctype html>
<head>
<meta charset="utf-8">
<title>Relationship Graph API Sample</title>

<script type="text/javascript" src="../../javascript/json2.js"></script> <!-- JSON implementation is needed because IE does not include it, whilst other browsers do. http://stackoverflow.com/questions/5339232/json-is-undefined-error-in-javascript-in-internet-explorer-->

<script type="text/javascript" src="../../javascript/jquery-1.5.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.relgraph.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.viz.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.dataview.js"></script>


<link rel="stylesheet" type="text/css" href="../../javascript/jquery.ddsmoothmenu/ddsmoothmenu.css" />
<link rel="stylesheet" type="text/css" href="../../javascript/jquery.ddsmoothmenu/ddsmoothmenu-v.css" />
<script type="text/javascript" src="../../javascript/jquery.ddsmoothmenu/ddsmoothmenu.js"></script>

<link rel="stylesheet" type="text/css" href="../../javascript/jquery.imgareaselect-0.9.3/css/imgareaselect-csi.css" />
<script type="text/javascript" src="../../javascript/jquery.imgareaselect-0.9.3/scripts/jquery.imgareaselect.min.js" ></script>
<script type="text/javascript" src="../../javascript/jquery.mousewheel.js" ></script>

<script type="text/javascript" src="_sampleApp.js" ></script>

<style type="text/css">
  body {
	font: 12px Arial, Helvetica, sans-serif;
	color:#333;
	margin:0px;
	padding:0px; 
	overflow: hidden;
  }
  
  html, body {
  	height:100%;
  	overflow:hidden;
  }
  
  .graphLegend {
	position: absolute;
	top: 48px;
	right: 10px;
	font: 11px Arial, Helvetica, sans-serif;
	border: 1px solid black;
	z-index: 1000;
	display: none;
	width: 300px;
	height: auto;
	white-space:nowrap;
  }
  
  .legendBody {
	padding: 5px;
	background: rgb(240,248,255);
	white-space:nowrap;
	overflow: auto;
	max-height: 300px;
  }

  .spinoffContentDiv {
      padding: 8px;
  }
  
  #spinoffErrors {
  	padding: 0 0 10px 0;
  	color:#CC0000;
  	display:none;
  	margin:0;
  }
  
  .spinoffContentDiv label {
  	float:left;
  	width:100px;
  	font-size:12px;
  	font-weight: bold;
  	line-height:20px;
  }
  
  .spinoffContentDiv .spinoffButtons {
  	padding:5px 0 0 100px;
  }
  
  .spinoffContentDiv .spinoffButtons input {
  	margin:0 5px 0 0;
  }
  
  #spinoffName {
  	width: 200px;
  }

  .legendItem {
  	float:left;
  	clear:left;
  }
  
  .legendImage {
  	width: 24px;
  	height: 24px;
  	vertical-align: middle;
	padding-top: 2px;
	padding-bottom: 2px;
  }

  .graphToolbar {
  	background:#6480FF;
    position:absolute;
    top:0;
    left:0;
  	height:31px;
  	width:100%;
  	font:bold 12px Verdana;
  	z-index:200;
  }
  
  .graphToolbar #menu {
  	float:left;
  }
  
  .graphTitleBar {
    position:relative;
  	border: 0px;
  	display: block;
  	background:rgb(100,128,255);
  	padding: 5px;
  	color: white;
  	font: 12px Arial, Helvetica, sans-serif;
  	font-style: bold;
  	white-space:nowrap;
  	border-bottom:1px solid black;
  }

  .filterContentDiv {
      width: 500px;
      max-height: 250px;
      overflow: auto;
  }

  a.mouseChange {
      display: block;
      width: 20px;
      height: 15px;
      text-decoration: none;
      color: #ffffff;
  }

  a.mouseChange:hover {
      background-position: -100px 0;
      text-decoration: none;
      color: #ffffff;
  }

  .filterValues{
      padding-left: 57px;
  }

  .filterDiv{
      padding: 5px;
  }
  
  .graphTitleBar table {
  	color:#FFF;
  }

  .graph {
  	float:left;
  	width:100%;
  	height:auto;
    position: absolute;
    top:31px;
    left:0;
    right:0;
    bottom:0;
  }
  	
  .graphImage {
	width:auto;
  	margin:0px;
  	padding:0px;
  	border:none;
  	display:block;
  	visibility:hidden;
  }
  
  .graphTooltip {
	background:rgb(240,248,255);
	border:1px solid lightgray;
	padding:5px;
	overflow:auto;
	position:absolute;
	display:none;
	z-index:1000;
	max-width: 300px;
	max-height: 300px;
  }
  
  .tipkey {
    font: 12px Arial, Helvetica, sans-serif;
  	border: 0px;
  	margin:0px;
  	border-collapse: collapse;
  	vertical-align: top;
  	padding: 0px;
  	padding-bottom: 2px;
  	white-space:nowrap;
  	
  }
  
  .tipitem {
    font: 12px Arial, Helvetica, sans-serif;
  	border: 0px;
  	margin:0px;
  	border-collapse: collapse;
  	vertical-align: top;
  	padding: 0px;
  	padding-bottom: 2px;
  	white-space:nowrap;
  	
  }
  
  .tiplist {
  	font: 12px Arial, Helvetica, sans-serif;
  	list-style-type: none;
  	padding: 0px;
  	border: 0px;
  	margin: 0px;
  	border-collapse: collapse;
  }

  .dialogContainer{
	  position: absolute;
	  width: 100%;
      z-index: 1000;
      text-align: center;
      display: none; 
  }
  
  .outerDialogPanel{
  	  padding-top: 150px;
	  width: 100%;
  }
  
  .innerSpinoffPanel {
      font: 11px Arial, Helvetica, sans-serif;
      border: 1px solid black;
      width: 350px;
      height: auto;
      background: rgb(240, 248, 255);
      white-space: nowrap;
      margin-left: auto;
      margin-right: auto;
      text-align: left;
  }
  
  .spinnerPanel{
	position: absolute;
	width: 100%;
    z-index: 1010;
    text-align: center;
    display: none;
  }
  
  .innerSpinner{
    border: 1px solid black;
    background: rgb(240, 248, 255);
    padding: 8px;
    width: 82px;
    margin-left: auto;
    margin-right: auto;
  }
  
  .innerFilterPanel {
      font: 11px Arial, Helvetica, sans-serif;
      border: 1px solid black;
      width: 500px;
      height: auto;
      background: rgb(240, 248, 255);
      white-space: nowrap;
      margin-left: auto;
      margin-right: auto;
      text-align: left;
  }
  
  </style>
<script>
	var dvuuid = null;
	var vizuuid = null;	
	var displayH = null;
	var displayW = null;
	
	<% if (dv != null){ %>
	dvuuid = "<%=dv%>";
	<%}%>
	<% if (viz != null){ %>
	vizuuid = "<%=viz%>";
	<%}%>
	var isSessionGraph = "<%=useSession%>";
	
	function initGraph() {
		if (isSessionGraph == 'true') {
			refreshImage();
		} else if (dvuuid && vizuuid){
			loadGraph();
		}
	}

	$(document).ready(function () {
	
		initGraph();
		
		ddsmoothmenu.arrowimages= {down:['downarrowclass', '../../javascript/jquery.ddsmoothmenu/down.gif', 23], right:['rightarrowclass', '../../javascript/jquery.ddsmoothmenu/right.gif']};
		ddsmoothmenu.init({
			mainmenuid: "menu", //menu DIV id
			orientation: 'h', //Horizontal or vertical menu: Set to "h" or "v"
			classname: 'ddsmoothmenu', //class added to menu's outer DIV
			//customtheme: ["#1c5a80", "#18374a"],
			contentsource: "markup" //"markup" or ["container_id", "path_to_menu_file"]
		});
		
		setDragMode('pan');

	});

</script>
</head>
<body>
	<div id="toolbar" class="graphToolbar">
		<div id="menu" >
			<ul >
				<li><a href="javascript:void(0);">Graph</a>
					<ul>
						<li id="savegraph"><a href="javascript:void(0);" onClick="saveGraph();return false;">Save Graph</a></li>
						<li id="exportGraphImage"><a href="javascript:void(0);" onClick="downloadGraphImage();return false;" >Export Graph Image</a></li>
						<li id="exportSelected"><a href="javascript:void(0);" onClick="downloadSelectedData();return false;">Export Selected Items</a></li>
						<li id="spinoff"><a href="javascript:void(0);" onClick="onSpinOffMenuItemClick();return false;">Spinoff...</a></li>
					</ul>
				</li>
				<li><a href="javascript:void(0);">Edit</a>
					<ul>						
						<li id="clearSelection"><a href="javascript:void(0);" onClick="clearSelection();return false;">Select None</a></li>
						<li id="selectAll"><a href="javascript:void(0);" onClick="selectAll();return false;">Select All</a></li>
					</ul>
				</li>
				
				<li><a href="javascript:void(0);">View</a>
					<ul>						
						<li id="legend"><a href="javascript:void(0);" onClick="enableLegend();return false;">Show Legend</a></li>
						<li id="fitToSize"><a href="javascript:void(0);" onClick="fitToSize();return false;">Fit To Size</a></li>
						<li id="fitToSelection"><a href="javascript:void(0);" onClick="fitToSelection();return false;">Fit To Selected</a></li>
					</ul>
				</li>
				
				<li id="layout"><a href="javascript:void(0);">Layout</a>
					<ul>
						<li><a href="javascript:void(0);" onClick="startLayout('centrifuge');return false">Centrifuge</a></li>
						<li><a href="javascript:void(0);" onClick="startLayout('circular');return false">Circular</a></li>
						<li><a href="javascript:void(0);" onClick="startLayout('forceDirected');return false">Force Directed</a></li>
						<li><a href="javascript:void(0);" onClick="startLayout('radial');return false">Radial Hierarchy</a></li>
						<li><a href="javascript:void(0);" onClick="startLayout('hierarchical');return false">Linear Hierarchy</a></li>
						<li><a href="javascript:void(0);" onClick="startLayout('scramble');return false">Scramble &amp; Place</a></li>
					</ul>
				</li>
				<li><a href="javascript:void(0);">Drag Mode</a>
					<ul>
						<li id="pan" >
							<a href="javascript:void(0);" onClick="setDragMode('pan');">
								<input name="modeGroup" type="radio" value="pan" checked='checked' alt="Pan/Select" src="../../images/pan-select.png" /><span>Pan</span>
							</a>
						</li>
						<li id="select">
							<a href="javascript:void(0);" onClick="setDragMode('select');" >
								<input  name="modeGroup" type="radio" value="select" alt="Select Region" src="../../images/select-region.png" /><span>Select</span>
							</a>
						</li>
						<li id="zoom">
							<a href="javascript:void(0);" onClick="setDragMode('zoom');" >
								<input name="modeGroup" type="radio" value="zoom" alt="Zoom to Region" src="../../images/zoom-region.png" /><span>Zoom</span>
							</a>
						</li>
					</ul>
				</li>
                <li><a href="javascript:void(0);">Filter</a>
                    <ul>
						<li><a id="displayFilters" href="#">Display Filters</a></li>
						<li><a id="resetFilters" href="#">Reset Filters</a></li>
					</ul>
                </li>
			</ul>		
		</div>
	</div>
	<div id="container" class="graph" >
		<img id="display" class="graphImage" src="" alt=""/>
	</div>
	<div id="tooltipPanel" class="graphTooltip"></div>
	<div id="legendPanel" class="graphLegend">
		<div class="graphTitleBar" >
			<table summary="">
				<tr><td nowrap="nowrap" width="100%">Legend</td><td align="right" nowrap="nowrap"><span onClick="javascript:collapseExpandLegend(this)">-</span>&nbsp;<span onClick="javascript:hideLegend()">x</span></td></tr>
			</table>
		</div>
		<div id="legendBody" class="legendBody"></div>
	</div>
	
	<div id="filterPanel" class="dialogContainer">
		<div class="outerDialogPanel">
		    <div class="innerFilterPanel">
		        <div class="graphTitleBar" >
					<table summary="">
						<tr><td nowrap="nowrap" width="100%">Filters</td><td align="right" nowrap="nowrap"><a class="mouseChange" href="#" onClick="javascript:closeFilterPanel()">x</a></td></tr>
					</table>
				</div>
		        <div id="filterContentDiv" class="filterContentDiv">
		
		        </div>
		        <br>
		        <br>
		        <div id="buttonsDiv">
		            <table width="100%">
		                 <tr>
		                    <td style="text-align: center;">
		                        <input type="button" value="Add Filter" onClick="addFilter();"/>
		                        <input type="button" value="Save" onClick="saveFilters();"/>
		                    </td>
		                </tr>
		            </table>
		        </div>
		    </div>
	    </div>
    </div>

	<div id="spinoffPanel" class="dialogContainer">
	    <div class="outerDialogPanel">
		    <div class="innerSpinoffPanel">
		        <div class="graphTitleBar" >
					<table summary="">
						<tr><td nowrap="nowrap" width="100%">Create spinoff</td><td align="right" nowrap="nowrap"><a class="cancelSpinoff" class="mouseChange" href="#">x</a></td></tr>
					</table>
				</div>
		        <div id="spinoffContentDiv" class="spinoffContentDiv">
		        	<p id="spinoffErrors"></p>
					<label>Dataview name:</label><input id="spinoffName" type="text" name="spinoffName"/>
					<div class="spinoffButtons"><input id="createSpinoff" type="button" name="createSpinoff" value="Spinoff" onClick="onSpinOffCreateBtnClick();"/><input class="cancelSpinoff" type="button" onClick="onSpinOffCancelBtnClick();" name="cancelSpinoff" value="Cancel"/></div>
		        </div>
		    </div>
	    </div>
    </div>
    
    <div id="spinnerPanel" class="spinnerPanel">
    	<div class="outerDialogPanel">
    		<div class="innerSpinner">
    			<img src="ajax-loader.gif"></img>
    		</div>
    	</div>
    </div>

</body>
</html>