function BundleSelectionBySpec(vizuuid) {
	this.vizuuid = vizuuid;
}

BundleSelectionBySpec.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var visualization = utils.getVisualisation(this.vizuuid).visualization;
	$('#bundle-dialogue' + vizuuid).modal('hide');
	if (visualization.position == 0 || visualization.position == undefined){
		var listNodes = $.data($('#nodes-table' + vizuuid)[0], "controller");
		var listLinks = new ListLinks(vizuuid);
	}
	var getGraphFlags = new GetGraphFlags(vizuuid);
	var doSuccess = function() {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
			if (visualization.position == 0 || visualization.position == undefined){
				listNodes.doFetch();
				listLinks.doTask();
			}
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.bundleSelectionBySpec(window.dataview.myData.resultData.uuid, vizuuid, {
		onsuccess : doSuccess()
	});
}