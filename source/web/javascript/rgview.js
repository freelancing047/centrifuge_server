function RGView(dvuuid, vizuuid, index, nodes, links, nodeDefsMap, linkDefsMap) {
	this.dvuuid = dvuuid;
	this.vizuuid = vizuuid;
	this.index = vizuuid;
	this.draggraph = null;
	this.nodes = nodes;
	this.links = links;
	this.nodeDefsMap = nodeDefsMap;
	this.linkDefsMap = linkDefsMap;
}

RGView.prototype.doLoad = function(relImage) {
	var rgv = this;
	var index = this.index;
	var dv = window.dataview;
	csi.relgraph.summary(rgv.vizuuid, {
		onsuccess: function(data) {
			var viz = utils.getVisualisation(rgv.vizuuid);
			if (data.resultData.nodeCount > viz.visualization.clientProperties["render.threshold"]) {
				var thresholdstatus = true;
				rgv.drawTables(thresholdstatus);
				dv.generateNodeExceedInfoPanel(relImage, data.resultData.nodeCount, viz.visualization.clientProperties["render.threshold"]);
				$('#selectall' + index ).addClass('disabled-link');
				$('#hidelegend' + index ).addClass('disabled-link');
				$('#resetlegend' + index ).addClass('disabled-link');
				$('#computeSNA' + index ).addClass('disabled-link');
				$('#scrambleLayout' + index ).addClass('disabled-link');
				$('#radialLayout' + index ).addClass('disabled-link');
				$('#hierarchicalLayout' + index ).addClass('disabled-link');
				$('#forceDirectedLayout' + index ).addClass('disabled-link');
				$('#circularLayout' + index ).addClass('disabled-link');
				$('#centrifugeLayout' + index ).addClass('disabled-link');				
			} else {
				rgv.loadGraph();
				rgv.doRegister();
			}
		}			
	});
}
RGView.prototype.doRegister = function() {
	var displayID = this.vizuuid;
	var img = document.getElementById(displayID);
	if (!img) {
		return;
	}
	var rgView = this;
	$.data(img, 'rgview', rgView);
	img._mouseinfo = {};
	var mouseMoveTask = new MouseMoveRG(this.vizuuid);
	var mm = function(rgView) {
		return function(event) {
			mouseMoveTask.doTask(event);
		};
	};
	$(img).mousemove(mm(rgView));
}

RGView.prototype.setDragGraph = function(draggraph) {
	this.draggraph = draggraph;
}

RGView.prototype.loadGraph = function() {
	var vizuuid = this.vizuuid;
	new RefreshImage(vizuuid).doTask();
	var dragGraph = new DragGraph(vizuuid);
	dragGraph.doRegister();
	this.setDragGraph(dragGraph);
	this.drawTables();
	var img = document.getElementById(vizuuid);
	this.draggraph.setImageAreaSelection(img);
	var getGraphFlags = new GetGraphFlags(vizuuid);
	getGraphFlags.doTask();
}
RGView.prototype.drawTables = function(thresholdstatus) {
	var vizuuid = this.vizuuid;
	var createNodeTable = new CreateNodeTable(vizuuid);
	createNodeTable.doTask();
	var createLinkTable = new CreateLinkTable(vizuuid);
	createLinkTable.doTask();
	var createPathsTable = new CreatePathsTable(vizuuid);
	createPathsTable.doTask();		
	var searchResultsTable = new CreateSearchResultsTable(vizuuid);			
	searchResultsTable.doTask();
	if(thresholdstatus != true){
		var listNodes = $.data($('#nodes-table' + vizuuid)[0], "controller");
		listNodes.doFetch();
		var listLinks = new ListLinks(vizuuid);
		listLinks.doTask();
	}
}
RGView.prototype.testForGraph = function() {
	if (this.vizuuid == 'null' || this.vizuuid == null) {
		bootbox.alert("No relationship graphs found.")
		return false;
	}
	return true;
}
