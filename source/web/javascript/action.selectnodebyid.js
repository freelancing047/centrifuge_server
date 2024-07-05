function SelectNodeById(vizuuid) {
	this.vizuuid = vizuuid;
}

SelectNodeById.prototype.doTask = function(nodeid, flag, bFitToSelection) {
	var vizuuid = this.vizuuid;
	var bFitToSelection = bFitToSelection;
	var doSuccess = function(vizuuid) {
		return function(data) {
			if (bFitToSelection){
				var fitToSelection = new FitToSelection(vizuuid);
				fitToSelection.doTask();			
			} else {
				new RefreshImage(vizuuid).doTask();
			}
			var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.selectNodeById(vizuuid, nodeid, flag, {
		onsuccess: 	doSuccess(vizuuid)
	});
}