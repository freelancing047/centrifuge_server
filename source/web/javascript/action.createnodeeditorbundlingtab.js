function NodeEditorBundlingTab(nodeEditorDiv, nodeName){
	this.nodeEditorDiv = nodeEditorDiv;
	this.nodeName = nodeName;
	this.vizdef = null;
}

NodeEditorBundlingTab.prototype.populateTabInfo = function(vizdef){
	this.vizdef = vizdef;
	this.initializeTab(this.getTableData());
}

NodeEditorBundlingTab.prototype.initializeTab = function(bundlingTabInfo){
	var bundleTab = this;
	var dataTable = this.nodeEditorDiv.find('#bundletab-table').dataTable({
		"aoColumns" : [ {
			 "sTitle" : "Field",
			 "mDataProp": "Field",									
		},  {
			 "sTitle" : "Delete",
			 "mDataProp": "Delete",
			 "sWidth" : "5px",
			 "fnRender" : function(obj) {
				obj.uuid = obj.aData.uuid;
				var sReturn = $('<img></img>').attr('src', '../images/node-delete.png').attr('id', '2')[0].outerHTML;
				return sReturn;
			 }
		} ],
		"fnCreatedRow": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			 var rowdata = aData;
			 $(nRow).find("#2").click(function(event) {
				 bundleTab.doDeleteBundle(dataTable, nRow);
			 });
			 $(nRow).click(function(event) {
			 	if($(nRow).hasClass('selected')) {
			 		$(nRow).removeClass('selected');
			 	} else {
			 		$(nRow).parent().find('tr').removeClass('selected');
			 		$(nRow).addClass('selected');
			 	}
			 });
			 $(nRow).find('td:eq(0)').live( 'click', function (event) {
				 bundleTab.doEditFieldName(dataTable, nRow, rowdata.fieldoptions)  
			 });	
			 $(nRow).find('td:eq(0)').find('#bundlefieldselect').live( 'focusout', function () {
				 bundleTab.doFocusOutFieldName(dataTable, this);
			 });
		},
		"sScrollY" : "100px",
		"aaData" : [],
		"sDom" : '<flp><"clear">',
		"bPaginate" : false,
		"aaSorting": []
	});
	if(bundlingTabInfo.length != 0){
		dataTable.fnAddData(bundlingTabInfo);
	}
	var modalDiv = this.nodeEditorDiv;
	this.nodeEditorDiv.find('#addBundlingBtn').click(function(){	
		var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	    var columnoptions = utils.getFieldNameColumnOptions(columns);
		var oTable = modalDiv.find('#bundletab-table').dataTable();
		var data = {};
		data.Field = columns[0].fieldName;
		data.Delete = "";
		data.fieldoptions = columnoptions;
		oTable.fnAddData(data);
		oTable.find('tr:last td:first').trigger('click');
	});
	this.registerBundleSort();
}
NodeEditorBundlingTab.prototype.registerBundleSort = function(){
	this.nodeEditorDiv.find('.sorting-block .move-up').click(function() {
		var currentSelected = $('.bundling-block #bundletab-table tbody tr.selected');
		var upperItem = currentSelected.prev();
		upperItem.before(currentSelected);
	});
	this.nodeEditorDiv.find('.sorting-block .move-down').click(function() {
		var currentSelected = $('.bundling-block #bundletab-table tbody tr.selected');
		var belowItem = currentSelected.next();
		belowItem.after(currentSelected);
	});
}
NodeEditorBundlingTab.prototype.swap = function(row1, row2) {
	var otable = $('#bundletab-table').dataTable();
	var pos1 = oTable.fnGetPosition(row1);
	var row1Data = oTable.fnGetData( pos1[0] );
	var pos2 = oTable.fnGetPosition(row2);
	var row2Data = oTable.fnGetData( pos2[0] );
	oTable.fnUpdate(row2Data, pos1);
	oTable.fnUpdate(row1Data, pos2);
	oTable.fnUpdate( ['a', 'b', 'c', 'd', 'e'], 1 );
}
NodeEditorBundlingTab.prototype.getTableData = function(){	
	var bundlingTab = this;
	var bundleDefArr = new Array();
	var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;	
	var columnoptions = utils.getFieldNameColumnOptions(columns);
	if(this.vizdef.bundleDefs.length > 0){
		$.each(this.vizdef.bundleDefs[0].operations, function(){
			if(this.nodeDef.name == bundlingTab.nodeName){
				var bundleData = new Object();
				bundleData.Field = this.field.fieldName;
				bundleData.uuid = this.uuid;
				bundleData.Delete = null;
				bundleData.fieldoptions = columnoptions;
				bundleDefArr.push(bundleData);
			}
		});
	}
	return bundleDefArr;
}

NodeEditorBundlingTab.prototype.doSave = function(vizdef) {
	var bundleTab = this;
	var nodeDefs = vizdef.nodeDefs;
	var nodeName = this.nodeName;
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var operations = this.getOps(vizdef,nodeName);
	var oTable = this.nodeEditorDiv.find('#bundletab-table').dataTable();
	if (oTable.fnSettings() == null){
		return;
	}
	var nTrs = oTable.fnGetNodes();        
	$.each(nTrs, function(){
		var oTable = bundleTab.nodeEditorDiv.find('#bundletab-table').dataTable();
		var uuid = utils.guidGenerator();
		if(oTable.fnGetData(this).uuid){
			uuid = oTable.fnGetData(this).uuid;
		}
        var fieldName = $(this).find('td:eq(0)').find('select').length > 0 ? $(this).find('td:eq(0)').find('select').val() : $(this).find('td:eq(0)').text();
        var nodeDef = nodeDefs[utils.getNodeDefIdx(nodeDefs, nodeName)];
        var fieldDef = utils.getFieldDef(fieldDefs,fieldName); 
        var newOps = {};
        newOps.class = "csi.server.common.model.bundle.BundleOp";
        newOps.clientProperties = {};
        newOps.expression = null;
        newOps.field = fieldDef;
        newOps.nodeDef = nodeDef;
        newOps.nodeType = null;
        newOps.priority = 0;
        newOps.uuid = uuid;
        operations.push(newOps);
	});  
	var bundleDefs = {};
	bundleDefs.bundleType = "BY_OBJECT";
	bundleDefs.class = "csi.server.common.model.bundle.BundleDef";
	bundleDefs.clientProperties = {};
	bundleDefs.operations = operations;
	if(vizdef.bundleDefs.length > 0 && vizdef.bundleDefs[0].uuid){
		bundleDefs.uuid = vizdef.bundleDefs[0].uuid;
	}
	else{
		bundleDefs.uuid = utils.guidGenerator();
	}
	return bundleDefs;
}

NodeEditorBundlingTab.prototype.triggerFocusOut = function(oTable , iDisplayIndex){
	var iDisplayIndex = iDisplayIndex;
	var oTable = oTable;
	var length = oTable.find('tr').length;
	if(length > 1){
		for(var i = 0; i < length-1; i++){
			if(i != iDisplayIndex){
				oTable.find('tbody tr:eq('+i+') td:eq(0)').find('select').focusout();			 
			}
		}
    }
}

NodeEditorBundlingTab.prototype.getNodeDef = function(nodeDefs, nodeName) {
	var nodDef = null;
	$.each(nodeDefs, function() { 
		if (this.name == nodeName ) {
			nodDef = this;
			return;
		}
	});
	return JSON.parse(JSON.stringify(nodDef));
}

NodeEditorBundlingTab.prototype.getOps = function(vizdef, nodeName) {
	var ops = vizdef.bundleDefs.length > 0 ? vizdef.bundleDefs[0].operations : null;
	var operations = [];
	if(ops != null){
		$.each(ops, function() { 
			if(this.nodeDef.name != nodeName){
				operations.push(this);
			}
		});
	}
	return operations;
}

NodeEditorBundlingTab.prototype.doEditFieldName = function(dataTable, nRow, fieldOptions) {
	if(dataTable) {	
	    var index = dataTable.fnGetPosition(nRow);
	    this.triggerFocusOut(dataTable, index);
	}	
    var data = $(nRow).find('td:eq(0)')[0].innerHTML;
    if($(nRow).find('#bundlefieldselect').length > 0){
    	return;
    }											  													  
    $(nRow).find('td:eq(0)').html($('<select></select').attr('id','bundlefieldselect').addClass('span2').append(fieldOptions));													 
	$(nRow).find('td:eq(0)').find('#bundlefieldselect option[value="'+data+'"]').attr("selected", "selected");	
}

NodeEditorBundlingTab.prototype.doFocusOutFieldName = function(dataTable, nTd) {
	if($(nTd).val() != null){
		if($(nTd).parent()[0] != null){
			var index = dataTable.fnGetPosition($(nTd).parent().parent()[0]);
			var newval = $(nTd).val();
			$(nTd).html('');
			dataTable.fnUpdate(newval, index, 0);
		}
	} 
}

NodeEditorBundlingTab.prototype.doDeleteBundle = function(dataTable, nRow) {
	var index = dataTable.fnGetPosition(nRow);
	dataTable.fnDeleteRow(index);
}
