function SaveGraph(vizuuid){
	this.vizuuid=vizuuid;	
}
SaveGraph.prototype.doTask=function(){
	var vizuuid = this.vizuuid;	
	var url = "/Centrifuge/services/graphs2/actions/saveGraph?vduuid="+vizuuid+"&_f=json";
	$.ajax({
		type : "POST",
		processData : false,
		url : url,
		contentType : 'application/json; charset=utf-8',
		dataType : 'json',
		success :function(data) {	
			new RefreshImage(vizuuid).doTask();
		},
		error : function(data) {
			// alert ("Error");
		}
	});
}