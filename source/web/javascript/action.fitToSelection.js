function FitToSelection(vizuuid) {
	this.vizuuid = vizuuid;
}

FitToSelection.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
		}
	}
	csi.relgraph.fitToSelected(vizuuid, {
		onsuccess : doSuccess()
	});

}