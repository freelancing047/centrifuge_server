function LinkEditorBasicsTab(linkEditorDiv, vizdef){
	this.linkEditorDiv = linkEditorDiv;	
	this.vizdef = vizdef;
	this.sourceNode;
	this.targetNode;
	this.linkName;
}

LinkEditorBasicsTab.prototype.populateTabInfo = function(sourceNode, targetNode){	
	this.sourceNode = sourceNode;
	this.targetNode = targetNode;
	this.initializeTab(this.getDataForTab(sourceNode, targetNode));
	this.registerDropDownActions(this.linkEditorDiv);
}

LinkEditorBasicsTab.prototype.getDataForTab = function(sourceNode, targetNode){
	var linkDef;
	var sourceNode = sourceNode;
	var targetNode = targetNode;
	var basicsTab = this;
	$.each(this.vizdef.linkDefs, function(){
		if(this.nodeDef1.name == sourceNode && this.nodeDef2.name == targetNode){
			linkDef = this;
		}
	});
	this.linkName = linkDef.name;
	var attributeDefs = linkDef.attributeDefs;
	var tabData = new Object();
	var labelAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.Label");	
	var typeAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.Type");
	if (labelAttrIdx != null){
		tabData.labelCat = attributeDefs[labelAttrIdx].fieldDef.fieldType;		
		tabData.labelValue = this.getValueForKey(attributeDefs[labelAttrIdx].fieldDef, tabData.labelCat);
	}	
	if (typeAttrIdx != null){
		tabData.typeCat = attributeDefs[typeAttrIdx].fieldDef.fieldType;
		tabData.typeValue = this.getValueForKey(attributeDefs[typeAttrIdx].fieldDef, tabData.typeCat);
	}
	return tabData;	
}

LinkEditorBasicsTab.prototype.initializeTab = function(basicInfo){
	var linksTab = this;
	var labelCat;
	var labelValue;
	var typeCat;
	var typeValue;
	if(basicInfo == null){		
		labelCat = "COLUMN_REF";
		labelValue = this.linkName;
		typeCat = "STATIC";
		typeValue = this.linkName;
	}
	if(basicInfo != null && basicInfo.labelCat == undefined){
		labelCat = "NONE";
	}
	else if(basicInfo != null) {
		labelCat = basicInfo.labelCat;
		labelValue = basicInfo.labelValue;		
	}
	if(basicInfo != null && basicInfo.typeCat == undefined){
		typeCat = "NONE";
	}else{
		typeCat = basicInfo.typeCat;
		typeValue = basicInfo.typeValue;
	}
	this.linkEditorDiv.find("#link-label-category option").each(function() {
		this.selected = (this.value == labelCat);
	});	
	if(labelCat == "STATIC"){
		this.linkEditorDiv.find("#link-label-value").attr('style','display:none;');
		this.linkEditorDiv.find('#link-label-text').removeAttr('style').val(labelValue);
	}else if(labelCat == "COLUMN_REF"){
		this.linkEditorDiv.find('#link-label-text').attr('style','display:none;');		
		this.populateColumnsDropDown("link-label-category", this.linkEditorDiv);
		this.linkEditorDiv.find("#link-label-value option").each(function() {
			this.selected = (this.text == labelValue);
		});		
	}
	this.linkEditorDiv.find("#link-type-category option").each(function() {
		this.selected = (this.value == typeCat);
	});
	if(typeCat == "STATIC"){
		this.linkEditorDiv.find("#link-type-value").attr('style','display:none;');
		this.linkEditorDiv.find('#link-type-text').removeAttr('style').val(typeValue);
	}else if(typeCat == "COLUMN_REF"){
		this.linkEditorDiv.find('#link-type-text').attr('style','display:none;');
		this.linkEditorDiv.find("#link-type-value").removeAttr('style');
		this.populateColumnsDropDown("link-type-category", this.linkEditorDiv);
		this.linkEditorDiv.find("#link-type-value option").each(function() {
			this.selected = (this.text == typeValue);
		});	
	}else{
		this.linkEditorDiv.find('#link-type-text').attr('style','display:none;');
		this.linkEditorDiv.find("#link-type-value").attr('style','display:none;');
	}		
}


LinkEditorBasicsTab.prototype.registerDropDownActions = function(modalDiv){
	var basicsTab = this;
	modalDiv.find('#link-label-category,#link-type-category').change(function(){
		var id = $(this).attr('id');
		var value = $(this).val();		
		if(value == "STATIC"){
			if(id == "link-label-category" || id == "link-type-category"){				
				$(this).parent().find('input').val("").removeAttr('style');
				$(this).parent().find('select:eq(1)').attr('style','display:none;');
			}
		}else if(value == "COLUMN_REF" ){
			basicsTab.populateColumnsDropDown(id,modalDiv);
			$(this).parent().find('select:eq(1)').find("option").each(function() {
				this.selected = (this.text == basicsTab.linkName);
			});
			$(this).parent().find('input').attr('style','display:none;');
		}else if(value == "NONE"){
			$(this).parent().find('select:eq(1)').attr('style','display:none;');
			$(this).parent().find('input').attr('style','display:none;');
		}
		
	});
	
}

LinkEditorBasicsTab.prototype.populateColumnsDropDown = function(id, modalDiv){
	var columnsDropDown = modalDiv.find('#'+id).parent().find('select:eq(1)').removeAttr('style').empty();
	$.each(window.dataview.myData.resultData.meta.modelDef.fieldDefs, function() {
		if(this.fieldType == "COLUMN_REF"){
			columnsDropDown.append($('<option></option>').val(this.fieldName).append(this.fieldName)[0].outerHTML);	
		}
	});		
}

LinkEditorBasicsTab.prototype.doSave = function(visualization){	
	var basicsTab = this;
	var labelCat = this.linkEditorDiv.find('#link-label-category').val();
	var labelValue = labelCat == "STATIC" ? this.linkEditorDiv.find('#link-label-text').val() : this.linkEditorDiv.find('#link-label-value').val();
	var typeCat = this.linkEditorDiv.find('#link-type-category').val();
	var typeValue = null;
	if(typeCat == "STATIC"){
		typeValue = this.linkEditorDiv.find('#link-type-text').val();
	}else if (typeCat == "COLUMN_REF"){
		typeValue = this.linkEditorDiv.find('#link-type-value').val();
	}	
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;	
	var linkDefIdx = utils.getlinkDefIdx(visualization.linkDefs, this.sourceNode, this.targetNode);	
	var linkDef = visualization.linkDefs[linkDefIdx];	
	$.each(linkDef.attributeDefs,function(){			
		if(this){
			if(this.name == "csi.internal.Label"){	
				if(labelCat != "NONE"){
					if(labelCat == "COLUMN_REF"){
						this.fieldDef = utils.getFieldDef(fieldDefs, labelValue);
					}else if(labelCat == "STATIC"){
						this.fieldDef = basicsTab.getFieldDef(labelCat, labelValue);
					}
				}
				else{
					delete this;
				}
				
			}else if(this.name == "csi.internal.Type"){
				if(typeCat != "NONE"){
					if(typeCat == "COLUMN_REF"){
						this.fieldDef = utils.getFieldDef(fieldDefs, typeValue);					
					}else if(typeCat == "STATIC"){
						this.fieldDef = basicsTab.getFieldDef(typeCat, typeValue);					
					}
				}
				else{
					delete this;
				}
			}
		}
		
	});	
	if(labelCat != "NONE" && !basicsTab.isAttributeDefExists("csi.internal.Label", linkDef)){
		linkDef.attributeDefs.push(this.getAttributeDef("csi.internal.Label", labelCat, labelValue));
	}	
	if(typeCat != "NONE" && !basicsTab.isAttributeDefExists("csi.internal.Type", linkDef)){
		linkDef.attributeDefs.push(this.getAttributeDef("csi.internal.Type", typeCat, typeValue));
	}	
	return visualization;
}

LinkEditorBasicsTab.prototype.getFieldDef = function(type, name){	
	if (type == "STATIC"){
		var fieldDefJson = new Object();
		fieldDefJson.anonymous = true;
		fieldDefJson.cacheScale = 0;
		fieldDefJson.cacheSize = 0;
		fieldDefJson.class = "csi.server.common.model.FieldDef";	
		fieldDefJson.clientProperties = {} 	
		fieldDefJson.functionType = "CONCAT";
		fieldDefJson.functions = [this.getConcatFunctionJSON()];
		fieldDefJson.ordinal = 0;
		fieldDefJson.rawScript = false;
		fieldDefJson.staticText = name;
		fieldDefJson.uuid = utils.guidGenerator();	
		fieldDefJson.fieldType = type;
		return fieldDefJson;
	} else if(type = "COLUMN_REF"){
		return utils.getFieldDef(window.dataview.myData.resultData.meta.modelDef.fieldDefs, name);
	}	
}

LinkEditorBasicsTab.prototype.getAttributeDef = function(attributeName,type, value){
	var attributeDef = new Object();
	attributeDef.class = "csi.server.common.model.attribute.AttributeDef";
	attributeDefclientProperties = {};
	attributeDef.bySize = false;
	attributeDef.byTransparency = false;
	attributeDef.byStatic = true;
	attributeDef.defaultIncludeInTooltip  = true;
	attributeDef.hideEmptyInTooltip = true;
	attributeDef.includeInTooltip = true;
	attributeDef.name = attributeName;
	attributeDef.uuid = utils.guidGenerator();
	attributeDef.fieldDef = this.getFieldDef(type, value);
	return attributeDef;
}

LinkEditorBasicsTab.prototype.getConcatFunctionJSON = function() {
	var cf = new Object();
	cf.class="csi.server.common.model.ConcatFunction";
	cf.clientProperties = new Object();
	cf.fields=[];
	cf.name = null;
	cf.ordinal = 0;
	cf.separator = "&#32;";
	cf.sortedFields = [];
	cf.uuid = utils.guidGenerator();
	return cf;
}

LinkEditorBasicsTab.prototype.isAttributeDefExists = function(key, linkDef){
	var flag = false;
	$.each(linkDef.attributeDefs, function(){
		if(key == this.name){
			flag = true;
			return;
		}
	});
	return flag;
}

LinkEditorBasicsTab.prototype.isKeyExistsInMap = function(map, key){
	if(map[key]){
		return true;
	}
	return false;
}

LinkEditorBasicsTab.prototype.getValueForKey = function(fieldDef,type){
	var value;
	if(type == "STATIC"){
		value = fieldDef.staticText;
	}else{
		value = fieldDef.fieldName;
	}
	return value;
}
