function NodeEditorLinksTab(nodeEditorDiv, nodeEditorDialogId, nodeName){
	this.nodeEditorDiv = nodeEditorDiv;
	this.nodeEditorDialogId = nodeEditorDialogId;
	this.nodeName = nodeName;
	this.nodeNameWithOutWhiteSpaces = nodeName.replace(/[^a-z0-9]/gi, '');
	this.vizdef = null;
}

NodeEditorLinksTab.prototype.populateTabInfo = function(vizdef){
	this.vizdef = vizdef;
	this.initializeTab(this.getDataForTab());
}

NodeEditorLinksTab.prototype.getDataForTab = function(){
	var nodeDef;
	var linksTab = this;
	$.each(this.vizdef.nodeDefs, function(){
		if(this.name == linksTab.nodeName){
			nodeDef = this;
		}
	});
	var linksUpArr = new Array();
	$.each(nodeDef.moreDetailQueries, function(){
		var row = new Array();
		row.push(this.dataViewName);
		row.push(this.dataViewDefId);
		row.push(this.paramMap[0].fieldDef.fieldName);
		row.push(this.paramMap[0].paramName);
		row.push(this.uuid);
		row.push(null);
		row.push(null);
		linksUpArr.push(row);
	});
	return linksUpArr;
}

NodeEditorLinksTab.prototype.doSave = function(visualization){
	var linksTab = this;
	var oTable = this.nodeEditorDiv.find('#linksup').dataTable();
	if (oTable.fnSettings() == null){
		return;
	}
	var data = oTable.fnGetData();
	var moreDetailQueries = new Array();
	$.each(data, function(index, rowData){		
		moreDetailQueries.push(linksTab.createMoreDetailQuery(rowData[0], rowData[1], rowData[2], rowData[3], rowData[4]));
	});
	var nodeDefIdx = utils.getNodeDefIdx(visualization.nodeDefs, this.nodeName);
	var nodeDef = visualization.nodeDefs[nodeDefIdx];
	visualization.nodeDefs[nodeDefIdx].moreDetailQueries = moreDetailQueries;
	return visualization;
}

NodeEditorLinksTab.prototype.createMoreDetailQuery = function(dvName, dvId, source, target, uuid){	
	var moredetailQuery = new Object();
	moredetailQuery.class = "csi.server.common.model.MoreDetailQuery";
	moredetailQuery.clientProperties = {};
	moredetailQuery.dataViewDefId = dvId;
	moredetailQuery.dataViewName = dvName;
	moredetailQuery.isDynamic = true;
	moredetailQuery.uuid = uuid == null ? utils.guidGenerator() : uuid;
	var paramMapEntry = new Object();
	paramMapEntry.class = "csi.server.common.model.ParamMapEntry";
	paramMapEntry.clientProperties = {};
	paramMapEntry.paramName = target;
	paramMapEntry.paramOrdinal = 0;
	paramMapEntry.uuid = utils.guidGenerator();
	paramMapEntry.fieldDef = utils.getFieldDef(window.dataview.myData.resultData.meta.modelDef.fieldDefs, source);
	moredetailQuery.paramMap = [paramMapEntry];
	return moredetailQuery;
}

NodeEditorLinksTab.prototype.getTabInfo = function(){
	var data = this.nodeEditorDiv.find('#linksup').dataTable().fnGetData();
	var tabInfoArr = new Array();
	$.each(data, function(){
		var row = new Object();
		row.linkTo_text = this[0];
		row.linkTo_uuid = this[1];
		row.source = this[2];
		row.target = this[3];
		row.uuid = this[4];
		tabInfoArr.push(row);
	});
	return tabInfoArr;
}

NodeEditorLinksTab.prototype.initializeTab = function(linkTabInfo){
	var linksTab = this;
	var dataTable = this.nodeEditorDiv.find('#linksup').dataTable({
		"aoColumns" : [
		                   {"sTitle" : "Name"},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"fnRender": function (obj) {
		                       return $('<a></a>').attr('href','#').append($('<img></img>').attr('id','edit').addClass('edit').attr('src','/Centrifuge/images/node-edit.png'))[0].outerHTML;
		                   	},
		                   	"bSortable": false,
		                   	"sWidth" : "5px"
		                   
		                   },
		                   {"fnRender": function (obj ) {
		                       return $('<a></a>').attr('href','#').append($('<img></img>').attr('id','delete').addClass('delete').attr('src','/Centrifuge/images/node-delete.png'))[0].outerHTML;
		                   	},
		                   	"bSortable": false,
		                   	"sWidth" : "5px"
		                   	
		                   }
		                   
		                  ],
		 "bPaginate": false,
		 "fnCreatedRow" : function(nRow, aData, iDisplayIndex,iDisplayIndexFull) {
			 $(nRow).find("#edit").click(function() {
				 var oTable = $("#linksup").dataTable();
				 var data = oTable.fnGetData($(this).parent().parent().parent()[0]);
				 var rowIndex = oTable.fnGetPosition($(this).parent().parent().parent()[0]);
				 linksTab.createLinkPopup(rowIndex,data[0], data[1], data[2], data[3], linksTab.nodeEditorDiv);	
			 });	
			 $(nRow).find("#delete").click(function() {
		    	 var oTable = $("#linksup").dataTable();
		    	 var index = oTable.fnGetPosition($(this).parent().parent().parent()[0]);
		    	 oTable.fnDeleteRow(index);		    	  	
			 });	
		 }
	});	
	if(linkTabInfo.length != 0){
		dataTable.fnAddData(linkTabInfo);
	}
	this.nodeEditorDiv.find('#addLinkBtn').click(function(){		
		linksTab.createLinkPopup("add", null, null, null, null, linksTab.nodeEditorDiv);		
	});	
}

NodeEditorLinksTab.prototype.createLinkPopup = function(id, linkToText, linkTo, sourceField, targetField, modalDiv){
	if(modalDiv.find('#editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id).length != 0){
		modalDiv.find('#editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id).modal();
		return;
	}
	var modalPop = modalDiv.find('#myModalPop').clone();
	modalPop.attr('id','editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id);
	modalDiv.append(modalPop);
	this.populateAddLinkPopup(id, linkToText, linkTo, sourceField, targetField, modalDiv);
	modalPop.modal();
}

NodeEditorLinksTab.prototype.populateAddLinkPopup = function(id, linkToText, selectedLinkTo, selectedSourceField, selectedTargetField, modalDiv){
	var linksTab = this;
	var url = "/Centrifuge/actions/dataviewdef/listDataViewDefs2?_f=json";
	
	$.get(url, function(data){
		var dvDropdown = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#dvList').empty();
		$.each(data.resultData, function(){			
			if(selectedLinkTo != null && selectedLinkTo == this.uuid ){
				dvDropdown.append($('<option></option>').val(this.uuid).attr('selected','selected').append(this.name)[0].outerHTML);
			}else{
				dvDropdown.append($('<option></option>').val(this.uuid).append(this.name)[0].outerHTML);
			}
		});
	});
	
	modalDiv.find('#editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id).find("#updateFields").click(function(){
		var selectedLinkTo = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#dvList').val();
		linksTab.populateSourceAndTargetFields(id, selectedLinkTo, null, null, linksTab, modalDiv);
	});
	
	modalDiv.find('#editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id).find("#updateLink").click(function(){
		var dataTable = modalDiv.find('#linksup').dataTable();
		var linkTo = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#dvList option:selected').text();
		var linkToId = modalDiv.find('#editLinksup'+ linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#dvList').val();
		var sourceField = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#sourceField').val();
		var targetField = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#targetField').val();
		
		if(id == "add"){
			dataTable.fnAddData([linkTo, linkToId, sourceField, targetField, null, null, null]);
		}else{
			var uuid = dataTable.fnGetData(id)[4];
			dataTable.fnUpdate([linkTo, linkToId, sourceField, targetField,uuid, null, null], id);
		}		
		
		//linksTab.registerActionsForDataTable(dataTable);
		
		modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).modal('hide');
		modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).remove();
	});
	
	
	modalDiv.find('#editLinksup' + this.nodeEditorDialogId + this.nodeNameWithOutWhiteSpaces + id).find("#cancelUpdate").click(function(){
		modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).modal('hide');		
	});
		
	if(id != "add"){
		this.populateSourceAndTargetFields(id, selectedLinkTo, selectedSourceField, selectedTargetField, linksTab, modalDiv);
	}
	
	
}

NodeEditorLinksTab.prototype.registerActionsForDataTable = function(dataTable){
	var linksTab = this;
	$(dataTable.fnGetNodes()).find('img.edit').click( function(){ 
		var data = dataTable.fnGetData($(this).parent().parent().parent()[0]);
		var rowIndex = dataTable.fnGetPosition($(this).parent().parent().parent()[0]);
		linksTab.createLinkPopup(rowIndex,data[0], data[1], data[2], data[3], linksTab.nodeEditorDiv);			
		
	});
	
	var nTrs = dataTable.fnGetNodes();
	var last; 
	$.each(nTrs,function(){
		last = this;
	 });
    $(last).find("img.delete").click(function(event) {
      	  var index = dataTable.fnGetPosition($(this).parent().parent().parent()[0]);
      	  dataTable.fnDeleteRow(index);
      	  linksTab.nodeEditorDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + index).remove();
      	  event.stopPropagation();
	 });
}

NodeEditorLinksTab.prototype.populateSourceAndTargetFields = function(id, selectedLinkTo, selectedSourceField, selectedTargetField, linksTab, modalDiv){
	modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#sourceField').find('option').remove();
	modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#sourceField').append(utils.getFieldNameColumnOptions(window.dataview.myData.resultData.meta.modelDef.fieldDefs));
	var url = "/Centrifuge/actions/dataviewdef/getDataViewDef?_f=json&uuid=" + selectedLinkTo; //uuid
	$.get(url, function(data){
		var targetFieldDropDown = modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find('#targetField').empty();
		var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))});
		$.each(cols, function(){
			if(selectedTargetField != null && selectedTargetField == this.fieldName){
				targetFieldDropDown.append($('<option></option>').val(this.fieldName).attr('selected','selected').append(this.fieldName)[0].outerHTML);
			}else{
				targetFieldDropDown.append($('<option></option>').val(this.fieldName).append(this.fieldName)[0].outerHTML);
			}
		});			
	});
	if(selectedSourceField != null){
		modalDiv.find('#editLinksup' + linksTab.nodeEditorDialogId + linksTab.nodeNameWithOutWhiteSpaces + id).find("#sourceField option").each(function() {
			this.selected = (this.text == selectedSourceField);
		});
	}	
}