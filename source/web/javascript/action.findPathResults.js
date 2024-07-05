function FindPathResults(vizUuid, startNode, endNode, numPaths, minLength, maxLength){
	this.vizUuid=vizUuid;
	this.startNode=startNode;
	this.endNode=endNode;
	this.numPaths=numPaths;
	this.minLength=minLength;
	this.maxLength=maxLength;
}
FindPathResults.prototype.doTask=function(){	
	var vizuuid = this.vizUuid;
	var startnode = this.startNode;
	var endnode = this.endNode;
	var numpaths = this.numPaths;
	var minlength=this.minLength;
	var maxlength=this.maxLength;
	var includedirection=$('#include-direction'+vizuuid).attr('checked')?true:false;
	var url = "/Centrifuge/services/graphs2/actions/findPaths?vduuid="+vizuuid+"&startNode="+startnode+"&endNode="+endnode+"&numPaths="+numpaths+"&minLength="+minlength+"&maxLength="+maxlength+"&includeDirection="+includedirection+"&_f=json"
	$.ajax({
		type: "POST",
		processData: false,
		url: url,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {	
			//display paths		
			$('#find-path-tab-header' + vizuuid + ' a:last').tab('show');
			var listPaths=new ListPaths(vizuuid);
			listPaths.doTask(data);			
		},
		error: function(data) {
		// alert ("Error");
		}
	});
}