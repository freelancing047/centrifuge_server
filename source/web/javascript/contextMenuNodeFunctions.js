function ContextMenuNodeFunction(vizuuid) {
	this.vizuuid = vizuuid;
	this.nodeId = 0;
	$('#contextMenuTemplateNode' + this.vizuuid + " .nodeSelect").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .nodeDeselect").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .nodeHideSelect").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .removeNodes").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .showOnly").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .bundle").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .revealNeighbor").unbind('click');
	$('#contextMenuTemplateNode' + this.vizuuid + " .selectNeighbor").unbind('click');
   	$('#contextMenuTemplateNode' + this.vizuuid + " .unbundle").unbind('click');
}

ContextMenuNodeFunction.prototype.doTask = function(x,y) {
	var contextMenu = this;
	contextMenu.getNode(x,y);
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .nodeSelect").click(function() {
		new SelectNodeById(contextMenu.vizuuid).doTask(contextMenu.nodeId, false, 0);
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .nodeDeselect").click(function() {
		new SelectNodeById(contextMenu.vizuuid).doTask(contextMenu.nodeId, false, 0);
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .nodeHideSelect").click(function() {
		contextMenu.nodeHide(contextMenu.nodeId);
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .showOnly").click(function() {
		new HideUnSelected(contextMenu.vizuuid).doTask();
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeSelect').removeAttr('disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeDeselect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeHideSelect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .removeNodes').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .revealNeighbor').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .selectNeighbor').attr('disabled','disabled');
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .bundle").click(function() {
		contextMenu.bundleCount();		
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .unbundle").click(function() {
		new UnBundleSingleNode(contextMenu.vizuuid, contextMenu.nodeId).doTask();
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .revealNeighbor").click(function() {
		new RevealNeighbours(contextMenu.vizuuid).doTask(1);
	});
	
	$('#contextMenuTemplateNode' + contextMenu.vizuuid + " .selectNeighbor").click(function() {		
		new SelectNeighbors(contextMenu.vizuuid).doTask(1);
		new GetGraphFlags(contextMenu.vizuuid).doTask();
	});
		
	
}

//hide the right clicked node only
ContextMenuNodeFunction.prototype.nodeHide = function(elementId) {
	var contextMenu = this;
	var type = "node";
	var flag = true;
	var dvUuid = window.dataview.myData.resultData.uuid;;
	var vizUuid = contextMenu.vizuuid;
	
	var url = "/Centrifuge/services/graphs2/actions/dohideUnhideTask?_f=json&vduuid="+vizUuid+"&dvuuid="+dvUuid+"&id="+elementId+"&type="+type+"&flag="+flag;
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			new RefreshImage(vizUuid).doTask(); 
			var listNodes = $.data($('#nodes-table' + vizUuid)[0], "controller");
			listNodes.doFetch();
			var listLinks = new ListLinks(vizUuid);
			listLinks.doTask();
		},
		error: function(data) {
		// alert ("Error");
		}
	});
	new RefreshImage(contextMenu.vizuuid).doTask(); 
}

//function to bundle the selected nodes
ContextMenuNodeFunction.prototype.doBundle = function() {
	var contextMenu = this;
	//Get the count of how many bundles are there in the graph from its canvas element.
	var bundleCount = $('#'+ contextMenu.vizuuid).attr('count');
	new ManualBundleSelection(contextMenu.vizuuid).doTask("UntitledBundle" + bundleCount); 
}

//Function to count the number of bundles before naming the bundle
ContextMenuNodeFunction.prototype.bundleCount = function() {
	var contextMenu = this;
	var doSuccess = function() {
		return function(data) {
			var i, count = 0;
			var nodesList = data.resultData;
			for(i in nodesList){
				if(nodesList[i].bundle == true){
					count++;
				}
				console.log("Count"+count);
			}
			//Save the count of bundles present in the image to canvas element (as the count is accessed for bundling from background as well as node)
			$('#'+ contextMenu.vizuuid).attr('count', (count+1));
			contextMenu.doBundle();
		};
	};
	csi.relgraph.listNodes(contextMenu.vizuuid, {
		onsuccess : doSuccess()
	});
}

//Get the node ID right clicked on.
ContextMenuNodeFunction.prototype.getNode = function(x,y) {
	var contextMenu = this;
	var findItemSuccess = function(){
		return function (data) {
			if (data.resultData) {
				//Check if it is a bundle or a node
				if(data.resultData.bundle == false){
					console.log("Find item:"+data.resultData.displayLabel);
					contextMenu.nodeId = data.resultData.itemId;
					contextMenu.checkSelected(contextMenu.nodeId, false);					
				}					
				else{
					console.log(data.resultData.visualItemType);
					contextMenu.nodeId = data.resultData.itemId;
					contextMenu.checkSelected(contextMenu.nodeId, true);	
					
				}					
			}
		}
	};
	csi.relgraph.findItemAt(contextMenu.vizuuid, x, y, {
		onsuccess: findItemSuccess()			
	});	
}

//Check if the node is (selected or not) in the graph
ContextMenuNodeFunction.prototype.checkSelected = function(nodeId, isBundle) {
	var contextMenu = this;
	var selectedflag = 0;
	var doSuccess = function() {
		return function(data) {
			var nodeLength = data.resultData.nodes==undefined ? 0 : data.resultData.nodes.length;
			var i;
			if(nodeLength > 0){
				NodesJson = data.resultData.nodes;
				for (i in NodesJson)	{
					//Check if item is in the selected nodes list
					if (NodesJson[i].itemId == nodeId){
						selectedflag = 1;
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeSelect').attr('disabled','disabled');
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeDeselect').removeAttr('disabled');
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeHideSelect').removeAttr('disabled');
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .removeNodes').removeAttr('disabled');
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .revealNeighbor').removeAttr('disabled');
						$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .selectNeighbor').removeAttr('disabled');
						
						//If it is a bundle
						if (isBundle){
							$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .unbundle').removeAttr('disabled');
							$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .bundle').attr('disabled','disabled');
						}
						else {
							$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .unbundle').attr('disabled','disabled');
						}
					}
				}
				//If (right clicked) node is not selected in the graph.
				if (selectedflag == 0){
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeSelect').removeAttr('disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeDeselect').attr('disabled','disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeHideSelect').attr('disabled','disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .removeNodes').attr('disabled','disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .revealNeighbor').attr('disabled','disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .selectNeighbor').attr('disabled','disabled');
					$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .unbundle').attr('disabled','disabled');
				}
			}
			new GetGraphFlags(contextMenu.vizuuid).doTask();			
		}
	}
	csi.relgraph.selectionInfo(contextMenu.vizuuid, {
		onsuccess: 	doSuccess()
	});	
}
