function RelationGraphJson(data){
	this.data = data;	
	this.utils = new Utils();
	this.nodes = [];
	this.nodeNames = [];
	this.nodesMap = new Object();
	this.position = $('#new-visualization-dialogue').data( 'position');
	$('#new-visualization-dialogue').removeData('position');
}
RelationGraphJson.prototype.getLinkAndNodeDefJson = function(visualDefJson){
	var rgJson = this;
	//remove all links and nodes not in the rg settings
	this.removeLinksAndNodesJson(visualDefJson);
	//store all the nodes
	$.each(visualDefJson.nodeDefs, function(index, nodeDef){
		rgJson.nodesMap[nodeDef.name] = nodeDef;
		rgJson.nodeNames.push(nodeDef.name);
		rgJson.nodes.push(nodeDef);
	});
	//all the new links
	var links = this.createLinkDefsJson(visualDefJson);
	if (links.length > 0){
		$.each(links, function(index, link){
			visualDefJson['linkDefs'].push(link);
		});
	}	
	visualDefJson['nodeDefs'] = this.nodes;
	return visualDefJson;
}
RelationGraphJson.prototype.getVisualDefJson = function(){
	var visualDefJson = new Object();
	visualDefJson.bundleDefs = [];
	visualDefJson['class'] = "csi.server.common.model.visualization.RelGraphViewDef";
	var clientProps = new Object();
	clientProps['render.threshold'] = 2000;
	clientProps['vizBox.height'] = 500;
	clientProps['vizBox.left'] = 383;
	clientProps['vizBox.loadOnStartup'] = $('#alertModal').find('#loadOnStartUp').is(":checked");
	clientProps['vizBox.top'] = 10;
	clientProps['vizBox.width'] = 800;
	visualDefJson.clientProperties = clientProps;
	visualDefJson.filterFields = [];
	visualDefJson.isAttached = false;
	visualDefJson.linkDefs = this.createLinkDefsJson();
	
	visualDefJson.name = "Relationship Graph";
	visualDefJson.position = this.position;
	visualDefJson.optionSetName = "Circular";
	visualDefJson.type = "RELGRAPH_V2";
	visualDefJson.uuid = this.utils.guidGenerator();
	visualDefJson.nodeDefs = this.getNodeDefsJson();
	visualDefJson.playerSettings = this.getPlayerSettingsJson();
	visualDefJson.settings = this.getSettingsJson();
	visualDefJson.selectionInfo = this.getSelectionInfoJson();	
	
	return visualDefJson;
	
}

RelationGraphJson.prototype.getSelectionInfoJson = function(){
	var selectionInfo = new Object();
	selectionInfo['class'] = "csi.server.common.model.SelectionInfo";	
	selectionInfo['deselectedItems'] =  [];    
	selectionInfo['selectAll'] = false;
	selectionInfo['selectedItems'] = [];
	selectionInfo['uuid'] = this.utils.guidGenerator();
	return selectionInfo;
}


RelationGraphJson.prototype.getNodeDefsJson = function(){		
	return this.nodes;
}

RelationGraphJson.prototype.getPlayerSettingsJson = function(){
	var playerSettings = new Object();
	playerSettings['class'] = "csi.server.common.model.GraphPlayerSettings";
	playerSettings['durationNumber'] = 1;
	playerSettings['durationPeriod'] = "entire";
	playerSettings['frameSizeNumber'] = 1;
	playerSettings['frameSizePeriod'] = "year";
	playerSettings['hideNonVisibleItems'] = false;
	playerSettings['initializedByClient'] = false;
	playerSettings['playbackEnd'] = 0;
	playerSettings['playbackMax'] = 0;
	playerSettings['playbackMin'] = 0;
	playerSettings['playbackMode'] = "CUMULATIVE";
	playerSettings['playbackStart'] = 0;
	playerSettings['speed'] = 1000;
	playerSettings['stepMode'] = "RELATIVE";
	playerSettings['stepSizeNumber'] = 1;
	playerSettings['stepSizePeriod'] = "millisecond";
	playerSettings['uuid'] = this.utils.guidGenerator();
	playerSettings['clientProperties'] = {};	
	return playerSettings;
}

RelationGraphJson.prototype.getSettingsJson = function(){
	var settings = new Object();
	settings['class'] = "csi.server.common.model.GenericProperties";
	settings['clientProperties'] = {};
	var properties = new Object();
	properties['class'] = "csi.server.common.model.Property";
	properties['clientProperties'] = {};
	properties['name'] = "csi.relgraph.backgroundColor";
	properties['uuid'] = this.utils.guidGenerator();
	properties['value'] = "16777215";
	settings['properties'] = [properties];
	settings['uuid'] = this.utils.guidGenerator();
	return settings;
}

RelationGraphJson.prototype.removeLinksAndNodesJson = function(visualDefJson){
	var nodesWithPos = getallNodesWithPosition();
	var nodesDefs = [];
	var linkDefs = [];
	var operations = [];
	var rg = this;
	//Remove nodes which were removed
	$.each(nodesWithPos.nodes, function(){
		var node = rg.getNodeIfPresent(visualDefJson, this.name);
		if (node != null || node != undefined){
			nodesDefs.push(node);
			var attributeDef = rg.getAttributeByName("csi.internal.xPos", node);
			attributeDef.fieldDef.staticText = this.position.left + "";
			attributeDef = rg.getAttributeByName("csi.internal.yPos", node);
			attributeDef.fieldDef.staticText = this.position.top + "";
		}
	});
	visualDefJson['nodeDefs'] = nodesDefs;
	//Remove links which were removed
	$.each(visualDefJson['linkDefs'], function(){
		if (rg.getNodeIfPresent(visualDefJson, this.nodeDef1.name) != null && rg.getNodeIfPresent(visualDefJson, this.nodeDef2.name) != null) {
			linkDefs.push(this);
		}
	});
	visualDefJson['linkDefs'] = linkDefs;
	//Remove bundleDefs.operations for removed nodes
	if (visualDefJson.bundleDefs.length > 0){
		$.each(visualDefJson.bundleDefs[0].operations, function(){
			if(rg.getNodeIfPresent(visualDefJson, this.nodeDef.name)!=null){
				operations.push(this);
			}
		});
		visualDefJson.bundleDefs[0].operations = operations;
	}
	return visualDefJson;
}
RelationGraphJson.prototype.getAttributeByName = function(attributeName, node) {
	var attributeDef = null;
	$.each(node.attributeDefs, function(){
		if (this.name == attributeName){
			attributeDef = this;
			return;
		}
	});
	return attributeDef;
}
RelationGraphJson.prototype.getNodeIfPresent = function(visualDefJson, nodeName) {
	var node = null;
	$.each(visualDefJson['nodeDefs'], function(index, value){
		if (value['name'] == nodeName) {
			node = value;
			return;
		}
	});
	return node;
}
RelationGraphJson.prototype.createLinkDefsJson = function(visualDefJson){
	var linkDefsJson = [];
	var nodeAttrMap = new Object();
	var nodesWithPos = getallNodesWithPosition();
	$.each(nodesWithPos['nodes'], function(index, value){
		nodeAttrMap[value['name']] = value;
	});
	var connection = getAllConnections();
	var connectionsJson = connection['connections'];
	
	for(var i = 0; i < connectionsJson.length; i++) {
		if (!this.isLinkPresent(connectionsJson[i], visualDefJson)) {
			linkDefsJson.push(this.createLinkDefJson(connectionsJson[i], nodeAttrMap));
		}
	}	
	return linkDefsJson;
}
RelationGraphJson.prototype.isLinkPresent = function(connection, visualDefJson){
	if (visualDefJson == undefined) {
		return false;
	}
	var linkIsPresent = false;
	var start = connection[0];
	var end = connection[1];
	var linkDefs = visualDefJson['linkDefs'];
	$.each(linkDefs, function(index, linkDef){
		if ( (linkDef['nodeDef1']['name'] == start && linkDef['nodeDef2']['name'] == end)
				|| (linkDef['nodeDef2']['name'] == start && linkDef['nodeDef1']['name'] == end)) {
			linkIsPresent = true;
			return;
		}
	});
	return linkIsPresent;
}
RelationGraphJson.prototype.createLinkDefJson = function(connection, nodeAttrMap, nodeDef1, nodeDef2){
	var linkDefJson = new Object();
	linkDefJson['attributeDefs'] = [this.getAttributeJson("csi.internal.Type", "Link", null, "STATIC")];
	linkDefJson['clientProperties'] = {};//TODO:- set the client props 
	linkDefJson['nodeDef1'] = nodeDef1 == null || nodeDef1 == undefined ? this.getNodeDefJson(connection[0], nodeAttrMap[connection[0]]) : nodeDef1;
	linkDefJson['nodeDef2'] = nodeDef2 == null || nodeDef2 == undefined ? this.getNodeDefJson(connection[1], nodeAttrMap[connection[1]]) : nodeDef2;
	linkDefJson['uuid'] = this.utils.guidGenerator();
	linkDefJson['attributeQueries'] = [];
	linkDefJson['moreDetailQueries'] = [];
	linkDefJson['name'] = linkDefJson['uuid'];
	linkDefJson['class'] = "csi.server.common.model.LinkDef";
	linkDefJson.attributeDefsAsMap = this.getAttributeDefsAsMap(linkDefJson.attributeDefs);
	return linkDefJson;
	
}

RelationGraphJson.prototype.getAttributeJson = function(name, staticText, fieldName, fieldType, valueType){
	var attributeDefJson = new Object();
	attributeDefJson['clientProperties'] = {};
	attributeDefJson['class'] = "csi.server.common.model.attribute.AttributeDef";
	attributeDefJson["bySize"] = false;
	attributeDefJson["byTransparency"] = false;
	attributeDefJson["byStatic"] = true;
	attributeDefJson['hideEmptyInTooltip'] = false;
	attributeDefJson['includeInTooltip'] = true;
	attributeDefJson['defaultIncludeInTooltip'] = true;
	attributeDefJson['name'] = name;
	attributeDefJson['uuid'] = this.utils.guidGenerator();	
	attributeDefJson['fieldDef'] = this.getFieldDefJson(staticText,fieldName, fieldType, valueType);//this.getFieldDefJson(staticText,"columnLocalId", "dsLocalId", "tableLocalId", fieldName, fieldType, valueType ); 
	return attributeDefJson;
}

RelationGraphJson.prototype.getFieldDefJson = function(staticText,fieldName,fieldType, valueType){
	if(fieldName == null){
		return this.createFieldDefJson(staticText,fieldType);
	}
	var fieldDefJson = this.data['resultData']['meta']['modelDef']['fieldDefMap'][fieldName.toLowerCase()];
	//fieldDefJson['functions'][0]['uuid'] = this.utils.guidGenerator();
	//fieldDefJson['uuid'] = this.utils.guidGenerator();
	if(staticText != null){
		fieldDefJson['staticText'] = staticText;
	}
	if(fieldName != null){
		fieldDefJson['fieldName'] = fieldName;
	}		
	return fieldDefJson;
}

RelationGraphJson.prototype.createFieldDefJson = function(staticText, fieldType, valueType){
	var fieldDefJson = new Object();
	fieldDefJson['anonymous'] = true;
	fieldDefJson['cacheScale'] = 0;
	fieldDefJson['cacheSize'] = 0;
	fieldDefJson['class'] = "csi.server.common.model.FieldDef";	
	fieldDefJson['clientProperties'] = {} 	
	fieldDefJson['functionType'] = "CONCAT";
	fieldDefJson['functions'] = [this.getConcatFunctionJSON()];
	fieldDefJson['ordinal'] = 0;
	fieldDefJson['rawScript'] = false;
	fieldDefJson['staticText'] = staticText;//"Link";
	fieldDefJson['uuid'] = this.utils.guidGenerator();	
	fieldDefJson['fieldType'] = fieldType;
	fieldDefJson['valueType'] = valueType;
	return fieldDefJson;
	
}

RelationGraphJson.prototype.getConcatFunctionJSON = function() {
	var cf = new Object();
	cf["class"]="csi.server.common.model.ConcatFunction";
	cf["clientProperties"] = new Object();
	cf["fields"]=[];
	cf["name"] = null;
	cf["ordinal"] = 0;
	cf["separator"] = "&#32;";
	cf["sortedFields"] = [];
	cf["uuid"]=this.utils.guidGenerator();
	return cf;
}

RelationGraphJson.prototype.getNodeDefJson = function(fieldName, nodeAttr){
	
	if(this.nodesMap[fieldName] != null){
		return this.nodesMap[fieldName];
	}
	var nodeDefJson = new Object();
	nodeDefJson['addPrefixId'] = false;
	nodeDefJson['clientProperties'] = {};
	nodeDefJson['hideLabels'] = false;
	nodeDefJson['initiallyHidden'] = false;
	nodeDefJson['moreDetailQueries'] = [];
	nodeDefJson['name'] = fieldName;	
	nodeDefJson['uuid'] = this.utils.guidGenerator();
	var attributeDefs = [];	
	attributeDefs.push(this.getAttributeJson("csi.internal.xPos", nodeAttr.position.left+"" , null,"STATIC", null)); //TODO:-set the value of staticText correctly
	attributeDefs.push(this.getAttributeJson("csi.internal.yPos",nodeAttr.position.top +"" , null, "STATIC", null)); //TODO:-set the value of staticText correctly
	attributeDefs.push(this.getAttributeJson("csi.internal.Label", null, fieldName, "COLUMN_REF", "string"));
	attributeDefs.push(this.getAttributeJson("csi.internal.ID", null, fieldName, "COLUMN_REF", "string"));
	attributeDefs.push(this.getAttributeJson("csi.internal.Type", fieldName, null, "STATIC", null));
	attributeDefs.push(this.getAttributeJson("csi.internal.Shape", nodeAttr.shape , null, "STATIC", "string"));
	attributeDefs.push(this.getAttributeJson("csi.internal.Color", nodeAttr.color, null, "STATIC", "number")); //TODO:-set the value of staticText correctly
	nodeDefJson["attributeDefs"] = attributeDefs;
	nodeDefJson['attributeDefsAsMap'] = this.getAttributeDefsAsMap(attributeDefs);
	nodeDefJson['attributeQueries'] = [];
	nodeDefJson['class'] = "csi.server.common.model.NodeDef";
	this.nodesMap[fieldName] = nodeDefJson;
	this.nodes.push(nodeDefJson);
	this.nodeNames.push(fieldName);
	return nodeDefJson;	
	
}
RelationGraphJson.prototype.getAttributeDefsAsMap = function(attributeDefs){
	var attributeDefsMap = new Object();
	for(i = 0; i < attributeDefs.length; i++){
		var attributeDef = attributeDefs[i];
		attributeDefsMap[attributeDef['name']] = attributeDef['fieldDef'];
	}
	return attributeDefsMap;
	
}
RelationGraphJson.prototype.getDummyVisualDefJson = function(){
	var visualDefJson = new Object();
	visualDefJson.bundleDefs = [];
	visualDefJson['class'] = "csi.server.common.model.visualization.RelGraphViewDef";
	var clientProps = new Object();
	clientProps['render.threshold'] = 2000;
	clientProps['vizBox.height'] = 500;
	clientProps['vizBox.left'] = 383;
	clientProps['vizBox.loadOnStartup'] = $('#alertModal').find('#loadOnStartUp').is(":checked");
	clientProps['vizBox.top'] = 10;
	clientProps['vizBox.width'] = 800;
	visualDefJson.clientProperties = clientProps;
	visualDefJson.filterFields = [];
	visualDefJson.isAttached = false;
	visualDefJson.nodeDefs = [];
	visualDefJson.linkDefs = [];	
	visualDefJson.name = "Relationship Graph";
	visualDefJson.position = this.position;
	visualDefJson.optionSetName = "Circular";
	visualDefJson.type = "RELGRAPH_V2";
	visualDefJson.uuid = utils.guidGenerator();	
	visualDefJson.playerSettings = this.getPlayerSettingsJson();
	visualDefJson.settings = this.getSettingsJson();
	visualDefJson.selectionInfo = this.getSelectionInfoJson();	
	return visualDefJson;
	
}