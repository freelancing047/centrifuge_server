function HighlightPath(vizuuid){
	this.vizuuid = vizuuid;

}
HighlightPath.prototype.doTask = function(index){	
	var vizuuid = this.vizuuid;
	var index = index;
	var resultJson = [index+""];
	var url = "/Centrifuge/services/graphs2/actions/highlightPaths?vduuid="+vizuuid+"&_f=json";
	$.ajax({
		type: "POST",
		processData: false,
		data: JSON.stringify(resultJson), 
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {	
			new RefreshImage(vizuuid).doTask();
		},
		error: function(data) {
		}
	});
}