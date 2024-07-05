function UnBundleSelection(vizuuid) {
	this.vizuuid = vizuuid;
}
UnBundleSelection.prototype.getRgView = function(vizuuid) {
	this.vizuuid = vizuuid;
}
UnBundleSelection.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var dvuuid = window.dataview.myData.resultData.uuid;
	var visualization = utils.getVisualisation(this.vizuuid).visualization;
	if (visualization.position == 0 || visualization.position == undefined){
		var listNodes = $.data($('#nodes-table' + vizuuid)[0], "controller");
		var listLinks = new ListLinks(vizuuid);
	}
	var doSuccess = function(vizuuid) {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
			if (visualization.position == 0 || visualization.position == undefined){
		    	listNodes.doFetch();
		    	listLinks.doTask();
			}
	    	var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.unbundleSelection(dvuuid, vizuuid, {
		onsuccess : doSuccess(vizuuid)
	});
}