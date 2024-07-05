function DragStart(vizuuid) {
	this.vizuuid = vizuuid;
}

DragStart.prototype.doTask = function(img, selection) {
	img._mouseinfo.regionSelecting = true;
	img._mouseinfo.startx = selection.x1;
	img._mouseinfo.starty = selection.y1;

	if (img._mousemoveTimer) {
		clearTimeout(img._mousemoveTimer);
	}
	var action = $.data(img, "mode");
	if (action == 'pan') {
		$(img).imgAreaSelect({
			hide : true
		});
	} else if (action == 'select' || action == 'zoom') {
		$(img).imgAreaSelect({
			hide : false
		});
	} else if (action == 'cursor') {
		// Do nothing
	} else {
		$.data(img, "mode", 'cursor');
	}
}
