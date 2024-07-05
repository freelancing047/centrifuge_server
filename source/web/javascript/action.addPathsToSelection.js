function AddPathsToSelection(vizuuid){
	this.vizuuid = vizuuid;

}
AddPathsToSelection.prototype.doTask = function(index){	
	var vizuuid = this.vizuuid;
	var resultJson = [""+index];
	var url = "/Centrifuge/services/graphs2/actions/selectPaths?vduuid="+vizuuid+"&addToSelection=true&_f=json";
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
		//      alert ("Error");
		}
	});
}