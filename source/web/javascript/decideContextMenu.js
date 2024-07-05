function DecideContextMenu(vizuuid) {
	this.vizuuid = vizuuid;
}

DecideContextMenu.prototype.doTask = function(event) {
	var canvas = document.getElementById(this.vizuuid);
    var ctx = canvas.getContext('2d');
    var here = this;
	var canvasOffset = $(canvas).offset();
	//get mouse x and y from canvas only
    var canvasX = Math.floor(event.pageX - canvasOffset.left);
    var canvasY = Math.floor(event.pageY - canvasOffset.top);
    var imageData = ctx.getImageData(canvasX, canvasY, 1, 1);
    var pixel = imageData.data;
    //getting background pixel data and formatting to 'rgb(r,g,b)' mode
    this.pixelrgb = 'rgb(' + pixel[0] + ', ' + pixel[1] + ', ' + pixel[2] + ')';
    here.selectContextMenu(canvasX,canvasY);     
}

//This function compares the mouse clicked (x,y) color with canvas values.
DecideContextMenu.prototype.selectContextMenu = function(x,y) {
	//console.log("In decide :("+x+","+y+")");
	var contextMenu = this;
	//Assuming black color for link. rgb(0,0,0) turns out to be '0' (when converted to integer)
	var linkColor = 5263440;
	//Get the visualisation properties of canvas with vizuuid  
	var vizSettings = utils.getVisualisation(contextMenu.vizuuid);
	//getting rgb of background color.
	var savedBGColor = vizSettings.visualization.settings.propertiesMap['csi.relgraph.backgroundColor'];
	//converting rgb to int for easy comparison with viz settings
	var pickedColor = utils.rgb2int(this.pixelrgb);
	console.log("Picked color:"+pickedColor);
		
	//comparing with link color (Color is either black [val = 0] or shade of grey [val = 5263440])
	if(pickedColor == linkColor || pickedColor == 0){
		$.contextMenu({
			selector: "#"+contextMenu.vizuuid,
			build: function($trigger) {
				return {
					callback: function(key, options) {
						var m = "clicked: " + key;
						console.log(m);
					},
					items: $.contextMenu.fromMenu($('#contextMenuTemplateLink' + contextMenu.vizuuid))
				};
			}
		});
		var obj = new ContextMenuLinkFunction(contextMenu.vizuuid);
		//Get link
		obj.getLink(x,y);
		//Function to bind context menu options
		obj.doTask();
	}
	//comparing with background color
	else if (pickedColor == savedBGColor){
		$.contextMenu({
			selector: "#"+contextMenu.vizuuid,
			build: function($trigger) {
				return {
					callback: function(key, options) {
						var m = "clicked: " + key;
						console.log(m);
					},
					items: $.contextMenu.fromMenu($('#contextMenuTemplate' + contextMenu.vizuuid))
				};
			}
		});
		new ContextMenuFunction(contextMenu.vizuuid).doTask();
	}
	// since the background and link comparison returns false, the only option left out is a node/bundle
	else {
			
		new ContextMenuNodeFunction(contextMenu.vizuuid).doTask(x,y);
		
		$.contextMenu({
			selector: "#"+contextMenu.vizuuid,
			build: function($trigger) {
				return {
					callback: function(key, options) {
						var m = "clicked: " + key;
						console.log(m);
					},
					items: $.contextMenu.fromMenu($('#contextMenuTemplateNode' + contextMenu.vizuuid))
				};
			}
		});		
	}
	
}

