function GraphSearchAddAll(vizuuid,type){
	this.vizuuid = vizuuid;
	this.type = type;
}

GraphSearchAddAll.prototype.doTask = function() {
	var vizUuid = this.vizuuid;
	var type = this.type;
	var requestJSON = JSON.stringify(this.getRequestJson(type));
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

GraphSearchAddAll.prototype.getRequestJson = function(type){
	var requestJson = new Object();
	requestJson['allLinks'] = false ;
	requestJson['allNodes'] = false ;
	if(type == "links"){
		requestJson['allLinks'] = true ;
	}
	else if(type == "nodes"){
		requestJson['allNodes'] = true ;
	}
	requestJson['class'] = "csi.server.common.dto.graph.GraphRequest";
	requestJson['links'] = null;
	requestJson['nodes'] = null;
	requestJson['useSearchResults'] = true;
	return requestJson;
	
}