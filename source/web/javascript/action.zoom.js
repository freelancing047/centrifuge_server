function Zoom(vizuuid) {
    this.vizuuid = vizuuid;
    this.zoomPercent = 0;
    this.zoomer;
}

Zoom.prototype.refreshImage = _.debounce(function() {
    console.log("zooming... " + this.zoomPercent);
    this.zoom(this.zoomPercent);
    this.zoomPercent = 0;
}, 500);

Zoom.prototype.zoomInOut = function(zoom, delta) {
    console.log("zoomin out... " + zoom.zoomPercent);
    if (delta > 0){
    	delta = 1/9;//1/9
    } else if (delta < 0){
    	delta = -1/35;//1/35
    }
    zoom.zoomPercent += delta;
    var z = 1.0 + zoom.zoomPercent;
    this.zoomCanvas(z);
    zoom.refreshImage();
};

Zoom.prototype.doRegister = function () {
    var zoom = this;
    $('#'+this.vizuuid).mousewheel(function (event, delta) {
        event.preventDefault();
        if (delta > 0){
        	delta = 1/9;//1/9
        } else if (delta < 0){
        	delta = -1/35;//-1/35
        }
        zoom.zoomPercent += delta;
        // When we are zooming in by 10%, the zoom factor will be 0.1,
        // for example, and so the size of the image should be 110% of
        // the container's size (a factor of 1 + z). At the same time,
        // the image should be remain horizontally and vertically
        // centered.
        var z = 1.0 + zoom.zoomPercent;
        console.log(zoom.zoomPercent);
        zoom.zoomCanvas(z);
        zoom.refreshImage();
    });
    $('#plus-button' + this.vizuuid).mousedown(function (event) {
    	if (event.which != 1){
    		return;
    	}
        event.preventDefault();
        var doZoom = function(zoom) {
    		zoom.zoomInOut(zoom, 1);
    	};
        zoom.zoomer = setInterval(function(){doZoom(zoom)},100);
     });
    
    $('#plus-button' + this.vizuuid).mouseout(function (event) {
        event.preventDefault();
        clearInterval(zoom.zoomer);
     });
    $('#plus-button' + this.vizuuid).mouseup(function (event) {
        event.preventDefault();
        clearInterval(zoom.zoomer);
     });
    $('#minus-button' + this.vizuuid).mousedown(function (event) {
    	if (event.which != 1){
    		return;
    	}
        event.preventDefault();
        var doZoom = function(zoom) {
    		zoom.zoomInOut(zoom, -1);
    	};
        zoom.zoomer = setInterval(function(){doZoom(zoom)},100);
     });
    
    $('#minus-button' + this.vizuuid).mouseout(function (event) {
        event.preventDefault();
        clearInterval(zoom.zoomer);
     });
    $('#minus-button' + this.vizuuid).mouseup(function (event) {
        event.preventDefault();
        clearInterval(zoom.zoomer);
     });    
    
};
Zoom.prototype.zoom = function(percent) {
	var vizuuid = this.vizuuid;
	var doSuccess = function(vizuuid) {
		return function(data) {
			new RefreshImage(vizuuid).doTask();
			$('#'+ vizuuid).css('zoom', "100%");
		};
	};
	var scale = 0;
	scale = 1 + percent;
	csi.relgraph.zoomTo(vizuuid, scale, {
		onsuccess : doSuccess(vizuuid)
	});
}
Zoom.prototype.zoomCanvas = function(scale) {
	var canvas = $('#' + this.vizuuid)[0];
	var img = canvasImageContainer[this.vizuuid];
	var context = canvas.getContext("2d");
	context.webkitImageSmoothingEnabled = true;
	context.mozImageSmoothingEnable = true;
	var w = canvas.width;
	var h = canvas.height;
	context.setTransform(1, 0, 0, 1, 0, 0);
	context.clearRect(0, 0, w, h);
	context.translate((w - w*scale)/2, (h - h*scale)/2); // To move the context to a middle position
	context.scale(scale, scale);
	context.drawImage(img, 0, 0);
}
