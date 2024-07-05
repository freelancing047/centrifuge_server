function UnBundleHandler(vizuuid) {
	this.vizuuid = vizuuid;
	this.hasBundleDefs = false; 
	this.hasBundles = false;
	this.hasBundleSelected = false;
}

UnBundleHandler.prototype.getRgView = function(vizuuid) {
	this.vizuuid = vizuuid;
}

UnBundleHandler.prototype.registerDialog = function(vizuuid,flags) {
	this.hasBundleDefs = flags.hasBundleDefs; 
	this.hasBundles = flags.hasBundles;
	this.hasBundleSelected = flags.hasBundleSelected;
	$('#unbundleselection'+ vizuuid).addClass('disabled-link');
	$('#unbundleEntire'+ vizuuid).attr('disabled','disabled');
	$('#unbundleSelected'+ vizuuid).attr('disabled','disabled');

	if(this.hasBundles || this.hasBundleSelected ){
		$('#unbundleselection'+ vizuuid).removeClass('disabled-link');
		$('#unbundleEntire'+ vizuuid).removeAttr('disabled','disabled');
		$('#unbundleEntire'+ vizuuid).attr('checked','checked');
	}
	if(this.hasBundleSelected){
		$('#unbundleselection'+ vizuuid).removeClass('disabled-link');
		$('#unbundleSelected'+ vizuuid).removeAttr('disabled');
		$('#unbundleSelected'+ vizuuid).attr('checked','checked');
	}
}

UnBundleHandler.prototype.doTask = function(vizuuid) {
	var checked = $('#unbundle-dialogue'+ vizuuid).find(':checked')[0].id;
	if(checked == 'unbundleEntire'+ vizuuid){
		var unbundleEntire = new UnBundleEntireGraph(this.vizuuid);
		unbundleEntire.doTask();
	}
	else if(checked == 'unbundleSelected'+ vizuuid){
		var unbundleSelected = new UnBundleSelection(this.vizuuid);
		unbundleSelected.doTask();
	}
}

