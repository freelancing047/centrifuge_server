function AddWorksheet(dvuuid,layout, worksheetname){
	this.dvUuid = dvuuid;
	this.layout = layout;
	this.worksheetname = worksheetname;
	this.utils = new Utils();
}

AddWorksheet.prototype.doTask = function(newLayoutActions){
	var requestJson = JSON.stringify(this.getWorksheetJSON());
	var url = "/Centrifuge/actions/viz/addWorksheet?_f=json&dvUuid="+this.dvUuid;
	var doSuccess = function() {
		return function(data) {			
			window.dataview.myData.resultData.meta.modelDef.worksheets.push(data.resultData);
			var worksheetUuid = data.resultData.uuid;
			var layout = data.resultData.layout;
			newLayoutActions.index = newLayoutActions.index + 1;
			if(layout == "RIGHT HAND LAYOUT") {
				newLayoutActions.addRightHandLayout(worksheetUuid);
			} else if(layout == "LEFT HAND LAYOUT") {
				newLayoutActions.addLeftHandLayout(worksheetUuid);
			} else if(layout == "SINGLE LAYOUT") {
				newLayoutActions.addSinglePanelLayout(worksheetUuid);
			} else if(layout == "EQUAL LAYOUT") {
				newLayoutActions.addEqualPanelLayout(worksheetUuid);
			} 
		};
	};
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
        data: requestJson,
		dataType: 'json',
		success: doSuccess()
	});
}

AddWorksheet.prototype.getWorksheetJSON = function() {
	ws = new Object();
	ws.annotations = null;
	ws.class =  "csi.server.common.model.worksheet.WorksheetDef";
	ws.clientProperties = new Object();
	ws.uuid = this.utils.guidGenerator();
	ws.visualizations = null;
	ws.worksheetName = this.worksheetname;
	ws.layout = this.layout;
	return ws;
}