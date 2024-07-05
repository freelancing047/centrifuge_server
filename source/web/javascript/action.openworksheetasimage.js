function OpenWorksheetAsImage(vizuuid){
	this.vizuuid=vizuuid;
}

OpenWorksheetAsImage.prototype.doTask = function(){
	var dvuuid = window.dataview.myData.resultData.uuid;
	var vizuuid = this.vizuuid;
	var d = $('#'+vizuuid)[0];
	displayH = d.height;
	displayW = d.width; 
	csi.relgraph.downloadGraphImage(dvuuid, vizuuid, displayW, displayH);	
}