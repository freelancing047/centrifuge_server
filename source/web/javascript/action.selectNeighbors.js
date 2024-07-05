function SelectNeighbors(vizuuid){
	this.vizuuid=vizuuid;
}

SelectNeighbors.prototype.getRgView = function(vizuuid) {
	this.vizuuid = vizuuid;
}
SelectNeighbors.prototype.doTask = function(degrees) {
	var doSuccess = function(vizuuid) {
	    return function(data) {
	    	new RefreshImage(vizuuid).doTask();
	    };
	};
	csi.relgraph.selectNeighbors(this.vizuuid, degrees, {
		onsuccess: doSuccess(this.vizuuid)
	});
}