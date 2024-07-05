function NodeEditorForComputedFields(modalDiv, nodeName, vizdef) {
	this.nodeName = nodeName;
	this.modalDiv = modalDiv;
	this.vizDef = vizdef;
	this.oTable;
	this.isValid = true;
}

NodeEditorForComputedFields.prototype.populateComputedFieldsTabInfo = function() {
	this.createComputedFieldsTab(this.modalDiv);
	this.oTable = this.modalDiv.find('#computed-fields-table').dataTable();
	this.initializeComputedFieldsActions();
	this.appendPreviousDataInTable(this.modalDiv);
}
NodeEditorForComputedFields.prototype.getFunctionObject = function() {
	var funData = {};
	funData["Absolute Average"] = {id:'ABS_AVG', value: 'Absolute Average'};
	funData["ABS_AVG"] = {id:'ABS_AVG', value: 'Absolute Average'};
	funData["Average"] = {id:'AVG', value: 'Average'};
	funData["AVG"] = {id:'AVG', value: 'Average'};
	funData["Absolute Sum"] = {id:'ABS_SUM', value: 'Absolute Sum'};
	funData["ABS_SUM"] = {id:'ABS_SUM', value: 'Absolute Sum'};
	funData["Count"] = {id:'COUNT', value: 'Count'};
	funData["COUNT"] = {id:'COUNT', value: 'Count'};
	funData["Count Distinct"] = {id:'COUNT_DISTINCT', value: 'Count Distinct'};
	funData["COUNT_DISTINCT"] = {id:'COUNT_DISTINCT', value: 'Count Distinct'};
	funData["Minimum"] = {id:'MIN', value: 'Minimum'};
	funData["MIN"] = {id:'MIN', value: 'Minimum'};
	funData["Maximum"] = {id:'MAX', value: 'Maximum'};
	funData["MAX"] = {id:'MAX', value: 'Maximum'};
	funData["Sum"] = {id:'SUM', value: 'Sum'};
	funData["SUM"] = {id:'SUM', value: 'Sum'};
	return funData;
}

NodeEditorForComputedFields.prototype.initializeComputedFieldsActions = function() {
	var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;		
	var columnoptions = utils.getFieldNameColumnOptions(columns);
	var computedFieldsTab = this;
	// Adding the new values
	this.modalDiv.find('#add-computed-field-button').click( function() {
		var oTable = $('#computed-fields-table').dataTable();
		var data = {};
		data.Display = "";
		data.SelectedAggregateFunction = "Count";	
		data.Delete = "";
		data.Tooltip = "";
		data.SelectedField = columns[0].fieldName;
		data.FieldOptions = columnoptions;				
		oTable.fnAddData(data);
		oTable.find('tr:last td:first').trigger('click');
		computedFieldsTab.isValid = false;
		computedFieldsTab.modalDiv.find('#treeFinish-ne').attr('disabled','disabled');
	});
}

NodeEditorForComputedFields.prototype.createComputedFieldsTab = function(modalDiv) {
	var nodeEditor = this;
	var oTable = modalDiv.find('#computed-fields-table').dataTable({
		"aoColumns" : [
			{
				"sTitle" : "Display Name",
				"mDataProp" : "Display"	,
                "sWidth": "160px",
                "sClass":"display_name",
			},
			{
				"sTitle" : "Field/Value",
				"mDataProp" : "SelectedField",
                "sWidth": "160px",
			},
			{
				"sTitle" : "Function",
				"mDataProp" : "SelectedAggregateFunction",
                "sWidth": "115px"
			},
			{
				"sTitle" : "Show In Tooltip",						
				"mDataProp" : "Tooltip",
	            "sWidth": "64px",
	            "sClass":"node_computed_tool_tip",
				"fnRender" : function(obj) {
					var sReturn = "";
					if (obj.aData['Tooltip']) {
						sReturn = $('<input></input>').attr('type','checkbox').val('').attr('checked','checked').attr('id', '4')[0].outerHTML;
					} else {
						sReturn = $('<input></input>').attr('type','checkbox').val('').attr('id', '4')[0].outerHTML;
					}
					return sReturn;
				}
			},
			{		
				"sTitle" : "Delete",
				"mDataProp" : "Delete",
                "sWidth": "35px",
                "sClass":"node_computed_delete",
				"fnRender" : function(obj) {
					var sReturn = "";
					sReturn = $('<img></img>').attr('src', '../images/node-delete.png').attr('id', '5')[0].outerHTML;
					return sReturn;
				}
			} ],
		"fnCreatedRow" : function(nRow, aData, iDisplayIndex,iDisplayIndexFull) {
	    	//ADD Listeners
		 	var rowdata = aData;
	    	$(nRow).find("#5").click(function() {
	    		var nTd = this;
	    		if(_.isEqual($(nRow).parent().find('tr').length,1)){
	    			nodeEditor.isValid = true;
	    			nodeEditor.modalDiv.find('#treeFinish-ne').trigger('change');
				}
	    		else{
	    			nodeEditor.isValid = true;
	    			nodeEditor.modalDiv.find('#treeFinish-ne').trigger('change');
					$.each($(nRow).parent().find('tr'),function(){
						if(_.isEmpty($(this).find('td:eq(0)').text()) &&  !_.isEqual($(this)[0],$(nTd).parent().parent()[0]) ){
							nodeEditor.isValid = false;
			    			nodeEditor.modalDiv.find('#treeFinish-ne').trigger('change');
						}
					});
	    		}
	    		nodeEditor.doDeleteComputedField(nRow);
			});	
	    	$(nRow).find('td:eq(0)').live( 'click', function () {
	    		nodeEditor.doEditDisplayName(nRow);
			});
	    	$(nRow).find('td:eq(1)').live( 'click', function () {
	    		nodeEditor.doEditFieldName(nRow, rowdata.FieldOptions);
			});
	    	$(nRow).find('td:eq(2)').live( 'click', function () {
	    		nodeEditor.doEditFunctionName(nRow);
			});
	    	$(nRow).find('td:eq(0)').find('#displayedit').live( 'focusout', function () {
	  		    nodeEditor.doFocusOutDisplayName(this);
	  		    nodeEditor.isValid = true;
	  		  	nodeEditor.modalDiv.find('#treeFinish-ne').trigger('change');
				$.each($(nRow).parent().find('tr'),function(){
					if(_.isEmpty($(this).find('td:eq(0)').text())){
						nodeEditor.isValid = false;
		    			nodeEditor.modalDiv.find('#treeFinish-ne').trigger('change');
					}
				});
			});	
 	  		$(nRow).find('td:eq(1)').find('#computedfieldselect').live( 'focusout', function () {
				nodeEditor.doFocusOutFieldName(this);      
			});
 	  		$(nRow).find('td:eq(2)').find('#functionselect').live( 'focusout', function () {
			    nodeEditor.doFocusOutFunctionName(this);
			});
	      },
	"aaData" : [],
	"sDom" : '<flp><"clear">',
    "sScrollY" : "150px",
	"bPaginate" : false,
    "bAutoWidth": false,
	"aaSorting": []
	});
    $.data($(modalDiv.find('#computed-fields-table'))[0], 'editDialog', this);
}

NodeEditorForComputedFields.prototype.appendPreviousDataInTable = function(modalDiv) {
	var modalDiv = modalDiv;
	var functionObj = this.getFunctionObject();
	var visualization = this.vizDef;
	var nodeName = this.nodeName;
	var nodeDefs = visualization.nodeDefs;
	var attributeDefs = "";
	$.each(nodeDefs, function() {
		if (this.name == nodeName) {
			attributeDefs = this.attributeDefs;
		}
	});
	var oTable = this.oTable;
	var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	var columnoptions = utils.getFieldNameColumnOptions(columns);
	$.each(attributeDefs, function() {
		if (this.kind == "COMPUTED") {
			var data = {};			
			data.Display = this.name;			
			data.Delete = "";
			data.Tooltip = this.includeInTooltip;
			data.SelectedField = this.fieldDef.fieldName;					
			data.SelectedAggregateFunction = functionObj[this.aggregateFunction].value ;
			data.FieldOptions = columnoptions;		
			oTable.fnAddData(data);
		}
	});
}

NodeEditorForComputedFields.prototype.doSave = function(visualization) {
	var modalDiv = this.modalDiv;
	var functionObj = this.getFunctionObject();
	var nodeName = this.nodeName;
	var nodeDefs = visualization.nodeDefs;
	var newAttrDefs = [];
	var fieldDefs = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	if (this.oTable.fnSettings() == null){
		return;
	}
	var nodes = this.oTable.fnGetNodes();
	$.each(nodes, function() {
		var uuid = utils.guidGenerator();
		var includeInTooltip = $(this).find('#4')[0].checked;
		var aggregationFunction = functionObj[($(this).find('td:eq(2)').find('select').length > 0 ? $(this).find('td:eq(2)').find('select').val() : $(this).find('td:eq(2)').text())]['id'];
        var computedNode = $(this).find('td:eq(1)').find('select').length > 0 ? $(this).find('td:eq(1)').find('select').val() : $(this).find('td:eq(1)').text();
        var name = $(this).find('td:eq(0)').find('input').length > 0 ? $(this).find('td:eq(0)').find('input').val() : $(this).find('td:eq(0)').text();		
		var fieldDef = utils.getFieldDef(fieldDefs, computedNode);
		var newAttrDef = {};
		newAttrDef.aggregateFunction = aggregationFunction;
		newAttrDef.class = "csi.server.common.model.attribute.AttributeDef";
		newAttrDef.bySize = false;
		newAttrDef.byTransparency = false;
		newAttrDef.byStatic = true;
		newAttrDef.clientProperties = {};
		newAttrDef.defaultIncludeInTooltip = true;
		newAttrDef.fieldDef = fieldDef;
		newAttrDef.hideEmptyInTooltip = false;
		newAttrDef.includeInTooltip = includeInTooltip;
		newAttrDef.kind = "COMPUTED";
		newAttrDef.name = name;
		newAttrDef.referenceName = null;
		newAttrDef.uuid = uuid;
		newAttrDefs.push(newAttrDef);
	});
	var nodeDefIdx = utils.getNodeDefIdx(nodeDefs, nodeName);
	$.each(nodeDefs[nodeDefIdx].attributeDefs, function() {
		if (this.kind != "COMPUTED") {
			newAttrDefs.push(this);
		}
	});
	visualization.nodeDefs[nodeDefIdx].attributeDefs = newAttrDefs;
	return visualization;
}

NodeEditorForComputedFields.prototype.generateFunctionOptions = function(columnname){	
	var datatype = "";
	var options = "";
	var columns = window.dataview.myData.resultData.meta.modelDef.fieldDefs;
	$.each(columns, function() {
		if (columnname == this.fieldName) {
			datatype = this.valueType.toUpperCase();
		}
	});
	if (datatype == "INTEGER" || datatype == "BIGINT") {
		options = this.createNumberFunctionOptions();
	} else {
		options = $('<option></option>').val('COUNT').append('Count')[0].outerHTML + $('<option></option>').val('COUNT_DISTINCT').append('Count Distinct')[0].outerHTML;
	}
	return options;
}

NodeEditorForComputedFields.prototype.createNumberFunctionOptions = function() {
	var functionObject = this.getFunctionObject();
	var options = "";
	var loopIndex = 0;
	$.each(functionObject,function(){
		if(loopIndex%2 == 0){ //Ignoring alternate values
			options += $('<option></option>').val(this.id).append(this.value)[0].outerHTML;
		}
		loopIndex++;
	});
	return options;
}

NodeEditorForComputedFields.prototype.triggerFocusOut = function(oTable , iDisplayIndex){	
	var length = oTable.find('tr').length;
	if(length > 1){
		for(var i = 0; i < length-1; i++){
			if(i != iDisplayIndex){
				  oTable.find('tbody tr:eq('+i+') td:eq(0) > input[type="text"]').focusout();
				  oTable.find('tbody tr:eq('+i+') td:eq(1)').find('select').focusout();
				  oTable.find('tbody tr:eq('+i+') td:eq(2)').find('select').focusout();
			}
		}
    }
}

NodeEditorForComputedFields.prototype.doDeleteComputedField = function(nRow){
  	var index = this.oTable.fnGetPosition(nRow);
  	var displayname = $(nRow).find('td:eq(0)').find('input').length > 0 ? $(nRow).find('td:eq(0)').find('input').val() : $(nRow).find('td:eq(0)').text();
  	this.modalDiv.find('#scaleNonStatic').find('Option[value="'+displayname+'"]').remove();
  	this.oTable.fnDeleteRow(index);
} 

NodeEditorForComputedFields.prototype.doEditDisplayName = function(nRow){
	var data = $(nRow).find('td:eq(0)')[0].innerHTML;
	if(this.oTable) {	
		var index = this.oTable.fnGetPosition(nRow);
		this.triggerFocusOut(this.oTable, index);
		this.modalDiv.find('#scaleNonStatic').find('Option[value="'+data+'"]').attr("id",index);
	}
	$(nRow).find('td:eq(2)').find('#functionselect').focusout();
	$(nRow).find('td:eq(1)').find('#computedfieldselect').focusout(); 					    		
	if($(nRow).find('#displayedit').length > 0){
		return;
	}
	$(nRow).find('td:eq(0)').html($('<input></input>').attr('type','text').addClass('span2').attr('id','displayedit').val(data));
	$(nRow).find('td:eq(0) > input[type="text"]').focus();
} 

NodeEditorForComputedFields.prototype.doEditFunctionName = function(nRow){
	 if(this.oTable) {
		 var index = this.oTable.fnGetPosition(nRow);
		 this.triggerFocusOut(this.oTable, index);
	 }	
	 $(nRow).find('td:eq(0)').find('#displayedit').focusout();
	 $(nRow).find('td:eq(1)').find('#computedfieldselect').focusout(); 
	 var data = $(nRow).find('td:eq(2)')[0].innerHTML;
	 var columnname = $(nRow).find('td:eq(1)')[0].innerHTML;
	 if($(nRow).find('#functionselect').length > 0){
		 return;
	 }
	 var options = this.generateFunctionOptions(columnname);						
	 $(nRow).find('td:eq(2)').html($('<select></select').attr('id','functionselect').addClass('span2').append(options));													 
	 $(nRow).find('td:eq(2)').find("#functionselect option[value='"+data+"']").attr("selected", "selected");
}

NodeEditorForComputedFields.prototype.doEditFieldName= function(nRow, fieldOptions){
	 if(this.oTable) {
		 var index = this.oTable.fnGetPosition(nRow);
		 this.triggerFocusOut(this.oTable, index);
	 }	
	 $(nRow).find('td:eq(0)').find('#displayedit').focusout();
	 $(nRow).find('td:eq(2)').find('#functionselect').focusout();
	 var data = $(nRow).find('td:eq(1)')[0].innerHTML;							
	 if($(this).find('#computedfieldselect').length > 0){
		 return;
	 }
	 $(nRow).find('td:eq(1)').html($('<select></select').attr('id','computedfieldselect').addClass('span2').append(fieldOptions));													 
	 $(nRow).find('td:eq(1)').find('#computedfieldselect option[value="'+data+'"]').attr("selected", "selected");
}

NodeEditorForComputedFields.prototype.doFocusOutFieldName= function(nTd){
	 if($(nTd).val() != null){
		 if($(nTd).parent()){
		   var index = this.oTable.fnGetPosition($(nTd).parent().parent()[0]);
		   var newval = $(nTd).val();
		   $(nTd).html('');
		   this.oTable.fnUpdate(newval, index, 1);
		 }
	 }
}

NodeEditorForComputedFields.prototype.doFocusOutFunctionName = function(nTd){
	if($(nTd).val() != null){
		if($(nTd).parent()){
		   var index = this.oTable.fnGetPosition($(nTd).parent().parent()[0]);
		   var newval = $(nTd).find("option:selected")[0].innerHTML;
		   $(nTd).html('');
		   this.oTable.fnUpdate(newval, index, 2);
		}
	}
}

NodeEditorForComputedFields.prototype.doFocusOutDisplayName = function(nTd){
	if($(nTd).val() != null){
		if($(nTd).parent()){
			var index = this.oTable.fnGetPosition($(nTd).parent().parent()[0]);
			var newval = $(nTd).val();
			$(nTd).html('');
			this.oTable.fnUpdate(newval, index, 0);
			if(newval != ""){
			   if(this.modalDiv.find('#scaleNonStatic').find('Option[id="'+index+'"]') != []){
				   this.modalDiv.find('#scaleNonStatic').find('Option[id="'+index+'"]').remove(); 
			   }
			   this.modalDiv.find('#scaleNonStatic').append( new Option(newval,newval) );
			   this.modalDiv.find('#scaleNonStatic').find('option[value="'+newval+'"]').attr("id",index);
			}
		}
	} 
}

NodeEditorForComputedFields.prototype.findIsValid = function(){
	return this.isValid;
}