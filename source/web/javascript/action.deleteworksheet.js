function DeleteWorksheet(wId){
    var wsIdx = utils.getWorksheetIndex(wId);
    this.wid = wId;
    this.dIndex= wsIdx;
}
DeleteWorksheet.prototype.doTask = function(){	
    var layoutId;	
	var ws = utils.getWorksheet(this.wid);
	var dvUuid = $.data(document.body, "dataview").myData.resultData.uuid;
	var requestJSON = JSON.stringify(ws);	
	var url = "/Centrifuge/actions/viz/removeWorksheet?dvUuid="+ dvUuid +"&_f=json";
    var d = this;
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: requestJSON,
		success: function(data) {			
			$.each(window.dataview.myData.resultData.meta.modelDef.worksheets, function (index, worksheet) {
				if (worksheet && worksheet.uuid == d.wid) {
					window.dataview.myData.resultData.meta.modelDef.worksheets.splice(index, 1);
				}
			});
			
            $("#layoutTab"+ d.wid).remove();
            $("#layout"+d.dIndex).remove();
            layoutId = d.dIndex;            
            if($(".tab-content").html()==""){
                $('#left-hand-layout').click();
            }
		},
		error: function(data) {
		}
	});
}