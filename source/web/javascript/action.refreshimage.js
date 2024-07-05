function RefreshImage(vizuuid){
       this.vizuuid = vizuuid;
}

RefreshImage.prototype.doTask = function(){
       this.refresh();
}
RefreshImage.prototype.refresh = function(){
	var refreshImg = this;
       var dvuuid = window.dataview.myData.resultData.uuid;
       var img = $('#'+this.vizuuid)[0];
       if (!img) {
   		return;
   	   }
       var viz = utils.getVisualisation(this.vizuuid).visualization;
       var ws = utils.getWorksheet4Visualization(this.vizuuid);
       var position = viz.position;
       var layout = ws.worksheet.layout;
       if (position == 0 || position == undefined || layout == "EQUAL LAYOUT"){
    	   this.getLegendData();
       } else {
    	   
       }
       if(img){
           var container = img.parentNode;
           var w = container.offsetWidth;
           var h = container.offsetHeight;
           if (!(position == 0 || position == undefined || layout == "EQUAL LAYOUT")){
        	   w = $('.small-panel-td .vizPanels:visible').width();
        	   h = 270;
           } else {
           	   w = $('.main-panel-td .vizPanels:visible').width();
           	   h = $('.main-panel-td .vizPanels:visible').height();
           }
           
           
           // For loading canvas
           var rgCanvas = img;
           var context = rgCanvas.getContext("2d");
           var imgCanvas = new Image();
           var imgSrc = csi.relgraph.graphImageUrl(dvuuid, this.vizuuid, w, h);
	           
			$(imgCanvas).load(function() {
				rgCanvas.width = w;
				rgCanvas.height = h;
				
				canvasImageContainer[refreshImg.vizuuid] = imgCanvas;
				context.drawImage(this, 0, 0);
				var viz = utils.getVisualisation(rgCanvas.id).visualization;
				var ws = utils.getWorksheet4Visualization(rgCanvas.id);
				var position = viz.position;
				var layout = ws.worksheet.layout;
				var newPanelId = 'layout' + ws.index + '_panel' + viz.position;
				if (!(position == 0 || position == undefined || layout == "EQUAL LAYOUT")) {
					$(rgCanvas).css({
						"width" : "100%",
						"top" : 0,
						"left" : 0
					});
				}
				$("#" + newPanelId + " .toggle_container").find(".progress_loader").hide();
			}); 

           imgCanvas.src = imgSrc;
           $("#tooltipPanel" + this.id).text("").hide(100);
       }
}
RefreshImage.prototype.getLegendData = function() {
    var vizuuid = this.vizuuid;
    var selectNodesByType = new SelectNodesByType(vizuuid); // For selecting nodes
    // when clicking on
    // legend data.
    var getLegend = function(vizuuid) {
        return function(data) {
			var legend = [];
			var lid = 'legendPanel' + vizuuid;
			var lbid = 'legendBody' + vizuuid;
			if ($("#" + lbid).length == 0){
				return;
			}
			$("#" + lbid).html('');
			$(data.resultData.nodeLegendItems).each(function() {
				var shapeSource = '/Centrifuge/WidgetControllerServlet?action=render&shape='+ this.shape+ '&color='+ this.color+ '&image=' + this.iconURI;
				legend.push($('<span></span>').attr('class','legendItem').append($('<img></img>').attr('class','legendImage').attr('onclick','selectNodesByType.doTask(event);return false;').attr('src',shapeSource).append('&nbsp; ')).append
				($('<a></a>').attr('onclick','selectNodesByType.doTask(event);return false;').append(this.typeName)).append('( '+this.count+' of '+this.totalCount+' )').after('</br>'));
			});
			$("#" + lbid).append(legend);
			$("#" + lid).show();
        };
    };
    csi.relgraph.legendData(vizuuid, {
        onsuccess : getLegend(vizuuid)
    });
}  
