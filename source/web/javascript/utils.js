function Utils() {
}
Utils.prototype.guidGenerator = function() {
	var S4 = function() {
		return (((1 + Math.random()) * 0x10000) | 0).toString(16)
				.substring(1);
	};
	return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4()
			+ S4() + S4());
}
Utils.prototype.getFieldDefByStaticText = function(fieldDefs, staticText) {
	var fldDef = null;
	$.each(fieldDefs, function(index, value) { 
		  if (value["staticText"] == staticText ) {
			  fldDef = value;
			  return;
		  }
	});
	return JSON.parse(JSON.stringify(fldDef));
}
Utils.prototype.getFieldDef = function(fieldDefs, fieldName) {
	var fldDef = null;
	$.each(fieldDefs, function() { 
		  if (this.fieldName == fieldName ) {
			  fldDef = this;
			  return;
		  }
	});
	return JSON.parse(JSON.stringify(fldDef));
}
Utils.prototype.getNodeDefIdx = function(nodeDefs,
		nodeName) {
	var nodeIdx = null;
	$.each(nodeDefs, function(index, value) {
		if (value["name"] == nodeName) {
			nodeIdx = index;
			return;
		}
	});
	return nodeIdx;
}
Utils.prototype.getAlphaNumericString = function(stringToFormat) {
	return stringToFormat.replace(/[^a-z0-9]/gi, '');
}
Utils.prototype.rgb2int = function(rgb) {
	rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
	var hex = ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
	return parseInt(hex,16);
}
Utils.prototype.rgbComp2Int = function(r, g, b) {
	var hex = ("0" + parseInt(r,10).toString(16)).slice(-2) + ("0" + parseInt(g,10).toString(16)).slice(-2) + ("0" + parseInt(b,10).toString(16)).slice(-2);
	return parseInt(hex,16);
}
Utils.prototype.int2hex = function(val) {
	var hex = parseInt(val).toString(16).toUpperCase();
	var pad = 6 - hex.length;
	while(pad > 0) {
		hex = "0" + hex;
		pad--;
	}
	return hex;
}
Utils.prototype.hexToRGBComponent = function(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : {r: 0, g: 0, b:0};
}
Utils.prototype.getVisualisation = function(vuuid) {
	var viz = null;
	var index = 0;
	var wrkAry = window.dataview.myData.resultData.meta.modelDef.worksheets;
	$.each(wrkAry, function() {
		$.each(this.visualizations, function() {
			if (this.uuid == vuuid) {
				viz = {};
				viz.visualization = this;
				viz.index = index;
				return;
			}
			index++;
		});
	});
	return viz;
}
Utils.prototype.getVisualisation4mWorksheet = function(wuid, vuuid) {
	var viz = null;
	var vizAry = this.getWorksheet4Visualization(vuuid).worksheet.visualizations;
	$.each(vizAry, function(index, visualization) {
		if (this.uuid == vuuid) {
			viz = this;
			return;
		}
	});
	return viz;
}
Utils.prototype.getWorksheet4Visualization = function(vuuid) {
	var wrkSht = null;
	var wrkAry = window.dataview.myData.resultData.meta.modelDef.worksheets;
	$.each(wrkAry, function(index, ws) {
		$.each(ws.visualizations, function() {
			if (this.uuid == vuuid) {
				wrkSht = {};
				wrkSht.worksheet = ws;
				wrkSht.index = index;
				return;
			}
		});
		if (wrkSht != null){
			return;
		}
	});
	return wrkSht;
}
Utils.prototype.getWorksheet = function(wid) {
	var wrkSht = null;
	var wrkAry = window.dataview.myData.resultData.meta.modelDef.worksheets;
	$.each(wrkAry, function(index, ws) {
		if (ws.uuid == wid){
			wrkSht = ws;
			return;
		}
	});
	return wrkSht;
}

Utils.prototype.getWorksheetIndex = function(wid) {
       var idx = null;
       var wrkAry = window.dataview.myData.resultData.meta.modelDef.worksheets;
       $.each(wrkAry, function(index, ws) {
               if (ws.uuid == wid){
                       idx = index;
                       return;
               }
       });
       return idx;
}
Utils.prototype.getMainVisualisation = function(wid) {
	var viz = null;
	var ws = this.getWorksheet(wid); 
	$.each(ws.visualizations, function() {
		if (this.position == 0 || this.position == null){
			viz = this;
			return;
		}
	});
	return viz;
}
Utils.prototype.drawLayout = function(relImage, ignoreLoadOnStartup, bLoad) {
	var dataview = window.dataview;
	var utils = this;
	var doLayout = function() {
		var vizIndex = relImage.vizIndex;
		var wkstIndex = relImage.wsIndex;
		var worksheet = relImage.ws;
		if (!ignoreLoadOnStartup) {
			var visualization = utils.getVisualisation(relImage.viz).visualization;
			var loadOnStartup = visualization.clientProperties["vizBox.loadOnStartup"];
			if (!loadOnStartup) {
				dataview.generateLoadPanel(relImage, wkstIndex);
				return;
			}
		}
		if((worksheet.layout == "LEFT HAND LAYOUT") || (worksheet.layout == "RIGHT HAND LAYOUT") || (worksheet.layout == undefined)) {
			if ((relImage.position == "0") || (relImage.position == null)) {
				var rgv = new RGView(dataview.myData.resultData.uuid, relImage.viz, vizIndex, relImage.nodes, relImage.links, relImage.nodeDefsMap, relImage.linkDefsMap);
				var rgPanel = new RGPanelFullWidth(rgv, vizIndex, relImage);
				dataview.generateRelPanel(rgPanel, relImage, rgv);
			} else {
				dataview.generateRelPanelSmall(relImage);
			}
		} else if((worksheet.layout == "EQUAL LAYOUT") || (worksheet.layout == "SINGLE LAYOUT")) {
			var rgv = new RGView(dataview.myData.resultData.uuid, relImage.viz, vizIndex, relImage.nodes, relImage.links, relImage.nodeDefsMap, relImage.linkDefsMap);
			var rgPanel = new RGPanelFullWidth(rgv, vizIndex, relImage);
			dataview.generateRelPanel(rgPanel, relImage, rgv);
		}
	};
	csi.relgraph.loadGraph(dataview.myData.resultData.uuid, relImage.viz, {
		onsuccess: function(data) {
			doLayout();
		}			
	});
}
Utils.prototype.getNodesList = function(nodeDefs){
	var nodes = new Array();
	$.each(nodeDefs, function(index,nodeDef){
		nodes.push(nodeDef['name']);
	});
	return nodes;
}
Utils.prototype.getLinksList = function(linkDefs){
	var links = new Array();
	$.each(linkDefs, function(index,linkDef){
		var link = new Object();
		link['node1'] = linkDef['nodeDef1']['name'];
		link['node2'] = linkDef['nodeDef2']['name'];
		link['uuid'] = linkDef['uuid'];
		links.push(link);
	});
	return links;
}
Utils.prototype.getNodeDefsMap = function(nodeDefs){
	var nodeDefsMap = new Object();
	$.each(nodeDefs, function(index, nodeDef){
		nodeDefsMap[nodeDef['name']] = nodeDef;
	});
	return nodeDefsMap;
}

Utils.prototype.getLinkDefsMap = function(linkDefs){
	var linkDefsMap = new Object();
	$.each(linkDefs, function(index, linkDef){
		linkDefsMap[linkDef['uuid']] = linkDef;
	});
	return linkDefsMap;
}
Utils.prototype.getRandomShape = function() {
	var shapes = ['Circle', 'Diamond', 'Hexagon', 'Pentagon', 'Octagon', 'Pentagon/House', 'Square', 'Star', 'Triangle'];
	return shapes[Math.floor(Math.random() * shapes.length)];
	
}
Utils.prototype.getRandomColor = function() {
	var rgb = this.hexToRgb(Math.floor(Math.random()*16777215).toString(16));
	return this.rgb2int(rgb);
}
Utils.prototype.hexToRgb = function hexToRgb(hex) {
	return 'rgb('+parseInt(hex.substring(0,2),16)+','+parseInt(hex.substring(2,4),16)+','+parseInt(hex.substring(4,6),16)+')';     
}
Utils.prototype.getAttributeDefIdx = function(attributeDefs,
		name) {
	var attributeDefIdx = null;
	$.each(attributeDefs, function(index, value) {
		if (value["name"] == name) {
			attributeDefIdx = index;			
		}
	});
	return attributeDefIdx;
}
Utils.prototype.getlinkDefIdx = function(linkDefs, sourceNode, targetNode) {
	var linkIdx = null;
	var sourceNode = sourceNode;
	var targetNode = targetNode;
	$.each(linkDefs, function(index,link) {
		if (link.nodeDef1.name == sourceNode && link.nodeDef2.name == targetNode) {
			linkIdx = index;
			return;
		}
	});
	return linkIdx;
}
Utils.prototype.getQueryParamsFromURL = function(url){
	var params = {};
	$.each(url.split('&'), function(index, value) {
		if(value.indexOf('shape') != -1) {
			params.shape = value.substring(6);
		}else if(value.indexOf('color') != -1){
			params.color = value.substring(6, value.length);
		}
	});
	return params;
}
Utils.prototype.getChangedItemsWithPosition = function(array1, array2) {
	var changedItems = [];
	$.each(array1, function(index, item) {
		if(item != array2[index]) {
			changedItems.push(item);
		}
	});
	return changedItems;
}
Utils.prototype.showLoadingIndicator = function(wsIndex, vizPosition) {
	panelId = "layout" + wsIndex + "_panel" + vizPosition;
	$('#' + panelId).find('.addVisualization').hide();
	$('#' + panelId).find('.zoom-element > img').hide();
	$('#' + panelId).find('.relGraphImage').hide();
	$('#' + panelId).find('.progress_loader').show();
	
}
Utils.prototype.getRandomPositionForNodes = function() {
	var randPosition = {};
	var parentPosition = $('#node_diagram').offset();
	var leftStart = parentPosition.left - 72 + 20;
	var leftEnd = leftStart - 72 + $('#node_diagram').width() - 30;
	var topStart = parentPosition.top + 10;
	var topEnd = parentPosition.top - 72 + $('#node_diagram').height() - 40;
	randPosition.left = this.randomFromInterval(leftStart, leftEnd);
	randPosition.top = this.randomFromInterval(topStart, topEnd);
	return randPosition;
}
Utils.prototype.randomFromInterval = function(from, to) {
	var diff = to - from;
	var rand = Math.random();
	return Math.floor((rand * (diff + 1)) + from);
}
Utils.prototype.getEncodedImageURL = function(imageURL) {	
	return encodeURI(imageURL);
}
Utils.prototype.getColumns = function() {
	var columnNames = [];
	if (this.myData == null){
		return;
	}
	if (window.dataview.resultData.meta.modelDef.fieldDefs.length > 0) {
		$.each(window.dataview.resultData.meta.modelDef.fieldDefs, function(index, value) {
			columnNames[index] = {
							        val: index,
							        text: this.fieldName
							    };
		});
	}
	return columnNames;
};
Utils.prototype.getIconURL = function(nodeName) {		
	var icon;
	var shape;
	var color;
	var currentTheme = $('#theme').val();
	var themeOptionXML;
	var themeURL = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/" + currentTheme + ".xml?asXML";
	$.ajax({
		url: themeURL,
		async : false,
		complete: function(data) {			
			themeOptionXML = data.responseText;	
                       
		}
	});
	var themeBasedAttr = $(themeOptionXML).find('NodeType').filter(function() {
	    return $(this).attr('nodetype').toLowerCase() == nodeName.toLowerCase() ;
	});
	var nodeImageURL = "/Centrifuge/WidgetControllerServlet?action=render";
	if (themeBasedAttr.length > 0) {
		shape = themeBasedAttr.attr('shape');		
		nodeImageURL += "&shape=" + shape;		
		color = themeBasedAttr.attr('color');
		if (color != "") {
			nodeImageURL += "&color=" + color;
		}
		icon = themeBasedAttr.attr('icon');
		if (icon != "") {			
			nodeImageURL += "&image=" + encodeURI("/Centrifuge/resources/icons/" + currentTheme + "/" + icon);
		}
	} else {
		shape = "Circle";
		if(currentTheme != "Circular") {
			shape = utils.getRandomShape();
		}		
		nodeImageURL += "&shape=" + shape + "&color=" + utils.getRandomColor() +  "&image=null";
	}
	return nodeImageURL;
}
Utils.prototype.htmlEncode = function(value){
	   if (value) {
	       return $('<div>').text(value).html();
	   } else {
	       return '';
	   }
}

Utils.prototype.getNodeUiProps = function(nodeName) {	
	var currentTheme = $('#theme').val();	
	var themeURL = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/" + currentTheme + ".xml?asXML";
	var nodeUiProps = {};
	var doSuccess = function (data) {
		var icon = "null";
		var shape;
		var color;
		var themeOptionXML = data.responseText;
		var themeBasedAttr = $(themeOptionXML).find('NodeType').filter(function() {
		    return $(this).attr('nodetype').toLowerCase() == nodeName.toLowerCase() ;
		});
		var nodeImageURL = "/Centrifuge/WidgetControllerServlet?action=render";
		if (themeBasedAttr.length > 0) {
			shape = themeBasedAttr.attr('shape');		
			nodeImageURL += "&shape=" + shape;		
			color = themeBasedAttr.attr('color');
			if (color != "") {
				nodeImageURL += "&color=" + color;
			}
			icon = themeBasedAttr.attr('icon');
			if (icon != "") {
				icon = "/Centrifuge/resources/icons/" + currentTheme + "/" + icon;
				nodeImageURL += "&image=" + encodeURI(icon);
			}
		} else {
			shape = "Circle";
			if(currentTheme != "Circular") {
				shape = utils.getRandomShape();
			}
			color = utils.getRandomColor();
			nodeImageURL += "&shape=" + shape + "&color=" + color +  "&image=null";
		}
		nodeUiProps.shape = shape;
		nodeUiProps.color = color;
		nodeUiProps.image = icon;	
		nodeUiProps.nodeImageUrl = nodeImageURL;
		
	};
	
	$.ajax({
		url: themeURL,
		async : false,
		complete: doSuccess
	});
	
	return nodeUiProps;
}
Utils.prototype.validateComponent = function(componentText) {
	var digitString = componentText.match(/\d+/);
	if(digitString == null) {
		digitString = '0';
	}
	var integerComponent = Math.min(255, Math.max(0, parseInt(digitString, 10)));
	return integerComponent;
}
Utils.prototype.getVizBgColorInt = function(vizUuid) {
	var viz = this.getVisualisation(vizUuid).visualization
	var colorInt = parseInt(viz.settings.propertiesMap['csi.relgraph.backgroundColor']);
	return colorInt;
}
Utils.prototype.getAttributeDef = function(status, value, uuid, attrType){
	var attributeDef = new Object();	
	attributeDef.class = "csi.server.common.model.attribute.AttributeDef";
	attributeDef.clientProperties = {};
	if(status){
		attributeDef.bySize = false;
		attributeDef.byStatic = true;
	}
	else{
		attributeDef.bySize = true;
		attributeDef.byStatic = false;
	}
	attributeDef.byTransparency = false;
	attributeDef.defaultIncludeInTooltip = true;
	attributeDef.hideEmptyInTooltip = true;
	attributeDef.includeInTooltip = true;
	attributeDef.name = attrType;
	attributeDef.uuid = uuid;	
	if(!status){		
		attributeDef.kind = "REFERENCE";		
		attributeDef.referenceName = value;
		attributeDef.name = "csi.internal.Size";
	}else{
		var fieldDefJson = new Object();
		fieldDefJson.anonymous = false;
		fieldDefJson.cacheScale = 0;
		fieldDefJson.cacheSize = 0;
		fieldDefJson.class = "csi.server.common.model.FieldDef";	
		fieldDefJson.clientProperties = {};
		fieldDefJson.fieldType = "STATIC";
		fieldDefJson.functionType = "CONCAT";
		var cf = new Object();
		cf.class="csi.server.common.model.ConcatFunction";
		cf.clientProperties = new Object();
		cf.fields=[];
		cf.name = null;
		cf.ordinal = 0;
		cf.separator = "&#32;";
		cf.sortedFields = [];
		cf.uuid=utils.guidGenerator();		
		fieldDefJson.functions = [cf];
		fieldDefJson.ordinal = 0;
		fieldDefJson.rawScript = false;
		fieldDefJson.staticText = value;
		fieldDefJson.uuid = utils.guidGenerator();
		attributeDef.fieldDef = fieldDefJson;		
	}
	return attributeDef;
}
Utils.prototype.getFieldNameColumnOptions = function(columns){	
	var columnOptions = "";
	$(_.reject(columns,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))})).each(function() {
		columnOptions += $('<option></option>').attr('type',this.valueType.toUpperCase()).val(this.fieldName).append(this.fieldName)[0].outerHTML;	
	});
	return columnOptions;
}
