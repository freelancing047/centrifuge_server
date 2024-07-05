function DragEnd(vizuuid){
	this.vizuuid=vizuuid;
	this.selectRegion = new SelectRegion(vizuuid);
	this.selectPan = new SelectPan(vizuuid);
	this.zoomToRegion = new ZoomToRegion(vizuuid);
}

DragEnd.prototype.doTask = function (img, selection) {

	if (img._mousemoveTimer) {
		clearTimeout(img._mousemoveTimer);
	}
	if (selection.width > 0 && selection.height > 0) {
		var action = $.data(img, "mode");
		if (action == 'zoom') {
			this.zoomToRegion.doTask(selection.x1, selection.y1, selection.x2, selection.y2);
			
		} else if (action == 'select') {
			var reset = true;
			if (document._keys && document._keys.ctrlKey) {
				reset = false;
			}
			this.selectRegion.doTask(selection.x1, selection.y1, selection.x2, selection.y2, reset);
			
		} else if (action == 'pan') {
			var minfo = img._mouseinfo;
			var dx = selection.width;
			var dy = selection.height;
			
			if (dx == 0 && dy == 0) {
				return;
			}
			
			if (minfo.startx != selection.x1) {
				dx = -dx;
			}
			
			if (minfo.starty != selection.y1) {
				dy = -dy;
			}
			this.selectPan.doTask(dx, dy);
		}
	}
	$(img).imgAreaSelect({ hide: true });
	img._mouseinfo.regionSelecting = false;
}
