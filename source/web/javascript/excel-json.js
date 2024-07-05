function ExcelJson() {
}
ExcelJson.prototype.getConnectionDefJSON = function(excelID, excelName) {
	var myJSON = new Object();
	myJSON["class"]="csi.server.common.model.ConnectionDef"; 
	myJSON["clientProperties"] = new Object();
	myJSON["connectString"]=null; 
	myJSON["createDate"]=null;
	myJSON["lastOpenDate"]=null;
	myJSON["lastUpdateDate"]=null;
	myJSON["name"]=null;
	myJSON["postSql"]=null;
	myJSON["preSql"]=null;
	var properties = new Object();
	properties["class"]="csi.server.common.model.GenericProperties"; 
	properties["clientProperties"] = new Object();
	
	var p1 = new Object();
	p1["class"]="csi.server.common.model.Property"; 
	p1["clientProperties"] = new Object();
	p1["name"]="csi.filetoken";
	p1["uuid"]=utils.guidGenerator();
	p1["value"]=excelID;
	
	var p2 = new Object();
	p2["class"]="csi.server.common.model.Property"; 
	p2["clientProperties"] = new Object();
	p2["name"]="csi.remoteFilePath";
	p2["uuid"]=utils.guidGenerator();
	p2["value"]=excelName;
	
	var p3 = new Object();
	p3["class"]="csi.server.common.model.Property"; 
	p3["clientProperties"] = new Object();
	p3["name"]="csi.runtime.username";
	p3["uuid"]=utils.guidGenerator();
	p3["value"]="";

	var p4 = new Object();
	p4["class"]="csi.server.common.model.Property"; 
	p4["clientProperties"] = new Object();
	p4["name"]="csi.runtime.password";
	p4["uuid"]=utils.guidGenerator();
	p4["value"]="";

	properties["properties"] = [p1, p2, p3, p4];
	properties["uuid"]=utils.guidGenerator();
	myJSON["properties"]=properties;
	
	myJSON["remarks"]=null;
	myJSON["type"]="excel";
	myJSON["uuid"]=utils.guidGenerator();
	return JSON.stringify(myJSON);
}
