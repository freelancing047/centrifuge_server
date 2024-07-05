function DragGraph(vizuuid) {
	this.vizuuid = vizuuid;
	this.dragStart = new DragStart(vizuuid);
	this.dragEnd = new DragEnd(vizuuid);
	this.displayID = vizuuid;
}

DragGraph.prototype.doRegister = function() {
	this.setDragMode("cursor");
}

DragGraph.prototype.setDragMode = function(mode) {
    var img = $('#'+this.vizuuid)[0];
    if (!img) {
   		return;
	}
	$.data(img, "mode", mode);
	if (mode == 'pan') {
		$('#' + this.displayID).css('cursor', 'move');
	} else if (mode == 'select' || mode == 'zoom') {
		$('#' + this.displayID).css('cursor', 'crosshair');
	} else if (mode == 'cursor') {
		$('#' + this.displayID).css('cursor', 'auto');
	}
	return false;
}
DragGraph.prototype.setImageAreaSelection = function(img) {
	var dragGraph = this;
	var dragStart = this.dragStart;
	var dragEnd = this.dragEnd;
	var doSelectEnd = function(dragEnd) {
		return function(img, selection) {
			dragEnd.doTask(img, selection);
		}
	};
	var doSelectStart = function(dragStart) {
		return function(img, selection) {
			dragStart.doTask(img, selection);
		}		
	};
	$(img).imgAreaSelect({
		handles : false,
		onSelectStart : doSelectStart(dragStart),
		onSelectEnd : doSelectEnd(dragEnd)
	});
}
DragGraph.prototype.pan = function(dx, dy) {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
		};
	};
	csi.relgraph.pan(vizuuid, dx, dy, {
		onsuccess : doSuccess()
	});
}
DragGraph.prototype.selectRegion = function selectRegion(x1, y1, x2, y2, reset) {
	var vizuuid = this.vizuuid;
	var doSuccess = function() {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
		};
	};
	csi.relgraph.selectRegion(vizuuid, x1, y1, x2, y2, reset, {
		onsuccess : doSuccess()
	});
}
