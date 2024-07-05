function SelectPan(vizuuid){
	this.vizuuid=vizuuid;
}

SelectPan.prototype.doTask =  function (dx, dy) {
	var doSuccess = function(vizuuid){
		return function(data){
			new RefreshImage(vizuuid).doTask();
		};
	};
	this.panCanvas(dx, dy);
	csi.relgraph.pan(this.vizuuid, dx, dy, {
		onsuccess: doSuccess(this.vizuuid)
	});
}
SelectPan.prototype.panCanvas =  function (dx, dy) {
	var canvas = $('#' + this.vizuuid)[0];
	var img = canvasImageContainer[this.vizuuid];
	var context = canvas.getContext("2d");
	context.webkitImageSmoothingEnabled = true;
	context.mozImageSmoothingEnable = true;
	var w = canvas.width;
	var h = canvas.height;
	context.setTransform(1, 0, 0, 1, 0, 0);
	context.clearRect(0, 0, w, h);
	context.translate(dx, dy); // To move the context to a middle position
	context.drawImage(img, 0, 0);
	
}
