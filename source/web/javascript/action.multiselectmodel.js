function MultiSelectModel(vizuuid, type, idList) {
	this.vizuuid = vizuuid;
	this.type = type;
	this.idList = idList;
}

MultiSelectModel.prototype.doTask = function(resetNodes,resetLinks){
	var vizUuid = this.vizuuid;
	var type = this.type;
	var idList = this.idList;
	var requestJSON = JSON.stringify(this.getRequestJson(type, idList));
	var url = "/Centrifuge/services/graphs2/actions/select?_f=json&vduuid="+vizUuid+"&reset=null&resetNodes="+resetNodes+"&resetLinks="+resetLinks;	
	$.ajax({
		type : "POST",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: requestJSON,
		success: function(data) {
			var fitToSelection = new FitToSelection(vizUuid);
			fitToSelection.doTask();	
		},
		error: function(data) {
		
		}
	});
}
MultiSelectModel.prototype.getRequestJson = function(type,idList) {
	var requestJson = new Object();
	requestJson['allLinks'] = false ;
	requestJson['allNodes'] = false ;
	requestJson['class'] = "csi.server.common.dto.graph.GraphRequest" ;
	requestJson['links'] = null ;
	requestJson['nodes'] = null ;
	if(type == "links"){
		requestJson['links'] = idList ;
	}
	else if(type == "nodes"){
		requestJson['nodes'] = idList ;
	}
	requestJson["useSearchResults"] = false ;
	return requestJson;
}