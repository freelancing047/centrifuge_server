function GraphSearchAddChosen(vizuuid,type,idList){
	this.vizuuid = vizuuid;
	this.type = type;
	this.idList = idList;
}

GraphSearchAddChosen.prototype.doTask = function(index) {
	var vizUuid = this.vizuuid;
	var type = this.type;
	var requestJSON = JSON.stringify(this.getRequestJson(type,index));
	var url = "/Centrifuge/services/graphs2/actions/visualizeNodes?_f=json&vduuid="+vizUuid;	
	$.ajax({
		type : "POST",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: requestJSON,
		success: function(data) {			
			var fitToSize = new FitToSize(vizUuid);
			fitToSize.doTask();
		},
		error: function(data) {
		// alert ("Error");
		}
	});
}

GraphSearchAddChosen.prototype.getRequestJson = function(type,index){
	var requestJson = new Object();
	requestJson['allLinks'] = false ;
	requestJson['allNodes'] = false ;
	requestJson['class'] = "csi.server.common.dto.graph.GraphRequest";
	requestJson['links'] = null;
	requestJson['nodes'] = null;
	if(type == "links"){
		requestJson['links'] = this.idList ;
	}
	else if(type == "nodes"){
		requestJson['nodes'] = this.idList ;
	}
	requestJson['useSearchResults'] = true;
	return requestJson;
	
}