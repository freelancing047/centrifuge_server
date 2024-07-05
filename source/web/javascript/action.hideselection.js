function HideSelection(vizuuid){
	this.vizuuid=vizuuid;
}

HideSelection.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
	    return function(data) {
	    	var getGraphFlags = new GetGraphFlags(vizuuid);
	    	getGraphFlags.doTask();
	    	var visualization = utils.getVisualisation(vizuuid).visualization;
	    	var index = visualization.position;
	        if(index == 0 || index ==  undefined){
				var listNodes = $.data($('#nodes-table' + vizuuid)[0], "controller");
		    	listNodes.doFetch();
		    	var listLinks = new ListLinks(vizuuid);
		    	listLinks.doTask();
	        }
	    	new RefreshImage(vizuuid).doTask();	    	
	    };
	};
	csi.relgraph.hideSelected(vizuuid, {
		onsuccess : doSuccess()
	});
}