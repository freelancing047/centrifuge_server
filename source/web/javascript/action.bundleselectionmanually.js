function ManualBundleSelection(vizuuid) {
	this.vizuuid = vizuuid;
}
ManualBundleSelection.prototype.doTask = function(bundleName) {
	var vizuuid = this.vizuuid;
	var visualization = utils.getVisualisation(this.vizuuid).visualization;
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
			$('#unbundleselection' + vizuuid).removeClass('disabled-link');
		};
	};
	csi.relgraph.manuallyBundleSelection(vizuuid, bundleName, {
		onsuccess : doSuccess()
	});
}