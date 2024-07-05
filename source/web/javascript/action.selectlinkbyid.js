function SelectLinkById(vizuuid) {
	this.vizuuid = vizuuid;
}

SelectLinkById.prototype.doTask = function(linkid,flag, bFitToSelection) {
	var vizuuid = this.vizuuid;
	var bFitToSelection = bFitToSelection;
	var doSuccess = function(vizuuid) {
		return function(data) {
			if (bFitToSelection){
				var fitToSelection = new FitToSelection(vizuuid);
				fitToSelection.doTask();
			}
			else{
				new RefreshImage(vizuuid).doTask();
			}		

			var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.selectLinkById(vizuuid, linkid, flag, {
		onsuccess: 	doSuccess(vizuuid)
	});
}



