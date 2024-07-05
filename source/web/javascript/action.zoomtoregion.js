function ZoomToRegion(vizuuid) {
	this.vizuuid=vizuuid;
}

ZoomToRegion.prototype.doTask = function(x1, y1, x2, y2) {
	var vizuuid = this.vizuuid;
	var doSuccess = function(vizuuid) {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
		};
	};
	csi.relgraph.zoomToRegion(vizuuid, x1, y1, x2, y2, {
		onsuccess : doSuccess(vizuuid)
	});
}