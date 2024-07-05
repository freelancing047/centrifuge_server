
function FindItemRG(vizuuid){
	this.vizuuid = vizuuid;
}

FindItemRG.prototype.doTask = function (x, y, vizuuid) {
	var findItemRG = this;
	var findItemSuccess = function() {
		return function (data) {
			if (data.resultData) {
				var tips = new Array();
				if (data.resultData['csi.internal.URL']) {					
					data.resultData.tooltips["URL"]=data.resultData['csi.internal.URL'];					
				}
				if(data.resultData['csi.internal.Document']) {
					data.resultData.tooltips.Document = data.resultData['csi.internal.Document'];
				}
				if(data.resultData.tooltips.Type=="Link"){
					delete data.resultData.tooltips.Label;
					if(data.resultData.computed){
						 $.each(data.resultData.computed,function(key, value){
							 data.resultData.tooltips[key]=value.ALL;							 
						 });
					 }
					 if (data.resultData.direction.NONE) {
						 data.resultData.tooltips["Direction"]=data.resultData.direction.NONE;														
						}
					 else if(data.resultData.direction.FORWARD){
						 data.resultData.tooltips["Direction"]=data.resultData.direction.FORWARD;	
					 }
					 else if(data.resultData.direction.REVERSE){
						 data.resultData.tooltips["Direction"]=data.resultData.direction.REVERSE;						
					 }						
				}	
				
				var orderedtooltips = findItemRG.listTipsInOrder(data.resultData.tooltips);
				var createtipsrg = new CreateTipsRG(orderedtooltips);
				createtipsrg.createTips(orderedtooltips,tips, vizuuid);										       						
				var tippanel = $("#tooltipPanel" + vizuuid);
				tippanel.css('left', x + 5 -22);
				tippanel.css('top', y + 5 + 37);
				
				/* hack to get IE to use max/min width and height css style */
				var minw;
				var minh;
				var maxw;
				var maxh; 
				if (tippanel.currentStyle) {
					minw = tippanel.currentStyle.minWidth;
					minh = tippanel.currentStyle.minHeight;
					maxw = tippanel.currentStyle.maxWidth;
					maxh = tippanel.currentStyle.maxHeight;						
					tippanel.style.width = minw;
					tippanel.style.height = minw;
				}
				
				tippanel.html(tips.join(""));
				
				/* part of IE max/min hack */
				if (maxw && tippanel.width() > parseInt(maxw)) {
					tippanel.style.width = maxw;
				}					

				if (maxh && tippanel.height() > parseInt(maxh)) {
					tippanel.style.height = maxh;
				}
				
				tippanel.show(100);		
			} else {
				$("#tooltipPanel" + vizuuid).text("").hide(100);
			}
		}
	};
	csi.relgraph.findItemAt(vizuuid, x, y, {
		onsuccess: findItemSuccess()			
	});
}
FindItemRG.prototype.listTipsInOrder = function(tooltips){
	var toolTips = tooltips;
	var tips = {};
	if(toolTips.Label){
		tips.Label = toolTips.Label;
		delete toolTips.Label;
	}
	tips.Type = toolTips.Type;
	delete toolTips.Type;
	if(tips.Type == "Bundle"){
		if(toolTips["Member Types"]){
			tips["Member Types"] = toolTips["Member Types"];
			delete toolTips["Member Types"];
		}  
		if(toolTips.Contents){
			if(toolTips.Contents instanceof Array){
				tips.Contents = toolTips.Contents.sort();
			}
			else{
				tips.Contents = toolTips.Contents;
			}
			delete toolTips.Contents;
		}
	}
	if(toolTips.ID){
		tips.ID = toolTips.ID;	
		delete toolTips.ID;
	}
	if(toolTips.URL){
		tips.URL = toolTips.URL;
		delete toolTips.URL;
	}
	if(toolTips.Document){
		tips.Document = toolTips.Document;
		delete toolTips.Document;
	}
	toolTips = this.sortObject(toolTips);
	$.each(toolTips,function(key,value){
		if(value instanceof Array){
			value = value.sort();
		}
		tips[key] = value;
	});
	return tips;
	
}  
FindItemRG.prototype.sortObject = function(o) {
    var sorted = {},
    key, a = [];

    for (key in o) {
        if (o.hasOwnProperty(key)) {
                a.push(key);
        }
    }

    a.sort();

    for (key = 0; key < a.length; key++) {
        sorted[a[key]] = o[a[key]];
    }
    return sorted;
}
