function CreateRgSettings(vizid){
	if (vizid) {
		this.uuid = vizid;
		var vizDef = utils.getVisualisation(this.uuid).visualization;
		this.vizDef = JSON.parse(JSON.stringify(vizDef));
	}
	this.themeXML = null;
	this.currentTheme = "Circular";
}

CreateRgSettings.prototype.generateSettingsDialog = function(){
	this.reset();
	this.initializeContainers();
	this.createNodeDiagram();	
	this.registerDragging();
	this.registerColorPicker();	
	$('#alertModal').attr('number', this.uuid).modal();
	$('#alertModal h3.box_head').text("Relationship Graph: "+ this.vizDef.name);
	$('#treeFinish-rg').data('vizDef', this.vizDef);
	this.createConnections();
	$('#rgName')[0].value = this.vizDef.name;
	$('#rndrThreshold')[0].value = this.vizDef.clientProperties['render.threshold'];
	var hex = utils.int2hex(this.vizDef.settings.propertiesMap['csi.relgraph.backgroundColor']);
	$('.node-diagram').css('backgroundColor', "#" + hex);
	$('#colorSelector2 div').css('backgroundColor', "#" + hex);
	var loadOnStartup = this.vizDef.clientProperties["vizBox.loadOnStartup"];
	$('#alertModal').find('#loadOnStartUp').attr("checked", loadOnStartup);
}

CreateRgSettings.prototype.reset = function(){
	var nodes = $('#node_diagram').find(".operation");
	$.each(nodes, function(index, node) {
		jsPlumb.detachAllConnections($(node).attr('id'));
		$(node).remove();
	});
	$('#rgName')[0].value = "";
	$('#rndrThreshold')[0].value = 2000;//default value 2000
	$('.node-diagram').css('backgroundColor', "#FFF"); //default bg colour
	$('#alertModal').find('#loadOnStartUp').attr("checked", true);
	$('.left_container').attr('id','left_container');
	$("#theme").val("Circular");
	$('#dontLoadAftrSave').attr("checked", false);
	$('#colorSelector2 div').css('backgroundColor', '#fefefe');
	this.createColumns();
	this.registerDragging();
	this.registerColorPicker();	
}
CreateRgSettings.prototype.createNodeDiagram = function(){
	var createsettingdialog = this;
	$.each(this.vizDef.nodeDefs, function(index, nodeDef) {
		var icon = "null";
		var shape = null;
		var color = null;
		
		if(nodeDef['attributeDefsAsMap']['csi.internal.Icon'] != undefined){
			icon = nodeDef['attributeDefsAsMap']['csi.internal.Icon']['staticText'];
		}
		if(nodeDef['attributeDefsAsMap']['csi.internal.Shape'] != undefined){
			shape = nodeDef['attributeDefsAsMap']['csi.internal.Shape']['staticText'];
		}
		if(nodeDef['attributeDefsAsMap']['csi.internal.Color'] != undefined){
			color = nodeDef['attributeDefsAsMap']['csi.internal.Color']['staticText'];
		}
		
		
		var nodeImageURL = "/Centrifuge/WidgetControllerServlet?action=render";		
		var themeBasedAttr = $(createsettingdialog.themeXML).find('NodeType').filter(function() {
		    return $(this).attr('nodetype').toLowerCase() == nodeDef.name.toLowerCase() ;
		});
		
		if (themeBasedAttr.length != 0) {
			shape = themeBasedAttr.attr('shape');		
			nodeImageURL += "&shape=" + shape;		
			color = themeBasedAttr.attr('color');
			if (color != "") {
				nodeImageURL += "&color=" + color;
			}
			icon = themeBasedAttr.attr('icon');
			if (icon != "") {
				icon = "/Centrifuge/resources/icons/" + createsettingdialog.currentTheme + "/" + icon;
				nodeImageURL += "&image=" + encodeURI(icon);
			}
		} else {			
			if(shape == undefined) {
				shape = "Circle";
			}
			nodeImageURL += "&shape=" + shape + "&color=" + color;
			nodeImageURL += "&image=" + (icon == "null" ? "null" : encodeURI(icon));
			
		}
		var nodeDetails = {
			name: nodeDef['name'],
			top: parseFloat(nodeDef['attributeDefsAsMap']['csi.internal.yPos']['staticText']),
			left: parseFloat(nodeDef['attributeDefsAsMap']['csi.internal.xPos']['staticText']),
			imageURL: nodeImageURL,
			shape : shape,
			color : color,
			image : icon
		}
		var nodeDiv = createSettingsNodesDiv(nodeDetails);
		$('#node_diagram').append(nodeDiv);
		$(nodeDiv).attr('uuid', nodeDef['uuid']);
		resetRenderMode(jsPlumb.CANVAS, nodeDiv);
	});
	
}

CreateRgSettings.prototype.initializeContainers = function(){
	var creatergsettings = this;
	$('.left_container').attr('id','left_container');
	var theme = this.vizDef.optionSetName == null ? "Circular" : this.vizDef.optionSetName;
	this.currentTheme = theme;
	$('#theme option[value="'+theme+'"]').attr('selected', 'selected');	
	this.setTheme(theme);	
}

CreateRgSettings.prototype.setTheme = function(themeName){
	var creatergsettings = this;
	var url = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/"+themeName+".xml?asXML";		
	$.ajax({
		url: url,
		async : false,
		complete: function(data) {			
			creatergsettings.themeXML = data.responseText;
			creatergsettings.vizDef.optionSetName = themeName;
			creatergsettings.currentTheme = themeName;
			creatergsettings.updateNodeImages();          
		}
	});
	
}

CreateRgSettings.prototype.createColumns = function() {
	var dv = $.data(document.body, 'dataview');
	var creRgSett = this;
	$('#left_container').empty();
	if (window.dataview.myData.resultData.meta.modelDef.fieldDefs.length > 0) {
		var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))});
		$.each(cols, function() {
			var columnDiv = $('<div>').addClass('inlinerows');
			var image1 = $('<img>').attr('src', '../images/icon1.jpg').attr('width', "15").attr('height', "18").addClass('floatLt');
			var image2;
			if(this.valueType == "integer"){
				image2 = $('<img>').attr('src', '../images/icon2integer.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			else if (this.valueType == "number"){
				image2 = $('<img>').attr('src', '../images/icon2number.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			else if (this.valueType == "string"){
				image2 = $('<img>').attr('src', '../images/icon2string.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			else if(this.valueType == "date"){
				image2 = $('<img>').attr('src', '../images/icon2date.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			else if(this.valueType == "time"){
				image2 = $('<img>').attr('src', '../images/icon2time.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			else if(this.valueType == "date/time"){
				image2 = $('<img>').attr('src', '../images/icon2date_time.jpg').attr('width', "21").attr('height', "18").addClass('floatLt');
			}
			else if(this.valueType == "boolean"){
				image2 = $('<img>').attr('src', '../images/icon2boolean.jpg').attr('width', "17").attr('height', "18").addClass('floatLt');
			}
			var titleSpan = $('<span>').css('float', 'left').text(this.fieldName).addClass('ellipsis columnName');
			var image3 = $('<img>').attr('src', '../images/icon3.jpg').attr('width', "15").attr('height', "18").addClass('floatRt dropLeft');
			creRgSett.registerClickDropping(image3, this.fieldName);
			columnDiv.empty().append(image1).append(image2).append(titleSpan).append(image3);
			$('#left_container').append(columnDiv);
			$('.node-diagram').attr('id','node_diagram');
		});
	}
}

CreateRgSettings.prototype.registerDragging=function() {	
	var createsettingdialog = this;
	$('.left_container .inlinerows').draggable({
		helper: "clone",
		revert: "invalid",
		start: function(event, ui) {
			ui.helper.css('width', "200px");
		}
	});
	$( ".node-diagram" ).droppable({
		activeClass: "ui-state-default",
		hoverClass: "ui-state-hover",
		accept: function(draggable) {
			return isNotAddedToDomAndFromLeftPanel(draggable);
		},
		drop: function( event, ui ) {
			var nodeName = ui.draggable.find('span').text();			
			var nodeUiProps = utils.getNodeUiProps(nodeName);
			operationsDiv = generateOperationsDiv(ui, nodeUiProps);
			$(this).append(operationsDiv);			
			resetRenderMode(jsPlumb.CANVAS, operationsDiv);
			var nodeInfo = {};
			nodeInfo.name = nodeName;
			nodeInfo.position = $(operationsDiv).position();
			nodeInfo.uuid = null;
			nodeInfo.shape = nodeUiProps.shape;				
			nodeInfo.color = nodeUiProps.color + "";
			nodeInfo.image = nodeUiProps.image;			
			
			var relgraph = new RelationGraphJson(window.dataview.myData);
			var nodeDef = relgraph.getNodeDefJson(nodeName, nodeInfo);
			var vizDef = $('#treeFinish-rg').data('vizDef');
			if (!vizDef){
				var vizDef = relgraph.getDummyVisualDefJson();
				vizDef.isNew = true;
			}
			vizDef.nodeDefs.push(nodeDef);
			$(operationsDiv).attr('uuid', nodeDef.uuid);
			$('#treeFinish-rg').data('vizDef', vizDef);
		}
	});
	
}
CreateRgSettings.prototype.registerColorPicker=function() {
	var default_color = '#ffffff';
	$('#rg-settings-color-picker').simplecolorpicker('destroy');
	if(this.vizDef) {
		var selectedColor = "#" + utils.int2hex(this.vizDef.settings.propertiesMap['csi.relgraph.backgroundColor']);
	} else {
		var selectedColor = default_color;
	}
	if($('#rg-settings-color-picker option[value="' + selectedColor + '"]').length == 0) {
		$('#rg-settings-color-picker').append($('<option>').attr('value', selectedColor).text(selectedColor));
	}
	$('#rg-settings-color-picker').val(selectedColor);
	$('#settings-color-picker-main-div .simplecolorpicker.icon').css('backgroundColor', selectedColor);
	$('#settings-color-picker-main-div .color-preview').css('backgroundColor', selectedColor);
	$('#settings-color-picker-main-div .color-picker-picker-wrapper').hide();
	
	$('#settings-color-picker-main-div').find('.color-component.red-component input').val(utils.hexToRGBComponent(selectedColor).r);
	$('#settings-color-picker-main-div').find('.color-component.green-component input').val(utils.hexToRGBComponent(selectedColor).g);
	$('#settings-color-picker-main-div').find('.color-component.blue-component input').val(utils.hexToRGBComponent(selectedColor).b);
	
	$('#settings-color-picker-main-div .simplecolorpicker.icon').unbind('click');
	$('#settings-color-picker-main-div .simplecolorpicker.icon').click(function() {
		$('#settings-color-picker-main-div .color-picker-picker-wrapper').slideToggle();
	});
	$('#rg-settings-color-picker').simplecolorpicker().change(function() {
		var hex = $('#settings-color-picker-main-div').find('.simplecolorpicker div.selected').attr('title').match(/#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})/i)[0].slice(1);
		$('#settings-color-picker-main-div .simplecolorpicker.icon').css('backgroundColor', "#" + hex);
		$('#settings-color-picker-main-div .color-preview').css('backgroundColor', "#" + hex);
		$('.node-diagram').css('backgroundColor', "#" + hex);
		$('#settings-color-picker-main-div .color-picker-picker-wrapper').slideUp();
		$('#settings-color-picker-main-div').find('.color-component.red-component input').val(utils.hexToRGBComponent(hex).r);
		$('#settings-color-picker-main-div').find('.color-component.green-component input').val(utils.hexToRGBComponent(hex).g);
		$('#settings-color-picker-main-div').find('.color-component.blue-component input').val(utils.hexToRGBComponent(hex).b);
	});
	$('#settings-color-picker-main-div').find('.color-component input').unbind('change');
	$('#settings-color-picker-main-div').find('.color-component input').change(function() {
		var rElement = $('#settings-color-picker-main-div').find('.color-component.red-component input');
		var gElement = $('#settings-color-picker-main-div').find('.color-component.green-component input');
		var bElement = $('#settings-color-picker-main-div').find('.color-component.blue-component input');
		var r = utils.validateComponent(rElement.val());
		var g = utils.validateComponent(gElement.val());
		var b = utils.validateComponent(bElement.val());
		var rgbstring = 'rgb(' + r + ',' +  g + ',' + b + ')';
		rElement.val(r);
		gElement.val(g);
		bElement.val(b);
		$('#settings-color-picker-main-div').find('.color-preview').css('background-color', rgbstring);
		$('#settings-color-picker-main-div .simplecolorpicker.icon').css('backgroundColor', rgbstring);
		$('.node-diagram').css('backgroundColor', rgbstring);
	});
	
}
CreateRgSettings.prototype.createConnections=function() {
	var rgSettings = this;
	$.each(this.vizDef.linkDefs, function(index, linkDef) {
		var startNode = $(".operation[title='"+linkDef.nodeDef1.name+"']").attr('id');
		var endNode = $(".operation[title='"+linkDef.nodeDef2.name+"']").attr('id');
		
		var con = jsPlumb.connect({
			source: startNode, 
			target: endNode,
			overlays: [
				[
					"Custom",
					{
						create: function() {
							return $("<img style=\"display:none\" src=\"../images/node-edit.png\"> ");
						},
						location: 0.45,
						cssClass: "link-editor",
						events: {
							click: function(overlay, originalEvent) {
								var source = overlay.component.source;
								var target = overlay.component.target;
								var linkName = source.attr('title') + '_' + target.attr('title');
								createLinkDialog = new CreateLinkEditDialog(source, target, linkName);
								createLinkDialog.doTask();
							}
						}
					}
				],
				[
					"Custom",
					{
						create: function() {
							return $("<img  style=\"display:none\" src=\"../images/node-delete.png\">");
						},
						location: 0.55,
						cssClass:"link-delete",
						events: {
							click: function(overlay, originalEvent) {
								bootbox.confirm("Are you sure you want to delete this link?", function(confirmed) {
									if (confirmed) {
										var source = overlay.component.source;
										var target = overlay.component.target;
										var node1 = source.attr('title');
										var node2 = target.attr('title');
										var vizdef = $.data($('#treeFinish-rg')[0], 'vizDef');
										$.each(vizdef.linkDefs, function(index, linkDef) {
											if(linkDef) {
												if( (linkDef.nodeDef1.name == node1) && (linkDef.nodeDef2.name == node2) ) {
													vizdef.linkDefs.splice(index, 1);
												}
											}
										});
										jsPlumb.detach(overlay.component);
									}
								}); 
							}	
						}
					}
				]
			]
		});
		if(linkDef.name != linkDef.uuid){
			con.addOverlay(["Label", {
				label:linkDef.name,
				location:0.5,
				id:"label",
				cssClass:"link-label"
			}]);
		}
		else{
			con.addOverlay(["Label", {
				label:"",
				location:0.5,
				id:"label",
				cssClass:"link-label"
			}]);
		}
	});
}
CreateRgSettings.prototype.updateNodeImages=function() {
	var createsettingdialog = this;
	var nodes = $('#node_diagram').find(".operation");	
	$.each(nodes, function(index, node) {
		var nodeName = $(node).find('.node-label').text();		
		var nodeUiProps = utils.getNodeUiProps(nodeName);
		$(node).css('background-image', 'url("' + nodeUiProps.nodeImageUrl + '")');
		$(node).attr('shape', nodeUiProps.shape);
		$(node).attr('color', nodeUiProps.color);
		$(node).attr('image', nodeUiProps.image);
		var nodeDefIdx = utils.getNodeDefIdx(createsettingdialog.vizDef.nodeDefs, nodeName);
		var attributeDefsArr = createsettingdialog.vizDef.nodeDefs[nodeDefIdx].attributeDefs;
		var colorAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Color");
		var shapeAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Shape");
		var iconAttrDefIdx = utils.getAttributeDefIdx(attributeDefsArr, "csi.internal.Icon");		
		createsettingdialog.vizDef.nodeDefs[nodeDefIdx].attributeDefs[colorAttrDefIdx].fieldDef.staticText = nodeUiProps.color + ""; 
		createsettingdialog.vizDef.nodeDefs[nodeDefIdx].attributeDefs[shapeAttrDefIdx].fieldDef.staticText = nodeUiProps.shape;
		if(iconAttrDefIdx != undefined) {
			createsettingdialog.vizDef.nodeDefs[nodeDefIdx].attributeDefs[iconAttrDefIdx].fieldDef.staticText = nodeUiProps.image;
		}
		
	});
}

CreateRgSettings.prototype.registerClickDropping = function(image3, nodeName) {
	var createsettingdialog = this;
	image3.click(function() {
		if(isNotAddedToDomAndFromLeftPanel(image3.parent())) {			
			var nodeUiProps = utils.getNodeUiProps(nodeName);
			var randomPos = utils.getRandomPositionForNodes();
			var nodeDetailsObj = {
				index: 0,
				name: nodeName,
				randomTop: randomPos.top,
				randomLeft: randomPos.left,
				imageURL : nodeUiProps.nodeImageUrl,
				shape : nodeUiProps.shape,
				color : nodeUiProps.color,
				image : nodeUiProps.image
			}
			operationsDiv = createSettingsNodesDiv(nodeDetailsObj);
			
			$('#node_diagram').append(operationsDiv);
			operationsDiv.offset({
				top: randomPos.top
			});
			operationsDiv.css({left: randomPos.left});
			resetRenderMode(jsPlumb.CANVAS, operationsDiv);
			var nodeInfo = {};
			nodeInfo.name = nodeName;
			nodeInfo.position = $(operationsDiv).position();
			nodeInfo.uuid = null;	
			nodeInfo.shape = nodeUiProps.shape;				
			nodeInfo.color = nodeUiProps.color + "";
			nodeInfo.image = nodeUiProps.image;				
			var relgraph = new RelationGraphJson(window.dataview.myData);
			var nodeDef = relgraph.getNodeDefJson(nodeName, nodeInfo);
			var vizDef = $.data($('#treeFinish-rg')[0], 'vizDef');
			if (!vizDef){
				var vizDef = relgraph.getDummyVisualDefJson();
				vizDef.isNew = true;
			}
			vizDef.nodeDefs.push(nodeDef);
			$.data($('#treeFinish-rg')[0], 'vizDef', vizDef);
			$(operationsDiv).attr('uuid', nodeDef.uuid);
		} else {
			bootbox.alert("Already added this node.");
		}
	});
}
