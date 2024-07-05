function UnBundleSingleNode(vizuuid, nodeId){
	this.vizuuid=vizuuid;
	this.nodeId = nodeId;
}
UnBundleSingleNode.prototype.doTask = function(){
	var vizuuid = this.vizuuid;
	var visualization = utils.getVisualisation(this.vizuuid).visualization;
	if (visualization.position == 0 || visualization.position == undefined){
		var listNodes = $.data($('#nodes-table' + vizuuid)[0], "controller");
	}
	var url ="/Centrifuge/services/graphs2/actions/unbundleSingleNode?vduuid="+vizuuid+"&id="+this.nodeId+"&_f=json";
	$.ajax({
		type : "POST",
		processData : false,
		url : url,
		contentType : 'application/json; charset=utf-8',
		dataType : 'json',
		success: function(data) {
			if (visualization.position == 0 || visualization.position == undefined){
				listNodes.doFetch();
			}
			new RefreshImage(vizuuid).doTask();
			var getGraphFlags = new GetGraphFlags(vizuuid);
			getGraphFlags.doTask();
		},
		error: function(data) {
		//          alert ("Error");
		}
	});
}
