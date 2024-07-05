function UnHideSelection(vizuuid){
	this.vizuuid=vizuuid;
}

UnHideSelection.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var doSuccess = function(vizuuid) {
	    return function(data) {
	    	new RefreshImage(vizuuid).doTask();
	    };
	};
	csi.relgraph.unhideSelected(vizuuid, {
		onsuccess : doSuccess(vizuuid)
	});
}