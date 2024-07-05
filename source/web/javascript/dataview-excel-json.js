function DataViewExcelJson(sourceName, sourceId, tableName, columns, dataviewName) {
       this.utils = new Utils();
       this.sourceName=sourceName;
       this.sourceId = sourceId;
       this.tableName = tableName;
       this.columns = columns;
       this.dataViewName = dataviewName; 
       this.connections = JSON.parse(new ExcelJson().getConnectionDefJSON(sourceId, sourceName));
       this.source = this.getDataSourcesJSON();
       this.tableDef = this.getTableDefJSON();
}

DataViewExcelJson.prototype.getDataSourcesJSON = function() {
	dataSourcesObj=new Object();
	dataSourcesObj["class"]="csi.server.common.model.DataSourceDef";
	dataSourcesObj["clientProperties"]=new Object();
	dataSourcesObj["connection"]=this.connections;
	dataSourcesObj["createDate"]=null;
	dataSourcesObj["lastOpenDate"]=null;
	dataSourcesObj["lastUpdateDate"]=null;
	dataSourcesObj["localId"]=this.utils.guidGenerator(); // DataSourceLocalID
	dataSourcesObj["mergeMap"]=[];
	dataSourcesObj["name"]=this.sourceName;
	dataSourcesObj["ordinal"]=0;
	dataSourcesObj["query"]=this.getQueryJSON();
	dataSourcesObj["remarks"]=null;
	dataSourcesObj["sqlTables"]=[];
	dataSourcesObj["uuid"]=this.utils.guidGenerator();
	return dataSourcesObj;
}

DataViewExcelJson.prototype.getDataSetOpsJSON = function(){
	dataSetOps = [];
	dataSetOpsObj = new Object();
	dataSetOpsObj["appendAll"]=false;
	dataSetOpsObj["childType"]=null;
	dataSetOpsObj["children"]=[];
	dataSetOpsObj["class"]="csi.server.common.model.DataSetOp";
	dataSetOpsObj["clientProperties"]=new Object();
	dataSetOpsObj["containsTables"]=false;
	dataSetOpsObj["joinType"]="EQUI_JOIN";
	dataSetOpsObj["localId"]=this.utils.guidGenerator();
	dataSetOpsObj["mapItems"]=[];
	dataSetOpsObj["mapType"]="APPEND";
	dataSetOpsObj["name"]="DataSetOp: "+this.tableName;
	dataSetOpsObj["tableDef"]=this.tableDef;
	dataSetOpsObj["uuid"]=this.utils.guidGenerator();
	dataSetOps.push(dataSetOpsObj);
	return dataSetOps;	
}

DataViewExcelJson.prototype.getTableDefJSON = function(){
	tableDef = new Object();
	tableDef["alias"]=null;
	tableDef["catalogName"]=null;
	tableDef["class"]="csi.server.common.model.tableview.TableDef";
	tableDef["clientProperties"]=new Object();
	tableDef["columns"]=this.columns;
	tableDef["customQuery"]=null;
	tableDef["localId"]=this.utils.guidGenerator();		// TableLocalId
	tableDef["schemaName"]=null;
	tableDef["source"]=this.source;
	tableDef["sqlTable"]=false;
	tableDef["tableName"]=this.tableName;
	tableDef["tableType"]="Worksheet";
	tableDef["uuid"]=this.utils.guidGenerator();
	return tableDef;
}

DataViewExcelJson.prototype.getQueryJSON = function() {
	query = new Object();
	this.utils.guidGenerator();
	query["class"]="csi.server.common.model.query.QueryDef";
	query["clientProperties"]=new Object();
	query["createDate"] = null;
	query["interceptors"] = [];
	query["lastOpenDate"] = null;
	query["lastUpdateDate"] = null;
	query["name"] = null;
	query["parameterValueMap"] = new Object();
	query["parameters"] = [];
	query["parametersByName"] = new Object();
	query["properties"] = this.getQueryPropertiesJSON();
	query["queryText"] = null;
	query["queryType"] = null;
	query["remarks"] = null;
	query["sql"] = null;
	query["timeout"] = 0;
	query["uuid"]=this.utils.guidGenerator();
	return query;

}
DataViewExcelJson.prototype.getQueryPropertiesJSON = function() {
	properties = new Object();
	properties["class"]="csi.server.common.model.GenericProperties";
	properties["clientProperties"]=new Object();
	properties["properties"] = [];
	properties["propertiesMap"] = new Object();	
	properties["uuid"]=this.utils.guidGenerator();
	return properties;
}
DataViewExcelJson.prototype.getClientPropertiesJSON = function() {
	cproperties = new Object();
	cproperties["isValid"] = true;
	return cproperties;
}
DataViewExcelJson.prototype.getConcatFunctionJSON = function() {
	cf = new Object();
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
DataViewExcelJson.prototype.getWorksheetJSON = function() {
	ws = new Object();
	ws["annotations"] = null;
	ws["class"] =  "csi.server.common.model.worksheet.WorksheetDef";
	ws["clientProperties"] = new Object();
	ws["uuid"] = this.utils.guidGenerator();
	ws["visualizations"] = null;
	ws["worksheetName"] = "Untitled Worksheet";
	return ws;
}
DataViewExcelJson.prototype.getMetaJSON = function() {
	meta = new Object();	
	meta["class"]= "csi.server.common.model.dataview.DataViewDef";
	meta["clientProperties"] = new Object();
	meta["createDate"] = null;
	meta["dataSetOps"] = this.getDataSetOpsJSON();
	meta["dataSetParameters"] = [];
	meta["dataSources"] = [this.source];
	meta["extensionConfigs"] = [];
	meta["extensionData"] = [];
	meta["lastOpenDate"] = null;
	meta["lastUpdateDate"] = null;
	meta["modelDef"] = this.getDMDJSON();
	meta["name"] = null;
	meta["remarks"] = null;
	meta["template"] = false;
	meta["uuid"] = this.utils.guidGenerator();
	return meta;
}
DataViewExcelJson.prototype.getDMDJSON = function() {
	dmd= new Object();	
	dmd["class"]= "csi.server.common.model.DataModelDef";
	dmd["clientProperties"] = new Object();
	dmd["createDate"] = null;
	dmd["fieldDefs"] = this.getFieldDefsJSON();
	dmd["googleMapsViewDef"] = null;
	dmd["initialViewIndex"] = 0;
	dmd["lastOpenDate"] = null;
	dmd["lastUpdateDate"] = null;
	dmd["name"] = null;
	dmd["remarks"] = null;
	dmd["sketchViewDef"] = null;
	dmd["uuid"] = this.utils.guidGenerator();
	dmd["visualizations"] = [];
	dmd["worksheets"] = [this.getWorksheetJSON()];
	return dmd;
}
DataViewExcelJson.prototype.getDVJSON = function() {
	dv = new Object();
	dv["class"] = "csi.server.common.model.dataview.DataView";
	dv["clientProperties"] = this.getClientPropertiesJSON();
	dv["createDate"] = null;
	dv["lastOpenDate"] = new Date().getTime();
	dv["lastUpdateDate"] = null;
	dv["meta"] = this.getMetaJSON();
	dv["name"] = this.dataViewName;
	dv["needsRefresh"] = true;
	dv["remarks"] = null;
	dv["spinoff"] = false;
	dv["type"] = "BASIC";
	dv["uuid"] = this.utils.guidGenerator();
	dv["version"] = null;
	
	return dv;
}
DataViewExcelJson.prototype.getFieldDefsJSON = function() {
	fieldDefs = [];
	dvo = this;
	$.each(dvo.columns, function(index, value) {
		fieldDef = new Object();
		fieldDef["anonymous"] = false;
		fieldDef["cacheOrdinal"] = null;
		fieldDef["cacheScale"]= 0;
		fieldDef["cacheSize"]= 0;
		fieldDef["cacheType"] =null;
		fieldDef["class"] ="csi.server.common.model.FieldDef";
		clientProperties = new Object();
		clientProperties["tableName"] = dvo.tableName;
		clientProperties["sourceName"] = dvo.sourceName;
		clientProperties["tableType"] = "Worksheet";
		fieldDef["clientProperties"] = clientProperties;
		guid = dvo.utils.guidGenerator();
		fieldDef["columnLocalId"] = guid;
		fieldDef["columnName"] = null;
		fieldDef["displayFormat"] = null;
		fieldDef["dsLocalId"] = dvo.source["localId"];
		fieldDef["dsLocalName"] = null;
		fieldDef["fieldName"] = value["columnName"];
		fieldDef["fieldType"] = "COLUMN_REF";
		fieldDef["functionType"] = "CONCAT";
		fieldDef["functions"] = [dvo.getConcatFunctionJSON()];
		fieldDef["hiddenConditional"] = null;
		fieldDef["ordinal"] = index;
		fieldDef["rawScript"] = false;
		fieldDef["scriptSeparator"] = null;
		fieldDef["scriptText"] = null;
		fieldDef["scriptType"] = null;
		fieldDef["staticText"] = null;
		fieldDef["tableLocalId"] = dvo.tableDef["localId"];
		fieldDef["uuid"] = dvo.utils.guidGenerator();
		fieldDef["valueType"] = value["overrideCsiType"];
		clientProperties1 = new Object();
		clientProperties1["tableType"] = "Worksheet";
		//setting values into column
		value["clientProperties"] = clientProperties1;
		value["localId"] = guid;//we never get this so setting a new id
		value["columnFilters"] = [];//we never get this so setting 
		value["selected"] = true;
		fieldDefs.push(fieldDef);
	});
	return fieldDefs;
}
