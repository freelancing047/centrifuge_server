<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="csi.server.business.helper.DataViewHelper" %>
<%@ page import="csi.server.common.model.dataview.DataView" %>
<%@ page import="csi.server.common.model.visualization.VisualizationDef"%>
<%@ page import="csi.server.common.model.visualization.graph.RelGraphViewDef"%>

<!doctype html>
<head>
<meta charset="utf-8">
<title>Relationship Graph</title>
<style type="text/css">
	body{
		font: 1em Arial, sans-serif;
	}
	#apiList>li{
	}
	
	#apiList>li>a{
		color: blue;
	}
	
	#apiList>li>a:hover{
		color: purple;
	}
	
	.centered{
		text-align: center;
		width: 100%;
	}
</style>
</head>

<body>

<div><a href="../../samples/RelateMapAPI/doc/" target="_api">Centrifuge API Documentation</a></div>
<h4>Recently Opened Dataviews: </h4>

<form name="selectDV" action="">
<select id="dataviews" name="viewId">
<%
    // Get a list of recently opened dataviews
   	DataViewHelper helper = new DataViewHelper();
    List<DataView> views = helper.listRecentlyOpenedDataViews();
	List<String> vids = new ArrayList<String>();
	List<String> vnames = new ArrayList<String>();

	DataView cview = null;
	String currentViewName = null;
	if(!views.isEmpty()){
		cview = views.get(0);
	}
	
	String viewId = request.getParameter("viewId");
	if(viewId!=null){
		for( DataView view : views) {
			view.getUuid();
			String uid = view.getUuid();
	    	if(viewId.equals(uid)){
	   			cview = view;
	   			break;
	    	}
		}
	}
	   
	if(cview!=null){
		viewId = cview.getUuid();
		List<VisualizationDef> visualizations = cview.getMeta().getModelDef().getVisualizations();    	
		for (VisualizationDef viz : visualizations) {
		   if (viz instanceof RelGraphViewDef) {
		    	String vizuuid = viz.getUuid();
		    	String vizname = viz.getName();
		    	vids.add(vizuuid);
		    	vnames.add(vizname);
		    }
		}
	}
	
	for( DataView view : views) {		
    	String name = view.getName(); 
    	String uid = view.getUuid();
    	String selected = "";
    	if(uid.equals(viewId)){
    		selected = "selected";
    		currentViewName = name;
    }
%>
    <option onclick="javascript:currentViewId='<%=name%>';" value="<%=uid%>" <%=selected%>><%= name %></option>  	

<% } %>

</select>
<input type="button" value="Load" onclick="javascript:loadView();">
</form>

<h4>Graph Visualizations:</h4>

<%
	if (vids != null && vids.size() > 0){
%>
	<ul>
	<%
		for(int i=0; i<vids.size(); i++){
		String guid = vids.get(i);
		String gname =  vnames.get(i);
		String url = "_sampleApp.jsp?dv=" + viewId + "&viz=" + guid;
	%>
		<li><a href="<%=url%>" target="right" onclick="javascript:currentVizId='<%=guid%>';currentDVId='<%=viewId%>';"><%=gname%></a>
	<%}%>
	</ul>
<%}	else {%>
	No relationship graphs found.
<%}%>

<h4>Additional API Calls:</h4>
  
<ul id="apiList">
	<li id="bundleEntireGraphBySpec" class="apiLi" onclick="bundleEntireGraphBySpec(); return false;"><a href="#">bundleEntireGraphBySpec</a></li>
	<li id="bundleSelectionBySpec" class="apiLi" onclick="bundleSelectionBySpec(); return false;"><a href="#">bundleSelectionBySpec</a></li>
	<li id="computeSNA" class="apiLi" onclick="computeSNA(); return false;"><a href="#">computeSNA</a></li>
	<li id="dataviewNameExists" class="apiLi" onclick="dataviewNameExistsPrompt(); return false;"><a href="#">dataviewNameExists</a></li>
	<li id="downloadGraphData" class="apiLi" onclick="downloadGraphData(); return false;"><a href="#">downloadGraphData</a></li>
    <li id="getUniqueDataviewName" class="apiLi" onclick="getUniqueDataviewName(currentViewName);return false;" ><a href="#">getUniqueDataviewName</a></li>	
	<li id="hasSelection" class="apiLi" onclick="hasSelection();return false;" ><a href="#">hasSelection</a></li>
	<li id="hideSelected" class="apiLi" onclick="hideSelection();return false;" ><a href="#">hideSelected</a></li>
	<li id="itemInfo" class="apiLi" onclick="itemInfo(0); return false;"><a href="#">itemInfo</a></li>
	<li id="layoutCentrifuge" class="apiLi" onclick="layoutCentrifuge(); return false;"><a href="#">layoutCentrifuge</a></li>	
	<li id="layoutCircular" class="apiLi" onclick="layoutCircular(); return false;"><a href="#">layoutCircular</a></li>
	<li id="layoutForceDirected" class="apiLi" onclick="layoutForceDirected(); return false;"><a href="#">layoutForceDirected</a></li>
	<li id="layoutLinearHierarchy" class="apiLi" onclick="layoutLinearHierarchy(); return false;"><a href="#">layoutLinearHierarchy</a></li>
	<li id="layoutRadialHierarchy" class="apiLi" onclick="layoutRadialHierarchy(); return false;"><a href="#">layoutRadialHierarchy</a></li>
	<li id="layoutScrambleAndPlace" class="apiLi" onclick="layoutScrambleAndPlace(); return false;"><a href="#">layoutScrambleAndPlace</a></li>
	<li id="listLinks" class="apiLi" onclick="listLinks(); return false;"><a href="#">listLinks</a></li>
	<li id="listNodes" class="apiLi" onclick="listNodes(); return false;"><a href="#">listNodes</a></li>
	<li id="manualBundleSelection" class="apiLi" onclick="manuallyBundleSelection(); return false;"><a href="#">manuallyBundleSelection</a></li>
	<li id="nodeNeighbors" class="apiLi" onclick="nodeNeighbors(0); return false;"><a href="#">nodeNeighbors</a></li>
	<li id="nodePosition" class="apiLi" onclick="nodePosition(0); return false;"><a href="#">nodePosition</a></li>		
	<li id="placeNode" class="apiLi" onclick="placeNode(0, 100, 200); return false;"><a href="#">placeNode</a></li>	
	<li id="selectionInfo" class="apiLi" onclick="selectionInfo(); return false;"><a href="#">selectionInfo</a></li>
	<li id="selectLinkById" class="apiLi" onclick="selectLinkById(0); return false;"><a href="#">selectLinkById</a></li>
	<li id="selectNeighbors1" class="apiLi" onclick="selectNeighbors(1); return false;"><a href="#">selectNeighbors1degree</a></li>
	<li id="selectNeighbors2" class="apiLi" onclick="selectNeighbors(2); return false;"><a href="#">selectNeighbors2degrees</a></li>
	<li id="selectNodeById" class="apiLi" onclick="selectNodeById(0); return false;"><a href="#">selectNodeById</a></li>	
	<li id="summary" class="apiLi" onclick="summary(); return false;"><a href="#">summary</a></li>
	<li id="unbundleEntireGraph" class="apiLi" onclick="unbundleEntireGraph();return false;" ><a href="#">unbundleEntireGraph</a></li>
	<li id="unbundleSelection" class="apiLi" onclick="unbundleSelection();return false;" ><a href="#">unbundleSelection</a></li>
	<li id="unhideSelected" class="apiLi" onclick="unhideSelection();return false;" ><a href="#">unhideSelected</a></li>
	<li id="toggleLabels" class="apiLi" onclick="toggleLabels();return false;" ><a href="#">toggleLabels</a></li>
	<li id="version" class="apiLi" onclick="version(); return false;"><a href="#">version</a></li>
</ul>
<hr>
<h4> Results Data </h4>

<textarea id="results-data" rows="20" cols="35" name="results" readonly="true"></textarea>

<script type="text/javascript" src="../../javascript/jquery-1.5.min.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.relgraph.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.viz.js"></script>
<script type="text/javascript" src="../../javascript/api/csi.dataview.js"></script>

<script type="text/javascript">
	
	var results = $('#results-data');
	var currentVizId = '<%=cview%>';
	var currentViewName = '<%=currentViewName != null ? currentViewName : ""%>';
	
	function dataviewNameExistsPrompt(){
		var name = prompt("Enter the dataview name you would like to check:");
		if(name){
			dataviewNameExists(name);	
		}
	};

	function hideSelection(){
		if (!testForViews()){return false;}
		
		csi.relgraph.hideSelected(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}
 
	function unhideSelection(){
		if (!testForViews()){return false;}
		
		csi.relgraph.unhideSelected(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}

    function toggleLabels(){
        if (!testForViews()){ return fale;}

        csi.relgraph.toggleLabels(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
    }
	
	function manuallyBundleSelection(){
		if (!testForViews()){return false;}
		
		csi.relgraph.manuallyBundleSelection(currentVizId, "My Bundle", {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}
	
	function bundleEntireGraphBySpec(){
		if (!testForViews()){return false;}
		
		csi.relgraph.bundleEntireGraphBySpec(currentDVId, currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}

	function bundleSelectionBySpec(){
		if (!testForViews()){return false;}
		
		csi.relgraph.bundleSelectionBySpec(currentDVId, currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}
			
	function unbundleEntireGraph(){
		if (!testForViews()){return false;}
		
		csi.relgraph.unbundleEntireGraph(currentDVId, currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}	
		});
	}
			
	function unbundleSelection(){
		if (!testForViews()){return false;}
		
		csi.relgraph.unbundleSelection(currentDVId, currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText); 
			}	
		});
	}
		
	function selectionInfo() {
		if (!testForViews()){return false;}
		
		csi.relgraph.selectionInfo(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	
	function listNodes() {
		if (!testForViews()){return false;}
		
		csi.relgraph.listNodes(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
	
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function listLinks() {
		if (!testForViews()){return false;}
		
		csi.relgraph.listLinks(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
	
			onerror: function(xhr, status) {
				// results.text(status+" "+xhr.responseText);
				results.text(xhr.responseText); // removed status because of parseerror in FF3
			}
		});
	}
	
	
	function nodeNeighbors(nodeid) {
		if (!testForViews()){return false;}
		
		csi.relgraph.nodeNeighbors(currentVizId, nodeid, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
	
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	
	function selectNodeById(nodeid) {
		if (!testForViews()){return false;}
		
		csi.relgraph.selectNodeById(currentVizId, nodeid, false, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},
	
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	function selectLinkById(linkid) {
		if (!testForViews()){return false;}
		
		csi.relgraph.selectLinkById(currentVizId, linkid, false, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
			},

			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	
	function computeSNA() {
		if (!testForViews()){return false;}
		
		csi.relgraph.computeSNA(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	
	
	function summary() {
		if (!testForViews()){return false;}
		
		csi.relgraph.summary(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	function selectNeighbors(degree) {
		if (!testForViews()){return false;}
		
		csi.relgraph.selectNeighbors(currentVizId, degree, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();
	
			},
	
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	}
	
	function itemInfo(nodeid) {
		if (!testForViews()){return false;}
		
		csi.relgraph.itemInfo(currentVizId, nodeid, "node", true, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);	
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	
	}
	
	function nodePosition(nodeid) {
		if (!testForViews()){return false;}
		
		csi.relgraph.nodePosition(currentVizId, nodeid, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	
	}
	
	function placeNode(nodeid, x, y, abs) {
		if (!testForViews()){return false;}
		
		csi.relgraph.placeNode(currentVizId, nodeid, x, y, abs, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}
		});
	
	}	
	
	function layoutCircular(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.circular(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}

	function layoutCentrifuge(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.centrifuge(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function layoutForceDirected(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.forceDirected(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function layoutRadialHierarchy(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.radialHierarchy(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function layoutLinearHierarchy(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.linearHierarchy(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function layoutScrambleAndPlace(){
		if (!testForViews()){return false;}
		
		csi.relgraph.layout.scrambleAndPlace(currentVizId, {
			onsuccess: function(data, status, xhr) {
				parent.frames['right'].refreshImage();			
			},
			
			onerror: function(xhr, status) {
				results.text(status+" "+xhr.responseText);
			}			
		});
	}
	
	function downloadGraphData(){
		if (!testForViews()){return false;}
		
		csi.relgraph.downloadSelectedData(currentDVId, currentVizId);
	}	

	function version() {
		results.text(csi.relgraph.version());
	}
	
	function dataviewNameExists(name) {
		if (!testForViews()){return false;}
		
		csi.dataview.dataviewNameExists(name, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},
	
			onerror: function(xhr, status) {
				results.text(xhr.responseText);
			}
		});
	}
	
	function hasSelection() {
		if (!testForViews()){return false;}
		
		csi.relgraph.hasSelection(currentVizId, {
			onsuccess: function(data, status, xhr) {
				results.text(xhr.responseText);
			},

			onerror: function(xhr, status) {
				results.text(xhr.responseText);
			}
		});
	}
    
    function getUniqueDataviewName(name) {
        csi.dataview.getUniqueDataviewName(name, {
            onsuccess: function(data, status, xhr) {
                results.text(xhr.responseText);
            },

            onerror: function(xhr, status) {
                results.text(xhr.responseText);
            }
        });
    }
    	
	function getCurrentViewName(){
		if (!testForViews()){return false;}
		return currentViewName;
	}

	function loadGraph(selected) {
		if (!testForViews()){return false;}
		document.selectDV.submit();
	}

	function loadView()	{
		if (!testForViews()){return false;}
		document.selectDV.submit();
	}
	
	function testForViews(){
		if(document.selectDV.viewId.options.length==0){
			alert("No dataviews found, please create at least one dataview.")
			return false;
		}
		return true;
	}

	function selectTestRegion()	{
		var img = parent.frames['right'].document.getElementById('display');
		parent.frames['right'].action='select';
	}

	function setMouseMode(mode)	{
		parent.frames['right'].setDragMode(mode);
	}

</script>

</body>
</html>