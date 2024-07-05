function Select(vizuuid, allLinks, allNodes, idList, searchInNodeOrLink){
	this.vizuuid = vizuuid;
	this.allLinks = allLinks;
	this.allNodes = allNodes;
	this.idList = idList;
	this.searchInNodeOrLink = searchInNodeOrLink;
}
Select.prototype.doTask = function(){
	var requestJson = JSON.stringify(this.getRequestJson());
	var url = "/Centrifuge/services/graphs2/actions/select?_f=json&resetNodes=false&resetLinks=false&vduuid="+this.vizuuid;
	var vizuuid = this.vizuuid;
	var graphSearch = this;
	$.ajax({
		type : "POST",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		data: requestJson,
		complete : function(data){
			var fitToSelection = new FitToSelection(vizuuid);
			fitToSelection.doTask();
		}
	});
}
Select.prototype.getRequestJson = function(){
	var requestJson = new Object();
	requestJson['allLinks'] = this.allLinks;
	requestJson['allNodes'] = this.allNodes;
	requestJson['class'] = "csi.server.common.dto.graph.GraphRequest";
	if(this.searchInNodeOrLink == "nodes"){
		requestJson['nodes'] = this.idList;
	}else{
		requestJson['links'] = this.idList;
	}
	requestJson['useSearchResults'] = true;
	return requestJson;
}
