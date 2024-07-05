function FitToSize(vizuuid) {
	this.vizuuid = vizuuid;
}

FitToSize.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
		}
	}
	csi.relgraph.fitToSize(vizuuid, {
		onsuccess : doSuccess()
	});
}