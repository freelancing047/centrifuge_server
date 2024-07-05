function ContextMenuFunction(vizuuid) {
	this.vizuuid = vizuuid;
	$('#contextMenuTemplate' + this.vizuuid + " .selectAll").unbind('click');
	$('#contextMenuTemplate' + this.vizuuid + " .deselectAll").unbind('click');
	$('#contextMenuTemplate' + this.vizuuid + " .hideSelection").unbind('click');
	$('#contextMenuTemplate' + this.vizuuid + " .hideUnselected").unbind('click');
	$('#contextMenuTemplate' + this.vizuuid + " .bundle").unbind('click');
	    
}

ContextMenuFunction.prototype.doTask = function() {
	var contextMenu = this;
	$('#contextMenuTemplate' + contextMenu.vizuuid + " .selectAll").click(function() {		
		$('#selectall' + contextMenu.vizuuid).click();
	});
	
	$('#contextMenuTemplate' + contextMenu.vizuuid + " .deselectAll").click(function() {		
		$('#deselectall' + contextMenu.vizuuid).click();
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeSelect').removeAttr('disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeDeselect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeHideSelect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .removeNodes').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .revealNeighbor').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .selectNeighbor').attr('disabled','disabled');
	});
	
	$('#contextMenuTemplate' + contextMenu.vizuuid + " .hideSelection").click(function() {		
		$('#hideselection' + contextMenu.vizuuid).click();
	});
	
	$('#contextMenuTemplate' + contextMenu.vizuuid + " .hideUnselected").click(function() {
		new HideUnSelected(contextMenu.vizuuid).doTask();
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeSelect').removeAttr('disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeDeselect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .nodeHideSelect').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .removeNodes').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .revealNeighbor').attr('disabled','disabled');
		$('#contextMenuTemplateNode' + contextMenu.vizuuid + ' .selectNeighbor').attr('disabled','disabled');		
	});
	
	$('#contextMenuTemplate' + contextMenu.vizuuid + " .bundle").click(function() {
		contextMenu.bundleCount();
	});
	
}

//Function to bundle selected nodes.
ContextMenuFunction.prototype.doBundle = function() {
	var contextMenu = this;
	//Get count of bundles selected from canvas element.
	var bundleCount = $('#'+ contextMenu.vizuuid).attr('count');
	//Name the bundle as 'UntitledBundle(n+1)', if 'n' is the no. of bundles in rel graph.
	new ManualBundleSelection(contextMenu.vizuuid).doTask("UntitledBundle" + bundleCount); 
}

//Function to count all bundles in the graph before naming it.
ContextMenuFunction.prototype.bundleCount = function() {
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