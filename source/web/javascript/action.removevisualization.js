function RemoveVisualization(){
	
}

RemoveVisualization.prototype.doTask = function(vizUuid) {
    var viz = utils.getVisualisation(vizUuid);
    var ws = utils.getWorksheet4Visualization(vizUuid);
    var smallPanelView = {
				workSheetIndex: ws.index,
				vizPosition: ws.position = (viz.visualization.position == undefined ? 0 : viz.visualization.position)
		};
    var url = "/Centrifuge/actions/viz/removeVisualization?_f=json&dvUuid=" +  window.dataview.myData.resultData.uuid  + "&worksheetUuid=" + ws.worksheet.uuid + "&vizUuid="+ vizUuid;
    
        $.ajax({
		type : "GET",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			$('#layout' + ws.index+"_panel" + smallPanelView.vizPosition).html(Mustache.render($('#small-panel-empty-layout').html(), smallPanelView));	
		},
		error: function(data) {
		
		}
	});
}
RemoveVisualization.prototype.deleteMainViz = function(vizUuid) {
	var viz = utils.getVisualisation(vizUuid);
	var ws = utils.getWorksheet4Visualization(vizUuid);
	var obj = {};
	obj.index=ws.index;
	obj.position = (viz.visualization.position == undefined ? 0 : viz.visualization.position);
	obj.worksheetUuid = ws.worksheet.uuid;
	var emptyPanel = Mustache.render($('#empty-panel').html(), obj); 
	var mainPanel = $('#layout' + ws.index+"_panel"+obj.position).parent();
    var url = "/Centrifuge/actions/viz/removeVisualization?_f=json&dvUuid=" + window.dataview.myData.resultData.uuid + "&worksheetUuid=" + obj.worksheetUuid + "&vizUuid="+ vizUuid;
    $.ajax({
		type : "GET",		
		processData: false,
		url: url ,
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(data) {
			emptyPanel = $(emptyPanel).css("height", $('#layout' + ws.index+"_panel"+obj.position).css("height"));
			$('#layout' + ws.index+"_panel"+obj.position).remove();
			$(mainPanel).append(emptyPanel);
		},
		error: function(data) {
		}
	});
}



RemoveVisualization.prototype.confirmDeleteVisualization = function(vizUuid) {
	bootbox.dialog("Are you sure that you want to delete the visualization?", [{
		"label" : "Delete Visualization",
		"class" : "btn-primary",
		"callback" : function() {
			var rv = new RemoveVisualization(); 
			rv.deleteMainViz(vizUuid);
		}
	}, {
		"label" : "Cancel",
		"class" : "null",
		"callback" : function() {
		}
	}]);
}

