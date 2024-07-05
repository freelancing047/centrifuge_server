function BundleHandler(vizuuid) {
		this.vizuuid = vizuuid;
		this.hasBundleDefs = false; 
		this.canBundle = false;
}

BundleHandler.prototype.getRgView = function(vizuuid) {
		this.vizuuid = vizuuid;
}

BundleHandler.prototype.registerDialog = function(index,flags) {
		$('#bundleselection'+ index).addClass('disabled-link');
		this.hasBundleDefs = flags.hasBundleDefs;
		this.canBundle = flags.canBundle;
		if(this.hasBundleDefs || this.canBundle){
			$('#bundleselection'+ index).removeClass('disabled-link');
		}
		$('#bundleManually'+ index).attr('disabled','disabled');
		$('#bundleSelectBySpec'+ index).attr('disabled','disabled');
		$('#bundleGraphBySpec'+ index).attr('disabled','disabled');
		$('#bundlename'+ index).attr('disabled','disabled');
		if(this.canBundle && this.hasBundleDefs){
			$('#bundleManually'+ index).removeAttr('disabled');
			$('#bundleSelectBySpec'+ index).removeAttr('disabled');
			$('#bundleGraphBySpec'+ index).removeAttr('disabled');
			$('#bundleGraphBySpec'+ index).attr('checked','checked');
			
		}
		else if(!this.canBundle && this.hasBundleDefs){
			$('#bundleGraphBySpec'+ index).removeAttr('disabled');
			$('#bundleGraphBySpec'+ index).attr('checked','checked');
			// bundleentiregraphbyspec radio enabled
		}
		else if(this.canBundle && (!this.hasBundleDefs)){
			$('#bundleManually'+ index).removeAttr('disabled');
			$('#bundleManually'+ index).attr('checked','checked');
			$('#bundlename'+ index).removeAttr('disabled');
			// manual bundle radio enabled
		}
		$('.bundle-radio').click(function(e) {
			$('#bundlename'+ index).attr('disabled','disabled');
			if($('#bundleManually'+ index).is(':checked')){
				$('#bundlename'+ index).removeAttr('disabled');
			}
			e.stopPropagation();
		});
}

BundleHandler.prototype.doTask = function(index) {
	
		var checked = $('#bundle-dialogue'+ index).find(':checked')[0].id;
		if(checked == 'bundleManually'+ index){
			var manualBundleSelection = new ManualBundleSelection(this.vizuuid);
			manualBundleSelection.doTask($('#bundlename' + this.vizuuid).val());
			$('#bundle-dialogue' + this.vizuuid).modal('hide');
		}
		else if(checked == 'bundleGraphBySpec'+ index){
			var bundleEntireGraphBySpec = new BundleEntireGraphBySpec(this.vizuuid);
			bundleEntireGraphBySpec.doTask();
		}
		else if(checked == 'bundleSelectBySpec'+ index){
			var bundleSelectionBySpec = new BundleSelectionBySpec(this.vizuuid);
			bundleSelectionBySpec.doTask();
		}

}
