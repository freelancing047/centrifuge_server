function NodeEditorBasicsTab(nodeEditorDiv, nodeName, nodeDef){
	this.nodeEditorDiv = nodeEditorDiv;	
	this.nodeName = nodeName;
	this.vizdef = null;
	this.nodeDef = nodeDef;
}

NodeEditorBasicsTab.prototype.populateTabInfo = function(vizdef){
	this.vizdef = vizdef;
	this.initializeTab(this.getDataForTab());
	this.registerDropDownActions(this.nodeEditorDiv);
}

NodeEditorBasicsTab.prototype.getDataForTab = function(){
	var nodeDef;
	var basicsTab = this;
	$.each(this.vizdef.nodeDefs, function(){
		if(this.name == basicsTab.nodeName){
			nodeDef = this;
		}
	});
	var attributeDefs = nodeDef.attributeDefs;
	var tabData = {};
	var labelAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.Label");
	var idAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.ID");
	var typeAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.Type");
	var urlAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.URL");
	var docAttrIdx = utils.getAttributeDefIdx(attributeDefs, "csi.internal.Document");	
	
	if (labelAttrIdx != null) {		
		tabData.labelCat = attributeDefs[labelAttrIdx].fieldDef.fieldType;		
		tabData.labelValue = this.getValueForKey(attributeDefs[labelAttrIdx].fieldDef, tabData.labelCat);
	}	
	if(idAttrIdx != null) {
		tabData.idCat = attributeDefs[idAttrIdx].fieldDef.fieldType;
		tabData.idValue = this.getValueForKey(attributeDefs[idAttrIdx].fieldDef, tabData.idCat);
	}
	if(typeAttrIdx != null){
		tabData.typeCat = attributeDefs[typeAttrIdx].fieldDef.fieldType;
		tabData.typeValue = this.getValueForKey(attributeDefs[typeAttrIdx].fieldDef, tabData.typeCat);
	}
	if(urlAttrIdx != null){
		tabData.urlCat = attributeDefs[urlAttrIdx].fieldDef.fieldType;
		tabData.urlValue = this.getValueForKey(attributeDefs[urlAttrIdx].fieldDef, tabData.urlCat);		
	}
	if(docAttrIdx != null){
		tabData.docCat = attributeDefs[docAttrIdx].fieldDef.fieldType;
		tabData.docValue = this.getValueForKey(attributeDefs[docAttrIdx].fieldDef, tabData.docCat);
	}
	
	return tabData;	
}

NodeEditorBasicsTab.prototype.getValueForKey = function(fieldDef,type){
	var value;
	if(type == "STATIC"){
		value = fieldDef.staticText;
	}else{
		value = fieldDef.fieldName;
	}
	return value;
}

NodeEditorBasicsTab.prototype.initializeTab = function(basicInfo){
	var linksTab = this;
	var labelCat;
	var labelValue;
	var idCat;
	var idValue;
	var typeCat;
	var typeValue;
	var urlCat;
	var docCat;
	var urlValue = "";
	var docValue = "";
	
	if(basicInfo == null){		
		labelCat = "COLUMN_REF";
		labelValue = this.nodeName;
		idCat = "COLUMN_REF";
		idValue = this.nodeName;	
		typeCat = "STATIC";
		typeValue = this.nodeName;
		urlCat = "NONE";
		docCat = "NONE";		
	}
	
	if(basicInfo != null) {
		labelCat = basicInfo.labelCat;
		labelValue = basicInfo.labelValue;
		idCat = basicInfo.idCat;
		idValue = basicInfo.idValue;
		
	}
	if(basicInfo != null && basicInfo.typeCat == undefined){
		typeCat = "NONE";
	}else{
		typeCat = basicInfo.typeCat;
		typeValue = basicInfo.typeValue;
	}
	if(basicInfo != null && basicInfo.urlCat == undefined){
		urlCat = "NONE";
	}else{
		urlCat = basicInfo.urlCat;
		urlValue = basicInfo.urlValue;
	}
	if(basicInfo != null && basicInfo.docCat == undefined){
		docCat = "NONE";
	}else{
		docCat = basicInfo.docCat;
		docValue = basicInfo.docValue;
	}
	
	this.nodeEditorDiv.find("#labelCategory option").each(function() {
		this.selected = (this.value == labelCat);
	});	
	if(labelCat == "STATIC"){
		this.nodeEditorDiv.find("#labelValue").attr('style','display:none;');
		this.nodeEditorDiv.find('#labelText').removeAttr('style').val(labelValue);
	}else if(labelCat == "COLUMN_REF"){
		this.nodeEditorDiv.find('#labelText').attr('style','display:none;');		
		this.populateColumnsDropDown("labelCategory", this.nodeEditorDiv);
		this.nodeEditorDiv.find("#labelValue option").each(function() {
			this.selected = (this.text == labelValue);
		});		
	}
	
	this.nodeEditorDiv.find("#idCategory option").each(function() {
		this.selected = (this.value == idCat);
	});	
	if(idCat == "STATIC"){
		this.nodeEditorDiv.find("#idValue").attr('style','display:none;');
		this.nodeEditorDiv.find('#idText').removeAttr('style').val(idValue);
	}else if(idCat == "COLUMN_REF"){
		this.nodeEditorDiv.find('#idText').attr('style','display:none;');
		this.nodeEditorDiv.find("#idValue").removeAttr('style');
		this.populateColumnsDropDown("idCategory", this.nodeEditorDiv);
		this.nodeEditorDiv.find("#idValue option").each(function() {
			this.selected = (this.text == idValue);
		});		
	}
	
	
	this.nodeEditorDiv.find("#typeCategory option").each(function() {
		this.selected = (this.value == typeCat);
	});
	if(typeCat == "STATIC"){
		this.nodeEditorDiv.find("#typeValue").attr('style','display:none;');
		this.nodeEditorDiv.find('#typeText').removeAttr('style').val(typeValue);
	}else if(typeCat == "COLUMN_REF"){
		this.nodeEditorDiv.find('#typeText').attr('style','display:none;');
		this.nodeEditorDiv.find("#typeValue").removeAttr('style');
		this.populateColumnsDropDown("typeCategory", this.nodeEditorDiv);
		this.nodeEditorDiv.find("#typeValue option").each(function() {
			this.selected = (this.text == typeValue);
		});	
	}else{
		this.nodeEditorDiv.find('#typeText').attr('style','display:none;');
		this.nodeEditorDiv.find("#typeValue").attr('style','display:none;');
	}
	
	
	this.nodeEditorDiv.find("#urlCategory option").each(function() {
		this.selected = (this.value == urlCat);
	});
	if(urlCat == "STATIC"){
		this.nodeEditorDiv.find("#urlValue").attr('style','display:none;');
		this.nodeEditorDiv.find('#urlText').removeAttr('style').val(urlValue);
	}else if(urlCat == "COLUMN_REF"){
		this.nodeEditorDiv.find('#urlText').attr('style','display:none;');
		this.nodeEditorDiv.find("#urlValue").removeAttr('style');
		this.populateColumnsDropDown("urlCategory", this.nodeEditorDiv);
		this.nodeEditorDiv.find("#urlValue option").each(function() {
			this.selected = (this.text == urlValue);
		});	
	}else{
		this.nodeEditorDiv.find('#urlText').attr('style','display:none;');
		this.nodeEditorDiv.find("#urlValue").attr('style','display:none;');
	}
	
	if (urlCat != "NONE") {
		this.nodeEditorDiv.find('.urlTextDiv').removeAttr('style');
	} else {
		this.nodeEditorDiv.find('.urlTextDiv').attr('style' , 'display : none;');
	}
	
	this.nodeEditorDiv.find("#docCategory option").each(function() {
		this.selected = (this.value == docCat);
	});
	if(docCat == "STATIC"){
		this.nodeEditorDiv.find("#docValue").attr('style','display:none;');
		this.nodeEditorDiv.find('#docText').removeAttr('style').val(docValue);
	}else if(docCat == "COLUMN_REF"){
		this.nodeEditorDiv.find('#docText').attr('style','display:none;');
		this.nodeEditorDiv.find("#docValue").removeAttr('style');
		this.populateColumnsDropDown("docCategory", this.nodeEditorDiv);
		this.nodeEditorDiv.find("#docValue option").each(function() {
			this.selected = (this.text == docValue);
		});	
	}else{
		this.nodeEditorDiv.find('#docText').attr('style','display:none;');
		this.nodeEditorDiv.find("#docValue").attr('style','display:none;');
	}
	
	if(this.nodeDef.addPrefixId) {
		this.nodeEditorDiv.find('#multi-type-node').attr('checked', false);
	}else {
		this.nodeEditorDiv.find('#multi-type-node').attr('checked', true);
	}
}

NodeEditorBasicsTab.prototype.populateColumnsDropDown = function(id, modalDiv){
	var columnsDropDown = modalDiv.find('#'+id).parent().find('select:eq(1)').removeAttr('style').empty();
	$.each(window.dataview.myData.resultData.meta.modelDef.fieldDefs, function() {
		columnsDropDown.append($('<option></option>').val(this.fieldName).append(this.fieldName)[0].outerHTML);	
	});			
}
NodeEditorBasicsTab.prototype.registerDropDownActions = function(modalDiv){
	var basicsTab = this;
	modalDiv.find('#labelCategory,#idCategory,#typeCategory,#urlCategory,#docCategory').change(function(){
		var id = $(this).attr('id');
		var value = $(this).val();		
		if(value == "STATIC"){
			if(id == "labelCategory" || id == "idCategory" || id == "typeCategory"){				
				$(this).parent().find('input').val(basicsTab.nodeName).removeAttr('style');
				$(this).parent().find('select:eq(1)').attr('style','display:none;');
			}else if (id == "urlCategory" || id == "docCategory"){
				$(this).parent().find('select:eq(1)').attr('style','display:none;');
				$(this).parent().find('input').removeAttr('style');
			}
		}else if(value == "COLUMN_REF" ){
			basicsTab.populateColumnsDropDown(id,modalDiv);
			$(this).parent().find('select:eq(1)').find("option").each(function() {
				this.selected = (this.text == basicsTab.nodeName);
			});
			$(this).parent().find('input').attr('style','display:none;');
		}else if(value == "NONE"){
			$(this).parent().find('select:eq(1)').attr('style','display:none;');
			$(this).parent().find('input').attr('style','display:none;');
		}
	});
	modalDiv.find('#urlCategory').change(function() {
		if($(this).val() == "NONE") {
			modalDiv.find('.urlTextDiv').hide();
		} else {
			modalDiv.find('.urlTextDiv').show();
		}
	});
	
}
NodeEditorBasicsTab.prototype.doSave = function(visualization){	
	var basicsTab = this;
	var labelCat = this.nodeEditorDiv.find('#labelCategory').val();
	var labelValue = labelCat == "STATIC" ? this.nodeEditorDiv.find('#labelText').val() : this.nodeEditorDiv.find('#labelValue').val();
	var idCat = this.nodeEditorDiv.find('#idCategory').val();
	var idValue = idCat == "STATIC" ? this.nodeEditorDiv.find('#idText').val() : this.nodeEditorDiv.find('#idValue').val();
	var typeCat = this.nodeEditorDiv.find('#typeCategory').val();
	var typeValue = null;
	if(typeCat == "STATIC"){
		typeValue = this.nodeEditorDiv.find('#typeText').val();
	}else if (typeCat == "COLUMN_REF"){
		typeValue = this.nodeEditorDiv.find('#typeValue').val();
	}
	var urlCat = this.nodeEditorDiv.find('#urlCategory').val();
	var urlValue = null;
	if(urlCat == "STATIC"){
		urlValue = this.nodeEditorDiv.find('#urlText').val();
	}else if(urlCat == "COLUMN_REF"){
		urlValue = this.nodeEditorDiv.find('#urlValue').val();
	}
	var docCat = this.nodeEditorDiv.find('#docCategory').val();
	var docValue = null;
	if(docCat == "STATIC"){
		docValue = this.nodeEditorDiv.find('#docText').val();
	}else if(docCat == "COLUMN_REF"){
		docValue = this.nodeEditorDiv.find('#docValue').val();
	}
	
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var nodeDefIdx = utils.getNodeDefIdx(visualization.nodeDefs, this.nodeName);
	var nodeDef = visualization.nodeDefs[nodeDefIdx];	
	var attributeDefsArr = new Array();
	$.each(nodeDef.attributeDefs,function(index, attributeDef){
		if (attributeDef != undefined) {
			var fieldDef;
			if(attributeDef.name == "csi.internal.Label"){				
				if(labelCat == "COLUMN_REF"){
					fieldDef = utils.getFieldDef(fieldDefs, labelValue);
				}else{
					fieldDef = basicsTab.getFieldDef(labelCat, labelValue);
				}
				visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;				
				
			}else if(attributeDef.name == "csi.internal.ID"){				
				if(idCat == "COLUMN_REF"){
					fieldDef = utils.getFieldDef(fieldDefs, idValue);
				}else{
					fieldDef = basicsTab.getFieldDef(idCat, idValue);
				}
				visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;				
				
			}else if(attributeDef.name == "csi.internal.Type"){
				if(typeCat == "COLUMN_REF"){
					fieldDef = utils.getFieldDef(fieldDefs, typeValue);
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				} else if(typeCat == "STATIC"){
					fieldDef = basicsTab.getFieldDef(typeCat, typeValue);
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				} else {
					visualization.nodeDefs[nodeDefIdx].attributeDefs.splice(index,1);
				}				
			}else if(attributeDef.name == "csi.internal.URL"){
				if(urlCat == "COLUMN_REF"){
					fieldDef = utils.getFieldDef(fieldDefs, urlValue);
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				}else if(urlCat == "STATIC"){
					fieldDef = basicsTab.getFieldDef(urlCat, urlValue);	
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				} else {
					visualization.nodeDefs[nodeDefIdx].attributeDefs.splice(index,1);
				}
				
				
			}else if(attributeDef.name == "csi.internal.Document"){
				if(docCat == "COLUMN_REF"){
					fieldDef = utils.getFieldDef(fieldDefs, docValue);
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				}else if(docCat == "STATIC"){
					fieldDef = basicsTab.getFieldDef(docCat, docValue);
					visualization.nodeDefs[nodeDefIdx].attributeDefs[index].fieldDef = fieldDef;
				} else {
					visualization.nodeDefs[nodeDefIdx].attributeDefs.splice(index,1);
				}			
			}
		}
	});	
	if(urlCat != "NONE" && utils.getAttributeDefIdx(nodeDef.attributeDefs ,"csi.internal.URL") == null){
		visualization.nodeDefs[nodeDefIdx].attributeDefs.push(this.getAttributeDef("csi.internal.URL", urlCat, urlValue));		
	}
	if(typeCat != "NONE" && utils.getAttributeDefIdx(nodeDef.attributeDefs ,"csi.internal.Type") == null){
		visualization.nodeDefs[nodeDefIdx].attributeDefs.push(this.getAttributeDef("csi.internal.Type", typeCat, typeValue));
	}
	if(docCat != "NONE" && utils.getAttributeDefIdx(nodeDef.attributeDefs ,"csi.internal.Document") == null){
		visualization.nodeDefs[nodeDefIdx].attributeDefs.push(this.getAttributeDef("csi.internal.Document", docCat, docValue));
	}	
	visualization.nodeDefs[nodeDefIdx].addPrefixId = !(this.nodeEditorDiv.find('#multi-type-node').is(':checked'));
	return visualization;
}

NodeEditorBasicsTab.prototype.getAttributeDef = function(attributeName,type, value){
	var attributeDef = new Object();
	attributeDef.class = "csi.server.common.model.attribute.AttributeDef";
	attributeDef.clientProperties = {};
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

NodeEditorBasicsTab.prototype.getFieldDef = function(type, name){	
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
NodeEditorBasicsTab.prototype.getConcatFunctionJSON = function() {
	var cf = new Object();
	cf.class = "csi.server.common.model.ConcatFunction";
	cf.clientProperties = new Object();
	cf.fields = [];
	cf.name = null;
	cf.ordinal = 0;
	cf.separator = "&#32;";
	cf.sortedFields = [];
	cf.uuid = utils.guidGenerator();
	return cf;
}
NodeEditorBasicsTab.prototype.isAttributeDefExists = function(key, nodeDef){
	var flag = false;
	$.each(linkDef.attributeDefs, function(){
		if(key == this.name){
			flag = true;
			return;
		}
	});
	return flag;
}

