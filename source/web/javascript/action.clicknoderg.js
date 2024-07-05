function ClickNodeRG(vizuuid,element,elementid){
	this.vizuuid = vizuuid;
	this.element = element;
	this.elementid=elementid;
}

ClickNodeRG.prototype.doTask = function (x, y) {
	var vizuuid = this.vizuuid;
	var element = this.element;
	var elementid = this.elementid;
	var nx = x;
	var ny = y;
	var url = "/Centrifuge/services/graphs2/actions/findNodeMeta?vduuid="+vizUuid+"&x="+nx+"&y="+ny+"&_f=json";
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			if (data.resultData.available) {
				var nodeId = data.resultData.id;
				var label = data.resultData.label;
				$('#'+ element)[0].textContent = label;
				$('#'+ elementid)[0].textContent = nodeId;
			}
		},
		error: function(data) {
		// alert ("Error");
		}
	});
}