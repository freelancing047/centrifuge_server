function SaveWorksheetIndex(dvuuid, index, wsUUID){
	this.dvUuid = dvuuid;
	this.index = index;
	this.wid = wsUUID;
}

SaveWorksheetIndex.prototype.doTask = function(){
	var ws = utils.getWorksheet(this.wid)
	var requestJson = JSON.stringify(ws);
	var url = "/Centrifuge/actions/viz/setWorksheetIndex?_f=json&dvUuid="+this.dvUuid + "&index="+this.index;
	var doSuccess = function() {
		return function(data) {
			window.dataview.myData.resultData=data.resultData;
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