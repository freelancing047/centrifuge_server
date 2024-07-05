function SelectTool(vizId){
	this.vizId = vizId;
	this.action = 'cursor';
}

SelectTool.prototype.doTask = function(){
	var vizid = this.vizId;
	var image = $('#' + vizid)[0];
	$(document).bind('keydown',function(event){
		if (event.keyCode == 17){
			$('#' + vizid).css('cursor', 'crosshair');
			$.data(image,'mode','select');
		}
	});
	$(document).bind('keyup',function(event){
		if (event.ctrlKey){
			$('#' + vizid).css('cursor', 'default');
			$.data(image,'mode','cursor');
		}
	});
	var draggraph = new DragGraph(this.vizId);
	draggraph.setImageAreaSelection(image);
}

