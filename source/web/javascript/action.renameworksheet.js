function RenameWorksheet(wId){
	this.wid = wId;
}
RenameWorksheet.prototype.doTask = function(){
	var wName =$('#renameWorksheetName').val();
    var layoutIndex=utils.getWorksheetIndex(this.wid);
    var isNameUnique = true;
    var renameWorkSheet = this;
    $.each(window.dataview.myData.resultData.meta.modelDef.worksheets, function (index, worksheet) {
    	if (wName == worksheet.worksheetName) {
    		isNameUnique = false;
    	}
    });
    if (isNameUnique) {
    	var url = "/Centrifuge/actions/viz/setWorksheetName?uuid=" + this.wid + "&name="+ wName +"&_f=json"
    	$.ajax({
    		type: "POST",
    		processData: false,
    		url: url,
    		contentType: 'application/json; charset=utf-8',
    		dataType: 'json',
    		success: function(data) {	
    			$('#layoutTab' + renameWorkSheet.wid + " .lName").text(wName);
    			$.each(window.dataview.myData.resultData.meta.modelDef.worksheets, function (index, worksheet) {
    		    	if (renameWorkSheet.wid == worksheet.uuid) {
    		    		window.dataview.myData.resultData.meta.modelDef.worksheets[index].worksheetName = wName;
    		    	}
    		    });
    		}
    	});
    } else {
    	bootbox.alert("Worksheets must have unique names");
    }
	
}