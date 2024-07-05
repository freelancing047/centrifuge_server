function SelectRegion(vizuuid){
	this.vizuuid=vizuuid;
}

SelectRegion.prototype.doTask  = function selectRegion(x1, y1, x2, y2, reset) {
	var doSuccess = function(vizuuid){
		return function(data){
			new RefreshImage(vizuuid).doTask();
			var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.selectRegion(this.vizuuid, x1, y1, x2, y2, reset, {
		onsuccess: doSuccess(this.vizuuid)			
	});
}
