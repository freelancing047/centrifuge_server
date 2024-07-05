function SelectAll(vizuuid){
	this.vizuuid=vizuuid;
}

SelectAll.prototype.doTask = function() {
	var doSuccess = function(vizuuid) {
	    return function(data) {
	    	new RefreshImage(vizuuid).doTask();
	    	var getGraphFlags = new GetGraphFlags(vizuuid);
	    	getGraphFlags.doTask();
	    };
	};
	csi.relgraph.selectAll(this.vizuuid, {
		onsuccess: doSuccess(this.vizuuid)
	});
}