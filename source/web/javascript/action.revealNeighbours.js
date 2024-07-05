function RevealNeighbours(vizuuid){
	this.vizuuid = vizuuid;

}
RevealNeighbours.prototype.getRgView = function(vizuuid) {
	this.vizuuid = vizuuid;
}
RevealNeighbours.prototype.doTask = function(steps){	
	var vizuuid = this.vizuuid;
	var nSteps =parseInt(steps);	
	var id="";
	var url = "/Centrifuge/services/graphs2/actions/degrees?vduuid="+vizuuid+"&n="+nSteps+"&id="+id+"&_f=json";
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {	
			new RefreshImage(vizuuid).doTask();
		},
		error: function(data) {
		// alert ("Error");
		}
	});
}