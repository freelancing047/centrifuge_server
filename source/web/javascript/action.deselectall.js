function DeSelectAll(vizuuid){
	this.vizuuid = vizuuid;
}

DeSelectAll.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
	    return function(data) {
	    	new RefreshImage(vizuuid).doTask();
	    	var getGraphFlag = new GetGraphFlags(vizuuid);
			getGraphFlag.doTask();
	    };
	};
	csi.relgraph.clearSelection(vizuuid, {
		onsuccess: doSuccess()
	});
	$("#tooltipPanel"+vizuuid).text("").hide(1000);

}
