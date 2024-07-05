function Layout(vizuuid){
	this.vizuuid = vizuuid;
}

Layout.prototype.doTask = function(type){
	var vizuuid = this.vizuuid;
	csi.relgraph.doLayout(vizuuid, type, {
		onsuccess: function(data) {
			new RefreshImage(vizuuid).doTask();
		}			
	});
}