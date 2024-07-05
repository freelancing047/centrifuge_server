function NodeHideUnhideTask(vizuuid,elementId){
	this.vizuuid = vizuuid;
	this.elementId = elementId;
}
NodeHideUnhideTask.prototype.doTask = function(e) {
	var type = "node";
	var flag = false;
	var dvUuid = window.dataview.myData.resultData.uuid;;
	var vizUuid = this.vizuuid;
	var elementId = this.elementId;
	if ($($(e.target)).attr("checked") == "checked") {
		flag = true;
	} else {
		flag = false;
	}
	e.stopPropagation();

	var url = "/Centrifuge/services/graphs2/actions/dohideUnhideTask?_f=json&vduuid="+vizUuid+"&dvuuid="+dvUuid+"&id="+elementId+"&type="+type+"&flag="+flag;
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			new RefreshImage(vizUuid).doTask(); 
			var listNodes = $.data($('#nodes-table' + vizUuid)[0], "controller");
			listNodes.doFetch();
			var listLinks = new ListLinks(vizUuid);
			listLinks.doTask();
		},
		error: function(data) {
		// alert ("Error");
		}
	});
}