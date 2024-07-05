function LinkEditorLinksTab(linkEditorDiv, linkName, vizdef){
	this.linkEditorDiv = linkEditorDiv;
	this.linkName = linkName;
	this.linkNameWithOutWhiteSpaces = linkName.replace(/[^a-z0-9]/gi, '');
	this.vizdef = vizdef;
	this.source = null;
	this.target = null;
}

LinkEditorLinksTab.prototype.populateTabInfo = function(source,target){
	this.source = source;
	this.target = target;
	this.initializeTab(this.getDataForTab());
}

LinkEditorLinksTab.prototype.getDataForTab = function(){
	var linkDef;
	var linksTab = this;
	$.each(this.vizdef.linkDefs, function(index, link){
		if ((link.nodeDef1.name == linksTab.source) && (link.nodeDef2.name==linksTab.target)){
			linkDef = link;
		}
	});
	var linksUpArr = [];
	$.each(linkDef.moreDetailQueries, function(){
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

LinkEditorLinksTab.prototype.doSave = function(visualization){
	var linksTab = this;
	var oTable = this.linkEditorDiv.find('#link-linkups-table').dataTable();
	if (oTable.fnSettings() == null){
		return;
	}
	var data = oTable.fnGetData();
	var moreDetailQueries = new Array();
	$.each(data, function(index, rowData){		
		moreDetailQueries.push(linksTab.createMoreDetailQuery(rowData[0], rowData[1], rowData[2], rowData[3], rowData[4]));
	});
	var linkDefIdx = linksTab.getLinkDefIdx(visualization.linkDefs);
	var linkDef = visualization.linkDefs[linkDefIdx];
	visualization.linkDefs[linkDefIdx].moreDetailQueries = moreDetailQueries;
	return visualization;
}
LinkEditorLinksTab.prototype.createMoreDetailQuery = function(dvName, dvId, source, target, uuid){	
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

LinkEditorLinksTab.prototype.getTabInfo = function(){
	var data = this.linkEditorDiv.find('#link-linkups-table').dataTable().fnGetData();
	var tabInfoArr = new Array();
	$.each(data, function(index, rowData){
		var row = new Object();
		row.linkTo_text = rowData[0];
		row.linkTo_uuid = rowData[1];
		row.source = rowData[2];
		row.target = rowData[3];
		row.uuid = rowData[4];
		tabInfoArr.push(row);
	});
	return tabInfoArr;
}

LinkEditorLinksTab.prototype.initializeTab = function(linkTabInfo){
	var linksTab = this;
	var dataTable = this.linkEditorDiv.find('#link-linkups-table').dataTable({
		"aoColumns" : [
		                   {"sTitle" : "Name"},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"bVisible" : false},
		                   {"fnRender": function (obj) {
		                       return $('<a>').append($('<img>').attr('src','/Centrifuge/images/node-edit.png').attr('id','edit-links').addClass('edit'))[0].outerHTML;
		                   	},
		                   	"bSortable": false,
		                   	"sWidth" : "5px"
		                   },
		                   {"fnRender": function (obj ) {
		                       return $('<a>').append($('<img>').attr('src','/Centrifuge/images/node-delete.png').attr('id','delete-links').addClass('delete'))[0].outerHTML;
		                   	},
		                   	"bSortable": false,
		                   	"sWidth" : "5px"
		                   }
		                  ],
		"bPaginate": false,
		"fnCreatedRow" : function(nRow, aData, iDisplayIndex,iDisplayIndexFull) {
			$(nRow).find('#edit-links').click(function(event) {
				 var oTable = linksTab.linkEditorDiv.find('#link-linkups-table').dataTable();
				 var data = oTable.fnGetData(this.parentNode.parentNode.parentNode);
				 var rowIndex = oTable.fnGetPosition(this.parentNode.parentNode.parentNode);
				 linksTab.createLinkPopup(rowIndex,data[0], data[1], data[2], data[3], linksTab.linkEditorDiv);	
			});	
			$(nRow).find('#delete-links').click(function(event) {
		    	 var oTable = linksTab.linkEditorDiv.find('#link-linkups-table').dataTable();
		    	 var index = oTable.fnGetPosition(this.parentNode.parentNode.parentNode);
		    	 oTable.fnDeleteRow(index);		    	  	
			});	
		}
	});	
	if(linkTabInfo.length != 0){
		dataTable.fnAddData(linkTabInfo);
		//this.registerActionsForDataTable(dataTable);	
	}
	
	this.linkEditorDiv.find('#addLinkups-links').click(function(){		
		linksTab.createLinkPopup("add", null, null, null, null, linksTab.linkEditorDiv);		
	});	
}

LinkEditorLinksTab.prototype.createLinkPopup = function(id, linkToText, linkTo, sourceField, targetField, modalDiv){
	if(modalDiv.find('#editLinksup-links'+ this.linkNameWithOutWhiteSpaces).length != 0){
		modalDiv.find('#editLinksup-links'+ this.linkNameWithOutWhiteSpaces).modal();
		return;
	}
	var modalPop = modalDiv.find('#link-linkups-popup').clone();
	modalPop.attr('id','editLinksup-links'+ this.linkNameWithOutWhiteSpaces);
	modalDiv.append(modalPop);
	this.populateAddLinkPopup(id, linkToText, linkTo, sourceField, targetField, modalDiv);
	modalPop.modal();
}

LinkEditorLinksTab.prototype.populateAddLinkPopup = function(id, linkToText, selectedLinkTo, selectedSourceField, selectedTargetField, modalDiv){
	var linksTab = this;
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#dvList-links').empty();
	var url = "/Centrifuge/actions/dataviewdef/listDataViewDefs2?_f=json";
	$.get(url, function(data){
		var dvDropdown = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#dvList-links');
		$.each(data.resultData, function(){			
			if(selectedLinkTo != null && selectedLinkTo == this.uuid ){
				dvDropdown.append($('<option>').val(this.uuid).attr('selected','selected').append(this.name)[0].outerHTML);
			}else{
				dvDropdown.append($('<option>').val(this.uuid).append(this.name)[0].outerHTML);
			}
			
		});
	});
	
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find("#updateFields-links").click(function(){
		var selectedLinkTo = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#dvList-links').val();
		linksTab.populateSourceAndTargetFields(id, selectedLinkTo, null, null, linksTab, modalDiv);
	});
	
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find("#updateLink-links").click(function(){
		var dataTable = modalDiv.find('#link-linkups-table').dataTable();
		var linkTo = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#dvList-links option:selected').text();
		var linkToId = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#dvList-links').val();
		var sourceField = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#sourceField-links').val();
		var targetField = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#targetField-links').val();
		
		if(id == "add"){
			dataTable.fnAddData([linkTo, linkToId, sourceField, targetField, null, null, null]);
		}else{
			var uuid = dataTable.fnGetData(id)[4];
			dataTable.fnUpdate([linkTo, linkToId, sourceField, targetField,uuid, null, null], id);
		}		
		
		//linksTab.registerActionsForDataTable(dataTable);
		modalDiv.find('#editLinksup-links'+ linksTab.linkNameWithOutWhiteSpaces).modal('hide');
		modalDiv.find('#editLinksup-links'+ linksTab.linkNameWithOutWhiteSpaces).remove();
	});
	
	
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find("#cancelUpdate-links").click(function(){
		modalDiv.find('#editLinksup-links'+ linksTab.linkNameWithOutWhiteSpaces).modal('hide');		
	});
		
	if(id != "add"){
		this.populateSourceAndTargetFields(id, selectedLinkTo, selectedSourceField, selectedTargetField, linksTab, modalDiv);
	}
}

LinkEditorLinksTab.prototype.registerActionsForDataTable = function(dataTable){
	var linksTab = this;
	$(dataTable.fnGetLinks()).find('img.edit').click( function(e){ 
		var data = dataTable.fnGetData(this.parentNode.parentNode.parentNode);
		var rowIndex = dataTable.fnGetPosition(this.parentNode.parentNode.parentNode);
		linksTab.createLinkPopup(rowIndex,data[0], data[1], data[2], data[3], linksTab.linkEditorDiv);			
		
	});
	
	var nTr = dataTable.fnGetLinks();
	var last; 
	$.each(nTr,function(key,value){
		last = value;
	 });
    $(last).find("img.delete").click(function(event) {
      	  var index = dataTable.fnGetPosition(this.parentNode.parentNode.parentNode);
      	  dataTable.fnDeleteRow(index);
      	  linksTab.linkEditorDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).remove();
      	  event.stopPropagation();
	 });
}

LinkEditorLinksTab.prototype.populateSourceAndTargetFields = function(id, selectedLinkTo, selectedSourceField, selectedTargetField, linksTab, modalDiv){
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#sourceField-links').find('option').remove();
	modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#sourceField-links').append(linksTab.generateColumnsOptions());
	var url = "/Centrifuge/actions/dataviewdef/getDataViewDef?_f=json&uuid=" + selectedLinkTo; //uuid
	$.get(url, function(data){
		var targetFieldDropDown = modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find('#targetField-links').empty();
		var cols = _.reject(window.dataview.myData.resultData.meta.modelDef.fieldDefs,function(obj){ return (_.isNull(obj.fieldName) || _.isEmpty(obj.fieldName))});
		$(cols).each(function(){
			if(selectedTargetField != null && selectedTargetField == this.fieldName){
				targetFieldDropDown.append($('<option>').val(this.fieldName).attr('selected','selected').append(this.fieldName)[0].outerHTML);
			}else{
				targetFieldDropDown.append($('<option>').val(this.fieldName).append(this.fieldName)[0].outerHTML);
			}
		});			
	});
	if(selectedSourceField != null){
		modalDiv.find('#editLinksup-links' + linksTab.linkNameWithOutWhiteSpaces).find("#sourceField-links option").each(function() {
			this.selected = (this.text == selectedSourceField);
		});
	}	
}

LinkEditorLinksTab.prototype.getLinkDefIdx = function( linkDefs ){
    var linkIdx = null;
    var source = this.source;
	var target = this.target;
    $.each(linkDefs, function(index, value) {
    	if (value.nodeDef1.name == source && value.nodeDef2.name==target) {
          linkIdx = index;
          return;
    	}
    });
    return linkIdx;
}

LinkEditorLinksTab.prototype.generateColumnsOptions = function(){
	var columnOptions = "";
	$.each(window.dataview.myData.resultData.meta.modelDef.fieldDefs, function() {
		if(this.fieldType == "COLUMN_REF"){
			columnOptions += $('<option>').val(this.fieldName).append(this.fieldName)[0].outerHTML;	
		}
	});
	return columnOptions;
}