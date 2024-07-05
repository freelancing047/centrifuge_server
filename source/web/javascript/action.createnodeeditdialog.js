function CreateNodeEditDialog(nodeName, shape, color, icon, uuid){
	this.nodeName = nodeName;
	this.editNodeId = 0;
	this.basicsTab = null;
	this.linksupTab = null;
	this.computedFieldsTab = null;
	this.toolTipTab = null;
	this.bundleTab = null;
	this.vizdef;
	this.nodeDef = null;
	this.shapesIndexMap = { None : 0, 
							Circle : 1,
							Diamond : 2,
							Hexagon : 3,
							Pentagon : 4,
							Octagon : 5,
							House : 6,
							Square : 7,
							Star : 8,
							Triangle : 9
						  };
	this.iconsForBaselineThemesMap = {
			"None" : {},
			"Buildings" : [],
			"Communications" : [],
			"Emergency Services" : [],
			"Events" : [],
			"Financial" : [],
			"Flags" : [],
			"Miscellaneous" : [],
			"People" : [],
			"Travel" : []
			
	};
	this.iconsForCircularThemesMap = {
			"None" : {},
			"Buildings" : [],
			"Communications" : [],
			"Danger" : [],			
			"Financial" : [],
			"Flags" : [],
			"Organizations" : [],
			"People" : [],
			"Time" : [],
			"Transport" : []
			
	};
	this.shape = shape;
	this.color = color;
	this.icon = icon;
	this.uuid = uuid;
	this.currentTheme;
	this.rgJson;
	this.themeOptionXML;
	
}
CreateNodeEditDialog.prototype.fetchVisualisation = function(vizuuid){
	this.vizdef = $('#treeFinish-rg').data('vizDef');
	this.doTask();
}

CreateNodeEditDialog.prototype.doTask = function(){	
	var nodeEditView = {
		nodeName: utils.getAlphaNumericString(this.nodeName)
	}
	var modalDiv = $(Mustache.render($('#edit-node-modal-div').html(), nodeEditView)).filter('.edit-node-modal');
	var existingModelDiv = $('#' + this.getModalDivId());
	existingModelDiv.remove();	
	this.initailizeNodeEditContainer(modalDiv);	
	modalDiv.find('.edit-node-name').val(this.nodeName);
	if(this.nodeDef) {
		this.populateGeneralInfo(modalDiv);
		this.createEditNodeTabs(modalDiv);	
		this.registerUpdateBttnAction(modalDiv);
		this.createNodeGeneralInfo(modalDiv);	
		modalDiv.modal();
	}	
	this.registerNoneIcon(modalDiv)
}

CreateNodeEditDialog.prototype.registerNoneIcon = function(modalDiv) {
	var editNode = this;
	// var prviousImageUrl = modalDiv.find(".preview-image").attr("src")
	var noneIconLink = modalDiv.find('#node-edit-none-option');
	noneIconLink.click(function(event) {
		if ($('.node-icon-selected')) {
			$('.node-icon-selected').removeClass('node-icon-selected');
			editNode.icon = 'null';
			$('.preview-image').attr('src', editNode.getURL());
		}
	});
}

CreateNodeEditDialog.prototype.getModalDivId = function() {
	var rgDialogId = $('body').find('.alertModal').attr('id');
	var editNodeId = rgDialogId.substring(8);	
	return ('editNode' + editNodeId + utils.getAlphaNumericString(this.nodeName));
}

CreateNodeEditDialog.prototype.initailizeNodeEditContainer = function(modalDiv){
	var rgDialogId = $('body').find('.alertModal').attr('id');
	var editNodeId = rgDialogId.substring(8);	
	modalDiv.attr('id','editNode'+ editNodeId+ utils.getAlphaNumericString(this.nodeName));	
	this.editNodeId = editNodeId;	
	this.setNodeDef();
	this.currentTheme = $('#theme').val();
	this.vizdef.optionSetName = this.currentTheme;
	this.getThemeOptionsXML(modalDiv);
	this.parseOptionSetImages();
	this.basicsTab = new NodeEditorBasicsTab(modalDiv,this.nodeName, this.nodeDef);
	this.linksupTab = new NodeEditorLinksTab(modalDiv, this.editNodeId, this.nodeName);
	this.toolTipTab = new NodeEditorToolTipTab(modalDiv,this.nodeName,this.vizdef);
	this.bundleTab = new NodeEditorBundlingTab(modalDiv,this.nodeName);
	this.computedFieldsTab = new NodeEditorForComputedFields(modalDiv, this.nodeName ,this.vizdef);
	this.isValidate(modalDiv);
}

CreateNodeEditDialog.prototype.setNodeDef = function(){
	var nodeEdit = this;
	$.each(nodeEdit.vizdef.nodeDefs, function(){
		if(this.name == nodeEdit.nodeName){
			nodeEdit.nodeDef = this;
		}
	});
}

CreateNodeEditDialog.prototype.populateGeneralInfo = function(modalDiv){
    var editNode = this;
    var nodeDefIdx = utils.getNodeDefIdx(this.vizdef.nodeDefs, this.nodeName);
    var nodeDef = this.vizdef.nodeDefs[nodeDefIdx];   
    var hideLabel = nodeDef.hideLabels;   
    modalDiv.find('#hideLabel').attr('checked',hideLabel);
    modalDiv.find('#hideEmptyValue').attr('checked',nodeDef.attributeDefs[0].hideEmptyInTooltip);   
    modalDiv.find(".edit-node-name").val(nodeDef.name);   
    editNode.populateIconCategories(modalDiv, "None");
    modalDiv.find('#static').attr('checked',true);
    modalDiv.find('#scaleNonStatic').attr('style','display:none;');
    modalDiv.find('#scaleStatic').val(1+"");
    modalDiv.find('#scaleStatic').removeAttr('style');       
    var isIconAttrExists = false;   
    $.each(nodeDef.attributeDefs, function(){
        if (this.kind == "COMPUTED") {
            modalDiv.find('#scaleNonStatic').append( new Option(this.name,this.name) );
        }
        if(this.name == "csi.internal.Size" && this.kind != undefined && this.kind == "REFERENCE"){
            modalDiv.find('#scaleStatic').attr('style','display:none;');
            modalDiv.find('#scaleNonStatic').val(this.referenceName);
            modalDiv.find('#scaleNonStatic').removeAttr('style');
            modalDiv.find('#static').attr('checked',false);
           
        }else if(this.name == "csi.internal.Size"){
            modalDiv.find('#static').attr('checked',true);
            modalDiv.find('#scaleNonStatic').attr('style','display:none;');
            modalDiv.find('#scaleStatic').val(this.fieldDef.staticText);
            modalDiv.find('#scaleStatic').removeAttr('style');       
           
        }else if(this['name'] == "csi.internal.Color"){
            editNode.setColor(modalDiv, this.fieldDef.staticText, this.uuid);
        }else if(this.name == "csi.internal.Shape"){
            editNode.setShape(modalDiv, this.fieldDef.staticText, this.uuid);           
        }else if (this.name == "csi.internal.Icon") {
            editNode.setIcon(modalDiv, this.fieldDef.staticText);
            isIconAttrExists = true;               
        }
    });
   
    if (!isIconAttrExists) {
        this.setIcon(modalDiv, this.icon);   
    }   
   
    modalDiv.find('#static').change(function(){
        if($(this).is(':checked')){
            modalDiv.find('#scaleNonStatic').attr('style','display:none;');
            modalDiv.find('#scaleStatic').removeAttr('style');
           
        }else{
            modalDiv.find('#scaleStatic').attr('style','display:none;');
            modalDiv.find('#scaleNonStatic').removeAttr('style');
        }
    });
    var default_color = '#fefefe';
    var widt = false;
    console.log(modalDiv.find('.accordion-body.collapse'))
    modalDiv.find('.accordion-body.collapse').on('shown', function() {
        $(this).prev().find('.caret').addClass('caret-up');
    });
    modalDiv.find('.accordion-body.collapse').on('hidden', function() {
        $(this).prev().find('.caret').removeClass('caret-up');
    });
   
    modalDiv.find('.iconrow .node-icon').on('click', function() {
        if($(this).hasClass('node-icon-selected')) {
            $(this).removeClass('node-icon-selected');
        } else {
            $('.iconrow .node-icon').removeClass('node-icon-selected');
            $(this).addClass('node-icon-selected');            
            editNode.icon = $(this).find('img').attr('src');//encodeURI($(this).attr('src'));
            modalDiv.find('.preview-image').attr('src', editNode.getURL());
        }
       
    });
   
    modalDiv.find('.shaperow .shape').on('click', function() {
        if($(this).hasClass('shape-selected')) {
            $(this).removeClass('shape-selected');
        } else {
            $('.shaperow .shape').removeClass('shape-selected');
            $(this).addClass('shape-selected');
            editNode.shape = $(this).attr('data-val');
            modalDiv.find('.preview-image').attr('src', editNode.getURL());
           
        }
    });
    
    modalDiv.find('.preview-image').attr('src', editNode.getURL());
    this.performThemeBasedAction(modalDiv);
}

CreateNodeEditDialog.prototype.createEditNodeTabs = function(modalDiv){
	this.basicsTab.populateTabInfo(this.vizdef);
	this.linksupTab.populateTabInfo(this.vizdef);
	this.toolTipTab.populateTabInfo();	
	this.bundleTab.populateTabInfo(this.vizdef);
	this.computedFieldsTab.populateComputedFieldsTabInfo();
}

CreateNodeEditDialog.prototype.registerUpdateBttnAction = function(modalDiv){
	var editnode = this;
	var vizDef = this.vizdef;
	var computedFieldsTab = this.computedFieldsTab;
	var toolTipTab = this.toolTipTab;
	var bundleTab = this.bundleTab; 
	var basicTab = this.basicsTab;
    var linksTab = this.linksupTab;
	modalDiv.find('#treeFinish-ne').click(function(){		
			var bundleDef = bundleTab.doSave(vizDef);
			var newVizDef = toolTipTab.doSave(vizDef);
			newVizDef = computedFieldsTab.doSave(newVizDef);
			newVizDef = basicTab.doSave(newVizDef);
			newVizDef = linksTab.doSave(newVizDef);
			newVizDef.bundleDefs[0] = bundleDef;			
			newVizDef = editnode.doSave(newVizDef, modalDiv);
			$('#treeFinish-rg').data('vizDef', newVizDef);
			modalDiv.modal('hide');	
			modalDiv.remove();
	});	
	modalDiv.find('#cancel-ne').click(function(){		
		modalDiv.modal('hide');
		modalDiv.remove();
	});	
}

CreateNodeEditDialog.prototype.doSave = function(newVizDef, modalDiv){
    var editNode = this;
    var nodeDefIdx = utils.getNodeDefIdx(this.vizdef['nodeDefs'], this.nodeName);
    var hideLabel = modalDiv.find('#hideLabel').is(":checked") ? true : false;
    newVizDef.nodeDefs[nodeDefIdx]['hideLabels'] = hideLabel;
    var hideEmptyValue = modalDiv.find('#hideEmptyValue').is(":checked") ? true : false;
   //newVizDef.nodeDefs[nodeDefIdx]['clientProperties']['hideAllEmptyLabels'] = hideEmptyValue;
    var nodeName = modalDiv.find(".edit-node-name").val();
    newVizDef.nodeDefs[nodeDefIdx].name = nodeName;
    var staticStatus = modalDiv.find('#static').is(':checked');   
    var scaleValue;
   
    if(staticStatus){
        scaleValue =  modalDiv.find('#scaleStatic').val();
        if (scaleValue == "" || parseFloat(scaleValue) <= 0) {
            scaleValue = 1;
        }
    }else{
        scaleValue =  modalDiv.find('#scaleNonStatic').val();
    }   
   
    var isSizeAttrExists = false;
    var iconAttrExists = false;   
    var isScaleAttrExists = false;
    $.each(newVizDef.nodeDefs[nodeDefIdx].attributeDefs, function(index, attributeDef){
        if(attributeDef.name == "csi.internal.Size"){
            newVizDef.nodeDefs[nodeDefIdx].attributeDefs[index] = utils.getAttributeDef(staticStatus, scaleValue + "", attributeDef.uuid, "csi.internal.Size");
            isSizeAttrExists = true;
        }else if(attributeDef.name == "csi.internal.Color" ){
            newVizDef.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef.staticText = editNode.color + "";           
        }else if(attributeDef.name == "csi.internal.Shape" ){
            newVizDef.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef.staticText = editNode.shape + "";           
        }else if (attributeDef.name == "csi.internal.Icon") {           
            newVizDef.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef.staticText =  editNode.icon;
            iconAttrExists = true;
        }else if (attributeDef.name == "csi.internal.Scale") {
            isScaleAttrExists = true;
        }
        newVizDef.nodeDefs[nodeDefIdx].attributeDefs[index].hideEmptyInTooltip = hideEmptyValue;

    });
    if(!isSizeAttrExists){
        var sizeAttributeDef = utils.getAttributeDef(staticStatus, scaleValue + "", utils.guidGenerator(), "csi.internal.Size");
        sizeAttributeDef.hideEmptyInTooltip = hideEmptyValue;
        newVizDef.nodeDefs[nodeDefIdx].attributeDefs.push(sizeAttributeDef);
    }
    if(!iconAttrExists) {       
        var iconAttrDef = utils.getAttributeDef(true, editNode.icon , utils.guidGenerator(), "csi.internal.Icon");
        iconAttrDef.hideEmptyInTooltip = hideEmptyValue;
        newVizDef.nodeDefs[nodeDefIdx].attributeDefs.push(iconAttrDef);
    }
    if(!isScaleAttrExists) {
    	var scaleValue = "0.75";
    	if (this.currentTheme == "Circular") {
    		scaleValue = $(this.themeOptionXML).find('iconscale').text();
    	}
        var scaleAttrDef = utils.getAttributeDef(true, scaleValue , utils.guidGenerator(), "csi.internal.Scale");
        scaleAttrDef.hideEmptyInTooltip = hideEmptyValue;
        newVizDef.nodeDefs[nodeDefIdx].attributeDefs.push(scaleAttrDef);
    }
   
    this.updateNode(newVizDef.nodeDefs[nodeDefIdx].uuid, this.shape + "", this.color + "", nodeName, this.icon);
    return newVizDef;
}

CreateNodeEditDialog.prototype.createNodeGeneralInfo = function(modalDiv){
	var default_color = '#fefefe';
	var widt = false;
	console.log(modalDiv.find('#colorpickerHolder2'));
	modalDiv.find('#colorpickerHolder2').ColorPicker({
		flat: true,
		color: default_color,
		onSubmit: function(hsb, hex, rgb) {
			intetgerValue = parseInt(hex, 16); //can be used to save to database
			modalDiv.find('#colorSelector2 div').css('backgroundColor', '#' + hex);
			modalDiv.find('#colorpickerHolder2').stop().animate({height: widt ? 0 : 173}, 500);
			widt = !widt;
		}
	});
	modalDiv.find('#colorpickerHolder2>div').css('position', 'absolute');
	modalDiv.find('#colorSelector2').bind('click', function() {
		modalDiv.find('#colorpickerHolder2').stop().animate({height: widt ? 0 : 173}, 500);
		widt = !widt;
	});
	modalDiv.find('#scaleStatic').blur(function(event) {
		var max = $(event.srcElement).attr("max");
		var min = $(event.srcElement).attr("min");
		if (isNaN($(event.srcElement).val())){
			$(event.srcElement).val(min);
			return;
		} else {
			var val = parseFloat($(event.srcElement).val());
			if (val < min) {
				$(event.srcElement).val(min);
			}else if (val > max) {
				$(event.srcElement).val(max);
			}
		}
	});
}

CreateNodeEditDialog.prototype.updateNode = function(uuid, shape, color, nodeName, icon){	
	var nodeeditdialog = this;
	var nodes = $('#node_diagram').find(".operation");	
	$.each(nodes, function() {
		if($(this).attr('uuid') == uuid){
			var iconURL = "/Centrifuge/WidgetControllerServlet?action=render&shape=" + shape + "&color=" + color;
			iconURL += "&image=" + (icon == null ? null : encodeURI(icon));			
			$(this).css('background-image', 'url("' + iconURL + '")');
			$(this).find('.node-label').text(nodeName);
			$(this).attr('shape', shape);
			$(this).attr('color', color);
			$(this).attr('image', icon);
		}			
	});
}

CreateNodeEditDialog.prototype.generateShapeDropDownOptions = function(modalDiv, value){
	var ddData = [
	              {	            
	                  value: "None",	                  
	                  description: "None"	                  
	              },
	              {	                  
	                  value: "Circle", 
	                  imageSrc: "/Centrifuge/images/shapes/circle.png"
	              },
	              {	                  
	                  value: "Diamond", 
	                  imageSrc: "/Centrifuge/images/shapes/diamond.png"
	              }	,
	              {	                  
	                  value: "Hexagon", 
	                  imageSrc: "/Centrifuge/images/shapes/hexagon.png"
	              }	,
	              {	                  
	                  value: "Pentagon", 
	                  imageSrc: "/Centrifuge/images/shapes/pentagon.png"
	              }	,
	              {	                  
	                  value: "Octagon", 
	                  imageSrc: "/Centrifuge/images/shapes/octagon.png"
	              }	,
	              {	                  
	                  value: "Pentagon/House", 
	                  imageSrc: "/Centrifuge/images/shapes/house.png"
	              }	,
	              {	                  
	                  value: "Square", 
	                  imageSrc: "/Centrifuge/images/shapes/square.png"
	              }	,
	              {	                  
	                  value: "Star", 
	                  imageSrc: "/Centrifuge/images/shapes/star.png"
	              },
	              {	                  
	                  value: "Triangle", 
	                  imageSrc: "/Centrifuge/images/shapes/triangle.png"
	              }	
	              
	          ];
	var editnode = this;
	modalDiv.find('#shape-container').removeAttr('style');
	modalDiv.find('#shape').ddslick({width : 219, data: ddData, defaultSelectedIndex : editnode.shapesIndexMap[value]});	
	
}

CreateNodeEditDialog.prototype.parseOptionSetImages = function(){
	var theme = this.currentTheme == "NoTypes" ? "Baseline" : this.currentTheme;	
	var url = "/Centrifuge/services/getappfileinfo/requestResource?res=resources/icons/" + theme;	
	var editNode = this;
	$.ajax({
		url: url,
		async : false,
		dataType: "xml",
		complete: function(data) {
			var iconsMap = editNode.currentTheme == "NoTypes" || editNode.currentTheme == "Baseline" ?
								editNode.iconsForBaselineThemesMap : editNode.iconsForCircularThemesMap;		
			$(data.responseText).find('file').each( function(index, value) {
				var path = $(this).attr('path');
				path = path.replace(/\\/g, '/');
				var pathProps = path.split('/');
				iconsMap[pathProps[3]].push("/Centrifuge/" + path);
			});
		}
	});
	
} 

CreateNodeEditDialog.prototype.generateIconsDropDown = function(category, iconURL, modalDiv) {
	var data = [];
	var selectedIndex = 0;
	var iconsMap = this.currentTheme == "NoTypes" || this.currentTheme == "Baseline" ?
			this.iconsForBaselineThemesMap : this.iconsForCircularThemesMap;
	$.each(iconsMap[category], function(index, value) {
		var optionData = {};
		optionData.value = value;
		optionData.imageSrc = value;
		data.push(optionData);
		if(value == iconURL) {
			selectedIndex = index;
		}
	});
	modalDiv.find('#icon').ddslick('destroy');
	modalDiv.find('#icon').removeAttr('style').ddslick(
			{	width : 219,
				data: data, 
				defaultSelectedIndex : selectedIndex
			}
			);
}
CreateNodeEditDialog.prototype.populateIconCategories = function(modalDiv, category){
	var iconsMap = this.currentTheme == "NoTypes" || this.currentTheme == "Baseline" ?
			this.iconsForBaselineThemesMap : this.iconsForCircularThemesMap;
	var iconCat = modalDiv.find('#icon-category').empty();
	var editNode = this;
	$.each(iconsMap, function(key, value) {
		if(key == category) {
			iconCat.append($('<option></option').attr('selected','selected').val(key).append(key)[0].outerHTML);
		}else {
			iconCat.append($('<option></option').val(key).append(key)[0].outerHTML);
		}
	});
	modalDiv.find('#icon-category').change(function() {
		var cat = $(this).val();
		editNode.generateIconsDropDown(cat, null, modalDiv);
	});
}
CreateNodeEditDialog.prototype.setShape = function(modalDiv, shape, uuid) {			
	modalDiv.find('.shaperow .shape[data-val="'+shape+'"]').addClass('shape-selected');	
	
}
CreateNodeEditDialog.prototype.setColor = function(modalDiv, color, uuid) {
	var editnode = this;
	modalDiv.find('#nodeColorPicker').simplecolorpicker({
		picker: false
	}).change(function() {
		var hex = modalDiv.find('.simplecolorpicker div.selected').attr('title').match(/#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})/i)[0].slice(1);		
		editnode.color = utils.rgb2int(utils.hexToRgb(hex));
		modalDiv.find('.color-preview').css('background-color', '#' + hex);
		modalDiv.find('.preview-image').attr('src', editnode.getURL());
		modalDiv.find('.color-component.red-component input').val(utils.hexToRGBComponent(hex).r);
		modalDiv.find('.color-component.green-component input').val(utils.hexToRGBComponent(hex).g);
		modalDiv.find('.color-component.blue-component input').val(utils.hexToRGBComponent(hex).b);
		editnode.addColorForCircularIcons(modalDiv, '#' + hex);
	});	
	modalDiv.find('.color-component input').change(function() {
		var rElement = modalDiv.find('.color-component.red-component input');
		var gElement = modalDiv.find('.color-component.green-component input');
		var bElement = modalDiv.find('.color-component.blue-component input');
		var r = utils.validateComponent(rElement.val());
		var g = utils.validateComponent(gElement.val());
		var b = utils.validateComponent(bElement.val());
		var rgbstring = 'rgb(' + r + ',' +  g + ',' + b + ')';
		editnode.color = utils.rgb2int(rgbstring);
		modalDiv.find('.preview-image').attr('src', editnode.getURL());
		rElement.val(r);
		gElement.val(g);
		bElement.val(b);
		modalDiv.find('.color-preview').css('background-color', rgbstring);
		editnode.addColorForCircularIcons(modalDiv, rgbstring);
	});
	var hexval = utils.int2hex(color);
	if (modalDiv.find('#nodeColorPicker option[value="#' + hexval + '"]').length == 0) {
		modalDiv.find('#nodeColorPicker').append('<option value="#'+hexval+'">#' + hexval + '/>');
	}
	modalDiv.find('#nodeColorPicker').val('#'+hexval);
	modalDiv.find('.color-preview').css('background-color', '#'+hexval);
	modalDiv.find('.color-component.red-component input').val(utils.hexToRGBComponent(hexval).r);
	modalDiv.find('.color-component.green-component input').val(utils.hexToRGBComponent(hexval).g);
	modalDiv.find('.color-component.blue-component input').val(utils.hexToRGBComponent(hexval).b);
	//modalDiv.find('#nodeColorPicker').attr('uuid', uuid);	
}
CreateNodeEditDialog.prototype.setIcon = function(modalDiv, iconURL) {	
	var createNode = this;
	var iconsMap = this.currentTheme == "NoTypes" || this.currentTheme == "Baseline" ?
			this.iconsForBaselineThemesMap : this.iconsForCircularThemesMap;
	
	$.each(iconsMap, function (key, value) {
		if(value instanceof Array ) {
			var categoryWrapper = $('<div>').addClass('categoryWrapper').attr('data-category', key);
			var categoryNameSpan = $('<span>').addClass('categoryName').text(key);
			var br = $('<br>').attr('clear', 'all');
			categoryWrapper.append(categoryNameSpan).append(br);
			var iconRowDiv = $('<div>').addClass('inlinerows');
			var iconMoreDiv = $('<div>').addClass('more-icons-div hide');
			$.each(value, function (index,imageURL) {
				var iconSpan = $('<span>').addClass('node-icon');
				var iconImg = $('<img>').attr('src', imageURL);
				iconSpan.append(iconImg);
				if (imageURL == createNode.icon) {
					iconSpan.addClass('node-icon-selected');
				}
				if(index > 5) {
					iconMoreDiv.append(iconSpan);
				} else {
					iconRowDiv.append(iconSpan);
				}
			});
			iconRowDiv.append(iconMoreDiv);
			var moreImg = $('<img>').attr('src', '../images/icons/icons9.png').addClass('node-icon-more');
			var moreSpan = $('<span>').addClass('more-icon hide');
			moreSpan.append(moreImg);
			var lessIcon = $('<span>').addClass('node-icon-less hide');
			var lessIconText = $('<span>').addClass('node-icon-less-text').text('-');
			lessIcon.text('Less').append('<br>').append(lessIconText);
			if(value.length > 6) {
				moreSpan.show();
			}
			createNode.registerMoreIconClick(moreSpan);
			createNode.registerLessIconClick(lessIcon);
			iconRowDiv.append(moreSpan).append(lessIcon);
			categoryWrapper.append(iconRowDiv);
			modalDiv.find('.iconrow').append(categoryWrapper);
		}
	});
	
	
	modalDiv.find('.preview-image').attr('src', this.icon);	
}

CreateNodeEditDialog.prototype.getThemeOptionsXML = function(){
	var dataView = this;
	var url = "/Centrifuge/services/getappfileinfoasxml/resources/OptionSets/" + this.currentTheme + ".xml?asXML";		
	$.ajax({
		url: url,
		async : false,
		complete: function(data) {			
			dataView.themeOptionXML = data.responseText;			      
		}
	});
}
CreateNodeEditDialog.prototype.performThemeBasedAction = function(modalDiv){	
	var editNode = this;
	var themeBasedAttr = $(this.themeOptionXML).find('NodeType').filter(function() {
	    return $(this).attr('nodetype').toLowerCase() == editNode.nodeName.toLowerCase() ;
	});
	if (themeBasedAttr.length > 0) {
		modalDiv.find('#theme-enabled-node-msg').removeAttr('style');		
		modalDiv.find('.edit_node_accordion .accordion-group .accordion-toggle').removeAttr('data-toggle').removeAttr('href');
		
	} else {
		modalDiv.find('#theme-enabled-node-msg').css('display', 'none');
	}
	
	if (this.currentTheme == "Circular") {
		modalDiv.find('.edit_node_accordion div.accordion-group:nth-child(2)').remove();
		modalDiv.find('.node-icon img').css('background-color', '#' + utils.int2hex(editNode.color));
		modalDiv.find('.node-icon img').css('border-radius', '72px');
	}
	this.addColorForCircularIcons(modalDiv, '#' + utils.int2hex(editNode.color));
}
CreateNodeEditDialog.prototype.addColorForCircularIcons = function(modalDiv, color) {
	if (this.currentTheme == "Circular") {
		modalDiv.find('.node-icon img').css('background-color', color);
		modalDiv.find('.node-icon img').css('border-radius', '72px');
	}
}
CreateNodeEditDialog.prototype.registerMoreIconClick = function(moreSpan) {
	var nodeEdit = this;
	moreSpan.click(function() {
		var iconRowDiv = moreSpan.parent();
		var lessIcon = iconRowDiv.find('.node-icon-less');
		moreSpan.hide();
		iconRowDiv.find('.more-icons-div').slideToggle('fast', function() {
			lessIcon.show();
		});
	});
}
CreateNodeEditDialog.prototype.registerLessIconClick = function(lessIcon) {
	var nodeEdit = this;
	lessIcon.click(function() {
		var iconRowDiv = lessIcon.parent();
		var moreIcon = iconRowDiv.find('.more-icon');
		lessIcon.hide();
		iconRowDiv.find('.more-icons-div').slideToggle('fast', function() {
			moreIcon.show();
		});
	});
}
CreateNodeEditDialog.prototype.getURL = function () {
	var previewUrl = "/Centrifuge/WidgetControllerServlet?action=render&shape="+ this.shape + "&color=" + this.color +
	"&image=" + (this.icon == "null" ? "null" : encodeURI(this.icon));
	return previewUrl;
}
CreateNodeEditDialog.prototype.isValidate =  function(modalDiv){
	var nodeEditDialog = this;
	modalDiv.find('#treeFinish-ne').bind("change", function () {
		if(nodeEditDialog.toolTipTab.findIsValid() && nodeEditDialog.computedFieldsTab.findIsValid()){
			$(this).removeAttr('disabled');
		}
		else{
			$(this).attr('disabled','disabled');
		}
	});
}
