function MouseMoveRG(vizuuid){
	this.vizuuid=vizuuid;
	this.findItemRG = new FindItemRG(vizuuid);
}

MouseMoveRG.prototype.doTask =function(event) {
	var vizuuid = this.vizuuid;
	var findItemRG = this.findItemRG;
	document._keys = {
            ctrlKey: event.ctrlKey,
            shiftKey: event.shiftKey,
            keyCode: event.keyCode
    };
	event.preventDefault();
	  
  	var img = event.currentTarget || event.target;
  
  	if (img._mousemoveTimer) {
		clearTimeout(img._mousemoveTimer);
  	}

  	if (img._mouseinfo.regionSelecting == true) {
		return;
  	}
  
  	if (event.ctrlKey || event.shiftKey || event.altKey || event.button) {
		return;
  	}
  
  	img._mousemoveTimer = setTimeout( function() {
		var location = $(img).offset();
	  	var x = event.pageX - location.left;
	  	var y = event.pageY - location.top;
	  	var hoverx = img._mouseinfo.hoverx;
	  	var hovery = img._mouseinfo.hovery;
	  
	  	if (hoverx != x && hovery != y) {
		  	img._mouseinfo.hoverx = x;
		  	img._mouseinfo.hovery = y;
		  	findItemRG.doTask(x, y, vizuuid);
	  	}
  	}, 1000);
}
