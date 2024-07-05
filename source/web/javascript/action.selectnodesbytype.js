function SelectNodesByType(vizuuid) {
	this.vizuuid=vizuuid;
}

SelectNodesByType.prototype.doTask = function(event) {
	var vizuuid = this.vizuuid;
	var dvuuid = window.dataview.myData.resultData.uuid;
	var type = event.target.textContent;
	if(type === "Bundle"){
		type = "csi.internal.Bundle";
	}
	var doSuccess = function(vizuuid) {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
			var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		};
	};
	csi.relgraph.selectNodesByType(dvuuid, vizuuid, type, true, {
			onsuccess : doSuccess(vizuuid)
	});
}